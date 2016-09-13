define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '应付对账单查询  | '+document.title;
  	  $('#menu_cost').addClass('active').find('ul').addClass('in');
  	  
      var dataTable = eeda.dt({
          id: 'order_table',
          paging: true,
          serverSide: false, //不打开会出现排序不对 
          ajax: "/costCheckOrder/orderList",
          columns: [
            { "data": "ORDER_NO", "width": "100px",
            	 "render": function ( data, type, full, meta ) {
           		  return "<a href='/costCheckOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
           	  }
            },
            { "data": "CREATE_STAMP", "width": "100px"},
            { "data": "STATUS", "width": "60px"},
            { "data": "SP_NAME", "width": "100px"},
            { "data": "TOTAL_AMOUNT", "width": "60px"},
            { "data": null, "width": "60px"},
            { "data": null, "width": "60px"},
            { "data": null, "width": "60px"},
            { "data": "PAID_AMOUNT", "width": "60px"},
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
          var start_date = $("#create_stamp1_begin_time").val();
          var end_date = $("#create_stamp1_end_time").val();
          var status = $("#status").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/costCheckOrder/orderList?order_no="+order_no
			           +"&sp_id="+sp
		               +"&create_stamp_begin_time="+start_date
		               +"&create_stamp_end_time="+end_date
          			   +"&status="+status;

          dataTable.ajax.url(url).load();
      };
      
   
  });
});