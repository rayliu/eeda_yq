define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco',  'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {
      eeda.hideSideBar();//打开报表时自动收起左边菜单
      
      var windowHeight = $(window).height();        //获取浏览器窗口高度
      var headerHeight =  $("#eeda_table").offset().top;//判断是否到达窗口顶部
      var page = windowHeight - headerHeight-80;
      $('.paDiv').css("height",windowHeight - headerHeight);

      var dataTable = eeda.dt({
          id: 'eeda_table',
          colReorder: true,
          paging: false,
          scrollY: page,
          serverSide: false, //不打开会出现排序不对 
//          ajax: "/profitAndPaymentRate/list",
          initComplete:function(settings){

          },
          columns: [
      			{ "data": "DATE_TIME","width": "120px", 
                	"render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                },
      			
	            { "data": "CHARGE_RMB", "width": "120px", 
	            	"render": function(data, type, full, meta) {
            	    if(!data){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            { "data": "COST_RMB", "width": "120px", 
	            	"render": function(data, type, full, meta) {
                  	    if(!data){
                  	    	return '';
                  	    }
      					 return eeda.numFormat(data.toFixed(2),3);
      				  }
	            },
	            { "width": "120px",
      					"render": function(data, type, full, meta) {
      	            	    var profit = parseFloat(full.CHARGE_RMB - full.COST_RMB).toFixed(2);
      	            	    if(profit<0){
      	            	    	return '<span style="color:red;width:120px">'+eeda.numFormat(profit,3)+'</span>';
      	            	    }
      						return eeda.numFormat(profit,3);
      					}
      			},
      		    { "width": "120px",
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
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
          var customer = $("#customer").val(); 
          var customer_name = $("#customer_input").val(); 
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
          $.post('totalProfit/listTotal',{
        	  customer:customer,
        	  abbr_like:customer_name,
        	  order_export_date_begin_time:order_export_date_begin_time,
        	  order_export_date_end_time:order_export_date_end_time
          },function(data){
        	  var total_charge = parseFloat(data.TOTAL_CHARGE).toFixed(2);
        	  var total_cost = parseFloat(data.TOTAL_COST).toFixed(2);
        	 
        	  $('#total_charge').text(total_charge);
        	  $('#total_cost').text(total_cost);
        	  
        	  
        	  var total_profit=parseFloat(total_charge-total_cost).toFixed(2);
        	  var average_profit_rate = parseFloat((total_profit/total_cost)*100).toFixed(2);
        	  if(!average_profit_rate){
        		  average_profit_rate=0.00;
        	  }
        	  if(total_profit<0){
        		  $('#total_profit').text(total_profit).css('color','red');
        	  }else(
        		  $('#total_profit').text(total_profit)
        	  )
        	  var total=parseFloat(data.TOTAL);
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(0).html('共'+total+'项汇总：');
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(1).html("折合应收(CNY):<br>"+eeda.numFormat(total_charge,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(2).html("折合应付(CNY):<br>"+eeda.numFormat(total_cost,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(4).html("平均利润率(%):<br>"+average_profit_rate);
        	  if(total_profit<0){
        		  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(3).html("利润(CNY):<br>"+eeda.numFormat(total_profit,3)).css('color','red');
        	  }else(
        		$($('.dataTables_scrollFoot tr')[0]).find('th').eq(3).html("利润(CNY):<br>"+eeda.numFormat(total_profit,3))
        	  )

          });
          
          var url = "/totalProfit/list?customer="+customer
         				  +"&abbr_like="+customer_name
				          +"&order_export_date_begin_time="+order_export_date_begin_time
				          +"&order_export_date_end_time="+order_export_date_end_time;
          dataTable.ajax.url(url).load();
          
         
          
      };
      //searchData();
      
      var cssTd=function(){
    	  $("#eeda_table th:eq(6)").css('background-color','#f5f5dc');
    	  $("#eeda_table td:nth-child(6)").css('background-color','#f5f5dc');
    	  $("#eeda_table td:nth-child(8)").css('background-color','#f5f5dc');
    	  $("#eeda_table td:nth-child(9)").css('background-color','#f5f5dc');
    	  $("#eeda_table td:nth-child(10)").css('background-color','#f5f5dc');
      }
      
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
    	  $.post('/totalProfit/downloadExcelList',{customer:customer_id,begin_time:begin_time,end_time:end_time}, function(data){
              $('#exportTotaledExcel').prop('disabled', false);
              $('#singlexportTotaledExcel').prop('disabled', false);
              $.scojs_message('导出利润表excel文件成功', $.scojs_message.TYPE_OK);
              window.open(data);
          }).fail(function() {
              $('#exportTotaledExcel').prop('disabled', false);
              $.scojs_message('导出利润表excel文件失败', $.scojs_message.TYPE_ERROR);
          });
      }
  });
});