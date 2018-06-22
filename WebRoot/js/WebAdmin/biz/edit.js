define(['jquery', 'validate_cn', 'sco', 'file_upload'], function ($, metisMenu) {
  $(document).ready(function() {
	  
	  var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: true,
          serverSide: true, //不打开会出现排序不对
          ajax: "/WebAdmin/biz/editList?id="+$("#user_id").val(),
          columns: [
	                     { "data":"ID","width": "80px"},
	                     { "data": "CREATE_TIME", "width":"60px"},
	                     { "data": "ORDER_NO", "width":"60px"},
	                     { "data": "TYPE", "width":"60px"},
	                     { "data": "DURINGDAY", "width":"60px"},
	                     { "data": "TOTAL_DAY", "width":"60px"},
	                     { "data": "PRICE", "width":"120px",
	                    	 "render":function(data,type,full,meta){
	                    		 return data;
	                    	 }
	                     }
                   ]
      });
	
	var table_id = 'invite_list';
	var add_btn = 'add_btn';
	var table_url = "/WebAdmin/biz/inviteList?user_id="+$("#user_id").val();
	  
	var dataTable = eeda.dt({
        id: table_id,
        paging: true,
        serverSide: false, //不打开会出现排序不对
        ajax: table_url,
        createdRow:function ( row, data, index ) {
            if(data){
                $(row).attr('id', data.ID);
            }
        },
        columns: [
			{ "data": "ID", "width":"10px",
			    "render":function(data,type,full,meta){
			    	if(!data){
			    		data = '';
			    	}
			    	return '<button type="button" class="delete layui-btn" item_id="'+full.ID+'"">删除</button>';
			    }
			},
	        { "data": "INVITE_CODE", "width":"60px",
	            "render":function(data,type,full,meta){
	            	if(!data)
	            		data = '';
	                return '<input type="text" name="invite_code" style="width:100%;" value="'+data+'">';
	            }
	        },
	        { "data": "INVITER_NAME", "width":"60px",
	            "render":function(data,type,full,meta){
	            	if(!data)
	            		data = '';
	                return '<input type="text" name="inviter_name" style="width:100%" value="'+data+'">';
	            }
	        },
	        { "data": "PHONE", "width":"60px",
	            "render":function(data,type,full,meta){
	            	if(!data)
	            		data = '';
	                return '<input type="text" name="phone" style="width:100%" value="'+data+'">';
	            }
	        },
	        { "data": "ALIPAY_NO", "width":"60px",
	            "render":function(data,type,full,meta){
	            	if(!data)
	            		data = '';
	                return '<input type="text" name="alipay_no" style="width:100%" value="'+data+'">';
	            }
	        },
	        { "data": "COMPANY", "width":"60px",
	            "render":function(data,type,full,meta){
	            	if(!data)
	            		data = '';
	                return '<input type="text" name="company" style="width:100%" value="'+data+'">';
	            }
	        },
	        { "data": "REMARK", "width":"60px",
	            "render":function(data,type,full,meta){
	            	if(!data)
	            		data = '';
	                return '<textarea name="remark" style="width:100%">'+data+'</textarea>';
	            }
	        }           
        ]
    });
	
	
	  //保存后异步刷新
	var item_table = $('#'+table_id).DataTable();
    reflesh_table = function(){
    	var url = table_url;
    	item_table.ajax.url(url).load();
    };
	

    $('#' + add_btn).on('click', function(){
        var item = {};
        
        $.post("/WebAdmin/biz/add_item",{user_id:$("#user_id").val()},function(data){
        	if(data){
        		reflesh_table();
        	}else{
        		$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
        	}
        }).fail(function(){
        	$.scojs_message('后台报错', $.scojs_message.TYPE_ERROR);
        })
    });
    
    //删除一行
    $("#" + table_id).on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();

        $.post("/WebAdmin/biz/delete_item",{item_id:$(this).attr('item_id')},function(data){
        	if(data){
        		reflesh_table();
        	}else{
        		$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
        		
        	}
        }).fail(function(){
        	$.scojs_message('后台报错', $.scojs_message.TYPE_ERROR);
        });
    }); 
    
    //编辑一行
    $("#" + table_id).on('blur', 'input,textarea', function(e){
    	e.preventDefault();
    	var self = this;
    	
    	var item_id = $(self).parent().parent().attr('id');
    	var item_name = $(self).attr('name');
    	var item_value = $(self).val();

    	$.post("/WebAdmin/biz/update_item",{item_id: item_id, item_name: item_name, item_value: item_value},function(data){
        	if(data){
        		//reflesh_table();
        	}else{
        		$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
        		
        	}
        }).fail(function(){
        	$.scojs_message('后台报错', $.scojs_message.TYPE_ERROR);
        });
    	
    });

	  
	  $("#delButton").on('click',function(){
		var self=$(this);
		 var result = confirm("你确定要删除这个商家吗");
		 if(result){
			 $(this).attr('disabled',true);
			 var id = $("#user_id").val();
			 $.post("/WebAdmin/biz/delete",{id:id},function(data){
				 if(data){
					 $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
					 window.location.href = "/WebAdmin/biz/";
				 }else{
					 $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
				 }
			 })
		 }
	  });
	  
	  $("#update_dimond").click(function(){
		  var id = $("#user_id").val();
		  var t=new Date();
		  var today=(t.getFullYear()+"-"+(t.getMonth()+1)+"-"+t.getDate());
		  var update = $("#new_date").val()==""?today:$("#new_date").val();
		  var last_date = $("#new_date").attr("last_date")==""?today:$("#new_date").attr("last_date");
		  var begin_date=today>=last_date?today:last_date;
		  if(new Date(update)<= new Date(today)||new Date(update)<=new Date(last_date)){
			  alert("请选择合适的更新日期！！")
			  return;
		  }
		  $.post("/WebAdmin/biz/updateDiamond",	{
			  									id:id,begin_date:begin_date,end_date:update
			  									},function(data){
	      		if(data){
	      			$.scojs_message('操作成功', $.scojs_message.TYPE_OK);
	      		}else{
	      			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
	      		}
	      	});
		  
	  });
	  
	   $("#update_cu").click(function(){
		  var id = $("#user_id").val();
		  var update = $("#cu_date").val();
		  var t=new Date();
		  var today=(t.getFullYear()+"-"+(t.getMonth()+1)+"-"+t.getDate());
		  var last_date = $("#cu_date").attr("last_date") == ''?today:$("#cu_date").attr("last_date");
		  var begin_date=new Date()>=last_date?'':last_date;
		  if(new Date(update)<=new Date(today)||new Date(update)<=new Date(last_date)){
			  alert("请选择合适的更新日期！！")
			  return;
		  }
		  $.post("/WebAdmin/biz/updateCu",	{
			  									id:id,begin_date:begin_date,end_date:update
			  									},function(data){
	      		if(data){
	      			$.scojs_message('操作成功', $.scojs_message.TYPE_OK);
	      		}else{
	      			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
	      		}
	      	});
		  
	  })
	  
      $(".huiBtn").click(function(){
      	var self = $(this);
      	self.attr("disabled",true);
      	var user_id = $("#user_id").val();
      	var status = self.val();
      	$.post("/WebAdmin/biz/updateHui",{id:user_id,status:status},function(data){
      		if(data){
      			$.scojs_message('操作成功', $.scojs_message.TYPE_OK);
      			$('.huiBtn').attr("disabled",false);
      			self.attr("disabled",true);
      			$('#hui_status').text(status);
      			
      		}else{
      			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
      			self.attr("disabled",false);
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
});