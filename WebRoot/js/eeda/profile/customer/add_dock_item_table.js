define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) { 
	$(document).ready(function() {

	    var deletedTableIds=[];

	    //删除一行
	    $("#dock_table").on('click', '.delete', function(e){
	        e.preventDefault();
	        var tr = $(this).parent().parent();
	        deletedTableIds.push(tr.attr('id'));
	        
	        cargoTable.row(tr).remove().draw();
	    }); 
	    
	    //构造函数，获得json
	    itemOrder.buildDockItem=function(){
	    	var cargo_table_rows = $("#dock_table tr");
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
	            item.party_type = 'customer';
	            item.office_id = $('#office_id').val();
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
            id: 'dock_table',
            autoWidth: false,
            "drawCallback": function( settings ) {
		        
		    },
            columns:[
	            {"width": "5%",
	                "render": function ( data, type, full, meta ) {
	                		return '<button type="button" class="delete btn btn-default btn-xs" style="width:100%" >删除</button> ';
	                }
	            },
	            { "data": "DOCK_NAME","width": "20%",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return '<input style="width:100%"  type="text" name="dock_name" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            {"data":"LAND_CONTACTS","width":"20%",
	            	 "render": function ( data, type, full, meta ) {
	            		 if(!data)
	            			 data='';
	            		return '<input style="width:100%" type="text" name="land_contacts" value="'+data+'" class="form-control search-control" />';
	            		 
	            	 }
	            },
	            {"data":"LAND_CONTACT_PHONE","width":"20%",
	            	 "render": function ( data, type, full, meta ) {
	            		 if(!data)
	            			 data='';
	            		return '<input style="width:100%" type="text" name="land_contact_phone" value="'+data+'" class="form-control search-control" />';
	            		 
	            	 }
	            },
	            { "data": "DOCK_NAME_ENG","width": "20%",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="dock_name_eng" value="'+data+'" class="form-control search-control" style="width:100%"/>';
	                }
	            },
	            { "data": "DOCK_REGION","width": "20%",
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                	   data='';
	                   return '<input type="text" name="dock_region" value="'+data+'" class="form-control search-control" style="width:100%"/>';
	                }
	            }
	        ]
	    });
	    


	    $('#add_cargo').on('click', function(){
	        var item={};
	        cargoTable.row.add(item).draw(true);
	    });
	    
	    //刷新明细表
	    itemOrder.refleshTable = function(order_id){
	    	var url = "/customer/tableList?order_id="+order_id+"&type=dock";
	    	cargoTable.ajax.url(url).load();
	    }
	    
	});
});
