define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/ad/hui/list",
            columns: [
	                     { "data":"ID","width": "80px" },
	                     { "data": "PRODUCTOR", "width":"120px"},
	                     { "data": "IS_ACTIVE", "width":"90px",
	                    	 "render":function(data,type,full,meta){
	                    		 if(data=="Y"){
	                    			 data="已开启";
	                    		 }else if(data=="N"){
	                    			 data="未开启";
	                    		 }else{
	                    			 data="已禁用"
	                    		 }
	                    		 return data;
	                    	 }
	                     }, 
	                     { "data": "DISCOUNT", "width":"60px"}, 
	                     { "data": "ID", "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 return"暂无"
	                    	 }
	                     },
	                     { "data": "ID", "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 return "暂无 ";
	                    	 }
	                     },
	                     { "data": "ID", "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 if(full.IS_ACTIVE=="Y"){
	                    			 var info="disabled";
	                    		 }
	                    		 return data="<button class='open' data-id="+data+" "+info+">开启</button>"
	                    	 }
	                     },
	                     { "data": "ID", "width":"60px",
	                    	"render":function(data,type,full,meta){
	                    		if(full.IS_ACTIVE=="N"){
	                    			 var info="disabled";
	                    		 }
	                    		return data="<button class='close' data-id="+data+" "+info+">关闭</button>"
	                    			} 
	                     },
	                     { "data": "ID", "width":"60px",
		                    	"render":function(data,type,full,meta){
		                    		if(full.IS_ACTIVE=="B"){
		                    			 var info="disabled";
		                    		 }
		                    		return data="<button class='ban' data-id="+data+" "+info+">禁用</button>"
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
        	})
        
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
        		
        	})
        	
        })
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
        		
        	})
        	
        })
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
        		
        	})
        	
        })
        
        $("#update_cu").click(function(){
        	var price=$("#price").val();
        	$.post("/WebAdmin/ad/cu/updateCu",{price,price},function(data){
        		if(data){
	    			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	    			$("#cu_price").text(price);
	    		}else{
	    			$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
	    		}
        	})
        
        })
 	 var refleshTable = function(){
   	  dataTable.ajax.url("/WebAdmin/ad/hui/list").load();
    }
     

		var DateDiff = function  DateDiff(sDate1,sDate2){   //sDate1和sDate2是2006-12-18格式  
			var  aDate,  oDate1,  oDate2,  iDays  ;
			aDate  =  sDate1.split("-")  
			oDate1  =  new  Date(aDate[1]  +  '-'  +  aDate[2]  +  '-'  +  aDate[0])    //转换为12-18-2006格式  
			aDate  =  sDate2.split("-")  
			oDate2  =  new  Date(aDate[1]  +  '-'  +  aDate[2]  +  '-'  +  aDate[0])  
			iDays  =  parseInt(Math.abs(oDate1  -  oDate2)  /  1000  /  60  /  60  /24)    //把相差的毫秒数转换为天数  
			return  iDays  
		}  
    	
});
});