define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '报关费用报表  | '+document.title;
  	
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
          paging: true,
          serverSide: true, //不打开会出现排序不对
          ajax: "/customReport/list",
          columns: [
              { "data": "SP_NAME"},
              { "data": "CREATE_STAMP"}, 
              { "data": null,
            	  "render": function ( data, type, full, meta ) {
            		  return 0;
            	  }
              }, 
              { "data": "CUSTOM_ORDER_NO"}, 
              { "data": null,
            	  "render": function ( data, type, full, meta ) {
            		  return 0;
            	  }
              }, 
              { "data": null,
            	  "render": function ( data, type, full, meta ) {
            		  return 0;
            	  }
              }, 
              { "data": null,
            	  "render": function ( data, type, full, meta ) {
            		  return 0;
            	  }
              }, 
              { "data": null,
            	  "render": function ( data, type, full, meta ) {
            		  return 0;
            	  }
              }, 
              { "data": null,
            	  "render": function ( data, type, full, meta ) {
            		  return 0;
            	  }
              }, 
              { "data": null,
            	  "render": function ( data, type, full, meta ) {
            		  return 0;
            	  }
              }, 
              { "data": null,
            	  "render": function ( data, type, full, meta ) {
            		  return 0;
            	  }
              }, 
              { "data": null,
            	  "render": function ( data, type, full, meta ) {
            		  return 0;
            	  }
              }, 
              { "data": null,
            	  "render": function ( data, type, full, meta ) {
            		  return 0;
            	  }
              }, 
              { "data": null,
            	  "render": function ( data, type, full, meta ) {
            		  return 0;
            	  }
              }, 
              { "data": null,
            	  "render": function ( data, type, full, meta ) {
            		  return 0;
            	  }
              }, 
              { "data": null,
            	  "render": function ( data, type, full, meta ) {
            		  return 0;
            	  }
              }, 
              { "data": null,
            	  "render": function ( data, type, full, meta ) {
            		  return 0;
            	  }
              }, 
              { "data": "TOTAL_AMOUNT"}
          ]
      });
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
          var sp_id = $("#sp_id").val();
          var create_time_begin_time = $("#create_time_begin_time").val();
          var create_time_end_time = $("#create_time_end_time").val();

          //增加出口日期查询
          var url = "/customReport/list?"
          	    +"sp_id="+sp_id
          		+"&create_stamp_begin_time="+create_time_begin_time
          		+"&create_stamp_end_time="+create_time_end_time;

          dataTable.ajax.url(url).load();
      };
  });
});