define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) { 
	$(document).ready(function() {

	    var deletedTableIds=[];

	    //删除一行
	    $("#cars_table").on('click', '.delete', function(e){
	        e.preventDefault();
	        var tr = $(this).parent().parent();
	        deletedTableIds.push(tr.attr('id'));
	        
	        carsTable.row(tr).remove().draw();
	    }); 
	    
	    //构造函数，获得json
	    itemOrder.buildCarsDetail=function(){
	    	var cars_table_rows = $("#cars_table tr");
	        var cargo_items_array=[];
	        for(var index=0; index<cars_table_rows.length; index++){
	            if(index==0)
	                continue;

	            var row = cars_table_rows[index];
	            var empty = $(row).find('.dataTables_empty').text();
	            if(empty)
	            	continue;
	            
	            var id = $(row).attr('id');
	            if(!id){
	                id='';
	            }
	            
	            var item={}
	            item.id = id;
	            item.party_id = $('#partyId').val();
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
	    var carsTable = eeda.dt({
            id: 'cars_table',
            autoWidth: false,
            "drawCallback": function( settings ) {
		        	
		    },
		    ajax: "/serviceProvider/tableList?order_id="+$('#partyId').val()+"&type=cars",
            columns:[
	            {"width": "15px",
	                "render": function ( data, type, full, meta ) {
	                		return '<button type="button" class="delete btn btn-default btn-xs"  >删除</button> ';
	                }
	            },
	            { "data": "CAR_NO","width": "100px",
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                	   data='';
	                   return '<input type="text" name="car_no" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "DRIVER","width": "100px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="driver" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "PHONE","width": "100px",
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                	   data='';
	                   return '<input type="text" name="phone" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "SHORT_PHONE","width": "150px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return '<input   type="text" name="short_phone" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "CARTYPE","width": "100px",
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                	   data='';
	                   return '<input type="text" name="cartype" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "LENGTH","width": "100px",
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                	   data='';
	                   return '<input type="text" name="length" placeholder="请输入数字" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "HUNDRED_FUEL_STANDARD","width": "100px",
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                	   data='';
	                   return '<input type="text" name="hundred_fuel_standard" placeholder="请输入数字" value="'+data+'" class="form-control search-control" />';
	                }
	            }
	        ]
	    });


	    $('#add_cars').on('click', function(){
	        var item={};
	        carsTable.row.add(item).draw(true);
	    });
	    
	    //刷新明细表
	    itemOrder.refleshCarsTable = function(order_id){
	    	var url = "/serviceProvider/tableList?order_id="+order_id+"&type=cars";
	    	carsTable.ajax.url(url).load();
	    }
	    
	});
});
