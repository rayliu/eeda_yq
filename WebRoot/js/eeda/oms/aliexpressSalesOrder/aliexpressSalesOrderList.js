define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 
    $(document).ready(function() {
    	document.title = '速卖通订单 | '+document.title;
    	
      $("#breadcrumb_li").text('速卖通订单');
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/aliexpressSalesOrder/list",
            columns:[
	              { "data": null, 
                  "render": function ( data, type, full, meta ) {
                      var status = "";
                      if(full.ORDER_STATUS=='WAIT_SELLER_SEND_GOODS'){
                        status = "等待发货";
                      }else if(full.ORDER_STATUS=='WAIT_BUYER_ACCEPT_GOODS'){
                        status = "等待收货";
                      }

                      var productList = full.PRODUCT_LIST;

                      var subRowHtml = "";
                      for (var i = 0; i < productList.length; i++) {
                        var product = productList[i];
                        var itemHtml = 
                          "<div class='row'>"
                          +  "<div class='col-lg-1'>"
                          +    '  <img src="'+product.PRODUCT_IMG_URL+'">'
                          +  "</div>"
                          +  "<div class='col-lg-3'>"
                          +    '  <label> '+product.PRODUCT_NAME+'</label>'
                          +  "</div>"
                          +  "<div class='col-lg-3'>"
                          +    '  <label> '+product.PRODUCT_COUNT+'</label>'
                          +  "</div>"
                          +"</div>";
                        subRowHtml = subRowHtml+itemHtml;
                      }

                      var row_html = 
                          "<div class='row'>"
                          +  "<div class='col-lg-12'>"
                          +    ''+full.CREATE_TIME
                          +    '  <label class="search-label">订单号: '+full.ORDER_ID+'</label>'
                          +    '  <label class="search-label">买家: '+full.BUYER_NAME+'</label>'
                          +    '  <label class="">'+full.PAY_CURRENCY_CODE+' '+full.PAY_AMOUNT+'</label>'
                          +    '  <label class="pull-right">'+status+'</label>'
                          +  "</div>"
                          +"</div>"
                          +subRowHtml;
                      return row_html;
                    }
                }
            ]
        });
      
      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){

          var url = "/aliexpressSalesOrder/list";

          dataTable.ajax.url(url).load();
      };
      
      $('#importBtn').click(function(){
           $.post('/aliexpressSalesOrder/importOrders', {nothing: 'nothing'}, function(data, textStatus, xhr) {
                
           });
      })
    	
});
});