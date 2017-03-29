define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '应付余额汇总表  | '+document.title;
  	  $('#menu_cost').addClass('active').find('ul').removeClass('in');
  	  
  	  
      var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: false,
          serverSide: true, //不打开会出现排序不对 
          ajax: "/costBalanceReport/list",
          columns: [
      			{ "data": "ABBR", "width": "120px"},
	            { "data": "CURRENCY", "width": "50px"},
	            {
					"render": function(data, type, full, meta) {
						return '<span style="color:red">'+eeda.numFormat(parseFloat(full.COST_TOTAL - full.COST_CONFIRM).toFixed(2),3)+'</span>';
					}
				},
	            { "data": "COST_CONFIRM", "width": "80px","visible":false,
					"render": function(data, type, full, meta) {
						return '<span style="color:green">'+eeda.numFormat(parseFloat(data).toFixed(2),3)+'</span>';
					}
	            },
	            { "data": "COST_TOTAL", "width": "80px",
	            	"render": function(data, type, full, meta) {
						return eeda.numFormat(parseFloat(data).toFixed(2),3);
					}
	            },
	            {
					"render": function(data, type, full, meta) {
						return ((parseFloat(full.COST_CONFIRM / full.COST_TOTAL).toFixed(4))*100).toFixed(2);
					}
				}
	          ]
	      });

      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
          var sp = $("#customer").val();
          var abbr_name=$('#customer_input').val();
          var order_export_date_begin_time = $("#order_export_date_begin_time").val();
          var order_export_date_end_time = $("#order_export_date_end_time").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/costBalanceReport/list?sp="+customer
          				+"&abbr_equals="+abbr_name
          				+"&order_export_date_begin_time="+order_export_date_begin_time
				        +"&order_export_date_end_time="+order_export_date_end_time;
          dataTable.ajax.url(url).load();
      };
  });
});