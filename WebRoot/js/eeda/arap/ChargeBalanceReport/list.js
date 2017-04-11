define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '客户应收余额汇总表  | '+document.title;
  		
  	$("#breadcrumb_li").text('应收余额汇总表');
  	  $('#menu_cost').addClass('active').find('ul').removeClass('in');
  	  
      var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: false,
          serverSide: true, //不打开会出现排序不对 
          ajax: "/chargeBalanceReport/list",
          initComplete:function(settings){
    	  cssTd();
      },
          columns: [
      			{ "data": "ABBR", "width": "120px"},
      			{ "data": "CHARGE_CNY", "width": "80px",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            },
	            { "data": "CHARGE_USD", "width": "80px"  ,
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            },
	            { "data": "CHARGE_JPY", "width": "80px",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            },
	            { "data": "CHARGE_HKD", "width": "80px",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            },
	            { "data": "CHARGE_RMB", "width": "100px",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            },
	            { "data": "UNCHARGE_CNY", "width": "80px",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            },
	            { "data": "UNCHARGE_USD", "width": "80px"  ,
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            },
	            { "data": "UNCHARGE_JPY", "width": "80px",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            },
	            { "data": "UNCHARGE_HKD", "width": "80px",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            },
	            { "data": "UNCHARGE_RMB", "width": "100px",
	            	"render": function(data, type, full, meta) {
					return '<span style="color:red;">'+eeda.numFormat(data,3)+'</span>';
				  }
	            },
	            {
					"render": function(data, type, full, meta) {
						return ((parseFloat((full.CHARGE_RMB-full.UNCHARGE_RMB) / full.CHARGE_RMB).toFixed(4))*100).toFixed(2);
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
      
      var cssTd=function(){
    	  $("#eeda_table td:nth-child(6)").css('background-color','#f5f5dc');
    	  $("#eeda_table td:nth-child(11)").css('background-color','#f5f5dc');
      }
      
      
	
     var searchData=function(){
          var sp_id = $("#sp_id").val();
          var abbr_name=$('#sp_id_input').val();
          var order_export_date_begin_time = $("#order_export_date_begin_time").val();
          var order_export_date_end_time = $("#order_export_date_end_time").val();
          
          
          
        //合计字段
          $.post('chargeBalanceReport/listTotal',{
        	  sp_id:sp_id,
        	  order_export_date_begin_time:order_export_date_begin_time,
        	  order_export_date_end_time:order_export_date_end_time
          },function(data){
        	  var charge_cny = parseFloat(data.CHARGE_CNY).toFixed(2);
        	  var charge_usd = parseFloat(data.CHARGE_USD).toFixed(2);
        	  var charge_jpy = parseFloat(data.CHARGE_JPY).toFixed(2);
        	  var charge_hkd = parseFloat(data.CHARGE_HKD).toFixed(2);
        	  var total_charge = parseFloat(data.TOTAL_CHARGE).toFixed(2);
        	  var uncharge_cny = parseFloat(data.UNCHARGE_CNY).toFixed(2);
        	  var uncharge_usd = parseFloat(data.UNCHARGE_USD).toFixed(2);
        	  var uncharge_jpy = parseFloat(data.UNCHARGE_JPY).toFixed(2);
        	  var uncharge_hkd = parseFloat(data.UNCHARGE_HKD).toFixed(2);
        	  var total_uncharge = parseFloat(data.TOTAL_UNCHARGE).toFixed(2);
        	  $('#CNY_charge_tatol').text(eeda.numFormat(charge_cny,3));
        	  $('#USD_charge_tatol').text(eeda.numFormat(charge_usd,3));
        	  $('#JPY_charge_tatol').text(eeda.numFormat(charge_jpy,3));
        	  $('#HKD_charge_tatol').text(eeda.numFormat(charge_hkd,3));
        	  $('#total_charge').text(eeda.numFormat(total_charge,3));
        	  $('#CNY_uncharge_tatol').text(eeda.numFormat(uncharge_cny,3)).css('color','red');
        	  $('#USD_uncharge_tatol').text(eeda.numFormat(uncharge_usd,3)).css('color','red');
        	  $('#JPY_uncharge_tatol').text(eeda.numFormat(uncharge_jpy,3)).css('color','red');
        	  $('#HKD_uncharge_tatol').text(eeda.numFormat(uncharge_hkd,3)).css('color','red');
        	  $('#total_uncharge').text(eeda.numFormat(total_uncharge,3)).css('color','red');
        	  
        	  var total_profit=parseFloat(total_charge-total_uncharge).toFixed(2);
        	  if(total_profit<0){
        		  $('#total_profit').text(total_profit).css('color','red');
        	  }else(
        		  $('#total_profit').text(total_profit)
        	  )

          });
          
          
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/chargeBalanceReport/list?sp="+sp_id
          				+"&abbr_equals="+abbr_name
          				+"&order_export_date_begin_time="+order_export_date_begin_time
				        +"&order_export_date_end_time="+order_export_date_end_time;
          dataTable.ajax.url(url).load();
      };
      searchData();
  });
});