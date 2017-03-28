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
          initComplete:function(settings){
        	  
          },
          columns: [
      			{ "data": "ABBR", "width": "100px"},
	            { "data": "CHARGE_CNY", "width": "80px",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return data;
				  }
	            },
	            { "data": "CHARGE_USD", "width": "80px"  ,
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return data;
				  }
	            },
	            { "data": "CHARGE_JPY", "width": "80px",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return data;
				  }
	            },
	            { "data": "CHARGE_HKD", "width": "80px",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return data;
				  }
	            },
	            { "data": "COST_CNY", "width": "80px" ,
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return data;
				  }
	            },
	            { "data": "COST_USD", "width": "80px",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return data;
				  }
	            },
	            { "data": "COST_JPY", "width": "80px",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return data;
				  }
	            },
	            { "data": "COST_HKD", "width": "80px",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return data;
				  }
	            },
	            { "data": "CHARGE_RMB", "width": "120px",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return data;
				  }
	            },
	            { "data": "COST_RMB", "width": "120px" ,
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return data;
				  }
	            },
	            {
					"render": function(data, type, full, meta) {
	            	    var profit = parseFloat(full.CHARGE_RMB - full.COST_RMB).toFixed(2);
	            	    if(profit<0){
	            	    	return '<span style="color:red;width:120px">'+profit+'</span>';
	            	    }
						return profit;
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
          $.post('profit/listTotal',{
        	  customer:customer,
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
        	  if(total_profit<0){
        		  $('#total_profit').text(total_profit).css('color','red');
        	  }else(
        		  $('#total_profit').text(total_profit)
        	  )

          });
          
          var cssTd=function(){
        	  $("#eeda_table th:eq(6)").css('background-color','#f5f5dc');
        	  $("#eeda_table td:nth-child(6)").css('background-color','#f5f5dc');
        	  $("#eeda_table td:nth-child(7)").css('background-color','#f5f5dc');
        	  $("#eeda_table td:nth-child(8)").css('background-color','#f5f5dc');
        	  $("#eeda_table td:nth-child(9)").css('background-color','#f5f5dc');
          }
         
          
          var url = "/profit/list?customer_id="+customer
				          +"&order_export_date_begin_time="+order_export_date_begin_time
				          +"&order_export_date_end_time="+order_export_date_end_time;
          dataTable.ajax.url(url).load(cssTd);
          
         
          
      };
  });
});