define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '应收应付利润率分析表  | '+document.title;
  	  $('#menu_cost').addClass('active').find('ul').removeClass('in');
  	  
      var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: false,
          serverSide: true, //不打开会出现排序不对 
//          ajax: "/profitAndPaymentRate/list",
          ajax:{
              //url: "/chargeCheckOrder/list",
              type: 'POST'
          }, 
          columns: [
      			{ "data": "ABBR", "width": "120px"},
	            { "data": "CHARGE_RMB", "width": "120px"},
	            { "data": "COST_RMB", "width": "120px"  },
	            {
					"render": function(data, type, full, meta) {
						return parseFloat(full.CHARGE_RMB - full.COST_RMB).toFixed(2);
					}
				},
	            { "data": "CHARGE_TOTAL", "width": "120px",
	            	"render": function(data, type, full, meta) {
						return parseFloat(((full.CHARGE_RMB - full.COST_RMB)/full.COST_RMB)*100).toFixed(2);
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
          
          //合计字段
          $.post('profitAndPaymentRate/listTotal',{
        	  customer:customer,
        	  order_export_date_begin_time:order_export_date_begin_time,
        	  order_export_date_end_time:order_export_date_end_time
          },function(data){
        	  var charge_cny = parseFloat(data.CHARGE_CNY).toFixed(2);
        	  var charge_usd = parseFloat(data.CHARGE_USD).toFixed(2);
        	  var charge_jpy = parseFloat(data.CHARGE_JPY).toFixed(2);
        	  var charge_hkd = parseFloat(data.CHARGE_HKD).toFixed(2);
        	  var cost_cny = parseFloat(data.COST_CNY).toFixed(2);
        	  var cost_usd = parseFloat(data.COST_USD).toFixed(2);
        	  var cost_jpy = parseFloat(data.COST_JPY).toFixed(2);
        	  var cost_hkd = parseFloat(data.COST_HKD).toFixed(2);
        	  $('#CNY_charge_tatol').text(charge_cny);
        	  $('#USD_charge_tatol').text(charge_usd);
        	  $('#JPY_charge_tatol').text(charge_jpy);
        	  $('#HKD_charge_tatol').text(charge_hkd);
        	  $('#CNY_cost_tatol').text(cost_cny);
        	  $('#USD_cost_tatol').text(cost_usd);
        	  $('#JPY_cost_tatol').text(cost_jpy);
        	  $('#HKD_cost_tatol').text(cost_hkd);
        	  
        	  var CNY_profit_tatol=parseFloat(charge_cny-cost_cny).toFixed(2);
        	  var USD_profit_tatol=parseFloat(charge_usd-cost_usd).toFixed(2);
        	  var JPY_profit_tatol=parseFloat(charge_jpy-cost_jpy).toFixed(2);
        	  var HKD_profit_tatol=parseFloat(charge_hkd-cost_hkd).toFixed(2);
        	  if(CNY_profit_tatol<0){
        		  $('#CNY_profit_tatol').text(CNY_profit_tatol).css('color','red');
        	  }else(
        		  $('#CNY_profit_tatol').text(CNY_profit_tatol)
        	  )
        	  
        	  if(USD_profit_tatol<0){
        		  $('#USD_profit_tatol').text(USD_profit_tatol).css('color','red');
        	  }else(
        		  $('#USD_profit_tatol').text(USD_profit_tatol)
        	  )
        	  
        	  if(JPY_profit_tatol<0){
        		  $('#JPY_profit_tatol').text(JPY_profit_tatol).css('color','red');
        	  }else(
        		  $('#JPY_profit_tatol').text(JPY_profit_tatol)
        	  )
        	  
        	  if(HKD_profit_tatol<0){
        		  $('#HKD_profit_tatol').text(HKD_profit_tatol).css('color','red');
        	  }else(
        		  $('#HKD_profit_tatol').text(HKD_profit_tatol)
        	  )

          });
          
          var url = "/profitAndPaymentRate/list?customer_id="+customer
				          +"&order_export_date_begin_time="+order_export_date_begin_time
				          +"&order_export_date_end_time="+order_export_date_end_time;
          dataTable.ajax.url(url).load();
          
          
      };
  });
});