define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/user/ask/list",
            columns: [
	                     { "data": "CREATE_TIME", "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 return data;
	                    	 }
	                     },
	                     { "data": "TITLE", "width":"60px"},
	                     { "data": "REPLY_NUMBER", "width":"60px"},
	                     {"width":"120px",
	                    	 "render":function(data,type,full,meta){
	                    		 return '<a href="/WebAdmin/user/ask/details?id='+full.ID+'"><button class="modifibtn">查看</button></a><input class="delete-btn delete" id='+full.ID+' type="button" value="删除"/>';
	                    	 }
	                     }
                     ]
        });

        $("#eeda_table").on("click",".delete",function(){
        	var id = $(this).attr("id");
        	$.post("/WebAdmin/user/ask/deleteQuestion",{id:id},function(data){
        		if(data.result){
        			window.location.reload();
        		}
            });
        });
        
 	 var refleshTable = function(){
   	  dataTable.ajax.url("/WebAdmin/user/quotation/list").load();
    }
});
});