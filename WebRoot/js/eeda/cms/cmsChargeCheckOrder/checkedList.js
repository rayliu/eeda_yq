define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','sco','validate_cn'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '应收对账单查询 | '+document.title;


    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-tableChecked',
            paging: true,
            scrollX:false,
            serverSide: true, //不打开会出现排序不对
            ajax: "/cmsChargeCheckOrder/checkedList",
            columns:[	                
					  { "data": "ORDER_NO", "width": "80px",
			            	 "render": function ( data, type, full, meta ) {
			           		  return "<a href='/cmsChargeCheckOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
			           	  }
			          },
					  
					  { "data": "REAL_STATUS", "width": "60px"},
					  { "data": "SP_NAME", "width": "120px"}, 
					  { "data": "CHECK_AMOUNT","width": "60px",
				    	 "render":function(data, type, full, meta){
				    		 if(data<0){
				    			 return '<span style="color:red">'+data+'</span>';
				    		 }else{
				    			 return data;
				    		 }
				    	 }
					  },
					  { "data": "CREATOR_NAME", "width": "80px"}, 
					  { "data": "CREATE_STAMP", "width": "80px"}, 
					  { "data": "CONFIRM_NAME", "width": "80px"}, 
					  { "data": "CONFIRM_STAMP", "width": "80px"}
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
  	      if(selectField=='order_no'){
  	    	  $("#public_text").val("");
  	    	  $("#single_sp1_id_input").hide();
  	    	  $("#single_create_stamp1_show").hide();
  	    	  $("#public_text").show();
  	      }
  	      if(selectField=='sp_name'){
  	    	  $("#single_sp1_id_input").val("");
  	    	  $("#single_create_stamp1_show").hide();
  	    	  $("#public_text").hide();
  	    	  $("#single_sp1_id_input").show();
  	      }
  	      if(selectField=='create_stamp'){
  	    	  $("#single_create_stamp1_begin_time").val("");
  	    	  $("#single_create_stamp1_end_time").val("");
  	    	  $("#public_text").hide();
  	    	  $("#single_sp1_id_input").hide();
  	    	  $("#single_create_stamp1_show").show();
  	      }
       });
        
        $("#singleSearchBtn").click(function(){
      	  var selectField = $('#selected_field').val();
      	  var selectValue = "";
	      if(selectField=='order_no'){
	    	  selectValue = $("#public_text").val();
	      }
	      if(selectField=='sp_name'){
	    	  selectValue = $("#single_sp1_id_input").val();
	      }
	      if(selectField=='create_stamp'){
	    	  var create_stamp1_begin = $("#single_create_stamp1_begin_time").val();
	    	  var create_stamp1_end = $("#single_create_stamp1_end_time").val();
	      }
  	      
  	      //增加出口日期查询
            var url = "/cmsChargeCheckOrder/checkedList?"+selectField+"="+selectValue
                 +"&create_stamp_begin_time="+create_stamp1_begin
                 +"&create_stamp_end_time="+create_stamp1_end;
            dataTable.ajax.url(url).load();
        });  
        
      $('#resetOrderBtn').click(function(e){
          $("#orderSearchForm")[0].reset();
      });

      $('#searchOrderBtn').click(function(){
          searchData(); 
      });

     var searchData=function(){
          var order_no = $("#order_no1").val();
          var sp_name = $('#sp1_input').val().trim();
          var start_date = $("#create_stamp1_begin_time").val();
          var end_date = $("#create_stamp1_end_time").val();
          

          var url = "/cmsChargeCheckOrder/checkedList?order_no="+order_no
               +"&sp_name="+sp_name
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
       }
       
    });
});