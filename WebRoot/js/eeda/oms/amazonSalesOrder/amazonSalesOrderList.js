define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 
    $(document).ready(function() {
    	document.title = '亚马逊订单 | '+document.title;
    	
      $("#breadcrumb_li").text('亚马逊订单');
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/ebaySalesOrder/list",
            columns:[
                {"data": "TRANSACTION_ID", "width":"120px"},
                { "data": "ITEM_ID", "width":"120px"},
	              { "data": "BUYER_USER_ID", "width":"120px"}, 
                { "data": "SELLER_USER_ID", "width":"90px"},
                { "data": "TOTAL", "width":"60px",
                  "render": function ( data, type, full, meta ) {
                      if(data){
                          return full.TOTAL_CURRENCY_ID+" "+data;
                      }
                      return "";
                    }
                }, 
                { "data": "PAID_TIME", "width":"90px"}, 
                { "data": "SHIPPED_TIME", "width":"60px"},
                { "data": "CREATED_TIME", "width":"60px"},
                { "data": "ORDER_STATUS", "width":"60px"},
                { "data": "TRACK_NO", "width":"120px"}
            ]
        });
      
      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){

          var url = "/ebaySalesOrder/list";

          dataTable.ajax.url(url).load();
      };
      
      $('#importBtn').click(function(){
           $.post('/ebaySalesOrder/importOrders', {nothing: 'nothing'}, function(data, textStatus, xhr) {
                
           });
      })
    	
});
});