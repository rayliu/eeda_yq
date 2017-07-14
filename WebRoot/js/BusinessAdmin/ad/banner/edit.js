define(['jquery', 'validate_cn', 'sco', 'file_upload'], function ($, metisMenu) {
	$(document).ready(function(){
		//电话不能为空
		$('#weddingform').validate({
			rules: {
				phone : {
				    required: true,
					isMobile:true
				}
			},
		    messages: {
		    	phone: {
		    		required: "电话不能为空!!"
			    }
		    }
		});
		
		
		jQuery.validator.addMethod("isMobile", function(value, element) { 
		  var length = value.length; 
		  var mobile = /^(((13[0-9]{1})|(15[0-9]{1}))+\d{8})$/; 
		  return this.optional(element) || (length == 11 && mobile.test(value)); 
		}, "请正确填写您的手机号码"); 
		
		
		
		
		$("#end_date").on('blur',function(){
			//获取日期
		
			var begin_date = $("#begin_date").val();
			var end_date = $("#end_date").val();
			var v = DateDiff(begin_date,end_date)-0;
			$("#days").text(v);
			var price = $("#price").val()-0;
			var sum=v*price;
			$("#total_price").text(sum);
		
		});
		
		$("#saveBtn").click(function(){
			var self = this;
			
			if(!$("#weddingform").validate()){
				return;
			}
			
			var ad = {};
			ad.begin_date = $("#begin_date").val();
			ad.end_date = $("#end_date").val();
			ad.ad_location = $("#ad_location").val();
			ad.total_price = $("#total_price").text();
			ad.price = $("#price").val();
			ad.phone = $("#phone").val();
			$.post('/BusinessAdmin/ad/saveBanner',{advantage:JSON.stringify(ad)},function(data) {
		    		if(data){
		    			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
		    		}else{
		    			$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
		    		}
		    		$(self).attr('disabled',false);
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