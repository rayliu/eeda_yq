define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn',  'dtColReorder','sco' ], function ($, metisMenu) {
$(document).ready(function() {

    
    //datatable, 动态处理
	var dataTable = eeda.dt({
	    id: 'truck_table',
        colReorder: true,
        paging: true,
	    serverSide: true, //不打开会出现排序不对
	    ajax: "/truckOrder/list",
	    columns:[
	            { "data": "ORDER_NO","width":"180px",
	                "render": function ( data, type, full, meta ) {
	                    return "<a href='/truckOrder/edit?id="+full.ID+"&order_no="+full.ORDER_NO+"&order_id="+full.ORDER_ID+" 'target='_blank'>"+data+"</a>";
	                }
	            },
	            { "data": "CREATE_STAMP","width":"180px"}, 
	            { "data": "STATUS","width":"180px"}, 
	            { "data": "UNLOAD_TYPE","width":"180px"}, 
	            { "data": "TRANSPORT_COMPANY","width":"180px"}, 
	            { "data": "DRIVER","width":"180px"}, 
	            { "data": "DRIVER_TEL","width":"180px"}, 
	            { "data": "TRUCK_TYPE","width":"180px"},
	            { "data": "CAR_NO","width":"180px"},
	            { "data": "CONSIGNOR","width":"180px"},
	            { "data": "CONSIGNOR_PHONE","width":"180px"},
	            { "data": "TAKE_ADDRESS","width":"180px"},
	            { "data": "CONSIGNEE","width":"180px"},
	            { "data": "CONSIGNEE_PHONE","width":"180px"},
	            { "data": "DELIVERY_ADDRESS","width":"180px"},
	            { "data": "ETA","width":"180px"},
	            { "data": "CARGO_INFO","width":"180px"},
	            { "data": "REQUIRED_TIME_REMARK","width":"180px"},
	            { "data": "SIGN_DESC","width":"180px"},
	            { "data": "SIGN_STATUS","width":"180px"}
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
	      if(selectField=='order_no'){
	    	  $("#single_order_no").val("");
	    	  $("#create_stamp_show").hide();
	    	  $("#single_status").hide();
	    	  $("#single_order_no").show();
	      }
	      if(selectField=='status'){
	    	  $("#single_order_no").hide();
	    	  $("#create_stamp_show").hide();
	    	  $("#single_status").show();
	      }
	      if(selectField=="create_stamp"){
	    	  $("#single_order_no").hide();
	    	  $("#single_status").hide();
	    	  $("#create_stamp_show").show();
	      }
     });
	
	$('#singleSearchBtn').click(function(){
	     var selectField = $('#selected_field').val();
	     if(selectField=='order_no'){
	    	 var order_no = $("#single_order_no").val();
	     }
	     if(selectField=='status'){
	    	 var status = $("#single_status").val();
	      }
	     if(selectField=="create_stamp"){
	    	var start_date = $("#single_create_stamp_begin_time").val();
	    	var end_date = $("#single_create_stamp_end_time").val();
	      }
	     var url = "/truckOrder/list?order_no="+order_no
	 				+"&status="+status
	 				+"&create_stamp_begin_time="+start_date
	 				+"&create_stamp_end_time="+end_date;
	     dataTable.ajax.url(url).load();
	}); 
    
    $('#resetBtn').click(function(e){
        $("#orderForm")[0].reset();
    });

    $('#searchBtn').click(function(){
        searchData(); 
    })

   var searchData=function(){
        var order_no = $.trim($("#order_no").val()); 
        var status = $("#status").val(); 
        var start_date = $("#create_stamp_begin_time").val();
        var end_date = $("#create_stamp_end_time").val();
        
        /*  
            查询规则：参数对应DB字段名
            *_no like
            *_id =
            *_status =
            时间字段需成双定义  *_begin_time *_end_time   between
        */
        var url = "/truckOrder/list?order_no="+order_no
        	 		+"&status="+status
        	 		+"&create_stamp_begin_time="+start_date
        	 		+"&create_stamp_end_time="+end_date;

        dataTable.ajax.url(url).load();
    };
});

});