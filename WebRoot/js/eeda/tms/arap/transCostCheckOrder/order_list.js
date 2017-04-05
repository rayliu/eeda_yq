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
          var land_export_date_begin_time = $("#land_export_date1_begin_time").val();
          var land_export_date_end_time = $("#land_export_date1_end_time").val();
          var status = $("#status").val();
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
		               +"&create_stamp_begin_time="+start_date
		               +"&create_stamp_end_time="+end_date
		               +"&land_export_date_begin_time="+land_export_date_begin_time
		               +"&land_export_date_end_time="+land_export_date_end_time
          			   +"&status="+status;

          dataTable.ajax.url(url).load();
      };
      
      
  });
});