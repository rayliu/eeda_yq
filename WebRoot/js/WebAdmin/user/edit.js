define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
$(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/user/myprojectList?user_id="+$('#user_id').val(),
            columns: [
	                     { "data": "PROJECT", "width":"60px"},
	                     { "data": "ITEM_NAME", "width":"120px"},
	                     { "data": "COMPLEATE_TIME", "width":"120px"}
                     ]
        });
        
        
        $('#eeda_table').on('click','.edit',function(){
        	var id = $(this).data('id');
        	location.href = '/WebAdmin/user/edit?id='+ id;
        })

 	arrefleshTable = function(){
 		 dataTable.ajax.url("/WebAdmin/user/quotation/list").load();
    }
    	
});
});