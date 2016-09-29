define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '运营报表  | '+document.title;
  	
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
          paging: true,
          serverSide: true, //不打开会出现排序不对
          ajax: "/profitReport/list",
          columns: [
              { "data": "ORDER_NO", 
                  "render": function ( data, type, full, meta ) {
                      return "<a href='/jobOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
                  }
              },
              { "data": "ABBR"},
              { "data": "PIECES"}, 
              { "data": "GROSS_WEIGHT"}, 
              { "data": "VOLUME"}, 
              { "data": "CHARGE",
            	  "render": function ( data, type, full, meta ) {
            		  if(data)
            			  return (data-full.COST).toFixed(2);
            		  else
            			  return '';
                  }
              },
              { "data": "CHARGE",
            	  "render": function ( data, type, full, meta ) {
            		  if(data)
            			  return ((data-full.COST)/data).toFixed(2);
            		  else
            			  return '';
                  }
              },
              { "data": "COST"},
              { "data": "CHARGE"},
          ]
      });

      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
          var order_no = $.trim($("#order_no").val()); 
          var customer_id = $("#customer_id").val();
          
          //增加出口日期查询
          var url = "/profitReport/list?order_no="+order_no
          	   +"&customer_id="+customer_id;

          dataTable.ajax.url(url).load();
      };
      
 

  });
});