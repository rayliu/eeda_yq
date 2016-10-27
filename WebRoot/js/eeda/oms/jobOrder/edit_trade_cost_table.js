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
        for(var index=0; index<cargo_table_rows.length; index++){
            if(index==0)
                continue;

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
            { "data": "NUMBER", "width": "180px",
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
            		return '<input type="text" name="domestic_price" value="'+data+'" class="form-control" style="width:200px"/>';
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
            		return '<input type="text" name="tax_refund_amount" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		return '' ;
            	}
            },
            { "data": "ADJUSTED_TAX_REFUND_AMOUNT", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="adjusted_tax_refund_amount" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "ADJUSTED_UNIT_PRICE", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="adjusted_unit_price" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "ADJUSTED_TOTAL_PRICE", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="adjusted_total_price" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "CUSTOM_PRICE", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="custom_price" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "CUSTOM_AMOUNT", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="custom_amount" value="'+data+'" class="form-control" style="width:200px"/>';
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
    

});
});