
define(['jquery', 'dataTablesBootstrap', 'validate_cn', 'sco','file_upload'], function ($, metisMenu) {
	var img_num;
  $(document).ready(function() {

	  function Id(id){
		  return document.getElementById(id);
	  };

	var dataTable = eeda.dt({
			id: 'eeda_table',
			paging: true,
			aLengthMenu:[5,10],
			serverSide: true, 
			ajax: "/WebAdmin/best_wedding/list",
			columns: [
				          { "data": "PRODUCTOR" ,"width": "20%"},
				          { "data": "ID" ,"width": "10%"},
				          { "data": "NAME" ,"width": "20%"},
				          { "data": "PICTURE_NAME" ,"width":"30%px",
				        	  "render":function(data){
				        		  return"<img src='/upload/"+data+"' style='width:200px'/><a href='#'>查看更多 </a>"
				        	  }
				          },
				          { "data": "ID" ,"width": "15%",
							"render":function(data,type,full,meta){
								var info = "";
								if(full.FLAG == 1){
									info = "checked";
								}
								return"<button class='btn btn-danger delBtn' data-id="+data+">删除</button>"
									+"<label class='padding10 '><input class='check' type='checkbox' name='checkbox' data-id="+data+" "+info+">精选</label>";
							}
				          }
					  ]
					});
			
		 var refleshTable = function(){
		    	  dataTable.ajax.url("/WebAdmin/best_wedding/list").load();
		     }
		 
		 $("#eeda_table").on("click",".delBtn",function(){
			 var self = $(this);
			 var id = self.data("id");
			 var del = confirm("你确定要删除吗")
			 var id = self.data("id");
			 if(!del){
				 return;
			 }
			 $.post("/WebAdmin/best_wedding/delete",{"id":id},function(data){
				 if(data){
					  $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
					  refleshTable();
				 }else{
					 $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
				 }
			 })
		 });
		 
		 
		  $("#eeda_table").on("click",".check",function(){
			 var self = $(this);
			 var id = self.data("id");
			 var flag;
			 var update = confirm("你确定要这样做吗")
			 if(self.is(":checked")){
				 flag = 1;
			 }else{
				 flag = 0;
			 };
			 if(!update){
				 return;
			 }
			 $.post("/WebAdmin/best_wedding/updateFlag",{"id":id,"flag":flag},function(data){
				 if(data){
					  $.scojs_message('更新成功', $.scojs_message.TYPE_OK);
					  refleshTable();
				 }else{
					 $.scojs_message('更新失败', $.scojs_message.TYPE_ERROR);
				 }
			 })
		 })
  });
});