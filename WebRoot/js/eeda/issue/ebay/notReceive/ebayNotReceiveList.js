define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 
    $(document).ready(function() {

      var base_path="/ebayOrderInquiries";

    	document.title = 'eBay未收到货品 | '+document.title;
    	
      $("#breadcrumb_li").text('eBay未收到货品');
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: base_path+"/list",
            columns:[
                { "data": "INQUIRY_ID", "width":"100px"},
                { "data": "TRANSACTION_ID", "width":"100px"},
                { "data": "ITEM_ID", "width":"100px"},
                { "data": "BUYER", "width":"100px"},
	              { "data": "SELLER", "width":"120px"}, 
                { "data": "CLAIM_AMOUNT", "width":"90px",
                  "render": function ( data, type, full, meta ) {
                      if(data){
                          return full.CLAIM_AMOUNT_CURRENCY+" "+data;
                      }
                      return "";
                    }
                },
                { "data": "CREATION_DATE", "width":"60px"},
                { "data": "LAST_MODIFIED_DATE", "width":"60px"},
                { "data": "RESPOND_BY_DATE", "width":"60px"},
                { "data": "INQUIRY_STATUS_ENUM", "width":"60px"},
            ]
        });
      
      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){

          var url = base_path+"/list";

          dataTable.ajax.url(url).load();
      };
      
      $('#importBtn').click(function(){
         $.blockUI();
         $.post(base_path+'/importData', {nothing: 'nothing'}, function(data, textStatus, xhr) {
            searchData();
            $.unblockUI();
         });
      })
    	
});
});