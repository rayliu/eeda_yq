define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '计划订单查询 | '+document.title;
    	if(type != ""){
    		$('#menu_order').removeClass('active').find('ul').removeClass('in');
            $('#menu_todo_list').addClass('active').find('ul').addClass('in');
          }
    	else{
        	$('#menu_order').addClass('active').find('ul').addClass('in');
            $('#menu_todo_list').removeClass('active').find('ul').removeClass('in');
    	 }
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/customPlanOrder/list",
            columns:[
                  {"data": "ORDER_NO", 
                	  "render": function ( data, type, full, meta ) {
                		  return "<a href='/customPlanOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
                	  }
                  },
	              { "data": "TYPE"}, 
	              { "data": "APPLICATION_COMPANY_NAME"}, 
	              { "data": "CREATOR_NAME"}, 
	              { "data": "CREATE_STAMP"}, 
	              { "data": "STATUS", 
	            	  
	            	"render": function(data, type, full, meta){
	            		if(data=="审核不通过"){
	            			return "<span style='color:red'>"+data+"</span>";
	            		}else{
	            			return data;
	            		}	            		
	            	}  
	              }
            ]
        });
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
          var order_no = $.trim($("#order_no").val());
          var customer_name = $('#customer_name').val().trim();
          var status = $('#status').val().trim();
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/customPlanOrder/list?order_no="+order_no
               +"&status="+status
               +"&application_company_name="+customer_name
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
      };
    	
    });
});