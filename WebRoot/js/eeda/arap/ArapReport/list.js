define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {

  	 
      var dataTable = eeda.dt({
          id: 'eeda_table',
          colReorder: true,
          paging: true,
          serverSide: true, //不打开会出现排序不对 
          ajax: "/arapReport/list",
          columns: [
      			{ "data": "ORDER_NO", "width": "60px",
			    	  "render": function ( data, type, full, meta ) {
	                      return "<a href='/jobOrder/edit?id="+full.JOBID+"'target='_blank'>"+data+"</a>";
	                  }
	            },
	            { "data": "ORDER_EXPORT_DATE", "width": "100px"},
	            { "data": "CUSTOMER_NAME", "width": "60px"},
	            { "data": "SP_NAME", "width": "60px" },
	            { "data": "ORDER_TYPE", "width": "40px",
	            	 "render":function(data,type,full,meta){
	            			var str="";
	            			if(data=="charge"){
	            				str="应收费用";
	            			}else if(data=="cost"){
	            				str="应付费用";
	            			}
	            			return str;
	            	}	
	            },
	            { "data": "FLAG", "width": "60px"},
	            { "data": "TYPE", "width": "30px"},
	            { "data": "FIN_NAME", "width": "60px"},
	            { "data": "CURRENCY_NAME", "width": "40px" },
	            { "data": "TOTAL_AMOUNT", "width": "40px",
	            	"render":function(data,type,full,meta){
	            		return parseFloat(data).toFixed(2);
	            	}	
	            },
	            { "data": "EXCHANGE_RATE", "width": "60px"},
	            { "data": "CURRENCY_TOTAL_AMOUNT", "width": "50px",
	            	"render":function(data,type,full,meta){
            			return parseFloat(data).toFixed(2);
            		}	
	            },
	            { "data": "EXCHANGE_CURRENCY_NAME", "width": "40px"},
	            { "data": "EXCHANGE_CURRENCY_RATE", "width": "60px"},
	            { "data": "EXCHANGE_TOTAL_AMOUNT", "width": "60px",
	            	"render":function(data,type,full,meta){
            			return parseFloat(data).toFixed(2);
            		}	
	            },
	            { "data": "CHECK_ORDER_NO", "width": "100px",
			    	  "render": function ( data, type, full, meta ) {
			    		  var str="";
			    		  if(!data)
			    			  data='';
	            			if(full.ORDER_TYPE=="charge"){
	            				str="<a href='/chargeCheckOrder/edit?id="+full.CHECK_ORDER_ID+"'target='_blank'>"+data+"</a>";
	            			}else if(full.ORDER_TYPE=="cost"){
	            				str="<a href='/costCheckOrder/edit?id="+full.CHECK_ORDER_ID+"'target='_blank'>"+data+"</a>";
	            			}
	                      return str;
	                  }
	            },
	            { "data": "APPLICATION_ORDER_NO", "width": "100px",
			    	  "render": function ( data, type, full, meta ) {
			    		  var str="";
			    		  if(!data)
			    			  data='';
	            			if(full.ORDER_TYPE=="charge"){
	            				str="<a href='/chargeRequest/edit?id="+full.APPLICATION_ORDER_ID+"'target='_blank'>"+data+"</a>";
	            			}else if(full.ORDER_TYPE=="cost"){
	            				str="<a href='/costRequest/edit?id="+full.APPLICATION_ORDER_ID+"'target='_blank'>"+data+"</a>";
	            			}
	                      return str;
	                  }
	            },
	            { "data": "CREATE_STAMP", "width": "100px"}
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

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
          var order_no = $.trim($("#order_no").val()); 
          var customer = $("#customer").val();
          var fin_item_id = $("#fin_item").val();
          var fin_name = $("#fin_item_input").val();
          var flag = $("#flag").val();
          var order_type = $("#order_type").val();
          var sp = $("#sp").val(); 
          var order_export_date_start_date = $("#order_export_date_begin_time").val();
          var order_export_date_end_date = $("#order_export_date_end_time").val();
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/arapReport/list?order_no="+order_no
			           +"&customer_id="+customer
			           +"&fin_item_id="+fin_item_id
			           +"&fin_name_equals="+fin_name
			           +"&sp_id="+sp
			           +"&order_type="+order_type
			           +"&flag="+flag
			           +"&order_export_date_begin_time="+order_export_date_start_date
		               +"&order_export_date_end_time="+order_export_date_end_date;
		               +"&create_stamp_begin_time="+start_date
		               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
      };
      
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
	      if(selectField=='order_no'||selectField=='fin_name'){
	    	  $("#public_text").val("");
	    	  $("#customer_name").hide();
	    	  $("#single_flag").hide();
	    	  $("#export_date").hide();
	    	  $("#sp_id").hide();
	    	  $("#public_text").show();
	      }
	      if(selectField=='sp_id'){
	    	  $("#public_text").hide();
	    	  $("#customer_name").hide();
	    	  $("#export_date").hide();
	    	  $("#single_flag").hide();
	    	  $("#sp_id").show();
	      }
	      if(selectField=='flag'){
	    	  $("#public_text").hide();
	    	  $("#customer_name").hide();
	    	  $("#export_date").hide();
	    	  $("#sp_id").hide();
	    	  $("#single_flag").show();
	      }
	      if(selectField=="customer_name"){
	    	  $("#single_customer_name_input").val("");
	    	  $("#public_text").hide();
	    	  $("#single_flag").hide();
	    	  $("#export_date").hide();
	    	  $("#sp_id").hide();
	    	  $("#customer_name").show();
	      }
	      if(selectField=="order_export_date"){
	    	  $("#public_text").hide();
	    	  $("#single_flag").hide();
	    	  $("#customer_name").hide();
	    	  $("#sp_id").hide();
	    	  $("#export_date").show();
	      }
     });
	
	$('#singleSearchBtn').click(function(){
	     var selectField = $('#selected_field').val();
	     var selectFieldValue = '';
	     if(selectField=='order_no'||selectField=='fin_name'){
	    	 selectFieldValue = $("#public_text").val();
	     }
	     if(selectField=='sp_id'){
	    	 selectFieldValue = $("#single_sp_id").val();
	      }
	     if(selectField=='flag'){
	    	 selectFieldValue = $("#single_flag").val();
	      }
	     if(selectField=="customer_name"){
	    	 selectFieldValue = $("#single_customer_name_input").val();
	    	 selectFieldValue +="&customer_id="+$("#single_customer_name").val();
	     }
	     if(selectField=="order_export_date"){
	    	  var export_date_start_date = $("#single_export_date_begin_time").val();
	    	  var export_date_end_date = $("#single_export_date_end_time").val();
	     }
	     var url = "/arapReport/list?"+selectField+"="+selectFieldValue
	     		 +"&order_export_date_begin_time="+export_date_start_date
	     		 +"&order_export_date_end_time="+export_date_end_date;
	     dataTable.ajax.url(url).load();
	});
	
	var order = {};
	 //导出excel利润表
    $("#singleExportTotaledExcel").click(function(){
    	$(this).attr('disabled', true);
  	  	var selectField = $('#selected_field').val();
  	  	var selectFieldValue = '';
  	  	if(selectField=='order_no'){
  	  		order.order_no = $("#public_text").val();
  	  	}
  	  	if(selectField=='sp_id'){
	  		order.sp_id = $("#single_sp_id").val();
	  	}
  	  	if(selectField=='fin_name'){
	  		order.fin_item = $("#public_text").val();
	  	}
  	  	if(selectField=='flag'){
  	  		order.flag = $("#single_flag").val();
  	  	}
  	  	if(selectField=="customer_name"){
  	  		order.customer_id = $("#single_customer_name").val();
  	  	}
  	  	if(selectField=="order_export_date"){
  	  		order.order_export_date_begin_time = $("#single_export_date_begin_time").val();
  	  		order.order_export_date_end_time = $("#single_export_date_end_time").val();
  	  	}
  	  	
        excel_method(order);
    })
    
    //导出excel利润表
    $('#exportTotaledExcel').click(function(){
        $(this).attr('disabled', true);
        order.order_no = $("#order_no").val();
        order.customer_id = $("#customer").val();
        order.sp_id = $("#sp").val();
        order.fin_item = $("#fin_item").val();
        order.flag = $("#flag").val();
        order.order_export_date_begin_time = $("#order_export_date_begin_time").val();
        order.order_export_date_end_time = $("#order_export_date_end_time").val();
        excel_method(order);
    });
    
    var excel_method = function(order){
  	  $.post('/arapReport/downloadExcelList',{params:JSON.stringify(order)}, function(data){
            $('#exportTotaledExcel').prop('disabled', false);
            $('#singleExportTotaledExcel').prop('disabled', false);
            $.scojs_message('生成Excel成功', $.scojs_message.TYPE_OK);
            window.open(data);
        }).fail(function() {
            $('#exportTotaledExcel').prop('disabled', false);
            $.scojs_message('生成Excel失败', $.scojs_message.TYPE_ERROR);
        });
    }
  });
});