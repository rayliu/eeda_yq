define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 
    $(document).ready(function() {
    	document.title = 'eBay订单 | '+document.title;
    	
      $("#breadcrumb_li").text('eBay订单');
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/ebaySalesOrder/list",
            columns:[
                {"data": "TRANSACTION_ID", "width":"100px"},
                { "data": "ITEM_ID", "width":"100px"},
                { "data": "GROUP_RECORD_NUMBER", "width":"80px"},
                { "data": "SKU", "width":"120px"},
	              { "data": "BUYER_USER_ID", "width":"120px",
                  "render": function ( data, type, full, meta ) {
                      if(data){
                          return "<a href='#' class='address' buyer_name='"+full.BUYER_USER_NAME
                            +"' buyer_address='"+full.BUYER_SHIP_ADDRESS+"' title='查看名称和地址'>" + data + "</a>";
                      }
                      return "";
                    }
                }, 
                { "data": "SELLER_USER_ID", "width":"90px"},
                { "data": "TOTAL", "width":"60px",
                  "render": function ( data, type, full, meta ) {
                      if(data){
                          return full.TOTAL_CURRENCY_ID+" "+data;
                      }
                      return "";
                    }
                }, 
                { "data": "CREATED_TIME", "width":"60px"},
                { "data": "PAID_TIME", "width":"60px",
                  "render": function ( data, type, full, meta ) {
                      if(data){
                          return data;
                      }
                      return "未付款";
                    }
                }, 
                { "data": "SHIPPED_TIME", "width":"60px",
                  "render": function ( data, type, full, meta ) {
                      if(data){
                          return data;
                      }
                      return "未发货";
                    }
                },
                { "data": "SHIPPING_CARRIER_USED", "width":"60px"},
                { "data": "ORDER_STATUS", "width":"60px"},
                { "data": "SHIPMENT_TRACKING_NUMBER", "width":"120px"
                  // "render": function ( data, type, full, meta ) {
                  //     var editBtn = '<button type="button" class="btn btn-default btn-xs"><i class="fa fa-edit"></i> 编辑</button>';
                  //     if(data){
                  //         return full.SHIPMENT_TRACKING_NUMBER +'<br>'+editBtn;
                          
                  //     }
                  //     return editBtn;
                  //   }
                }
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
        $.blockUI();
         $.post('/ebaySalesOrder/importOrders', {nothing: 'nothing'}, function(data, textStatus, xhr) {
            searchData();
            $.unblockUI();
         });
      });
    	
      $('#eeda_table').on('click', '.address', function(){
        var name = $(this).attr('buyer_name');
        var address = $(this).attr('buyer_address');
        $('#buyerName').val(name);
        $('#buyerAddress').text(address);

        $('#buyerAddressModal').modal('show');
      });
      
  });
});