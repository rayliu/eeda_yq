define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/user/consult/list",
            columns: [
	                     { "data": "USER_NAME", "width":"60px"},
	                     { "data": "MOBILE", "width":"60px"},
	                     { "data": "WEDDING_DATE", "width":"60px"},
	                     { "data": "REMARK", "width":"60px"},
	                     { "data": "CREATE_TIME", "width":"60px"}
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