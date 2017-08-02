
define(['jquery', 'dataTablesBootstrap', 'validate_cn', 'sco','file_upload'], function ($, metisMenu) {
$(document).ready(function() {

	var dataTable = eeda.dt({
			id: 'eeda_table',
			paging: true,
			serverSide: true, 
			ajax: "/WebAdmin/best_wedding/list",
			columns: [
				        { "data": "PRODUCTOR", "width":"100px"},
				        { "data": "DISTRICT" , "width":"100px",
				        	"render":function(data,type,full,meta){
				        		var address='';
				        		if(full.CISTRICT!=null){
				        			address = full.DISTRICT;
				        		}else if(full.CITY!=null){
				        			address = full.CITY;
				        		}else if(full.PROVINCE!=null){
				        			address = full.PROVINCE;
				        		}else{
				        			address = '暂无';
				        		}
				        		return address;
				        	}
				        },
				        { "data": "NAME", "width":"100px"},
				        { "data": "PICTURE_NAME" ,"width":"120px",
				        	  "render":function(data){
				        		  return"<img src='/upload/"+data+"' style='width:120px; height:90px'/>"
				        	  }
				        },
				        { "data": "ID" ,"width":"50px",
							"render":function(data,type,full,meta){
								var info = "";
								if(full.FLAG == 1){
									info = "checked";
								}
								return"<button class='delete-btn delBtn' data-id='"+data+"'>删除精选</button>";
							}
				        }
					]
		});	
			
		 var refleshTable = function(){
		    	  dataTable.ajax.url("/WebAdmin/best_wedding/list").load();
		     }
		 
		 $("#eeda_table").on("click",".delBtn",function(){
			 var self = this;
			 var id = $(self).data("id");
			 var del = confirm("你确定要删除精选吗")
			 if(!del){
				 return;
			 }
			 self.disabled = true;
			 $.post("/WebAdmin/best_wedding/delete",{"id":id},function(data){
				 if(data){
					  $.scojs_message('删除精选成功', $.scojs_message.TYPE_OK);
					  refleshTable();
				 }else{
					 $.scojs_message('删除精选失败', $.scojs_message.TYPE_ERROR);
				 }
				 self.diabled = false;
			 });
		 });
		 
		 
		  $("#eeda_table").on("click",".check",function(){
			 var self = this;
			 var id = $(self).data("id");
			 var flag;
			 if($(self).is(":checked")){
				 flag = 1;
			 }else{
				 flag = 0;
			 };
			 self.disabled = true;
			 $.post("/WebAdmin/best_wedding/updateFlag",{"id":id,"flag":flag},function(data){
				 if(data){
					  $.scojs_message('更新成功', $.scojs_message.TYPE_OK);
					  refleshTable();
				 }else{
					 $.scojs_message('更新失败', $.scojs_message.TYPE_ERROR);
				 }
				 self.disabled = false;
			 });
		 });
  });
});