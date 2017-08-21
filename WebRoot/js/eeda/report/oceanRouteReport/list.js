define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {

  	
  	
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
          colReorder: true,
          paging: true,
          serverSide: true, 
          pageLength: 25,
          ajax: "/oceanRouteReport/list",
          columns: [
              { "data": "ORDER_EXPORT_DATE" ,"class":"order_export_date"},
              { "data": "CUSTOMER_NAME","class":"customer_name"},
              { "data": "ROUTE","class":"route"},
              { "data": "PIECES","class":"pieces", 
                "render": function ( data, type, full, meta ) {
                  if(data){
                    return "<span class=''>"+eeda.numFormat(data,3) + "</span>";
                  } else {
                    return '';
                  }
                }
              }, 
              { "data": "GROSS_WEIGHT","class":"gross_weight", 
                "render": function ( data, type, full, meta ) {
                  if(data){
                    return "<span class=''>"+eeda.numFormat(data.toFixed(2),3) + "</span>";
                  } else {
                    return '';
                  }
                }
              }, 
              { "data": "VOLUME", "class":"volume", 
                "render": function ( data, type, full, meta ) {
                  if(data){
                    return "<span class=''>"+eeda.numFormat(data.toFixed(2),3) + "</span>";
                  } else{
                    return '';
                  }
                }
              },
              { "data": "OCEAN_FCL_TEU","class":"ocean_fcl_teu", 
                "render": function ( data, type, full, meta ) {
                    if(data){
                      return "<span class=''>"+eeda.numFormat(data.toFixed(0),3) + "</span>";
                    } else{
                      return '';
                  }
                }
              },
              { "data": "OCEAN_LCL_CBM","class":"ocean_lcl_cbm", 
                "render": function ( data, type, full, meta ) {
                    if(data){
                      return "<span class=''>"+eeda.numFormat(data.toFixed(2),3) + "</span>";
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
          var customer_name = $("#customer_name_input").val();
          var date_type = $('[name=type]:checked').val();
          var begin_date = '';
          var end_date = '';
          var type = $("#type").val()
          if(date_type=='year'){
        	  begin_date = $("#year_begin_time").val();
              end_date = $("#year_end_time").val();
          }else if(date_type=='season'){
        	  begin_date = $("#season_begin_time").val();
              end_date = $("#season_end_time").val();
          }else if (date_type =="month"){
        	  begin_date = $("#month_begin_time").val();
              end_date = $("#month_end_time").val();
          }else{
        	  begin_date = $("#day_begin_time").val()
        	  end_date = $("#day_end_time").val()
          }
          
          
        //合计字段
          $.post('oceanRouteReport/listTotal',{
        	  customer_id:customer_id,
        	  date_type:date_type,
        	  begin_date:begin_date,
        	  end_date:end_date,
        	  type:type
          },function(data){
        	  var pieces_total = parseFloat(data.PIECES_TOTAL);
        	  var gross_weight_total = parseFloat(data.GROSS_WEIGHT_TOTAL);
        	  var volume_total = parseFloat(data.VOLUME_TOTAL);
        	  var ocean_fcl_teu_total = parseFloat(data.OCEAN_FCL_TEU_TOTAL);
        	 
        	  var ocean_lcl_cbm_total = parseFloat(data.OCEAN_LCL_CBM_TOTAL);

        	  var total=parseFloat(data.TOTAL);
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=order_export_date]').html('共'+total+'项汇总：');
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=pieces]').html(eeda.numFormat(pieces_total,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=gross_weight]').html(eeda.numFormat(gross_weight_total,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=volume]').html(eeda.numFormat(volume_total,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=ocean_fcl_teu]').html(eeda.numFormat(ocean_fcl_teu_total,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=ocean_lcl_cbm]').html(eeda.numFormat(ocean_lcl_cbm_total,3));
          });
  
          
          //增加出口日期查询
          var url = "/oceanRouteReport/list?order_no="+order_no
          	    +"&customer_id="+customer_id
          	    +"&date_type="+date_type
          	    +"&customer_name_like="+customer_name
          		+"&begin_date="+begin_date
          		+"&end_date="+end_date
          		+"&type="+type;

          dataTable.ajax.url(url).load();
      };
      
      
      $('[name=type]').on('click',function(){
    	  var value  = $(this).val();
    	  if(value=='year'){
    		  $('#year').show();
    		  $('#day').hide();
    		  $('#month').hide();
    		  $('#season').hide();
    	  }else if(value=='season'){
    		  $('#year').hide();
    		  $('#day').hide();
    		  $('#month').hide();
    		  $('#season').show();
    	  } else if(value=='month'){
    		  $('#year').hide();
    		  $('#month').show();
    		  $('#day').hide();    		  
    		  $('#season').hide();
    	  }else {
    		  $('#year').hide();
    		  $('#month').hide();
    		  $('#season').hide();
    		  $("#day").show()
    	  }
      })
 
      searchData();
  });
});