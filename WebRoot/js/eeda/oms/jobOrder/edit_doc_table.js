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

    
    itemOrder.buildDocItem=function(){
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
            { "data": "DOC_NAME","width": "280px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<a class="doc_name" href="/upload/doc/'+data+'" style="width:300px" target="_blank">'+data+'</a>';
                }
            },
            { "data": "C_NAME","width": "180px",
                "render": function ( data, type, full, meta ) {
                	if(!data)
                        data='';
                	return data;
                }
            },
            { "data": "UPLOAD_TIME", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            },
            { "data": "REMARK","width": "280px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="remark" value="'+data+'" class="form-control" style="width:300px"/>';
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
    	$('#confirmSendBtn').attr("disabled",true);
    	//提交前，校验数据
        if(!$("#emailForm").valid()){
            return;
        }
    	var order_id = $('#order_id').val();
        var docs = [];
        $('#doc_table input[type="checkbox"]:checked').each(function(){
        	var doc_name = $($(this).parent().parent().find('.doc_name')).text();
        	docs.push(doc_name);
        });
        var email =  $('#email').val().trim();
        var ccEmail =  $('#ccEmail').val().trim();
        var bccEmail =  $('#bccEmail').val().trim();
    	var title = $('#emailTitle').val().trim();
    	var content = $('#emailContent').val().trim();
    	$.post('/jobOrder/sendMail', {order_id:order_id,mailTitle:title,email:email,ccEmail:ccEmail,bccEmail:bccEmail,mailContent:content,docs:docs.toString()}, function(data){
    		$('#confirmSendBtn').attr("disabled",false);
    		if(data.result==true){
	        	 $.scojs_message('发送邮件成功', $.scojs_message.TYPE_OK);
	        	 itemOrder.refleshEmailTable(order_id);
	       	 }else if(data.result==false){
	       		 $.scojs_message('发送邮件失败', $.scojs_message.TYPE_ERROR);
	       	 }
    	},'json').fail(function() {
    		 $('#confirmSendBtn').attr("disabled",false);
        	 $.scojs_message('发送邮件时出现未知错误，请查看邮箱是否填错!', $.scojs_message.TYPE_ERROR);
        });
    })
    //------------事件处理,email_table
    var emailTable = eeda.dt({
        id: 'email_table',
        autoWidth: false,
        columns:[
            { "data": "MAIL_TITLE","width": "100px"},
            { "data": "DOC_NAME","width": "300px"}, 
            { "data": "RECEIVE_MAIL","width": "300px"}, 
            { "data": "CC_MAIL","width": "300px"}, 
            { "data": "BCC_MAIL","width": "300px"}, 
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
    
    //全选
    $('#allCheckOfDoc').click(function(){
    	var ischeck = this.checked;
    	$('.checkBox').each(function(){
    		this.checked = ischeck;
    	})
    })
    
    //常用邮箱模版
    $('#useEmailTemplate').on('click', 'li', function(){
        var li = $(this);
        $('#email').val(li.attr('email'));
        $('#ccEmail').val(li.attr('ccEmail'));
        $('#bccEmail').val(li.attr('bccEmail'));
       
    });
    $('#collapseEmailInfo').on('show.bs.collapse', function () {
      $('#collapseEmailIcon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
    });
    $('#collapseEmailInfo').on('hide.bs.collapse', function () {
      $('#collapseEmailIcon').removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
    });
    
    //添加常用邮箱模版
    $('#addEmailTemplate').click(function(){
    	var email =  $('#email').val().trim();
        var ccEmail =  $('#ccEmail').val().trim();
        var bccEmail =  $('#bccEmail').val().trim();
    	var remark = $('#emailTemplateRemark').val().trim();
    	
//    	var arr = email.split(",");
//    	var arr1 = ccEmail.split(",");
//    	var arr2 = bccEmail.split(",");
//     	//验证邮箱合法性
//    	 var reg = /\w+[@]{1}\w+[.]\w+/;
//    	 for(var i=0;i<arr.length;i++){
//    	   if(!reg.test(arr[i])){
//    		   $.scojs_message('收件人含有不合法邮箱', $.scojs_message.TYPE_ERROR);
//    		   return false;
//    	   }
//    	 }
//    	 for(var i=0;i<arr1.length;i++){
//    		 if(!reg.test(arr1[i])){
//    			 $.scojs_message('抄送人含有不合法邮箱', $.scojs_message.TYPE_ERROR);
//    			 return;
//    		 }
//    	 }
//    	 for(var i=0;i<arr2.length;i++){
//    		 if(!reg.test(arr2[i])){
//    			 $.scojs_message('密送人含有不合法邮箱', $.scojs_message.TYPE_ERROR);
//    			 return;
//    		 }
//    	 }
    	$.post('/jobOrder/saveEmailTemplate', {email:email,ccEmail:ccEmail,bccEmail:bccEmail,remark:remark}, function(data){
    		if(data.result==true){
	        	 $.scojs_message('添加成功', $.scojs_message.TYPE_OK);
	        	 itemOrder.refleshEmailTable(order_id);
	       	 }else{
	       		 $.scojs_message(data.result, $.scojs_message.TYPE_ERROR);
	       	 }
    	},'json').fail(function() {
        	 $.scojs_message('添加失败!', $.scojs_message.TYPE_ERROR);
        });
    })
    
    
});
});