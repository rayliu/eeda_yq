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
	                     { "data": "PRODUCTOR", "width":"120px"},
	                     { "data": "ID", "width":"90px",
	                    	 "render":function(data,full){
	                    		 return "暂无";
	                    	 }
	                     }, 
	                     { "data": "ID", "width":"60px",
	                    	 "render":function(data,full){
	                    		 return "暂无";
	                     	 }
	                     }, 
	                     { "data": "CREATE_TIME", "width":"60px"},
	                     { "data": "AMOUNT", "width":"60px"},
	                     { "data": "PUT_IN_TIME", "width":"60px"},
	                     { "data": "PHONE", "width":"60px"},
	                     { "data": "TOTAL_PRICE", "width":"60px"},
	                     { "data": "STATUS", "width":"60px",
	                    	"render":function(data,type,full,meta){
	                    		if(data == "已审批"){
	                    			data = "<button class='btn' disabled>"+data+"</button>"
	                    		}else{
	                    			data = "<button class='btn action' data-id="+full.ID+">审批</button>"
	                    		}
	                    			
	                    		return data;
	                    	} 
	                     }
                     ]
        });
        
        
        $("#update_diamond").click(function(){
        	var price=$("#diamond").val();
        	$.post("/WebAdmin/biz/mobilePush/updateDiamond",{price,price},function(data){
        		if(data){
	    			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	    			$("#diamond_price").text(price);
	    		}else{
	    			$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
	    		}
        	});
        });
        
        
        
        $("#update_mobile").click(function(){
        	var price = $("#mobile").val();
        	$.post("/WebAdmin/biz/mobilePush/updateMobile",{price,price},function(data){
        		if(data){
	    			$.scojs_message('更新成功', $.scojs_message.TYPE_OK);
	    			$("#mobile_price").text(price);
	    		}else{
	    			$.scojs_message('更新失败', $.scojs_message.TYPE_ERROR);
	    		}
        	});
        });
        
        var refleshTable = function(){
        	dataTable.ajax.url("/WebAdmin/biz/mobilePush/list").load();
        }
     
        $('#eeda_table').on('click','.action',function(){
    		var self=$(this);	
    		var sid=$(this).data("id");
    	  	$.post("/WebAdmin/biz/mobilePush/exam",{id:sid},function(data){
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