define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '运营报表  | '+document.title;
  	$('#breadcrumb_li').text('运营报表');
  	
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
          paging: true,
          serverSide: true, //不打开会出现排序不对
          ajax: "/profitReport/list",
          columns: [
              { "data": "ORDER_EXPORT_DATE","width":"60px" },
              { "data": "CUSTOMER_NAME","width":"60px"},
              { "data": "PIECES","width":"50px",
                "render": function ( data, type, full, meta ) {
                  if(data){
                    return "<span class=''>" + eeda.numFormat(data,3) + "</span>";
                  } else {
                    return '';
                  }
                }
              }, 
              { "data": "GROSS_WEIGHT","width":"90px",
                "render": function ( data, type, full, meta ) {
                  if(data){
                    return "<span class='' >" + eeda.numFormat(data.toFixed(2),3) + "</span>";
                  } else {
                    return '';
                  }
                }
              }, 
              { "data": "VOLUME","width":"90px",
                "render": function ( data, type, full, meta ) {
                  if(data){
                    return "<span class=''>" + eeda.numFormat(data.toFixed(2),3) + "</span>";
                  } else{
                    return '';
                  }
                }
              },
              { "data": "OCEAN_FCL_TEU","width":"90px",
                "render": function ( data, type, full, meta ) {
                    if(data){
                      return "<span class=''>"+eeda.numFormat(data.toFixed(0),3) + "</span>";
                    } else{
                      return '';
                  }
                }
              },
              { "data": "OCEAN_FCL_BILL","width":"120px",
                  "render": function ( data, type, full, meta ) {
                      if(data){
                        return "<span class=''>"+eeda.numFormat(data.toFixed(0),3) + "</span>";
                      } else{
                        return '';
                    }
                  }
                },
              { "data": "OCEAN_LCL_CBM","width":"100px",
                "render": function ( data, type, full, meta ) {
                    if(data){
                      return "<span class=''>"+eeda.numFormat(data.toFixed(2),3) + "</span>";
                    } else{
                      return '';
                    }
                }
              }, 
              { "data": "OCEAN_LCL_BILL","width":"120px",
                  "render": function ( data, type, full, meta ) {
                      if(data){
                        return "<span class=''>"+eeda.numFormat(data.toFixed(0),3) + "</span>";
                      } else{
                        return '';
                      }
                  }
                },
              { "data": "ARI_KG","width":"100px",
                "render": function ( data, type, full, meta ) {
                    if(data){
                      return "<span class=''>"+eeda.numFormat(data.toFixed(2),3) + "</span>";
                    } else{
                      return '';
                    }
                }
              },
              { "data": "ARI_KG_BILL","width":"120px",
                  "render": function ( data, type, full, meta ) {
                      if(data){
                        return "<span class=''>"+eeda.numFormat(data.toFixed(0),3)+ "</span>";
                      } else{
                        return '';
                      }
                  }
                }, 
              { "data": "TRUCK_TYPE"}
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
      
      
      
//      if($('#eeda-table td').length>1){
//      	var col = [3, 4, ,11,12, 13, 16, 19,21];
//      	for (var i=0;i<col.length;i++){
//  	    	var arr = cargoTable.column(col[i]).data();
//      		$('#eeda-table tfoot').find('th').eq(col[i]).html(
//  	    		arr.reduce(function (a, b) {
//  		    		a = parseFloat(a);
//  		    		if(isNaN(a)){ a = 0; }                   
//  		    		b = parseFloat(b);
//  		    		if(isNaN(b)){ b = 0; }
//  		    		return (a + b).toFixed(3);
//  		    	})
//  	    	);
//      	}
//      }
      
      
      
      

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
          $.post('profitReport/listTotal',{
        	  customer_id:customer_id,
        	  date_type:date_type,
        	  end_date:end_date,
        	  end_date:end_date
          },function(data){
        	  var pieces_total = parseFloat(data.PIECES_TOTAL);
        	  var gross_weight_total = parseFloat(data.GROSS_WEIGHT_TOTAL);
        	  var volume_total = parseFloat(data.VOLUME_TOTAL);
        	  var ocean_fcl_teu_total = parseFloat(data.OCEAN_FCL_TEU_TOTAL);
        	  var ocean_fcl_bill_total = parseFloat(data.OCEAN_FCL_BILL_TOTAL);
        	  var ocean_lcl_cbm_total = parseFloat(data.OCEAN_LCL_CBM_TOTAL);
        	  var ocean_lcl_bill_total = parseFloat(data.OCEAN_LCL_BILL_TOTAL);
        	  var ari_kg_total = parseFloat(data.ARI_KG_TOTAL);
        	  var ari_kg_bill_total = parseFloat(data.ARI_KG_BILL_TOTAL);
        	  var total=parseFloat(data.TOTAL);
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(0).html('共'+total+'项汇总：');
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(2).html(eeda.numFormat(pieces_total,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(3).html(eeda.numFormat(gross_weight_total,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(4).html(eeda.numFormat(volume_total,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(5).html(eeda.numFormat(ocean_fcl_teu_total,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(6).html(eeda.numFormat(ocean_fcl_bill_total,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(7).html(eeda.numFormat(ocean_lcl_cbm_total,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(8).html(eeda.numFormat(ocean_lcl_bill_total,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(9).html(eeda.numFormat(ari_kg_total,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(10).html(eeda.numFormat(ari_kg_bill_total,3));

          });
          
          //增加出口日期查询
          var url = "/profitReport/list?order_no="+order_no
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