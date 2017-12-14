define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn','./import', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
	  $('.search_single input,.search_single select').on('input',function(){
		  $("#orderForm")[0].reset();
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
          sort:true,
          paging: true,
          serverSide: true, //不打开会出现排序不对
          ajax: "/transJobOrder/list?type="+type,
          "drawCallback": function( settings ) {
              $('.other').popover({
                  html: true,
                  container: 'body',
                  placement: 'right',
                  trigger: 'hover'
              });
		  },
          columns: [
              { "width": "10px",
                  "render": function ( data, type, full, meta ) {
                    return '<button type="button" class="btn table_btn delete btn-xs" >'+
                          '<i class="fa fa-trash-o"></i> 删除</button>';
                  }
              },
              { "data": "ORDER_NO",  "width": "80px",
                  "render": function ( data, type, full, meta ) {
                	  var other = '';
                	  var updateApply = '';
                	  if(full.FROM_ORDER_ID!=undefined){
                		  other = '<img src="/images/order_from_outside.png" style="height:15px;" title="Outside Order">';
                	  }
                	  if(full.UPDATE_APPLY_FLAG=='Y'){
                		  updateApply = '&nbsp<span style="color:red;">修改申请<span>';
                	  }
                      return "<a href='/transJobOrder/edit?id="+full.ID+"'target='_blank'>"+data+other+updateApply+"</a>";
                  }
              },
              { "data": "CUSTOMER_NAME","width":"60px"},
              { "data": "TYPE","width":"40px",
                  "render": function ( data, type, full, meta ) {
	                    if(!data)
	                    	data='';
	                    return data;
                  }
              },
              { "data": "COMBINE_WHARF","width":"60px"},
              { "data": "CONTAINER_NO","width":"60px"}, 
              { "data": "SO_NO","width":"60px"}, 
              { "data": "CABINET_DATE", "width":"80px",//提柜、提货时间
            	  "render": function(data){
            		  if(data)
            			  return data;
            		  return '';
            	  }
              },
              { "data": "CHARGE_TIME","width":"60px"},
              { "data": "CABINET_TYPE","width":"30px",
            	  "render":function(data, type, full, meta ) {
            		  if(!data){
            			  return  data="";
            		  }else{
            			  if(full.CABINET_TYPE==full.TRUCK_TYPE){
                			  return data;
                		  }else{
                			  data="<span style='color:red;'>"+full.CABINET_TYPE+"</span>";
                		  }
                		  return data; 
            		  }            		  
            	  }
              },
              { "data": "HEAD_CARRIER_NAME","width":"60px"}, 
              { "data": "YUNFEI","width":"35px",
                  "render": function ( data, type, full, meta ) {
                    if(data)
                      return eeda.numFormat(parseFloat(data).toFixed(2),3)
                    else
                      return '';
                    }
              },
              { "data": null,"width":"45px",
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
              { "data": "CREATOR_NAME","width":"40px"},
              { "data": "CREATE_STAMP","width":"60px"}, 
              { "data": "STATUS","width":"30px"}
              
          ]
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
	      if(selectField=='order_no'||selectField=='container_no'||selectField=='creator_name'){
	    	  $("#public_text").val("");
	    	  $("#single_customer").hide();
	    	  $("#single_status").hide();
	    	  $("#single_cabinet_type").hide();
	    	  $("#public_time").hide();
	    	  $("#car_id_show").hide();
	    	  $("#take_wharf_show").hide();
	    	  $("#public_text").show();
	      }
	      if(selectField=='customer_name_like'){
	    	  $("#single_status").hide();
	    	  $("#single_cabinet_type").hide();
	    	  $("#public_time").hide();
	    	  $("#public_text").hide();
	    	  $("#car_id_show").hide();
	    	  $("#take_wharf_show").hide();
	    	  $("#single_customer").show();
	      }
	      if(selectField=='car_id'){
	    	  $("#single_status").hide();
	    	  $("#single_cabinet_type").hide();
	    	  $("#public_time").hide();
	    	  $("#public_text").hide();
	    	  $("#single_customer").hide();
	    	  $("#take_wharf_show").hide();
	    	  $("#car_id_show").show();
	      }
	      if(selectField=='cabinet_type'){
	    	  $("#single_status").hide();
	    	  $("#public_time").hide();
	    	  $("#public_text").hide();
	    	  $("#single_customer").hide();
	    	  $("#car_id_show").hide();
	    	  $("#take_wharf_show").hide();
	    	  $("#single_cabinet_type").show();
	      }
	      if(selectField=='status'){
	    	  $("#public_time").hide();
	    	  $("#public_text").hide();
	    	  $("#single_customer").hide();
	    	  $("#single_cabinet_type").hide();
	    	  $("#car_id_show").hide();
	    	  $("#take_wharf_show").hide();
	    	  $("#single_status").show();
	      }
	      if(selectField=='cabinet_date'){
	    	  $("#public_text").hide();
	    	  $("#single_customer").hide();
	    	  $("#single_cabinet_type").hide();
	    	  $("#single_status").hide();
	    	  $("#car_id_show").hide();
	    	  $("#take_wharf_show").hide();
	    	  $("#public_time").show();
	      }
	      if(selectField=='charge_time'){
	    	  $("#public_text").hide();
	    	  $("#single_customer").hide();
	    	  $("#single_cabinet_type").hide();
	    	  $("#single_status").hide();
	    	  $("#car_id_show").hide();
	    	  $("#take_wharf_show").hide();
	    	  $("#public_time").show();
	      }
	      if(selectField=='create_time'){
	    	  $("#public_text").hide();
	    	  $("#single_customer").hide();
	    	  $("#single_cabinet_type").hide();
	    	  $("#single_status").hide();
	    	  $("#car_id_show").hide();
	    	  $("#take_wharf_show").hide();
	    	  $("#public_time").show();
	      }
	      if(selectField=='take_wharf'){
	    	  $("#public_text").hide();
	    	  $("#single_customer").hide();
	    	  $("#single_cabinet_type").hide();
	    	  $("#single_status").hide();
	    	  $("#car_id_show").hide();
	    	  $("#public_time").hide();
	    	  $("#take_wharf_show").show();
	      }
     });
      
      $("#singleSearchBtn").click(function(){
    	  $("#orderForm")[0].reset();
    	  var selectField = $('#selected_field').val();
	      if(selectField=='order_no'||selectField=='container_no'||selectField=='creator_name'){
	    	  $("#"+selectField).val($("#public_text").val());
	      }
	      if(selectField=='customer_name_like'){
	    	  $("#customer_name").val($("#single_customer_name").val());
	    	  $("#customer_name_input").val($("#single_customer_name_input").val());
	      }
	      if(selectField=='car_id'){
	    	  $("#car_id").val($("#single_car_id").val());
	    	  $("#car_id_input").val($("#single_car_id_input").val());
	      }
	      if(selectField=='cabinet_type'){
	    	  $("#cabinet_type").val($("#single_cabinet_type").val());
	      }
	      if(selectField=='status'){
	    	  $("#status").val($("#single_status").val());
	      }
	      if(selectField=='take_wharf'){
	    	  $("#take_wharf").val($("#single_take_wharf").val());
	    	  $("#take_wharf_input").val($("#single_take_wharf_input").val());
	      }
	      if(selectField=='cabinet_date'){
	    	  $("#cabinet_date_begin_time").val($("#single_public_time_begin_time").val());
	    	  $("#cabinet_date_end_time").val($("#single_public_time_end_time").val());
	      }
	      if(selectField=='charge_time'){
	    	 $("#charge_time_begin_time").val($("#single_public_time_begin_time").val());
	    	 $("#charge_time_end_time").val($("#single_public_time_end_time").val());
	      }
	      if(selectField=='create_time'){
	    	  $("#create_time_begin_time").val($("#single_public_time_begin_time").val());
	    	  $("#create_time_end_time").val($("#single_public_time_end_time").val());
	      }
	      $('#searchBtn').click();
      });
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
          var order_no = $.trim($("#order_no").val()); 
          var sp_id = $("#sp_id").val(); 
          var start_date = $("#charge_time_begin_time").val();
          var end_date = $("#charge_time_end_time").val();
          var cabinet_date_begin_time = $("#cabinet_date_begin_time").val();
          var cabinet_date_end_time = $("#cabinet_date_end_time").val();
          var create_stamp_begin_time = $("#create_time_begin_time").val();
          var create_stamp_end_time = $("#create_time_end_time").val();
          var status = $('#status').val();
          var car_id = $("#car_id").val().trim();
          var customer_name = $("#customer_name_input").val().trim();
          var cabinet_type=$("#cabinet_type").val().trim();
          var container_no= $.trim($("#container_no").val());
          var creator_name= $.trim($("#creator_name").val());
          var take_wharf = $('#take_wharf').val();
          //增加出口日期查询
          var url = "/transJobOrder/list?order_no="+order_no
          	   +"&sp_id="+sp_id
          	   +"&status="+status
          	   +"&car_id="+car_id
               +"&customer_name_like="+customer_name
               +"&charge_time_begin_time="+start_date
               +"&charge_time_end_time="+end_date
          	   +"&cabinet_date_begin_time="+cabinet_date_begin_time
          	   +"&cabinet_date_end_time="+cabinet_date_end_time
          	   +"&create_stamp_begin_time="+create_stamp_begin_time
        	   +"&create_stamp_end_time="+create_stamp_end_time
          	   +"&cabinet_type="+cabinet_type
          	   +"&container_no="+container_no
          	   +"&creator_name="+creator_name
          	   +"&take_wharf_equals="+take_wharf;
          dataTable.ajax.url(url).load();
      };
      
      
      $('#oceanDetailTab').click(function(e){
    	  var transport_type = "ocean";
    	  	var url = "/jobOrder/list?transport_type_like="+transport_type;
    	  	dataTable.ajax.url(url).load();
      })
      $('#airDetailTab').click(function(e){
    	  var transport_type = "air";
    	  var url = "/jobOrder/list?transport_type_like="+transport_type;
    	  dataTable.ajax.url(url).load();
      })
      $('#landDetailTab').click(function(e){
    	  var transport_type = "land";
    	  var url = "/transJobOrder/list?transport_type_like="+transport_type;
    	  dataTable.ajax.url(url).load();
      })
      $('#customDetailTab').click(function(e){
    	  var transport_type = "custom";
    	  var url = "/jobOrder/list?transport_type_like="+transport_type;
    	  dataTable.ajax.url(url).load();
      })
      $('#allTab').click(function(e){
    	  var transport_type = "";
    	  var url = "/transJobOrder/list?transport_type_like="+transport_type;
    	  dataTable.ajax.url(url).load();
      })
      
      
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
          $.post('/transJobOrder/deleteOrder', {id:id,delete_reason:deleteReason}, function(data){
        	  $('#deleteReasonDetail .return').click();
        	  tr.hide();
        	  $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
          },'json').fail(function() {
              $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
          });
      });
      
      //导出工作单
      $('#exportJobBtn').click(function(){
    	  var condition={};
    	  condition.sp_id=$('#sp_id').val();
    	  condition.creator_name_like=$('#creator_name').val();
    	  condition.customer_name_like=$('#customer_name_input').val();
    	  condition.cabinet_date_begin_time = $('#cabinet_date_begin_time').val();
    	  condition.cabinet_date_end_time = $('#cabinet_date_end_time').val();
    	  condition.charge_time_begin_time = $('#charge_time_begin_time').val();
    	  condition.charge_time_end_time = $('#charge_time_end_time').val();
    	  
    	  
    	  $.post('/transJobOrder/exportJobList', {params:JSON.stringify(condition)}, function(data){
    		  if(data){
					window.open(data);
					$.scojs_message('导出工作单列表成功', $.scojs_message.TYPE_OK);
				}else{
					$.scojs_message('导出工作单列表失败',$.scojs_message.TYPE_ERROR);
				}
    		  $.unblockUI();
    		 });
      });
      
  });
});