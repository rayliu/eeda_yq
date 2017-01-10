define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '客户应收账龄分析表  | '+document.title;
  	  $('#menu_cost').addClass('active').find('ul').removeClass('in');
  	  
      var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: true,
          serverSide: true, //不打开会出现排序不对 
          ajax: "/accountAging/list",
          columns: [
      			{ "data": "ABBR_NAME", "width": "100px"},
	            { "data": "CURRENCY", "width": "100px"},
	            {
					"render": function(data, type, full, meta) {
						return parseFloat(full.CHARGE_TOTAL - full.CHARGE_CONFIRM).toFixed(2);
					}
				},
	            { "data": "CHARGE_CONFIRM", "width": "100px",
					"render": function(data, type, full, meta) {
						return parseFloat(data).toFixed(2);
					}
	            },
	            { "data": "CHARGE_TOTAL", "width": "100px",
	            	"render": function(data, type, full, meta) {
						return parseFloat(data).toFixed(2);;
					}
	            },
	            {
					"render": function(data, type, full, meta) {
						return ((parseFloat(full.CHARGE_CONFIRM / full.CHARGE_TOTAL).toFixed(4))*100).toFixed(2);
					}
				},
				{
					"render": function(data, type, full, meta) {
						return (0*100).toFixed(2);
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
          var customer = $("#customer").val(); 
          var service_stamp_between = $("#service_stamp").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/chargeBalanceReport/list?abbr_name="+customer
		               +"&service_stamp_between="+service_stamp_between;
          dataTable.ajax.url(url).load();
      };
  });
});