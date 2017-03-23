define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 
    $(document).ready(function() {
    	document.title = 'eBay站内信 | '+document.title;
    	
      $("#breadcrumb_li").text('eBay站内信');
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/ebayMemberMsg/list",
            columns:[
                { "data": "ITEM_ID", "width":"120px"},
	              { "data": "SENDER_ID", "width":"120px"}, 
                { "data": "SELLER_ID", "width":"90px"},
                { "data": "SELLING_PRICE", "width":"60px",
                  "render": function ( data, type, full, meta ) {
                      if(data){
                          return full.SELLING_CURRENCY+" "+data;
                      }
                      return "";
                    }
                }, 
                { "data": "TITLE", "width":"60px"},
                { "data": "BODY", "width":"60px"}, 
                { "data": "MESSAGE_STATUS", "width":"60px"}, 
                { "data": "CREATION_DATE", "width":"60px"}
                
            ]
        });
      
      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){

          var url = "/ebayMemberMsg/list";

          dataTable.ajax.url(url).load();
      };
      
      $('#importBtn').click(function(){
           $.post('/ebayMemberMsg/importOrders', {nothing: 'nothing'}, function(data, textStatus, xhr) {
                
           });
      })
    	
});
});