define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '客户应收账龄分析表  | '+document.title;
  	  $('#menu_cost').addClass('active').find('ul').removeClass('in');
  	  
  	  var cny_total = 0.0;
  	  var hkd_total = 0.0;
  	  var usd_total = 0.0;
  	  var jpy_total = 0.0;
  	  var total_amount = 0.0;
      var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: true,
          serverSide: true, //不打开会出现排序不对 
          drawCallback: function( settings ) {
        	  calc_amount();
          },
          ajax: "/accountAging/list",
          columns: [
      			{ "data": "ABBR_NAME", "width": "100px"},
	            { "data": "CURRENCY_NAME","class":"currency_name", "width": "100px"},
	            { "data": "TOTAL_AMOUNT","class":"total_amount", "width": "100px"},
	            { "data": "THREE", "width": "100px"},
	            { "data": "SIX", "width": "100px"},
	            { "data": "NINE", "width": "100px"}
	          ]
	  });
      
      var calc_amount = function(){
    	  cny_total = 0.0;
    	  hkd_total = 0.0;
    	  usd_total = 0.0;
    	  jpy_total = 0.0;
    	  total_amount = 0.0;
          var item_table_rows = $("#eeda_table tr");
          var items_array=[];
          for(var index=0; index<item_table_rows.length; index++){
              if(index==0)
                  continue;

              var row = item_table_rows[index];
              var empty = $(row).find('.dataTables_empty').text();
              if(empty)
              	continue;

              var currency_name = $(row.childNodes[1]).text();
              var data = $(row.childNodes[2]).text();
              if(currency_name=='CNY'){
      			  cny_total += parseFloat(data);
      		  }
      		  else if(currency_name=='HKD'){
      			  hkd_total += parseFloat(data);
      		  }
          	  else if(currency_name=='USD'){
          		  usd_total += parseFloat(data);
          	  }
          	  else if(currency_name=='JPY'){
          		  jpy_total += parseFloat(data);
          	  }
      		
      		$('#cny_totalAmountSpan').html((Math.round(cny_total*100)/100).toFixed(2));
      		$('#usd_totalAmountSpan').html((Math.round(usd_total*100)/100).toFixed(2));
      		$('#hkd_totalAmountSpan').html((Math.round(hkd_total*100)/100).toFixed(2));
      		$('#jpy_totalAmountSpan').html((Math.round(jpy_total*100)/100).toFixed(2));
      		$('#totalAmountSpan').html((Math.round(total_amount*100)/100).toFixed(2)); 
          }
      };

      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
    	$('#cny_totalAmountSpan').html(0);
    	$('#usd_totalAmountSpan').html(0);
    	$('#hkd_totalAmountSpan').html(0);
    	$('#jpy_totalAmountSpan').html(0);
    	$('#totalAmountSpan').html(0); 
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
          var url = "/accountAging/list?sp_id="+customer
		               +"&service_stamp_between="+service_stamp_between;
          dataTable.ajax.url(url).load(false);
      };
  });
});