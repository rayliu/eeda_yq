define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','datetimepicker_CN',
    './edit_shipment_table','./edit_shipment_detail','./edit_land_table', './edit_charge_table','./edit_cost_table',
    './edit_air_table', './edit_air_cargoDesc_table', './edit_air_detail','./edit_custom_detail','./edit_custom_table',
    './edit_doc_table', './edit_file_upload'], function ($, metisMenu) {
$(document).ready(function() {

	document.title = order_no + ' | ' + document.title;
	$('#menu_order').addClass('active').find('ul').addClass('in');
	
	$('#confirmCustomDetailBtn').click(function(e){
		$('#returnCustomOrderExportModal').click()
	})
	
	$('#customDetailExportForm input[name="type"]').click(function(){
		if($(this).val()=="export"){
			$($('#export_port').parent().find("label")).text("出口口岸");
			$($('#custom_export_date').parent().find("label")).text("出口日期");
		}else{
			$($('#export_port').parent().find("label")).text("进口口岸");
			$($('#custom_export_date').parent().find("label")).text("进口日期");
		}
	})
	
	$('#customer_id_input').keyup(function(e){
		if($(this).val()==""){
			$('#contacts').val("");
			$('#contacts_phone').val("");
		}	
	})
	$('#customer_id_list').on('click','li',function(e){
		var id = $('#customer_id').val();
		$.get('/customJobOrder/searchContacts', {id:id}, function(data){
			$('#contacts').val(data.CONTACTS);
			$('#contacts_phone').val(data.PHONE);
		})
	})
    //------------save
	$('#saveBtn').click(function(){
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
        var service_items = [];
        $('#service_items input[type="checkbox"]:checked').each(function(){
        	service_items.push($(this).val()); 
        });
        var service_items_str = service_items.toString();
        
        var order={}
        order.id = $('#order_id').val();
        order.plan_order_no = $('#plan_order_no').val();
        order.ref_no = $("#ref_no").val();
        order.type = $('#type').val();
        order.service_items = service_items_str;
        order.customer_id = $('#customer_id').val();
        order.contacts = $('#contacts').val();
        order.contacts_phone = $('#contacts_phone').val();
        order.status = $('#status').val()==''?'新建':$('#status').val();
        order.remark = $('#remark').val();
        order.gross_weight = $("#gross_weight").val();
        order.net_weight = $("#net_weight").val();
        order.volume = $("#volume").val();
        order.pieces = $("#pieces").val();
        order.billing_method = $('#billing_method input[type="radio"]:checked').val();
        order.trans_clause = $("#trans_clause").val();
        order.trade_type = $("#trade_type").val();

        
        //海运
        order.shipment_detail = itemOrder.buildShipmentDetail();
        order.shipment_item = itemOrder.buildShipmentItem();
        //空运
        order.air_detail = itemOrder.buildAirDetail();
        order.air_item = itemOrder.buildAirItem();
        order.air_cargoDescItem = itemOrder.buildCargoDescItem();
        //陆运
        order.load_item = itemOrder.buildLoadItem();
        //报关
        order.custom_detail = itemOrder.buildCustomDetail();
        order.custom_item = itemOrder.buildCustomItem();
        //费用明细，应收，应付
        order.charge_item = itemOrder.buildChargeDetail();
        order.cost_item = itemOrder.buildCostDetail();
       
        //异步向后台提交数据
        $.post('/customJobOrder/save', {params:JSON.stringify(order)}, function(data){
            var order = data;
            if(order.ID){
            	eeda.contactUrl("edit?id",order.ID);
            	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
            	$('#saveBtn').attr('disabled', false);
                $("#order_id").val(order.ID);
                $("#order_no").val(order.ORDER_NO);
                $("#creator_name").val(order.CREATOR_NAME);
                $("#create_stamp").val(order.CREATE_STAMP);
                $("#shipment_id").val(order.SHIPMENT.ID);
                $("#custom_id").val(order.CUSTOM.ID);
                
                $("#fileuploadSpan").show();
                $("#sendEmail").show();
                //异步刷新明细表
                itemOrder.refleshOceanTable(order.ID);
                itemOrder.refleshAirItemTable(order.ID);
                itemOrder.refleshCargoDescTable(order.ID);
                itemOrder.refleshLandItemTable(order.ID);
                itemOrder.refleshChargeTable(order.ID);
                itemOrder.refleshCostTable(order.ID);
                itemOrder.refleshCustomItemTable(order.ID);
                itemOrder.refleshCustomTable(order.ID);
                
            }else{
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
            }
        },'json').fail(function() {
            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
            $('#saveBtn').attr('disabled', false);
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

    $('#service_items input[type="checkbox"]').change(function(){
        var checkValue=$(this).val();
        if($(this).prop('checked')){
            showServiceTab(checkValue);
        }else{
            hideServiceTab(checkValue);
        }
    });
    //服务项目checkbox回显,用js拿值
    var checkArray = service_items.split(",");
    for(var i=0;i<checkArray.length;i++){
	    $('#service_items input[type="checkbox"]').each(function(){
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
    
	    
});
});