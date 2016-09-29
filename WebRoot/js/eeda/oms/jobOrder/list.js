define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '工作单查询   | '+document.title;
  	  if(type!=""){
  		  $('#menu_todo_list').addClass('active').find('ul').addClass('in');
  		  $('#menu_order').removeClass('active').find('ul').removeClass('in');
  	  }else{
  		$('#menu_todo_list').removeClass('active').find('ul').removeClass('in');
		  $('#menu_order').addClass('active').find('ul').addClass('in');
  	  }
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
          paging: true,
          serverSide: true, //不打开会出现排序不对
          ajax: "/jobOrder/list?type="+type,
          columns: [
              { "data": "ORDER_NO", 
                  "render": function ( data, type, full, meta ) {
                      return "<a href='/jobOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
                  }
              },
              { "data": "TYPE",
                  "render": function ( data, type, full, meta ) {
                    var str = '';
                    if(data == '出口柜货')
                      str = '出口柜货'; 
                    if(data == '进口柜货')
                      str = '进口柜货';
                    if(data == '进口散货')
                      str = '进口散货';
                    if(data == '出口空运')
                  	  str = '出口空运';
                    if(data == '进口空运')
                  	  str = '进口空运';
                    if(data == '香港头程')
                  	  str = '香港头程';
                    if(data == '香港游')
                  	  str = '香港游';
                    if(data == '陆运')
                  	  str = '陆运';
                    if(data == '报关')
                  	  str = '报关';
                    if(data == '快递')
                  	  str = '快递';
                    if(data == '加贸')
                  	  str = '加贸';
                    if(data == '贸易')
                  	  str = '贸易';
                    if(data == '园区游')
                  	  str = '园区游';
                    return str;
                  }
              }, 
              { "data": "SENT_OUT_TIME", 
            	  render: function(data){
            		  if(data)
            			  return data;
            		  return '';
            	  }
              }, 
              { "data": "CUSTOMER_NAME"}, 
              { "data": "CREATOR_NAME"}, 
              { "data": "CREATE_STAMP"}, 
              { "data": "STATUS"},
              
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
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          var sent_out_time_begin_time = $("#sent_out_time_begin_time").val();
          var sent_out_time_end_time = $("#sent_out_time_end_time").val();
          var status = $('#status').val();
          var customer_code = $("#customer_code").val().trim();
          var customer_name = $("#customer_name").val().trim();
          //增加出口日期查询
          var url = "/jobOrder/list?order_no="+order_no
          	   +"&status="+status
          	   +"&customer_code="+customer_code
               +"&customer="+customer_name
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date
          	   +"&sent_out_time_begin_time="+sent_out_time_begin_time
          	   +"&sent_out_time_end_time="+sent_out_time_end_time;

          dataTable.ajax.url(url).load();
      };
      
      
      $('#oceanDetailTab').click(function(e){
    	  var transport_type = "ocean";
    	  	var url = "/jobOrder/list?transport_type_like="+transport_type;
    	  	dataTable.ajax.url(url).load();
      })
      $('#airDetailTab').click(function(e){
    	  var transport_type = "air";
    	  var url = "/jobOrder/list?transport_type_like="+transport_type;
    	  dataTable.ajax.url(url).load();
      })
      $('#landDetailTab').click(function(e){
    	  var transport_type = "land";
    	  var url = "/jobOrder/list?transport_type_like="+transport_type;
    	  dataTable.ajax.url(url).load();
      })
      $('#customDetailTab').click(function(e){
    	  var transport_type = "custom";
    	  var url = "/jobOrder/list?transport_type_like="+transport_type;
    	  dataTable.ajax.url(url).load();
      })
      $('#allTab').click(function(e){
    	  var transport_type = "";
    	  var url = "/jobOrder/list?transport_type_like="+transport_type;
    	  dataTable.ajax.url(url).load();
      })
      
      
  });
});