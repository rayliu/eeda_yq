define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {

      
    $('.complex_search').click(function(event) {
        if($('.search_single').is(':visible')){
          $('.search_single').hide();
        }else{
          $('.search_single').show();
        }
    });
  	  if(type!=""){
  		  $('#menu_todo_list').addClass('active').find('ul').addClass('in');
  		  $('#menu_order').removeClass('active').find('ul').removeClass('in');
  	  }else{
  		$('#menu_todo_list').removeClass('active').find('ul').removeClass('in');
		  $('#menu_order').addClass('active').find('ul').addClass('in');
  	  }
  	  if(type!=""){
  		$('#orderTabs').css('display','none');
  	  }
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
          colReorder: true,
          paging: true,
          serverSide: true, //不打开会出现排序不对
          ajax: "/bookingOrder/list?type="+type,
          columns: [
              { "width": "30px",
                  "render": function ( data, type, full, meta ) {
                    return '<button type="button" class="btn table_btn delete_btn btn-xs" disabled>'+
                          '<i class="fa fa-trash-o"></i> 删除</button>';
                  }
              },
              
              { "data": "BOOKING_NO", 
                  "render": function ( data, type, full, meta ) {
                    var str='';
                    if(full.NEW_COUNT>0){
                      str='<span class="badge" style="background-color:white;color:red;margin-left:5px;">新</span>';
                    }
                    return "<a href='/bookingOrder/edit?id="+full.ID+"'target='_blank' >"+data+str+"</a>";
                  }
              },
              { "data": "ORDER_STATUS"},
              { "data": "TO_DO"}, 
              { "data": "TYPE",
                  "render": function ( data, type, full, meta ) {
	                    if(!data)
	                    	data='';
	                    return data;
                  }
              }, 
              { "data": "ORDER_EXPORT_DATE", 
            	  render: function(data){
            		  if(data)
            			  return data;
            		  return '';
            	  }
              }, 
              { "data": "SP_NAME"}, 
              { "data": "CREATOR_NAME"}, 
              { "data": "CREATE_STAMP"}, 
              { "data": "STATUS"},
              
          ]
      });

      //简单查询下拉列表控制
      $("#selected_field").change(function(event){
    	  var selected_field = $("#selected_field").val();
    	  if(selected_field == "booking_no"||selected_field == "creator_name"||selected_field == "sp_name"){
    		  $("#public_text").val("");
    		  $("#single_sp_name").hide();
    		  $("#single_create_stamp").hide();
    		  $("#single_sent_out_time").hide();
    		  $("#public_text").show();
    	  }
    	 /* if(selected_field == "sp_name"){
    		  $("#sp_name_k_input").val("");
    		  $("#public_text").hide();
    		  $("#single_create_stamp").hide();
    		  $("#single_sent_out_time").hide();
    		  $("#single_sp_name").show();
    	  }*/
    	  if(selected_field == "create_stamp"){
    		  $("#public_text").hide();
    		  $("#single_sent_out_time").hide();
    		  $("#single_sp_name").hide();
    		  $("#single_create_stamp").show();
    	  }
    	  if(selected_field == "sent_out_time"){
    		  $("#public_text").hide();
    		  $("#single_sp_name").hide();
    		  $("#single_create_stamp").hide();
    		  $("#single_sent_out_time").show();
    	  }
      });
      //简单查询按钮
      $('#singleSearchBtn').click(function(){
    	  $("#orderForm")[0].reset();
          var selectField = $('#selected_field').val();
          if(selectField == "booking_no"||selectField == "creator_name"){
            $("#"+selectField).val($('#public_text').val());
          }
          if(selectField == 'sp_name'){
        	$("#sp_name").val($('#sp_name_k').val());
            $("#sp_name_input").val($('#public_text').val());
          }
          if(selectField == 'create_stamp'){
        	$('#create_stamp_begin_time').val($('#create_stamp_k_begin_time').val());
        	$('#create_stamp_end_time').val($('#create_stamp_k_end_time').val());
          }
          if(selectField == 'sent_out_time'){
          	$('#sent_out_time_begin_time').val($('#sent_out_time_k_begin_time').val());
          	$('#sent_out_time_end_time').val($('#sent_out_time_k_end_time').val());
            }
          $('#searchBtn').click();
      });
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });
      
      $('#searchBtn').click(function(){
    	  var orderStatus = $("#orderTabs li.active").text().trim();
    	  if(orderStatus=='全部'){
    		  orderStatus = "";
    	  }
          searchData(orderStatus); 
      })
      
     var searchData=function(order_status){
          var booking_no = $.trim($("#booking_no").val()); 
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          var sent_out_time_begin_time = $("#sent_out_time_begin_time").val();
          var sent_out_time_end_time = $("#sent_out_time_end_time").val();
          var creator_name = $("#creator_name").val();
          var sp_name = $("#sp_name_input").val();
          //var status = $('#status').val();
          //var customer_code = $("#customer_code").val().trim();
          //var customer_name = $("#customer_name_input").val().trim();
          var order_status = order_status;
          
          //增加出口日期查询
          var url = "/bookingOrder/list?booking_no="+booking_no
          	   //+"&status="+status
          	   +"&order_status="+order_status
          	   //+"&customer_code_like="+customer_code
               //+"&customer_name_like="+customer_name
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date
          	   +"&order_export_date_begin_time="+sent_out_time_begin_time
          	   +"&order_export_date_end_time="+sent_out_time_end_time
          	   +"&creator_name="+creator_name
          	   +"&sp_name="+sp_name;

          dataTable.ajax.url(url).load();
      };
      
      $('#orderTabs a').click(function(){
    	  var value = $(this).text();
    	  if(value=='全部'){
    		  value = "";
    	  }
    	  searchData(value);
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
          $.post('/bookingOrder/deleteOrder', {id:id,delete_reason:deleteReason}, function(data){
        	  $('#deleteReasonDetail .return').click();
        	  tr.hide();
        	  $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
          },'json').fail(function() {
              $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
          });
      });
      
      $('#collapseDocInfo').on('show.bs.collapse', function () {
          $('#collapseDocIcon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
        });
      $('#collapseDocInfo').on('hide.bs.collapse', function () {
          $('#collapseDocIcon').removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
        });
      
  });
});