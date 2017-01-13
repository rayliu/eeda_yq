define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '应收应付查询  | '+document.title;
  	  $('#menu_cost').addClass('active').find('ul').removeClass('in');
  	  
      var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: true,
          serverSide: true, //不打开会出现排序不对 
          ajax: "/arapReport/list",
          columns: [
      			{ "data": "ORDER_NO", "width": "60px",
			    	  "render": function ( data, type, full, meta ) {
	                      return "<a href='/jobOrder/edit?id="+full.JOBID+"'target='_blank'>"+data+"</a>";
	                  }
	            },
	            { "data": "CREATE_STAMP", "width": "100px"},
	            { "data": "CREATE_STAMP", "width": "100px"},
	            { "data": "CUSTOMER_NAME", "width": "80px"},
	            { "data": "CUSTOMER_NAME", "width": "80px"},
	            { "data": "BILL_FLAG", "width": "60px",
	                "render": function ( data, type, full, meta ) {
	                		if(data){
	      	            		if(data != 'Y'){
	      	            			return '未创建对账单';
	      	            		}else{
	      	            			return '已创建对账单';
	      	            		} 	  
	                  	}else{
	                			return '';
	                	}
	    			}
	            },
	            { "data": "TYPE", "width": "30px"},
	            { "data": "FIN_NAME", "width": "60px"},
	            { "data": "CURRENCY_NAME", "width": "40px" },
	            { "data": "TOTAL_AMOUNT", "width": "40px"},
	            { "data": "EXCHANGE_RATE", "width": "60px"},
	            { "data": "CURRENCY_TOTAL_AMOUNT", "width": "50px"},
	            { "data": "TOTAL_AMOUNT", "width": "60px"},
	            { "data": "EXCHANGE_RATE", "width": "60px"},
	            { "data": "CURRENCY_TOTAL_AMOUNT", "width": "60px"}
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
          var customer = $("#customer").val(); 
          var sp = $("#sp").val(); 
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/costCheckOrder/list?order_no="+order_no
			           +"&customer_id="+customer
			           +"&sp_id="+sp
		               +"&create_stamp_begin_time="+start_date
		               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
      };
  });
});