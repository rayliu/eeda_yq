define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {

  	  
      var dataTable = eeda.dt({
          id: 'eeda_table',
          colReorder: true,
          paging: true,
          serverSide: true, //不打开会出现排序不对 
          ajax: "/billProfitAndPayment/list",
          drawCallback:function(data){
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(0).html('共'+data.json.recordsFiltered+'项汇总：');
          },
          columns: [
                {"data": "ORDER_NO", 
              	  "render": function ( data, type, full, meta ) {
              		  return "<a href='/jobOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
              	  }
                },
      			{ "data": "ABBR", "width": "120px"},
      			{ "data": "ORDER_EXPORT_DATE", "width": "120px"},
	            { "data": "CHARGE_RMB", "width": "120px",
      				"render":function( data, type, full, meta ){
      					if(data==0){
      						return '';
      					}
      					return data;
      				}
	            },
	            { "data": "COST_RMB", "width": "120px",
      				"render":function( data, type, full, meta ){
      					if(data==0){
      						return '';
      					}
      					return data;
      				}
	            },
	            {
					"render": function(data, type, full, meta) {
						var str = parseFloat(full.CHARGE_RMB - full.COST_RMB).toFixed(2);
						if(str<0){
							return '<span style="color:red" >'+str+'</span>';
						}
						return str;
					}
				},
	            { "data": "CHARGE_TOTAL", "width": "120px",
	            	"render": function(data, type, full, meta) {
	            		var str="";
	            		if(full.COST_RMB!=0){
	            			str = parseFloat(((full.CHARGE_RMB - full.COST_RMB)/full.COST_RMB)*100).toFixed(2);
	            		}
						return str
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
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });
      
      $('#checkboxNegative').click(function(){
    	  searchData(); 
    	  
    	  
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
	      if(selectField=='customer_name'){
	    	  $("#single_export_date_begin_time").val("");
	    	  $("#single_export_date_end_time").val("");
	    	  $("#export_date").hide();
	    	  $("#customer_name").show();
	      }
	      if(selectField=='order_export_date'){
	    	  $("#single_customer_name").val("");
	    	  $("#single_customer_name_input").val("");
	    	  $("#customer_name").hide();
	    	  $("#export_date").show();
	      }
     });
	
	$('#singleSearchBtn').click(function(){
		var selectField = $("#selected_field").val();
		var checked = "";
		var customer = "";
	     if(selectField=='customer_name'){
	    	 customer = $("#single_customer_name").val();
	      }
	      if(selectField=='order_export_date'){
	    	  var single_export_date_begin_time = $("#single_export_date_begin_time").val();
	          var single_export_date_end_time = $("#single_export_date_end_time").val();
	      }
	      listTotalMoney(checked,customer,single_export_date_begin_time,single_export_date_end_time);
	     var url = "/billProfitAndPayment/list?customer_id="+customer
	     		 +"&order_export_date_begin_time="+single_export_date_begin_time
	     		 +"&order_export_date_end_time="+single_export_date_end_time;
	     dataTable.ajax.url(url).load();
	});
      
      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
    	  var checked = '';
    	  if($('#checkboxNegative').prop('checked')==true){
    		  checked = 'Y';
    		  }
          var customer = $("#customer").val(); 
          var order_export_date_begin_time = $("#order_export_date_begin_time").val();
          var order_export_date_end_time = $("#order_export_date_end_time").val();
          listTotalMoney(checked,customer,order_export_date_begin_time,order_export_date_end_time);
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          
          
          var url = "/billProfitAndPayment/list?checked="+checked
          				  +"&customer_id="+customer
				          +"&order_export_date_begin_time="+order_export_date_begin_time
				          +"&order_export_date_end_time="+order_export_date_end_time;
          dataTable.ajax.url(url).load();
          
          
      };
      
    //导出excel利润表
      $("#singleExportTotaledExcel").click(function(){
    	  $(this).attr('disabled', true);
    	  var customer_id = $("#single_customer_name").val();
          var begin_time = $("#single_export_date_begin_time").val();
          var end_time = $("#single_export_date_end_time").val();
          excel_method(customer_id,begin_time,end_time);
      })
      
      //导出excel利润表
      $('#exportTotaledExcel').click(function(){
          $(this).attr('disabled', true);
          var customer_id = $("#customer").val();
          var begin_time = $("#order_export_date_begin_time").val();
          var end_time = $("#order_export_date_end_time").val();
          var checked = '';
    	  if($('#checkboxNegative').prop('checked')==true){
    		  checked = 'Y';
    		  }
          excel_method(customer_id,begin_time,end_time,checked);
      });
      
      var excel_method = function(customer_id,begin_time,end_time,checked){
    	  $.post('/billProfitAndPayment/downloadExcelList',{customer_id:customer_id,begin_time:begin_time,end_time:end_time,checked:checked}, function(data){
              $('#exportTotaledExcel').prop('disabled', false);
              $('#singleExportTotaledExcel').prop('disabled', false);
              $.scojs_message('生成应收Excel对账单成功', $.scojs_message.TYPE_OK);
              window.open(data);
          }).fail(function() {
              $('#exportTotaledExcel').prop('disabled', false);
              $.scojs_message('生成应收Excel对账单失败', $.scojs_message.TYPE_ERROR);
          });
      }
      
      var listTotalMoney = function(checked,customer,order_export_date_begin_time,order_export_date_end_time){
	      //合计字段
         $.post('/billProfitAndPayment/listTotal',{
          checked:checked,
          customer:customer,
       	  order_export_date_begin_time:order_export_date_begin_time,
       	  order_export_date_end_time:order_export_date_end_time
         },function(data){
        	 var charge_total = parseFloat(data.CHARGE_TOTAL).toFixed(2);
        	 var cost_total = parseFloat(data.COST_TOTAL).toFixed(2);
        	 var total_profit = parseFloat(data.TOTAL_PROFIT).toFixed(2);
        	 var total_profit_rate = parseFloat(data.TOTAL_PROFIT_RATE).toFixed(2);
        	 $($('.dataTables_scrollFoot tr')[0]).find('th').eq(3).html("应收(合计):<br>"+eeda.numFormat(charge_total,3));
        	 $($('.dataTables_scrollFoot tr')[0]).find('th').eq(4).html("应付(合计):<br>"+eeda.numFormat(cost_total,3));
        	 $($('.dataTables_scrollFoot tr')[0]).find('th').eq(5).html("利润(合计):<br>"+total_profit);
        	 $($('.dataTables_scrollFoot tr')[0]).find('th').eq(6).html("平均利润率:<br>"+total_profit_rate);
         });
	 }
      
     listTotalMoney();
      
  });
});