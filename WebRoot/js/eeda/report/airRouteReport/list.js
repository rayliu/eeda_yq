define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '运营报表  | '+document.title;
  	
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
          paging: true,
          serverSide: true, 
          pageLength: 25,
          ajax: "/airRouteReport/list",
          columns: [
              { "data": "ORDER_EXPORT_DATE" },
              { "data": "CUSTOMER_NAME"},
              { "data": "ROUTE"},
              { "data": "PIECES", 
                "render": function ( data, type, full, meta ) {
                  if(data){
                    return "<span class='pull-right'>"+data + "</span>";
                  } else {
                    return '';
                  }
                }
              },
              { "data": "VOLUME", 
                "render": function ( data, type, full, meta ) {
                  if(data){
                    return "<span class='pull-right'>"+(data).toFixed(2) + "</span>";
                  } else{
                    return '';
                  }
                }
              },
              { "data": "ARI_KG",
                "render": function ( data, type, full, meta ) {
                    if(data){
                      return "<span class='pull-right'>"+(data).toFixed(2) + "</span>";
                    } else{
                      return '';
                  }
                }
              }
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
          var date_type = $('[name=type]:checked').val();
          var begin_date = '';
          var end_date = '';
          if(date_type=='year'){
        	  begin_date = $("#year_begin_time").val();
              end_date = $("#year_end_time").val();
          }else if(date_type=='season'){
        	  begin_date = $("#season_begin_time").val();
              end_date = $("#season_end_time").val();
          }else{
        	  begin_date = $("#month_begin_time").val();
              end_date = $("#month_end_time").val();
          }
         
          
          //增加出口日期查询
          var url = "/airRouteReport/list?order_no="+order_no
          	    +"&customer_id="+customer_id
          	    +"&date_type="+date_type
          		+"&begin_date="+begin_date
          		+"&end_date="+end_date;

          dataTable.ajax.url(url).load();
      };
      
      
      $('[name=type]').on('click',function(){
    	  var value  = $(this).val();
    	  if(value=='year'){
    		  $('#year').show();
    		  $('#month').hide();
    		  $('#season').hide();
    	  }else if(value=='season'){
    		  $('#year').hide();
    		  $('#month').hide();
    		  $('#season').show();
    	  }else{
    		  $('#year').hide();
    		  $('#month').show();
    		  $('#season').hide();
    	  }
      })
 

  });
});