
define(['jquery', 'dataTablesBootstrap', 'validate_cn', 'sco','file_upload'], function ($, metisMenu) {
$(document).ready(function() {

	var dataTable = eeda.dt({
			id: 'eeda_table',
			paging: true,
			serverSide: true, 
			ajax: "/WebAdmin/best_wedding/list",
			columns: [
						{ "data": "ID" ,"width":"20px",
							"render":function(data,type,full,meta){
								var info = "";
								if(full.FLAG == 1){
									info = "checked";
								}
								return  "<input class='check' style='width:20px' type='checkBox' name='checkbox' data-id='"+data+"' "+info+">";
							}
						},
				        { "data": "PRODUCTOR", "width":"100px"},
				        { "data": "" , "width":"100px"},
				        { "data": "NAME", "width":"100px"},
				        { "data": "PICTURE_NAME" ,"width":"120px",
				        	  "render":function(data){
				        		  return"<img src='/upload/"+data+"' style='width:120px; height:90px'/><a>查看更多 </a>"
				        	  }
				        },
				        { "data": "ID" ,"width":"50px",
							"render":function(data,type,full,meta){
								var info = "";
								if(full.FLAG == 1){
									info = "checked";
								}
								return"<button class='btn btn-danger delBtn' data-id='"+data+"'>删除</button>";
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
			 var del = confirm("你确定要删除吗")
			 if(!del){
				 return;
			 }
			 self.diabled = true;
			 $.post("/WebAdmin/best_wedding/delete",{"id":id},function(data){
				 if(data){
					  $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
					  refleshTable();
				 }else{
					 $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
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