define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco',  'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {
  	
  	  
      var dataTable = eeda.dt({
          id: 'eeda_table',
          colReorder: true,
          paging: false,
          serverSide: true, //不打开会出现排序不对 
//          ajax: "/transProfitAndPaymentRate/list",
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

            	    	data = (parseFloat(data)).toFixed(2)
	                  if(isNaN(data)){
	                  	data = "";
	                  }
            	    	return data;
				  }
	            },
	            { "data": "CHARGE_USD", "width": "100px"  ,"class":"charge_usd",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
            	    data = (parseFloat(data)).toFixed(2)
	                  if(isNaN(data)){
	                  	data = "";
	                  }
          	    	return data;
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
            	    data = (parseFloat(data)).toFixed(2)
	                  if(isNaN(data)){
	                  	data = "";
	                  }
          	    	return data;
				  }
	            },
	            { "data": "COST_CNY", "width": "100px" ,"class":"cost_cny",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
            	    data = (parseFloat(data)).toFixed(2)
	                  if(isNaN(data)){
	                  	data = "";
	                  }
          	    	return data;
				  }
	            },
	            { "data": "COST_USD", "width": "100px","class":"cost_usd",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
            	    data = (parseFloat(data)).toFixed(2)
	                  if(isNaN(data)){
	                  	data = "";
	                  }
          	    	return data;
				  }
	            },
	            { "data": "COST_JPY", "width": "100px","class":"cost_jpy",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
            	    data = (parseFloat(data)).toFixed(2)
	                  if(isNaN(data)){
	                  	data = "";
	                  }
          	    	return data;
				  }
	            },
	            { "data": "COST_HKD", "width": "100px","class":"cost_hkd",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
            	    data = (parseFloat(data)).toFixed(2)
	                  if(isNaN(data)){
	                  	data = "";
	                  }
          	    	return data;
				  }
	            },
	            { "data": "CHARGE_RMB", "width": "120px","class":"charge_rmb",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
            	    data = (parseFloat(data)).toFixed(2)
	                  if(isNaN(data)){
	                  	data = "";
	                  }
          	    	return data;
				  }
	            },
	            { "data": "COST_RMB", "width": "120px" ,"class":"cost_rmb",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
            	    data = (parseFloat(data)).toFixed(2)
	                  if(isNaN(data)){
	                  	data = "";
	                  }
          	    	return data;
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
	      if(selectField=='sp_id'){
	    	  $("#single_sp_id_input").val("");
	    	  $("#charge_time_show").hide();
	    	  $("#sp_id_show").show();
	      }
	      if(selectField=='charge_time'){
	    	  $("#single_charge_time_begin_time").val("");
	    	  $("#single_charge_time_end_time").val("");
	    	  $("#charge_time_show").show();
	    	  $("#sp_id_show").hide();
	      }
     });
      
      $("#singleSearchBtn").click(function(){
    	  var selectField = $('#selected_field').val();
    	  if(selectField=='sp_id'){
	    	 var sp_id = $("#single_sp_id").val();
	      }
	      if(selectField=='charge_time'){
	    	 var single_charge_time_begin_time = $("#single_charge_time_begin_time").val();
	    	 var single_charge_time_end_time = $("#single_charge_time_end_time").val();
	      }
	      
	      
	    //合计字段
	         $.post('transProfitAndPaymentRate/listTotal',{
	        	 sp_id:sp_id,
	        	 charge_time_begin_time:single_charge_time_begin_time,
	        	 charge_time_end_time:single_charge_time_end_time
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
//	       	 var total=parseFloat(data.TOTAL);
//	  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=abbr]').html('共'+total+'项汇总：');
//	  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_cny]').html("CNY:"+eeda.numFormat(cost_cny,3));
//	  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_usd]').html("USD:"+eeda.numFormat(cost_usd,3));
//	  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_jpy]').html("JPY:"+eeda.numFormat(cost_jpy,3));
//	  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_hkd]').html("HKD:"+eeda.numFormat(cost_hkd,3));
//	  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_rmb]').html("应付折合(CNY):"+eeda.numFormat(total_cost,3));
//	  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncost_cny]').html("CNY:"+eeda.numFormat(uncost_cny,3)).css('color','red');
//	  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncost_usd]').html("USD:"+eeda.numFormat(uncost_usd,3)).css('color','red');
//	  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncost_jpy]').html("JPY:"+eeda.numFormat(uncost_jpy,3)).css('color','red');
//	  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncost_hkd]').html("HKD:"+eeda.numFormat(uncost_hkd,3)).css('color','red');
//	  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncost_rmb]').html("未付折合(CNY):"+eeda.numFormat(total_uncost,3)).css('color','red');
	       	  
	       	  var total_profit=parseFloat(total_cost-total_uncost).toFixed(2);
	       	  if(total_profit<0){
	       		  $('#total_profit').text(total_profit).css('color','red');
	       	  }else(
	       		  $('#total_profit').text(total_profit)
	       	  )

	         });
	      
          var url = "/transProfitAndPaymentRate/list?sp_id="+sp_id
               +"&charge_time_begin_time="+single_charge_time_begin_time
               +"&charge_time_end_time="+single_charge_time_end_time
          dataTable.ajax.url(url).load(cssTd);
      });
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
          var sp_id = $("#sp_id").val(); 
          var charge_time_begin_time = $("#charge_time_begin_time").val();
          var charge_time_end_time = $("#charge_time_end_time").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          
          //合计字段
          $.post('transProfitAndPaymentRate/listTotal',{
        	  sp_id:sp_id,
        	  charge_time_begin_time:charge_time_begin_time,
        	  charge_time_end_time:charge_time_end_time
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
//        	  var total=parseFloat(data.TOTAL);
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=abbr]').html('共'+total+'项汇总：');
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_cny]').html("应收CNY"+eeda.numFormat(charge_cny,3));
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_usd]').html("应收USD"+eeda.numFormat(charge_usd,3));
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_jpy]').html("应收JPY"+eeda.numFormat(charge_jpy,3));
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_hkd]').html("应收HKD"+eeda.numFormat(charge_hkd,3));
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_cny]').html("应付CNY"+eeda.numFormat(total_charge,3));
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_usd]').html("应付USD"+eeda.numFormat(cost_cny,3));
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_jpy]').html("应付JPY"+eeda.numFormat(cost_usd,3));
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_hkd]').html("应付HKD"+eeda.numFormat(cost_jpy,3));
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_rmb]').html("折合应收(RMB)"+eeda.numFormat(cost_hkd,3));
//        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_rmb]').html("折合应付(RMB)"+eeda.numFormat(total_cost,3));
        	  
        	  var total_profit=parseFloat(total_charge-total_cost).toFixed(2);
        	  if(total_profit<0){
        		  $('#total_profit').text(total_profit).css('color','red');
        	  }else(
        		  $('#total_profit').text(total_profit)
        	  )

          });
          
          var url = "/transProfitAndPaymentRate/list?sp_id="+sp_id
				          +"&charge_time_begin_time="+charge_time_begin_time
				          +"&charge_time_end_time="+charge_time_end_time;
          dataTable.ajax.url(url).load(cssTd);
      };
      var cssTd=function(){
    	  $("#eeda_table th:eq(6)").css('background-color','#f5f5dc');
    	  $("#eeda_table td:nth-child(6)").css('background-color','#f5f5dc');
    	  $("#eeda_table td:nth-child(7)").css('background-color','#f5f5dc');
    	  $("#eeda_table td:nth-child(8)").css('background-color','#f5f5dc');
    	  $("#eeda_table td:nth-child(9)").css('background-color','#f5f5dc');
      }
      
      searchData(); 
      
    //导出excel
      $('#exportTotaledExcel1').click(function(){
    	  $(this).attr('disabled', true);
          var sp_id = $("#single_sp_id").val();
          var begin_time = $("#single_charge_time_begin_time").val();
          var end_time = $("#single_charge_time_end_time").val();
          excel_method(sp_id,begin_time,end_time);
      });
      $('#exportTotaledExcel').click(function(){
    	  $(this).attr('disabled', true);
          var sp_id = $("#sp_id").val();
          var begin_time = $("#charge_time_begin_time").val();
          var end_time = $("#charge_time_end_time").val();
          excel_method(sp_id,begin_time,end_time);
      });
      var excel_method = function(sp_id,begin_time,end_time){
		  $.post('/transProfitAndPaymentRate/downloadExcelList',{sp_id:sp_id,begin_time:begin_time,end_time:end_time}, function(data){
	          $('#exportTotaledExcel1').prop('disabled', false);
	          $('#exportTotaledExcel').prop('disabled', false);
	          $('#singlexportTotaledExcel').prop('disabled', false);
	          $.scojs_message('生成应收Excel对账单成功', $.scojs_message.TYPE_OK);
	          window.open(data);
	      }).fail(function() {
	          $('#exportTotaledExcel').prop('disabled', false);
	          $.scojs_message('生成应收Excel对账单失败', $.scojs_message.TYPE_ERROR);
	      });
      }
  });
});