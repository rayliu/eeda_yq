
define(['jquery', 'dataTablesBootstrap', 'validate_cn', 'sco','file_upload'], function ($, metisMenu) {
$(document).ready(function() {
	var img_num = 0.0;
	$("#picture_up").on("click",function(){
		$(this).fileupload({
			validation: {allowedExtensions: ['*']},
			autoUpload:true,
			url:"/WebAdmin/tao_manage/case/saveFile",
			dataType:"json",
			done:function (e, data) {
        		if(data){
		    		$('#example_img').attr('value',data.result.NAME);
		    	
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
		var img = "<i><img style='width: 200px; height: 150px;margin-left:20px;display:none' name='img_photo'  class='shadow' id='img_photo"+img_num+"'>" +
		"<span style='color:red;cursor:pointer' class='delete_item' code='"+img_num+"' ><strong> 删除 </strong></span></i>";
		
		$(this).fileupload({
			validation: {allowedExtensions: ['*']},
			autoUpload: true, 
		    url: '/WebAdmin/tao_manage/case/saveFile',
		    dataType: 'json',
	        done: function (e, data) {
        		if(data){
        			$('#img_item').append(img);
        			$('#img_photo'+img_num).show();
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
	  };
	  
	  
	$("#save_btn").click(function(){
		var self = $(this);
		var example = {};
		example.img_num = img_num;
		example.id=$("#example_id").val();
		example.name=$("#name").val();
		example.picture_name=$("#example_img").attr('value');
	  	for(var i = 1;i <= img_num;i++){
    		example['photo'+i] = $('#img_photo'+i).attr('value');
    	}
		if($.trim(example.name)==""){
			alert("案例名称必须要填！！")
			return;
		}
		$.post("/WebAdmin/tao_manage/case/update",{jsonStr:JSON.stringify(example)},function(data){
  			if(data){
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				self.attr('disabled',false);
				refleshTable();
			}else{
				$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
			}  
		});
	});
	

			
	var refleshTable = function(){
		dataTable.ajax.url("/BusinessAdmin/case/list").load();
	};
	
	$("#back_btn").click(function(event){
  		window.history.back();
  	});
	
	
	$("#img_item").on('click','.delete_item',function(){
		  var code = $(this).attr('code');
		  $('#img_photo'+code).parent().remove();
		  img_num--;
	  });
	  
	  $("#img_item").on('click','.delete_item_id',function(){
		  var item_id = $(this).attr('item_id');
		  var photo = $(this).attr('photo');
		  var product_id = $('#example_id').val();
		  
		  var result = confirm("你真的要删除这张图片吗?");
  		  if(!result){
  			  return;
  		  }
		  $.post('/WebAdmin/tao_manage/case/deleteItem',{item_id:item_id,photo:photo,product_id:product_id},function(data){
			  if(data){
				  location.reload();
			  }else{
				  $.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
			  }
		  }).fail(function(){
			  alert('后台报错')
		  });
	  });
		 
		 
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