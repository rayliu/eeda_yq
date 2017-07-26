define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','sco','validate_cn'], function ($, metisMenu) { 

    $(document).ready(function() {
    	


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
					  { "data": "STATUS", "width": "50px"},
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
               +"&status="+status;

          dataTable.ajax.url(url).load();
       }
       
    });
});