define(['jquery', 'metisMenu'], function ($, metisMenu) {
$(document).ready(function() {

	var deletedTableIds=[];
    //删除一行
    $("#land_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))
        
        cargoTable.row(tr).remove().draw();
    }); 
    //添加一行
    $('#add_land').on('click', function(){
        var item={};
        cargoTable.row.add(item).draw(true);
    });
    

    itemOrder.buildLoadItem=function(){
        var cargo_table_rows = $("#land_table tr");
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
            	var el = $(row.childNodes[i]).find('input');
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
    var cargoTable = $('#land_table').DataTable({
        "processing": true,
        "searching": false,
        "paging": false,
        "info": false,
        "scrollX":  true,
        "autoWidth": true,
        "language": {
            "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
        },
        "createdRow": function ( row, data, index ) {
            $(row).attr('id', data.ID);
        },
        "columns": [
            { "width": "3px",
                "render": function ( data, type, full, meta ) {
                	return '<button type="button" class="delete btn btn-default btn-xs">删除</button>';
                }
            },
            { "data": "UNLOAD_TYPE", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="number" name="unload_type" value="'+data+'" class="form-control easyui-numberbox" data-options="max:0"/>';
                }
            },
            { "data": "TRANSPORT_COMPANY", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="transport_company" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "DRIVER", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="driver" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "DRIVER_TEL", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="driver_tel" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "CAR_TYPE", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="car_type" value="'+data+'" class="form-control"/>';
                }
            },
            { "data": "CAR_NO", 
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="car_no" value="'+data+'" class="form-control"/>';
            	}
            },
            { "data": "CARGO_CONTACTS", 
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="cargo_contacts" value="'+data+'" class="form-control"/>';
            	}
            },
            { "data": "PHONE", 
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="phone" value="'+data+'" class="form-control"/>';
            	}
            },
            { "data": "ADDRESS", 
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="address" value="'+data+'" class="form-control"/>';
            	}
            },
            { "data": "ETA", 
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="eta" value="'+data+'" class="form-control"/>';
            	}
            },
            { "data": "CARGO_INFO", 
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="cargo_info" value="'+data+'" class="form-control"/>';
            	}
            },
            { "data": "SIGN_DESC", 
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="sign_desc" value="'+data+'" class="form-control"/>';
            	}
            },
            { "data": "SIGN_STATUS", 
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="sign_status" value="'+data+'" class="form-control"/>';
            	}
            }
        ]
    });

    //刷新明细表
    itemOrder.refleshLandItemTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=land";
    	cargoTable.ajax.url(url).load();
    }
    

});
});