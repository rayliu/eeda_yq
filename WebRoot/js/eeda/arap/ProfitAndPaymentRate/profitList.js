define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco',  'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {
  	  
      var dataTable = eeda.dt({
          id: 'eeda_table',
          colReorder: true,
          paging: false,
          serverSide: true, //不打开会出现排序不对 
//          ajax: "/profitAndPaymentRate/list",
          ajax:{
              //url: "/chargeCheckOrder/list",
              type: 'POST'
          },
          initComplete:function(settings){
        	  
          },
          columns: [
      			{ "data": "ABBR", "width": "120px"},
      			{ "data": "EMPLOYEE_NAME", "width": "120px"},
	            { "data": "CHARGE_RMB", 
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            },
	            { "data": "COST_RMB", 
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            },
	            {
					"render": function(data, type, full, meta) {
	            	    var profit = parseFloat(full.CHARGE_RMB - full.COST_RMB).toFixed(2);
	            	    if(profit<0){
	            	    	return '<span style="color:red;width:120px">'+eeda.numFormat(profit,3)+'</span>';
	            	    }
						return eeda.numFormat(profit,3);
					}
				},
	            { 
	            	"render": function(data, type, full, meta) {
					    var profit_rate=parseFloat(((full.CHARGE_RMB - full.COST_RMB)/full.COST_RMB)*100).toFixed(2);
	            		if(!full.COST_RMB){
	            			return "";
	            		}
	            		if(profit_rate<0){
	            			return '<span style="color:red;">'+profit_rate+'</span>';
	            		}
	            		return profit_rate;
					}
	            },
	            {
					"render": function(data, type, full, meta) {
	            	    var profit = parseFloat(full.CHARGE_RMB - full.COST_RMB).toFixed(2);
	            	    var profit = ((profit*full.ROYALTY_RATE)/100).toFixed(2);
	            	    if(profit<0){
	            	    	return '<span style="color:red;width:120px">'+eeda.numFormat(profit,3)+'</span>';
	            	    }else if(profit=="0.00"){
	            	    	return '';
	            	    }
						return eeda.numFormat(profit,3);
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
          var employee_name = $("#employee_id_input").val().trim(); 
          var employee_id = $("#employee_id").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          
          //合计字段
          $.post('profit/listTotal',{
        	  customer:customer,
        	  employee_name:employee_name,
        	  employee_id:employee_id,
        	  order_export_date_begin_time:order_export_date_begin_time,
        	  order_export_date_end_time:order_export_date_end_time
          },function(data){
        	  var charge_cny = parseFloat(data.CHARGE_CNY).toFixed(2);
        	  var charge_usd = parseFloat(data.CHARGE_USD).toFixed(2);
        	  var charge_jpy = parseFloat(data.CHARGE_JPY).toFixed(2);
        	  var charge_hkd = parseFloat(data.CHARGE_HKD).toFixed(2);
        	  var total_charge = parseFloat(data.TOTAL_CHARGE).toFixed(2);
        	  var cost_cny = parseFloat(data.COST_CNY).toFixed(2);
        	  var cost_usd = parseFloat(data.COST_USD).toFixed(2);
        	  var cost_jpy = parseFloat(data.COST_JPY).toFixed(2);
        	  var cost_hkd = parseFloat(data.COST_HKD).toFixed(2);
        	  var total_cost = parseFloat(data.TOTAL_COST).toFixed(2);
        	  $('#CNY_charge_tatol').text(charge_cny);
        	  $('#USD_charge_tatol').text(charge_usd);
        	  $('#JPY_charge_tatol').text(charge_jpy);
        	  $('#HKD_charge_tatol').text(charge_hkd);
        	  $('#total_charge').text(total_charge);
        	  $('#CNY_cost_tatol').text(cost_cny);
        	  $('#USD_cost_tatol').text(cost_usd);
        	  $('#JPY_cost_tatol').text(cost_jpy);
        	  $('#HKD_cost_tatol').text(cost_hkd);
        	  $('#total_cost').text(total_cost);
        	  
        	  
        	  var total_profit=parseFloat(total_charge-total_cost).toFixed(2);
        	  var average_profit_rate = parseFloat((total_profit/total_cost)*100).toFixed(2);
        	  if(!average_profit_rate){
        		  average_profit_rate=0.00;
        	  }
        	  if(total_profit<0){
        		  $('#total_profit').text(total_profit).css('color','red');
        	  }else(
        		  $('#total_profit').text(total_profit)
        	  )
        	  var total=parseFloat(data.TOTAL);
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(0).html('共'+total+'项汇总：');
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(2).html("折合应收(CNY):<br>"+eeda.numFormat(total_charge,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(3).html("折合应付(CNY):<br>"+eeda.numFormat(total_cost,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(5).html("平均利润率(%):<br>"+average_profit_rate);
        	  if(total_profit<0){
        		  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(4).html("利润(CNY):<br>"+eeda.numFormat(total_profit,3)).css('color','red');
        	  }else(
        		$($('.dataTables_scrollFoot tr')[0]).find('th').eq(4).html("利润(CNY):<br>"+eeda.numFormat(total_profit,3))
        	  )
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(6).html("提成");

          });
          
          var cssTd=function(){
        	  $("#eeda_table th:eq(6)").css('background-color','#f5f5dc');
        	  $("#eeda_table td:nth-child(6)").css('background-color','#f5f5dc');
        	  $("#eeda_table td:nth-child(8)").css('background-color','#f5f5dc');
        	  $("#eeda_table td:nth-child(9)").css('background-color','#f5f5dc');
        	  $("#eeda_table td:nth-child(10)").css('background-color','#f5f5dc');
          }
         
          
          var url = "/profit/list?customer_id="+customer
          				  +"&employee_name="+employee_name  
          				  +"&employee_id="+employee_id
				          +"&order_export_date_begin_time="+order_export_date_begin_time
				          +"&order_export_date_end_time="+order_export_date_end_time;
          dataTable.ajax.url(url).load(cssTd);
          
         
          
      };
      searchData();
  });
});