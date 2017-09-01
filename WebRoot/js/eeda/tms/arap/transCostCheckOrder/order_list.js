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
          ajax: "/transCostCheckOrder/orderList",
          columns: [
            { "data": "ORDER_NO",
            	 "render": function ( data, type, full, meta ) {
           		  return "<a href='/transCostCheckOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
           	  }
            },
            { "data": "CREATE_STAMP"},
            { "data": "STATUS"},
            { "data": "SP_NAME"},
            { "data": "CAR_NO"},
            { "data": "TOTAL_AMOUNT","visible":false},
            { "data": "TOTAL_AMOUNT_CNY",
              "render":function(data,type,full,meta){
                if(!data)
                  data=0.00;
                var usd_str=eeda.numFormat(parseFloat(data).toFixed(2),3);
                return usd_str;
              }
            },
            { "data": "TOTAL_RECEIVE_CNY",
              "render":function(data,type,full,meta){
                if(!data)
                  data=0.00;
                var usd_str=eeda.numFormat(parseFloat(data).toFixed(2),3);
                return usd_str;
              }
            },
            { "data": "TOTAL_RESIDUAL_CNY",
              "render":function(data,type,full,meta){
                if(!data)
                  data=0.00;
                var usd_str=eeda.numFormat(parseFloat(data).toFixed(2),3);
                return usd_str;
              }
            }
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
	      if(selectField=='car_no_like'){
	    	  $("#single_car_no1_input").val("");
	    	  $("#single_sp1_id_show").hide();
	    	  $("#single_status1").hide();
	    	  $("#single_create_stamp1_show").hide();
	    	  $("#public_text").hide();
	    	  $("#single_car_no1_show").show();
	      }
	      if(selectField=='order_no'){
	    	  $("#public_text").val("");
	    	  $("#single_sp1_id_show").hide();
	    	  $("#single_status1").hide();
	    	  $("#single_create_stamp1_show").hide();
	    	  $("#single_car_no1_show").hide();
	    	  $("#public_text").show();
	      }
	      if(selectField=='sp_name_like'){
	    	  $("#single_sp1_id_input").val("");
	    	  $("#single_status1").hide();
	    	  $("#single_create_stamp1_show").hide();
	    	  $("#single_car_no1_show").hide();
	    	  $("#public_text").hide();
	    	  $("#single_sp1_id_show").show();
	      }
	      if(selectField=='status_equals'){
	    	  $("#single_status1").val("");
	    	  $("#single_create_stamp1_show").hide();
	    	  $("#single_car_no1_show").hide();
	    	  $("#public_text").hide();
	    	  $("#single_sp1_id_show").hide();
	    	  $("#single_status1").show();
	      }
	      if(selectField=='create_stamp'){
	    	  $("#single_create_stamp1_begin_time").val("");
	    	  $("#single_create_stamp1_end_time").val("");
	    	  $("#single_car_no1_show").hide();
	    	  $("#public_text").hide();
	    	  $("#single_sp1_id_show").hide();
	    	  $("#single_status1").hide();
	    	  $("#single_create_stamp1_show").show();
	      }
     });
      
      $("#singleSearchBtn").click(function(){
    	  var selectField = $('#selected_field').val();
    	  var selectValue = "";
    	  if(selectField=='car_no_like'){
	    	  selectValue = $("#single_car_no1_input").val();
	      }
	      if(selectField=='order_no'){
	    	  selectValue = $("#public_text").val();
	      }
	      if(selectField=='sp_name_like'){
	    	  selectValue = $("#single_sp1_id_input").val();
	      }
	      if(selectField=='status_equals'){
	    	  selectValue = $("#single_status1").val();
	      }
	      if(selectField=='create_stamp'){
	    	  var create_stamp1_begin = $("#single_create_stamp1_begin_time").val();
	    	  var create_stamp1_end = $("#single_create_stamp1_end_time").val();
	      }
	      
          var url = "/transCostCheckOrder/orderList?"+selectField+"="+selectValue
               +"&create_stamp_begin_time="+create_stamp1_begin
               +"&create_stamp_end_time="+create_stamp1_end;
          dataTable.ajax.url(url).load();
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
          var car_no_input = $("#car_no1_input").val();
          var start_date = $("#create_stamp1_begin_time").val();
          var end_date = $("#create_stamp1_end_time").val();
          var status = $("#status1").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/transCostCheckOrder/orderList?order_no="+order_no
			           +"&sp_id="+sp
			           +"&sp_name_like="+sp1_input
			           +"&car_no_like="+car_no_input
		               +"&create_stamp_begin_time="+start_date
		               +"&create_stamp_end_time="+end_date
          			   +"&status="+status;

          dataTable.ajax.url(url).load();
      };
      
      
  });
});