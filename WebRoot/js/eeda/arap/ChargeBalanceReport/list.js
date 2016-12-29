define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '客户应收余额汇总表  | '+document.title;
  	  $('#menu_cost').addClass('active').find('ul').removeClass('in');
  	  
      var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: true,
          serverSide: true, //不打开会出现排序不对 
          ajax: "/chargeBalanceReport/list",
          columns: [
      			{ "data": "ORDER_NO", "width": "100px",
			    	  "render": function ( data, type, full, meta ) {
	                      return "<a href='/jobOrder/edit?id="+full.JOBID+"'target='_blank'>"+data+"</a>";
	                  }
	            },
	            { "data": "CREATE_STAMP", "width": "100px"},
	            { "data": "CUSTOMER_NAME", "width": "100px"},
	            { "data": "BILL_FLAG", "width": "60px",
	                "render": function ( data, type, full, meta ) {
	                		if(data){
	      	            		if(data != 'Y')
	      				    		    return '未创建对账单';
	      				    	   else 
	      				    		  return '已创建对账单';
	                  	}else{
	                			return '';
	                	}
	    			}
	            },
	            { "data": "TYPE", "width": "60px"},
	            { "data": "CURRENCY_NAME", "width": "60px" },
	            
	          ]
	      });

      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
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
          var url = "/costCheckOrder/list?customer_id="+customer
		               +"&service_stamp_between="+service_stamp_between;
          dataTable.ajax.url(url).load();
      };
  });
});