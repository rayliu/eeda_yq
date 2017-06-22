define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 
	'./oceanEdit_charge','./airEdit_charge','./landEdit_charge', './edit_charge_trade', './edit_charge_tour'], function ($, metisMenu) { 

    $(document).ready(function() {
	  $("#breadcrumb_li").text('客户合同');
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
	        if(formRequired>0){
	        	$.scojs_message('客户和合同有效期为必填', $.scojs_message.TYPE_ERROR);
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
	        
	        $.post("/customerContract/save",{params:JSON.stringify(order)},function(data){
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
	        	itemOrder.refleshAirItemTable(data.ID);
	        	itemOrder.refleshLandItemTable(data.ID);
	        },'json').fail(function() {
	            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
	            $('#saveBtn').attr('disabled', false);
	            $.unblockUI();
	        });
	        
	  });

	  $('#orderForm').validate({
	        rules: {
	        	period: {
	        		number:true
	        	}
	        }, 
	        messages:{
	        	period: {
	              
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