define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '应付明细查询   | '+document.title;
  	  if(type!=""){
  		  $('#menu_todo_list').addClass('active').find('ul').addClass('in');
  		  $('#menu_cost').removeClass('active').find('ul').removeClass('in');
  	  }else{
  		$('#menu_todo_list').removeClass('active').find('ul').removeClass('in');
		  $('#menu_cost').addClass('active').find('ul').addClass('in');
  	  }
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: true,
          serverSide: true, //不打开会出现排序不对 
          ajax: "/costConfirmList/list",
          columns: [
			{ "data":"ID","width": "10px",
			    "render": function ( data, type, full, meta ) {
			    	if(data)
			    		return '<input type="checkbox" class="checkBox" style="width:30px">';
			    	else 
			    		return '<input type="checkbox" class="checkBox" style="width:30px" disabled>';
			    }
			},
            { "data": "ORDER_NO", "width": "100px"},
            { "data": "CREATE_STAMP", "width": "100px"},
            { "data": "CUSTOMER", "width": "100px"},
            { "data": "TYPE", "width": "60px"},
            { "data": "SP_NAME", "width": "100px"},
            { "data": "CHARGE_NAME", "width": "60px"},
            { "data": "PRICE", "width": "60px"},
            { "data": "AMOUNT","width": "60px"},
            { "data": "UNIT_NAME", "width": "60px"},
            { "data": "CURRENCY_NAME", "width": "60px"},
            { "data": "TOTAL_AMOUNT", "width": "60px"},
            { "data": "EXCHANGE_RATE", "width": "60px"},
            { "data": "CURRENCY_TOTAL_AMOUNT", "width": "60px"},
            { "data": "REMARK", "width": "180px"},
          ]
      });

      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
          var order_no = $("#order_no").val(); 
          var customer = $("#customer").val(); 
          var sp = $("#sp").val(); 
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          debugger
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/costConfirmList/list?order_no="+order_no
			           +"&customer_id="+customer
			           +"&sp_id="+sp
		               +"&create_stamp_begin_time="+start_date
		               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
      };
  });

});