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
			{ "data": "ID", "width": "10px",
			    "render": function ( data, type, full, meta ) {
                    if(full.SHARE_FLAG=='Y'){//有doc id证明是自己上传的，否则是从job order 共享过来的
                        return '';
                    }else{
                        if(full.CMS_SHARE_FLAG=='Y'){
                            return '<input type="checkbox" class="checkBox" checked style="width:50px">';
                        }else {
                            return '<input type="checkbox" class="checkBox" style="width:50px">';
                        }
                    }
			    }
			},
            {"width": "30px",
                "render": function ( data, type, full, meta ) {
                	if(full.SHARE_FLAG=='Y'){
                		return '';
                	}else {
                		return '<button type="button" class="delete btn btn-default btn-xs" style="width:50px">删除</button> ';
                	}
                	
                }
            },
            { "data": "DOC_NAME","width": "280px","name":"doc_name",
                "render": function ( data, type, full, meta ) {
                    if(!data){
                        data='';
                    }
                    if(full.NEW_FLAG=='Y'){
                    	return '<input type="hidden" name="doc_name" value="'+data+'" ><span class="badge" style="background-color: red;">新</span>&nbsp &nbsp<a class="doc_name" href="/upload/doc/'+data+'" style="width:300px" target="_blank">'+data+'</a>';
                    }else{
                    	return '<input type="hidden" name="doc_name" value="'+data+'" >&nbsp &nbsp<a class="doc_name" href="/upload/doc/'+data+'" style="width:300px" target="_blank">'+data+'</a>';
                    }
                }
            },
            { "data": "C_NAME","width": "80px",
                "render": function ( data, type, full, meta ) {
                	if(!data)
                        data='';
                	return '<input type="hidden" name="uploader" value="'+full.UPLOADER+'">'+data;
                }
            },
            { "data": "UPLOAD_TIME", "width": "80px",
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
            { "data": "SHARE_FLAG", "visible": false },
            { "data": "CMS_SHARE_FLAG", "visible": false,
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return data;
            	}
            },
            { "data": "NEW_FLAG", "visible": false,
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return data;
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
    
    $('#doc_table').on('change','.checkBox',function(){
        var check = $(this).prop('checked');
        var item_id = $(this).parent().parent().attr('id');
        var msg = "";
        if(check){
            msg = '共享';
            check = 'Y';
        }else{
            msg = '取消共享';
            check = 'N';
        }
        
        $.blockUI({ 
            message: '<h4><img src="/images/loading.gif" style="height: 20px; margin-top: -3px;"/> 正在'+msg+'...</h4>' 
        });
        
        $.post('/customPlanOrder/updateShare', {order_id:$('#order_id').val(),item_id:item_id,check:check}, function(data){
            if(data){
                $.scojs_message(msg+'成功', $.scojs_message.TYPE_OK);
                $.unblockUI();
            }
        }).fail(function() {
            $.scojs_message(msg+'失败', $.scojs_message.TYPE_ERROR);
            $.unblockUI();
        });
        
    });
});
});