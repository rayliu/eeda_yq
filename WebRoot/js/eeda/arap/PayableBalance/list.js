define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '客户应收余额汇总表  | '+document.title;
  	  $('#menu_cost').addClass('active').find('ul').removeClass('in');
  	  
      var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: true,
          serverSide: true, //不打开会出现排序不对 
          ajax: "/payableBalance/list",
          columns: [
      			{ "data": "ABBR", "width": "100px"},
	            { "data": "CURRENCY", "width": "100px"},
	            {
					"render": function(data, type, full, meta) {
						return parseFloat(full.COST_TOTAL - full.COST_CONFIRM).toFixed(2);
					}
				},
	            { "data": "COST_CONFIRM", "width": "100px",
					"render": function(data, type, full, meta) {
						return parseFloat(data).toFixed(2);
					}
	            },
	            { "data": "COST_TOTAL", "width": "100px",
	            	"render": function(data, type, full, meta) {
						return parseFloat(data).toFixed(2);;
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
          var service_stamp_between = $("#service_stamp").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/payableBalance/list?sp="+customer
          				+"&abbr_equals="+abbr_name
		                +"&service_stamp_between="+service_stamp_between;
          dataTable.ajax.url(url).load();
      };
  });
});