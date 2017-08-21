define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco',  'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {

  	  
      var dataTable = eeda.dt({
          id: 'eeda_table',
          colReorder: true,
          paging: false,
          serverSide: true, //不打开会出现排序不对 
          ajax: "/transCostBalanceReport/list",
          initComplete:function(settings){
        	  cssTd();
        	  tableStyle();
        	  hideColumn();
              },
          columns: [
          			{ "data": "ABBR", "width": "120px","class":"abbr"},
          			{ "data": "CAR_NO_NAME", "width": "100px","class":"car_no_name"},
          			{ "data": "COST_CNY", "width": "100px","class":"cost_cny",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data,3);
    				  }
    	            },
    	            { "data": "COST_USD", "width": "100px","class":"cost_usd"  ,
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data,3);
    				  }
    	            },
    	            { "data": "COST_JPY", "width": "100px","class":"cost_jpy",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data,3);
    				  }
    	            },
    	            { "data": "COST_HKD", "width": "100px","class":"cost_hkd",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data,3);
    				  }
    	            },
    	            { "data": "COST_RMB", "width": "120px","class":"cost_rmb",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data,3);
    				  }
    	            },
    	            { "data": "UNCOST_CNY", "width": "100px","class":"uncost_cny",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data,3);
    				  }
    	            },
    	            { "data": "UNCOST_USD", "width": "100px"  ,"class":"uncost_usd",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data,3);
    				  }
    	            },
    	            { "data": "UNCOST_JPY", "width": "100px","class":"uncost_jpy",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data,3);
    				  }
    	            },
    	            { "data": "UNCOST_HKD", "width": "100px","class":"uncost_hkd",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data,3);
    				  }
    	            },
    	            { "data": "UNCOST_RMB", "width": "120px","class":"uncost_rmb",
    	            	"render": function(data, type, full, meta) {
    					return '<span style="color:red;">'+eeda.numFormat(data,3)+'</span>';
    				  }
    	            },
    	            {"width": "50px",
    					"render": function(data, type, full, meta) {
    						return ((parseFloat((full.COST_RMB-full.UNCOST_RMB) / full.COST_RMB).toFixed(4))*100).toFixed(2);
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
      
      $('.complex_search').click(function(event) {
          if($('.search_single').is(':visible')){
            $('.search_single').hide();
          }else{
            $('.search_single').show();
          }
      });
      
    //简单查询
      $('#selected_field').change(function(event) {
	      var selectField = $('#selected_field').val();
	      if(selectField=='car_id'){
	    	  $("#single_car_id_input").val("");
	    	  $("#charge_time_show").hide();
	    	  $("#car_id_show").show();
	      }
	      if(selectField=='charge_time'){
	    	  $("#single_charge_time_begin_time").val("");
	    	  $("#single_charge_time_end_time").val("");
	    	  $("#charge_time_show").show();
	    	  $("#car_id_show").hide();
	      }
     });
      
      $("#singleSearchBtn").click(function(){
    	  var selectField = $('#selected_field').val();
    	  if(selectField=='car_id'){
	    	 var car_id = $("#single_car_id").val();
	      }
    	  if(selectField=='charge_time'){
 	    	 var single_charge_time_begin_time = $("#single_charge_time_begin_time").val();
 	    	 var single_charge_time_end_time = $("#single_charge_time_end_time").val();
 	      }
	      
	      
    	//合计字段
          $.post('transCostBalanceReport/listTotal',{
         	 car_id:car_id,
         	 charge_time_begin_time:single_charge_time_begin_time,
         	 charge_time_end_time:single_charge_time_end_time
          },function(data){
        	  var cost_cny = parseFloat(data.COST_CNY).toFixed(2);
        	  var cost_usd = parseFloat(data.COST_USD).toFixed(2);
        	  var cost_jpy = parseFloat(data.COST_JPY).toFixed(2);
        	  var cost_hkd = parseFloat(data.COST_HKD).toFixed(2);
        	  var total_cost = parseFloat(data.TOTAL_COST).toFixed(2);
        	  var uncost_cny = parseFloat(data.UNCOST_CNY).toFixed(2);
        	  var uncost_usd = parseFloat(data.UNCOST_USD).toFixed(2);
        	  var uncost_jpy = parseFloat(data.UNCOST_JPY).toFixed(2);
        	  var uncost_hkd = parseFloat(data.UNCOST_HKD).toFixed(2);
        	  var total_uncost = parseFloat(data.TOTAL_UNCOST).toFixed(2);
        	  $('#CNY_cost_tatol').text(eeda.numFormat(cost_cny,3));
        	  $('#USD_cost_tatol').text(eeda.numFormat(cost_usd,3));
        	  $('#JPY_cost_tatol').text(eeda.numFormat(cost_jpy,3));
        	  $('#HKD_cost_tatol').text(eeda.numFormat(cost_hkd,3));
        	  $('#total_cost').text(eeda.numFormat(total_cost,3));
        	  $('#CNY_uncost_tatol').text(eeda.numFormat(uncost_cny,3)).css('color','red');
        	  $('#USD_uncost_tatol').text(eeda.numFormat(uncost_usd,3)).css('color','red');
        	  $('#JPY_uncost_tatol').text(eeda.numFormat(uncost_jpy,3)).css('color','red');
        	  $('#HKD_uncost_tatol').text(eeda.numFormat(uncost_hkd,3)).css('color','red');
        	  $('#total_uncost').text(eeda.numFormat(total_uncost,3)).css('color','red');
        	 var total=parseFloat(data.TOTAL);
   	/*  $($('.dataTables_scrollFoot tr')[0]).find('th[class=abbr]').html('共'+total+'项汇总：');
   	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_cny]').html("CNY:"+eeda.numFormat(cost_cny,3));
   	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_usd]').html("USD:"+eeda.numFormat(cost_usd,3));
   	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_jpy]').html("JPY:"+eeda.numFormat(cost_jpy,3));
   	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_hkd]').html("HKD:"+eeda.numFormat(cost_hkd,3));
   	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_rmb]').html("应付折合(CNY):"+eeda.numFormat(total_cost,3));
   	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncost_cny]').html("CNY:"+eeda.numFormat(uncost_cny,3)).css('color','red');
   	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncost_usd]').html("USD:"+eeda.numFormat(uncost_usd,3)).css('color','red');
   	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncost_jpy]').html("JPY:"+eeda.numFormat(uncost_jpy,3)).css('color','red');
   	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncost_hkd]').html("HKD:"+eeda.numFormat(uncost_hkd,3)).css('color','red');
   	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncost_rmb]').html("未付折合(CNY):"+eeda.numFormat(total_uncost,3)).css('color','red');*/
        	  
        	  var total_profit=parseFloat(total_cost-total_uncost).toFixed(2);
        	  if(total_profit<0){
        		  $('#total_profit').text(total_profit).css('color','red');
        	  }else(
        		  $('#total_profit').text(total_profit)
        	  )

          });
	      
          var url = "/transCostBalanceReport/list?car_id="+car_id
               +"&charge_time_begin_time="+single_charge_time_begin_time
               +"&charge_time_end_time="+single_charge_time_end_time
          dataTable.ajax.url(url).load(cssTd);
      });
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })
      
      var cssTd=function(){
    	  $("#eeda_table td:nth-child(6)").css('background-color','#f5f5dc');
    	  $("#eeda_table td:nth-child(11)").css('background-color','#f5f5dc');
      }

     var searchData=function(){
    	 var car_id = $("#car_id").val();
    	 var charge_time_begin_time = $("#charge_time_begin_time").val();
         var charge_time_end_time = $("#charge_time_end_time").val();
         
         
         
       //合计字段
         $.post('transCostBalanceReport/listTotal',{
        	 car_id:car_id,
        	 charge_time_begin_time:charge_time_begin_time,
        	 charge_time_end_time:charge_time_end_time
         },function(data){
       	  var cost_cny = parseFloat(data.COST_CNY).toFixed(2);
       	  var cost_usd = parseFloat(data.COST_USD).toFixed(2);
       	  var cost_jpy = parseFloat(data.COST_JPY).toFixed(2);
       	  var cost_hkd = parseFloat(data.COST_HKD).toFixed(2);
       	  var total_cost = parseFloat(data.TOTAL_COST).toFixed(2);
       	  var uncost_cny = parseFloat(data.UNCOST_CNY).toFixed(2);
       	  var uncost_usd = parseFloat(data.UNCOST_USD).toFixed(2);
       	  var uncost_jpy = parseFloat(data.UNCOST_JPY).toFixed(2);
       	  var uncost_hkd = parseFloat(data.UNCOST_HKD).toFixed(2);
       	  var total_uncost = parseFloat(data.TOTAL_UNCOST).toFixed(2);
       	  $('#CNY_cost_tatol').text(eeda.numFormat(cost_cny,3));
       	  $('#USD_cost_tatol').text(eeda.numFormat(cost_usd,3));
       	  $('#JPY_cost_tatol').text(eeda.numFormat(cost_jpy,3));
       	  $('#HKD_cost_tatol').text(eeda.numFormat(cost_hkd,3));
       	  $('#total_cost').text(eeda.numFormat(total_cost,3));
       	  $('#CNY_uncost_tatol').text(eeda.numFormat(uncost_cny,3)).css('color','red');
       	  $('#USD_uncost_tatol').text(eeda.numFormat(uncost_usd,3)).css('color','red');
       	  $('#JPY_uncost_tatol').text(eeda.numFormat(uncost_jpy,3)).css('color','red');
       	  $('#HKD_uncost_tatol').text(eeda.numFormat(uncost_hkd,3)).css('color','red');
       	  $('#total_uncost').text(eeda.numFormat(total_uncost,3)).css('color','red');
       	 var total=parseFloat(data.TOTAL);
       	 
  	  /*$($('.dataTables_scrollFoot tr')[0]).find('th[class=abbr]').html('共'+total+'项汇总：');
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_cny]').html("CNY:"+eeda.numFormat(cost_cny,3));
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_usd]').html("USD:"+eeda.numFormat(cost_usd,3));
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_jpy]').html("JPY:"+eeda.numFormat(cost_jpy,3));
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_hkd]').html("HKD:"+eeda.numFormat(cost_hkd,3));
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_rmb]').html("应付折合(CNY):"+eeda.numFormat(total_cost,3));
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncost_cny]').html("CNY:"+eeda.numFormat(uncost_cny,3)).css('color','red');
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncost_usd]').html("USD:"+eeda.numFormat(uncost_usd,3)).css('color','red');
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncost_jpy]').html("JPY:"+eeda.numFormat(uncost_jpy,3)).css('color','red');
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncost_hkd]').html("HKD:"+eeda.numFormat(uncost_hkd,3)).css('color','red');
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncost_rmb]').html("未付折合(CNY):"+eeda.numFormat(total_uncost,3)).css('color','red');*/
       	  
       	  var total_profit=parseFloat(total_cost-total_uncost).toFixed(2);
       	  if(total_profit<0){
       		  $('#total_profit').text(total_profit).css('color','red');
       	  }else(
       		  $('#total_profit').text(total_profit)
       	  )

         });
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/transCostBalanceReport/list?car_id="+car_id
          				+"&charge_time_begin_time="+charge_time_begin_time
				        +"&charge_time_end_time="+charge_time_end_time;
          dataTable.ajax.url(url).load(cssTd);
          
      };
      
      searchData();
      
    //td列背景颜色
      var tableStyle = function(){
    	  $('.oneRow').css('line-height','30px');
    	  $('.doubleRow').css('text-align','center');
    	  
    	  var tableName = "eeda_table";
    	  //格式【合成表头的第一列位置，合成的列数，颜色】
    	  var array= [[2,5,'#f8fff0'],[7,5,'#eeffff']];
    	  for (var i = 0; i < array.length; i++) {
    		  var firstChild = array[i][0];
        	  var cols = array[i][1];
        	  var bgColor = array[i][2];
        	  for (var j = firstChild; j < (firstChild+cols); j++) {
        		  $("#"+tableName+" td:nth-child("+j+")").css('background-color',bgColor);
        	  }
		  }
      }
      
      var hideColumn = function(){         	
       	//隐藏对usd和jpy列
          	var dataTable = $('#eeda_table').dataTable();
          	dataTable.fnSetColumnVis(3, false);
          	dataTable.fnSetColumnVis(4, false);
          	dataTable.fnSetColumnVis(8, false);
          	dataTable.fnSetColumnVis(9, false);
      }
  });
});