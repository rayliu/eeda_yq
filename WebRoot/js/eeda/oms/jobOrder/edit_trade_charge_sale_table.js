define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
$(document).ready(function() {

	var deletedTableIds=[];
    //删除一行
    $("#trade_sale_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))
        
        cargoTable.row(tr).remove().draw();
    }); 
    
    itemOrder.buildTradeSaleItem=function(){
        var cargo_table_rows = $("#trade_sale_table tr");
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
	    id: 'trade_sale_table',
	    autoWidth: false,
	    columns:[
			{ "width": "30px",
			    "render": function ( data, type, full, meta ) {
			    	if(full.AUDIT_FLAG == 'Y'){
			    		return '<button type="button" class="delete btn btn-default btn-xs" style="width:50px" disabled>删除</button> ';
			    	}else{
			    		return '<button type="button" class="delete btn btn-default btn-xs" style="width:50px">删除</button> ';
			    	}
			    }
			},
            { "data": "COMMODITY_NAME", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="commodity_name" value="'+data+'" class="form-control" style="width:200px"/>';
                }
            },
            { "data": "SPECIFICATION_MODEL", "width": "280px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="specification_model" value="'+data+'" class="form-control" style="width:300px"/>';
                }
            },
            { "data": "NUMBER", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="number" value="'+data+'" class="form-control" style="width:200px"/>';
                }
            },
            { "data": "PRICE", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="price" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "EXPORT_AMOUT", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="export_amout" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "REMARK", "width": "200px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="remark" value="'+data+'" class="form-control" style="width:220px"/>';
            	}
            }
        ]
    });

    $('#add_trade_sale_table').on('click', function(){
        var item={};
        cargoTable.row.add(item).draw(true);
    });
    
    //刷新明细表
    itemOrder.refleshTradeSaleItemTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=trade_sale";
    	cargoTable.ajax.url(url).load();
    }
    

});
});