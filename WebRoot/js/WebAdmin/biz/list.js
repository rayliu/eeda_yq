define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/biz/reminder/list",
            columns: [
	                     { "data":"ID","width": "80px"},
	                     { "data": "C_NAME", "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 return "<a href='/WebAdmin/biz/edit?id="+full.ID+"'>"+data+"</a>";
	                    	 }
	                     },
	                     { "data": "USER_NAME", "width":"60px"},
	                     { "data": "CONTACT", "width":"60px"},
	                     { "data": "TELEPHONE", "width":"60px"},
	                     { "data": "TARDE_TYPE", "width":"120px",
	                    	 "render":function(data,type,full,meta){
	                    		 return data;
	                    	 }
	                     }, 
	                     { "data": "PHONE", "width":"60px"},
	                     { "data": "CITY", "width":"60px"},
	                     { "data": "CREATE_TIME", "width":"60px"},
	                     { "data": "LEAVL", "width":"60px"},
	                     { "data": "LEAVE_TIME", "width":"60px"}
                     ]
        });
        
        
        $("#updateDiamond, #updateMobile").click(function(){
        	var self = this;	
        	var btn_id = self.id;
        	var price = $($(self).parent().parent().find('input')).val();
        	self.disabled = true;
        	$.post("/WebAdmin/biz/mobilePush/"+btn_id, {price,price},function(data){
        		if(data){
	    			$.scojs_message('更新成功', $.scojs_message.TYPE_OK);
	    		}else{
	    			$.scojs_message('更新失败', $.scojs_message.TYPE_ERROR);
	    		}
        		self.disabled = false;
        	});
        });
        
        $('#eeda_table').on('click','.action',function(){
    		var self = this;	

    		var sid = $(self).data("id");
    		self.disabled = true;
    		return;
    	  	$.post("/WebAdmin/biz/mobilePush/exam",{id:sid},function(data){
    	  		if(data){
	    			$.scojs_message('操作成功', $.scojs_message.TYPE_OK);
	    			refleshTable();
	    		}else{
	    			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
	    		}
    	  	});
        });
        
       
        var refleshTable = function(){
        	dataTable.ajax.url("/WebAdmin/biz/mobilePush/list").load();
        }
     
        $('#eeda_table').on('click','.action',function(){
    		var self = this;	

    		var sid = $(self).data("id");
    		self.disabled = true;
    	  	$.post("/WebAdmin/biz/mobilePush/exam",{id:sid},function(data){
    	  		if(data){
	    			$.scojs_message('操作成功', $.scojs_message.TYPE_OK);
	    			refleshTable();
	    		}else{
	    			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
	    		}
    	  	});
        });
        
        $('#eeda_table').on('click','.rollback',function(){
    		var self = this;	

    		var sid = $(self).data("id");
    		self.disabled = true;
    	  	$.post("/WebAdmin/biz/mobilePush/rollBack",{id:sid},function(data){
    	  		if(data){
	    			$.scojs_message('操作成功', $.scojs_message.TYPE_OK);
	    			refleshTable();
	    		}else{
	    			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
	    		}
    	  	});
        });
    	
    });
});