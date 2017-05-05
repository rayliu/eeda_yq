define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '单据状态  | '+document.title;
  	
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
          paging: true,
          serverSide: true, //不打开会出现排序不对
          ajax: "/orderStatus/list",
          columns: [
              { "data": "PLAN_ORDER_NO"},
              { "data": "JOB_ORDER_NO",
            	  "render": function ( data, type, full, meta ) {
                  	return orderUrl(data);
                  }
              },
              { "data": "CUSTOMER_NAME"}, 
              { "data": "CHARGE_CHECK_NO",
            	  "render": function ( data, type, full, meta ) {
                    	return orderUrl(data);
                    }
                },
              { "data": "COST_CHECK_NO",
              	  "render": function ( data, type, full, meta ) {
                    	return orderUrl(data);
                    }
                },
              { "data": "CHARGE_APP_NO",
              	  "render": function ( data, type, full, meta ) {
                    	return orderUrl(data);
                    }
                },
              { "data": "COST_APP_NO",
              	  "render": function ( data, type, full, meta ) {
                    	return orderUrl(data);
                    }
                }
          ]
      });

      var orderUrl = function(data){
    	  var array = [];
    	  var re = "";
    	  if(data != null){
    		  array = data.split('<br/>');
        	  for (var i = 0; i < array.length; i++) {
        		  var id = array[i].substring(0, array[i].indexOf(':'));
            	  var order_no = array[i].substring((array[i].indexOf(':')+1),array[i].indexOf('-'));
            	  var status = array[i].substring(array[i].indexOf('-'),50);
            	  re += eeda.getUrlByNo(id,order_no)+status+'<br/>';
			  }
    	  }
    	  return re;
      }
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
          
          var customer_id = $("#customer_id").val();
          var plan_order = $.trim($("#plan_order").val()); 
          var job_order = $.trim($("#job_order").val()); 
          var charge_check = $.trim($("#charge_check").val()); 
          var cost_check = $.trim($("#cost_check").val()); 
          var charge_invoice = $.trim($("#charge_invoice").val()); 
          var charge_app = $.trim($("#charge_app").val()); 
          var cost_app = $.trim($("#cost_app").val()); 
          
          //增加出口日期查询
          var url = "/orderStatus/list?"
        	    + "customer_id="+customer_id
          	    +"&plan_order_no="+plan_order
          	    +"&job_order_no="+job_order
	          	+"&charge_check_no="+charge_check
	          	+"&cost_check_no="+cost_check
	          	+"&charge_invoice_no="+charge_invoice
	          	+"&charge_app_no="+charge_app
	          	+"&cost_app_no="+cost_app;

          dataTable.ajax.url(url).load();
      };
      
 

  });
});