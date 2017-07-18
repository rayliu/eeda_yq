define(['jquery', 'metisMenu',  'dataTablesBootstrap'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/tao_manage/case/list",
            columns:[
                {	"data":"PRODUCTOR",
                	"width": "80px"
                },
                {	"data": "NAME", "width":"120px"},
	              { "data": "CREATE_TIME", "width":"90px"}, 
	              { "data": "ID", "width":"60px"}
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
      
      $('#eeda_table').on('click','.edit',function(){

      });
    	
});
});