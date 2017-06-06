define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','datetimepicker_CN', 'jq_blockui',
    './edit_land_table', './edit_charge_table','./edit_cost_table','./edit_party_detail', './edit_doc_table', './edit_file_upload','./job_order_report',
    './edit_arap_detail'], function ($, metisMenu) {
$(document).ready(function() {
	
	var container_no=$('#container_no').val();
	document.title = container_no + ' | ' + document.title;
	$('#breadcrumb_li').text('托运工作单');
	 //柜号限制输入位为11位数，
	$("#orderForm").validate({
	    rules: {
		      container_no: {
		    	  rangelength: [11,11]
		      },
              take_wharf_input: {
                  required: true
              },
              back_wharf_input: {
                  required: true
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
     
	
	//已完成工作单确认
	$('#confirmCompleted').click(function(){
        $.blockUI({ 
            message: '<h4><img src="/images/loading.gif" style="height: 20px; margin-top: -3px;"/> 正在提交...</h4>' 
        });
		$('#confirmCompleted').attr('disabled', true);
		id = $('#order_id').val();
		$.post('/jobOrder/confirmCompleted', {id:id}, function(data){
	            $.scojs_message('确认成功', $.scojs_message.TYPE_OK);
	            $('#saveBtn').attr('disabled', true);
	            $.unblockUI();
	    },'json').fail(function() {
	        $.scojs_message('确认失败', $.scojs_message.TYPE_ERROR);
	        $('#confirmCompleted').attr('disabled', false);
            $.unblockUI();
	   });
	})

	
    //------------save
	$('#saveBtn').click(function(e){
        //提交前，校验数据
    	if(!$('#orderForm').valid()){
    		$.scojs_message('有必填字段未填', $.scojs_message.TYPE_ERROR);
    		return false;
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
        
       
        
        
        if(transport_type_str.indexOf('land')>-1){
	        //陆运
	        order.land_list = itemOrder.buildLoadItem();
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
                $("#oceanPDF").show();
                $("#airPDF").show();
                $("#truckOrderPDF").show();
                
                //异步刷新明细表
                
               
                itemOrder.refleshLandItemTable(order.ID);
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
    
    $('#take_wharf_list').on('mousedown', '.fromLocationItem', function(e){
        $('#back_wharf_input').val($(this).text());
        $('#back_wharf').val($(this).attr('dock_id'));
    });
    $('#back_wharf_list').on('mousedown', '.fromLocationItem', function(e){
        $('#land_table tr:eq(2)').find('[name=delivery_address]').val($(this).text());
    });
});
});