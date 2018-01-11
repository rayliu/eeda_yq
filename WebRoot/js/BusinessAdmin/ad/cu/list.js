define(['jquery', 'dataTablesBootstrap', 'file_upload', 'validate_cn', 'sco'], function ($, metisMenu) {
	$(document).ready(function() {

		$("#title_up").on('click',function(){
			  $(this).fileupload({
					validation: {allowedExtensions: ['*']},
					autoUpload: true, 
				    url: '/BusinessAdmin/video/saveFile',
				    dataType: 'json',
			        done: function (e, data) {
		        		if(data){
				    		$('#cover').val(data.result.NAME);
				    	
				    		var imgPre =Id("cover");
				  		    imgPre.src = '/upload/'+data.result.NAME;
				    	}else{
				    		$.scojs_message('上传失败', $.scojs_message.TYPE_ERROR);
				    	}
				     },error: function () {
			            alert('上传的时候出现了错误！');
			        }
			   });
		  });
		  //定义id选择器
		  function Id(id){
			  return document.getElementById(id);
		  }
		
		var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: false,
          serverSide: true, 
          ajax: "/BusinessAdmin/ad/cu/list",
          columns: [
            { "data": "ORDER_NO" ,"width": "80px"},
            { "data": "TITLE", "width": "100px"},
            { "data": "COVER", "width": "100px",
                render: function(data,type,full,meta){
                	return "<img class='shadow' src='/upload/"+data+"' style='width:100px;height:75px' />";
            	}
            },
            { "data": "BEGIN_DATE","width": "100px" },
            { "data": "END_DATE", "width": "100px" },
            { "data": "PRICE", "width": "100px"},
            { "data": "STATUS","width": "100px" ,
                render: function(data,type,full,meta){
                	var status = '开启';
                	if(data=='开启'){
                		status = '关闭';
                	}
                    return data + " <button class='stdbtn btn_blue statusBtn' id='"+full.ID+"' content='"+full.CONTENT+"' href='#title'>"+status+"</button>";
                }
            },
            { "data": null, "width": "100px",
              render: function(data,type,full,meta){
                return "<button class='stdbtn btn_blue editBtn'  title='"+full.TITLE+"' cover='"+full.COVER+"' id='"+full.ID+"' content='"+full.CONTENT+"' href='#title'>编辑</button>";
              }
            }
          ]
		});
		
		 var refleshTable = function(){
	    	  dataTable.ajax.url("/BusinessAdmin/ad/cu/list").load();
	     }
		
		
		$('#eeda_table').on('click','.statusBtn',function(){
			var id = $(this).attr('id');
			var status = $(this).text();
			$.post('/BusinessAdmin/ad/cu/changeStatus',{id:id,status:status},function(data){
				if(data){
					refleshTable();
					$.scojs_message('操作成功', $.scojs_message.TYPE_OK);
				}else{
					$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
				}
			});
				
		});
		 
		 
		$('#eeda_table').on('click','.editBtn',function(){
			var $row = $(this).parent().parent();
			var content = $(this).attr('content')=='null'?"暂无":$(this).attr('content');
			var id = $(this).attr('id');
			var cover = $(this).attr('cover');
			var title = $(this).attr('title');
			
			
			$("#cover").val(cover);
			$('#cover').prop('src',"/upload/"+cover)
			$('#item_id').val(id);
			$('#title').val(title);
			$('#content').val(content);
			$("#edit").show();
			
		});
		
		  $('#saveBtn').click(function(event) {
			  if(!$('#orderForm').valid()){
				  return ;
			  }
			  
		    	var self = this;
		    	$(self).attr('disabled',true);
	    	  
		    	var id = $('#item_id').val();
		    	var title = $('#title').val();
		    	var cover = $('#cover').val();
				var content = $('#content').val();
		    	
		    	$.post('/BusinessAdmin/ad/cu/update',{id:id,title:title,content:content,cover:cover}, function(data, textStatus, xhr) {
		    		if(data){
		    			refleshTable()
		    			$.scojs_message('更新成功', $.scojs_message.TYPE_OK);
		    			$('#item_id').val('');
		    			$('#title').val('');
		    			$('#content').val('');
		    			$("#edit"). hide();
		    		}else{
		    			$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
		    		}
		    		$(self).attr('disabled',false);
		    	});
		    });
		
	});
});