define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
$(document).ready(function() {

	var deletedTableIds=[];
    //删除一行
    $("#custom_self_item_table").on('click', '.delete', function(){
        var tr = $(this).parent().parent();
        tr.css("display","none");
        deletedTableIds.push(tr.attr('id'))
    }); 
    
    itemOrder.buildCustomSelfItem=function(){
        var cargo_table_rows = $("#custom_self_item_table tr");
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

    
    var bindFieldEvent=function(){
        $('table .date').datetimepicker({  
            format: 'yyyy-MM-dd',  
            language: 'zh-CN'
        }).on('changeDate', function(el){
            $(".bootstrap-datetimepicker-widget").hide();   
            $(el).trigger('keyup');
        });
        eeda.bindTableField('custom_self_item_table','CUSTOM_BANK','/customer/searchParty','broker');
   };
    //------------事件处理
    var cargoTable = eeda.dt({
	    id: 'custom_self_item_table',
	    autoWidth: false,
	    drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
	        bindFieldEvent();
	    },
	    columns:[
            { "width": "30px",
                "render": function ( data, type, full, meta ) {
                	return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:50px"><i class="fa fa-trash-o"></i> 删除</button></button>';
                }
            },
            { "data": "CUSTOM_BANK", "width": "180px",
                "render": function ( data, type, full, meta ) {
                	if(!data)
                        data='';
                    var field_html = template('table_dropdown_template',
                        {
                            id: 'CUSTOM_BANK',
                            value: data,
                            display_value: full.CUSTOM_BANK_NAME,
                            style:'width:200px'
                        }
                    );
                    return field_html;
                }
            },
            { "data": "CUSTOM_ORDER_NO", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="custom_order_no" value="'+data+'" class="form-control" style="width:200px"/>';
                }
            },
            { "data": "STATUS", "width": "80px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
                 	   data='';
                    var str= '<select name="status" class="form-control search-control" style="width:100px">'
                 	   	 	+'<option></option>'
 		                   +'<option value="接单" '+ (data=='接单'?'selected':'') +'>接单</option>'
 		                   +'<option value="审单" '+ (data=='审单'?'selected':'') +'>审单</option>'
 		                   +'<option value="查验" '+ (data=='查验'?'selected':'') +'>查验</option>'
 		                   +'<option value="征税" '+ (data=='征税'?'selected':'') +'>征税</option>'
 		                   +'<option value="放行" '+ (data=='放行'?'selected':'') +'>放行</option>'
 		                   +'</select>';
 		           return str;
            	}
            },
            { "data": "CREATOR", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="creator" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "CREATE_STAMP", "width": "180px",
            	"render": function ( data, type, full, meta ) {
           		 		if(!data)
	                        data='';
	                    var field_html = template('table_date_field_template',
		                    {
		                        id: 'create_stamp',
		                        value: data.substr(0,19),
		                        style:'width:180px'
		                    }
		                );
	                    return field_html;
            	}
            },
            { "data": "CUSTOM_BANK_NAME", "visible": false,
            	"render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            }
        ]
    });

    $('#addCustomSelf').on('click', function(){
        var item={};
        cargoTable.row.add(item).draw(true);
    });
    
    //刷新明细表
    itemOrder.refleshCustomChinaSelfItemTable = function(order_id){
    	var url = "/bookOrder/tableList?order_id="+order_id+"&type=china_self";
    	cargoTable.ajax.url(url).load();
    }
    

});
});