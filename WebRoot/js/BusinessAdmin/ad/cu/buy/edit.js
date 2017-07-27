define(['jquery', 'validate_cn', 'sco', 'file_upload'], function ($, metisMenu) {
	$(document).ready(function(){

		$("#end_date,#begin_date").on('blur',function(){
			//获取日期
			var begin_date = $("#begin_date").val();
			var end_date = $("#end_date").val();
			var v = DateDiff(begin_date,end_date);
			if(v=="N"){
				alert("选择的日期不合法，请重新选择")
				window.location.href="http://localhost:8080/BusinessAdmin/ad/cu/buy";
			}else if(v){
				$("#total_day").text(v);
				var price = $("#price").text();
				var sum=v*price;
				$("#price").text(sum);
			}
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
			var  aDate,  bDate,oDate1,  oDate2,  iDays  ;
			aDate  =  sDate1.split("-")  
			oDate1  =  new  Date(aDate[1]  +  '-'  +  aDate[2]  +  '-'  +  aDate[0])    //转换为12-18-2006格式  
			bDate  =  sDate2.split("-")  
			oDate2  =  new  Date(bDate[1]  +  '-'  +  bDate[2]  +  '-'  +  bDate[0])
			if(new Date(oDate1)>new Date(oDate2)){
				return "N";
			}
			iDays  =  parseInt(Math.abs(oDate1  -  oDate2)  /  1000  /  60  /  60  /24)    //把相差的毫秒数转换为天数  
			return  iDays  
		}      
	});	
})