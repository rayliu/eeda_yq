define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','file_upload'], function ($, metisMenu, template) { 
$(document).ready(function() {
	//未保存不能上传文件
	if($('#order_id').val()==''){
		$("#fileuploadSpan").hide();
	}
    
    //------------事件处理,文档table
    var dataTable = eeda.dt({
    	id: 'doc_table',
        autoWidth: false,
        columns:[
            { "width": "30px",
			    "render": function ( data, type, full, meta ) {
			    	return '<button type="button" class="delete btn btn-default btn-xs" style="width:50px">删除</button> ';
			    }
			},
            { "data": "DOC_NAME","width": "100px",
		        "render": function ( data, type, full, meta ) {
		            if(!data)
		                data='';
		            return '<a class="doc_name" href="#" style="width:300px">'+data+'</a>';
		        }
		    },
		    { "data": "C_NAME","width": "100px"},
		    { "data": "UPLOAD_STAMP","width": "100px", 
		        "render": function ( data, type, full, meta ) {
		            if(!data)
		                data='';
		            return data.substring(0, 19);
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
    
  //构造函数，获得json
    itemOrder.buildDocDetail=function(){
    	var cargo_table_rows = $("#dock_table tr");
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
   
  //上传文件
	$('#fileupload').click(function(){
		var order_id = $('#order_id').val();
	
		$('#fileupload').fileupload({
				autoUpload: true, 
			    url: '/supplierRating/saveDocFile?order_id='+order_id,
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
	//查看文档
    $("#doc_table").on('click', '.doc_name',function(){
    	var url = "/upload/serviceProvider_doc/"+$(this).text();
    	window.open(url);
    })
     //删除一行
    $("#doc_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        var id = tr.attr('id');
        
         $.post('/supplierRating/deleteDoc', {docId:id}, function(data){
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
    //刷新明细表
    var refleshDocTable = function(order_id){
		var url = "/supplierRating/tableList?order_id="+order_id+"&type=docItem";
		dataTable.ajax.url(url).load();
    }
});
});