define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/biz/list",
            columns: [
	                     { "data":"UID","width": "80px"},
	                     { "data": "C_NAME", "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 if(data == null){
	                    			 data=full.USER_NAME;
	                    		 }
	                    		 return "<a href='/WebAdmin/biz/edit?id="+full.UID+"'>"+data+"</a>";
	                    	 }
	                     },
	                     { "data": "USER_NAME", "width":"60px"},
	                     { "data": "CONTACT", "width":"60px"},
	                     { "data": "PHONE", "width":"60px"},
	                     { "data": "TRADE_TYPE_NAME", "width":"120px",
	                    	 "render":function(data,type,full,meta){
	                    		 return data;
	                    	 }
	                     }, 
	                     { "data": "TELEPHONE", "width":"60px"},
	                     { "data": "LOCATION", "width":"60px"},
	                     { "data": "CREATE_TIME", "width":"60px"},
	                     { "data": "LEAVE_DAYS", "width":"60px"
	                    	 		
	                     },
	                     { "data": "LEAVE_DAYS", "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 if(data<0){
	                    			 data = "已过期";
	                    		 }
	                    		 return data;
	                    	 }
	                     }
                     ]
        });
        
        //按地区过滤
        $("#location").change(function(){
        	var self = $(this);
        	var location = self.select().val();
        	dataTable.ajax.url("/WebAdmin/biz/list?location="+location).load();
        });
        
        //按商家类型过滤
        $("#user_type").change(function(){
        	var self = $(this);
        	var user_type = self.select().val();
        	dataTable.ajax.url("/WebAdmin/biz/list?user_type="+user_type).load();
        });
        
        
        var refleshTable = function(){
        	dataTable.ajax.url("/WebAdmin/biz/list").load();
        }

    });
});