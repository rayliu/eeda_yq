define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 
    $(document).ready(function() {
    	document.title = 'eBay取消交易 | '+document.title;
    	
      $("#breadcrumb_li").text('eBay取消交易');
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/ebayCancellation/list",
            columns:[
                { "data": "CANCEL_ID", "width":"100px"},
                { "data": "LEGACY_ORDER_ID", "width":"100px"},
                { "data": "CANCEL_REASON", "width":"100px"},
	              { "data": "CANCEL_REQUEST_DATE", "width":"120px"}, 
                { "data": "CANCEL_STATUS", "width":"90px"},
                { "data": "CANCEL_CLOSE_DATE", "width":"60px"}
            ]
        });
      
      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){

          var url = "/ebayCancellation/list";

          dataTable.ajax.url(url).load();
      };
      
      $('#importBtn').click(function(){
        $.blockUI();
         $.post('/ebayCancellation/importCancellation', {nothing: 'nothing'}, function(data, textStatus, xhr) {
            searchData();
            $.unblockUI();
         });
      })
    	
});
});