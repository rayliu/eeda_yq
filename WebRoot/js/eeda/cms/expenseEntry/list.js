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
	          { "data": "DATE_CUSTOM", "width": "100px"},
	          { "data": "COST_MSG", "width": "100px","visible":false},
	          { "data": "CHARGE_MSG", "width": "100px","visible":false}
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
  	      if(selectField=='customer_name'){
  	    	  $("#public_text").val("");
	  	      $("#single_date_custom_begin_time").val("");
	  	      $("#single_date_custom_end_time").val("");
  	    	  $("#date_custom_show").hide();
  	    	  $("#public_text").hide();
  	    	  $("#customer_id_show").show();
  	      }
  	      if(selectField=='order_no'||selectField=='tracking_no'){
  	    	  $("#single_customer_id_input").val("");
	  	      $("#single_date_custom_begin_time").val("");
	  	      $("#single_date_custom_end_time").val("");
  	    	  $("#date_custom_show").hide();
	    	  $("#customer_id_show").hide();
	    	  $("#public_text").show();
  	      }
  	      if(selectField=="date_custom"){
	  	      $("#public_text").val("");
	  	      $("#single_customer_id_input").val("");
	    	  $("#public_text").hide();
	    	  $("#customer_id_show").hide();
	    	  $("#date_custom_show").show();
  	      }
       });
        
        var tableStyle = function(){
      	  $('.oneRow').css('line-height','30px');
      	  $('.doubleRow').css('text-align','center');
      	  
      	  var tableName = "eeda_table";
      	  //格式【合成表头的第一列位置，合成的列数，颜色】
      	  var array= [[2,5,'#f8fff0'],[7,5,'#eeffff']];
      	  for (var i = 0; i < array.length; i++) {
      		  var firstChild = array[i][0];
          	  var cols = array[i][1];
          	  var bgColor = array[i][2];
          	  for (var j = firstChild; j < (firstChild+cols); j++) {
          		  $("#"+tableName+" td:nth-child("+j+")").css('background-color',bgColor);
          	  }
  		  }
        }
        
    	$('#singleSearchBtn').click(function(){
   	     var selectField = $('#selected_field').val();
   	     var selectValue = "";
   	     if(selectField=='customer_name'){
   	    	 selectValue = $("#single_customer_id_input").val();
   	      }
   	      if(selectField=='order_no'||selectField=='tracking_no'){
   	    	 selectValue = $("#public_text").val();
   	      }
   	      if(selectField=="date_custom"){
   	    	 var begin_time = $("#single_date_custom_begin_time").val();
   	    	 var end_time = $("#single_date_custom_end_time").val();
   	      }
   	   var url = "/expenseEntry/list?"+selectField+"="+selectValue
       +"&date_custom_begin_time="+begin_time
       +"&date_custom_end_time="+end_time;
   	   	dataTable.ajax.url(url).load();
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
          var begin_time = $("#date_custom_begin_time").val();
          var end_time = $("#date_custom_end_time").val();
          var tracking_no= $("#tracking_no").val().trim(); 
          var url = "/expenseEntry/list?order_no="+order_no
               +"&customer_name="+customer_name
               +"&tracking_no="+tracking_no
	           +"&date_custom_begin_time="+begin_time
	           +"&date_custom_end_time="+end_time;

          dataTable.ajax.url(url).load();
        }
    });
});