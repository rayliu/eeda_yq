define(['jquery', 'validate_cn', 'sco', 'file_upload'], function ($, metisMenu) {
  $(document).ready(function() {
	  
	  var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: true,
          serverSide: true, //不打开会出现排序不对
          ajax: "/WebAdmin/biz/editList?id="+$("#user_id").val(),
          columns: [
	                     { "data":"ID","width": "80px"},
	                     { "data": "CREATE_TIME", "width":"60px"},
	                     { "data": "ORDER_NO", "width":"60px"},
	                     { "data": "TYPE", "width":"60px"},
	                     { "data": "DURINGDAY", "width":"60px"},
	                     { "data": "TOTAL_DAY", "width":"60px"},
	                     { "data": "PRICE", "width":"120px",
	                    	 "render":function(data,type,full,meta){
	                    		 return data;
	                    	 }
	                     }
	                    
                   ]
      });
	  
	  var info = '你确定要让这个商家入驻吗？';
	  //初始化页面js
	  function init(){
		 
	  }
	  init();
	  
	  $("#delButton").on('click',function(){
		var self=$(this);
		 var result = confirm(info);
		 if(result){
			 $(this).attr('disabled',true);
			 var id = $("#user_id").val();
			 $.post("/WebAdmin/biz/delete",{id:id},function(data){
				 if(data){
					 $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
					 window.location.href = "/WebAdmin/biz/";
				 }else{
					 $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
				 }
			 })
		 }
	  });
	  
	  $("#update_dimond").click(function(){
		  var id = $("#user_id").val();
		  var t=new Date();
		  var today=(t.getFullYear()+"-"+(t.getMonth()+1)+"-"+t.getDate());
		  var update = $("#new_date").val()==""?today:$("#new_date").val();
		  var last_date = $("#new_date").attr("last_date")==""?today:$("#new_date").attr("last_date");
		  var begin_date=today>=last_date?today:last_date;
		  if(new Date(update)<= new Date(today)||new Date(update)<=new Date(last_date)){
			  alert("请选择合适的更新日期！！")
			  return;
		  }
		  $.post("/WebAdmin/biz/updateDimond",	{
			  									id:id,begin_date:begin_date,end_date:update
			  									},function(data){
	      		if(data){
	      			$.scojs_message('操作成功', $.scojs_message.TYPE_OK);
	      		}else{
	      			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
	      		}
	      	});
		  
	  })
	  
	   $("#update_cu").click(function(){
		  var id = $("#user_id").val();
		  var update = $("#cu_date").val();
		  var t=new Date();
		  var today=(t.getFullYear()+"-"+(t.getMonth()+1)+"-"+t.getDate());
		  var last_date = $("#cu_date").attr("last_date") == ''?today:$("#cu_date").attr("last_date");
		  var begin_date=new Date()>=last_date?'':last_date;
		  if(new Date(update)<=new Date(today)||new Date(update)<=new Date(last_date)){
			  alert("请选择合适的更新日期！！")
			  return;
		  }
		  $.post("/WebAdmin/biz/updateCu",	{
			  									id:id,begin_date:begin_date,end_date:update
			  									},function(data){
	      		if(data){
	      			$.scojs_message('操作成功', $.scojs_message.TYPE_OK);
	      		}else{
	      			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
	      		}
	      	});
		  
	  })
	  
      $("#closeButton,#openButton,#banButton").click(function(){
      	var self=$(this);
      	self.attr("disabled",true);
      	var id=$("#user_id").val();
      	var status=self.attr('status');
      	$.post("/WebAdmin/biz/updateStatus",{id:id,status:status},function(data){
      		if(data){
      			$.scojs_message('操作成功', $.scojs_message.TYPE_OK);
      			self.siblings().attr('disabled',false);
      			$("#hui_status").val(status);
      		}else{
      			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
      		}
      	});
      });
	  
	  var DateDiff = function  DateDiff(sDate1,sDate2){   //sDate1和sDate2是2006-12-18格式  
			var  aDate,  bDate,oDate1,  oDate2,  iDays  ;
			aDate  =  sDate1.split("-")  
			oDate1  =  new  Date(aDate[1]  +  '-'  +  aDate[2]  +  '-'  +  aDate[0])    //转换为12-18-2006格式  
			bDate  =  sDate2.split("-")  
			oDate2  =  new  Date(bDate[1]  +  '-'  +  bDate[2]  +  '-'  +  bDate[0])
			iDays  =  parseInt(Math.abs(oDate1  -  oDate2)  /  1000  /  60  /  60  /24)    //把相差的毫秒数转换为天数  
			return  iDays  
		} 
	  
  });
});