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
			var v = DateDiff(begin_date,end_date);
			
			if(v){
				$("#total_day").text(v);
				var price = $("#price").text();
				var sum=v*price;
				$("#total_price").text(sum);
			}
			
		});
		
		
		$("#saveBtn").click(function(){
			var self = this;
			
			if(!$("#weddingform").validate()){
				return;
			}
			
			var order = {};
			order.id=$("#order_id").val();
			order.begin_date = $("#begin_date").val();
			order.end_date = $("#end_date").val();
			order.ad_location = $("#ad_location").val();
			order.total_price = $("#total_price").text();
			order.total_day = $("#total_day").text();
			order.price = $("#price").text();
			order.phone = $("#phone").val();
			order.remark = $("#remark").val();
			self.disabled = true;
			$.post('/BusinessAdmin/ad/banner_save',{jsonStr:JSON.stringify(order)},function(data) {
	    		if(data){
	    			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	    			itemOrder.refleshTable();
	    		}else{
	    			$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
	    		}
	    		self.disabled = false;
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
		
		$("#eeda_table").on("click",".editBtn" ,function(){
			var self = $(this);
			var id = self.attr("id");
			var begin_date = self.attr("begin_date");
			var end_date = self.attr("end_date");
			var phone = self.attr("phone");
			var ad_location = self.attr("ad_location");
			var total_day = self.attr("total_day");
			var total_price = self.attr("total_price");
			$("#order_id").val(id);
			var remark =(self.attr("remark")=='null'?"该订单暂时没备注！":self.attr("remark"));
			$("#begin_date").val(begin_date);
			$("#end_date").val(end_date);
			$("#phone").val(phone);
			$("#ad_location").val(ad_location);
			$("#total_day").text(total_day);
			$("#total_price").text(total_price);
			$("#remark").text(remark);
		})
		
	});	
})