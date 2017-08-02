define(['jquery', 'validate_cn',  'dataTablesBootstrap', 'sco', 'file_upload'], function ($, metisMenu) { 
    $(document).ready(function() {
    	
    	$("[name=file_photo]").on('click',function(){
		  var fileid = this.id;
  		  var str = fileid.substring(5,20);
  		  $(this).fileupload({
  				validation: {allowedExtensions: ['*']},
  				autoUpload: true, 
  			    url: '/BusinessAdmin/account/saveFile',
  			    dataType: 'json',
  		        done: function (e, data) {
  	        		if(data){
  			    		$("#img_"+str).val(data.result.NAME);
  			    		var imgPre =Id("img_"+str);
  			  		    imgPre.src = '/upload/'+data.result.NAME;
  			    	}else{
  			    		$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
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
    
    	//datatable, 动态处理
    	var dataTable = eeda.dt({
             id: 'eeda_table',
             paging: true,
             serverSide: true, //不打开会出现排序不对
             ajax: "/WebAdmin/biz/bannerApplication/list",
             columns: [
 	                     { "data": "PRODUCTOR", "width":"120px"},
 	                     { "data": "PHONE", "width":"90px"}, 
 	                     { "data": "AD_LOCATION", "width":"60px"}, 
 	                     { "data": "BEGIN_DATE", "width":"60px"}, 
 	                     { "data": "END_DATE", "width":"60px"}, 
 	                     { "data": "PRICE", "width":"60px"}, 
 	                     { "data": "TOTAL_DAY", "width":"60px"},
 	                     { "data": "TOTAL_PRICE", "width":"60px"},
 	                     { "data": "STATUS", "width":"60px",
 	                    	 render: function(data,type,full,meta){
 	     	            		var data = "";
 	     	            		if(full.STATUS=="已审批"){
 	     	            			data = '已审批'; 
 	     	            		}else if(full.STATUS=='已拒绝'){
 	     	            			data="已拒绝";
 	     	            		}else{
 	     	            			data =  "<button class='modifibtn  wherether_approve' " +
 	     	              					" data-id="+full.ID+" href='#begin_date' status='Y'>审批</button>"+
 	     	              					"&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp" +
 	     	              					"<button class='modifibtn btn-dangger wherether_approve' " +
 	     	              					" data-id="+full.ID+" href='#begin_date' status='N'>拒绝</button>";
 	     	            	  	}
 	     	            		return data;
 	     	            	} 
 		                  }
                      ]
         });
    	 
        //是否审批
        $("#eeda_table").on("click"," .wherether_approve",function(){
        	var result = confirm("确定要这样做吗？");
        	var self = $(this);
        	var id = self.data('id');
        	var status = self.attr("status");
        	if(result){
        		$.post("/WebAdmin/biz/bannerApplication/whetherApprove",{id:id,status:status},function(data){
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
        });
        
   	 	var refleshTable = function(){
      	  dataTable.ajax.url("/WebAdmin/biz/bannerApplication/list").load();
        }
        
        
        $(".update").click(function(){
        	var self = $(this);
        	var loc = self.attr("loc");
        	var info = "";
        	var id = "";
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

        
        $('[name=saveBtn]').click(function(){
        	
        	var self = this;
        	var row = $(self).parent().parent().parent();
        	var user_id = $(row.find('[name=user]')).val();
        	var product_id = $(row.find('[name=product]')).val();
        	var begin_date = $(row.find('[name=begin_date]')).val();
    		var end_date = $(row.find('[name=end_date]')).val();
			var photo = $(row.find('.shadow')).val();
			var index = $(row.find('[name=index]')).val();
			var default_flag = $(row.find('[name=default]')).val();

			self.diabled = true;
			$.post('/WebAdmin/ad/tao/save',{user_id:user_id,product_id:product_id,begin_date:begin_date,end_date:end_date,
				photo:photo,index:index,default_flag:default_flag},function(data){
				if(data){
					$.scojs_message("保存成功",$.scojs_message.TYPE_OK);
				}else{
					$.scojs.message("保存失败",$.scojs_message.TYPE_ERROR);
				}
				self.diabled = false;
			});
        	
        });

        
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