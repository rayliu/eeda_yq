define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/user/quotation/list",
            columns: [
	                     { "data":"ID","width": "80px" },
	                     { "data": "CONTACT_PERSON", "width":"120px"},
	                     { "data": "COMPANY", "width":"120px"},
	                     { "data": "PRODUCT_PAGE", "width":"90px",
	                    	 "render":function(data,type,full,meta){
	                    		
	                    		 return data;
	                    	 }
	                     }, 
	                     { "data": "SUBMIT_TIME", "width":"60px"}, 
	                     { "data": "EXPECT_TIME", "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 return data;
	                    	 }
	                     },
	                     { "data": "PHONE", "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 return data;
	                    	 }
	                     },
	                     { "data": "REMARK", "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 data="<textarea name='remark"+full.ID+"'  cols='40' >"+data+"</textarea>"
	                    			+"	<button class='update_remark' style='width:50px' type='button'  data-id="+full.ID+">确定</button>" ;
	                    		 return data;
	                    	 }
	                     }
                     ]
        });
        $("#eeda_table").on("click",".update_remark",function(){
        	var self=$(this);
        	var id=self.data('id');
        	var remark=$("textarea[name=remark"+self.data('id')+"]").val();
        	$.post("/WebAdmin/user/quotation/updateRemark",{id:id,remark:remark},function(data){
        		if(data){
        			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
        			refleshTable();
        		}else{
        			$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
        		}
        	})
        })


 	 var refleshTable = function(){
   	  dataTable.ajax.url("/WebAdmin/user/quotation/list").load();
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