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

    itemOrder.buildCustom=function(){
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
        autoWidth: false,
        columns: [
            {
                "render": function ( data, type, full, meta ) {
                    return '<button type="button" class="delete btn btn-default btn-xs" style="width:40px">删除</button>';
                }
            },
            { "data": "TYPE", 
                "render": function ( data, type, full, meta ) {
                	var str = '<select name="type" class="form-control search-control" style="width:80px">'
                        +'<option value="进口" '+(data=='进口' ? 'selected':'')+'>进口</option>'
                        +'<option value="出口" '+(data=='出口' ? 'selected':'')+'>出口</option>'
                        +'</select>';
                	return str;
                }
            }, 
            { "data": "CUSTOM_NO",
            	"render": function ( data, type, full, meta ) {
	            	if(!data)
	                    data='';
	                return '<input type="text" name="custom_no"  value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            }, 
            { "data": "CUSTOM_STATE",
            	"render": function ( data, type, full, meta ) {
            		var str = '<select name="custom_state" class="form-control search-control" style="width:80px">'
                        +'<option value="审单" '+(data=='审单' ? 'selected':'')+'>审单</option>'
                        +'<option value="查验" '+(data=='查验' ? 'selected':'')+'>查验</option>'
                        +'<option value="征税" '+(data=='征税' ? 'selected':'')+'>征税</option>'
                        +'<option value="放行" '+(data=='放行' ? 'selected':'')+'>放行</option>'
                        +'</select>';
                	return str;
            	}
            }, 
            { "data": "ORDER_INFO",
            	"render": function ( data, type, full, meta ) {
	            	if(!data)
	                    data='';
	                return '<input type="text" name="order_info"  value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "CARGO_INFO",
            	"render": function ( data, type, full, meta ) {
	            	if(!data)
	                    data='';
	                return '<input type="text" name="cargo_info"  value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            }
          ]
    });
	  //刷新明细表
	itemOrder.refleshCustomTable = function(order_id){
		var url = "/customJobOrder/tableList?order_id="+order_id+"&type=custom";
		dataTable.ajax.url(url).load();
	}
	
	//添加一行
	$('#confirmCustomDetailBtn').click(function(e){
		$('#returnCustomOrderExportModal').click()
		var type = $('#customDetailExportForm input[name="type"]:checked').val();
		var item={};
		item.TYPE = type;
		item.CUSTOM_NO = "";
		item.ORDER_INFO = "";
		item.CARGO_INFO = "";

		dataTable.row.add(item).draw(true);
	})


});
});