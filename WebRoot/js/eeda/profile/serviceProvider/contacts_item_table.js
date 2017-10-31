define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) { 
	$(document).ready(function() {

	    var deletedTableIds=[];

	    //删除一行
	    $("#contacts_table").on('click', '.delete', function(e){
	        e.preventDefault();
	        var tr = $(this).parent().parent();
	        deletedTableIds.push(tr.attr('id'));
	        
	        contactsTable.row(tr).remove().draw();
	    }); 
	    
	    //构造函数，获得json
	    itemOrder.buildContactsDetail=function(){
	    	var cargo_table_rows = $("#contacts_table tr");
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
	    var contactsTable = eeda.dt({
            id: 'contacts_table',
            autoWidth: false,
            "drawCallback": function( settings ) {
		        
		    },
            columns:[
	            {"width": "5%",
	                "render": function ( data, type, full, meta ) {
	                		return '<button type="button" class="delete btn btn-default btn-xs" style="width:100%" >删除</button> ';
	                }
	            },
	            { "data": "POSITION","width": "100px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return '<input style="width:100%"  type="text" name="POSITION" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "FULL_NAME","width": "100px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="FULL_NAME" value="'+data+'" class="form-control search-control" style="width:100%"/>';
	                }
	            },
	            { "data": "MOBILE_PHONE","width": "100px",
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                	   data='';
	                   return '<input type="text" name="MOBILE_PHONE" value="'+data+'" class="form-control search-control" style="width:100%"/>';
	                }
	            },
	            { "data": "EMAIL","width": "100px",
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                	   data='';
	                   return '<input type="text" name="EMAIL" value="'+data+'" class="form-control search-control" style="width:100%"/>';
	                }
	            },
	            { "data": "OFFICE_PHONE","width": "100px",
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                	   data='';
	                   return '<input type="text" name="OFFICE_PHONE" value="'+data+'" class="form-control search-control" style="width:100%"/>';
	                }
	            }
	        ]
	    });
	    


	    $('#add_contacts').on('click', function(){
	        var item={};
	        contactsTable.row.add(item).draw(true);
	    });
	    
	    //刷新明细表
	    itemOrder.refleshContactsTable = function(order_id){
	    	var url = "/serviceProvider/tableList?order_id="+order_id+"&type=contacts";
	    	contactsTable.ajax.url(url).load();
	    }
	    
	  //校验
        $('#contacts_table').on('blur','[name=POSITION],[name=FULL_NAME],[name=MOBILE_PHONE],[name=EMAIL],[name=OFFICE_PHONE]',function(){
        	var data = $(this).val();
        	var name = $(this).attr("name");
        	var len = $.trim(data).length;
        	if(name=="POSITION"||name=="FULL_NAME"){
        		var re = /^.{100,}$/;
        		if(re.test(data)&&len>0){
        			$(this).parent().append("<span style='color:red;display:block;' class='error_span'>只能输入长度100内的字符串</span>");
        			return;
        		}
        	}
        	if(name=="MOBILE_PHONE"){
        		var re = /^[\u4e00-\u9fa5]$/;
            	if(re.test(data)){
            		$(this).parent().append("<span style='color:red;display:block;' class='error_span'>不能输入汉字</span>");
            	}else if(len>100){
            		$(this).parent().append("<span style='color:red;display:block;' class='error_span'>只能输入长度100内的字符串</span>");
            	}
        	}
        	if(name=="EMAIL"){
        		var re = /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;
        		if(!re.test(data)&&len>0){
        			$(this).parent().append("<span style='color:red;display:block;' class='error_span'>邮箱格式不正确</span>");
        			return;
        		}
        	}
        	if(name=="OFFICE_PHONE"){
        		var re = /^[\u4e00-\u9fa5]$/;
            	if(re.test(data)){
            		$(this).parent().append("<span style='color:red;display:block;' class='error_span'>不能输入汉字</span>");
            	}else if(len>100){
            		$(this).parent().append("<span style='color:red;display:block;' class='error_span'>只能输入长度100内的字符串</span>");
            	}
        	}
        });
        $('#contacts_table').on('focus','[name=POSITION],[name=FULL_NAME],[name=MOBILE_PHONE],[name=EMAIL],[name=OFFICE_PHONE]',function(){
        	$(this).parent().find("span").remove();
        });
	    
	});
});
