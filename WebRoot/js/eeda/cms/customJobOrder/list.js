define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '工作单查询   | '+document.title;
  
  	  //datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
          paging: true,
          serverSide: true, //不打开会出现排序不对
          ajax: "/customJobOrder/list",
          columns: [
              { "data": "ORDER_NO", 
                  "render": function ( data, type, full, meta ) {
                      return "<a href='/customJobOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
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
              { "data": "APPLICATION_DATE", 
            	  render: function(data){
            		  if(data)
            			  return data;
            		  return '';
            	  }
              },
              { "data": "EXPORT_PORT"}, 
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
          var application_date_begin_time = $("#application_date_begin_time").val();
          var application_date_end_time = $("#application_date_end_time").val();
          var status = $('#status').val();
          var customer_code = $("#customer_code").val().trim();
          var customer_name = $("#customer_name").val().trim();
          //增加出口日期查询
          var url = "/customJobOrder/list?order_no="+order_no
          	   +"&status="+status
          	   +"&customer_code_like="+customer_code
               +"&customer_name_like="+customer_name
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date
          	   +"&application_date_begin_time="+application_date_begin_time
          	   +"&application_date_end_time="+application_date_end_time;

          dataTable.ajax.url(url).load();
      };
  });

});