define(['jquery', 'sco', 'file_upload',"validate_cn"], function ($, metisMenu) {
	var img_num = $('[name=img_photo]').size();
	$(document).ready(function() {
		//校验表单字段
		$('#orderForm').validate({
			rules: {
				ProductName : {
				    required: true
				}
			},
		    messages: {
		    	ProductName: {
			        required: "产品名不能为空!!"
			    }
		    }
		});
		
	  $('[name=file_photo]').on('click',function(){
		  var fileid = this.id;
		  var str = fileid.substring(5,20);
		  //changeToop('file_'+str,'img_'+str);	
		  if(str != 'photo'){
			  $('#file_'+str).fileupload({
					validation: {allowedExtensions: ['*']},
					autoUpload: true, 
				    url: '/WebAdmin/tao_manage/product/saveFile',
				    dataType: 'json',
				    add: function(e, data) {
				        var uploadErrors = [];
				        var acceptFileTypes = /^image\/(gif|jpe?g|png)$/i;

						//文件类型判断
				        if(data.originalFiles[0]['type'] && !acceptFileTypes.test(data.originalFiles[0]['type'])) {
				        	//uploadErrors.push('图片类型不对');
				        	$.scojs_message('图片类型不对', $.scojs_message.TYPE_ERROR);
				        	return;
				        }

						//文件大小判断
				        if(data.originalFiles[0]['size'] > 512000) {
				            $.scojs_message('文件不能大于500K', $.scojs_message.TYPE_ERROR);
				            return;
				        }else{
				        	data.submit();
				        }
					},
			        done: function (e, data) {
		        		if(data){
				    		$.scojs_message('已选择', $.scojs_message.TYPE_OK);
				    		$('#img_'+str).attr('value',data.result.NAME);
				    		var imgPre =Id('img_'+str);
				  		    imgPre.src = '/upload/'+data.result.NAME; 

				    	}else{
				    		$.scojs_message('上传失败', $.scojs_message.TYPE_ERROR);
				    	}
				     },error: function () {
			            alert('上传的时候出现了错误！');
			        }
			   });
		  }else{
			  img_num = img_num+1;
			  var img = "<i><img style='width: 200px; height: 150px;margin-left:20px;display:none' name='img_photo'  class='shadow' id='img_photo"+img_num+"'>" +
	  			"<span style='color:red;cursor:pointer' class='delete_item' code='"+img_num+"' ><strong> 删除 </strong></span></i>";
			  
			  
			  $('#file_'+str).fileupload({
					validation: {allowedExtensions: ['*']},
					autoUpload: true, 
				    url: '/WebAdmin/tao_manage/product/saveFile',
				    dataType: 'json',
				    add: function(e, data) {
				        var uploadErrors = [];
				        var acceptFileTypes = /^image\/(gif|jpe?g|png)$/i;

						//文件类型判断
				        if(data.originalFiles[0]['type'] && !acceptFileTypes.test(data.originalFiles[0]['type'])) {
				        	//uploadErrors.push('图片类型不对');
				        	$.scojs_message('图片类型不对', $.scojs_message.TYPE_ERROR);
				        	return;
				        }

						//文件大小判断
				        if(data.originalFiles[0]['size'] > 512000) {
				            $.scojs_message('文件不能大于500K', $.scojs_message.TYPE_ERROR);
				            return;
				        }else{
				        	data.submit();
				        }
					},
			        done: function (e, data) {
		        		if(data){
		        			$('#img_item').append(img);
		        			$('#img_'+str+img_num).show();
				    		$.scojs_message('已选择', $.scojs_message.TYPE_OK);
				    		$('#img_'+str+img_num).attr('value',data.result.NAME);
				    		var imgPre =Id('img_'+str+img_num);
				  		    imgPre.src = '/upload/'+data.result.NAME;
				    	}else{
				    		$.scojs_message('上传失败', $.scojs_message.TYPE_ERROR);
				    	}
				     },error: function () {
			            alert('上传的时候出现了错误！');
			        }
			   });
		  }
	  });
	  
	  
	  //定义id选择器
	  function Id(id){
		  return document.getElementById(id);
	  }
	  //入口函数，两个参数分别为<input type='file'/>的id，还有一个就是图片的id，然后会自动根据文件id得到图片，然后把图片放到指定id的图片标签中
	  changeToop = function(fileid,imgid){
	      var file = Id(fileid);
	      if(file.value==''){
	          //设置默认图片
	      Id(imgid).src='http://sandbox.runjs.cn/uploads/rs/72/huvtowwn/zanwu.png';
	      }else{
	          preImg(fileid,imgid);
	      }
	  }
	
	  //读取图片后预览
	  function preImg(fileId,imgId) { 
		  var imgPre =Id(imgId);
		  imgPre.src = getFileUrl(fileId); 
	  }
	  
	  //获取input[file]图片的url Important
	  function getFileUrl(fileId) { 
	      var url; 
	      var file = Id(fileId);
	      var agent = navigator.userAgent;
	      if (agent.indexOf("MSIE")>=1) {
	      url = file.value; 
	      } else if(agent.indexOf("Firefox")>0) { 
	      url = window.URL.createObjectURL(file.files.item(0)); 
	      } else if(agent.indexOf("Chrome")>0) {
	      url = window.URL.createObjectURL(file.files.item(0)); 
	      } 
	      return url; 
	  } 
	  
	  $("#img_item").on('click','.delete_item',function(){
		  var code = $(this).attr('code');
		  $('#img_photo'+code).parent().remove();
		  img_num--;
	  });
	  
	  $("#img_item").on('click','.delete_item_id',function(){
		  var item_id = $(this).attr('item_id');
		  var photo = $(this).attr('photo');
		  var product_id = $('#product_id').val();
		  
		  var result = confirm("你真的要删除这张图片吗?");
  		  if(!result){
  		 	  return;
  		  }
		  $.post('/WebAdmin/tao_manage/product/deleteItem',{item_id:item_id,photo:photo,product_id:product_id},function(data){
			  if(data){
				  location.reload();
			  }else{
				  $.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
			  }
		  }).fail(function(){
			  alert('后台报错')
		  });
	  });
	  
	  
	  	
	  	$("#back_btn").click(function(event){
	  		window.location.href="/WebAdmin/tao_manage/product";
	  	})

	    $('#save_btn').click(function(event) {
			if($("input[name=price_type]:checked").val()=="面议"){
				$("input[name=price]").prop("required",false);
			}
	    	if(!$('#orderForm').valid()){
				 return;
			 }
	    	var self = this;
	    	$(self).attr('disabled',true);
	    	
	    	var order = {};
	    	order.img_num = img_num;
	    	order.id = $('#product_id').val();
	    	order.name = $('#product_name').val();
	    	order.category = $('#category').val();
	    	order.price_type = $('[name=price_type]:checked').val();
	    	if(order.price_type == '人民币'){
	    		order.price = $('#price').val();
	    	}else{
	    		order.price = "0";
	    	}
	    	order.unit = $('[name=unit]:checked').val();
	    	order.content = $('#content').val();
	    	order.cover = $('#img_cover').attr('value');
	    	for(var i = 1;i <= img_num;i++){
	    		var photo_name = $('#img_photo'+i).attr('value');
	    		if(photo_name == undefined){
	    			for(var j = i+1 ; j < 100 ; j++){
	    				if($('#img_photo'+j).attr('value') != undefined){
	    					photo_name = $('#img_photo'+j).attr('value');
	    					break;
	    				}
	    			}
	    		}
	    		order['photo'+i] = photo_name;
	    	}

	    	$.post('/WebAdmin/tao_manage/product/update',{jsonStr:JSON.stringify(order)}, function(data, textStatus, xhr) {
	    		if(data){
	    			eeda.refreshUrl('edit?id='+data.ID);
	    			$('#order_id').val(data.ID);
	    			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	    		}else{
	    			$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
	    		}
	    		$(self).attr('disabled',false);
	    	});
	    });
	});
});