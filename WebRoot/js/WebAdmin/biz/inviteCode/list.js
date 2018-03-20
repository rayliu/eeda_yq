define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
$(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/biz/inviteCode/list",
            columns: [
	                     { "data": "INVITE_COUNT", "width":"60px"},
	                     { "data": "CATEGORY_NAME", "width":"60px"},
	                     { "data": "COMPNAY_NAME", "width":"60px"},
	                     { "data": "INVITATION_CODE", "width":"120px"},
	                     { "data": "PHONE", "width":"120px"}
                     ]
        });
        
        
        $('#eeda_table').on('click','.edit',function(){
        	var id = $(this).data('id');
        	location.href = '/WebAdmin/user/edit?id='+ id;
        });
        
        $('#searchBtn').on('click',function(){
        	var begin_date = $('#begin_date').val();
        	var end_date = $('#end_date').val();
      
        	dataTable.ajax.url("/WebAdmin/biz/inviteCode/list?begin_date="+begin_date+"&end_date="+end_date).load();
        });

	
});
});