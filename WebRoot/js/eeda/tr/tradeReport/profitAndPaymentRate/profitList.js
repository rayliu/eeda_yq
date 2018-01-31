define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco',  'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {
	  $('.search_single input,.search_single select').on('input',function(){
  		  $("#orderForm")[0].reset();
  	  });
	  
      var dataTable = eeda.dt({
          id: 'eeda_table',
          colReorder: true,
          paging: false,
          serverSide: true, //不打开会出现排序不对 
          ajax: "/tradeProfit/list",
          columns: [
      			{ "data": "ABBR", "width": "120px"},
	            { "data": "CHARGE_RMB", 
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            { "data": "COST_RMB", 
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
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
	            }
	          ]
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
	    	  $("#single_customer").val("");
	    	  $("#sp_customer_show").hide();
	    	  $("#order_export_date_show").show();
		  }
		  if(selectField=='customer'){
			  $("#single_order_export_date_begin_time").val("");
	    	  $("#single_order_export_date_end_time").val("");
	    	  $("#order_export_date_show").hide();
			  $("#sp_customer_show").show();
		  }
     });
	
	$('#singleSearchBtn').click(function(){
		 $("#orderForm")[0].reset();
	     var selectField = $('#selected_field').val();
	     if(selectField=='order_export_date'){
	    	 $("#order_export_date_begin_time").val($("#single_order_export_date_begin_time").val());
	    	 $("#order_export_date_end_time").val($("#single_order_export_date_end_time").val());
	      }
	      if(selectField=='customer'){
	    	 $("#customer").val($("#single_customer").val());
	    	 $("#customer_input").val($("#single_customer_input").val());
	      }
	      $('#searchBtn').click();
	}); 
	
	 var listTotalMoney = function(customer,order_export_date_begin_time,order_export_date_end_time){
	      //合计字段
         $.post('/tradeProfit/listTotal',{
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
       	  $('#CNY_charge_tatol').text(eeda.numFormat(charge_cny));
       	  $('#USD_charge_tatol').text(eeda.numFormat(charge_usd));
       	  $('#JPY_charge_tatol').text(eeda.numFormat(charge_jpy));
       	  $('#HKD_charge_tatol').text(eeda.numFormat(charge_hkd));
       	  $('#total_charge').text(eeda.numFormat(total_charge));
       	  $('#CNY_cost_tatol').text(eeda.numFormat(cost_cny));
       	  $('#USD_cost_tatol').text(eeda.numFormat(cost_usd));
       	  $('#JPY_cost_tatol').text(eeda.numFormat(cost_jpy));
       	  $('#HKD_cost_tatol').text(eeda.numFormat(cost_hkd));
       	  $('#total_cost').text(eeda.numFormat(total_cost));
       	  
       	  
       	  var total_profit=parseFloat(total_charge-total_cost).toFixed(2);
       	  var average_profit_rate = parseFloat((total_profit/total_cost)*100).toFixed(2);
       	  if(!average_profit_rate){
       		  average_profit_rate=0.00;
       	  }
       	  if(total_profit<0){
       		  $('#total_profit').text(eeda.numFormat(total_profit)).css('color','red');
       	  }else(
       		  $('#total_profit').text(eeda.numFormat(total_profit))
       	  )
       	  var total=parseFloat(data.TOTAL);
       	 $($('.dataTables_scrollFoot tr')[0]).find('th').eq(0).html('共'+total+'项汇总：');
       	 $($('.dataTables_scrollFoot tr')[0]).find('th').eq(1).html("折合应收(CNY):"+eeda.numFormat(total_charge,3));
       	 $($('.dataTables_scrollFoot tr')[0]).find('th').eq(2).html("折合应付(CNY):"+eeda.numFormat(total_cost,3));
       	 $($('.dataTables_scrollFoot tr')[0]).find('th').eq(4).html("平均利润率(%)："+average_profit_rate);
       	 if(total_profit<0){
   		  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(3).html("利润(CNY):"+eeda.numFormat(total_profit,3)).css('color','red');
       	 }else{
       			 $($('.dataTables_scrollFoot tr')[0]).find('th').eq(3).html("利润(CNY):"+eeda.numFormat(total_profit,3))
       	 }
       	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(6).html("提成");

         });
	 }
	
	  //导出excel
    $("#singlexportTotaledExcel").click(function(){
  	  $(this).attr('disabled', true);
  	  var customer_id = $("#single_customer").val();
        var begin_time = $("#single_order_export_date_begin_time").val();
        var end_time = $("#single_order_export_date_end_time").val();
        excel_method(customer_id,begin_time,end_time);
    })
	
    //导出excel
    $('#exportTotaledExcel').click(function(){
        $(this).attr('disabled', true);
        var customer_id = $("#customer").val();
        var begin_time = $("#order_export_date_begin_time").val();
        var end_time = $("#order_export_date_end_time").val();
        excel_method(customer_id,begin_time,end_time);
    });
    
    var excel_method = function(customer_id,begin_time,end_time){
  	  $.post('/tradeProfit/downloadExcelList',{customer_id:customer_id,begin_time:begin_time,end_time:end_time}, function(data){
            $('#exportTotaledExcel').prop('disabled', false);
            $('#singlexportTotaledExcel').prop('disabled', false);
            $.scojs_message('生成利润表Excel成功', $.scojs_message.TYPE_OK);
            window.open(data);
        }).fail(function() {
            $('#exportTotaledExcel').prop('disabled', false);
            $('#singlexportTotaledExcel').prop('disabled', false);
            $.scojs_message('生成利润表Excel失败', $.scojs_message.TYPE_ERROR);
        });
    }
	
	 var cssTd=function(){
   	  $("#eeda_table th:eq(6)").css('background-color','#f5f5dc');
   	  $("#eeda_table td:nth-child(6)").css('background-color','#f5f5dc');
   	  $("#eeda_table td:nth-child(8)").css('background-color','#f5f5dc');
   	  $("#eeda_table td:nth-child(9)").css('background-color','#f5f5dc');
   	  $("#eeda_table td:nth-child(10)").css('background-color','#f5f5dc');
     }
      
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
          listTotalMoney(customer,order_export_date_begin_time,order_export_date_end_time);
          var cssTd=function(){
        	  $("#eeda_table th:eq(6)").css('background-color','#f5f5dc');
        	  $("#eeda_table td:nth-child(6)").css('background-color','#f5f5dc');
        	  $("#eeda_table td:nth-child(7)").css('background-color','#f5f5dc');
        	  $("#eeda_table td:nth-child(8)").css('background-color','#f5f5dc');
        	  $("#eeda_table td:nth-child(9)").css('background-color','#f5f5dc');
          }
         
          
          var url = "/tradeProfit/list?customer_id="+customer
				          +"&order_export_date_begin_time="+order_export_date_begin_time
				          +"&order_export_date_end_time="+order_export_date_end_time;
          dataTable.ajax.url(url).load(cssTd);
      };
      listTotalMoney();
  });
});