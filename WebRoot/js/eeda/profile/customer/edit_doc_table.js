define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','file_upload'], function ($, metisMenu, template) { 
$(document).ready(function() {
	
	//未保存不能上传文件
	if($('#partyId').val()==''){
		$("#fileuploadSpan").hide();
	}
	
    //删除一行
    $("#doc_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        var id = tr.attr('id');
        
         $.post('/customer/deleteDoc', {docId:id}, function(data){
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
			{ "width": "30px",
			    "render": function ( data, type, full, meta ) {
			    	return '<button type="button" class="delete btn btn-default btn-xs" style="width:50px">删除</button> ';
			    }
			},
            { "data": "DOC_NAME","width": "280px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<a class="doc_name" href="#" style="width:300px">'+data+'</a>';
                }
            },
            { "data": "REMARK","width": "280px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="remark" value="'+data+'" class="form-control" style="width:300px"/>';
                }
            },
            { "data": "C_NAME"},
            { "data": "UPLOAD_TIME", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data.substring(0, 19);
                }
            }
        ]
    });
    
   
   
     
    //刷新明细表
    var refleshDocTable = function(order_id){
    	var url = "/customer/tableList?order_id="+order_id+"&type=docItem";
    	docTable.ajax.url(url).load();
    }
     
  
    
    //查看文档
    $("#doc_table").on('click', '.doc_name',function(){
    	var url = "/upload/customer_doc/"+$(this).text();
    	window.open(url);
    })
    
    //上传文件
	$('#fileupload').click(function(){
		var order_id = $('#partyId').val();
	
		$('#fileupload').fileupload({
				autoUpload: true, 
			    url: '/customer/saveDocFile?order_id='+order_id,
			    dataType: 'json',
		        done: function (e, data) {
	        	if(data.result){
			    		$.scojs_message('上传成功', $.scojs_message.TYPE_OK);
			    		//异步刷新显示上传的文档信息
			    		refleshDocTable(order_id);
			    	}else{
			    		$.scojs_message('上传失败', $.scojs_message.TYPE_ERROR);
			    	}
			     },
		        error: function () {
		            alert('上传的时候出现了错误！');
		        }
		});
	})

});
});