define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'pageguide',
	'./oceanEdit_charge','./airEdit_charge','./landEdit_charge', './edit_charge_trade', './edit_charge_tour'], function ($, metisMenu) { 

    $(document).ready(function() {

    	tl.pg.init({
	        pg_caption: '本页教程'
	    });
	    
	  //主表构造json
	  var buildOrder = function(){
		  var item = {};
		  item.id = $('#contract_id').val();
		  var orderForm = $('#orderForm input,#orderForm select,#orderForm textarea');
		  for(var i= 0;i< orderForm.length;i++){
			  var name = orderForm[i].id;
			  var value = orderForm[i].value;
			  if(name){
				  item[name]= value;
			  }
		  }
		  return item;
	  }
      //提交保存
	  $('#saveBtn').click(function(){
		//提交前，校验数据
	        var formRequired = 0;
	        $('form').each(function(){
	        	if(!$(this).valid()){
	        		formRequired++;
	            }
	        })
	        
	        var errorlength = $("[class=error_span]").length;
	        var loc_id = $($(".error_span").get(0)).parent().parent().parent().parent().attr('id');
	        if(formRequired>0){
	        	var customer = $("#customer_id_input").val();
	        	var contract_begin_time = $("#contract_begin_time").val();
	        	var contract_end_time = $("#contract_end_time").val();
	        	if(customer==""||contract_begin_time==""||contract_end_time==""){
	        		$.scojs_message('客户或者合同有效期是必填字段', $.scojs_message.TYPE_ERROR);
	        		return;
	        	}else{
	        		$.scojs_message('单据存在填写格式错误字段未处理', $.scojs_message.TYPE_ERROR);
	        		return;
	        	}
	        }
	        if(errorlength>0){
	        	$.scojs_message('单据存在填写格式错误字段未处理', $.scojs_message.TYPE_ERROR);
	        	location.hash="#"+loc_id;
	        	return;
	        }
	        
	        var order={};
	        order = buildOrder();
	        order.itemOceanList = itemOrder.buildOceanItem();
	        order.itemAirList = itemOrder.buildAirItem();
	        order.itemLandList = itemOrder.buildLandItem();
	        order.itemTradeList = itemOrder.buildTradeItem();
	        order.itemTourList = itemOrder.buildTourItem();
	        
	        order.itemOceanLocList = itemOrder.buildOceanLocItem();
	        order.itemAirLocList = itemOrder.buildAirLocItem();
	        order.itemLandLocList = itemOrder.buildLandLocItem();
	        order.itemTourLocList = itemOrder.buildTourLocItem();
	        $("#saveBtn").attr("disabled",true);
	        
	        $.post("/supplierContract/save",{params:JSON.stringify(order)},function(data){
	        	if(data.ID != null && data.ID != ""){
	        		$('#contract_no').val(data.CONTRACT_NO);
	        		$('#status').val(data.STATUS);
	        		$('#create_date').val(data.CREATE_DATE);
	     			eeda.contactUrl("edit?id",data.ID);
	     			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	     			$("#contract_id").val(data.ID);
	     			$("#saveBtn").attr("disabled",false);
	     		}else{
	     			$.scojs_message('数据有误', $.scojs_message.TYPE_ERROR);
	     			$("#saveBtn").attr("disabled",false);
	     		}
	        	itemOrder.refleshOceanLocTable(data.ID);
	        	itemOrder.refleshOceanItemTable(data.ID);
	        	
	        	itemOrder.refleshAirLocTable(data.ID);
	        	itemOrder.refleshAirItemTable(data.ID);
	        	
	        	itemOrder.refleshLandLocTable(data.ID);
	        	itemOrder.refleshLandItemTable(data.ID);
	        	
	        	itemOrder.refleshTourLocTable(data.ID);
	        	itemOrder.refleshTourItemTable(data.ID);
	        	
	        },'json').fail(function() {
	            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
	            $('#saveBtn').attr('disabled', false);
	            $.unblockUI();
	        });
	        
	  });
	  
	  $('#orderForm').validate({
	        rules: {
	        	period: {
	        		number:true,
	        		maxlength:15
	        	},
	        	special_item: {
			    	maxlength:500
			  	},
			  	remark: {
			  		maxlength:2450
			  	}
	        }, 
	        messages:{
	        	period:{
	        		maxlength:$.validator.format("最多输入{0}个的数字"),
	        	}
	        },
	        highlight: function(element) {
	            $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
	        },
	        success: function(element) {
	            element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
	        }
	    });
    });
});