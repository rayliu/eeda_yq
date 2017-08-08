define(['jquery', 'validate_cn', 'sco', 'file_upload'], function ($, metisMenu) {
	$(document).ready(function(){
		

		//电话不能为空
		$('#weddingform').validate({
			rules: {
				phone : {
				    required: true,
					isMobile:true
				},
				begin_date : {
					required : true,
					afterToday: true
				},
				end_date :{
					required : true,
					afterBegin:true
				}
			},
		    messages: {
		    	phone: {
		    		required: "电话不能为空!!"
			    },
			    begin_date:{
			    	afterToday:"必须大于当前日期"
			    },
			    end_date:{
			    	
			    }
			    
		    }
		});
		
		  //上传公司logo
		  $("#ad_picture").on('click',function(){
			  $(this).fileupload({
					validation: {allowedExtensions: ['*']},
					autoUpload: true, 
				    url: '/BusinessAdmin/ad/saveFile',
				    dataType: 'json',
			        done: function (e, data) {
		        		if(data){
				    		$('#img_logo').attr('value',data.result.NAME);
				    		var imgPre =Id("img_logo");
				  		    imgPre.src = '/upload/'+data.result.NAME;
				    	}else{
				    		$.scojs_message('上传失败', $.scojs_message.TYPE_ERROR);
				    	}
				     },error: function () {
			            alert('上传的时候出现了错误！');
			        }
			   });
		  });
		  
		  //定义id选择器
		  function Id(id){
			  return document.getElementById(id);
		  };
		
		jQuery.validator.addMethod("isMobile", function(value, element) { 
		  var length = value.length; 
		  var mobile = /^(((13[0-9]{1})|(15[0-9]{1}))+\d{8})$/; 
		  return this.optional(element) || (length == 11 && mobile.test(value)); 
		}, "请正确填写您的手机号码"); 
		
		jQuery.validator.addMethod("afterBegin", function(value, element) { 
				var begin = $("#begin_date").val();
				var after = $("#end_date").val();
			  return new Date(begin) < new Date(after); 
			}, "开始时间大于结束时间"); 
		
		  jQuery.validator.addMethod("afterToday", function(value, element) { 
			  
			  return new Date(value) > new Date(); 
		  }, "请选择今天之后的日期"); 
		  
		$("#ad_location").change(function(){
			var self = this;
			var index = this.options.selectedIndex;
			$("#price").text($(self.options[index]).attr("price"));
			account();
		})
		
		//结算
		function account(){
			//获取日期
			var begin_date = $("#begin_date").val();
			var end_date = $("#end_date").val();
			var v = DateDiff(begin_date,end_date);
			if(v>0){
				$("#total_day").text(v);
				var price = $("#price").text();
				var sum = v*price;
				$("#total_price").text(sum);
			}
		}
		
		$("#end_date,#begin_date").on('blur',function(){
			account();
		});
		
		
		$("#saveBtn").click(function(){
			var self = this;
			
			if(!$("#weddingform").valid()){
				alert("填写信息有误，请重新核对！")
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
			order.phone = $("#telphone").val();
			order.remark = $("#remark").val();
			order.picture=$("#img_logo").attr('value');
			self.disabled = true;
			$.post('/BusinessAdmin/ad/banner_save',{jsonStr:JSON.stringify(order)},function(data) {
	    		if(data){
	    			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	    			itemOrder.refleshTable();
	    			window.location.href="/BusinessAdmin/ad/banner";
	    		}else{
	    			$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
	    		}
	    		self.disabled = false;
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
		
		$("#eeda_table").on("click",".editBtn" ,function(){
			var self = $(this);
			var id = self.attr("id");
			var begin_date = self.attr("begin_date");
			var end_date = self.attr("end_date");
			var phone = self.attr("phone");
			var ad_location = self.attr("ad_location");
			var total_day = self.attr("total_day");
			var remark = self.attr("remark");
			var price = self.attr("price");
			var total_price = self.attr("total_price");
			var picture = self.attr("picture");
			$("#order_id").val(id);
			var remark =(self.attr("remark")=='null'?"该订单暂时没备注！":self.attr("remark"));
			$("#begin_date").val(begin_date);
			$("#end_date").val(end_date);
			$("#price").text(price);
			$("#telphone").val(phone);
			$("#ad_location").val(ad_location);
			$("#total_day").text(total_day);
			$("#total_price").text(total_price);
			$("#img_logo").attr('src','/upload/'+picture);
			$("#remark").val(remark);
		});
		
		
		
	});	
});