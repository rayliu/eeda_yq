define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '空运路线运营报表  | '+document.title;
  	$('#breadcrumb_li').text('空运路线运营报表');
  	
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
                    return "<span class=''>"+data + "</span>";
                  } else {
                    return '';
                  }
                }
              },
              { "data": "VOLUME", 
                "render": function ( data, type, full, meta ) {
                  if(data){
                    return "<span class=''>"+(data).toFixed(2) + "</span>";
                  } else{
                    return '';
                  }
                }
              },
              { "data": "ARI_KG",
                "render": function ( data, type, full, meta ) {
                    if(data){
                      return "<span class=''>"+(data).toFixed(2) + "</span>";
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
          
          
        //合计字段
          $.post('airRouteReport/listTotal',{
        	  customer_id:customer_id,
        	  date_type:date_type,
        	  end_date:end_date,
        	  end_date:end_date
          },function(data){
        	  var pieces_total = parseFloat(data.PIECES_TOTAL);
        	  var gross_weight_total = parseFloat(data.GROSS_WEIGHT_TOTAL);
        	  var volume_total = parseFloat(data.VOLUME_TOTAL);
        	  var ari_kg_total = parseFloat(data.ARI_KG_TOTAL);
        	  var total=parseFloat(data.TOTAL);
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(0).html('共'+total+'项汇总：');
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(3).html(eeda.numFormat(pieces_total,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(4).html(eeda.numFormat(gross_weight_total,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(5).html(eeda.numFormat(volume_total,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(6).html(eeda.numFormat(ari_kg_total,3));

          });
         
          
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
 
      searchData(); 
  });
});