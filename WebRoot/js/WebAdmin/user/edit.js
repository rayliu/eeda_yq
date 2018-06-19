define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
$(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/user/myprojectList?user_id="+$('#user_id').val(),
            columns: [
	                     { "data": "PROJECT", "width":"160px"},
	                     { "data": "ITEM_NAME", "width":"120px"},
	                     { "data": "COMPLETE_DATE", "width":"120px"}
                     ]
        });
        
        
        $('#eeda_table').on('click','.edit',function(){
        	var id = $(this).data('id');
        	location.href = '/WebAdmin/user/edit?id='+ id;
        })

 	arrefleshTable = function(){
 		 dataTable.ajax.url("/WebAdmin/user/quotation/list").load();
    }
        
        
        $('.deleteBtn').click(function(){
        	var self = this;
        	var user_id = $('#user_id').val();
        	
        	self.disabled = true;
        	$.post('/WebAdmin/user/deleteUser',{user_id : user_id},function(data){
        		if(data){
        			$.scojs_message('已停用', $.scojs_message.OK);
        			setTimeout(function(){
        				location.href = "/WebAdmin/user";
        			},2000);
        		}else{
        			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
        		}
        	});
        });
    	
});
});