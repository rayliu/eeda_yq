define(['jquery', 'metisMenu', 'template','file_upload', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) { 
$(document).ready(function() {
	
	//未保存不能上传文件
	if($('#order_id').val()==''){
		$("#fileuploadSpanOfCustom").hide();
	}
	
	//上传
	$('#fileuploadOfCustom').click(function(){
		var order_id = $('#order_id').val();
		$('#fileuploadOfCustom').fileupload({
				autoUpload: true, 
			    url: '/jobOrder/uploadCustomDoc?order_id='+order_id,
			    dataType: 'json',
		        done: function (e, data) {
	        	    if(data.result.result){
			    		$.scojs_message('上传成功', $.scojs_message.TYPE_OK);
			    		//异步刷新显示上传的文档信息
			    		itemOrder.refleshCustomDocTable(order_id);
			    	}else{
			    		$.scojs_message('上传失败:'+data.result.ERRMSG, $.scojs_message.TYPE_ERROR);
			    	}
			     },
		        error: function () {
		            alert('上传的时候出现了错误！');
		        }
		});
	})
	
    //删除一行
    $("#custom_doc_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        var id = tr.attr('id');
        
         $.post('/jobOrder/deleteCustomDoc', {id:id}, function(data){
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

    //------------事件处理,文档table
    var docTable = eeda.dt({
        id: 'custom_doc_table',
        autoWidth: false,
        columns:[
			{ "data":"ID","width": "80px",
			    "render": function ( data, type, full, meta ) {
                    if(!full.ID){//有doc id证明是自己上传的，否则是从job order 共享过来的
                        return '';
                    }else{
                        if(full.SHARE_FLAG=='Y')
                            return '<input type="checkbox" class="checkBox" checked >';
                        else 
                            return '<input type="checkbox" class="checkBox" >';
                    }
			    }
			},
            {"width": "30px",
                "render": function ( data, type, full, meta ) {
                    if(full.ID){
                	   return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:50px"><i class="fa fa-trash-o"></i> 删除</button></button> ';
                    }else{
                        return '';
                    }
                }
            },
            { "data": "DOC_NAME",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<a class="doc_name" href="/upload/doc/'+data+'" target="_blank">'+data+'</a>';
                }
            },
            { "data": "REMARK",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="remark" value="'+data+'" class="form-control" style="width:100%"/>';
                }
            },
            { "data": "C_NAME","width": "80px"},
            { "data": "UPLOAD_TIME", "width": "80px"},
            { "data": "SHARE_FLAG", "visible": false },
            { "data": "REF_JOB_ORDER_ID", "visible": false }
        ]
    });
    
    $('#custom_doc_table').on('change','.checkBox',function(){
    	var check = $(this).prop('checked');
    	var item_id = $(this).parent().parent().attr('id');
    	var msg = "";
    	if(check){
    		msg = '共享';
    		check = 'Y';
    	}else{
    		msg = '取消';
    		check = 'N';
    	}
    	
    	$.blockUI({ 
            message: '<h4><img src="/images/loading.gif" style="height: 20px; margin-top: -3px;"/> 正在'+msg+'...</h4>' 
        });
    	
    	$.post('/jobOrder/updateShare', {order_id:$('#order_id').val(),item_id:item_id,check:check}, function(data){
    		if(data){
    			$.scojs_message(msg+'成功', $.scojs_message.TYPE_OK);
    			$.unblockUI();
    		}
    	}).fail(function() {
            $.scojs_message(msg+'失败', $.scojs_message.TYPE_ERROR);
            $.unblockUI();
        });
    	
    });
    
  
  
    //刷新明细表
    itemOrder.refleshCustomDocTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=custom_doc";
    	docTable.ajax.url(url).load();
    };

    
    //全选
    $('#allCheckOfCustomDoc').click(function(){
    	var ischeck = this.checked;
    	$('.checkBox').each(function(){
    		this.checked = ischeck;
    	});
    	
    	var msg = "";
		if(this.checked){
			ischeck = 'Y';
			msg = '共享';
    	}else{
    		ischeck = 'N';
    		msg = '取消';
    	}
		$.blockUI({ 
            message: '<h4><img src="/images/loading.gif" style="height: 20px; margin-top: -3px;"/> 正在'+msg+'...</h4>' 
        });
    	
    	$.post('/jobOrder/updateShare', {order_id:$('#order_id').val(),check:ischeck}, function(data){
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