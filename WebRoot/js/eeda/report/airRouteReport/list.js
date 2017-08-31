define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {

  	
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
          colReorder: true,
          paging: true,
          serverSide: true, 
          pageLength: 25,
          ajax: "/airRouteReport/list",
          columns: [
              { "data": "ORDER_EXPORT_DATE","class":"order_export_date" },
              { "data": "CUSTOMER_NAME","class":"customer_name"},
              { "data": "ROUTE","class":"route"},
              { "data": "PIECES","class":"pieces", 
                "render": function ( data, type, full, meta ) {
                  if(data){
                    return "<span class=''>"+data + "</span>";
                  } else {
                    return '';
                  }
                }
              },
              { "data": "VOLUME","class":"volume", 
                "render": function ( data, type, full, meta ) {
                  if(data){
                    return "<span class=''>"+(data).toFixed(2) + "</span>";
                  } else{
                    return '';
                  }
                }
              },
              { "data": "ARI_KG","class":"ari_kg",
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
      
       //base on config hide cols
      dataTable.columns().eq(0).each( function(index) {
          var column = dataTable.column(index);
          $.each(cols_config, function(index, el) {
              
              if(column.dataSrc() == el.COL_FIELD){
                
                if(el.IS_SHOW == 'N'){
                  column.visible(false, false);
                }else{
                  column.visible(true, false);
                }
              }
          });
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
          var type = $("#type").val()
          var begin_date = '';
          var end_date = '';
          if(date_type=='year'){
        	  begin_date = $("#year_begin_time").val();
              end_date = $("#year_end_time").val();
          }else if(date_type=='season'){
        	  begin_date = $("#season_begin_time").val();
              end_date = $("#season_end_time").val();
          }else if(date_type=="month"){
        	  begin_date = $("#month_begin_time").val();
              end_date = $("#month_end_time").val();
          }else{
        	  begin_date = $("#day_begin_time").val()
        	  end_date = $("#day_end_time").val()
          }
          
          
        //合计字段
          $.post('airRouteReport/listTotal',{
        	  customer_id:customer_id,
        	  date_type:date_type,
        	  begin_date:begin_date,
        	  end_date:end_date,
        	  type:type
          },function(data){
        	  var pieces_total = parseFloat(data.PIECES_TOTAL);
        	  var gross_weight_total = parseFloat(data.GROSS_WEIGHT_TOTAL);
        	  var volume_total = parseFloat(data.VOLUME_TOTAL);
        	  var ari_kg_total = parseFloat(data.ARI_KG_TOTAL);
        	  var total=parseFloat(data.TOTAL);
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=order_export_date]').html('共'+total+'项汇总：');
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=pieces]').html(eeda.numFormat(pieces_total,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=volume]').html(eeda.numFormat(volume_total,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=ari_kg]').html(eeda.numFormat(ari_kg_total,3));

          });
         
          
          //增加出口日期查询
          var url = "/airRouteReport/list?order_no="+order_no
          	    +"&customer_id="+customer_id
          	    +"&date_type="+date_type
          		+"&begin_date="+begin_date
          		+"&end_date="+end_date
          		+"&type="+type;

          dataTable.ajax.url(url).load();
      };
      
      
      $('[name=type]').on('click',function(){
    	  var value  = $(this).val();
    	  if(value=='year'){
    		  $('#year').show();
    		  $('#month').hide();
    		  $('#day').hide();
    		  $('#season').hide();
    	  }else if(value=='season'){
    		  $('#year').hide();
    		  $('#day').hide();
    		  $('#month').hide();
    		  $('#season').show();
    	  }else if(value =="month"){
    		  $('#year').hide();
    		  $('#day').hide();
    		  $('#month').show();
    		  $('#season').hide();
    	  }else{
    		  $('#year').hide();
    		  $('#month').hide();
    		  $('#season').hide();
    		  $("#day").show()
    	  }
      })
 
      searchData(); 
      
      //导出excel
      $('#exportTotaledExcel').click(function(){
    	  $(this).attr('disabled', true);
          var customer_id = $("#customer_id").val();
          var date_type = $('[name=type]:checked').val();
          var type = $("#type").val()
          var begin_date = '';
          var end_date = '';
          if(date_type=='year'){
        	  begin_date = $("#year_begin_time").val();
              end_date = $("#year_end_time").val();
          }else if(date_type=='season'){
        	  begin_date = $("#season_begin_time").val();
              end_date = $("#season_end_time").val();
          }else if(date_type=="month"){
        	  begin_date = $("#month_begin_time").val();
              end_date = $("#month_end_time").val();
          }else{
        	  begin_date = $("#day_begin_time").val()
        	  end_date = $("#day_end_time").val()
          }
       
		  $.post('/airRouteReport/downloadExcelList',{customer_id:customer_id,date_type:date_type,type:type,begin_date:begin_date,end_date:end_date}, function(data){
	          $('#exportTotaledExcel').prop('disabled', false);
	          $('#singlexportTotaledExcel').prop('disabled', false);
	          $.scojs_message('生成应收Excel对账单成功', $.scojs_message.TYPE_OK);
	          window.open(data);
	      }).fail(function() {
	          $('#exportTotaledExcel').prop('disabled', false);
	          $.scojs_message('生成应收Excel对账单失败', $.scojs_message.TYPE_ERROR);
	      });
      });
  });
});