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
      			{ "data": "ABBR", "width": "100px","class":"abbr"},
	            { "data": "CHARGE_CNY", "width": "80px","class":"charge_cny",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            { "data": "CHARGE_USD", "width": "100px"  ,"class":"charge_usd",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            { "data": "CHARGE_JPY", "width": "100px","class":"charge_jpy",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            { "data": "CHARGE_HKD", "width": "100px","class":"charge_hkd",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            { "data": "CHARGE_RMB", "width": "120px","class":"charge_rmb",
	            	"render": function(data, type, full, meta) {
            	    if(!data){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            { "data": "COST_CNY", "width": "100px" ,"class":"cost_cny",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            { "data": "COST_USD", "width": "100px","class":"cost_usd",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            { "data": "COST_JPY", "width": "100px","class":"cost_jpy",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            { "data": "COST_HKD", "width": "100px","class":"cost_hkd",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            { "data": "COST_RMB", "width": "120px" ,"class":"cost_rmb",
	            	"render": function(data, type, full, meta) {
            	    if(!data){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            }]
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
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

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
          $.post('profitAndPaymentRate/listTotal',{
        	  sp_id:sp_id,
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
        	  var total=parseFloat(data.TOTAL);
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=abbr]').html('共'+total+'项汇总：');
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_cny]').html("应收CNY:<br>"+eeda.numFormat(charge_cny,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_usd]').html("应收USD:<br>"+eeda.numFormat(charge_usd,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_jpy]').html("应收JPY:<br>"+eeda.numFormat(charge_jpy,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_hkd]').html("应收HKD:<br>"+eeda.numFormat(charge_hkd,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_cny]').html("应付CNY:<br>"+eeda.numFormat(cost_cny,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_usd]').html("应付USD:<br>"+eeda.numFormat(cost_usd,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_jpy]').html("应付JPY:<br>"+eeda.numFormat(cost_jpy,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_hkd]').html("应付HKD:<br>"+eeda.numFormat(cost_hkd,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_rmb]').html("折合应收(RMB):<br>"+eeda.numFormat(total_charge,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_rmb]').html("折合应付(RMB):<br>"+eeda.numFormat(total_cost,3));
        	  
        	  var total_profit=parseFloat(total_charge-total_cost).toFixed(2);
        	  if(total_profit<0){
        		  $('#total_profit').text(total_profit).css('color','red');
        	  }else(
        		  $('#total_profit').text(total_profit)
        	  )

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
         
          
          var url = "/profitAndPaymentRate/list?sp_id="+sp_id
				          +"&order_export_date_begin_time="+order_export_date_begin_time
				          +"&order_export_date_end_time="+order_export_date_end_time;
          dataTable.ajax.url(url).load(tableStyle);
          
         
          
      };
      searchData(); 
  });
});