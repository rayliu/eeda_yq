define(['jquery', 'metisMenu', 'sb_admin','dataTablesBootstrap','./order_item_table','validate_cn', 'sco', './request_item_table'], function ($, metisMenu, sb, requestItemContr, selectContr) {
	$(document).ready(function() {
		//构造主表json
	    var buildOrder = function(){
	    	var item = {};
	    	item.id = $('#order_id').val();
	    	item.status='新建';
	    	var orderForm = $('#orderForm input,select,textarea');
	    	for(var i = 0; i < orderForm.length; i++){
	    		var name = orderForm[i].id;
	        	var value =orderForm[i].value;
	        	if(name){
	        		if(name.indexOf("biz_period_begin") != -1){
	        			name = "biz_period_from";
	        		}else if(name.indexOf("biz_period_end") != -1){
	        			name = "biz_period_to"
	        		}
	        		if(name.indexOf("modal_") != -1){
	            	  	value=value.replace(/,/g,'');
	            	}
	        		item[name] = value;
	        	}
	    	}
	        return item;
	    }
	    
		//收款方式回显
		$('#charge_type').change(function(){
			var type = $(this).val();
			if(type == 'cash'||type==""){
				$('#transfers_massage').hide();
				$('#receive_type_massage').hide();
			}else{
				$('#transfers_massage').show();
				$('#receive_type_massage').show();
			}
		});
		
		//发票类型（1）
		$('#invoice_type').change(function(){
			var type = $(this).val();
			if(type == 'wbill'||type==""){
				$('#invoiceDiv').hide();
				$('#projectFee').hide();
			}else{
				$('#invoiceDiv').show();
				$('#projectFee').show();
			}
		});
		var projectFeeType = $('#invoice_type').val();
		if(projectFeeType == 'wbill'||projectFeeType==""){
			$('#projectFee').hide();
			$('#invoiceDiv').hide();
		}else{
			$('#projectFee').show();
			$('#invoiceDiv').show();
		}
		
		//选中开户行带出收款账号跟账户名
		$('#bank_id_list').on('mousedown','a',function(){
			   $('#account_no').val( $(this).attr('account_no'));
		 	   $('#account_name').val( $(this).attr('account_name'));
		    })
		    
		//申请保存
		$("#createSave").on('click',function(){
			$("#createSave").attr("disabled", true);
			if($('#biz_period_begin_time').val()==""||$('#biz_period_end_time').val()==""){
					$.scojs_message('业务发生月不能为空', $.scojs_message.TYPE_FALSE);
					$("#createSave").attr("disabled", false);
					return false;
			}
			
			if($("#charge_type").val()=='transfers'||$("#charge_type").val()=='checkTransfers'){
				if($("#bank_name").val()=='' && $("#account_no").val()==''&& $("#account_name").val()==''){
					$.scojs_message('转账的信息不能为空', $.scojs_message.TYPE_FALSE);
					$("#createSave").attr("disabled", false);
					return false;
				}
			}
			
			var order = buildOrder();
			order.party_id=$('#party_id').val();
			order.selected_item_ids = $('#selected_ids').val();
			order.ids=$('#ids').val();
			
			$.post('/invoiceApply/save',{params:JSON.stringify(order)}, function(data){
				$("#createSave").attr("disabled", false);
				if(data.ID>0){
					$.scojs_message('创建开票单成功', $.scojs_message.TYPE_OK);
					$("#order_no").val(data.ORDER_NO);
					if(confirm('刚生成的新开票单号'+data.ORDER_NO+':是否前往该开票单？')){
						self.location='/invoiceApply/edit?id='+data.ID; 
					 }
					selectContr.refleshSelectTable(data.IDSARRAY);
					requestItemContr.refleshRequestItemTable();
				}else{
					$.scojs_message('创建新失败', $.scojs_message.TYPE_FALSE);
				}
			 },'json').fail(function() {
		            $.scojs_message('创建新申请失败', $.scojs_message.TYPE_ERROR);
		            $('#saveBtn').attr('disabled', false);
		        });
		});
	});
});