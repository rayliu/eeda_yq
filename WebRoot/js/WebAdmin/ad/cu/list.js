define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/ad/cu/list",
            columns: [
	                     { "data":"ID","width": "80px" },
	                     { "data": "PRODUCTOR", "width":"120px"},
	                     { "data": "TOTAL_DAY", "width":"90px"}, 
	                     { "data": "ID", "width":"60px",
	                    	 "render":function(data,type,full,meta){
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
	                    	"render":function(data,type,full,meta){
	                    		
	                    		if(data=="已审批"){
	                    			data="<button>"+data+"</button>"
	                    		}else{
	                    			data="<a class='stdbtn  exam' data-id="+full.ID+" href='#eeda_table'>审批</a>"
	                    		}	
	                    		return "";
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
   	  dataTable.ajax.url("/WebAdmin/biz/mobilePush/list").load();
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