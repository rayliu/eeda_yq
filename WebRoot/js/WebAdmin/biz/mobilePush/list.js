define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/biz/mobilePush/list",
            columns: [
	                     { "data":"ORDER_NO","width": "80px"},
	                     { "data": "PRODUCTOR", "width":"120px",
                            "render":function(data,type,full,meta){
                                 return "<a href='/WebAdmin/biz/reminder/edit?id="+full.WC_ID+"'>"+data+"</a>";
                             }
                         }, 
	                     { "data": "CREATE_TIME", "width":"60px"},
	                     { "data": "AMOUNT", "width":"60px"},
	                     { "data": "PUT_IN_TIME", "width":"60px"},
	                     { "data": "PHONE", "width":"60px"},
	                     { "data": "TOTAL_PRICE", "width":"60px"},
	                     { "data": "REMARK", "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 return data;
	                    	 }
	                     },
	                     { "data": "STATUS", "width":"60px",
	                    	"render":function(data,type,full,meta){
	                    		if(data == "已审批"){
	                    			data = "<button class='delete-btn rollback' data-id="+full.ID+">"+data+"</button>"
	                    		}else{
	                    			data = "<button class='modifibtn action' data-id="+full.ID+">审批</button>"
	                    		}
	                    			
	                    		return data;
	                    	} 
	                     }
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
	    			checkMessage();
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
	    			checkMessage();
	    		}else{
	    			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
	    		}
    	  	});
        });
    	
    });
});