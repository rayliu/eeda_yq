
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
		//算图片数量
		var diamond_flag = $('#diamond_flag').val();
		var photo_size = $('[name=img_photo]').size();
		if(diamond_flag != 'Y'){
			if(photo_size > 9){
				alert('普通商家只能上传十个产品，如需更多，请开通钻石会员');
				return false;
			}
		}
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
	  }
	  
	  $("#img_item").on('click','.delete_item',function(){
		  var code = $(this).attr('code');
		  $('#img_photo'+code).parent().remove();
		  img_num--;
	  });
	  
	  $("#img_item").on('click','.delete_item_id',function(){
		  var item_id = $(this).attr('item_id');
		  var photo = $(this).attr('photo');
		  var product_id = $('#order_id').val();
		  
		  $.post('/BusinessAdmin/product/deleteItem',{item_id:item_id,photo:photo,product_id:product_id},function(data){
			  if(data){
				  location.reload();
			  }else{
				  $.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
			  }
		  }).fail(function(){
			  alert('后台报错')
		  });
	  });

	
	$("#save_btn").click(function(){
		var self = $(this);
		var example = {};
		example.img_num = img_num;
		example.id=$("#case_id").val();
		example.name=$("#name").val();
		example.picture_name=$("#example_img").attr('value');
	  	for(var i = 1;i <= img_num;i++){
    		example['photo'+i] = $('#img_photo'+i).attr('value');
    	}
		if($.trim(example.name)==""){
			alert("案例名称必须要填！！")
			return;
		}
		$.post("/BusinessAdmin/case/save",{jsonStr:JSON.stringify(example)},function(data){
  			if(data){
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				self.attr('disabled',false);
				
				setTimeout(function(){
					location.href="/BusinessAdmin/case";
				},2000)
				refleshTable();
			}else{
				$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
			}  
		});
	});
	
	$("#img_item").on('click','.delete_item',function(){
		  var code = $(this).attr('code');
		  $('#img_photo'+code).parent().remove();
		  img_num--;
	  });
	
	 $("#img_item").on('click','.delete_item_id',function(){
		  var item_id = $(this).attr('item_id');
		  var photo = $(this).attr('photo');
		  var product_id = $('#case_id').val();
		  
		  var result = confirm("你真的要删除这张图片吗?");
 		  if(!result){
 			  return;
 		  }
		  $.post('/BusinessAdmin/case/deleteItem',{item_id:item_id,photo:photo,product_id:product_id},function(data){
			  if(data){
				  location.reload();
			  }else{
				  $.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
			  }
		  }).fail(function(){
			  alert('后台报错')
		  });
	  });
	
	
	
	var dataTable = eeda.dt({
		id: 'eeda_table',
        paging: true,
        aLengthMenu:[5,10],
        serverSide: true, 
        ajax: "/BusinessAdmin/case/list",
        columns: [
			{ "data": "PICTURE_NAME", "width": "150px",
				render: function(data,type,full,meta){
					return "<img class='shadow' src='/upload/"+data+"' style='width:100px;height:75px' />";
				} 
			},
            { "data": "NAME" ,"width": "300px"},
            { "data": "CREATE_TIME","class":"title", "width": "200px"},
            { "data": "ID",
            	render: function(data,type,full,meta){
            			data =  "<a class='stdbtn btn_primary editBtn' " +
              				" data-id="+full.ID+" href='#eeda_table'>编辑</a>" +
              				"<a class='stdbtn btn_warning delBtn' " +
              				" data-id="+full.ID+" href='#eeda_table'>删除</a>";
            		return data;
            	} 
            }
        ]
	});
			
	var refleshTable = function(){
		dataTable.ajax.url("/BusinessAdmin/case/list").load();
	}
	
	//编辑
	$("#eeda_table").on("click",".editBtn",function(){
		 var self=$(this);
		 var id=self.data("id");
		 window.location.href="/BusinessAdmin/case?id="+id;
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