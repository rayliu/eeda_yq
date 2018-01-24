define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) { 
	$(document).ready(function() {

	    var deletedTableIds=[];

	    //删除一行
	    $("#account_table").on('click', '.delete', function(e){
	        e.preventDefault();
	        var tr = $(this).parent().parent();
	        deletedTableIds.push(tr.attr('id'));
	        
	        accountTable.row(tr).remove().draw();
	    }); 
	    
	    //构造函数，获得json
	    itemOrder.buildAccountDetail=function(){
	    	var account_table_rows = $("#account_table tr");
	        var cargo_items_array=[];
	        for(var index=0; index<account_table_rows.length; index++){
	            if(index==0)
	                continue;

	            var row = account_table_rows[index];
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
	    
	    var bindFieldEvent=function(){
	        eeda.bindTableFieldCurrencyId('account_table','CURRENCY_ID','/serviceProvider/searchCurrency','notRate');
	    };
	    

	    //------------事件处理
	    var accountTable = eeda.dt({
            id: 'account_table',
            autoWidth: false,
            drawCallback: function( settings ) {
            	bindFieldEvent();
		    },
            columns:[
	            {"width": "5%",
	                "render": function ( data, type, full, meta ) {
	                		return '<button type="button" class="delete btn btn-default btn-xs" style="width:100%" >删除</button> ';
	                }
	            },
	            { "data": "ACCOUNT_NAME","width": "25%",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return '<input style="width:100%"  type="text" name="ACCOUNT_NAME" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "BANK_NAME","width": "25%",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="BANK_NAME" value="'+data+'" class="form-control search-control" style="width:100%"/>';
	                }
	            },
	            { "data": "ACCOUNT_NO","width": "25%",
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                	   data='';
	                   return '<input type="text" name="ACCOUNT_NO" value="'+data+'" class="form-control search-control" style="width:100%"/>';
	                }
	            },
	            { "data": "CURRENCY_ID", "width":"5%","className":"currency_name",
	                "render": function ( data, type, full, meta ) {
	            	   if(!data)
	                       data='';
	                   var field_html = template('table_dropdown_template',
	                       {
	                           id: 'CURRENCY_ID',
	                           value: data,
	                           display_value: full.CURRENCY_NAME,
	                           style:'width:100%'
	                       }
	                   );
	                   return field_html; 
	                }
	            },
	            { "data": "REMARK","width": "20%",
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                	   data='';
	                   return '<input type="text" name="REMARK" value="'+data+'" class="form-control search-control" style="width:100%"/>';
	                }
	            },
	            { "data": "CURRENCY_NAME", "visible": false,
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return data;
	                }
	            }
	        ]
	    });
	    


	    $('#add_account').on('click', function(){
	        var item={};
	        accountTable.row.add(item).draw(true);
	    });
	    
	    //刷新明细表
	    itemOrder.refleshTable = function(order_id){
	    	var url = "/serviceProvider/tableList?order_id="+order_id;
	    	accountTable.ajax.url(url).load();
	    }
	   
	    //校验
        $('#account_table').on('blur','[name=ACCOUNT_NAME],[name=BANK_NAME],[name=ACCOUNT_NO],[name=REMARK]',function(){
        	var data = $(this).val();
        	var name = $(this).attr("name");
        	var len = $.trim(data).length;
        	if(name=="ACCOUNT_NAME"||name=="BANK_NAME"||name=="REMARK"){
        		var re = /^.{255,}$/;
        		if(re.test(data)&&len>0){
        			$(this).parent().append("<span style='color:red;display:block;' class='error_span'>只能输入长度255内的字符串</span>");
        			return;
        		}
        	}
        	if(name=="ACCOUNT_NO"){
        		var re = /^.{0,30}$/;
        		if(!re.test(data)&&len>0){
        			$(this).parent().append("<span style='color:red;display:block;' class='error_span'>输入银行账户的字符长度最多为30</span>");
        			return;
        		}
        	}
        });
        $('#account_table').on('focus','[name=ACCOUNT_NAME],[name=BANK_NAME],[name=ACCOUNT_NO],[name=REMARK]',function(){
        	$(this).parent().find("span").remove();
        });
	});
});
