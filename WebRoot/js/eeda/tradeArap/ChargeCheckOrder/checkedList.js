define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','sco','validate_cn'], function ($, metisMenu) { 
    $(document).ready(function() {
    	$('.search_single input,.search_single select').on('input',function(){
    		  $("#orderSearchForm")[0].reset();
    	  });

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-tableChecked',
            paging: true,
            scrollX:false,
            serverSide: true, //不打开会出现排序不对
            ajax: "/tradeChargeCheckOrder/checkedList",
            columns:[	                
					  { "data": "ORDER_NO", "width": "100px",
			            	 "render": function ( data, type, full, meta ) {
			           		  return "<a href='/tradeChargeCheckOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
			           	  }
			          },
					  { "data": "CREATE_STAMP", "width": "100px"},  
					  { "data": "TOSTATUS", "width": "100px",
						  "render":function(data, type, full, meta){
					    		 if(data=="已退单"){
					    			 return '<span style="color:red">'+data+'</span>';
					    		 }else{
					    			 return data;
					    		 }
					    	 }
					  },
					  { "data": "SP_NAME", "width": "100px"}, 
					  { "data": "TOTAL_AMOUNT","width": "60px","visible":false,
				    	 "render":function(data, type, full, meta){
				    		 if(data<0){
				    			 return '<span style="color:red">'+eeda.numFormat(parseFloat(data).toFixed(2),3)+'</span>';
				    		 }else{
				    			 return data;
				    		 }
				    	 }
					  },
			          { "data": "CNY","width": "80px",
						  "render":function(data,type,full,meta){
							  var usd_str=eeda.numFormat(parseFloat(data).toFixed(2),3);
							  return usd_str;
						  }
					  },
					  { "data": "USD","width": "80px",
						  "render":function(data,type,full,meta){
							  var usd_str=eeda.numFormat(parseFloat(data).toFixed(2),3);
							  return usd_str;
						  }
					  },
			          { "data": "JPY","width": "80px",
						  "render":function(data,type,full,meta){
							  var usd_str=eeda.numFormat(parseFloat(data).toFixed(2),3);
							  return usd_str;
						  }
					  },
			          { "data": "HKD","width": "80px",
						  "render":function(data,type,full,meta){
							  var usd_str=eeda.numFormat(parseFloat(data).toFixed(2),3);
							  return usd_str;
						  }
					  },
					  { "data": "RETURN_REASON", "width": "120px"}
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
  	    	$("#single_order_no1").val("");
  	    	  $("#sp_name_show").hide();
  	    	  $("#single_status").hide();
  	    	  $("#create_stamp1_show").hide();
  	    	  $("#single_order_no1").show();
  	      }
  	      if(selectField=='sp_name'){
  	    	$("#sp_name_show").val("");
  	    	  $("#single_order_no1").hide();
	    	  $("#single_status").hide();
	    	  $("#create_stamp1_show").hide();
	    	  $("#sp_name_show").show();
  	      }
  	      if(selectField=="toStatus_equals"){
  	    	$("#single_status").val("");
  	    	  $("#single_order_no1").hide();
	    	  $("#create_stamp1_show").hide();
	    	  $("#sp_name_show").hide();
	    	  $("#single_status").show();
  	      }
  	      if(selectField=="create_stamp"){
  	    	  $("#single_order_no1").hide();
	    	  $("#sp_name_show").hide();
	    	  $("#single_status").hide();
	    	  $("#create_stamp1_show").show();
  	      }
       });
  	
  	$('#singleSearchBtn').click(function(){
  		$("#orderSearchForm")[0].reset();
  	    var selectField = $('#selected_field').val();
  	    if(selectField=='order_no'){
  	    	$("#order_no1").val($("#single_order_no1").val());
  	    }
  	    if(selectField=='sp_name'){
  	    	$("#sp1").val($("#single_sp_name").val());
  	    	$("#sp1_input").val($("#single_sp_name_input").val());
  	    }
  	    if(selectField=="toStatus_equals"){
  	    	$("#status").val($("#single_status").val());
  	    }
  	    if(selectField=="create_stamp"){
		  $("#create_stamp1_begin_time").val($("#single_create_stamp1_begin_time").val());
		  $("#create_stamp1_end_time").val($("#single_create_stamp1_end_time").val());
  	    }
  	  $('#searchBtn1').click();
  	});
   	           
      $('#resetBtn1').click(function(e){
          $("#orderSearchForm")[0].reset();
      });

      $('#searchBtn1').click(function(){
          searchData(); 
      });

     var searchData=function(){
          var order_no = $("#order_no1").val();
          var sp_name = $('#sp1_input').val().trim();
          var start_date = $("#create_stamp1_begin_time").val();
          var end_date = $("#create_stamp1_end_time").val();
          var toStatus = $("#status").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/tradeChargeCheckOrder/checkedList?order_no="+order_no
               +"&sp_name="+sp_name
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date
               +"&toStatus_equals="+toStatus;

          dataTable.ajax.url(url).load();
       }
       
    });
});