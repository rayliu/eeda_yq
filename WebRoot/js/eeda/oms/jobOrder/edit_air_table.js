define(['jquery', 'metisMenu', 'dataTablesBootstrap'], function ($, metisMenu) {
$(document).ready(function() {

	var deletedTableIds=[];
    //删除一行
    $("#air_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))
        
        cargoTable.row(tr).remove().draw();
    }); 

    itemOrder.buildAirItem=function(){
        var cargo_table_rows = $("#air_table tr");
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
    var cargoTable = $('#air_table').DataTable({
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
            { "width": "30px",
                "render": function ( data, type, full, meta ) {
                	return '<button type="button" class="delete btn btn-default btn-xs">删除</button>';
                }
            },
            { "data": "AIR_COMPANY", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="number" name="air_company" value="'+data+'" class="form-control easyui-numberbox" data-options="max:0"/>';
                }
            },
            { "data": "FLIGHT_NO", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="flight_no" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "VOYAGE_NO", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="voyage_no" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "START_FROM", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="start_from" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "ETD", 
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="etd" value="'+data+'" class="form-control" />';
            	}
            },
            { "data": "DESTINATION", 
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="destination" value="'+data+'" class="form-control" />';
            	}
            },
            { "data": "ETA", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="eta" value="'+data+'" class="form-control"/>';
                }
            }
        ]
    });

    $('#add_air').on('click', function(){
        var item={};
        cargoTable.row.add(item).draw(true);
    });
    
    //刷新明细表
    itemOrder.refleshAirItemTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=air";
    	cargoTable.ajax.url(url).load();
    }
    

});
});