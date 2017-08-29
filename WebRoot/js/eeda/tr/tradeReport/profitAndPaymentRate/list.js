define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco',  'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {
  	
      var dataTable = eeda.dt({
          id: 'eeda_table',
          colReorder: true,
          paging: false,
          serverSide: true, //不打开会出现排序不对 
//          ajax: "/tradeProfitAndPaymentRate/list",
          ajax:{
              //url: "/chargeCheckOrder/list",
              type: 'POST'
          },
          initComplete:function(settings){
        	  
          },
          columns: [
      			{ "data": "ABBR", "width": "100px","class":"abbr"},
	            { "data": "CHARGE_CNY", "width": "80px","class":"charge_cny",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            },
	            { "data": "CHARGE_USD", "width": "100px"  ,"class":"charge_usd",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            },
	            { "data": "CHARGE_JPY", "width": "100px","class":"charge_jpy",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            },
	            { "data": "CHARGE_HKD", "width": "100px","class":"charge_hkd",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            },
	            { "data": "COST_CNY", "width": "100px" ,"class":"cost_cny",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            },
	            { "data": "COST_USD", "width": "100px","class":"cost_usd",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            },
	            { "data": "COST_JPY", "width": "100px","class":"cost_jpy",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            },
	            { "data": "COST_HKD", "width": "100px","class":"cost_hkd",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            },
	            { "data": "CHARGE_RMB", "width": "120px","class":"charge_rmb",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            },
	            { "data": "COST_RMB", "width": "120px" ,"class":"cost_rmb",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data,3);
				  }
	            }
	          ]
	      });
      //base on config hide cols
      dataTable.columns().eq(0).each( function(index) {
          var column = dataTable.column(index);
          $.each(cols_config, function(index, el) {
              
              if(column.dataSrc() == el.COL_FIELD){
                
                if(el.IS_SHOW == 'N'){
                  column.visible(false, false);
                }else{
                  column.visible(true, false);
                }
              }
          });
      });
      
      $('.complex_search').click(function(event) {
          if($('.search_single').is(':visible')){
            $('.search_single').hide();
          }else{
            $('.search_single').show();
          }
      });
      //简单查询
      $('#selected_field').change(function(event) {
	      var selectField = $('#selected_field').val();
	      if(selectField=='order_export_date'){
	    	  $("#single_customer_input").val("");
	    	  $("#user_id_show").hide();
	    	  $("#sp_customer_show").hide();
	    	  $("#order_export_date_show").show();
		  }
		  if(selectField=='customer'){
			  $("#single_order_export_date_begin_time").val("");
	    	  $("#single_order_export_date_end_time").val("");
			  $("#user_id_show").hide();
	    	  $("#order_export_date_show").hide();
			  $("#sp_customer_show").show();
		  }
		  if(selectField=="user_id"){
			  $("#user_id_show").val("");
			  $("#sp_customer_show").hide();
			  $("#order_export_date_show").hide();
			  $("#user_id_show").show();
		  }
     });
	
	$('#singleSearchBtn').click(function(){
		var selectField = $("#selected_field").val();
		  if(selectField=='sp_id'){
			  var sp_id = $("#single_sp_id").val();
	      }
	      if(selectField=='employee_id'){
	    	  var employee_id = $("#employee_id_show").val();
	      }
	      if(selectField=="order_export_date"){
	    	  var order_export_date_begin_time = $("#single_order_export_date_begin_time").val();
			  var order_export_date_end_time = $("#single_order_export_date_end_time").val();
	      }
	      if(selectField=='customer'){
		    	 var customer = $("#single_customer").val();
		    }
	      
	      
	      var url = "/tradeProfitAndPaymentRate/list?sp_id="+sp_id
	      	+"&customer_id="+customer
	      	+"&employee_id="+employee_id
			+"&order_export_date_begin_time="+order_export_date_begin_time
	        +"&order_export_date_end_time="+order_export_date_end_time;
	      	dataTable.ajax.url(url).load(tableStyle);
	      	
	      	listTotalMoney(sp_id,order_export_date_begin_time,order_export_date_end_time)
	 
	}); 
      
    var tableStyle = function(){
  	  $('.oneRow').css('line-height','30px');
  	  $('.doubleRow').css('text-align','center');
  	  
  	  var tableName = "eeda_table";
  	  //格式【合成表头的第一列位置，合成的列数，颜色】
  	  var array= [[2,5,'#f8fff0'],[7,5,'#eeffff']];
  	  for (var i = 0; i < array.length; i++) {
  		  var firstChild = array[i][0];
      	  var cols = array[i][1];
      	  var bgColor = array[i][2];
      	  for (var j = firstChild; j < (firstChild+cols); j++) {
      		  $("#"+tableName+" td:nth-child("+j+")").css('background-color',bgColor);
      	  }
		  }
    }
    var listTotalMoney = function(sp_id,order_export_date_begin_time,order_export_date_end_time){
        //合计字段
	        $.post('tradeProfitAndPaymentRate/listTotal',{
	      	  sp_id:sp_id,
	      	  order_export_date_begin_time:order_export_date_begin_time,
	      	  order_export_date_end_time:order_export_date_end_time
	        },function(data){
	      	  
	          var cost_cny = parseFloat(data.COST_CNY).toFixed(2);
	      	  var cost_usd = parseFloat(data.COST_USD).toFixed(2);
	      	  var cost_jpy = parseFloat(data.COST_JPY).toFixed(2);
	      	  var cost_hkd = parseFloat(data.COST_HKD).toFixed(2);
	      	  var total_cost = parseFloat(data.TOTAL_COST).toFixed(2);
	      	  
	      	  var uncost_cny = parseFloat(data.UNCOST_CNY).toFixed(2);
	      	  var uncost_usd = parseFloat(data.UNCOST_USD).toFixed(2);
	      	  var uncost_jpy = parseFloat(data.UNCOST_JPY).toFixed(2);
	      	  var uncost_hkd = parseFloat(data.UNCOST_HKD).toFixed(2);
	      	  var total_uncost = parseFloat(data.TOTAL_UNCOST).toFixed(2);
	      	  
	      	  var charge_cny = parseFloat(data.CHARGE_CNY).toFixed(2);
        	  var charge_usd = parseFloat(data.CHARGE_USD).toFixed(2);
        	  var charge_jpy = parseFloat(data.CHARGE_JPY).toFixed(2);
        	  var charge_hkd = parseFloat(data.CHARGE_HKD).toFixed(2);
        	  var total_charge = parseFloat(data.TOTAL_CHARGE).toFixed(2);
	      	  
        	  $('#CNY_charge_tatol').text(charge_cny);
        	  $('#USD_charge_tatol').text(charge_usd);
        	  $('#JPY_charge_tatol').text(charge_jpy);
        	  $('#HKD_charge_tatol').text(charge_hkd);
        	  $('#total_charge').text(total_charge);
        	  
        	  $('#CNY_cost_tatol').text(eeda.numFormat(cost_cny,3));
	      	  $('#USD_cost_tatol').text(eeda.numFormat(cost_usd,3));
	      	  $('#JPY_cost_tatol').text(eeda.numFormat(cost_jpy,3));
	      	  $('#HKD_cost_tatol').text(eeda.numFormat(cost_hkd,3));
	      	  $('#total_cost').text(eeda.numFormat(total_cost,3));
	      	  
	      	  $('#CNY_uncost_tatol').text(eeda.numFormat(uncost_cny,3)).css('color','red');
	      	  $('#USD_uncost_tatol').text(eeda.numFormat(uncost_usd,3)).css('color','red');
	      	  $('#JPY_uncost_tatol').text(eeda.numFormat(uncost_jpy,3)).css('color','red');
	      	  $('#HKD_uncost_tatol').text(eeda.numFormat(uncost_hkd,3)).css('color','red');
	      	  $('#total_uncost').text(eeda.numFormat(total_uncost,3)).css('color','red');
	      	  
//	      	  var total=parseFloat(data.TOTAL);
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=abbr]').html('共'+total+'项汇总：');
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_cny]').html("应收CNY"+eeda.numFormat(charge_cny,3));
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_usd]').html("应收USD"+eeda.numFormat(charge_usd,3));
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_jpy]').html("应收JPY"+eeda.numFormat(charge_jpy,3));
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_hkd]').html("应收HKD"+eeda.numFormat(charge_hkd,3));
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_cny]').html("应付CNY"+eeda.numFormat(cost_cny,3));
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_usd]').html("应付USD"+eeda.numFormat(cost_usd,3));
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_jpy]').html("应付JPY"+eeda.numFormat(cost_jpy,3));
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_hkd]').html("应付HKD"+eeda.numFormat(cost_hkd,3));
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_rmb]').html("折合应收(RMB)"+eeda.numFormat(total_charge,3));
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_rmb]').html("折合应付(RMB)"+eeda.numFormat(total_cost,3));
	        });
    }
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })
      
	  //导出excel利润表
      $("#singlexportTotaledExcel").click(function(){
    	  $(this).attr('disabled', true);
    	  var customer_id = $("#single_customer").val();
          var begin_time = $("#single_order_export_date_begin_time").val();
          var end_time = $("#single_order_export_date_end_time").val();
          excel_method(customer_id,begin_time,end_time);
      })
  	
      //导出excel利润表
      $('#exportTotaledExcel').click(function(){
          $(this).attr('disabled', true);
          var customer_id = $("#customer").val();
          var begin_time = $("#order_export_date_begin_time").val();
          var end_time = $("#order_export_date_end_time").val();
          excel_method(customer_id,begin_time,end_time);
      });
      
      var excel_method = function(customer_id,begin_time,end_time){
    	  $.post('/tradeProfitAndPaymentRate/downloadExcelList',{customer_id:customer_id,begin_time:begin_time,end_time:end_time}, function(data){
              $('#exportTotaledExcel').prop('disabled', false);
              $('#singlexportTotaledExcel').prop('disabled', false);
              $.scojs_message('生成应收Excel对账单成功', $.scojs_message.TYPE_OK);
              window.open(data);
          }).fail(function() {
              $('#exportTotaledExcel').prop('disabled', false);
              $.scojs_message('生成应收Excel对账单失败', $.scojs_message.TYPE_ERROR);
          });
      }
      
     var searchData=function(){
          var sp_id = $("#sp_id").val(); 
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
          
      	listTotalMoney(sp_id,order_export_date_begin_time,order_export_date_end_time)
          
          var cssTd=function(){
        	  $("#eeda_table th:eq(6)").css('background-color','#f5f5dc');
        	  $("#eeda_table td:nth-child(6)").css('background-color','#f5f5dc');
        	  $("#eeda_table td:nth-child(7)").css('background-color','#f5f5dc');
        	  $("#eeda_table td:nth-child(8)").css('background-color','#f5f5dc');
        	  $("#eeda_table td:nth-child(9)").css('background-color','#f5f5dc');
          }
         
          
          var url = "/tradeProfitAndPaymentRate/list?sp_id="+sp_id
				          +"&order_export_date_begin_time="+order_export_date_begin_time
				          +"&order_export_date_end_time="+order_export_date_end_time;
          dataTable.ajax.url(url).load(cssTd);
          
         
          
      };
      searchData(); 
  });
});