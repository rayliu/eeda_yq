define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
$(document).ready(function() {

	var deletedTableIds=[];
	$("#total_difference_span").text($("#total_difference").val());
	$("#price_difference_span").text($("#price_difference").val());
    //删除一行
    $("#trade_cost_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))
        
        cargoTable.row(tr).remove().draw();
    }); 
    
    itemOrder.buildTradeCostItem=function(){
        var cargo_table_rows = $("#trade_cost_table tr");
        var cargo_items_array=[];
        for(var index=2; index<cargo_table_rows.length; index++){

            var row = cargo_table_rows[index];
            var empty = $(row).find('.dataTables_empty').text();
            if(empty)
            	continue;
            
            var id = $(row).attr('id');
            if(!id){
                id='';
            }
            
            var item={}
            
            item.id = id;
            item.commodity_name = $($(row).find('[name=COMMODITY_ID_input]')).val();
            for(var i = 1; i < row.childNodes.length; i++){
            	var els = $(row.childNodes[i]).find('input, select');

                $.each(els, function(index, inputEl) {
                    var el = $(inputEl);
                    var name = el.attr('name'); //name='abc'
                
                    if(el && name){
                        var value = el.val();//元素的值
                        item[name] = value;
                    }
                });
            	
            }
            item.action = id.length > 0?'UPDATE':'CREATE';
            cargo_items_array.push(item);
        }

        //add deleted items
        for(var index=0; index<deletedTableIds.length; index++){
            var id = deletedTableIds[index];
            var item={
                id: id,
                action: 'DELETE'
            };
            cargo_items_array.push(item);
        }
        deletedTableIds = [];
        return cargo_items_array;
    };


    //------------事件处理
    var bindFieldEvent=function(){	
        eeda.bindTableFieldCurrencyId('trade_cost_table','CUSTOM_CURRENCY','/serviceProvider/searchCurrency','');
        eeda.bindTableFieldTradeItem('trade_cost_table','COMMODITY_ID','/trJobOrder/searchCommodity','');
    };
    var cargoTable = eeda.dt({
	    id: 'trade_cost_table',
	    autoWidth: false,
        scrollY: 530,
        scrollCollapse: true,

        createdRow: function ( row, data, index ) {
            if(data.ID){
                $(row).attr('id', data.ID);
            }
            $('td:eq(0)',row).append('<span style="float:right;">'+(index+1)+'</span>');
        },
	    drawCallback: function( settings ) {
            bindFieldEvent();
            $.unblockUI();
        },
	    columns:[
            { "width": "5px"},
			{ "width": "30px",
			    "render": function ( data, type, full, meta ) {
			    		return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:50px"><i class="fa fa-trash-o"></i> 删除</button></button> ';
			    }
			},
            { "data": "COMMODITY_ID", "width": "130px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    var field_html = template('table_trade_item_template',
                            {
                                id: 'COMMODITY_ID',
                                value: data,
                                display_value: full.COMMODITY_NAME,
                                style:'width:120px'
                            }
                        );
                    return field_html;
                }
            },
            { "data": "NUMBER", "width": "100px","className":"number",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="number" value="'+data+'" class="form-control" style="width:120px"/>';
                }
            },
            { "data": "LEGAL_UNIT", "width": "80px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
                        data='';
                    var str = '<select name="legal_unit" class="form-control search-control" style="width:100px" >'
                    	+'<option value="" '+(data=='' ? 'selected':'')+'></option>'
                    	+'<option value="个" '+(data=='个' ? 'selected':'')+'>个</option>'
                        +'<option value="千克" '+(data=='千克' ? 'selected':'')+'>千克</option>'
                        +'<option value="克" '+(data=='克' ? 'selected':'')+'>克</option>'
                        +'<option value="毫克" '+(data=='毫克' ? 'selected':'')+'>毫克</option>'
                        +'<option value="吨" '+(data=='吨' ? 'selected':'')+'>吨</option>'
                        +'<option value="平方米" '+(data=='平方米' ? 'selected':'')+'>平方米</option>'
                        +'<option value="厘米" '+(data=='厘米' ? 'selected':'')+'>厘米</option>'
                        +'<option value="公顷" '+(data=='公顷' ? 'selected':'')+'>公顷</option>'
                        +'<option value="立方米" '+(data=='立方米' ? 'selected':'')+'>立方米</option>'
                        +'<option value="台" '+(data=='台' ? 'selected':'')+'>台</option>'
                        +'<option value="套" '+(data=='套' ? 'selected':'')+'>套</option>'
                        +'</select>';
                	return str;
            	}
            },
            { "data": "PRICE", "width": "100px",
            	"render": function ( data, type, full, meta ) {
            		
            		if(!data)
            			data='';
            		if(full.ID && data=='')
            			data = 0.0;
            			
            		return '<input type="text" name="price" value="'+data+'" class="form-control" style="width:120px"/>';
            	}
            },
            { "data": "DOMESTIC_PRICE", "width": "130px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="domestic_price" value="'+data+'" class="form-control" style="width:150px"/>';
            	}
            },
            { "data": "VALUE_ADDED_TAX", "width": "80px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		if(full.ID && data=='')
            			data = 0.0;
            		return '<input type="text" name="value_added_tax" value="'+data+'" class="form-control" style="width:100px"/>';
            	}
            },
            { "data": "TAX_REFUND_RATE", "width": "80px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		if(full.ID && data=='')
            			data = 0.0;
            		return '<input type="text" name="tax_refund_rate" value="'+data+'" class="form-control" style="width:100px"/>';
            	}
            },
            { "data": "TAX_REFUND_RATE_CUSTOMER", "width": "80px",//客户退税率
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		if(full.ID && data=='')
            			data = 0.0;
            		return '<input type="text" name="tax_refund_rate_customer" value="'+data+'" class="form-control" style="width:100px"/>';
            	}
            },//客户退税率
            { "data": "TAX_REFUND_AMOUNT", "width": "100px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="tax_refund_amount" value="'+data+'" class="form-control" style="width:120px" disabled/>';
            	}
            },
            { "data": "ADJUSTED_TAX_REFUND_AMOUNT", "width": "100px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="adjusted_tax_refund_amount" value="'+data+'" class="form-control" style="width:120px" disabled/>';
            	}
            },
            { "data": "ADJUSTED_UNIT_PRICE", "width": "80px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="adjusted_unit_price" value="'+data+'" class="form-control" style="width:100px" disabled/>';
            	}
            },
            { "data": "ADJUSTED_TOTAL_PRICE", "width": "80px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="adjusted_total_price" value="'+data+'" class="form-control" style="width:100px" disabled/>';
            	}
            },
            { "data": "CUSTOM_PRICE", "width": "80px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="custom_price" value="'+data+'" class="form-control" style="width:100px"/>';
            	}
            },
            { "data": "NUMBER", "width": "80px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="number" value="'+data+'" class="form-control" style="width:100px"/>';
                }
            },
            { "data": "CUSTOM_AMOUNT", "width": "100px","className":"currency_total_amount",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="custom_amount" value="'+data+'" class="form-control" style="width:120px" />';
            	}
            },
            { "data": "CUSTOM_CURRENCY", "width": "60px", 
            	"render": function ( data, type, full, meta ) {
	                	if(!data){
	                			var field_html = template('table_currency_dropdown_template',
	    	                        {
	    	                            id: 'CUSTOM_CURRENCY',
	    	                            value: $('#cost_currency').val(),
	    	                            display_value: $('#cost_currency_input').val(),
	    	                            style:'width:80px'
	    	                        }
	                		    );
	                        }else{
			                    var field_html = template('table_currency_dropdown_template',
			                        {
			                            id: 'CUSTOM_CURRENCY',
			                            value: data,
			                            display_value: full.CURRENCY_NAME,
			                            style:'width:80px'
			                        }
			                    );
	                    }
	                    return field_html;
            	}
            },
            { "data": "CUSTOM_RATE", "width": "80px",
            	"render": function ( data, type, full, meta ) {
            		if(!data){
            			if($('#cost_currency_rate').val()==''){
            					data=0;
            				}else{
            					data=$('#cost_currency_rate').val();
            				}
            			}
            		return '<input type="text" name="custom_rate" value="'+parseFloat(data).toFixed(6)+'" class="form-control" style="width:100px"/>';
            	}
            },
            { "data": "CUSTOM_AMOUNT_CNY", "width": "100px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="custom_amount_cny" value="'+data+'" class="form-control" style="width:120px" disabled/>';
            	}
            },
            
            { "data": "AGENCY_RATE", "width": "100px",//代理费百分比
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="agency_rate" value="'+data+'" class="form-control" style="width:120px" />';
            	}
            },//代理费百分比
            { "data": "AGENCY_AMOUNT_CNY", "width": "100px",//代理费金额
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="agency_amount_cny" value="'+data+'" class="form-control" style="width:120px" />';
            	}
            },//代理费金额
            
            { "data": "CURRENCY_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            },
            { "data": "COMMODITY_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            },
            { "data": "CURRENCY_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            }
        ]
    });
    
    $('#add_trade_cost_table').on('click', function(){
    	if($('#rateExpired').val()=='Y'){
    		$.scojs_message('当前汇率已过期，请更新汇率才能进行添加费用', $.scojs_message.TYPE_ERROR);
    		return;
    	}
        var item={};
        cargoTable.row.add(item).draw(true);
    });
    
    //刷新明细表
    itemOrder.refleshTradeCostItemTable = function(order_id){
    	var url = "/trJobOrder/tableList?order_id="+order_id+"&type=trade_cost";
    	cargoTable.ajax.url(url).load();
    }
    
    if($('#trade_cost_table td').length>1){
    	var col = [3, 6, 10,11,12, 13, 16, 19,21];
    	for (var i=0;i<col.length;i++){
	    	var arr = cargoTable.column(col[i]).data();
    		$('#trade_cost_table tfoot').find('th').eq(col[i]).html(
	    		arr.reduce(function (a, b) {
		    		a = parseFloat(a);
		    		if(isNaN(a)){ a = 0; }                   
		    		b = parseFloat(b);
		    		if(isNaN(b)){ b = 0; }
		    		return (a + b).toFixed(3);
		    	})
	    	);
    	}
    }
    
    $('#trade_cost_table').on('blur', '[name=number]', function(){
    	var row = $(this).parent().parent();
    	var count = $(row.find('[name=number]')).val();
    	if(count==''){
    		$(row.find('[name=custom_number]')).val('');
    	}else if(!isNaN(count)){
    		$(row.find('[name=custom_number]')).val(count);
    	}
    	
    	var total = 0;
		$('#trade_cost_table tr').find('[name=number]:eq(0)').each(function(){
			var a = this.value;
			if(a!=''&&!isNaN(a)){
				total+=parseInt(a);
			}
		})
		$($('.dataTables_scrollFoot tr')[0]).find('th').eq(3).html(total.toFixed(3));
    })
    
    $('#trade_cost_table').on('blur', '[name=custom_number]', function(){
    	var row = $(this).parent().parent();
    	var customNumber = $(row.find('[name=custom_number]')).val();
    	var custom_price = $(row.find('[name=custom_price]')).val();
    	if(customNumber==''){
    		$(row.find('[name=number]')).val('');
    		$(row.find('[name=custom_amount]')).val('');
    	}else if(!isNaN(customNumber)){
    		$(row.find('[name=number]')).val(customNumber);
    	}
    	if(!isNaN(custom_price)&&!isNaN(customNumber)){
    		var custom_amount = parseFloat(custom_price*customNumber);
    		$(row.find('[name=custom_amount]')).val(custom_amount.toFixed(3));
    		var total = 0;
    		$('#trade_cost_table [name=custom_amount]').each(function(){
    			var a = this.value;
    			if(a!=''&&!isNaN(a)){
    				total+=parseFloat(a);
    			}
    		})
    		$($('.dataTables_scrollFoot tr')[0]).find('th').eq(16).html(total.toFixed(3));
    	}
    })
    
    $('#trade_cost_table').on('blur', '[name=custom_rate],[name=custom_price],[name=custom_number]', function(){
    	var row = $(this).parent().parent();
    	var rate = $(this).val();
    	var custom_amount = $(row.find('[name=custom_amount]')).val();
    	if(custom_amount==''||rate==''){
    		$(row.find('[name=custom_amount_cny]')).val('');
    	}else if(!isNaN(custom_amount)&&!isNaN(rate)){
    		$(row.find('[name=custom_amount_cny]')).val((custom_amount*rate).toFixed(3));
    	}
    	var total = 0;
		$('#trade_cost_table [name=custom_amount_cny]').each(function(){
			var a = this.value;
			if(a!=''&&!isNaN(a)){
				total+=parseFloat(a);
			}
		})
		$($('.dataTables_scrollFoot tr')[0]).find('th').eq(19).html(total.toFixed(3));
    })
    
    $('#trade_cost_table').on('blur', '[name=number],[name=custom_price]', function(){
    	var row = $(this).parent().parent();
    	var custom_price = $(row.find('[name=custom_price]')).val();
    	var custom_rate = $(row.find('[name=custom_rate]')).val();
    	var count = $(row.find('[name=number]')).val();
    	if(custom_price==''||count==''){
    		$(row.find('[name=custom_amount]')).val('');
    		$(row.find('[name=custom_amount_cny]')).val('');
    	}else if(!isNaN(custom_price)&&!isNaN(count)){
    		var custom_amount = parseFloat(custom_price*count);
    		$(row.find('[name=custom_amount]')).val(custom_amount.toFixed(3));
    		
    		var total = 0;
    		$('#trade_cost_table [name=custom_amount]').each(function(){
    			var a = this.value;
    			if(a!=''&&!isNaN(a)){
    				total+=parseFloat(a);
    			}
    		})
    		$($('.dataTables_scrollFoot tr')[0]).find('th').eq(16).html(total.toFixed(3));
    	}
    })

    

    $('#trade_cost_table').on('blur', '[name=custom_amount]', function(){
        var row = $(this).parent().parent();
        var custom_amount = $(row.find('[name=custom_amount]')).val();
        var number = $(row.find('[name=number]')).val();
        if(custom_amount==''){
            $(row.find('[name=custom_price]')).val('');
        }else if(!isNaN(custom_amount)&&!isNaN(number)){
            var custom_price = parseFloat(custom_amount/number);
            $(row.find('[name=custom_price]')).val(custom_price);
            
            var total = 0;
            $('#trade_cost_table [name=custom_amount]').each(function(){
                var a = this.value;
                if(a!=''&&!isNaN(a)){
                    total+=parseFloat(a);
                }
            })
            $($('.dataTables_scrollFoot tr')[0]).find('th').eq(16).html(total.toFixed(3));
        }
    })

    $('#trade_cost_table').on('blur', '[name=number], [name=price], [name=tax_refund_rate],[name=agency_rate],[name=domestic_price],[name=value_added_tax],[name=tax_refund_rate_customer],[name=agency_rate],[name=agency_amount_cny]', function(){
    	var this_input = $(this).attr('name');
    	
    	var row = $(this).parent().parent();
    	var price = parseFloat($(row.find('[name=price]')).val());
    	var number = parseFloat($(row.find('[name=number]')).val());
    	var domestic_price = parseFloat($(row.find('[name=domestic_price]')).val());
    	var tax_refund_rate = parseFloat($(row.find('[name=tax_refund_rate]')).val());
    	var tax_refund_rate_customer = parseFloat($(row.find('[name=tax_refund_rate_customer]')).val());
    	
    	var agency_rate = parseFloat($(row.find('[name=agency_rate]')).val());
    	var agency_amount_cny = parseFloat($(row.find('[name=agency_amount_cny]')).val());
    	
    	
    		//计算国内货值
	    	var total ;
	    	var calcPrice ;
	    	var calcNumber;
	    	if(this_input=='number'){
	    		if(!isNaN(price) && !isNaN(number)){
	    			total = parseFloat(price*number);
		    		calcPrice = price;
	    		}else if(!isNaN(domestic_price) && !isNaN(number) && isNaN(price) ){
	    			calcPrice = parseFloat(domestic_price/number);
	    			total = domestic_price;
	    		}
	    		calcNumber = number;
	    	}else if(this_input=='price'){
	    		if(!isNaN(price) && !isNaN(number) ){
	    			total = parseFloat(price*number);
	    			calcNumber = number;
	    		}else if(!isNaN(price) && !isNaN(domestic_price) && isNaN(number) ){
	    			calcNumber = parseFloat(domestic_price/price);
	    			total = domestic_price;
	    		}
	    		calcPrice = price;
	    	} else if(this_input=='domestic_price'){
	    		if(!isNaN(price) && !isNaN(domestic_price)){
	    			calcNumber = parseFloat(domestic_price/price);
	    			calcPrice = price;
	    		}else if(isNaN(price) && !isNaN(number) && !isNaN(domestic_price) ){
	    			calcPrice = parseFloat(domestic_price/number);
	    			calcNumber = number;
	    		}
	    		total = domestic_price;
	    	}
	    	if(!isNaN(calcNumber))
    			$(row.find('[name=number]')).val(calcNumber);
    		if(!isNaN(calcPrice))
    			$(row.find('[name=price]')).val(calcPrice);
    		if(!isNaN(total)){
    			$(row.find('[name=domestic_price]')).val(total.toFixed(3));
            }



	    	
	    	
	    	//计算代理费
	    	var agency_total ;
	    	var agency_calcPrice ;
	    	var agency_calcNumber;
	    	if(this_input=='domestic_price'){
	    		if(!isNaN(domestic_price) && !isNaN(agency_rate)){
	    			agency_total = parseFloat(domestic_price*agency_rate)/100;
	    			agency_calcPrice = agency_rate;
	    		}else if(!isNaN(domestic_price) && !isNaN(agency_rate) && isNaN(agency_amount_cny) ){
	    			agency_rate = parseFloat(agency_amount_cny/domestic_price)*100;
	    			agency_total = agency_amount_cny;
	    		}
	    		agency_calcNumber = domestic_price;
	    	}else if(this_input=='agency_rate'){
	    		if(!isNaN(agency_rate) && !isNaN(domestic_price) ){
	    			agency_total = parseFloat(agency_rate*domestic_price)/100;
	    			agency_calcNumber = domestic_price;
	    		
	    			agency_calcPrice = agency_rate;
	    	   } 
	    	}else if(this_input=='agency_amount_cny'){
	    		 if(!isNaN(agency_amount_cny) && !isNaN(domestic_price) ){
	    			 agency_calcPrice = parseFloat(agency_amount_cny/domestic_price)*100;
	    			 agency_calcNumber = domestic_price;
	    		}
	    		 agency_total = agency_amount_cny;
	    	}
	    		if(!isNaN(agency_calcPrice))
	    			$(row.find('[name=agency_rate]')).val(agency_calcPrice.toFixed(3));
	    		if(!isNaN(agency_total))
	    			$(row.find('[name=agency_amount_cny]')).val(agency_total.toFixed(3));

    		
    		var total = 0;
    		$('#trade_cost_table [name=domestic_price]').each(function(){
    			var a = this.value;
    			if(a!=''&&!isNaN(a)){
    				total+=parseFloat(a);
    			}
    		});
    		$($('.dataTables_scrollFoot tr')[0]).find('th').eq(6).html(total.toFixed(3));
    		
    		
    		if(tax_refund_rate_customer==0){
        		$(row.find('[name=adjusted_tax_refund_amount]')).val(0);
        	}else if(!isNaN(tax_refund_rate_customer)){
        		var value_added_tax = parseFloat($(row.find('[name=value_added_tax]')).val());
        		var adjusted_tax_refund_amount = parseFloat(domestic_price*tax_refund_rate_customer/(1+value_added_tax));
        		
        		$(row.find('[name=adjusted_tax_refund_amount]')).val(adjusted_tax_refund_amount.toFixed(3));
        		var total = 0;
        		$('#trade_cost_table [name=adjusted_tax_refund_amount]').each(function(){
        			var a = this.value;
        			if(a!=''&&!isNaN(a)){
        				total+=parseFloat(a);
        			}
        		});
        		$($('.dataTables_scrollFoot tr')[0]).find('th').eq(11).html(total.toFixed(3));
        	}
    		
    
    	if(price==''){
    		$(row.find('[name=adjusted_unit_price]')).val(0);
    		$(row.find('[name=adjusted_total_price]')).val(0);
    	}else if(!isNaN(price)){
    		var total_adjusted_tax_refund_amount = 0;
    		var total_service_fee = 0;
    		var total_count = 0;
    		$('#trade_cost_table [name=adjusted_tax_refund_amount]').each(function(){
    			var a = this.value;
    			if(a!=''&&!isNaN(a)){
    				total_adjusted_tax_refund_amount+=parseFloat(a);
    			}
    		});
    		$('#charge_service_table [name=currency_total_amount]').each(function(){
    			var a = this.value;
    			if(a!=''&&!isNaN(a)){
    				total_service_fee+=parseFloat(a);
    			}
    		})
        	$('#trade_cost_table tr').find('[name=number]:eq(0)').each(function(){
        		var a = this.value;
    			if(a!=''&&!isNaN(a)){
        			total_count+=parseInt(a);
        		}
    		});
        	var total_difference = total_adjusted_tax_refund_amount-total_service_fee
    		var price_difference = total_difference/total_count;
        	$('#total_difference').val(total_difference.toFixed(3));
    	    $('#price_difference').val(price_difference.toFixed(3));
    	    $('#total_difference_span').text(total_difference.toFixed(3));
    	    $('#price_difference_span').text(price_difference.toFixed(3));
    	    
    		var adjusted_unit_price = (parseFloat(price_difference)+parseFloat(price));
    		$(row.find('[name=adjusted_unit_price]')).val(adjusted_unit_price.toFixed(3));
    		if(number==''){
        		$(row.find('[name=adjusted_total_price]')).val('');
        	}else if(!isNaN(number)){
        		var adjusted_total_price = parseFloat(adjusted_unit_price)*parseFloat(number);
        		$(row.find('[name=adjusted_total_price]')).val(adjusted_total_price.toFixed(3));
        		var total = 0;
        		$('#trade_cost_table [name=adjusted_total_price]').each(function(){
        			var a = this.value;
        			if(a!=''&&!isNaN(a)){
        				total+=parseFloat(a);
        			}
        		});
        		$($('.dataTables_scrollFoot tr')[0]).find('th').eq(13).html(total.toFixed(3));
//        		if(tax_refund_rate==''){
//            		$(row.find('[name=adjusted_tax_refund_amount]')).val('');
//            	}else if(!isNaN(tax_refund_rate)){
//            		var adjusted_tax_refund_amount = (parseFloat(adjusted_total_price)*parseFloat(tax_refund_rate)/1.17);
//            		$(row.find('[name=adjusted_tax_refund_amount]')).val(adjusted_tax_refund_amount.toFixed(3));
//            		var total = 0;
//            		$('#trade_cost_table [name=adjusted_tax_refund_amount]').each(function(){
//            			var a = this.value;
//            			if(a!=''&&!isNaN(a)){
//            				total+=parseFloat(a);
//            			}
//            		});
//            		$($('.dataTables_scrollFoot tr')[0]).find('th').eq(12).html(total.toFixed(3));
//            	}
        	}

            if(tax_refund_rate==''){
                $(row.find('[name=tax_refund_amount]')).val(0);
            }else if(!isNaN(tax_refund_rate)){
                var value_added_tax = parseFloat($(row.find('[name=value_added_tax]')).val());
                var adjusted_total_price = parseFloat($(row.find('[name=adjusted_total_price]')).val());
                var tax_refund_amount = parseFloat(adjusted_total_price*tax_refund_rate/(1+value_added_tax));
                
                $(row.find('[name=tax_refund_amount]')).val(tax_refund_amount.toFixed(3));
                var total = 0;
                $('#trade_cost_table [name=tax_refund_amount]').each(function(){
                    var a = this.value;
                    if(a!=''&&!isNaN(a)){
                        total+=parseFloat(a);
                    }
                });
                $($('.dataTables_scrollFoot tr')[0]).find('th').eq(10).html(total.toFixed(3));
            }

        if(domestic_price==''||agency_rate==''){
            $(row.find('[name=agency_amount_cny]')).val('');
        }else if(!isNaN(domestic_price)&&!isNaN(agency_rate)){
            var total = 0;
            $('#trade_cost_table [name=agency_amount_cny]').each(function(){
                var a = this.value;
                if(a!=''&&!isNaN(a)){
                    total+=parseFloat(a);
                }
            })
            $($('.dataTables_scrollFoot tr')[0]).find('th').eq(21).html(total.toFixed(3));
        }
    	}
    	
    })

    
    
});
});