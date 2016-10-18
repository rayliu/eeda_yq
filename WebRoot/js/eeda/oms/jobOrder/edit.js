define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','datetimepicker_CN', 'jq_blockui',
    './edit_shipment_table','./edit_shipment_detail','./edit_land_table', './edit_charge_table','./edit_cost_table',
    './edit_air_table', './edit_air_cargoDesc_table', './edit_air_detail','./edit_custom_detail',
    './edit_insurance_detail','./edit_party_detail', './edit_doc_table', './edit_file_upload','./job_order_report'], function ($, metisMenu) {
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
        
        
        //运输方式checkbox遍历取值
        var transport_type = [];
        $('#transport_type input[type="checkbox"]:checked').each(function(){
        	transport_type.push($(this).val()); 
        });
        var transport_type_str = transport_type.toString();
        
        //报关类型遍历取值
        var custom_type = [];
        $('#custom_type input[type="checkbox"]:checked').each(function(){
        	custom_type.push($(this).val()); 
        });
        var custom_type_str = custom_type.toString();
        
        //海运
        var shipment_detail = itemOrder.buildShipmentDetail();
        var shipment_item = itemOrder.buildOceanItem();
        //空运
        var air_detail = itemOrder.buildAirDetail();
        var air_item = itemOrder.buildAirItem();
        var air_cargoDesc = itemOrder.buildCargoDescDetail();
        //陆运
        var load_detail = itemOrder.buildLoadItem();
        //报关
        if(custom_type_str.includes("china")){
        	var chinaCustom = itemOrder.buildCustomDetail();
        }
        if(custom_type_str.includes("HK/MAC")){
        	var hkCustom = itemOrder.buildHkCustomDetail();
        }
        if(custom_type_str.includes("abroad")){
        	var abroadCustom = itemOrder.buildAbroadCustomDetail();
        }
        //保险
        var insurance_detail=itemOrder.buildInsuranceDetail();
        //费用明细，应收，应付
        var charge_list = itemOrder.buildChargeDetail();
        var chargeCost_list = itemOrder.buildChargeCostDetail();
        
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
        order.land_export_date =$('#land_export_date').val();
        
//        order.total_chargeRMB = chargeRMB;
//        order.total_chargeUSD = chargeUSD;
//        order.total_profitTotalCharge = profitTotalCharge;
//        order.total_costRMB = costRMB;
//        order.total_costUSD = costUSD;
//        order.total_profitTotalCost = profitTotalCost;
//        order.total_profitRMB = profitRMB;
//        order.total_profitUSD = profitUSD;
//        order.total_profitTotalRMB = profitTotalRMB;

        //海运
        order.shipment_detail = shipment_detail;
        order.shipment_list = shipment_item;
        //空运
        order.air_detail = air_detail;
        order.air_list = air_item;
        order.air_cargoDesc = air_cargoDesc;
        //陆运
        order.land_list = load_detail;
        
        //报关
        order.chinaCustom = chinaCustom;
        order.hkCustom = hkCustom;
        order.abroadCustom = abroadCustom;
        //保险
        order.insurance_detail=insurance_detail;
       //费用明细，应收，应付
        order.charge_list = charge_list;
        order.chargeCost_list = chargeCost_list;
        //相关文档
        order.doc_list = itemOrder.buildDocItem();
       
        //异步向后台提交数据
        $.post('/jobOrder/save', {params:JSON.stringify(order)}, function(data){
            var order = data;
            if(order.ID){
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
                $("#shipment_id").val(order.SHIPMENT.ID);
                $("#insurance_id").val(order.INSURANCE.ID);
                $("#air_id").val(order.AIR.ID);
                
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
    
});
});