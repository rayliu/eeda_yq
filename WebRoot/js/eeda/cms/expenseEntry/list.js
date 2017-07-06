define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','sco','validate_cn'], function ($, metisMenu) { 

    $(document).ready(function() {
    	
    	//datatable, 动态处理
		var cnames = [];
		var itemIds=[];
        var totalAmount = 0.0;
        
        var dataTable = eeda.dt({
            id: 'uncheckedEeda-table',
             paging: true,
            serverSide: false,
            ajax: "/expenseEntry/list",
            columns:[
			      { "width": "10px", "orderable": false,
				    "render": function ( data, type, full, meta ) {
			            var strcheck='<input type="checkbox" class="checkBox" name="order_check_box" value="'+full.ORDER_ID+'">';
			        	for(var i=0;i<itemIds.length;i++){
	                         if(itemIds[i]==full.ID){
	                        	 strcheck= '<input type="checkbox" class="checkBox" checked="checked"  name="order_check_box" value="'+full.ORDER_ID+'">';
	                         }
	                     }
			        	return strcheck;
				    }
			      },
	            { "data": "TRACKING_NO", "width": "100px",
			    	  "render": function ( data, type, full, meta ) {
	                      return "<a href='/customPlanOrder/edit?id="+full.ORDER_ID+"' target='_blank'>"+data+"</a>";
	                  }
	            },
	            { "data": "CUSTOMER_NAME", "width": "100px"},
	            { "data": "ORDER_NO", "width": "100px"},
	            { "width": "180px",
	            	"render": function ( data, type, full, meta ) {
			            var strcheck='';
			            if(full.CHARGE_MSG) {strcheck+='<span style="color:red">应收：</span><br/>'+full.CHARGE_MSG};
			            if(full.COST_MSG) {strcheck+='<br/><span style="color:red">应付：</span><br/>'+full.COST_MSG};
			        	return strcheck;
			        }
	        	},
	          { "data": "CREATE_STAMP", "width": "100px"},
	          { "data": "COST_MSG", "width": "100px","visible":false},
	          { "data": "CHARGE_MSG", "width": "100px","visible":false}
	          ]
	      });


  
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      });

     var searchData=function(){
    	 
    	 
          var order_no = $("#order_no").val().trim(); 
          var customer_name = $('#customer_input').val().trim();
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          var tracking_no= $("#tracking_no").val().trim(); 
          var url = "/expenseEntry/list?order_no="+order_no
               +"&customer_name="+customer_name
               +"&tracking_no="+tracking_no
	           +"&create_stamp_begin_time="+start_date
	           +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
        }
    });
});