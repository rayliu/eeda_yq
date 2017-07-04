define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '托运工作单列表   | '+document.title;
  	$('#breadcrumb_li').text('托运工作单列表');
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
              { "data": "ORDER_NO", 
                  "render": function ( data, type, full, meta ) {
                      return "<a href='/transJobOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
                  }
              },
              { "data": "CUSTOMER_NAME"},
              { "data": "TYPE",
                  "render": function ( data, type, full, meta ) {
	                    if(!data)
	                    	data='';
	                    return data;
                  }
              },
              { "data": "CONTAINER_NO"}, 
              { "data": "SO_NO"}, 
              { "data": "CABINET_DATE", 
            	  "render": function(data){
            		  if(data)
            			  return data.substr(0,10);
            		  return '';
            	  }
              },
              { "data": "CABINET_TYPE"},
              { "data": "HEAD_CARRIER_NAME"}, 
              { "data": "YUNFEI",
                  "render": function ( data, type, full, meta ) {
                    if(data)
                      return eeda.numFormat(parseFloat(data).toFixed(2),3)
                    else
                      return '';
                    }
              },
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
              { "data": "CREATOR_NAME"},
              { "data": "CREATE_STAMP"}, 
              { "data": "STATUS"}
              
          ]
      });
      

      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
          var order_no = $.trim($("#order_no").val()); 
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          var cabinet_date_begin_time = $("#cabinet_date_begin_time").val();
          var cabinet_date_end_time = $("#cabinet_date_end_time").val();
          var status = $('#status').val();
          var customer_code = $("#customer_code").val().trim();
          var customer_name = $("#customer_name").val().trim();
          var cabinet_type=$("#cabinet_type").val().trim();
          var container_no= $.trim($("#container_no").val());
          //增加出口日期查询
          var url = "/transJobOrder/list?order_no="+order_no
          	   +"&status="+status
          	   +"&customer_code_like="+customer_code
               +"&customer_name_like="+customer_name
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date
          	   +"&cabinet_date_begin_time="+cabinet_date_begin_time
          	   +"&cabinet_date_end_time="+cabinet_date_end_time
          	   +"&cabinet_type="+cabinet_type
          	   +"&container_no="+container_no;
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
      
  });
});