define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/tao_manage/case/list",
            columns:[
                {	"data":"PRODUCTOR",
                	"width": "80px"
                },
                {	"data": "NAME", "width":"120px"},
	              { "data": "CREATE_TIME", "width":"90px"}, 
	              { "data": "PICTURE_NAME", "width":"90px",
	            	"render":function(data,type,full,meta){
	            		var img ="<img src='/upload/"+data+"' style='width:120px;height:90px;'/>";
	            		return img;
	            	}  
	              }, 
	              { "data": "ID", "width":"60px",
	            	"render":function(data,type,full,meta){
	            		data ="<button  class='modifibtn btn-blue  delete' " 
       							+" data-id="+data+" >删除</button>" 
       							+"&nbsp&nbsp&nbsp<a href='/WebAdmin/tao_manage/case/detail?id="+data+"'>查看或修改信息</a>";
	            		return data;
	            	}  
	              }
            ]
        });
      
        $("#eeda_table").on("click"," .delete",function(){
        	var result = confirm("确定要这样做吗？");
        	var self = $(this);
        	var id = self.data('id');
        	if(result){
        		$.post("/WebAdmin/tao_manage/case/delete",{id:id},function(data){
            		if(data){
            				$.scojs_message("删除成功",$.scojs_message.TYPE_OK);
            				refleshTable();
            		}else{
            			$.scojs.message("操作失败",$.scojs_message.TYPE_OK);
            		}
            	})
        	}
        });

     var searchData=function(){
          var creator = $.trim($("#creator").val()); 
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();

          var url = "/msgBoard/list?create_name_like="+creator
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
      };
      
      var refleshTable = function(){
     	 dataTable.ajax.url("/WebAdmin/tao_manage/case/list").load();
     }
    	
});
});