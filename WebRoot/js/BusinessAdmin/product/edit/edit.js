define(['jquery', 'sco', 'file_upload'], function ($, metisMenu) {
	$(document).ready(function() {
	  
	  $('[name=file_photo]').on('click',function(){
		  var fileid = this.id;
		  var str = fileid.substring(5,20);
		  
		  //changeToop('file_'+str,'img_'+str);
		  
		  $('#file_'+str).fileupload({
				validation: {allowedExtensions: ['*']},
				autoUpload: true, 
			    url: '/BusinessAdmin/product/saveFile',
			    dataType: 'json',
		        done: function (e, data) {
	        		if(data){
			    		$.scojs_message('上传成功', $.scojs_message.TYPE_OK);
			    		$('#img_'+str).val(data.result.NAME);
			    		var imgPre =Id('img_'+str);
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

	    $('#save_btn').click(function(event) {
	    	var self = this;
	    	$(self).attr('disabled',true);
    	  
	    	var order = {};
	    	order.id = $('#order_id').val();
	    	order.name = $('#name').val();
	    	order.category = $('#category').val();
	    	order.price_type = $('[name=price_type]:checked').val();
	    	if(order.price_type=='人民币'){
	    		order.price = $('#price').val();
	    	}else{
	    		order.price = 0.00;
	    	}
	    	order.unit = $('[name=unit]:checked').val();
	    	order.content = $('#content').val();
	    	order.cover = $('#img_cover').val();
	    	order.photo1 = $('#img_photo1').val();
	    	order.photo2 = $('#img_photo2').val();
	    	order.photo3 = $('#img_photo3').val();
	    	order.photo4 = $('#img_photo4').val();
	    	order.photo5 = $('#img_photo5').val();
	    	order.photo6 = $('#img_photo6').val();
	    	order.photo7 = $('#img_photo7').val();
	    	order.photo8 = $('#img_photo8').val();
	    	order.photo9 = $('#img_photo9').val();
	    	order.photo10 = $('#img_photo10').val();
	    	
 		  
	    	$.post('/BusinessAdmin/product/save',{jsonStr:JSON.stringify(order)}, function(data, textStatus, xhr) {
	    		if(data){
	    			eeda.refreshUrl('edit?id='+data.ID)
	    			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	    		}else{
	    			$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
	    		}
	    		$(self).attr('disabled',false);
	    	});
	    });
	});
});