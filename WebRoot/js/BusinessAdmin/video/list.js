define(['jquery', 'validate_cn', 'sco', 'file_upload','dataTablesBootstrap'], function ($, metisMenu) {
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
	  })
	  //定义id选择器
	  function Id(id){
		  return document.getElementById(id);
	  }
  	  
  	 
		var dataTable = eeda.dt({
	          id: 'eeda_table',
	          paging: true,
	          aLengthMenu:[5,10],
	          serverSide: true, 
	          ajax: "/BusinessAdmin/video/list",
	          columns: [
				{ "data": "COVER", "width": "150px",
					render: function(data,type,full,meta){
						return "<img class='shadow' src='/upload/"+data+"' style='width:100px;height:75px' />";
					} 
				},
	            { "data": "NAME" ,"width": "300px"},
	            { "data": "VIDEO_URL","class":"title", "width": "200px"},
	            { "data": "CREATE_TIME","width": "150px"},
	            { "data": "ID", 
	            	render: function(data,type,full,meta){
	            		
	            			data =  "<button class='stdbtn btn_primary editBtn' " +
	              				" data-id='"+full.ID+"' data-cover='"+full.COVER+"' data-url='"+full.VIDEO_URL+"'" +
	              				" data-name='"+full.NAME+"' href='#eeda_table'>编辑</button> " +
	              				"<button class='stdbtn btn_warning delBtn' " +
	              				" data-id='"+full.ID+"' href='#eeda_table'>删除</button>";
	            	
	            		return data;
	            	} 
	            }
	          ]
			});
			
		 var refleshTable = function(){
	    	 dataTable.ajax.url("/BusinessAdmin/video/list").load();
	     }
		 
		 
		 $("#eeda_table").on("click",".delBtn",function(){
			 var self=$(this);
			 var id=self.data("id");
			 $.post("/BusinessAdmin/video/delete",{"id":id},function(data){
  			 if(data){
				 $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
				 refleshTable();
			 }else{
				 $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
			 }  
  		  })
		 })
		 
		 
		$("#upload").on("click",function(){
			var self = this;
	  		var video = {};
	  		video.order_id = $("#order_id").val();
  			video.name = $("#name").val();
  			video.cover = $("#cover").val();
  			video.video_url = $("#video_url").val();
  			if($.trim(video.name) == ""||$.trim(video.video_url) == ""){
  				alert("案例名称和优酷地址都必须要填！！！")
  				return;
  			}
	  		  $.post("/BusinessAdmin/video/save",{jsonStr:JSON.stringify(video)},function(data){
	  			 if(data){
					 $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
					 $(self).attr('disabled',false);
					 refleshTable();
					 
					 //清空
					 $('#order_id').val("");
					 $('#name').val("");
					 $('#cover').prop('src',"/images/fengmianpic.png")
					 $("#cover").val("");
					 $('#video_url').val("");
				 }else{
					 $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
				 }  
	  		  });
  	     });
		 
		 
		 $("#eeda_table").on("click",".editBtn",function(){
			 var self = $(this);
			 var order_id = self.data("id");
			 var cover = self.data("cover");
			 var name = self.data("name");
			 var video_url = self.data("url");
			 
			 $('#order_id').val(order_id);
			 $('#name').val(name);
			 $("#cover").val(cover);
			 $('#cover').prop('src',"/upload/"+cover)
			 $('#video_url').val(video_url);
			 
  		  });
    	
    });
});
