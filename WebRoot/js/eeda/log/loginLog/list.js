define(['jquery', 'metisMenu', 'sb_admin', 'sco', 'dataTablesBootstrap', 'validate_cn',  'dtColReorder', 'jq_blockui'], function ($, metisMenu) { 

    $(document).ready(function() {


    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            colReorder: true,
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/loginLog/list",
            columns:[
                { "data": "USER_NAME","width": "30%"},
                { "data": "CREATE_STAMP","width": "30%"},
                { "data": "IP", "width": "30%"}
            ]
        });

      //条件筛选
    	$("#searchBtn").on('click', function () {
        	var user_name = $.trim($("#user_name").val()); 
          var create_stamp_begin_time = $("#create_stamp_begin_time").val();
          var create_stamp_end_time = $("#create_stamp_end_time").val();
        	var url = "/loginLog/list?c_name_like="+user_name
              +"&create_stamp_begin_time="+create_stamp_begin_time
              +"&create_stamp_end_time="+create_stamp_end_time;
        	dataTable.ajax.url(url).load();
      });

      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });
    });
});