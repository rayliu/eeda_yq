define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/ad/cu/list",
            columns: [
	                     { "data":"ORDER_NO","width": "60px" },
	                     { "data": "C_NAME", "width":"80px"},
	                     { "data":"TITLE","width": "120px" },
	                     { "data":"CONTENT","width": "150px" },
	                     { "data": "TOTAL_DAYS", "width":"50px"}, 
	                     { "data": "LEAVE_DAYS", "width":"50px"}, 
	                     { "data": "PRICE", "width":"50px"}, 
	                     { "data": "END_DATE", "width":"60px"},
	                     { "data": "LOC_NAME", "width":"60px"},
	                     { "data": "REMARK", "width":"60px"},
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
        
        //过滤
        $("#location,#category").change(function(){
        	var self = $(this);
        	var location = $("#location").select().val();
        	var category = $("#category").select().val();
        	dataTable.ajax.url("/WebAdmin/ad/cu/list?location="+location+"&category="+category).load();
        })

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
            				checkMessage();
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