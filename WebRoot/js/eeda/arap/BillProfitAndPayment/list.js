define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '应收应付利润率分析表  | '+document.title;
  	  $('#menu_cost').addClass('active').find('ul').removeClass('in');
  	  
      var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: true,
          serverSide: true, //不打开会出现排序不对 
          ajax: "/billProfitAndPayment/list",
          columns: [
                {"data": "ORDER_NO", 
              	  "render": function ( data, type, full, meta ) {
              		  return "<a href='/jobOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
              	  }
                },
      			{ "data": "ABBR", "width": "120px"},
      			{ "data": "ORDER_EXPORT_DATE", "width": "120px"},
	            { "data": "CHARGE_RMB", "width": "120px"},
	            { "data": "COST_RMB", "width": "120px"  },
	            {
					"render": function(data, type, full, meta) {
						return parseFloat(full.CHARGE_RMB - full.COST_RMB).toFixed(2);
					}
				},
	            { "data": "CHARGE_TOTAL", "width": "120px",
	            	"render": function(data, type, full, meta) {
	            		var str="";
	            		if(full.COST_RMB!=0){
	            			str = parseFloat(((full.CHARGE_RMB - full.COST_RMB)/full.COST_RMB)*100).toFixed(2);
	            		}
						return str
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
          var order_export_date_begin_time = $("#order_export_date_begin_time").val();
          var order_export_date_end_time = $("#order_export_date_end_time").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          
          
          var url = "/billProfitAndPayment/list?customer_id="+customer
				          +"&order_export_date_begin_time="+order_export_date_begin_time
				          +"&order_export_date_end_time="+order_export_date_end_time;
          dataTable.ajax.url(url).load();
          
          
      };
  });
});