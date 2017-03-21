define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 
    $(document).ready(function() {
    	document.title = '亚马逊订单 | '+document.title;
    	
      $("#breadcrumb_li").text('亚马逊订单');
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/amazonSalesOrder/list",
            columns:[
                {"data": "AMAZON_ORDER_ID", "width":"120px"},
                { "data": "BUYER_NAME", "width":"120px"},
	              { "data": "", "width":"120px"}, 
                { "data": "", "width":"90px"},
                { "data": "PURCHASE_DATE", "width":"60px",
                }, 
                { "data": "PAID_TIME", "width":"90px"}, 
                { "data": "TOTAL_AMOUNT", "width":"60px",
                  "render": function ( data, type, full, meta ) {
                      if(data){
                          return full.ORDER_CURRENCY_CODE+" "+data;
                      }
                      return "";
                    }},
                { "data": "CREATED_TIME", "width":"60px"},
                { "data": "FULFILLMENT_CHANNEL", "width":"60px"},
                { "data": "ORDER_STATUS", "width":"60px"}
            ]
        });
      
      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){

          var url = "/amazonSalesOrder/list";

          dataTable.ajax.url(url).load();
      };
      
      $('#importBtn').click(function(){
           $.post('/amazonSalesOrder/importOrders', {nothing: 'nothing'}, function(data, textStatus, xhr) {
                
           });
      })
    	
});
});