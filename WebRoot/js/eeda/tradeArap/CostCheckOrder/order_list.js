define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	
  	  $('#cost_check_order_tab').click(function(){
          $('#order_table').DataTable().draw();
      });

      var dataTable = eeda.dt({
          id: 'order_table',
          paging: true,
          autoWidth: false,
          serverSide: true, 
          ajax: "/tradeCostCheckOrder/orderList",
          columns: [
            { "data": "ORDER_NO",
            	 "render": function ( data, type, full, meta ) {
           		  return "<a href='/tradeCostCheckOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
           	  }
            },
            { "data": "CREATE_STAMP"},
            { "data": "STATUS"},
            { "data": "SP_NAME"},
            { "data": "TOTAL_AMOUNT","visible":false},
            { "data": "CNY",
                "render":function(data,type,full,meta){
                  var usd_str=eeda.numFormat(parseFloat(data).toFixed(2),3);
                  return usd_str;
                }
            },
            { "data": "USD",
              "render":function(data,type,full,meta){
                var usd_str=eeda.numFormat(parseFloat(data).toFixed(2),3);
                return usd_str;
              }
            },
            { "data": "JPY",
                "render":function(data,type,full,meta){
                  var usd_str=eeda.numFormat(parseFloat(data).toFixed(2),3);
                  return usd_str;
                }
            },
            { "data": "HKD",
              "render":function(data,type,full,meta){
                var usd_str=eeda.numFormat(parseFloat(data).toFixed(2),3);
                return usd_str;
              }
            }
          ]
      });

      $('#resetOrderBtn').click(function(e){
          $("#orderSearchForm")[0].reset();
      });

      $('#searchOrderBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
    	  var order_no = $("#order_no1").val().trim(); 
          var sp = $("#sp1").val(); 
          var sp1_input = $("#sp1_input").val().trim(); 
          var start_date = $("#create_stamp1_begin_time").val();
          var end_date = $("#create_stamp1_end_time").val();
          var order_export_date_begin_time = $("#order_export_date1_begin_time").val();
          var order_export_date_end_time = $("#order_export_date1_end_time").val();
          var status = $("#status").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/tradeCostCheckOrder/orderList?order_no="+order_no
			           +"&sp_id="+sp
			           +"&sp_name_like="+sp1_input
		               +"&create_stamp_begin_time="+start_date
		               +"&create_stamp_end_time="+end_date
		               +"&order_export_date_begin_time="+order_export_date_begin_time
		               +"&order_export_date_end_time="+order_export_date_end_time
          			   +"&status="+status;

          dataTable.ajax.url(url).load();
      };
      
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
	      if(selectField=='order_no'){
	    	$("#single_order_no1").val("");
	    	  $("#sp_name_show").hide();
	    	  $("#single_status").hide();
	    	  $("#create_stamp1_show").hide();
	    	  $("#single_order_no1").show();
	      }
	      if(selectField=='sp_name'){
	    	$("#sp_name_show").val("");
	    	  $("#single_order_no1").hide();
	    	  $("#single_status").hide();
	    	  $("#create_stamp1_show").hide();
	    	  $("#sp_name_show").show();
	      }
	      if(selectField=="toStatus_equals"){
	    	$("#single_status").val("");
	    	  $("#single_order_no1").hide();
	    	  $("#create_stamp1_show").hide();
	    	  $("#sp_name_show").hide();
	    	  $("#single_status").show();
	      }
	      if(selectField=="create_stamp"){
	    	  $("#single_order_no1").hide();
	    	  $("#sp_name_show").hide();
	    	  $("#single_status").hide();
	    	  $("#create_stamp1_show").show();
	      }
     });
	
	$('#singleSearchBtn').click(function(){
	     var selectField = $('#selected_field').val();
	     var selectFieldValue = '';
	      if(selectField=='order_no'){
	    	  selectFieldValue = $("#single_order_no1").val();
	      }
	      if(selectField=='sp_name'){
	    	  selectFieldValue = $("#single_sp_name_input").val();
	      }
	      if(selectField=="toStatus_equals"){
	    	  selectFieldValue = $("#single_status").val();
	      }
	      if(selectField=="create_stamp"){
	    	  var start_date = $("#single_create_stamp1_begin_time").val();
	    	  var end_date = $("#single_create_stamp1_end_time").val();
	      }
	     var url = "/tradeCostCheckOrder/orderList?"+selectField+"="+selectFieldValue
	     		+"&create_stamp_begin_time="+start_date
	     		+"&create_stamp_end_time="+end_date;
	     	dataTable.ajax.url(url).load();
	});
      
      
  });
});