define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'file_upload'], function ($, metisMenu, template) { 
$(document).ready(function() {
	
	//未保存不能上传文件
	if($('#order_id').val()==''){
		$("#fileuploadSpan").hide();
		$("#sendEmail").hide();
	}
	
	//上传文档
	$('#fileupload').click(function(){
		var order_id = $('#order_id').val();
		$('#fileupload').fileupload({
				autoUpload: true, 
			    url: '/customPlanOrder/uploadDocFile?order_id='+order_id,
			    dataType: 'json',
		        done: function (e, data) {
	        	if(data.result){
			    		$.scojs_message('上传成功', $.scojs_message.TYPE_OK);
			    		salesOrder.refleshDocTable(order_id);
			    	}else{
			    		$.scojs_message('上传失败', $.scojs_message.TYPE_ERROR);
			    	}
			     },
		        error: function () {
		            alert('上传的时候出现了错误！');
		        }
		});
	})

    //删除一行
    $("#doc_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        var id = tr.attr('id');
        
         $.post('/customPlanOrder/deleteDoc', {docId:id}, function(data){
        	 if(data.result==true){
        		 docTable.row(tr).remove().draw();
	        	 $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
        	 }else if(data.result==false){
        		 $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
        	 }else{
        		 docTable.row(tr).remove().draw();
        		 $.scojs_message(data.result, $.scojs_message.TYPE_ERROR);
        	 }
         },'json').fail(function() {
             	 $.scojs_message('删除失败!', $.scojs_message.TYPE_ERROR);
         });
    }); 

    
    salesOrder.buildDocItem=function(){
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
            
            if(id == '' || id == null)
            	continue;
            
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

        return cargo_items_array;
    };
    
    //------------事件处理,文档table
    var docTable = eeda.dt({
        id: 'doc_table',
        autoWidth: false,
        columns:[
//			{ "data":"ID","width": "10px",
//			    "render": function ( data, type, full, meta ) {
//			    	if(data)
//			    		return '<input type="checkbox" class="checkBox" style="width:30px">';
//			    	else 
//			    		return '<input type="checkbox" class="checkBox" style="width:30px" disabled>';
//			    }
//			},
//          '<input id="allCheckOfDoc" type="checkbox" style="width:30px">',
            { "width": "30px",
                "render": function ( data, type, full, meta ) {
                	if(full.ID || full.REF_JOB_ORDER_ID==''){
                		
                		return '<button type="button" class="delete btn btn-default btn-xs" style="width:50px">删除</button> ';
                	}else {
                		return '';
                	}
                }
            },
            { "data": "DOC_NAME","width": "280px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="hidden" name="doc_name" value="'+data+'" ><a class="doc_name" href="/upload/doc/'+data+'" style="width:300px" target="_blank">'+data+'</a>';
                }
            },
            { "data": "C_NAME","width": "180px",
                "render": function ( data, type, full, meta ) {
                	if(!data)
                        data='';
                	return '<input type="hidden" name="uploader" value="'+full.UPLOADER+'">'+data;
                }
            },
            { "data": "UPLOAD_TIME", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="hidden" name="upload_time" value="'+data+'">'+data;
                }
            },
            { "data": "REMARK","width": "280px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    if(full.ID)
                    	return '<input type="text" name="remark" value="'+data+'" class="form-control" style="width:300px"/>';
                    else 
                    	return data;
                }
            },
            { "data": "UPLOADER", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="uploader" value="'+data+'">'
                }
            },
            { "data": "REF_JOB_ORDER_ID", "visible": false,
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return data;
            	}
            }
        ]
    });
    
    //刷新明细表
    salesOrder.refleshDocTable = function(order_id){
    	var url = "/customPlanOrder/tableList?order_id="+order_id+"&type=doc";
    	docTable.ajax.url(url).load();
    }
    
});
});