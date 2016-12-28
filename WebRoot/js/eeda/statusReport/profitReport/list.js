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
              { "data": "ORDER_EXPORT_DATE" },
              { "data": "CUSTOMER_NAME"},
              { "data": "PIECES"}, 
              { "data": "GROSS_WEIGHT"}, 
              { "data": "VOLUME"}, 
              { "data": null,
            	  "render": function ( data, type, full, meta ) {
            		  var charge = full.TOTAL_CHARGE;
            		  var cost = full.TOTAL_COST;
            		  if(data){
            			  var total = (charge-cost).toFixed(2);
            			  if(total>0){
            				  return "<span style='color:green'>"+total+"</span>"; 
            			  }else{
            				  return "<span style='color:red'>"+total+"</span>"; 
            			  } 
            		  } else
            			  return '';
                  }
              },
              { "data": null,
            	  "render": function ( data, type, full, meta ) {
            		  var charge = full.TOTAL_CHARGE;
            		  var cost = full.TOTAL_COST;
            		  if(data)
            			  if(((charge-cost)/charge*100).toFixed(2)>0){
            				 return ((charge-cost)/charge*100).toFixed(2)+ '%';
            			  } else{
            				  return 0;
            			  }
            		  else
            			  return '';
                  }
              },
              { "data": "OCEAN_COST","visible":false},
              { "data": "LOAD_COST","visible":false},
              { "data": "ARI_COST","visible":false},
              { "data": "CUSTOM_COST","visible":false},
              { "data": "INSURANCE_COST","visible":false},
              { "data": "TOTAL_COST"},
              { "data": "OCEAN_CHARGE","visible":false},
              { "data": "LOAD_CHARGE","visible":false},
              { "data": "ARI_CHARGE","visible":false},
              { "data": "CUSTOM_CHARGE","visible":false},
              { "data": "INSURANCE_CHARGE","visible":false},
              { "data": "TOTAL_CHARGE"},
          ]
      });
      
      $("#cost").click(function(){
      	var type = $(this).attr("alt");
      	//0代表隐藏
      	var table = $('#eeda-table').dataTable();
      	if(type == 1){
      		table.fnSetColumnVis(8, false);
      		table.fnSetColumnVis(9, false);
      		table.fnSetColumnVis(10, false);
      		table.fnSetColumnVis(11, false);
      		table.fnSetColumnVis(7, false);
      		$(this).attr("alt",0).text(">>");
      		$('#eeda-table').attr('style','width:100%')
      	}else{
      		table.fnSetColumnVis(8, true);
      		table.fnSetColumnVis(9, true);
      		table.fnSetColumnVis(10, true);
      		table.fnSetColumnVis(11, true);
      		table.fnSetColumnVis(7, true);
      		$(this).attr("alt",1).text("<<");
      		$('#eeda-table').attr('style','width:1600px')
      	}
      });
      
      $("#charge").click(function(){
        	var type = $(this).attr("alt");
        	//0代表隐藏
        	var table = $('#eeda-table').dataTable();
        	if(type == 1){
        		table.fnSetColumnVis(13, false);
        		table.fnSetColumnVis(14, false);
        		table.fnSetColumnVis(15, false);
        		table.fnSetColumnVis(16, false);
        		table.fnSetColumnVis(17, false);
        		$(this).attr("alt",0).text(">>");
        	}else{
        		table.fnSetColumnVis(13, true);
        		table.fnSetColumnVis(14, true);
        		table.fnSetColumnVis(15, true);
        		table.fnSetColumnVis(16, true);
        		table.fnSetColumnVis(17, true);
        		$(this).attr("alt",1).text("<<");
        	}
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
 

  });
});