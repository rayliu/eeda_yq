define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','sco','validate_cn'], function ($, metisMenu) { 
    $(document).ready(function() {
    	$('.search_single input,.search_single select').on('input',function(){
  		  $("#orderForm1")[0].reset();
  	    });

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-tableChecked',
            paging: true,
            scrollX:false,
            serverSide: true, //不打开会出现排序不对
            ajax: "/transChargeCheckOrder/checkedList",
            columns:[	                
					  { "data": "ORDER_NO", "width": "100px",
			            	 "render": function ( data, type, full, meta ) {
			           		  return "<a href='/transChargeCheckOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
			           	  }
			          },
					  { "data": "CREATE_STAMP", "width": "100px"},  
					  { "data": "TOSTATUS", "width": "50px",
						  "render": function ( data, type, full, meta ) {
								  return data;
			           	  }
					  },
					  { "data": "SP_NAME", "width": "80px"}, 
					  { "data": "SERVICE_STAMP","width": "100px"}, 
					  { "data": "TOTAL_AMOUNT","width": "60px","visible":false,
				    	 "render":function(data, type, full, meta){
				    		 if(data<0){
				    			 return '<span style="color:red">'+eeda.numFormat(parseFloat(data).toFixed(2),3)+'</span>';
				    		 }else{
				    			 return data;
				    		 }
				    	 }
					  },
			          { "data": "TOTAL_AMOUNT_CNY",
						  "render":function(data,type,full,meta){
						  	if(!data)
						  		data=0.00;
							  var usd_str=eeda.numFormat(parseFloat(data).toFixed(2),3);
							  return usd_str;
						  }
					  },
					  { "data": "TOTAL_RECEIVE_CNY",
						  "render":function(data,type,full,meta){
						  	if(!data)
						  		data=0.00;
							  var usd_str=eeda.numFormat(parseFloat(data).toFixed(2),3);
							  return usd_str;
						  }
					  },
			          { "data": "TOTAL_RESIDUAL_CNY",
						  "render":function(data,type,full,meta){
						  	if(!data)
						  		data=0.00;
							  var usd_str=eeda.numFormat(parseFloat(data).toFixed(2),3);
							  return usd_str;
						  }
					  },
					  {"data":"CONFIRM_NAME"},
					  {"data":"CONFIRM_STAMP"}
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
  	    	  $("#single_status").hide();
  	    	  $("#single_create_stamp1_show").hide();
  	    	  $("#public_text").show();
  	      }
  	      if(selectField=='sp_name'){
  	    	  $("#single_sp1_id_input").val("");
  	    	  $("#single_status").hide();
  	    	  $("#single_create_stamp1_show").hide();
  	    	  $("#public_text").hide();
  	    	  $("#single_sp1_id_input").show();
  	      }
  	      if(selectField=='toStatus_equals'){
  	    	  $("#single_status").val("");
  	    	  $("#single_create_stamp1_show").hide();
  	    	  $("#public_text").hide();
  	    	  $("#single_sp1_id_input").hide();
  	    	  $("#single_status").show();
  	      }
  	      if(selectField=='create_stamp1'){
  	    	  $("#single_create_stamp1_begin_time").val("");
  	    	  $("#single_create_stamp1_end_time").val("");
  	    	  $("#public_text").hide();
  	    	  $("#single_sp1_id_input").hide();
  	    	  $("#single_status").hide();
  	    	  $("#single_create_stamp1_show").show();
  	      }
       });
        
        $("#singleSearchBtn").click(function(){
          $("#orderForm1")[0].reset();
      	  var selectField = $('#selected_field').val();
	      if(selectField=='order_no'){
	    	  $("#order_no1").val($("#public_text").val());
	      }
	      if(selectField=='sp_name'){
	    	  $("#sp1").val($("#single_sp1_id").val());
	    	  $("#sp1_input").val($("#single_sp1_id_input").val());
	      }
	      if(selectField=='toStatus_equals'){
	    	  $("#status").val($("#single_status").val());
	      }
	      if(selectField=='create_stamp1'){
	    	  $("#create_stamp1_begin_time").val($("#single_create_stamp1_begin_time").val());
	    	  $("#create_stamp1_end_time").val($("#single_create_stamp1_end_time").val());
	      }
	      $('#searchBtn1').click();
        });
        
      $('#resetBtn1').click(function(e){
          $("#orderForm1")[0].reset();
      });

      $('#searchBtn1').click(function(){
          searchData(); 
      });

     var searchData=function(){
          var order_no = $("#order_no1").val();
          var sp_name = $('#sp1_input').val().trim();
          var start_date = $("#create_stamp1_begin_time").val();
          var end_date = $("#create_stamp1_end_time").val();
          var status = $("#status").val();
          
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/transChargeCheckOrder/checkedList?order_no="+order_no
               +"&sp_name="+sp_name
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date
               +"&toStatus_equals="+status;

          dataTable.ajax.url(url).load();
       }
       
    });
});