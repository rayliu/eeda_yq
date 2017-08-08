define(['jquery', 'validate_cn', 'sco', 'file_upload'], function ($, metisMenu) {
	$(document).ready(function(){

		$("#order_form").validate({
			rules:{
				begin_date:{
					required:true,
					afterToday:true
				},
				end_date : {
					required:true,
					afterBegin:true
				}
			},
			messages:{
				begin_date : {
					required:"请选择广告的开始时间！"
				},
				end_date : {
					required:"请选择广告的结束时间！"
				}
				
			}
		})
		
		function getToday(){
			var date = new Date();
			var year = date.getFullYear();
			var month = date.getMonth()+1;
			var day = date.getDate();
			return year+"-"+month+"-"+day;
		}
		
		jQuery.validator.addMethod("afterBegin", function(value, element) { 
				var begin = $("#begin_date").val();
				var after = $("#end_date").val();
			  return new Date(begin) <= new Date(after); 
			}, "开始时间大于结束时间"); 
		
		  jQuery.validator.addMethod("afterToday", function(value, element) { 
			 
			  return new Date(value) >= new Date(getToday()); 
		  }, "请选择今天之后的日期"); 
		
		
		$("#end_date,#begin_date").on('blur',function(){
			//获取日期
			var begin_date = $("#begin_date").val();
			var end_date = $("#end_date").val();
			var v = DateDiff(begin_date,end_date);if(v){
				$("#total_day").text(v);
				var price = $("#price").text();
				var sum=v*price;
				$("#price").text(sum);
			}
		});
		
		$("#saveBtn").click(function(){
			var self = this;
			var ad = {};
			if(!$("#order_form").valid()){
				alert("保存失败，请重新核对填写信息！");
				return;
			}
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
			iDays  =  parseInt(Math.abs(oDate1  -  oDate2)  /  1000  /  60  /  60  /24)    //把相差的毫秒数转换为天数  
			return  iDays  
		}      
	});	
})