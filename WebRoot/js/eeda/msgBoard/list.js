define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 
    $(document).ready(function() {
    	document.title = '公告列表| '+document.title;
    	
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/msgBoard/list",
            columns:[
                  {"data": "TITLE", 
                	  "render": function ( data, type, full, meta ) {
                		  return "<a href='/msgBoard/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
                	  }
                  },
	              { "data": "CONTENT"}, 
	              { "data": "CREATE_NAME"}, 
	              { "data": "CREATE_STAMP"}, 
	              { "data": "UPDATE_NAME"},
	              { "data": "UPDATE_STAMP"},
            ]
        });
      
      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
          var creator = $.trim($("#creator").val()); 
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();

          var url = "/msgBoard/list?create_name_like="+creator
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
      };
    	
});
});