define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
$(document).ready(function() {

	var deletedTableIds=[];
    //删除一行
    $("#custom_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))
        dataTable.row(tr).remove().draw();
    }); 

    itemOrder.buildCustomItem=function(){
        var cargo_table_rows = $("#custom_table tr");
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
            item.order_type = "cost";//应付
            for(var i = 1; i < row.childNodes.length; i++){
            	var el = $(row.childNodes[i]).find('input,select');
            	var name = el.attr('name'); 
            	
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
    
    var dataTable = eeda.dt({
        id: 'custom_table',
        columns: [
            {
                "render": function ( data, type, full, meta ) {
                    return '<button type="button" class="delete btn btn-default btn-xs">删除</button>';
                }
            },
            { "data": "TYPE", 
                "render": function ( data, type, full, meta ) {
                	var str = '<select name="type" class="form-control search-control">'
                        +'<option value="import" '+(data=='import' ? 'selected':'')+'>进口</option>'
                        +'<option value="export" '+(data=='export' ? 'selected':'')+'>出口</option>'
                        +'</select>';
                	return str;
                }
            }, 
            { "data": "CUSTOM_NO",
            	"render": function ( data, type, full, meta ) {
	            	if(!data)
	                    data='';
	                return '<input type="text" name="item_no"  value="'+data+'" class="form-control" />';
            	}
            }, 
            { "data": "CUSTOM_STATE",
            	"render": function ( data, type, full, meta ) {
            		var str = '<select name="type" class="form-control search-control">'
                        +'<option value="import" '+(data=='import' ? 'selected':'')+'>海关状态</option>'
                        +'<option value="export" '+(data=='export' ? 'selected':'')+'>海关状态</option>'
                        +'<option value="export" '+(data=='export' ? 'selected':'')+'>海关状态</option>'
                        +'<option value="export" '+(data=='export' ? 'selected':'')+'>海关状态</option>'
                        +'<option value="export" '+(data=='export' ? 'selected':'')+'>海关状态</option>'
                        +'</select>';
                	return str;
            	}
            }, 
            { "data": "ORDER_INFO",
            	"render": function ( data, type, full, meta ) {
	            	if(!data)
	                    data='';
	                return '<input type="text" name="item_no"  value="'+data+'" class="form-control" />';
            	}
            },
            { "data": "CARGO_INFO",
            	"render": function ( data, type, full, meta ) {
	            	if(!data)
	                    data='';
	                return '<input type="text" name="item_no"  value="'+data+'" class="form-control" />';
            	}
            }
          ]
    });
	  //刷新明细表
	itemOrder.refleshCustomTable = function(order_id){
		var url = "/customJobOrder/tableList?order_id="+order_id+"&type=custom";
		dataTable.ajax.url(url).load();
	}



});
});