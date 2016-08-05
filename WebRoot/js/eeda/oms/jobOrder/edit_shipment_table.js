define(['jquery', 'metisMenu', 'dataTablesBootstrap'], function ($, metisMenu) {
$(document).ready(function() {

	var deletedTableIds=[];
    //删除一行
    $("#ocean_cargo_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))
        
        cargoTable.row(tr).remove().draw();
    }); 

    itemOrder.buildOceanItem=function(){
        var cargo_table_rows = $("#ocean_cargo_table tr");
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
    var cargoTable = $('#ocean_cargo_table').DataTable({
        "processing": true,
        "searching": false,
        "paging": false,
        "info": false,
        "scrollX":  true,
        "autoWidth": false,
        "language": {
            "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
        },
        "createdRow": function ( row, data, index ) {
            $(row).attr('id', data.ID);
        },
        "columns": [
            { "data":"ID","width": "3px",
                "render": function ( data, type, full, meta ) {
                	if(data)
                		return '<input type="checkbox" class="checkBox">';
                	else 
                		return '<input type="checkbox" class="checkBox" disabled>';
                }
            },
            { "width": "10px",
                "render": function ( data, type, full, meta ) {
                	return '<button type="button" class="delete btn btn-default btn-xs">删除</button>';
                }
            },
           
            { "data": "LOAD_TYPE", 
                "render": function ( data, type, full, meta ) {
                   if(!data)
                	   data='';
                    
                   var str= '<select name="load_type" class="form-control search-control">'
                	   	 	+'<option></option>'
		                   +'<option value="FCL" '+ (data=='FCL'?'selected':'') +'>FCL</option>'
		                   +'<option value="LCL" '+ (data=='LCL'?'selected':'') +'>LCL</option>'
		                   +'<option value="FTL" '+ (data=='FTL'?'selected':'') +'>FTL</option>'
		                   +'<option value="LTL" '+ (data=='LTL'?'selected':'') +'>LTL</option>'
		                   +'</select>';
		           return str;
                }
            },
            { "data": "CONTAINER_TYPE", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    var str = '<select name="container_type" class="form-control search-control">'
                    			+'<option></option>'
			                   +'<option value="20GP" '+(data=='20GP' ? 'selected':'')+'>20GP</option>'
			                   +'<option value="40GP" '+(data=='40GP' ? 'selected':'')+'>40GP</option>'
			                   +'<option value="45GP" '+(data=='45GP' ? 'selected':'')+'>45GP</option>'
			                   +'</select>';
                    return str;
                }
            },
            { "data": "CONTAINER_NO", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="container_no" value="'+data+'" class="form-control easyui-numberbox" data-options="max:0"/>';
                }
            },
            { "data": "SEAL_NO", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="seal_no" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "PIECES", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="pieces" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "GROSS_WEIGHT", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="gross_weight" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "VOLUME", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="volume" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "VGM", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="vgm" value="'+data+'" class="form-control"/>';
                }
            }
        ]
    });

    $('#add_ocean_cargo').on('click', function(){
        var item={};
        cargoTable.row.add(item).draw(true);
    });
    
    //刷新明细表
    itemOrder.refleshOceanTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=shipment";
    	cargoTable.ajax.url(url).load();
    }
    
    //checkbox选中则button可点击
/*	$('#cargo_table').on('click','.checkBox',function(){
		var hava_check = 0;
		$('#cargo_table input[type="checkbox"]').each(function(){	
			var checkbox = $(this).prop('checked');
    		if(checkbox){
    			hava_check=1;
    		}	
		})
		if(hava_check>0){
			$('#create_truckOrder').attr('disabled',false);
		}else{
			$('#create_truckOrder').attr('disabled',true);
		}
	});*/

});
});