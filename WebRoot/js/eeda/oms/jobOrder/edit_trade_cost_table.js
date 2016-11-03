define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
$(document).ready(function() {

	var deletedTableIds=[];
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
            for(var i = 1; i < row.childNodes.length; i++){
            	var el = $(row.childNodes[i]).find('input, select');
            	var name = el.attr('name'); //name='abc'
            	
            	if(el && name){
                	var value = el.val();//元素的值
                	item[name] = value;
            	}
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
    	eeda.bindTableField('trade_cost_table','SP','/serviceProvider/searchCompany','');
        eeda.bindTableFieldCurrencyId('trade_cost_table','CUSTOM_CURRENCY','/serviceProvider/searchCurrency','');
    };
    var cargoTable = eeda.dt({
	    id: 'trade_cost_table',
	    autoWidth: false,
	    drawCallback: function( settings ) {
            bindFieldEvent();
            $.unblockUI();
        },
	    columns:[
			{ "width": "30px",
			    "render": function ( data, type, full, meta ) {
			    		return '<button type="button" class="delete btn btn-default btn-xs" style="width:50px">删除</button> ';
			    }
			},
			{ "data": "SP", "width": "180px",
				"render": function ( data, type, full, meta ) {
                		if(!data)
                            data='';
                        var field_html = template('table_dropdown_template',
                            {
                                id: 'SP',
                                value: data,
                                display_value: full.SP_NAME,
                                style:'width:200px'
                            }
                        );
                        return field_html;
				}
			},
            { "data": "COMMODITY_NAME", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="commodity_name" value="'+data+'" class="form-control" style="width:200px"/>';
                }
            },
            { "data": "SPECIFICATION_MODEL", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="specification_model" value="'+data+'" class="form-control" style="width:200px"/>';
                }
            },
            { "data": "NUMBER", "width": "180px","className":"number",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="number" value="'+data+'" class="form-control" style="width:200px"/>';
                }
            },
            { "data": "UNIT", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="unit" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "PRICE", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="price" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "DOMESTIC_PRICE", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="domestic_price" value="'+data+'" class="form-control" style="width:200px" disabled/>';
            	}
            },
            { "data": "VALUE_ADDED_TAX", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="value_added_tax" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "TAX_REFUND_RATE", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="tax_refund_rate" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "TAX_REFUND_AMOUNT", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="tax_refund_amount" value="'+data+'" class="form-control" style="width:200px" disabled/>';
            	}
            },
            { "data": "ADJUSTED_TAX_REFUND_AMOUNT", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="adjusted_tax_refund_amount" value="'+data+'" class="form-control" style="width:200px" disabled/>';
            	}
            },
            { "data": "ADJUSTED_UNIT_PRICE", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="adjusted_unit_price" value="'+data+'" class="form-control" style="width:200px" disabled/>';
            	}
            },
            { "data": "ADJUSTED_TOTAL_PRICE", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="adjusted_total_price" value="'+data+'" class="form-control" style="width:200px" disabled/>';
            	}
            },
            { "data": "CUSTOM_PRICE", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="custom_price" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "number", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="custom_number" value="'+data+'" class="form-control" style="width:200px"/>';
                }
            },
            { "data": "CUSTOM_AMOUNT", "width": "180px","className":"currency_total_amount",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="custom_amount" value="'+data+'" class="form-control" style="width:200px" disabled/>';
            	}
            },
            { "data": "CUSTOM_CURRENCY", "width": "60px",
            	"render": function ( data, type, full, meta ) {
	                	if(!data)
	                        data='';
	                    var field_html = template('table_dropdown_template',
	                        {
	                            id: 'CUSTOM_CURRENCY',
	                            value: data,
	                            display_value: full.CURRENCY_NAME,
	                            style:'width:80px'
	                        }
	                    );
	                    return field_html;
            	}
            },
            { "data": "CUSTOM_RATE", "width": "180px","className":"currency_rate",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="custom_rate" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "CUSTOM_AMOUNT_CNY", "width": "180px","className":"cny_total_amount",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="custom_amount_cny" value="'+data+'" class="form-control" style="width:200px" disabled/>';
            	}
            },
            { "data": "CURRENCY_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            },
            { "data": "SP_NAME", "visible": false,
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return data;
            	}
            }
        ]
    });
    
    $('#add_trade_cost_table').on('click', function(){
        var item={};
        cargoTable.row.add(item).draw(true);
    });
    
    //刷新明细表
    itemOrder.refleshTradeCostItemTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=trade_cost";
    	cargoTable.ajax.url(url).load();
    }
    
    if($('#trade_cost_table td').length>1){
    	var col = [4, 7, 10, 11, 13, 16, 19];
    	for (var i=0;i<col.length;i++){
	    	var arr = cargoTable.column(col[i]).data();
    		$('#trade_cost_table tfoot').find('th').eq(col[i]).html(
	    		arr.reduce(function (a, b) {
		    		a = parseFloat(a);
		    		if(isNaN(a)){ a = 0; }                   
		    		b = parseFloat(b);
		    		if(isNaN(b)){ b = 0; }
		    		return a + b;
		    	})
	    	);
    	}
    }
    
    $('#trade_cost_table').on('keyup', '[name=number]', function(){
    	var row = $(this).parent().parent();
    	var count = $(row.find('[name=number]')).val();
    	if(count==''){
    		$(row.find('[name=custom_number]')).val('');
    	}else if(!isNaN(count)){
    		$(row.find('[name=custom_number]')).val(count);
    	}
    	
    	var total = 0;
		$('#trade_cost_table [name=number]').each(function(){
			var a = this.value;
			if(a!=''&&!isNaN(a)){
				total+=parseInt(a);
			}
		})
		$($('.dataTables_scrollFoot tr')[0]).find('th').eq(4).html(total);
    })
    
    $('#trade_cost_table').on('keyup', '[name=custom_number]', function(){
    	var row = $(this).parent().parent();
    	var customNumber = $(row.find('[name=custom_number]')).val();
    	if(customNumber==''){
    		$(row.find('[name=number]')).val('');
    	}else if(!isNaN(customNumber)){
    		$(row.find('[name=number]')).val(customNumber);
    	}
    })
    
    $('#trade_cost_table').on('keyup', '[name=number],[name=custom_price]', function(){
    	var row = $(this).parent().parent();
    	var custom_price = $(row.find('[name=custom_price]')).val();
    	var custom_rate = $(row.find('[name=custom_rate]')).val();
    	var count = $(row.find('[name=number]')).val();
    	if(custom_price==''||count==''){
    		$(row.find('[name=custom_amount]')).val('');
    		$(row.find('[name=custom_amount_cny]')).val('');
    	}else if(!isNaN(custom_price)&&!isNaN(count)){
    		var custom_amount = parseFloat(custom_price*count);
    		$(row.find('[name=custom_amount]')).val(custom_amount.toFixed(4));
    		
    		var total = 0;
    		$('#trade_cost_table [name=custom_amount]').each(function(){
    			var a = this.value;
    			if(a!=''&&!isNaN(a)){
    				total+=parseFloat(a);
    			}
    		})
    		$($('.dataTables_scrollFoot tr')[0]).find('th').eq(16).html(total);
    	}
    })

    $('#trade_cost_table').on('keyup', '[name=number], [name=price], [name=tax_refund_rate]', function(){
    	
    	var row = $(this).parent().parent();
    	var price = $(row.find('[name=price]')).val()
    	var count = $(row.find('[name=number]')).val()
    	var tax_refund_rate = $(row.find('[name=tax_refund_rate]')).val()
    	
    	if(price==''||count==''){
    		$(row.find('[name=domestic_price]')).val('');
    	}else if(!isNaN(price)&&!isNaN(count)){
    		var domestic_price = parseFloat(price*count);
    		$(row.find('[name=domestic_price]')).val(domestic_price.toFixed(4));
    		var total = 0;
    		$('#trade_cost_table [name=domestic_price]').each(function(){
    			var a = this.value;
    			if(a!=''&&!isNaN(a)){
    				total+=parseFloat(a);
    			}
    		})
    		$($('.dataTables_scrollFoot tr')[0]).find('th').eq(7).html(total);
    		if(tax_refund_rate==''){
        		$(row.find('[name=tax_refund_amount]')).val('');
        	}else if(!isNaN(tax_refund_rate)){
        		var tax_refund_amount = parseFloat(domestic_price*tax_refund_rate/1.17);
        		$(row.find('[name=tax_refund_amount]')).val(tax_refund_amount.toFixed(4));
        		var total = 0;
        		$('#trade_cost_table [name=tax_refund_amount]').each(function(){
        			var a = this.value;
        			if(a!=''&&!isNaN(a)){
        				total+=parseFloat(a);
        			}
        		})
        		$($('.dataTables_scrollFoot tr')[0]).find('th').eq(10).html(total);
        	}
    	}
    
    	if(price==''){
    		$(row.find('[name=adjusted_unit_price]')).val('');
    		$(row.find('[name=adjusted_total_price]')).val('');
    	}else if(!isNaN(price)){
    		var total_tax_refund_amount = 0;
    		var total_service_fee = 0;
    		var total_count = 0;
    		$('#trade_cost_table [name=tax_refund_amount]').each(function(){
    			var a = this.value;
    			if(a!=''&&!isNaN(a)){
    				total_tax_refund_amount+=parseFloat(a);
    			}
    		})
    		$('#charge_service_table [name=fee_amount]').each(function(){
    			var a = this.value;
    			if(a!=''&&!isNaN(a)){
    				total_service_fee+=parseFloat(a);
    			}
    		})
        	$('#trade_cost_table [name=number]').each(function(){
        		var a = this.value;
    			if(a!=''&&!isNaN(a)){
        			total_count+=parseInt(a);
        		}
    		})
        	var total_difference = total_tax_refund_amount-total_service_fee
    		var price_difference = total_difference/total_count;
        	$('#total_difference').text(total_difference.toFixed(3));
    	    $('#price_difference').text(price_difference.toFixed(6));
    	    
    		var adjusted_unit_price = (parseFloat(price_difference)+parseFloat(price));
    		$(row.find('[name=adjusted_unit_price]')).val(adjusted_unit_price.toFixed(6));
    		if(count==''){
        		$(row.find('[name=adjusted_total_price]')).val('');
        	}else if(!isNaN(count)){
        		var adjusted_total_price = parseFloat(adjusted_unit_price)*parseFloat(count);
        		$(row.find('[name=adjusted_total_price]')).val(adjusted_total_price.toFixed(3));
        		var total = 0;
        		$('#trade_cost_table [name=adjusted_total_price]').each(function(){
        			var a = this.value;
        			if(a!=''&&!isNaN(a)){
        				total+=parseFloat(a);
        			}
        		})
        		$($('.dataTables_scrollFoot tr')[0]).find('th').eq(13).html(total);
        		if(tax_refund_rate==''){
            		$(row.find('[name=adjusted_tax_refund_amount]')).val('');
            	}else if(!isNaN(tax_refund_rate)){
            		var adjusted_tax_refund_amount = (parseFloat(adjusted_total_price)*parseFloat(tax_refund_rate)/1.17);
            		$(row.find('[name=adjusted_tax_refund_amount]')).val(adjusted_tax_refund_amount.toFixed(3));
            		var total = 0;
            		$('#trade_cost_table [name=adjusted_tax_refund_amount]').each(function(){
            			var a = this.value;
            			if(a!=''&&!isNaN(a)){
            				total+=parseFloat(a);
            			}
            		})
            		$($('.dataTables_scrollFoot tr')[0]).find('th').eq(11).html(total);
            	}
        	}
    	}
    	
    })

    
    
});
});