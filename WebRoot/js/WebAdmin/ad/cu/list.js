define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/ad/cu/list",
            columns: [
	                     { "data":"ORDER_NO","width": "80px" },
	                     { "data":"TITLE","width": "80px" },
	                     { "data": "PRODUCTOR", "width":"120px"},
	                     { "data": "TOTAL_DAY", "width":"90px"}, 
	                     { "data": "ID", "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 var   d=new   Date(Date.parse(full.END_DATE .replace(/-/g,"/")));
	                    		 if(d<new Date){
	                    			 return "已经过期"
	                    		 }
	                    		 var index=full.END_DATE.indexOf(" ");
	                    		 var endday=full.END_DATE.substring(0,index);
	                    		 var nowday=new Date().getFullYear()+"-"+(new Date().getMonth()+1)+"-"+new Date().getDate();
	                    		
                    		 return DateDiff(nowday,endday);
	                     	}
	                     }, 
	                     { "data": "END_DATE", "width":"60px"},
	                     { "data": "ID", "width":"60px"},
	                     { "data": "ID", "width":"60px"},
	                     { "data": "STATUS", "width":"60px",
	                    	 render: function(data,type,full,meta){
	     	            		var status = "";
	     	            		var info = ""
	     	            		var button = "";
	     	            		if(full.STATUS=="关闭"){
	     	            			button = "modifibtn btn-blue";
	     	            			status="toUp";
	     	            			info = '开启'; 
	     	            		}else if(full.STATUS=='开启'){
	     	            			button="delete-btn"
	     	            			status='toDown'
	     	            			info = "关闭";
	     	            		}
	     	            		data =  "<button  class='"+button+" wherether_carriage' " +
	     	              					" data-id="+full.ID+" href='#begin_date' status="+status+">"+info+"</button>";
	     	            		return data;
	     	            	} 
		                  }
                     ]
        });
        
        //更新状态 
        $("#eeda_table").on("click"," .wherether_carriage",function(){
        	var result = confirm("确定要这样做吗？");
        	var self = $(this);
        	var id = self.data('id');
        	var status = self.attr("status");
        	if(result){
        		$.post("/WebAdmin/ad/cu/whetherCarriage",{id:id,status:status},function(data){
            		if(data){
            			if(status == "toUp"){
            				$.scojs_message("开启成功",$.scojs_message.TYPE_OK);
                			
            			}
            			if(status == "toDown"){
            				$.scojs_message("已关闭",$.scojs_message.TYPE_OK);
            			}
            			refleshTable();
            		}else{
            			$.scojs.message("操作失败",$.scojs_message.TYPE_OK);
            		}
            	})
        	}
        });
        

        
         $("#update_cu").click(function(){
        	var self = this;
        	var price=$("#price").val();
        	self.disabled = true;
        	$.post("/WebAdmin/ad/cu/updateCu",{price,price},function(data){
        		if(data){
	    			$.scojs_message('操作成功', $.scojs_message.TYPE_OK);
	    			$("#cu_price").text(price);
	    		}else{
	    			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
	    		}
        		self.disabled = true;
        	});
        });
        
        
        var refleshTable = function(){
        	 dataTable.ajax.url("/WebAdmin/ad/cu/list").load();
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