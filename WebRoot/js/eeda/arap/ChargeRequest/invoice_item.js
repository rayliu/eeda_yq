define(['jquery','metisMenu', 'template', 'sb_admin', 'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
    
var deletedTableIds=[];
//删除一行
$("#invoice_table").on('click', '.delete', function(){
    var tr = $(this).parent().parent();
    tr.css("display","none");
    deletedTableIds.push(tr.attr('id'))
}); 


        var itemTable = eeda.dt({
            id: 'invoice_table',
            drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
                bindFieldEvent();
            },
            columns:[
                { "width": "70px",
	                "render": function ( data, type, full, meta ) {
	                	return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:70px">删除</button>';
	                }
                },
                { "data": "INVOICE_NO","width":"200px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" name="invoice_no" value="'+data+'" class="form-control" style="width:200px"/>';
                    }
                },
                { "data": "AMOUNT","width":"150px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" name="amount" value="'+data+'" class="form-control" style="width:150px"/>';
                    }
                },
                { "data": "CURRENCY_ID" ,"width": "80px",
	            	 "render": function ( data, type, full, meta ) {
	 	                	if(!data)
	 	                        data='';
	 	                    var field_html = template('table_dropdown_template',
	 	                        {
	 	                            id: 'CURRENCY_ID',
	 	                            value: data,
	 	                            display_value: full.CURRENCY_NAME,
	 	                            style:'width:100px'
	 	                        }
	 	                    );
	 	                    return field_html;
	                 }
	            },
                { "data": "REMARK","width":"300px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" name="remark" value="'+data+'" class="form-control" style="width:300px"/>';
                    }
                },
                { "data": "CREATOR","width":"100px",
                	 "render": function ( data, type, full, meta ) {
                		 var str = '';
                         if(!data){
                        	 data = '';
                        	 str = '';
                         }else{
                        	 str = full.CREATOR_NAME;
                         }
                    	 return '<input type="text" name="creator" value="'+data+'" class="form-control" style="display:none;" />'+
                  		'<input type="text" value="'+str+'" class="form-control" style="width:120px" disabled />';
                         
                         
                     }
                },
                { "data": "CREATE_STAMP","width":"120px",
                	 "render": function ( data, type, full, meta ) {
                         if(!data)
                             data='';
                         return '<input type="text" name="create_stamp" value="'+data+'" class="form-control" style="width:147px" disabled/>';
                     }
                },
                { "data": "CURRENCY_NAME", "visible": false},
                { "data": "CREATOR_NAME", "visible": false}
            ]
        });

      var bindFieldEvent=function(){
    	  eeda.bindTableFieldCurrencyId('invoice_table','CURRENCY_ID','/serviceProvider/searchCurrency','notRate');
      };

      //添加新的明细
      $('#add_invoice_btn').click(function(){
    	  var item={};
          itemTable.row.add(item).draw(false);
      }) 
      
      itemOrder.buildInvoiceItem=function(){
          var cargo_table_rows = $("#invoice_table tr");
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
              	var name = el.attr('name');
              	if(el && name){
                  	var value = el.val();
                  	item[name] = value;
              	}
              }
              item.action = id.length > 0?'UPDATE':'CREATE';
              cargo_items_array.push(item);
          }
          
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

});