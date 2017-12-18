define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/user/list",
            columns: [
	                     { "data": "USER_NAME", "width":"60px"},
	                     { "data": "PHONE", "width":"120px",
	                    	 "render":function(data,type,full,meta){
	                    		 return data;
	                    	 }
	                     },
	                     { "data": "INVITATION_CODE", "width":"120px"},
	                     { "data": "WEDDING_DATE", "width":"120px"},
	                     { "data": "CREATE_TIME", "width":"120px"}
                     ]
        });

 	 var refleshTable = function(){
   	  dataTable.ajax.url("/WebAdmin/user/quotation/list").load();
    }
    	
});
});