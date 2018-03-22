define(['jquery', 'validate_cn', 'sco', 'file_upload'], function ($, metisMenu) {
	$(document).ready(function(){

		var DateDiff = function  DateDiff(sDate1,sDate2){   //sDate1和sDate2是2006-12-18格式  
			var  aDate,  bDate,oDate1,  oDate2,  iDays  ;
			aDate  =  sDate1.split("-")  
			oDate1  =  new  Date(aDate[1]  +  '-'  +  aDate[2]  +  '-'  +  aDate[0])    //转换为12-18-2006格式  
			bDate  =  sDate2.split("-")  
			oDate2  =  new  Date(bDate[1]  +  '-'  +  bDate[2]  +  '-'  +  bDate[0])
			iDays  =  parseInt(Math.abs(oDate1  -  oDate2)  /  1000  /  60  /  60  /24)    //把相差的毫秒数转换为天数  
			return  iDays  
		}     
		
		
		 $("#title_up").on('click',function(){
			  $(this).fileupload({
					validation: {allowedExtensions: ['*']},
					autoUpload: true, 
				    url: '/BusinessAdmin/video/saveFile',
				    dataType: 'json',
			        done: function (e, data) {
		        		if(data){
				    		$('#cover').val(data.result.NAME);
				    	
				    		var imgPre =Id("cover");
				  		    imgPre.src = '/upload/'+data.result.NAME;
				    	}else{
				    		$.scojs_message('上传失败', $.scojs_message.TYPE_ERROR);
				    	}
				     },error: function () {
			            alert('上传的时候出现了错误！');
			        }
			   });
		  })
		  //定义id选择器
		  function Id(id){
			  return document.getElementById(id);
		  }
		
		
		$("#order_form").validate({
			rules:{
				cover:{
					required:true,
				},
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
				cover:{
					required:"促销图片不能为空"
				},
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
		
		
		$("#end_date,#begin_date").on('change',function(){
			//获取日期
			var begin_date = $("#begin_date").val();
			var end_date = $("#end_date").val();
			if(begin_date != '' && end_date !=''){
				var v = DateDiff(begin_date,end_date);
				if(v){
					$("#total_day").text(v);
					var per_price = $("#per_price").text();
					var sum=v*per_price;
					$("#price").text(sum);
				}else{
					$("#saveBtn").attr("disabled",true);
				}
			}
		});
		
		$("#saveBtn").click(function(){
			var ad = {};
			if(!$("#order_form").valid()){
				return;
			}
			ad.title = $("#title").val();
			ad.content = $("#content").val();
			ad.begin_date = $("#begin_date").val();
			ad.end_date = $("#end_date").val();
			ad.total_day = $("#total_day").text();
			ad.cover = $("#cover").val();
			ad.price = $("#price").text();
			ad.remark=$("#remark").val();
			var self = this;
			self.disabled = true;
			$.post('/BusinessAdmin/ad/cu/save',{advertisement:JSON.stringify(ad)},function(data) {
				if(data){
				//新开支付页面
				  $('#WIDout_trade_no').val(data.ORDER_NO);
				  $('#WIDtotal_amount').val(price);
				  $('#alipayment_form').submit();
				  
				  window.location.href="/BusinessAdmin/ad/cu";
	    		}else{
	    			$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
	    			$(self).attr('disabled',false);
	    		}
			});
		}).fail(function(){
			alert("操作失败");
		});
	});	
})