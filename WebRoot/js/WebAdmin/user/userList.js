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
	                     { "data": "PHONE", "width":"80px",
	                    	 "render":function(data,type,full,meta){
	                    		 return data;
	                    	 }
	                     },
	                     { "data": "WEDDING_DATE", "width":"80px"},
	                     { "data": "CREATE_TIME", "width":"80px"},
	                     { "data": "STATUS", "width":"80px"},
	                     { "data": "PARENT_CODE", "width":"60px"},
	                     { "data": "PARENT_NAME", "width":"60px"},
	                     { "data": "INVITATION_CODE", "width":"60px"},
	                     { "data": "INVITER_NAME", "width":"60px"},
	                     { "data": "REMARK1", "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 if(!data){
	                    			 data = '';
	                    		 }
	                    		 return "<textarea style='width:100%' class='remark1' data-id='"+full.ID+"'>"+data+"</textarea>";
	                    	 }
	                     },
	                     { "data": "REMARK2", "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 if(!data){
	                    			 data = '';
	                    		 }
	                    		 return "<textarea style='width:100%' class='remark2' data-id='"+full.ID+"'>"+data+"</textarea>";
	                    	 }
	                     },
	                     { "data": "REMARK3", "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 if(!data){
	                    			 data = '';
	                    		 }
	                    		 return "<textarea style='width:100%' class='remark3' data-id='"+full.ID+"'>"+data+"</textarea>";
	                    	 }
	                     },
	                     { "data": null, "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 return "<button class='modifibtn edit' data-id='"+full.ID+"'>编辑</button>";
	                    	 }
	                     }
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