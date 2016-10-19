define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
$(document).ready(function() {

	var deletedTableIds=[];
    //删除一行
    $("#charge_service_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))
        
        cargoTable.row(tr).remove().draw();
    }); 
    
    itemOrder.buildTradeServiceItem=function(){
        var cargo_table_rows = $("#charge_service_table tr");
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
    var cargoTable = eeda.dt({
	    id: 'charge_service_table',
	    autoWidth: false,
	    columns:[
            { "data": "FEE_NAME", "width": "180px",
                "render": function ( data, type, full, meta ) {
                	var str = '<select name="type" class="form-control search-control" style="width:100px">'
                        +'<option value="海运费" '+(data=='海运费' ? 'selected':'')+'>海运费</option>'
                        +'<option value="空运费" '+(data=='空运费' ? 'selected':'')+'>空运费</option>'
                        +'<option value="陆运费" '+(data=='陆运费' ? 'selected':'')+'>陆运费</option>'
                        +'<option value="报关费" '+(data=='报关费' ? 'selected':'')+'>报关费</option>'
                        +'<option value="拖车费" '+(data=='拖车费' ? 'selected':'')+'>拖车费</option>'
                        +'<option value="出口代理费" '+(data=='出口代理费' ? 'selected':'')+'>出口代理费</option>'
                        +'</select>';
                	return str;
                }
            },
            { "data": "FEE_AMOUNT", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="fee_amount" value="'+data+'" class="form-control" style="width:200px"/>';
                }
            }
        ]
    });

    $('#add_charge_service_table').on('click', function(){
        var item={};
        cargoTable.row.add(item).draw(true);
    });
    
    //刷新明细表
    itemOrder.refleshTradeServiceItemTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=trade_service";
    	cargoTable.ajax.url(url).load();
    }
    

});
});