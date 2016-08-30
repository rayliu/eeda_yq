define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) { 
$(document).ready(function() {
	
	//未保存不能上传文件
	if($('#order_id').val()==''){
		$("#fileuploadSpan").hide();
		$("#sendEmail").hide();
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
        	 }else if(data.result==false){
        		 $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
        	 }else{
        		 $.scojs_message(data.result, $.scojs_message.TYPE_ERROR);
        	 }
         },'json').fail(function() {
             	 $.scojs_message('删除失败!', $.scojs_message.TYPE_ERROR);
           });
    }); 

    //------------事件处理,文档table
    var docTable = eeda.dt({
        id: 'doc_table',
        autoWidth: false,
        columns:[
			{ "data":"ID","width": "10px",
			    "render": function ( data, type, full, meta ) {
			    	if(data)
			    		return '<input type="checkbox" class="checkBox" style="width:30px">';
			    	else 
			    		return '<input type="checkbox" class="checkBox" style="width:30px" disabled>';
			    }
			},
            { "width": "30px",
                "render": function ( data, type, full, meta ) {
                	return '<button type="button" class="delete btn btn-default btn-xs" style="width:50px">删除</button> ';
                }
            },
            { "data": "DOC_NAME","width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" value="'+data+'" class="doc_name form-control" style="width:200px" disabled/>';
                }
            },
            { "data": "C_NAME","width": "180px",
                "render": function ( data, type, full, meta ) {
                	if(!data)
                        data='';
                	return '<input type="text" value="'+data+'" class="form-control" style="width:200px" disabled/>';
                }
            },
            { "data": "UPLOAD_TIME", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" value="'+data+'" class="form-control" style="width:200px" disabled/>';
                }
            },
            { "data": "REMARK","width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="remark" value="'+data+'" class="form-control" style="width:200px"/>';
                }
            }
        ]
    });
    
    
    $('#sendEmail').click(function(){
    	 var i = 0;
         $('#doc_table input[type="checkbox"]:checked').each(function(){
         	i++;
         });
    	 $('#attachment').html('您选择了 '+i+'个附件');
    })
    
    $('#confirmSendBtn').click(function(){
    	//提交前，校验数据
        if(!$("#emailForm").valid()){
            return;
        }
        $('#returnBtn').click();
    	var order_id = $('#order_id').val();
        var docs = [];
        $('#doc_table input[type="checkbox"]:checked').each(function(){
        	var doc_name = $($(this).parent().parent().find('.doc_name')).val();
        	docs.push(doc_name);
        });
        var email =  $('#email').val();
    	var title = $('#emailTitle').val();
    	var content = $('#emailContent').val();
    	$.post('/jobOrder/sendMail', {order_id:order_id,mailTitle:title,userEmail:email,mailContent:content,docs:docs.toString()}, function(data){
    		if(data.result==true){
	        	 $.scojs_message('发送邮件成功', $.scojs_message.TYPE_OK);
	        	 itemOrder.refleshEmailTable(order_id);
	       	 }else if(data.result==false){
	       		 $.scojs_message('发送邮件失败', $.scojs_message.TYPE_ERROR);
	       	 }
    	},'json').fail(function() {
        	 $.scojs_message('发送邮件时出现未知错误!', $.scojs_message.TYPE_ERROR);
        });
    })
    //------------事件处理,email_table
    var emailTable = eeda.dt({
        id: 'email_table',
        autoWidth: false,
        columns:[
            { "data": "MAIL_TITLE","width": "100px"},
            { "data": "DOC_NAME","width": "300px"}, 
            { "data": "RECEIVE_MAIL","width": "100px"}, 
            { "data": "SENDER","width": "100px"}, 
            { "data": "SEND_TIME","width": "100px"}
        ]
    });
    //刷新明细表
    itemOrder.refleshDocTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=doc";
    	docTable.ajax.url(url).load();
    }
    
    itemOrder.refleshEmailTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=mail";
    	emailTable.ajax.url(url).load();
    }
    
});
});