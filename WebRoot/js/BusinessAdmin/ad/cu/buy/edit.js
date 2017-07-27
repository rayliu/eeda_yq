define(['jquery', 'validate_cn', 'sco', 'file_upload'], function ($, metisMenu) {
	$(document).ready(function(){

		$("#end_date,#begin_date").on('blur',function(){
			//获取日期
			var v=0,sum=0, price=0;
			var begin_date = $("#begin_date").val();
			var end_date = $("#end_date").val();
			if(!($.trim(begin_date)==""||$.trim(end_date)=="")){
				v = DateDiff(end_date,begin_date);
				 price = $("#per_price").text();
			}
			$("#total_day").text(v);
			 sum=v*price;
			$("#price").text(sum);
		
		});
		
		$("#saveBtn").click(function(){
			var self = this;
			var ad = {};
			ad.begin_date = $("#begin_date").val();
			ad.end_date = $("#end_date").val();
			ad.total_day = $("#total_day").text();
			ad.price = $("#price").text();
			ad.remark=$("#remark").val();
			$.post('/BusinessAdmin/ad/cu/save',{advertisement:JSON.stringify(ad)},function(data) {
				if(data){
		    			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
		    			$(self).attr('disabled',false);
		    			window.location.href="http://localhost:8080/BusinessAdmin/ad/cu";
		    		}else{
		    			$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
		    		}
		    		
				});
			});
		
		
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
})