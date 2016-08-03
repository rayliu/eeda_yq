define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) { 
$(document).ready(function() {
	
	//未保存不能上传文件
	if($('#order_id').val()==''){
		$("#fileupload").attr('disabled', true);
	}

    //删除一行
    $("#doc_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        var id = tr.attr('id');
        var order_id = $('#order_id').val();
        
         $.post('/jobOrder/deleteDoc', {docId:id}, function(data){
        	 if(data.result==true){
        		 docTable.row(tr).remove().draw();
	        	 $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
	        	 //异步刷新显示上传的文档信息
		    	 itemOrder.refleshDocTable(order_id);
        	 }else if(data.result==false){
        		 $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
        	 }else{
        		 $.scojs_message(data.result, $.scojs_message.TYPE_ERROR);
        	 }
         },'json').fail(function() {
             	 $.scojs_message('删除失败!', $.scojs_message.TYPE_ERROR);
           });
    }); 

    itemOrder.buildDocDetail=function(){
        var cargo_table_rows = $("#doc_table tr");
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
                	var value = el.val();//元素的值
                	item[name] = value;
            	}
            }
            item.action = id.length > 0?'UPDATE':'CREATE';
            cargo_items_array.push(item);
        }

        return cargo_items_array;
    };
    
    
    //------------事件处理,文档table
    var docTable = eeda.dt({
        id: 'doc_table',
        columns:[
			{ "data":"ID","width": "10px",
			    "render": function ( data, type, full, meta ) {
			    	if(data)
			    		return '<input type="checkbox" class="checkBox">';
			    	else 
			    		return '<input type="checkbox" class="checkBox" disabled>';
			    }
			},
            { "width": "30px",
                "render": function ( data, type, full, meta ) {
                	return '<button type="button" class="delete btn btn-default btn-xs">删除</button> ';
                }
            },
            { "data": "DOC_NAME",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" value="'+data+'" class="form-control" disabled/>';
                }
            },
            { "data": "C_NAME",
                "render": function ( data, type, full, meta ) {
                	if(!data)
                        data='';
                	return '<input type="text" value="'+data+'" class="form-control" disabled/>';
                }
            },
            { "data": "UPLOAD_TIME", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" value="'+data+'" class="form-control" disabled/>';
                }
            },
            { "data": "REMARK",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="remark" value="'+data+'" class="form-control "/>';
                }
            }
        ]
    });
    
    //------------事件处理,email_table
    var emailTable = eeda.dt({
        id: 'email_table',
        columns:[
            { "data": "ORDER_NO", 
                "render": function ( data, type, full, meta ) {
                    return "<a href='#'  target='_blank'>"+data+"</a>";
                }
            },
            { "data": "CUSTOMER_NAME"}, 
            { "data": "CREATOR_NAME"}, 
            { "data": "CREATE_STAMP"}, 
            { "data": "STATUS"}
        ]
    });

    //刷新明细表
    itemOrder.refleshDocTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=doc";
    	docTable.ajax.url(url).load();
    }
    
});
});