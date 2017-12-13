define(['jquery', 'metisMenu', 'sb_admin','template','./item_table','dataTablesBootstrap','validate_cn', 'sco', 'pageguide'], function ($, metisMenu, sb, template,itemTable) {
	$(document).ready(function() {
		var cioiciIds = null;
		var deletedTableIds=[];
		var order_ids = $("#selected_item_ids").val();
		var id = $("#order_id").val();
		//构造主表json
	    var buildOrder = function(){
	    	var item = {};
	    	item.id = $('#order_id').val();
	    	item.selected_item_ids = $('#selected_ids').val();
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
	   
	    var buildInvoiceDetail = function(){
	        var table_rows = $("#invoice_item_table tr");
	        var items_array=[];
	        for(var index = 0; index<table_rows.length; index++){
	            if(index == 0)
	                continue;

	            var row = table_rows[index];
	            var empty = $(row).find('.dataTables_empty').text();
	            if(empty)
	            	continue;
	            
	            var id = $(row).attr('id');
	            if(!id){
	                id = '';
	            }
	            
	            var item = {}
	            item.id = id;
	            for(var i = 0; i < row.childNodes.length; i++){
	            	var el = $(row.childNodes[i]).find('.itemList,input,select');
	            	var name = el.attr('name'); 

	            	if(el && name){
	                	var value = el.val();//元素的值
	                	item[name] = value;
	                	
	            	}
	            }
	            item.action = id.length > 0?'UPDATE':'CREATE';
	            items_array.push(item);
	        }

	        //add deleted items
	        for(var i=0; i<deletedTableIds.length; i++){
	            var id = deletedTableIds[i];
	            var item = {
	                id: id,
	                action: 'DELETE'
	            };
	            items_array.push(item);
	        }
	        deletedTableIds = [];
	        return items_array;
	    };
	    
		var dataTable = eeda.dt({
		    id: 'invoice_item_table',
		    autoWidth: false,
		    //serverSide: true, //不打开会出现排序不对 
		    ajax: "/invoiceApply/invoiceList?id="+id,
		    drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
	            bindFieldEvent();
	        },
		    columns: [
					{ "width":"110px",
					    "render": function(data, type, full, meta) {
					    	var status = $("#status").val();
					    	if(status=='已开票'||status=='已提交'){
					    		return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:40px" disabled>删除</button>'
					    			  +'<button type="button" class="itemList btn table_btn btn_green btn-xs" style="width:40px;" name="invoice_ids" value="'+full.CHARGE_IDS+'" disabled>明细</button>';
					    	}else{
					    		return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:40px" >删除</button>'
					    			  +'<button type="button" class="itemList btn table_btn btn_green btn-xs" style="width:40px;" name="invoice_ids" value="'+full.CHARGE_IDS+'">明细</button>';
					    	}
					    }
					},
					{"data":"FEE_ID","width":"100px",
						"render": function ( data, type, full, meta ) {
		                	if(full.AUDIT_FLAG == 'Y'){
		                		if(!data)
		                            data='';
		                        var field_html = template('table_dropdown_template',
		                            {
		                                id: 'FEE_ID',
		                                value: data,
		                                display_value: full.FEE_NAME,
		                                style:'width:100px',
		                                disabled:'disabled'
		                            }
		                        );
		                        return field_html;
		                     }else{
		                    if(!data)
		                        data='';
		                    var field_html = template('table_dropdown_template',
		                        {
		                            id: 'FEE_ID',//对应数据库字段
		                            value: data,
		                            display_value: full.FEE_NAME,
		                            style:'width:100px'
		                        }
		                    );
		                    return field_html;
		                }
		              }
						
					},
					{"data":"AMOUNT","width":"60px",
						"render": function(data, type, full, meta) {
							if(data){
								return '<input type="text" style="width:60px" name="amount" value="'+data+'">';
							}
							return '<input type="text" style="width:60px" name="amount" value="">';
						}	
					},
					{"data":"TOTAL_AMOUNT","width":"80px",
						"render": function(data, type, full, meta) {
							if(data){
								return '<input type="text" style="width:80px" name="total_amount" value="'+data+'">';
							}
							return '<input type="text" style="width:80px" name="total_amount" value="">';
						}	
					},
					{"data":"TAX_RATE","width":"70px",
						"render": function(data, type, full, meta) {
							if(data){
								return '<input type="text" style="width:70px" name="tax_rate" value="'+data+'">';
							}
							return '<input type="text" style="width:70px" name="tax_rate" value="">';
						}	
					},
					{"data":"TOTAL_AMOUNT_NO_TAX","width":"90px",
						"render": function(data, type, full, meta) {
							if(data){
								return '<input type="text" style="width:90px" name="total_amount_no_tax" value="'+data+'" disabled>';
							}
							return '<input type="text" style="width:90px" name="total_amount_no_tax" value="" disabled>';
						}	
					},
					{"data":"TAX_AMOUNT","width":"70px",
						"render": function(data, type, full, meta) {
							if(data){
								return '<input type="text" style="width:70px" name="tax_amount" value="'+data+'" disabled>';
							}
							return '<input type="text" style="width:70px" name="tax_amount" value="" disabled>';
						}	
					},
					{"data":"TAX_TOTAL_AMOUNT","width":"70px",
						"render": function(data, type, full, meta) {
							if(data){
								return '<input type="text" style="width:70px" name="tax_total_amount" value="'+data+'" disabled>';
							}
							return '<input type="text" style="width:70px" name="tax_total_amount" value="" disabled>';
						}	
					}
	        ]      
	    });
		
		var itemListTable = eeda.dt({
		    id: 'itemList_table',
		    autoWidth: false,
		    //ajax: "/chargeRequest/OrderList?pageBoolean="+pageBoolean,
		    drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件

		    },
		    columns: [
					{"width":"30px",
					    "render": function(data, type, full, meta) {
					    	var str = '';
					    	for(var i =0;i<cioiciIds.length;i++){
					    		if(cioiciIds[i]==full.ID){
					    			str = '<input type="checkbox" name="order_check_box" class="checkBox" checked>';
					    			return str;
					    		}
					    	}
				    		str = '<input type="checkbox" name="order_check_box" class="checkBox" >';
					    	return str;
					    }
					},
					{"data":"CHECK_ORDER_NO","width":"120px"},
					{ "data": "ORDER_NO","width":"90px",
	                	"render": function ( data, type, full, meta ) {
			           		  return "<a href='/jobOrder/edit?id="+full.ID+"'>"+data+"</a>";
			           	  }
	                },
					{"data":"SP_NAME","width":"120px"},
					{"data":"FIN_NAME","width":"120px"},
					{"data":"CURRENCY_NAME","width":"120px"},
					{"data":"EXCHANGE_TOTAL_AMOUNT","width":"120px"}
	        ]      
	    });
		
		var bindFieldEvent=function(){
			eeda.bindTableFieldChargeId('invoice_item_table','FEE_ID','/finItem/search','');
	        eeda.bindTableFieldCurrencyId('invoice_item_table','CURRENCY_ID','/serviceProvider/searchCurrency','');
	    };
		
		//选中开户行带出收款账号跟账户名
		$('#bank_id_list').on('mousedown','a',function(){
			   $('#account_no').val( $(this).attr('account_no'));
		 	   $('#account_name').val( $(this).attr('account_name'));
		    })
		
		//收款方式显示
		var charge_type_show = function(){
			var type = $('#charge_type').val();
			if(type == 'cash'||type==""){
				$('#transfers_massage').hide();
				$("#bank_id_input").val("");
				$("#account_no").val("");
				$("#account_name").val("");
			}else{
				$('#transfers_massage').show();
			}
		}
		$('#charge_type').change(function(){
			charge_type_show();
		});
		charge_type_show();
		
		//发票类型显示
		var invoice_type_show = function(){
			var type = $('#invoice_type').val();
			if(type == 'wbill'||type==""){
				$("#fee_type").val("");
				$("#billing_unit").val("");
				$('#projectFee').hide();
				$('#invoiceDiv').hide();
			}else{
				$('#projectFee').show();
				$('#invoiceDiv').show();
			}
		}
		$('#invoice_type').change(function(){
			invoice_type_show();
		});
		invoice_type_show();
		
		//保存按钮动作
		$("#saveBtn").click(function(){
			var order = buildOrder();
			order.invoiceList = buildInvoiceDetail();
			order.ids=$('#ids').val();
			$.post("/invoiceApply/save",{params:JSON.stringify(order)},function(data){
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				invoiceRefreshDetail(id);
			},"json").fail(function() {
	            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
	            $('#saveBtn').attr('disabled', false);
	            $.unblockUI();
	        });
		});
		
		//提交按钮动作
		$("#submitBtn").click(function(){
			var order_id = $("#order_id").val();
		    $.post("/invoiceApply/submitMethod",{order_id:order_id},function(data){
		    	if(data){
		    		$.scojs_message('提交成功', $.scojs_message.TYPE_OK);
		    		$("#status").val("已提交");
		    		$("button").attr("disabled",true);
		    		$("#checkBtn,#cancelcheckBtn").attr("disabled",false);
		    	}else{
		    		$.scojs_message('提交失败', $.scojs_message.TYPE_ERROR);
		    	}
		    });
		});
		
		//复核按钮动作
		$("#checkBtn").click(function(){
			var order_id = $("#order_id").val();
			var chargeItemIds = [];
			$("#item_table tr").each(function(){
				var chargeItemId = $(this).attr("id");
				chargeItemIds.push(chargeItemId);
			});
		    $.post("/invoiceApply/checkMethod",{order_id:order_id,chargeItemIds:chargeItemIds.toString()},function(data){
		    	if(data){
		    		$.scojs_message('开票复核成功', $.scojs_message.TYPE_OK);
		    		$("#status").val("已开票");
		    		$("button").attr("disabled",true);
		    	}else{
		    		$.scojs_message('开票复核失败', $.scojs_message.TYPE_ERROR);
		    	}
		    });
		});
		
		//复核不通过按钮动作
		$("#cancelcheckBtn").click(function(){
			var order_id = $("#order_id").val();
		    $.post("/invoiceApply/cancelcheckMethod",{order_id:order_id},function(data){
		    	if(data){
		    		$.scojs_message('开票复核成功', $.scojs_message.TYPE_OK);
		    		$("#status").val("开票复核不通过");
		    		$("#saveBtn,#submitBtn,#cancelcheckBtn,#add_invoice_item,#add_item").attr("disabled",false);
					$("#checkBtn,#cancelcheckBtn").attr("disabled",true);
		    	}else{
		    		$.scojs_message('开票复核失败', $.scojs_message.TYPE_ERROR);
		    	}
		    });
		});
		
		//发票明细-添加明细
		$("#add_invoice_item").click(function(){
			var item={};
			dataTable.row.add(item).draw(true);
		});
		
		//用来判断是否有发票明细id的标记
		var invoice_id = "";
		
		//发票明细-明细按钮动作
		$("#invoice_item_table").on("click",".itemList",function(){
			$("#allChargeItem").prop("checked",false);
			//每单击进来都清空之前的标记位
			$(".itemList").attr("sign","");
			//重新在当前行设定标记位
			$(this).attr("sign","1");
            $('#itemList_msg_btn').click();
            var order_id = $("#order_id").val();
            cioiciIds = ($(this).val()).split(",");
            if(cioiciIds=="undefined"){
            	cioiciIds = "";
            }
            var ids = [];
            $("#invoice_item_table tbody td [name='invoice_ids']").each(function(){
				var list = ($(this).val()).split(",");
				for(var i = 0;i<list.length;i++){
					if(list[i]!="undefined"&&list[i]!=""&&list[i]!="null"){
						ids.push(list[i]);
					}
				}
			});
            if(cioiciIds!=""&&ids!=""){
            	for(var i = 0;i<cioiciIds.length;i++){
            		for(var j = 0;j<ids.length;j++){
    					if(cioiciIds[i]==ids[j]){
    						ids.splice($.inArray(cioiciIds[i],ids),1);
    					}
    				}
				}
            }
            //判断是否有发票明细id
            invoice_id = $(this).parent().parent().attr("id");
            
            var url = "/invoiceApply/itemList?order_ids="+order_id
            		+ "&cioiciIds="+cioiciIds
            		+ "&ids="+ids;;
            itemListTable.ajax.url(url).load();
		});
		
		//发票明细-明细-确定按钮动作
		$("#confirmBtn").click(function(){
			var charge_itemlist = [];
			$("#itemList_table input[name=order_check_box]:checked").each(function(){
				var id = $(this).parent().parent().attr("id");
				charge_itemlist.push(id);
			});
			
			$("#invoice_item_table .itemList").each(function(){
				var sign = $(this).attr("sign");
				if(sign==1){
					$(this).val(charge_itemlist);
				}
			});
		});
		
		//添加明细里的全选按钮
		$("#allChargeItem").click(function(){
			$("input[type='checkbox']").prop("checked",$(this).prop("checked"));
		});
		
		//发票明细-删除按钮动作
		$("#invoice_item_table").on("click",".delete",function(){
			var tr = $(this).parent().parent()
			var rowId = tr.attr('id');
			var ids = $(this).parent().find("[name=invoice_ids]").val()
			if(ids=="undefined"){
				dataTable.row(tr).remove().draw();
				return;
			}
			$.post("/invoiceApply/deleteInvoiceItem",{rowId:rowId,ids:ids},function(data){
				dataTable.row(tr).remove().draw();
			});
		});
		
		//刷新发票明细
		var invoiceRefreshDetail = function(id){
			var url = "/invoiceApply/invoiceList?id="+id;
			dataTable.ajax.url(url).load();
		}
		
		var status = $("#status").val()
		//按钮控制
		if(status=="开票中"||status=="复核不通过"){
			$("button").attr("disabled",false);
			$("#checkBtn,#cancelcheckBtn").attr("disabled",true);
		
		}else if(status=="已提交"){
			$("button").attr("disabled",true);
			$("#checkBtn,#cancelcheckBtn").attr("disabled",false);
		}else if(status=="已开票"){
			$("button").attr("disabled",true);
		}
		
		$("#invoice_item_table").on("keyup","input[name=amount],input[name=total_amount],input[name=tax_rate]",function(){
			var tr = $(this).parent().parent();
			var amount = tr.find("[name=amount]").val();
			var total_amount = tr.find("[name=total_amount]").val();
			var tax_rate = tr.find("[name=tax_rate]").val();
			var total_amount_no_tax = amount*total_amount-amount*total_amount*(tax_rate*0.01);
			var tax_amount = amount*total_amount*(tax_rate*0.01);
			tr.find("[name=total_amount_no_tax]").val(total_amount_no_tax.toFixed(3));
			tr.find("[name=tax_amount]").val(tax_amount.toFixed(3));
			tr.find("[name=tax_total_amount]").val((amount*total_amount).toFixed(3));
		});
		
	});
});