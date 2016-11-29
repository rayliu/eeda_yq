define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'datetimepicker_CN', './edit_doc_table', './edit_cost_table', './edit_charge_table', './edit_shipping_item_table'], function ($, metisMenu) { 
    $(document).ready(function() {
        var order_no=$('#order_no').val();
        if(order_no.length>0)
            document.title = order_no + ' | ' + document.title;
    	//已报关行按钮状态
    	$('#confirmCompleted,#passBtn,#refuseBtn,#cancelAuditBtn').click(function(){
    		var btnId = $(this).attr("id");
    		$(this).attr('disabled', true);
    		id = $('#order_id').val();
    		var plan_order_no = $('#order_no').val();
    		var customer_id = $('#application_company').val();
    		$.post('/customPlanOrder/confirmCompleted', {id:id,btnId:btnId,plan_order_no:plan_order_no,customer_id:customer_id}, function(order){
    				$('#status').val(order.STATUS);
    				var status = order.STATUS;
					//提交报关行按钮状态
    				if(status=="待审核"){
						$('#confirmCompleted').attr('disabled', true);
						$('#saveBtn').attr('disabled', true);
						//审核按钮状态
						$('#passBtn').attr('disabled',false);
                        $('#cancelAuditBtn').attr('disabled', false);
			        	$('#refuseBtn').attr('disabled',false);
			        	$.scojs_message('申请单提交成功', $.scojs_message.TYPE_OK);
		        	}
    				if(status=="审核通过"){
		        		$('#confirmCompleted').attr('disabled', true);
						$('#saveBtn').attr('disabled', false);
                        $('#cancelAuditBtn').show().attr('disabled', false);
                         if(show_passAuditing){
                        $('#custom_state').attr('disabled', false);
                        $('#customs_billCode').attr('disabled', false);
                        }
						//审核按钮状态
						$('#passBtn').attr('disabled',true);
			        	$('#refuseBtn').attr('disabled',true).hide();
			        	$.scojs_message('审核成功', $.scojs_message.TYPE_OK);
//			        	if(confirm('确定要前往工作单？')){
//			        		location.href="/customJobOrder/edit?id="+order.JOB_ORDER_ID;
//			        	}
		        	}
    				if(status=="审核不通过"){
    					$('#confirmCompleted').attr('disabled', true);
						$('#saveBtn').attr('disabled', false);
						//审核按钮状态
                        $('#cancelAuditBtn').hide();
						$('#passBtn').attr('disabled',true);
			        	$('#refuseBtn').attr('disabled',true).show();
			        	$.scojs_message('审核成功', $.scojs_message.TYPE_OK);
    				}
		    	},'json').fail(function() {
		    	    	if(status=="新建"){
			    	        $.scojs_message('申请单提交失败', $.scojs_message.TYPE_ERROR);
			    	        $('#confirmCompleted').attr('disabled', false);
			    	        $('#passBtn').attr('disabled',true);
				        	$('#refuseBtn').attr('disabled',true);
		    	        }
		    	    	if(status=="待审核"){
		    	    		$.scojs_message('申请单提交失败', $.scojs_message.TYPE_ERROR);
			    	        $('#confirmCompleted').attr('disabled', true);
			    	        $('#saveBtn').attr('disabled', true);
			    	        $('#passBtn').attr('disabled',false);
				        	$('#refuseBtn').attr('disabled',false);
		    	    	}
		    	      });
    	});
    	
    	 //提交报关行按钮状态
    	var id = $('#order_id').val();
    	var status = $('#status').val();
        if(id!=''){
        	$('#confirmCompleted').attr('disabled', false);
        }
		if(status=='待审核'){
			//提交报关行按钮状态
			$('#confirmCompleted').attr('disabled', true);
			$('#saveBtn').attr('disabled', true);
			//审核按钮状态
			$('#passBtn').attr('disabled',false);
        	$('#refuseBtn').attr('disabled',false);
        }
		if(status=="审核通过"){
            $('#cancelAuditBtn').show().attr('disabled',false);
            $('#refuseBtn').hide();
            if(show_passAuditing){
            $('#custom_state').attr('disabled', false);
            $('#customs_billCode').attr('disabled', false);
            }
            
        }
		if(status=="审核通过"||status=="审核不通过"){
			//提交报关行按钮状态
			$('#confirmCompleted').attr('disabled', true);
			$('#saveBtn').attr('disabled',false);
			//审核按钮状态
			$('#passBtn').attr('disabled',true);
        	$('#refuseBtn').attr('disabled',true);
		}

    	
        //------------save保存
        $('#saveBtn').click(function(e){
            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            //提交前，校验页面中必填字段
            if(!$("#customForm").valid()){
                return;
            }
            $(this).attr('disabled', true);
            
       //获取页面数据，构造json 
            var items_array = salesOrder.buildCargoDetail();
        	var order = {};
        	order['id'] = $('#order_id').val();
        	order['custom_bill'] = $('#customDetail input[type="radio"]:checked').val();
        	order['ref_job_order_id'] = $('#ref_job_order_id').val();
        	order['to_office_id'] = $('#to_office_id').val();
//        	order['status'] = $('#status').val()==""?"新建":$('#status').val();
        	var customForm = $('#customForm :input,#customDetail :input,#headDetail :input,#shippingOrderDetail :input');
        	for(var i = 0; i < customForm.length; i++){
        		var name = customForm[i].id;
            	var value =customForm[i].value;
            if(name){
            	
            	order[name] = value;
            	if(name="status"){
            		order[name] = $('#status').val()==""?"新建":$('#status').val();
            	}            	
        	  }
        	}
        	order['item_list'] = items_array;
        	order.doc_list = salesOrder.buildDocItem();
        	order.charge_list = salesOrder.buildChargeDetail();
        	order.cost_list = salesOrder.buildCostDetail();
        	order.shipping_item = salesOrder.buildShippingItem();

//            var order = {
//            		    id: $('#order_id').val(),
//            	carrier_id: $('#carrier').val(),
//            	 deal_mode: $('#deal_mode').val(),
//                      type: $('#type').val(),
//                    remark: $('#note').val(),
//                    status: $('#status').val()==''?'新建':$('#status').val(),
//                 item_list:items_array
//                
//            };
            //异步向后台提交数据
            $.post('/customPlanOrder/save', {params:JSON.stringify(order)}, function(data){
                var order = data;
                if(order.ID>0){
                    $("#create_stamp").val(order.CREATE_STAMP);
                    $("#creator_name").val(order.CREATOR_NAME);
                    $("#order_id").val(order.ID);
                    $("#order_no").val(order.ORDER_NO);
                    $("#status").val(order.STATUS);
                    $("#note").val(order.NOTE);
                    eeda.contactUrl("edit?id",order.ID);
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                    $('#saveBtn').attr('disabled', false);
                    $('#confirmCompleted').attr('disabled', false);
                    //异步刷新明细表
                    salesOrder.refleshTable(order.ID);
                    salesOrder.refleshDocTable(order.ID);
                    salesOrder.refleshCostTable(order.ID);
                    salesOrder.refleshChargeTable(order.ID);
                    salesOrder.refleshShippingItemTable(order.ID);
                    $("#fileuploadSpan").show();
                }else{
                    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                    $('#saveBtn').attr('disabled', false);
                }
            },'json').fail(function() {
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
              });
        });  
        
        
      //常用海运信息模版
        $('#usedAirInfo').on('click', '.selectAirTemplate', function(){
        	var li = $(this).parent().parent();
            $('#receive_sent_consignee').val(li.attr('receive_sent_consignee'));
            $('#receive_sent_consignee_input').val(li.attr('receive_sent_consignee_abbr'));
            $('#receive_sent_consignee_info').val(li.attr('receive_sent_consignee_info'));
            $('#production_and_sales').val(li.attr('production_and_sales'));
            $('#production_and_sales_input').val(li.attr('production_and_sales_abbr'));
            $('#production_and_sales_info').val(li.attr('production_and_sales_info'));
            $('#application_unit').val(li.attr('application_unit'));
            $('#application_unit_input').val(li.attr('application_unit_abbr'));
            $('#application_unit_info').val(li.attr('application_unit_info'));
            $('#export_port').val(li.attr('export_port'));
            
            $('#transport_type').val(li.attr('transport_type'))
            
            $('#supervision_mode').val(li.attr('supervision_mode'));
            $('#supervision_mode_input').val(li.attr('supervision_mode_name'));
            
            $('#nature_of_exemption').val(li.attr('nature_of_exemption'))
            	
            
            $('#record_no').val(li.attr('record_no'));
            $('#trading_country').val(li.attr('trading_country'));
            $('#trading_country_input').val(li.attr('trading_country_name'));
            $('#destination_country').val(li.attr('destination_country'));
            $('#destination_country_input').val(li.attr('destination_country_name'));
            $('#destination_port').val(li.attr('destination_port'));
            $('#destination_port_input').val(li.attr('destination_port_name'));
            $('#supply_of_goods').val(li.attr('supply_of_goods'));
            $('#license_no').val(li.attr('license_no'));
            $('#contract_agreement_no').val(li.attr('contract_agreement_no'));
            
            $('#deal_mode').val(li.attr('deal_mode'))
            
            $('#note').val(li.attr('note'));
            
        });
        $('#collapseAirInfo').on('show.bs.collapse', function () {
          $('#collapseAirIcon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
        });
        $('#collapseAirInfo').on('hide.bs.collapse', function () {
          $('#collapseAirIcon').removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
        });
        
        $('.deleteAirTemplate').click(function(e) {
        	$(this).attr('disabled', true);
        	e.preventDefault();
        	var li = $(this).parent().parent();
        	var id = li.attr('id');
        	$.post('/customPlanOrder/deleteCustomTemplate', {id:id}, function(data){
        		$.scojs_message('删除成功', $.scojs_message.TYPE_OK);
        		$(this).attr('disabled', false);
        		li.css("display","none");
        	},'json').fail(function() {
        		$(this).attr('disabled', false);
                $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
            });
        })
        
        $('#printBtn').click(function(){
        	$(this).attr('disabled', true);
        	var id = $('#order_id').val();
	    	$.post('/jobOrderReport/printConsignmentBill', {id:id}, function(data){
	    			$('#printBtn').prop('disabled', false);
	                window.open(data);
	    	}).fail(function(data) {
	    			$('#printBtn').prop('disabled', false);
	        		$.scojs_message('生成托运申报单PDF失败', $.scojs_message.TYPE_ERROR);
            });
        });
        
        $('#printCustomOrderBtn').click(function(){
        	$(this).attr('disabled', true);
        	var id = $('#order_id').val();
        	var type = $('#type').val();
        	$.post('/jobOrderReport/printCargoCustomOrder', {id:id,type:type}, function(data){
        		$('#printCustomOrderBtn').prop('disabled', false);
        		window.open(data);
        	}).fail(function() {
        		$('#printCustomOrderBtn').prop('disabled', false);
        		$.scojs_message('生成货物报关单PDF失败', $.scojs_message.TYPE_ERROR);
        	});
        });
        
        $("#type").click(function(){
        	if(this.value=="出口"){
        		$('#receive_sent_consignee_input').prev().text("收发货人(退税企业)");
        		$('#production_and_sales_input').prev().text("生产销售单位");
        		$('#export_port_input').prev().text("出口口岸");
        		$('#custom_export_date').prev().prev().text("出口日期");
        		$('#destination_country_input').prev().text("运抵国");
        		$('#destination_port_input').prev().text("指运港");
        		$('#supply_of_goods_input').prev().text("境内货源地");
        	}else{
        		$('#receive_sent_consignee_input').prev().text("收发货人");
        		$('#production_and_sales_input').prev().text("销售使用单位");
        		$('#export_port_input').prev().text("进口口岸");
        		$('#custom_export_date').prev().prev().text("进口日期");
        		$('#destination_country_input').prev().text("启运国");
        		$('#destination_port_input').prev().text("装货港");
        		$('#supply_of_goods_input').prev().text("境内目的地");
        	}
        })
        
        $('#customs_code_list').on('mousedown','a', function () {
        	var id = $(this).attr('portId');
        	var info = $(this).attr('str');
        	$('#shipping_men').val(id);
        	$('#receive_sent_consignee').val(id);
        	var str = $(this).text();
        	var arr = str.split(' ');
        	if(arr!=undefined&&arr.length>=0){
        		$('#shipping_men_input').val(arr[0]);
        		$('#receive_sent_consignee_input').val(arr[0]);
        	}
            if(arr!=undefined&&arr.length>=1){
        		$('#customs_number').val(arr[1]);
        	}
            if(info!='undefined'){
            	$('#receive_sent_consignee_info').val(info);
            }
        })  
     });
});