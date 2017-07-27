define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
	  $('#AllCheck').attr('disabled',true);
      var dataTable = eeda.dt({
      id: 'eeda_table',
      serverSide: false, //不打开会出现排序不对 
      ajax: '/outputScale/list?export_flag='+$("#export_flag").val(),
      columns:[
              { "width": "10px",
				 "render": function ( data, type, full, meta ) {
					       if(full.EXPORT_FLAG != 'Y')
					    	   return '<input type="checkbox" class="checkBox" disabled>';
					       else 
					    	   return '<input type="checkbox" disabled>';
				 			}
              },
              { "data": "ORDER_NO", "width": "80px",
		    	  "render": function ( data, type, full, meta ) {
                    return "<a href='/transJobOrder/edit?id="+full.TJOID+"'target='_blank'>"+data+"</a>";
                }
              },
              { "data": "LADING_NO", "width": "60px"},			
              { "data": "C_DATE", "width": "80px"},
              { "data": "CHARGE_TIME", "width": "120px"},
              { "data": "CUSTOMER_NAME", "width": "80px"},
              { "data": "TYPE", "width": "60px"},
              { "data": "EXPORT_FLAG", "width": "60px",
				"render": function ( data, type, full, meta ) {
					if(data != 'Y')
						return '未导出';
					else 
						return '已导出';
				}
              },
              { "data": "COMBINE_WHARF", "width": "150px"},
              { "data": "CONTAINER_NO", "width": "80px"},
              { "data": "CABINET_TYPE", "width": "40px"},
              { "data": "COMBINE_UNLOAD_TYPE", "width": "80px"},
              { "data": "COMBINE_CAR_NO", "width": "70px"},
              { "data": "null", "width": "40px"},
              { "data": "FREIGHT","width":"60px",
                "render": function ( data, type, full, meta ) {
                  if(data)
                    return eeda.numFormat(parseFloat(data).toFixed(2),3)
                  else
                    return '';
                  }
              },
              { "data": "REMARK", "width": "200px"},
              { "data": "CREATE_STAMP","width": "80px"}
              ]
     	});

      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });
      
      $('#searchBtn').click(function(){
          searchData();
          var driver = $("#driver").val().trim();
          var car_no = $("#car_id_input").val().trim();
          var export_flag=$("#export_flag").val();
          if((driver == null || driver == "") && (car_no == null || car_no == "") || (export_flag == "Y")){
        	  $('#AllCheck').attr('disabled',true);
        	  $('#eeda_table input[type="checkbox"]').each(function(){
        		  $(this).attr('disabled',true);
  			  });
         	 return;
          }else{
        	   allCheck();
               click_checkbox();
          }
      })

     var searchData=function(){
          var order_no = $.trim($("#order_no").val()); 
          var driver = $("#driver").val().trim();
          var export_flag=$("#export_flag").val();
          var c_date_begin_time = $("#c_date_begin_time").val();
          var c_date_end_time = $("#c_date_end_time").val();
          var customer = $("#customer").val();
          var customer_name = $("#customer_input").val().trim(); 
//        var sp = $("#sp").val(); 
          var car_id = $("#car_id").val();
          var car_no = $("#car_id_input").val().trim();
          var start_date = $("#charge_time_begin_time").val();
          var end_date = $("#charge_time_end_time").val();
          var audit_flag = $("#audit_flag").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/outputScale/list?order_no="+order_no
          			   +"&c_date_begin_time="+c_date_begin_time
          			   +"&c_date_end_time="+c_date_end_time
			           +"&customer="+customer
			           +"&customer_name="+customer_name
//			           +"&sp_id="+sp
                       +"&car_id="+car_id
                       +"&car_no="+car_no
                       +"&driver_equals="+driver
                       +"&export_flag="+export_flag
		               +"&charge_time_begin_time="+start_date
		               +"&charge_time_end_time="+end_date;

          dataTable.ajax.url(url).load(enable_checkbox);
      };
      
      //全选 
      var allCheck = function(){
    	  $('#AllCheck').attr('disabled',false);
    	  $('#AllCheck').click(function(){
  	      	$(".checkBox").prop("checked",this.checked);
  	      	if($('#AllCheck').prop('checked')){
          		$('#export_outputTable').attr('disabled',false);
          	}else{
          		$('#export_outputTable').attr('disabled',true);
          	}
        });
      }
      $("#eeda_table").on('click','.checkBox',function(){
		    $("#AllCheck").prop("checked",$(".checkBox").length == $(".checkBox:checked").length ? true : false);
      });

      //checkbox选中则button可点击
	 var click_checkbox = function(){
		 $('#eeda_table').on('click','.checkBox',function(){
			 var hava_check = 0;
			 $('#eeda_table input[type="checkbox"]').each(function(){	
				 var checkbox = $(this).prop('checked');
				 if(checkbox){
					 hava_check = 1;
	    		 }	
			 })
			 if(hava_check>0){
				 $('#export_outputTable').attr('disabled',false);
			 }else{
				 $('#export_outputTable').attr('disabled',true);
			 }
		 });
	  }
	 
	 //循环遍历table启用checkbox
	 var enable_checkbox = function(){
		 var driver = $("#driver").val().trim();
         var car_no = $("#car_id_input").val().trim();
         var export_flag = $("#export_flag").val();
         if((driver == null || driver == "") && (car_no == null || car_no == "") || (export_flag == "Y")){
        	 $('#export_outputTable').attr('disabled',true);
        	 $('#AllCheck').attr('disabled',true);
        	 $('#AllCheck').attr('checked',false)
        	 return;
         }else{	   
        	 $('#export_outputTable').attr('disabled',false);
        	 $('#AllCheck').prop('checked',true);
			 $('#eeda_table input[type="checkbox"]').each(function(){
				 $(this).attr('disabled',false);
	        	 $(this).attr('checked',true);
			 });
         }
      }
	 
       //导出产值表
       $('#export_outputTable').click(function(){
    	   $('#export_outputTable').attr('disabled',true);
           var car_no = $('#car_id_input').val().trim();
           var driver = $('#driver').val().trim();
           var itemIds=[];
           $('#eeda_table input[type="checkbox"]').each(function(){
        	   var checkbox = $(this).prop('checked');
        	   if(checkbox){
        		   var itemId = $(this).parent().parent().attr('id');
        		   if(itemId!=undefined){
        			   itemIds.push(itemId);
        		   }
        	   }
           });
           if(car_no||driver){
        	   if(car_no||driver||itemIds){
        		   var order_id = $('#order_id').val();
        		   var company_name = $('#company_name').val();
	               $.post('/outputScale/downloadList?itemIds='+itemIds,{car_no:car_no,driver:driver},function(data){
	            	   if(data){
	            		   window.open(data);
	            		   $.scojs_message('生成产值表PDF成功', $.scojs_message.TYPE_OK);
	            		   refresh();
	            		   $('#AllCheck').attr('disabled',true);
	            	   }else{
	            		   $.scojs_message('生成产值表PDF失败',$.scojs_message.TYPE_ERROR);
	            	   }
	               });
        	   }else{
	        			$.scojs_message('请填上车牌或者司机', $.scojs_message.TYPE_ERROR);
	        		}
	        }else{
		    		$.scojs_message('结算车牌或者司机不能为空', $.scojs_message.TYPE_ERROR);
		    		$('#export_outputTable').attr('disabled',false);
		    		return;
        		 }
        });
        
      //刷新明细表
        var refresh = function(order_id){
        	var url = "/outputScale/list?export_flag="+$('#export_flag').val();
        	dataTable.ajax.url(url).load(function(){
          });
        }
      
  });
});