define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco',  'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '报关费用明细  | '+document.title;
  	$("#breadcrumb_li").text('报关费用明细');
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
          colReorder: true,
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