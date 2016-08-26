define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','datetimepicker_CN',
    './edit_shipment_table','./edit_shipment_detail','./edit_land_table', './edit_charge_table','./edit_charge_cost_table',
    './edit_air_table', './edit_air_cargoDesc_table', './edit_air_detail','./edit_custom_detail',
    './edit_insurance_detail', './edit_doc_table', './edit_file_upload','./job_order_report'], function ($, metisMenu, template) {
$(document).ready(function() {

	document.title = order_no + ' | ' + document.title;
	$('#menu_order').addClass('active').find('ul').addClass('in');
	
    //------------save
	$('#saveBtn').click(function(e){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
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
        
        $('#saveBtn').attr('disabled', true);
        
        //运输方式checkbox遍历取值
        var transport_type = [];
        $('#transport_type input[type="checkbox"]:checked').each(function(){
        	transport_type.push($(this).val()); 
        });
        
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
        var chinaCustom = itemOrder.buildCustomDetail();
        var hkCustom = itemOrder.buildHkCustomDetail();
        var abroadCustom = itemOrder.buildAbroadCustomDetail();
        //保险
        var insurance_detail=itemOrder.buildInsuranceDetail();
        //费用明细，应收，应付
        var charge_list = itemOrder.buildChargeDetail();
        var chargeCost_list = itemOrder.buildChargeCostDetail();
        //相关文档
        var doc_list = itemOrder.buildDocDetail();
        
        var order={}
        order.id = $('#order_id').val();
        order.plan_order_item_id = $('#plan_order_item_id').val();
        order.customer_id = $('#customer_id').val();
        order.plan_order_no = $('#plan_order_no').val();
        order.type = $('#type').val();
        order.status = $('#status').val()==''?'新建':$('#status').val();
        order.remark = $('#note').val();
        order.transport_type = transport_type.toString();
        order.gross_weight = $("#gross_weight").val();
        order.net_weight = $("#net_weight").val();
        order.volume = $("#volume").val();
        order.pieces = $("#pieces").val();
        order.billing_method = $('#billing_method input[type="radio"]:checked').val();
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
        order.doc_list = doc_list;
        //异步向后台提交数据
        $.post('/jobOrder/save', {params:JSON.stringify(order)}, function(data){
            var order = data;
            if(order.ID>0){
                $("#order_id").val(order.ID);
                $("#order_no").val(order.ORDER_NO);
                $("#creator_name").val(order.CREATOR_NAME);
                $("#create_stamp").val(order.CREATE_STAMP);
                
                $("#custom_id").val(order.CUSTOM.ID);
                $("#china_custom_id").val(order.ABROADCUSTOM.ID);
                $("#hk_custom_id").val(order.HKCUSTOM.ID);
                
                $("#shipment_id").val(order.SHIPMENT.ID);
                $("#insurance_id").val(order.INSURANCE.ID);
                $("#air_id").val(order.AIR.ID);
                
                eeda.contactUrl("edit?id",order.ID);
                $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                $('#saveBtn').attr('disabled', false);
                
                $("#fileuploadSpan").show();
                $("#sendEmail").show();
                $("#oceanPDF").show();
                $("#airPDF").show();
                $("#truckOrderPDF").show();
                
                //异步刷新海运明细表
                itemOrder.refleshOceanTable(order.ID);
                //异步刷新空运明细表
                itemOrder.refleshAirItemTable(order.ID);
                itemOrder.refleshCargoDescTable(order.ID);
                //异步刷新路运明细表
                itemOrder.refleshLandItemTable(order.ID);
                //异步刷新费用明细应收
                itemOrder.refleshChargeTable(order.ID);
                //异步刷新费用明细应付
                itemOrder.refleshCostTable(order.ID);
                
            }else{
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
            }
        },'json').fail(function() {
            $.scojs_message('失败', $.scojs_message.TYPE_ERROR);
            $('#saveBtn').attr('disabled', false);
          });
    	
	});
    
    //创建派车单URL跳转传参
//    $('#create_truckOrder').click(function(){
//    	$(this).attr('disabled', true);
//    	var order_id = $('#order_id').val();
//    	var itemIds=[];
//    	$('#cargo_table input[type="checkbox"]').each(function(){
//    		var checkbox = $(this).prop('checked');
//    		if(checkbox){
//    			var itemId = $(this).parent().parent().attr('id');
//    			itemIds.push(itemId);
//    		}
//    	});
//    	location.href ="/truckOrder/create?order_id="+order_id+"&itemIds="+itemIds;
//    });
    
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
    var checkArray = transport_type.split(",");
    for(var i=0;i<checkArray.length;i++){
	    $('#transport_type input[type="checkbox"]').each(function(){
	        var checkValue=$(this).val();
	        if(checkArray[i]==checkValue){
	        	$(this).attr("checked",true);

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
    
    //取得当前时间
    getTime = function(){
	    var d = new Date();
	    var year = d.getFullYear();
	    var mon = d.getMonth() + 1;
	    var day = d.getDate();
	    var h = d.getHours(); 
	    var m = d.getMinutes(); 
	    var s = d.getSeconds(); 
	    timeStr=year+"-"+(mon<10 ? "0" + mon : mon)+"-"+(day<10 ? "0"+ day : day)+" "+(h<10 ? "0"+ h : h)+":"+(m<10 ? "0" + m : m)+":"+(s<10 ? "0" +s : s);
	    return timeStr;
    }
        
});
});