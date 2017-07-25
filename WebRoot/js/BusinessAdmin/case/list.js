
define(['jquery', 'dataTablesBootstrap', 'validate_cn', 'sco','file_upload'], function ($, metisMenu) {
$(document).ready(function() {
	var img_num = 0.0;
	$("#picture_up").on("click",function(){
		$(this).fileupload({
			validation: {allowedExtensions: ['*']},
			autoUpload:true,
			url:"/BusinessAdmin/case/saveFile",
			dataType:"json",
			done:function (e, data) {
        		if(data){
		    		$('#example_img').val(data.result.NAME);
		    	
		    		var imgPre =Id("example_img");
		  		    imgPre.src = '/upload/'+data.result.NAME;
		    	}else{
		    		$.scojs_message('上传失败', $.scojs_message.TYPE_ERROR);
		    	}
		     },error:function () {
	            alert('上传的时候出现了错误！');
	        }
		});
	});
	  
	  
	$("#file_photo").on('click',function(){
		img_num = $('[name=img_photo]').size();
		img_num = img_num+1;
		var img = "<img style='width: 200px; height: 150px;margin-left:20px;display:none' name='img_photo'  class='shadow' id='img_photo"+img_num+"'>";
		$('#img_item').append(img);
		  
		$(this).fileupload({
			validation: {allowedExtensions: ['*']},
			autoUpload: true, 
		    url: '/BusinessAdmin/case/saveFile',
		    dataType: 'json',
	        done: function (e, data) {
        		if(data){
        			$('#img_photo'+img_num).show();
		    		$.scojs_message('已选择', $.scojs_message.TYPE_OK);
		    		$('#img_photo'+img_num).attr('value',data.result.NAME);
		    		var imgPre =Id('img_photo'+img_num);
		  		    imgPre.src = '/upload/'+data.result.NAME;
		    	}else{
		    		$.scojs_message('上传失败', $.scojs_message.TYPE_ERROR);
		    	}
		     },error: function () {
	            alert('上传的时候出现了错误！');
		     }
		});
	  });
	
	
	  function Id(id){
		  return document.getElementById(id);
	  }
	  
	  
	$("#save_btn").click(function(){
		var self=$(this);
		var example={};
		example.img_num = img_num;
		example.id=$("#id").val();
		example.name=$("#name").val();
		example.picture_name=$("#example_img").val();
	  	for(var i = 1;i <= img_num;i++){
    		example['photo'+i] = $('#img_photo'+i).attr('value');
    	}
		$.post("/BusinessAdmin/case/save",{jsonStr:JSON.stringify(example)},function(data){
  			if(data){
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				self.attr('disabled',false);
				refleshTable();
			}else{
				$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
			}  
		});
	});
	  

	var dataTable = eeda.dt({
		id: 'eeda_table',
        paging: true,
        aLengthMenu:[5,10],
        serverSide: true, 
        ajax: "/BusinessAdmin/case/list",
        columns: [
            { "data": "NAME" ,"width": "50px"},
            { "data": "CREATE_TIME","class":"title", "width": "100px"},
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
		dataTable.ajax.url("/BusinessAdmin/case/list").load();
	}
		 
		 
	$("#eeda_table").on("click",".delBtn",function(){
		 var self=$(this);
		 var id=self.data("id");
		 $.post("/BusinessAdmin/case/delete",{"id":id},function(data){
			 if(data){
				  $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
				  refleshTable();
			 }else{
				 $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
			 }
		 });
	});
});
});