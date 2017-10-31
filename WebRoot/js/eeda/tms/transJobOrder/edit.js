define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','datetimepicker_CN', 'jq_blockui','pageguide',
    './edit_land_table','./edit_land_bulk_cargo_table', './edit_charge_table','./edit_cost_table','./edit_party_detail', './edit_doc_table', './edit_file_upload','./job_order_report',
    './edit_arap_detail'], function ($, metisMenu) {
$(document).ready(function() {
	  tl.pg.init({
          pg_caption: '本页教程'
      });
	var container_no=$('#container_no').val();

	 //柜号限制输入位为11位数，
	$("#orderForm").validate({
	    rules: {
		      container_no: {
		    	  rangelength: [11,11]
		      },
		      so_no: {
		    	  maxlength:100
		      },
		      seal_no: {
		    	  maxlength:100
		      },
		      lading_no: {
		    	  maxlength:100
		      },
		      contract_no: {
		    	  maxlength:200
		      },
		      customer_salesman: {
		    	  maxlength:100
		      },
		      toca_no: {
		    	  maxlength:100
		      },
		      remark: {
		    	  maxlength:255
		      }
	    }
	});
	 //按钮状态
	var id = $('#order_id').val();
	var status = $('#status').val();
    if(id==''){
    	$('#confirmCompleted').attr('disabled', true);
    }else{
		if(status=='已完成'){
			$('#confirmCompleted').attr('disabled', true);
			$('#saveBtn').attr('disabled', true);
		}
    }
    
    //柜货、散货显示控制
	var type_controll= function(type_required){
	    	if(type_required=='出口散货'||type_required=='进口散货'){
	    		
	    		$('#cabinet_type').val('');
	    		
	        	var take_wharf = $('#take_wharf_input');
	        	take_wharf.attr('required',false);
	        	take_wharf.parent().find('span').text('');
	        	
	        	var back_wharf = $('#back_wharf_input');
	        	back_wharf.attr('required',false);
	        	back_wharf.parent().find('span').text('');
	        	
	        	var head_carrier = $('#head_carrier_input');
	        	head_carrier.attr('required',false);
	        	head_carrier.parent().find('span').text('');
	        	
	        	var container_no = $('#container_no');
	        	container_no.attr('required',false);
	        	container_no.parent().find('span').text('');
	        	
	        	var so_no = $('#so_no');
	        	so_no.attr('required',false);
	        	so_no.parent().find('span').text('');
	        	
	        	$('#land_shipmentTab').parent().hide();
	        	$('#land_shipmentDetail').hide("active");
	        	
	        	$('#land_bulk_cargoTab').parent().show();
	        	$('#land_bulk_cargoTab').parent().addClass("active");
	        	$('#land_bulk_cargoDetail').addClass("active");
	        }else{
	        	if(!$('#cabinet_type').val()){
	        		$('#cabinet_type').val('40HQ');
	        	}

	        	var take_wharf = $('#take_wharf_input');
	        	take_wharf.attr('required',true);
	        	take_wharf.parent().find('span').text('*');
	        	
	        	
	        	
	        	var back_wharf = $('#back_wharf_input');
	        	back_wharf.attr('required',true);
	        	back_wharf.parent().find('span').text('*');
	        	
	        	var head_carrier = $('#head_carrier_input');
	        	head_carrier.attr('required',true);
	        	head_carrier.parent().find('span').text('*');
	        	
	        	var container_no = $('#container_no');
	        	container_no.attr('required',true);
	        	container_no.parent().find('span').text('*');
	        	
	        	var so_no = $('#so_no');
	        	so_no.attr('required',true);
	        	so_no.parent().find('span').text('*');
	        	
	        	$('#land_shipmentTab').parent().show();
	        	$('#land_shipmentTab').parent().addClass("active");
	        	$('#land_shipmentDetail').show("active");
	        	
	        	$('#land_bulk_cargoTab').parent().hide();
	        	$('#land_bulk_cargoDetail').removeClass("active");
		    }
	    }
	
	var type_required = $('#type').val();
    type_controll(type_required);
    
    $('#type').click(function(){
    	type_required = $('#type').val();
    	 type_controll(type_required);
    });
     
	
	//锁单
	$('#confirmCompleted').click(function(){
        $.blockUI({ 
            message: '<h4><img src="/images/loading.gif" style="height: 20px; margin-top: -3px;"/> 正在提交...</h4>' 
        });
		$('#confirmCompleted').attr('disabled', true);
		id = $('#order_id').val();
		var action = 'lock';
		$.post('/transJobOrder/lockRelease', {id:id,action:action}, function(data){
	            $.scojs_message('锁单成功', $.scojs_message.TYPE_OK);
	            $('#status').val("已完成");
	            $('#saveBtn').attr('disabled', true);
	            $.unblockUI();
	    },'json').fail(function() {
	        $.scojs_message('锁单失败', $.scojs_message.TYPE_ERROR);
	        $('#confirmCompleted').attr('disabled', false);
            $.unblockUI();
	   });
	});
	
	//锁单状态控制
	if($("#status").val()=='已完成'){
		$("#add_land").attr("disabled",true); 
		$("#add_charge").attr("disabled",true);
		$("#add_charge_cost").attr("disabled",true);
		$(".delete").attr("disabled",true);
		$(".chargeConfirm_btn").attr("disabled",true);
		$(".chargeCancelConfirm_btn").attr("disabled",true);
		$(".costConfirm_btn").attr("disabled",true);
		$(".costCancelConfirm_btn").attr("disabled",true);
		$("#fileuploadSpan").attr("disabled",true);
		$("#fileupload").attr("disabled",true);
		$("#sendEmail").attr("disabled",true);
	}else{
		
	}
	
	/*$("input[name='CLOSING_DATE']").on('changeDate', function(ev){
		if($("input[name='CLOSING_DATE']").val()){
	    	$("#charge_time").val($("input[name='CLOSING_DATE']").val());
	    }
     });*/
	
	
	
    //------------save
	$('#saveBtn').click(function(e){
		//提交前，校验数据
        var formRequired = 0;
        $('form').each(function(){
        	if(!$(this).valid()){
        		formRequired++;
            }
        })
        errorlength = $("[class=error_span]").length;
        var loc_id = $($(".error_span").get(0)).parent().parent().parent().parent().attr('id');
        
        if(formRequired>0||errorlength>0){
        	$.scojs_message('单据存在填写格式错误字段未处理', $.scojs_message.TYPE_ERROR);
        	location.hash="#"+loc_id;
        	return;
        }
    	
        //费用的结算公司必填
        var error_data=0;
        $('#charge_table [name=SP_ID],#charge_table [name=CURRENCY_ID]').each(function(index,item){
            if(!item.value){
                error_data++;
                if(item.name=='SP_ID'){
                    $.scojs_message('费用明细;应收信息表第'+(Math.ceil((parseInt(index)+1)/2))+'行的结算公司还没有填好', $.scojs_message.TYPE_ERROR);
                     // return;
                }else if(item.name=='CURRENCY_ID'){
                    $.scojs_message('费用明细;应收信息表第'+(Math.ceil((parseInt(index)+1)/2))+'行的币制为必填', $.scojs_message.TYPE_ERROR);
                }
            }
        });
        $('#cost_table [name=CURRENCY_ID]').each(function(index,item){
            if(!item.value){
                error_data++;
               if(item.name=='CURRENCY_ID'){
                    $.scojs_message('费用明细;应付信息表第'+(Math.ceil((parseInt(index)+1)/2))+'行的币制为必填', $.scojs_message.TYPE_ERROR);
                }
            }
        });
        if(error_data>0)return;

        $.blockUI({ 
            message: '<h4><img src="/images/loading.gif" style="height: 20px; margin-top: -3px;"/> 正在提交...</h4>' 
        });
        $('#saveBtn').attr('disabled', true);
        
        //这张工作单的应收应付汇总存数据进数据库
        var chargeRMB =$('#profit_table').find('[name=chargeRMB]').text().replace('CNY ','');
        var chargeUSD =$('#profit_table').find('[name=chargeUSD]').text().replace('USD ','');
        var profitTotalCharge =$('#profit_table').find('[name=profitTotalCharge]').text().replace('CNY ','');
        var costRMB =$('#profit_table').find('[name=costRMB]').text().replace('CNY ','');
        var costUSD =$('#profit_table').find('[name=costUSD]').text().replace('USD ','');
        var profitTotalCost =$('#profit_table').find('[name=profitTotalCost]').text().replace('CNY ','');
        var profitRMB =$('#profit_table').find('[name=profitRMB]').text().replace('CNY ','');
        var profitUSD =$('#profit_table').find('[name=profitUSD]').text().replace('USD ','');
        var profitTotalRMB =$('#profit_table').find('[name=profitTotalRMB]').text().replace('CNY ','');
        
        

        var transport_type_str = "land";
        

        
        var order={}
        order.id = $('#order_id').val();
        order.plan_order_id = $('#plan_order_id').val();
        order.plan_order_item_id = $('#plan_order_item_id').val();
        order.customer_id = $('#customer_id').val();
        order.plan_order_no = $('#plan_order_no').val();
        order.type = $('#type').val();
        order.status = $('#status').val()==''?'新建':$('#status').val();
        order.remark = $('#remark').val();
        order.container_no = $("#container_no").val();
        order.so_no = $("#so_no").val();
        order.seal_no = $("#seal_no").val();
        order.lading_no = $("#lading_no").val();
        order.cabinet_type = $("#cabinet_type").val();
        order.head_carrier = $("#head_carrier").val();
        order.carriage_fee = $("#carriage_fee").val();
        order.bill_fee = $("#bill_fee").val();
        order.trans_clause = $("#trans_clause").val();
        order.trade_type = $("#trade_type").val();
        order.land_export_date =$('#land_export_date').val();
        order.take_wharf =$('#take_wharf').val();
        order.back_wharf =$('#back_wharf').val();
        order.take_stamp =$('#take_stamp').val();
        order.land_export_stamp =$('#land_export_stamp').val();
        order.contract_no =$('#contract_no').val();
        order.customer_salesman =$('#customer_salesman').val();
        order.transport_type = transport_type_str;
        order.toca_no = $('#toca_no').val();
        order.cross_border_travel = $('#cross_border_travel').val();      
        order.charge_time = $("#charge_time").val();
        
        if(transport_type_str.indexOf('land')>-1){
	        //陆运
	        order.land_list = itemOrder.buildLoadItem();//柜货
	        order.land_bulk_list = itemOrder.buildLoadBulkItem();//散货
        }

        //费用明细，应收，应付
        order.charge_list = itemOrder.buildChargeDetail();
        order.chargeCost_list = itemOrder.buildChargeCostDetail();
        order.charge_template = itemOrder.buildChargeTemplate();
        order.cost_template = itemOrder.buildCostTemplate();
        order.allCharge_template = itemOrder.buildAllChargeTemplate();
        order.allCost_template = itemOrder.buildAllCostTemplate();
        //相关文档
        order.doc_list = eeda.buildTableDetail("doc_table","");
       
        //异步向后台提交数据
        $.post('/transJobOrder/save', {params:JSON.stringify(order)}, function(data){
            var order = data;
            if(order.ID){
            	//控制报关申请单按钮
            	$('#custom_type input[type="checkbox"]:checked').each(function(){
                	if($(this).val()=="china"){
                		custom_type='china';
                	};
                });
            	eeda.contactUrl("edit?id",order.ID);
            	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
            	$('#saveBtn').attr('disabled', false);
            	$('#confirmCompleted').attr('disabled', false);
                $('#order_id').val(order.ID);
                $('#status').val(order.STATUS);
                $("#order_no").val(order.ORDER_NO);
                $("#creator_name").val(order.CREATOR_NAME);
                $("#create_stamp").val(order.CREATE_STAMP);
                if(order.CUSTOM){
                	$("#custom_id").val(order.CUSTOM.ID);
                }
                if(order.ABROADCUSTOM){
                	$("#abroad_custom_id").val(order.ABROADCUSTOM.ID);
                }
                if(order.HKCUSTOM){
                	$("#hk_custom_id").val(order.HKCUSTOM.ID);
                }
                if(order.CUSTOMSELF){
                	$("#customSelf_id").val(order.CUSTOMSELF.ID);
                }

                if(order.INSURANCE){
                	$("#insurance_id").val(order.INSURANCE.ID);
                }
                if(order.AIR){
                	$("#air_id").val(order.AIR.ID);
                }
                if(order.TRADE){
                	$("#trade_id").val(order.TRADE.ID);
                }
                
                $("#fileuploadSpan").show();
                $("#sendEmail").show();
                
                //异步刷新明细表
                
               
                itemOrder.refleshLandItemTable(order.ID);
                itemOrder.refleshLandBulkItemTable(order.ID);
                itemOrder.refleshChargeTable(order.ID);
                itemOrder.refleshCostTable(order.ID);
                $.unblockUI();
            }else{
            	if(order.ERR_MSG){
            		 $.scojs_message(order.ERR_MSG, $.scojs_message.TYPE_ERROR);
            	}else{
            		 $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
            	}
                $('#saveBtn').attr('disabled', false);
                $.unblockUI();
            }
        },'json').fail(function() {
            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
            $('#saveBtn').attr('disabled', false);
            $.unblockUI();
        });
    	
	});
	
	
	//更新结算日期
	$('#updateChaegeTime').click(function(){
		$.post('/transJobOrder/updateChageTimeSameASClosingDate',{},function(data){
			$.scojs_message('更新成功', $.scojs_message.TYPE_OK);
		},'json').fail(function(){
			 $.scojs_message('失败', $.scojs_message.TYPE_ERROR);
		});
	});
	
	
    
    $('#take_wharf_list').on('mousedown', '.fromLocationItem', function(e){
        $('#back_wharf_input').val($(this).text());
        $('#back_wharf').val($(this).attr('dock_id'));
    });
    $('#back_wharf_list').on('mousedown', '.fromLocationItem', function(e){
        $('#land_table tr:eq(2)').find('[name=delivery_address]').val($(this).text());
    });
});
});