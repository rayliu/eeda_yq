define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
		var dataTable = eeda.dt({
	          id: 'eeda_table',
	          paging: true,
	          aLengthMenu:[5,10],
	          serverSide: true, 
	          ajax: "/WebAdmin/ad/tao/list",
	          columns: [
	            { "data": "BEGIN_DATE" ,"width": "100px"},
	            { "data": "END_DATE","class":"title", "width": "100px"},
	            { "data": "PRICE","width": "100px"},
	            { "data": "PHONE","width": "100px" },
	            { "data": "BEGIN_DATE", "width": "100px",
	            	render: function(data,type,full,meta){
	            		var data = "";
	            		if(full.STATUS=="已审批"){
	            			data = '已审批'; 
	            		}else if(full.STATUS=='已拒绝'){
	            			data="已拒绝";
	            		}else{
	            			data =  "<a class=' stdbtn  wherether_approve' " +
	              					" data-id="+full.ID+" href='#begin_date' status='Y'>审批</a>"
	              					+"&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp<a class=' stdbtn btn-dangger color='red' wherether_approve' " +
	              					" data-id="+full.ID+" href='#begin_date' status='N'>拒绝</a>";
	            	  	}
	            		return data;
	            	} 
	            }
	          ]
			});
        
        
        $(".update").click(function(){
        	var self=$(this);
        	var loc=self.attr("loc");
        	var info="";
        	var id="";
        	switch(loc){
	        	case "first":{
	        			id=6;
	        			info=$("#first_ad").val()
	        			break;
	        	}
	        	case "second":{
	        			id=7;
		        		info=$("#second_ad").val()
	        			break;
	        	}
	        	case "third":{
	        			id=8;
		        		info=$("#third_ad").val()
	        			break;
	        	}
	        	case "fourth":{ 
	        			id=9;
		        		info=$("#fourth_ad").val()
	        			break;
	        	}
        	}
        	$.post("/WebAdmin/ad/tao/updatePrice",{id:id,price:info},function(data){
        		if(data){
        			$.scojs_message("成功",$.scojs_message.TYPE_OK);
        		}else{
        			$.scojs_message("成败",$.scojs_message.TYPE_ERROR);
        		}
        	})
        	
        })
        //是否审批
        $("#eeda_table").on("click"," .wherether_approve",function(){
        	var result = confirm("确定要这样做吗？");
        	var self = $(this);
        	var id = self.data('id');
        	var status = self.attr("status");
        	if(result){
        		$.post("/WebAdmin/ad/tao/whetherApprove",{id:id,status:status},function(data){
            		if(data){
            			if(status == "Y"){
            				$.scojs_message("审批成功",$.scojs_message.TYPE_OK);
            			}
            		
            			if(status == "N"){
            				$.scojs_message("已拒绝",$.scojs_message.TYPE_OK);
            			}
            			refleshTable();
            		}else{
            			$.scojs.message("审批失败",$.scojs_message.TYPE_OK);
            		}
            	})
        	}
        })

        
 	 var refleshTable = function(){
   	  dataTable.ajax.url("/WebAdmin/ad/tao/list").load();
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