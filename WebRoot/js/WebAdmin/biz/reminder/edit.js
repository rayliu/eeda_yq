define(['jquery', 'validate_cn', 'sco', 'file_upload'], function ($, metisMenu) {
  $(document).ready(function() {
	  var info = '你确定要让这个商家入驻吗？';
	  //初始化页面js
	  function init(){
		  var status = $("#pass").val();
		  if(status == '通过'){
			  status = '不通过';
			  $("#pass").val(status);
			  info = '你确定不让这个商家入驻吗';
		  }else{
			  status = '通过'
			$("#pass").val(status);
		  }
	  }
	  init();
	  
	  $("#delButton").on('click',function(){
		 $(this).attr('disabled',true);
		 var result = confirm(info);
		 if(result){
			 var id = $("#user_id").val();
			 $.post("/WebAdmin/biz/reminder/delete",{id:id},function(data){
				 if(data){
					 $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
					 window.location.href = "/WebAdmin/biz/reminder";
				 }else{
					 $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
				 }
			 })
		 }
	  })
	  
	  
	 $('#pass').on('click',function(){
		 $(this).attr('disabled',true);
		 var result = confirm("你确定要让这个商家入驻吗？");
		 if(result){
			 var id = $("#user_id").val(); 
			 var status=$("#pass").val();
			 $.post('/WebAdmin/biz/reminder/pass',{id:id,status:status},function(data){
				 if(data){
					 $.scojs_message('处理成功', $.scojs_message.TYPE_OK);
					 window.location.href = "/WebAdmin/biz/reminder";
				 }else{
					 $.scojs_message('处理失败', $.scojs_message.TYPE_ERROR);
				 }
			 });
		 }
		 
	 });
  });
});