define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '计划订单查询 | '+document.title;
    	$('#menu_order').addClass('active').find('ul').addClass('in');

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            ajax: "/planOrder/list",
            columns:[
                  {"data": "ORDER_NO", 
                	  "render": function ( data, type, full, meta ) {
                		  return "<a href='/planOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
                	  }
                  },
	              { "data": "TYPE",
                    "render": function ( data, type, full, meta ) {
                      var str = '';
                      if(data == 'export')
                        str = '出口'; 
                      if(data == 'import')
                        str = '进口';
                      if(data == 'both')
                        str = '进出口';
                      if(data == 'oneDayTrip')
                        str = '一日游';
                      return str;
                    }
                }, 
	              { "data": "CUSTOMER_NAME"}, 
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
          var order_no = $("#order_no").val(); 
          var status = $('#status').val();
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/planOrder/list?order_no="+order_no
               +"&status="+status
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
      };
    	
    });
});