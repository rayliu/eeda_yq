define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/ad/hui/list",
            columns: [
	                     { "data": "PRODUCTOR", "width":"120px"},
	                     { "data": "IS_ACTIVE", "width":"90px",
	                    	 "render":function(data,type,full,meta){
	                    		 if(data=="Y"){
	                    			 data="已开启";
	                    		 }else if(data=="N"){
	                    			 data="未开启";
	                    		 }else if(data=='B'){
	                    			 data="已禁用"
	                    		 }
	                    		 return data;
	                    	 }
	                     }, 
	                     { "data": "DISCOUNT", "width":"60px"}, 
	                     { "data": "LOCATION", "width":"60px"},
	                     { "data": "TRADE_TYPE_NAME", "width":"60px"},
	                     { "data": "ID", "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 
	                    		 var returnDate;
	                    		 if(full.IS_ACTIVE=="Y"){
	                    			 returnDate = "<button class='delete-btn' disabled >开启</button>";
	                    		 }else{
	                    			 returnDate ="<button class='modifibtn open' data-id='"+data+"' >开启</button>";
	                    		 }
	                    		 return returnDate;
	                    	 }
	                     },
	                     { "data": "ID", "width":"60px",
	                    	"render":function(data,type,full,meta){
	                    		 var returnDate;
	                    		 if(full.IS_ACTIVE=="N"){
	                    			 returnDate = "<button class='delete-btn' disabled >关闭</button>";
	                    		 }else{
	                    			 returnDate ="<button class='modifibtn close' data-id='"+data+"' >关闭</button>";
	                    		 }
	                    		 return returnDate;
	                    	} 
	                     },
	                     { "data": "ID", "width":"60px",
		                    	"render":function(data,type,full,meta){
		                    		 var returnDate;
		                    		 if(full.IS_ACTIVE=="B"){
		                    			 returnDate = "<button class='delete-btn' disabled >禁用</button>";
		                    		 }else{
		                    			 returnDate ="<button class='modifibtn ban' data-id='"+data+"'>禁用</button>";
		                    		 }
		                    		 return returnDate;
		                    	} 
		                  }
                     ]
        });
        

        //按地区过滤
        $("#location").change(function(){
        	var self = $(this);
        	var location = self.select().val();
        	dataTable.ajax.url("/WebAdmin/ad/hui/list?location="+location).load();
        })
        //按商家类型过滤
           $("#category").change(function(){
        	var self = $(this);
        	var category = self.select().val();
        	dataTable.ajax.url("/WebAdmin/ad/hui/list?category="+category).load();
        })
        
        $("#eeda_table").on("click",".open",function(){
        	var self=$(this);
        	var id=self.data("id");
        	var status="Y";
        	$.post("/WebAdmin/ad/hui/updateStatus",{id:id,status:status},function(data){
        		if(data){
        			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
        			refleshTable();
        		}else{
        			$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
        		}
        	});
        });
        
        
        $("#eeda_table").on("click",".close",function(){
        	var self=$(this);
        	var id=self.data("id");
        	var status="N";
        	$.post("/WebAdmin/ad/hui/updateStatus",{id:id,status:status},function(data){
        		if(data){
        			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
        			refleshTable();
        		}else{
        			$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
        		}
        	});
        });
        
        
       $("#eeda_table").on("click",".ban",function(){
        	var self=$(this);
        	var id=self.data("id");
        	var status="B";
        	$.post("/WebAdmin/ad/hui/updateStatus",{id:id,status:status},function(data){
        		if(data){
        			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
        			refleshTable();
        		}else{
        			$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
        		}
        	});
        });
        

     
        var refleshTable = function(){
        	dataTable.ajax.url("/WebAdmin/ad/hui/list").load();
 	 	}
    	
});
});