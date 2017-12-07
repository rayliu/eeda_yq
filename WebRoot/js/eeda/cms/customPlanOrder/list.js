define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','dtColReorder','validate_cn', 'sco'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '报关申请单列表 | '+document.title;

    	if(type != ""){
    		$('#menu_order').removeClass('active').find('ul').removeClass('in');
            $('#menu_todo_list').addClass('active').find('ul').addClass('in');
          }
    	else{
        	$('#menu_order').addClass('active').find('ul').addClass('in');
            $('#menu_todo_list').removeClass('active').find('ul').removeClass('in');
    	 }
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/customPlanOrder/list?confirmFee=unConfirmFee",
            "drawCallback": function( settings ) {
                $('.other').popover({
                    html: true,
                    container: 'body',
                    placement: 'right',
                    trigger: 'hover'
                });
  		  },
  		initComplete:function(settings){
            hideColumn();
            },
            columns:[
					{ "width": "30px",
					    "render": function ( data, type, full, meta ) {
					    	var to_office_id=full.TO_OFFICE_ID;
					    	if(to_office_id){
					    		return '<button type="button" class="btn table_btn delete btn-xs" disabled>'+
					            '<i class="fa fa-trash-o"></i> 删除</button>';
					    	}
					    		return '<button type="button" class="btn table_btn delete btn-xs" >'+
				            '<i class="fa fa-trash-o"></i> 删除</button>';
					    }
				   },
                  {"data": "ORDER_NO", 
                	  "render": function ( data, type, full, meta ) {
                		  var other = '';
                    	  if(full.OTHER_FLAG=='other'){
                    		  other = ' <span class="badge">外</span>';
                    	  }
                		  return "<a href='/customPlanOrder/edit?id="+full.ID+"'target='_blank'>"+data+other+"</a>";
                	  }
                  },
                  { "data": "TRACKING_NO"},
                  { "data": "CUSTOMS_BILLCODE"},
                  { "data": "STATUS", 
  	            	"render": function(data, type, full, meta){
  	            		$("#waitAuditing").text(full.WAITAUDITING);
  	            		if(data=="审核不通过"){
  	            			return "<span style='color:red'>"+data+"</span>";
  	            		}else{
  	            			return data;
  	            		}	            		
  	            	}  
  	              },
  	              { "data": "CUSTOM_STATE", 
  	            	"render": function(data, type, full, meta){
  	            		$("#release").text(full.PASS);
  	            		$("#checked").text(full.CHECKED);
  	            		$("#handing").text(full.HANDLING);
  	            		$("#abnormal").text(full.ABNORMAL);
  	            		if(data=="异常"){
  	            			return "<span style='color:red'>"+data+"</span>";
  	            		}else if(data=="异常待处理"||data=="查验"){
  	            			return "<span style='color:#f7b314'>"+data+"</span>";
  	            		}else if(data=="放行"){
  	            			return "<span style='color:green'>"+data+"</span>";
  	            		}else{
  	            			return data;
  	            		}	            		
  	            	}  
  	              },
	              { "data": "TYPE"}, 
	              { "data": "RECEIVE_COMPANY_NAME"}, 
	              { "data": null,
	                    "render": function ( data, type, full, meta ) {
	                      if(data){
	                        data = '';
	                      }
	                      
	                      var cost = full.COST;
	                      var charge = full.CHARGE;
	                      var costShow="";
	                      var chargeShow="";
	                      if(cost){
	                        var costArray = cost.split(',');
	                        costShow='<h5><strong>应付费用</strong></h5>';
	                        for (var i = 0; i < costArray.length; i++) {
	                          costShow += '<li>'+costArray[i]+'</li>';
	                }
	                      }
	                      if(charge){
	                        chargeShow='<h5><strong>应收费用</strong></h5>';
	                        var chargeArray = charge.split(',');
	                        for (var i = 0; i < chargeArray.length; i++) {
	                          chargeShow += '<li>'+chargeArray[i]+'</li>';
	                  
	                }
	                      }
	                      if(cost){
	                        data += '<span class="other" width="50" '
	                            +' data-content="<div'
	                            +' height=&quot;140&quot; >'+costShow+'</div>" ><span class="badge" style="">￥付</span></span>';
	                      }
	                      if(charge){
	                        data += ' <span class="other" width="50" '
	                                +' data-content="<div'
	                                +' height=&quot;140&quot; >'+chargeShow+'</div>" ><span class="badge" style="">￥收</span></span>';
	                      }
	
	                      return data;
	                    }
	              },
                  { "data": "DATE_CUSTOM",
                	"render":function(data,type,full,meta){
                		if(data){
                			return data.substring(0,10);
                		}
                		return '';
                	}
                  }, 
	              { "data": "CREATOR_NAME"}, 
	              { "data": "CREATE_STAMP",
	            	  "render": function ( data, type, full, meta ) {
	            		  if(data)
	            			  return data.substring(0, 10);
	            		  else
	            			  return '';
                	  }
	              },
	              { "data": "LOCK_BILL_STATUS"}
            ]
        });

        //base on config hide cols
        dataTable.columns().eq(0).each( function(index) {
            var column = dataTable.column(index);
            $.each(cols_config, function(index, el) {
                
                if(column.dataSrc() == el.COL_FIELD){
                  
                  if(el.IS_SHOW == 'N'){
                    column.visible(false, false);
                  }else{
                    column.visible(true, false);
                  }
                }
            });
        });
        
        $('.complex_search').click(function(event) {
            if($('.search_single').is(':visible')){
              $('.search_single').hide();
            }else{
              $('.search_single').show();
            }
        });
      //简单查询
        $('#selected_field').change(function(event) {
  	      var selectField = $('#selected_field').val();
  	      if(selectField=='receive_company_name'){
  	    	  $("#customer_name_show").val("");
  	    	  $("#single_order_no").hide();
  	    	  $("#single_booking_no").hide();
  	    	  $("#single_status").hide();
  	    	  $("#single_lock_bill_status").hide();
  	    	  $("#single_custom_state").hide();
  	    	  $("#single_type").hide();
  	    	  $("#public_time_show").hide();
  	    	  $("#customer_name_show").show();
  	      }
  	      if(selectField=='order_no'){
  	    	  $("#single_order_no").val("");
  	    	  $("#single_order_no").show();
	    	  $("#single_booking_no").hide();
	    	  $("#single_status").hide();
	    	  $("#single_lock_bill_status").hide();
	    	  $("#single_custom_state").hide();
	    	  $("#single_type").hide();
	    	  $("#public_time_show").hide();
	    	  $("#customer_name_show").hide();
	    	  $("#single_order_no").show();
  	      }
  	      if(selectField=="booking_no_equals"){
  	    	  $("#single_booking_no").val("");
  	    	  $("#single_order_no").hide();
	    	  $("#single_status").hide();
	    	  $("#single_lock_bill_status").hide();
	    	  $("#single_custom_state").hide();
	    	  $("#single_type").hide();
	    	  $("#public_time_show").hide();
	    	  $("#customer_name_show").hide();
	    	  $("#single_booking_no").show();
  	      }
  	      if(selectField=="status"){
  	    	  $("#single_status").val("");
  	    	  $("#single_lock_bill_status").hide();
  	    	  $("#single_order_no").hide();
	    	  $("#single_booking_no").hide();
	    	  $("#single_custom_state").hide();
	    	  $("#single_type").hide();
	    	  $("#public_time_show").hide();
	    	  $("#customer_name_show").hide();
	    	  $("#single_status").show();
  	      }
  	    if(selectField=="lock_bill_status"){
	    	  $("#single_status").val("");
	    	  $("#single_order_no").hide();
	    	  $("#single_booking_no").hide();
	    	  $("#single_custom_state").hide();
	    	  $("#single_type").hide();
	    	  $("#public_time_show").hide();
	    	  $("#customer_name_show").hide();
	    	  $("#single_status").hide();
	    	  $("#single_lock_bill_status").show();
	      }
  	      if(selectField=="custom_state_equals"){
  	    	  $("#single_custom_state").val("");
  	    	  $("#single_order_no").hide();
	    	  $("#single_booking_no").hide();
	    	  $("#single_status").hide();
	    	  $("#single_lock_bill_status").hide();
	    	  $("#single_type").hide();
	    	  $("#public_time_show").hide();
	    	  $("#customer_name_show").hide();
	    	  $("#single_custom_state").show();
	      }
  	      if(selectField=="type_equals"){
  	    	  $("#single_type").val("")
  	    	  $("#single_order_no").hide();
	    	  $("#single_booking_no").hide();
	    	  $("#single_status").hide();
	    	  $("#single_lock_bill_status").hide();
	    	  $("#single_custom_state").hide();
	    	  $("#public_time_show").hide();
	    	  $("#customer_name_show").hide();
	    	  $("#single_type").show();
	      }
  	      if(selectField=="create_stamp"||selectField=="date_custom"){
  	    	  $("#public_time_begin_time").val("");
  	    	  $("#public_time_end_time").val("");
  	    	  $("#single_order_no").hide();
	    	  $("#single_booking_no").hide();
	    	  $("#single_status").hide();
	    	  $("#single_lock_bill_status").hide();
	    	  $("#single_custom_state").hide();
	    	  $("#single_type").hide();
	    	  $("#customer_name_show").hide();
	    	  $("#public_time_show").show();
	      }
       });
  	
	  $('#singleSearchBtn').click(function(){
		  $("#orderForm")[0].reset();
	  	  var orderStatus = $("#orderTabs li.active a").attr("name").trim();
	  	  singleSearchData(orderStatus);
	  });
      var singleSearchData = function(paraStr){
    	 var selectField = $('#selected_field').val();
   	     var selectFieldValue = '';
 	     if(selectField=='receive_company_name'){
 	    	 selectFieldValue = $("#single_customer_name_input").val();
 	    	 selectFieldValue +="&customer="+$("#single_customer_name").val();
 	    	 $("#customer_input").val($("#single_customer_name_input").val());
 	     }
 	     if(selectField=='order_no'){
 	    	 selectFieldValue = $("#single_order_no").val();
 	    	$("#order_no").val($("#single_order_no").val());
 	     }
 	     if(selectField=="booking_no_equals"){
 	    	 selectFieldValue = $("#single_booking_no").val();
 	    	$("#booking_no").val($("#single_booking_no").val());
 	     }
 	     if(selectField=="status"){
 	    	 selectFieldValue = $("#single_status").val();
 	    	 $("#status").val($("#single_status").val());
 	     }
 	     if(selectField=="lock_bill_status"){
	    	 selectFieldValue = $("#single_lock_bill_status").val();
	    	 $("#lock_bill_status").val($("#single_lock_bill_status").val());
	     }
 	     if(selectField=="custom_state_equals"){
 	    	 selectFieldValue = $("#single_custom_state").val();
 	    	 $("#custom_state").val($("#single_custom_state").val());
 	     }
 	     if(selectField=="type_equals"){
 	    	  selectFieldValue = $("#single_type").val();
 	    	 $("#type").val($("#single_type").val());
 	     }
 	     if(selectField=="date_custom"){
 	    	 var custom_start_date = $("#public_time_begin_time").val();
 	    	 var custom_end_date = $("#public_time_end_time").val();
 	    	 $("#date_custom_begin_time").val($("#public_time_begin_time").val());
 	    	 $("#date_custom_end_time").val($("#public_time_end_time").val());
 	     }
 	     if(selectField=="create_stamp"){
 	    	 var start_date = $("#public_time_begin_time").val();
 	    	 var end_date = $("#public_time_end_time").val();
 	    	 $("#create_stamp_begin_time").val($("#public_time_begin_time").val());
 	    	 $("#create_stamp_end_time").val($("#public_time_end_time").val());
 	     }
 	     var confirmFee = "";
 	     if(paraStr=="未完成费用确认"){
        	  confirmFee ="unConfirmFee";
          }else if(paraStr=="已完成费用确认"){
        	  confirmFee="confirmFee"
          }else{
        	  if(paraStr=="待审核"&&paraStr!=undefined){
            	  status = paraStr;
              }else{
            	  
    	    	  if(paraStr!='全部'&&paraStr!=undefined){
    	    		  custom_state=paraStr;
    	    	  }
              }
          }
   	     var url = "/customPlanOrder/list?"+selectField+"="+selectFieldValue
 	       +"&create_stamp_begin_time="+start_date
 	       +"&create_stamp_end_time="+end_date
 	       +"&date_custom_begin_time="+custom_start_date
 	       +"&date_custom_end_time="+custom_end_date
 	       +"&confirmFee="+confirmFee;
   	     dataTable.ajax.url(url).load();
      }
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
    	  var orderStatus = $("#orderTabs li.active a").attr("name").trim();
          searchData(orderStatus); 
      })

     var searchData=function(paraStr){
          var order_no = $.trim($("#order_no").val());
          var customer_name = $('#customer_input').val();
          var customer = $('#customer').val();
          var status = $('#status').val();
          var lock_bill_status = $("#lock_bill_status").val();
          var custom_state = $('#custom_state').val();
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          var custom_start_date = $("#date_custom_begin_time").val();
          var custom_end_date = $("#date_custom_end_time").val();
          var booking_no = $("#booking_no").val().trim();
          var type = $("#type").val();
          var confirmFee = "";
          if(paraStr=="未完成费用确认"){
        	  confirmFee ="unConfirmFee";
          }else if(paraStr=="已完成费用确认"){
        	  confirmFee="confirmFee"
          }else{
        	  if(paraStr=="待审核"&&paraStr!=undefined){
            	  status = paraStr;
              }else{
            	  
    	    	  if(paraStr!='全部'&&paraStr!=undefined){
    	    		  custom_state=paraStr;
    	    	  }
              }
          }
          
          
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/customPlanOrder/list?order_no="+order_no
               +"&status_equals="+status
               +"&lock_bill_status_equals="+lock_bill_status
               +"&booking_no_equals="+booking_no
               +"&custom_state_equals="+custom_state
               +"&receive_company_name="+customer_name
               +"&customer="+customer
               +"&type_equals="+type
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date
               +"&date_custom_begin_time="+custom_start_date
               +"&date_custom_end_time="+custom_end_date
               +"&date_custom_end_time="+custom_end_date
               +"&confirmFee="+confirmFee;
          dataTable.ajax.url(url).load();
      };
      
      $('#orderTabs a').click(function(){
    	  var custom_state = $(this).attr("name");    	  
        	  searchData(custom_state);
      });

      $("#eeda-table").on('click', '.delete', function(){
        var tr = $(this).parent().parent();
          var id = tr.attr('id');
        $('#delete_id').val(id);
        $('#deleteReasonDetailAlert').click();
      })
      $("#deleteReasonDetail").on('click', '.deleteReason', function(){
        $('#deleteReason').val($(this).val());
      })
       $("#deleteReasonDetail").on('click', '.confirm', function(){
         if(!$("#deleteReasonDetailForm").valid()){
               return;
           }
         var id = $('#delete_id').val();
           var deleteReason = $('#deleteReason').val();
           var tr = $('#'+id+'');
          $.post('/customPlanOrder/deleteOrder', {id:id,delete_reason:deleteReason}, function(data){
            $('#deleteReasonDetail .return').click();
            tr.hide();
            $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
          },'json').fail(function() {
              $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
          });
      });
      var hideColumn = function(){}
      if(!$("#jurisdiction").val()){
		  var hideColumn = function(){         	
		       	//隐藏对usd和jpy列
		    	  var dataTable = $('#eeda-table').dataTable();
		        	dataTable.fnSetColumnVis(8, false);
		  }
      }
    });
});