define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/biz/dimond/list",
            columns: [
	                     { "data":"ORDER_NO","width": "80px" },
	                     { "data": "CREATOR_NAME", "width":"120px"},
	                     { "data": "PUT_IN_DAYS", "width":"90px"}, 
	                     { "data": "LESS_DAYS", "width":"90px"}, 
	                     { "data": "END_DATE", "width":"60px"},
	                     { "data": "TOTAL_PRICE", "width":"60px"},
	                     { "data": "STATUS", "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 if(data == '新建'){
	                    			 data = '待处理'
	                    		 }
	                    		 
	                    		 return data;
	                    	 }
	                     },
	                     { "data": "STATUS", "width":"60px",
	                    	 render: function(data,type,full,meta){
	                    		 var result = '';
	                    		 if(data == '新建'){
	                    			 result = "<button class='modifibtn open' data-id='"+full.ID+"'>开通会员</button>";
	                    		 }else{
	                    			 result = "<button class='delete-btn' data-id='"+full.ID+"'>开通会员</button>";
	                    		 }
	     	            		return result;
	     	            	 } 
		                 }
                     ]
        });
        
        
        $("#eeda_table").on("click",".open",function(){
        	var self= this;
        	var order_id = $(self).data("id");
        	
        	$.post("/WebAdmin/biz/dimond/updateStatus",{order_id:order_id},function(data){
        		if(data){
        			$.scojs_message('操作成功', $.scojs_message.TYPE_OK);
        			refleshTable();
        		}else{
        			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
        		}
        	});
        });
        
/*        //更新状态 
        $("#eeda_table").on("click"," .wherether_carriage",function(){
        	var result = confirm("确定要这样做吗？");
        	var self = $(this);
        	var id = self.data('id');
        	var status = self.attr("status");
        	if(result){
        		$.post("/WebAdmin/ad/cu/whetherCarriage",{id:id,status:status},function(data){
            		if(data){
            			if(status == "toUp"){
            				$.scojs_message("上架成功",$.scojs_message.TYPE_OK);
                			
            			}
            			if(status == "toDown"){
            				$.scojs_message("已下架",$.scojs_message.TYPE_OK);
            			}
            			refleshTable();
            		}else{
            			$.scojs.message("操作失败",$.scojs_message.TYPE_OK);
            		}
            	})
        	}
        });*/
        

        
         $("#updateBtn").click(function(){
        	var self = this;
        	var price = $("#price").val();
        	self.disabled = true;
        	$.post("/WebAdmin/biz/dimond/updatePrice",{price,price},function(data){
        		if(data){
	    			$.scojs_message('更新成功', $.scojs_message.TYPE_OK);
	    			$("#cu_price").text(price);
	    		}else{
	    			$.scojs_message('更新失败', $.scojs_message.TYPE_ERROR);
	    		}
        		self.disabled = false;
        	});
        });
        
        
        var refleshTable = function(){
        	 dataTable.ajax.url("/WebAdmin/biz/dimond/list").load();
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