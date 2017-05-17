define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '应收应付查询  | '+document.title;
  	  $('#menu_cost').addClass('active').find('ul').removeClass('in');
  	 $("#breadcrumb_li").text('应收应付查询');
  	 
      var dataTable = eeda.dt({
          id: 'eeda_table',
          colReorder: true,
          paging: true,
          serverSide: true, //不打开会出现排序不对 
          ajax: "/transArapReport/list",
          columns: [
      			{ "data": "ORDER_NO", "width": "60px",
			    	  "render": function ( data, type, full, meta ) {
	                      return "<a href='/transJobOrder/edit?id="+full.JOBID+"'target='_blank'>"+data+"</a>";
	                  }
	            },
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
	            				str="<a href='/transChargeCheckOrder/edit?id="+full.CHECK_ORDER_ID+"'target='_blank'>"+data+"</a>";
	            			}else if(full.ORDER_TYPE=="cost"){
	            				str="<a href='/transCostCheckOrder/edit?id="+full.CHECK_ORDER_ID+"'target='_blank'>"+data+"</a>";
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
          var url = "/transArapReport/list?order_no="+order_no
			           +"&customer_id="+customer
			           +"&fin_item_id="+fin_item_id
			           +"&fin_name_equals="+fin_name
			           +"&sp_id="+sp
			           +"&flag="+flag
			           +"&order_export_date_begin_time="+order_export_date_start_date
		               +"&order_export_date_end_time="+order_export_date_end_date;
		               +"&create_stamp_begin_time="+start_date
		               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
      };
  });
});