define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
$(document).ready(function() {

	var deletedTableIds=[];
    //删除一行
    $("#custom_item_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))
        
        cargoTable.row(tr).remove().draw();
    }); 
    //添加一行
    $('#add_custom_item').on('click', function(){
        var item={};
        cargoTable.row.add(item).draw(true);
    });

    itemOrder.buildCustomItem=function(){
        var cargo_table_rows = $("#custom_item_table tr");
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
    
    //------------事件处理
	 var cargoTable = eeda.dt({
	        id: 'custom_item_table',
	        autoWidth: false,
	        columns:[
	         {"data": "ID","width": "10px",
			    "render": function ( data, type, full, meta ) {
			    	if(data)
			    		return '<input type="checkbox" name="subBox" style="width:30px">';
			    	else 
			    		return '<input type="checkbox" style="width:30px" disabled>';
			    }
			},
            {"width": "20px",
                "render": function ( data, type, full, meta ) {
                	return '<button type="button" class="delete btn btn-default btn-xs" style="width:40px">删除</button>';
                }
            },
            { "data": "ITEM_NO", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="item_no"  value="'+data+'" class="form-control" style="width:200px"/>';
                }
            },
            { "data": "COMMODITY_NO", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="commodity_no"  value="'+data+'" class="form-control" style="width:200px"/>';
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
            { "data": "QUANTITY_AND_UNIT","width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="quantity_and_unit" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "DESTINATION_COUNTRY","width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="destination_country" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "PRICE","width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="price"  value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "TOTAL","width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="total" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "CURRENCY","width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="currency"  value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "EXEMPTION","width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="exemption"  value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            }
           
        ]
    });

    //刷新明细表
    itemOrder.refleshCustomItemTable = function(order_id){
    	var url = "/customJobOrder/tableList?order_id="+order_id+"&type=customItem";
    	cargoTable.ajax.url(url).load();
    }
    
    //全选
    $("#allCheckOfCustomItem").click(function() {
        $('input[name="subBox"]').prop("checked",this.checked); 
    });
    var $subBox = $("input[name='subBox']");
    $subBox.click(function(){
        $("#allCheckOfCustomItem").prop("checked",$subBox.length == $("input[name='subBox']:checked").length ? true : false);
    });


});
});