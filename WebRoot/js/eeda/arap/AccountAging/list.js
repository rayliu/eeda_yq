define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco',  'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {
  	  
      eeda.hideSideBar();//打开报表时自动收起左边菜单
      
  	  var cny_total = 0.0;
  	  var hkd_total = 0.0;
  	  var usd_total = 0.0;
  	  var jpy_total = 0.0;
  	  var total_amount = 0.0;
      var dataTable = eeda.dt({
          id: 'eeda_table',
          colReorder: true,
          paging: true,
          pageLength: 200,
          serverSide: false, //不打开会出现排序不对 
          drawCallback: function( settings ) {
        	  calc_amount();
          },
         // ajax: "/accountAging/list",
          columns: [
      			{ "data": "ABBR_NAME", "width": "100px"},
	            { "data": "CURRENCY_NAME" ,"width": "100px"},
	            { "data": "TOTAL_AMOUNT","width": "100px"},
	            { "data": "CURRENCY_TOTAL_AMOUNT","width": "100px"},
	            { "data": "THREE", "width": "100px"},
	            { "data": "SIX", "width": "100px"},
	            { "data": "NINE", "width": "100px",
	               "render":function(data,type,full,meta){
	            	   var str = data;
	            	   if(data>0){
	            		   return '<span style="color:red">'+str+'</span>';
	            	   }
	            	   return str;
	               }	
	            },
	            { "data": "AFTER_NINE", "width": "100px",
	            	"render":function(data,type,full,meta){
		            	   var str = data;
		            	   if(data>0){
		            		   return '<span style="color:red">'+str+'</span>';
		            	   }
		            	   return str;
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
              var RMB_total = $(row.childNodes[3]).text();
              
              total_amount += parseFloat(RMB_total);
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
      		if(isNaN(total_amount)){
      			$('#totalAmountSpan').html("转成人民币金额列，有空值"); 
      		}else{
      			$('#totalAmountSpan').html((Math.round(total_amount*100)/100).toFixed(2)); 
      		}
      		
          }
      };
      
      $('.collapsed').click(function(event) {
          if($('#exportTotaledExcel1').is(':visible')){
            $('#exportTotaledExcel1').hide();
          }else{
            $('#exportTotaledExcel1').show();
          }
      });
      
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
      
      $('#oldSearchBtn').click(function(){
    	$('#cny_totalAmountSpan').html(0);
    	$('#usd_totalAmountSpan').html(0);
    	$('#hkd_totalAmountSpan').html(0);
    	$('#jpy_totalAmountSpan').html(0);
    	$('#totalAmountSpan').html(0); 
        searchData("old"); 
      })

     var searchData=function(type){
    	  if(!type){
    		  type="";
    	  }
    	   
          var sp_id = $("#sp").val(); 
          var service_stamp_between = $("#service_stamp").val();

          var url = "/accountAging/list?sp_id="+sp_id
		               +"&service_stamp_between="+service_stamp_between+"&type="+type;
          dataTable.ajax.url(url).load(false);
      };
      
      //导出excel利润表
      $('#exportTotaledExcel').click(function(){
          excel_method();
      });
      $('#exportTotaledExcel1').click(function(){
          excel_method();
      });
      
      var excel_method = function(){
    	  $(this).attr('disabled', true);
          var sp_id = $("#sp").val();
          if(sp_id==""){
        	  $.post('/accountAging/downloadExcelList', function(data){
                  $('#exportTotaledExcel').prop('disabled', false);
                  $('#singlexportTotaledExcel').prop('disabled', false);
                  $.scojs_message('生成应收Excel对账单成功', $.scojs_message.TYPE_OK);
                  window.open(data);
              }).fail(function() {
                  $('#exportTotaledExcel').prop('disabled', false);
                  $.scojs_message('生成应收Excel对账单失败', $.scojs_message.TYPE_ERROR);
              });
          }else{
        	  $.post('/accountAging/downloadExcelList',{sp_id:sp_id}, function(data){
                  $('#exportTotaledExcel').prop('disabled', false);
                  $('#singlexportTotaledExcel').prop('disabled', false);
                  $.scojs_message('生成应收Excel对账单成功', $.scojs_message.TYPE_OK);
                  window.open(data);
              }).fail(function() {
                  $('#exportTotaledExcel').prop('disabled', false);
                  $.scojs_message('生成应收Excel对账单失败', $.scojs_message.TYPE_ERROR);
              });
          }
    	 
      }
  });
});