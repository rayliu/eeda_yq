define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) { 
	$(document).ready(function() {

	    var deletedTableIds=[];

	    //删除一行
	    $("#shipping_item_table").on('click', '.delete', function(e){
	        e.preventDefault();
	        var tr = $(this).parent().parent();
	        deletedTableIds.push(tr.attr('id'));
	        
	        cargoTable.row(tr).remove().draw();
	    }); 
	    
	    //构造函数，获得json
	    salesOrder.buildShippingItem=function(){
	    	var cargo_table_rows = $("#shipping_item_table tr");
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
	            	var name = $(row.childNodes[i]).find('input,select').attr('name');
	            	var value = $(row.childNodes[i]).find('input,select').val();
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
	    var cargoTable = eeda.dt({
            id: 'shipping_item_table',
            autoWidth: false,
            columns:[
	            {"width": "10px",
	                "render": function ( data, type, full, meta ) {
	                		return '<button type="button" class="delete btn btn-default btn-xs" style="width:40px">删除</button> ';
	                }
	            },
	            { "data": "CONTAINER_NUMBER","width": "220px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="container_number" value="'+data+'" class="form-control search-control" style="width:240px" />';
	                }
	            },
	            { "data": "PACK","width": "120px",
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                	   data='';
	                   return '<input type="text" name="pack" value="'+data+'" class="form-control search-control" style="width:140px"/>';
	                }
	            },
	            { "data": "NUMBER","width": "120px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="number" value="'+data+'" class="form-control search-control" style="width:140px"/>';
	                }
	            },
	            { "data": "CARGO_NAME","width": "220px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="cargo_name" value="'+data+'" class="form-control search-control" style="width:240px" />';
	                }
	            },
	            { "data": "NET_WEIGHT","width": "120px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input style="width:140px" type="text" name="net_weight" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "GROSS_WEIGHT","width": "120px", 
	                "render": function ( data, type, full, meta ) {
	                	if(!data)
	                        data='';
	                	return '<input style="width:140px" type="text" name="gross_weight" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "VOLUME","width": "120px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="volume" value="'+data+'" class="form-control search-control" style="width:140px" />';
	                }
	            }
	         ]
	    });
	    
	    
	    $('#add_shipping_item').on('click', function(){
	        var item={};
	        cargoTable.row.add(item).draw(true);
	    });
	    
	    //刷新明细表
	    salesOrder.refleshShippingItemTable = function(order_id){
	    	var url = "/customPlanOrder/tableList?order_id="+order_id+"&type=shipping";
	    	cargoTable.ajax.url(url).load();
	    }
	
	    //校验
	    $('#shipping_item_table').on("blur","[name=container_number],[name=pack],[name=number],[name=cargo_name]," 
	    		+"[name=net_weight],[name=gross_weight],[name=volume]",function(){
			var data = $(this).val();
			var name = $(this).attr("name");
			var len = $.trim(data).length;
			if(name=="container_number"||name=="pack"||name=="cargo_name"){
				if(len>255){
					$(this).parent().append("<span style='color:red;' class='error_span'>请输入长度255以内的字符串</span>")
				}
			}
			if(name=="net_weight"||name=="gross_weight"||name=="volume"||name=="number"){
				var re = /^\d{0,9}(\.?\d{1,5})$/;
				if(!re.test(data)||len>15){
					$(this).parent().append("<span style='color:red;' class='error_span'>请输入合法的数字</span>")
				}
			}
		});
	    $('#shipping_item_table').on("focus","[name=container_number],[name=pack],[name=number],[name=cargo_name]," 
	    		+"[name=net_weight],[name=gross_weight],[name=volume]",function(){
			$(this).parent().find("span").remove();
		});
	});
});
