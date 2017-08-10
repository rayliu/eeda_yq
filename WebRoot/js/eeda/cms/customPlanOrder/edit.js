define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'pageguide',
        'datetimepicker_CN', './edit_doc_table', './edit_cost_table', './edit_charge_table', './edit_shipping_item_table'], function ($, metisMenu) { 
    $(document).ready(function() {
        tl.pg.init({
            pg_caption: '本页教程'
        });

        var Tracking_no=$('#copyTracking_no').val();
        if(Tracking_no.length>0)
            document.title = Tracking_no + ' | ' + document.title;
    	//已报关行按钮状态
    	$('#confirmCompleted,#passBtn,#refuseBtn,#cancelAuditBtn').click(function(){
    		var btnId = $(this).attr("id");
    		$(this).attr('disabled', true);
    		id = $('#order_id').val();
    		var plan_order_no = $('#order_no').val();
    		var customer_id = $('#receive_sent_consignee').val();
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

            //费用明细，应收，应付
            order.charge_template = salesOrder.buildChargeTemplate();
            order.cost_template = salesOrder.buildCostTemplate();
            order.allCharge_template = salesOrder.buildAllChargeTemplate();
            order.allCost_template = salesOrder.buildAllCostTemplate();
            order.order_type = $('#type').val();
            order.customer_id = $('#receive_sent_consignee').val();

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
        
      //常用托运单信息模版
        $('#usedConsignmentInfo').on('click', '.selectConsignmentTemplate', function(){
        	var li = $(this).parent().parent();
            $('#shipping_date').val(li.attr('shipping_date'));
            $('#customs_number').val(li.attr('customs_number'));
            $('#boat_company').val(li.attr('boat_company'));
            $('#boat_name').val(li.attr('boat_name'));
            
            $('#consignee').val(li.attr('consignee'));
            $('#consignee_input').val(li.attr('consignee_name'));
            
            $('#appointed_port').val(li.attr('appointed_port'));
            $('#appointed_port_input').val(li.attr('port_name'));
            
            $('#shipping_men_phone').val(li.attr('shipping_men_phone'));
            $('#shipping_men').val(li.attr('shipping_men'));
            $('#shipping_men_input').val(li.attr('shipping_men_name'));
            $('#consignee_phone').val(li.attr('consignee_phone'));
            $('#notice_man').val(li.attr('notice_man'));
            $('#notice_man_input').val(li.attr('notice_man_name'));
            $('#notice_man_phone').val(li.attr('notice_man_phone'));
            
        });
        $('#collapseConsignmentNoteInfo').on('show.bs.collapse', function () {
          $('#collapseConsignmentNoteIcon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
        });
        $('#collapseConsignmentNoteInfo').on('hide.bs.collapse', function () {
          $('#collapseConsignmentNoteIcon').removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
        });
        
        $('.deleteConsignmentTemplate').click(function(e) {
        	$(this).attr('disabled', true);
        	e.preventDefault();
        	var li = $(this).parent().parent();
        	var id = li.attr('id');
        	$.post('/customPlanOrder/deleteConsignmentTemplate', {id:id}, function(data){
        		$.scojs_message('删除成功', $.scojs_message.TYPE_OK);
        		$(this).attr('disabled', false);
        		li.css("display","none");
        	},'json').fail(function() {
        		$(this).attr('disabled', false);
                $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
            });
        })
        

        
      //常用报关单信息模版
        $('#usedCustomNoteInfo').on('click', '.selectCustomNoteTemplate', function(){
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
        $('#collapseCustomNoteInfo').on('show.bs.collapse', function () {
          $('#collapseCustomNoteIcon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
        });
        $('#collapseCustomNoteInfo').on('hide.bs.collapse', function () {
          $('#collapseCustomNoteIcon').removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
        });
        
        $('.deleteCustomNoteTemplate').click(function(e) {
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
        
        $('#doc_table a').click(function(){
        	var td=$(this).parent();
        	var id=td.parent().attr('id');
        	$.post('/customPlanOrder/newFlag',{id:id},function(data){
        		td.find('span').remove();
        		$('#doc_table [name=doc_name]').each(function(){
        			if($(this).parent().find('span').attr('class')=='badge'){
        				$('#tabDocDetail').find('span').show();
        			}else{
        				$('#tabDocDetail').find('span').hide();
        			}
        			
        		});
        	});
        })
        
        $('#doc_table [name=doc_name]').each(function(){
			if($(this).parent().find('span').attr('class')=='badge'){
				$('#tabDocDetail').find('span').show();
			}
		});

        //应收应付常用费用
         $('#collapseChargeInfo,#collapseCostInfo').on('show.bs.collapse', function () {
        var thisType = $(this).attr('id');
        var type = 'Charge';
        if('collapseChargeInfo'!=thisType){
            type='Cost';
        }
        var div = $('#'+type+'Div').empty();
        $('#collapse'+type+'Icon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
        var order_type = $('#type').val();
        var customer_id = $('#receive_sent_consignee').val();
        if(order_type == ''|| order_type ==null|| customer_id == ''){
            $.scojs_message('请先选择类型和收发货人', $.scojs_message.TYPE_ERROR);
            return;
        }else{
            
            $.post('/customPlanOrder/getArapTemplate', {order_type:order_type,customer_id:customer_id,arap_type:type}, function(data){
                if(data){
                    for(var i = 0;i<data.length;i++){
                        var json_obj = JSON.parse(data[i].JSON_VALUE);
                        var li = '';
                        var li_val = '';
                        for(var j = 0;j<json_obj.length;j++){
                            li +='<li '
                                +' sp_name="'+json_obj[j].sp_name+'" '
                                +'charge_eng_id="'+json_obj[j].CHARGE_ENG_ID+'" '
                                +'charge_id="'+json_obj[j].CHARGE_ID+'" '
                                +'currency_id="'+json_obj[j].CURRENCY_ID+'" '
                                +'sp_id="'+json_obj[j].SP_ID+'" '
                                +'unit_id="'+json_obj[j].UNIT_ID+'" '
                                +'amount="'+json_obj[j].amount+'" '
                                +'charge_name="'+json_obj[j].charge_name+'" '
                                +'charge_name_eng="'+json_obj[j].charge_eng_name+'" '
                                +'currency_name="'+json_obj[j].currency_name+'" '
                                +'currency_total_amount="'+json_obj[j].currency_total_amount+'" '
                                +'exchange_currency_id="'+json_obj[j].exchange_currency_id+'" '
                                +'exchange_currency_name="'+json_obj[j].exchange_currency_name+'" '
                                +'exchange_currency_rate="'+json_obj[j].exchange_currency_rate+'" '
                                +'exchange_rate="'+json_obj[j].exchange_rate+'" '
                                +'exchange_total_amount="'+json_obj[j].exchange_total_amount+'" '
                                +'order_type="'+json_obj[j].order_type+'" '
                                +'price="'+json_obj[j].price+'" '
                                +'remark="'+json_obj[j].remark+'" '
                                +'total_amount="'+json_obj[j].total_amount+'" '
                                +'type="'+json_obj[j].type+'" '
                                +'unit_name="'+json_obj[j].unit_name+'" '
                                +'></li>';
                            li_val += '<span></span> '+json_obj[j].sp_name+' , '+json_obj[j].charge_name+' , '+json_obj[j].total_amount+'<br/>';
                        }
                        
                        div.append('<ul class="used'+type+'Info" id="'+data[i].ID+'">'
                                +li
                                +'<div class="radio">'
                                +'  <a class="delete'+type+'Template" style="margin-right: 10px;padding-top: 5px;float: left;">删除</a>'
                                +'  <div class="select'+type+'Template" style="margin-left: 60px;padding-top: 0px;">'
                                +'      <input type="radio" value="1" name="used'+type+'Info">'
                                +       li_val
                                +'  </div>'
                                +'</div><hr/>'
                                +'</ul>');
                        
                    }
                }
            });
        }
    });

     $('#collapseChargeInfo,#collapseCostInfo').on('hide.bs.collapse', function () {
        var thisType = $(this).attr('id');
        var type = 'Charge';
        if('collapseChargeInfo'!=thisType){
            type='Cost';
        }
        $('#collapse'+type+'Icon').removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
    });

    $('#ChargeDiv,#CostDiv').on('click', '.deleteChargeTemplate,.deleteCostTemplate', function(){
        $(this).attr('disabled', true);
        var ul = $(this).parent().parent();
        var id = ul.attr('id');
        $.post('/customPlanOrder/deleteArapTemplate', {id:id}, function(data){
            if(data){
                $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
                $(this).attr('disabled', false);
                ul.css("display","none");
            }
        },'json').fail(function() {
            $(this).attr('disabled', false);
              $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
        });
    })
    
    //提运单号的copy
    $('#copyTracking_no').keyup(function(){
    	
    	$('#tracking_no').val($('#copyTracking_no').val());
    });
    $('#tracking_no').keyup(function(){
    	$('#copyTracking_no').val($('#tracking_no').val());
    });
    
    //外单申请企业控制
    if($('#other_flag').val()=="other"){
    	$('#receive_sent_consignee1_input').attr('disabled',true);
    	$('#receive_sent_consignee_input').attr('disabled',true);
    	$('#receive_sent_consignee_info').attr('disabled',true);
    }
    
    

     //选中回显
    $('#ChargeDiv,#CostDiv').on('click', '.selectChargeTemplate,.selectCostTemplate', function(){
        $(this).parent().find('[type=radio]').prop('checked',true)
        
        var thisType = $(this).attr('class');
        var type = 'Charge';
        var table = 'charge_table';
        if('selectChargeTemplate'!=thisType){
            type='Cost';
            table='cost_table';
        }
        
        var li = $(this).parent().parent().find('li');
        var dataTable = $('#'+table).DataTable();
        
        for(var i=0; i<li.length; i++){
            var row = $(li[i]);
            var item={};
            item.ID='';
            item.TYPE=row.attr('type');
            item.SP_ID=row.attr('sp_id');
            item.CHARGE_ID= row.attr('charge_id');
            item.CHARGE_ENG_ID= row.attr('charge_eng_id');
            item.PRICE= row.attr('PRICE');
            item.AMOUNT= row.attr('amount');
            item.UNIT_ID= row.attr('unit_id');
            item.TOTAL_AMOUNT= row.attr('total_amount');
            item.CURRENCY_ID= row.attr('currency_id');
            item.EXCHANGE_RATE= row.attr('exchange_rate');
            item.CURRENCY_TOTAL_AMOUNT= row.attr('currency_total_amount');
            item.EXCHANGE_CURRENCY_ID= row.attr('exchange_currency_id');
            item.EXCHANGE_CURRENCY_RATE= row.attr('exchange_currency_rate');
            item.EXCHANGE_TOTAL_AMOUNT= row.attr('exchange_total_amount');
            item.REMARK= row.attr('remark');
            item.SP_NAME=row.attr('sp_name');
            item.CHARGE_NAME=row.attr('charge_name');
            item.CHARGE_NAME_ENG=row.attr('charge_name_eng');
            item.UNIT_NAME=row.attr('unit_name');
            item.CURRENCY_NAME=row.attr('currency_name');
            item.EXCHANGE_CURRENCY_ID_NAME=row.attr('exchange_currency_name');
            item.AUDIT_FLAG='';
            dataTable.row.add(item).draw();
        }
    });
    //将申请单位的值赋给收发企业
    $('#receive_sent_consignee1_list').on('mousedown','a',function(){
 	   $('#receive_sent_consignee').val( $(this).attr('partyid'));
 	   $('#receive_sent_consignee_input').val( $(this).text());
    });

    //当报关单号码或者提运单号为空时，隐藏费用信息
    var chargeDetail_tabShow=function(){
    	var val_billCode=$('#customs_billCode').val();
        var val_no=$('#copyTracking_no').val();
        var val=val_billCode+val_no;
        if(!val) $('#chargeDetail_tab').hide();
        if(val) $('#chargeDetail_tab').show()
    }
    chargeDetail_tabShow();
    $('#customs_billCode,#copyTracking_no').blur(function(){
    // on('keyup',function(){
    	chargeDetail_tabShow();
    })
  });
});