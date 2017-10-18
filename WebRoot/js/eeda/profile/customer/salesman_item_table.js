define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) { 
	$(document).ready(function() {

	    var deletedTableIds=[];

	    //删除一行
	    $("#salesman_table").on('click', '.delete', function(e){
	        e.preventDefault();
	        var tr = $(this).parent().parent();
	        deletedTableIds.push(tr.attr('id'));
	        
	        salesmanTable.row(tr).remove().draw();
	    }); 
	    
	    //构造函数，获得json
	    itemOrder.buildSalesmanDetail=function(){
	    	var salesman_table_rows = $("#salesman_table tr");
	        var cargo_items_array=[];
	        for(var index=0; index<salesman_table_rows.length; index++){
	            if(index==0)
	                continue;

	            var row = salesman_table_rows[index];
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
	    var bindFieldEvent=function(){	
	    	eeda.bindTableField('salesman_table','SALESMAN_ID','/role/searchOneStation','业务员');
	    };
	    var salesmanTable = eeda.dt({
            id: 'salesman_table',
            autoWidth: false,
            drawCallback: function( settings ) {
            	bindFieldEvent();
		    },
            columns:[
	            {"width": "10%",
	                "render": function ( data, type, full, meta ) {
	                		return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:100%" >删除</button> ';
	                }
	            },
	            { "data": "SALESMAN_ID", "width": "20%",
                    "render": function ( data, type, full, meta ) {
                    	if(!data)
                            data='';
                        var field_html = template('table_dropdown_template',
                            {
                                id: 'SALESMAN_ID',
                                value: data,
                                display_value: full.SALESMAN_NAME,
                                style:'width:100%'
                            }
                        );
                        return field_html;
                    }
                },
	            { "data": "ROYALTY_RATE","width": "20%",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="ROYALTY_RATE" value="'+data+'" class="form-control search-control" style="width:100%"/>';
	                }
	            },
	            { "data": "REMARK","width": "45%",
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                	   data='';
	                   return '<input type="text" name="REMARK" value="'+data+'" class="form-control search-control" style="width:100%"/>';
	                }
	            },
                { "data": "SALESMAN_NAME", "visible": false,
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                }
	        ]
	    });
	    


	    $('#add_salesman').on('click', function(){
	        var item={};
	        salesmanTable.row.add(item).draw(true);
	    });
	    
	    //刷新明细表
	    itemOrder.refleshSalesmanTable = function(order_id){
	    	var url = "/customer/tableList?order_id="+order_id+"&type=salesman";
	    	salesmanTable.ajax.url(url).load();
	    }
	    
	    //校验
        $('#salesman_table').on('blur','[name=ROYALTY_RATE],[name=REMARK]',function(){
        	var data = $(this).val();
        	var name = $(this).attr("name");
        	var len = $.trim(data).length;
        	if(name=="ROYALTY_RATE"){
        		var re = /^\d{0,5}(\.\d{1,5})?$/;
        		if(!re.test(data)){
        			$(this).parent().append("<span style='color:red;display:block;' class='error_span'>请输入合法的数字</span>");
        			return;
        		}
        	}
        	if(name=="REMARK"){
        		var re = /^.{500,}$/;
        		if(re.test(data)){
        			$(this).parent().append("<span style='color:red;display:block;' class='error_span'>只能输入长度500内的字符串</span>");
        			return;
        		}
        	}
        });
        $('#salesman_table').on('focus','[name=ROYALTY_RATE],[name=REMARK]',function(){
        	$(this).parent().find("span").remove();
        });
	});
});
