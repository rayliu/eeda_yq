define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/biz/diamond/list",
            columns: [
	                     { "data":"ORDER_NO","width": "20%" },
	                     { "data": "C_NAME", "width":"15%"},
	                     { "data": "DAYS", "width":"5%"}, 
	                     { "data": "BEGIN_DATE", "width":"10%"},
	                     { "data": "END_DATE", "width":"10%"},
	                     { "data": "TOTAL_PRICE", "width":"5%"},
	                     { "data": "REMARK", "width":"20%"},
	                     { "data": "STATUS", "width":"15%",
	                    	 render: function(data,type,full,meta){
	                    		 var result = '';
	                    		 if(data == '新建'){
	                    			 result = "<button class='modifibtn open' data-id='"+full.ID+"'>开通会员</button>";
	                    		 }else{
	                    			 result = "<button class='delete-btn' disabled data-id='"+full.ID+"'>已开通</button>";
	                    		 }
	     	            		return result;
	     	            	 } 
		                 }
                     ]
        });
        
        
        $("#eeda_table").on("click",".open",function(){
        	var self= this;
        	var order_id = $(self).data("id");
        	var result = confirm("你确定为该商家开通钻石会员吗？");
        	if(result){
        		$.post("/WebAdmin/biz/diamond/updateStatus",{order_id:order_id},function(data){
            		if(data){
            			$.scojs_message('操作成功', $.scojs_message.TYPE_OK);
            			refleshTable();
            		}else{
            			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
            		}
            	});
        	}
        });
        
         $("#updateBtn").click(function(){
        	var self = this;
        	var price = $("#price").val();
        	self.disabled = true;
        	$.post("/WebAdmin/biz/diamond/updatePrice",{price,price},function(data){
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
        	 dataTable.ajax.url("/WebAdmin/biz/diamond/list").load();
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