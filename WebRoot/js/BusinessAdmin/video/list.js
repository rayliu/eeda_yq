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
			    		$('#title_img').val(data.result.NAME);
			    	
			    		var imgPre =Id("title_img");
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
  	  
  	  $("#upload").on("click",function(){
  		var self=this;
  		var video={};
  			video.name=$("#name").val();
  			video.title_img=$("#title_img").val();
  			video.youku_address=$("#youku_address").val();
  		  $.post("/BusinessAdmin/video/save",{jsonStr:JSON.stringify(video)},function(data){
  			 if(data){
				 $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				 $(self).attr('disabled',false);
				 refleshTable();
			 }else{
				 $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
			 }  
  		  })
  	  })
		var dataTable = eeda.dt({
	          id: 'eeda_table',
	          paging: true,
	          aLengthMenu:[5,10],
	          serverSide: true, 
	          ajax: "/BusinessAdmin/video/list",
	          columns: [
	            { "data": "NAME" ,"width": "50px"},
	            { "data": "YOUKU_URL","class":"title", "width": "100px"},
	            { "data": "CREATE_TIME","width": "100px"},
	            { "data": "BEGIN_DATE", "width": "100px",
	            	render: function(data,type,full,meta){
	            		
	            			data =  "<a class='stdbtn btn_warning delBtn' " +
	              				" data-id="+full.ID+" href='#eeda_table'>删除</a>";
	            	
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
				// $('#order_id').val(data.ID);
				 $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
				 refleshTable();
			 }else{
				 $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
			 }  
  		  })
		 })
    	
    });
});
