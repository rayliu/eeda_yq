define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','datetimepicker_CN', 'jq_blockui',
    './edit_shipment_table','./edit_shipment_detail','./edit_land_table', './edit_charge_table','./edit_cost_table',
    './edit_air_table', './edit_air_cargoDesc_table', './edit_air_detail','./edit_custom_detail',
    './edit_express_detail','./edit_insurance_detail','./edit_party_detail', './edit_doc_table', './edit_file_upload','./job_order_report',
    './edit_trade_cost_table', './edit_trade_charge_sale_table', './edit_trade_charge_service_table','./edit_trade_detail',
    './edit_custom_china_self_table', './edit_custom_doc_table','./edit_land_charge_table'], function ($, metisMenu) {
$(document).ready(function() {

	document.title = order_no + ' | ' + document.title;
	
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
        var formRequired = 0;
        $('form').each(function(){
        	if(!$(this).valid()){
        		formRequired++;
            }
        })
        if(formRequired>0){
        	$.scojs_message('客户和出货时间为必填字段', $.scojs_message.TYPE_ERROR);
        	return;
        }
        //费用的结算公司必填
        var sp = 0;
        $('#chargeDetail [name=SP_ID]').each(function(){
        	if(this.value==''){
        		sp++;
        	}
        })
        if(sp>0){
        	$.scojs_message('费用明细里，新增的条目中，第'+sp+'行的结算公司未填好', $.scojs_message.TYPE_ERROR);
    		return;
        }
        
        var rate=0;
        $('#chargeDetail [name=exchange_currency_id_input]').each(function(){
        	if(this.value!=''&&$(this).parent().parent().parent().find('[name=exchange_currency_rate]').val()==''){
        		rate++;
        	}
        })
        if(rate>0){
        	$.scojs_message('费用明细里，新增的条目中，第'+rate+'行的结算汇率未填好', $.scojs_message.TYPE_ERROR);
    		return;
        }
        
        
 
        //币制为必填字段
        var CURRENCY_ID = 0;
        $('#chargeDetail [name=CURRENCY_ID]').each(function(){
        	if(this.value==''){
        		CURRENCY_ID++;
        	}
        })
        if(CURRENCY_ID>0){
        	$.scojs_message('费用明细里的币制为必填', $.scojs_message.TYPE_ERROR);
        	return;
        }
        
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
        
        
        //服务项目checkbox遍历取值
        var transport_type = [];
        $('#transport_type input[type="checkbox"]:checked').each(function(){
        	transport_type.push($(this).val()); 
        });
        var transport_type_str = transport_type.toString();
        
        //报关类型遍历取值
        var  this_custom_type = [];
        $('#custom_type input[type="checkbox"]:checked').each(function(){
        	this_custom_type.push($(this).val()); 
        });
        var custom_type_str = this_custom_type.toString();
        
        //自理报关还是委托报关
        var entrust_or_self_custom_str = $('#entrust_or_self_custom input[type="radio"]:checked').val();
        
        var order={}
        order.id = $('#order_id').val();
        order.plan_order_id = $('#plan_order_id').val();
        order.plan_order_item_id = $('#plan_order_item_id').val();
        order.customer_id = $('#customer_id').val();
        order.plan_order_no = $('#plan_order_no').val();
        order.type = $('#type').val();
        order.status = $('#status').val()==''?'新建':$('#status').val();
        order.remark = $('#note').val();
        order.transport_type = transport_type_str;
        order.gross_weight = $("#gross_weight").val();
        order.net_weight = $("#net_weight").val();
        order.volume = $("#volume").val();
        order.pieces = $("#pieces").val();
        order.billing_method = $('#billing_method input[type="radio"]:checked').val();
        order.ref_no = $("#ref_no").val();
        order.trans_clause = $("#trans_clause").val();
        order.trade_type = $("#trade_type").val();
        order.land_export_date = $('#land_export_date').val();
        order.order_export_date = $('#order_export_date').val();
        //报关类型
        order.custom_type = custom_type_str;
        //自理报关还是委托报关
        order.entrust_or_self_custom = entrust_or_self_custom_str;

        if(transport_type_str.indexOf('ocean')>-1){
	        //海运
	        order.shipment_detail = itemOrder.buildShipmentDetail();
	        order.shipment_list = itemOrder.buildOceanItem();
        }
        if(transport_type_str.indexOf('express')>-1){
	        //快递
	        order.express_detail = itemOrder.buildExpressDetail();
        }
        if(transport_type_str.indexOf('air')>-1){
	        //空运
	        order.air_detail = itemOrder.buildAirDetail();
	        order.air_list = itemOrder.buildAirItem();
	        order.air_cargoDesc = itemOrder.buildCargoDescDetail();
        }
        if(transport_type_str.indexOf('land')>-1){
	        //陆运
	        order.land_list = itemOrder.buildLoadItem();
        }
        if(transport_type_str.indexOf('custom')>-1){
	        //报关
	        if(custom_type_str.indexOf('china')>-1){
	        	if(entrust_or_self_custom_str.indexOf('self_custom')>-1){
			        order.chinaCustom_self_item = itemOrder.buildCustomSelfItem();
	        	}
	        	if(entrust_or_self_custom_str.indexOf('entrust_custom')>-1){
	        		order.chinaCustom = itemOrder.buildCustomDetail();
	        	}
	        }
	        if(custom_type_str.indexOf('HK/MAC')>-1){
	        	order.hkCustom = itemOrder.buildHkCustomDetail();
	        }
	        if(custom_type_str.indexOf('abroad')>-1){
	        	order.abroadCustom = itemOrder.buildAbroadCustomDetail();
	        }
        }
        if(transport_type_str.indexOf('insurance')>-1){
	        //保险
	        order.insurance_detail = itemOrder.buildInsuranceDetail();
        }
        
        if(transport_type_str.indexOf('trade')>-1){
	        //贸易
	        order.trade_detail = itemOrder.buildTradeDetail();
	        order.trade_cost = itemOrder.buildTradeCostItem();
	        order.trade_service = itemOrder.buildTradeServiceItem();
	        order.trade_sale = itemOrder.buildTradeSaleItem();
        }
        
        //费用明细，应收，应付
        order.charge_list = itemOrder.buildChargeDetail();
        order.chargeCost_list = itemOrder.buildChargeCostDetail();
        //相关文档
        order.doc_list = eeda.buildTableDetail("doc_table","");
       
        //异步向后台提交数据
        $.post('/jobOrder/save', {params:JSON.stringify(order)}, function(data){
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
                $("#order_id").val(order.ID);
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
                if(order.SHIPMENT){
                	$("#shipment_id").val(order.SHIPMENT.ID);
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
                if(order.EXPRESS){
                	$("#express_id").val(order.EXPRESS.ID);
                }
                
                $("#fileuploadSpan").show();
                $("#sendEmail").show();
                $("#oceanPDF").show();
                $("#airPDF").show();
                $("#truckOrderPDF").show();
                
                //异步刷新明细表
                itemOrder.refleshOceanTable(order.ID);
                itemOrder.refleshAirItemTable(order.ID);
                itemOrder.refleshCargoDescTable(order.ID);
                itemOrder.refleshLandItemTable(order.ID);
                itemOrder.refleshChargeTable(order.ID);
                itemOrder.refleshCostTable(order.ID);
                itemOrder.refleshTradeCostItemTable(order.ID);
                itemOrder.refleshTradeServiceItemTable(order.ID);
                itemOrder.refleshTradeSaleItemTable(order.ID);
                itemOrder.refleshCustomChinaSelfItemTable(order.ID);
                $.unblockUI();
            }else{
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
                $.unblockUI();
            }
        },'json').fail(function() {
            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
            $('#saveBtn').attr('disabled', false);
            $.unblockUI();
        });
    	
	});
    
    var showServiceTab=function(service){
        switch (service){
            case 'ocean':
                $('#oceanDetailTab').show();
                break;
            case 'air':
                $('#airDetailTab').show();
                break;
            case 'land':
                $('#domesticDetailTab').show();
                break;
            case 'custom':
                $('#customDetailTab').show();
                break;
            case 'insurance':
                $('#insurDetailTab').show();
                break;
            case 'trade':
                $('#tradeDetailTab').show();
                break;
            case 'express':
                $('#expressDetailTab').show();
                break;
        }
    };

    var hideServiceTab=function(service){
        switch (service){
            case 'ocean':
                $('#oceanDetailTab').hide();
                break;
            case 'air':
                $('#airDetailTab').hide();
                break;
            case 'land':
                $('#domesticDetailTab').hide();
                break;
            case 'custom':
                $('#customDetailTab').hide();
                break;
            case 'insurance':
                $('#insurDetailTab').hide();
                break;
            case 'trade':
                $('#tradeDetailTab').hide();
                break;
            case 'express':
                $('#expressDetailTab').hide();
                break;
        }
    };

    $('#transport_type input[type="checkbox"]').change(function(){
        var checkValue=$(this).val();
        if($(this).prop('checked')){
            showServiceTab(checkValue);
        }else{
            hideServiceTab(checkValue);
        }
    });
    //服务项目checkbox回显,transport_type是用js拿值
    var checkArray = transport_type_hidden.split(",");
    for(var i=0;i<checkArray.length;i++){
	    $('#transport_type input[type="checkbox"]').each(function(){
	        var checkValue=$(this).val();
	        if(checkArray[i]==checkValue){
	        	this.checked = true;

                showServiceTab(checkValue);
	        }
	    })
    }
    
    //放货方式radio回显
    var radioVal = $('#hidden_billing_method').val();
    $('#billing_method input[type="radio"]').each(function(){
    	var checkValue = $(this).val();
    	if(radioVal==checkValue){
    		$(this).attr("checked",true);
    	}
    });
    
    //------------------费用明细
    $('#collapseArapInfo').on('show.bs.collapse', function () {
        $('#collapseArapIcon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
      });
      $('#collapseArapInfo').on('hide.bs.collapse', function () {
        $('#collapseArapIcon').removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
      });
      
      $('.deleteArapTemplate').click(function(e) {
      	$(this).attr('disabled', true);
      	e.preventDefault();
      	var li = $(this).parent().parent();
      	var id = li.attr('id');
      	$.post('/jobOrder/deleteArapTemplate', {id:id}, function(data){
      		$.scojs_message('删除成功', $.scojs_message.TYPE_OK);
      		$(this).attr('disabled', false);
      		li.css("display","none");
      	},'json').fail(function() {
      		$(this).attr('disabled', false);
              $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
          });
      })
    
    
    
});
});