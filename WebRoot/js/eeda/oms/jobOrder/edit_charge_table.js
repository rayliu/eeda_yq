define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {

	var deletedTableIds=[];
	
    //删除一行
    $("#charge_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))
        
        chargeTable.row(tr).remove().draw();
    }); 

    itemOrder.buildChargeDetail=function(){
        var cargo_table_rows = $("#charge_table tr");
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
            	var name = $(row.childNodes[i]).find('input').attr('name');
            	var value = $(row.childNodes[i]).find('input').val();
            	if(name){
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
    var chargeTable = eeda.dt({
        id: 'charge_table',
        columns:[
            { "width": "30px",
                "render": function ( data, type, full, meta ) {
                	return '<button type="button" class="delete btn btn-default btn-xs">删除</button> ';
                }
            },
            { "data": "TYPE", 
                "render": function ( data, type, full, meta ) {
                    var str = '<select name="container_type" class="form-control search-control">'
                               +'<option value="20GP" '+(data=='20GP' ? 'selected':'')+'>海运</option>'
                               +'<option value="40GP" '+(data=='40GP' ? 'selected':'')+'>空运</option>'
                               +'<option value="45GP" '+(data=='45GP' ? 'selected':'')+'>陆运</option>'
                               +'<option value="45GP" '+(data=='45GP' ? 'selected':'')+'>报关</option>'
                               +'<option value="45GP" '+(data=='45GP' ? 'selected':'')+'>保险</option>'
                               +'</select>';
                    return str;
                }
            },
            { "data": "SP_ID",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="sp_id" style="width:80px" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "CHARGE_ID",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="charge_id" style="width:80px" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "PRICE", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='0';
                    return '<input type="text" name="price" style="width:80px" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "AMOUNT",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='1';
                    return '<input type="number" name="amount" style="width:80px" value="'+data+'" class="form-control "/>';
                }
            },
            { "data": "UNIT_ID", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="unit_id" style="width:80px" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "CURRENCY_ID", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='1';
                    return '<input type="text" name="currency_id" style="width:80px" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "TOTAL_AMOUNT", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='0';
                    return '<input type="text" name="total_amount" style="width:80px" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "EXCHANGE_RATE", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='0';
                    return '<input type="text" name="exchange_rate" style="width:80px" value="'+data+'" class="form-control"/>';
                }
            },
            { "data": "CURRENCY_TOTAL_AMOUNT", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='0';
                    return '<input type="text" name="currency_total_amount" style="width:80px" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "RECEIPT_NO", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="receipt_no" style="width:80px" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "INVOICE_NO", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="invoice_no" style="width:80px" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "REMARK",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="remark" style="width:80px" value="'+data+'" class="form-control" />';
                }
            }
        ]
    });

    $('#add_charge').on('click', function(){
        var item={};
        chargeTable.row.add(item).draw(true);
    });
    
    //刷新明细表
    itemOrder.refleshChargeTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=charge";
    	chargeTable.ajax.url(url).load();
    }
} );
});