define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '应付对账单查询  | '+document.title;
  	  if(type!=""){
  		  $('#menu_todo_list').addClass('active').find('ul').addClass('in');
  		  $('#menu_cost').removeClass('active').find('ul').removeClass('in');
  	  }else{
  		$('#menu_todo_list').removeClass('active').find('ul').removeClass('in');
		  $('#menu_cost').addClass('active').find('ul').addClass('in');
  	  }
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'order_table',
          paging: true,
          serverSide: false, //不打开会出现排序不对 
          ajax: "/costCheckOrder/orderList",
          columns: [
            { "data": "ORDER_NO", "width": "100px"},
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

      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
    	  var order_no = $("#order_no1").val().trim(); 
          var customer = $("#customer1").val(); 
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
			           +"&customer_id="+customer
			           +"&sp_id="+sp
		               +"&create_stamp_begin_time="+start_date
		               +"&create_stamp_end_time="+end_date
          			   +"&status="+status;

          dataTable.ajax.url(url).load();
      };
      
   
  });
});