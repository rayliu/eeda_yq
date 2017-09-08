define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'dtColReorder'], function ($, metisMenu) {
	$(document).ready(function() {
		var dataTable = eeda.dt({
	          id: 'eeda_table',
	          colReorder: true,
	          paging: true,
	          serverSide: true, //不打开会出现排序不对 
	          ajax: "/tradeBillProfitAndPayment/list",
	          columns: [
	                {"data": "ORDER_NO", 
	              	  "render": function ( data, type, full, meta ) {
	              		  return "<a href='/trJobOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
	              	  }
	                },
	      			{ "data": "ABBR", "width": "120px"},
	      			{ "data": "ORDER_EXPORT_DATE", "width": "120px"},
		            { "data": "CHARGE_RMB", "width": "120px"},
		            { "data": "COST_RMB", "width": "120px"  },
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
		     if(selectField=='customer_name'){
		    	  var customer_name = $("#single_customer_name").val();
		      }
		      if(selectField=='order_export_date'){
		    	  var single_export_date_begin_time = $("#single_export_date_begin_time").val();
		          var single_export_date_end_time = $("#single_export_date_end_time").val();
		      }
		     var url = "/tradeBillProfitAndPayment/list?customer_id="+customer_name
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
	          /*  
	              查询规则：参数对应DB字段名
	              *_no like
	              *_id =
	              *_status =
	              时间字段需成双定义  *_begin_time *_end_time   between
	          */
	          
	          
	          var url = "/tradeBillProfitAndPayment/list?checked="+checked
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
	    	  $.post('/tradeBillProfitAndPayment/downloadExcelList',{customer_id:customer_id,begin_time:begin_time,end_time:end_time,checked:checked}, function(data){
	              $('#exportTotaledExcel').prop('disabled', false);
	              $('#singleExportTotaledExcel').prop('disabled', false);
	              $.scojs_message('生成应收Excel对账单成功', $.scojs_message.TYPE_OK);
	              window.open(data);
	          }).fail(function() {
	              $('#exportTotaledExcel').prop('disabled', false);
	              $.scojs_message('生成应收Excel对账单失败', $.scojs_message.TYPE_ERROR);
	          });
	      }
	});
});