define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 
    $(document).ready(function() {
    	document.title = 'eBay退货 | '+document.title;
    	
      $("#breadcrumb_li").text('eBay退货');
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/ebayReturn/list",
            columns:[
                {"data": "RETURN_ID", "width":"100px"},
                {"data": "TRANSACTION_ID", "width":"100px"},
                { "data": "ITEM_ID", "width":"100px"},
	              { "data": "BUYER_LOGIN_NAME", "width":"120px"}, 
                { "data": "SELLER_LOGIN_NAME", "width":"90px"},
                { "data": "SELLER_ESTIMATE_TOTAL_REFUND", "width":"60px",
                  "render": function ( data, type, full, meta ) {
                      if(data){
                          return full.SELLER_ESTIMATE_TOTAL_REFUND_CURRENCY+" "+data;
                      }
                      return "";
                    }
                },
                { "data": "SELLER_ACTUAL_TOTAL_REFUND", "width":"60px",
                  "render": function ( data, type, full, meta ) {
                      if(data){
                          return full.SELLER_ACTUAL_TOTAL_REFUND_CURRENCY+" "+data;
                      }
                      return "";
                    }
                }, 
                { "data": "CREATION_DATE", "width":"60px"},
                { "data": "STATUS", "width":"60px"}
            ]
        });
      
      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){

          var url = "/ebayReturn/list";

          dataTable.ajax.url(url).load();
      };
      
      $('#importBtn').click(function(){
        $.blockUI();
         $.post('/ebayReturn/importReturn', {nothing: 'nothing'}, function(data, textStatus, xhr) {
            searchData();
            $.unblockUI();
         });
      })
    	
});
});