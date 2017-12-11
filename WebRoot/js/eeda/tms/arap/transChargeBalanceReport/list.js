define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco',  'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {
	  $('.search_single input,.search_single select').on('input',function(){
		  $("#orderForm")[0].reset();
	  });
  	  
      var dataTable = eeda.dt({
          id: 'eeda_table',
          colReorder: true,
          paging: false,
          serverSide: true, //不打开会出现排序不对 
          ajax: "/transChargeBalanceReport/list",
          initComplete:function(settings){
    	  cssTd();
          tableStyle();
          hideColumn();
          },
          columns: [
      			{ "data": "ABBR", "className":"abbr"},
      			{ "data": "CHARGE_CNY", "className":"charge_cny",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
            	    data = (parseFloat(data)).toFixed(2)
                      if(isNaN(data)){
                      	data = "";
                      }
            	    return data
				  }
	            },
	            { "data": "CHARGE_USD","className":"charge_usd" ,
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
            	    data = (parseFloat(data)).toFixed(2)
                    if(isNaN(data)){
                    	data = "";
                    }
            	    return data
				  }
	            },
	            { "data": "CHARGE_JPY","className":"charge_jpy",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
            	    data = (parseFloat(data)).toFixed(2)
                    if(isNaN(data)){
                    	data = "";
                    }
            	    return data
				  }
	            },
	            { "data": "CHARGE_HKD","className":"charge_hkd",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
            	    data = (parseFloat(data)).toFixed(2)
                    if(isNaN(data)){
                    	data = "";
                    }
            	    return data
				  }
	            },
	            { "data": "CHARGE_RMB","className":"charge_rmb",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
            	    data = (parseFloat(data)).toFixed(2)
                    if(isNaN(data)){
                    	data = "";
                    }
            	    return data
				  }
	            },
	            { "data": "UNCHARGE_CNY","className":"uncharge_cny",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
            	    data = (parseFloat(data)).toFixed(2)
                    if(isNaN(data)){
                    	data = "";
                    }
            	    return data
				  }
	            },
	            { "data": "UNCHARGE_USD","className":"uncharge_usd",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
            	    data = (parseFloat(data)).toFixed(2)
                    if(isNaN(data)){
                    	data = "";
                    }
            	    return data
				  }
	            },
	            { "data": "UNCHARGE_JPY","className":"uncharge_jpy",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
            	    data = (parseFloat(data)).toFixed(2)
                    if(isNaN(data)){
                    	data = "";
                    }
            	    return data
				  }
	            },
	            { "data": "UNCHARGE_HKD", "className":"uncharge_hkd",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
            	    data = (parseFloat(data)).toFixed(2)
                    if(isNaN(data)){
                    	data = "";
                    }
            	    return data
				  }
	            },
	            { "data": "UNCHARGE_RMB","class":"uncharge_rmb",
	            	"render": function(data, type, full, meta) {
	            		 data = (parseFloat(data)).toFixed(2)
	                     if(isNaN(data)){
	                     	data = "";
	                     }
					return '<span style="color:red;">'+data+'</span>';
				  }
	            },
	            {
					"render": function(data, type, full, meta) {
						return ((parseFloat((full.CHARGE_RMB-full.UNCHARGE_RMB) / full.CHARGE_RMB).toFixed(4))*100).toFixed(2);
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
	      if(selectField=='sp_id'){
	    	  $("#single_charge_time_begin_time").val("");
	    	  $("#single_charge_time_end_time").val("");
	    	  $("#charge_time_show").hide();
	    	  $("#sp_id_show").show();
	      }
	      if(selectField=='charge_time'){
	    	  $("#single_sp_id_input").val("");
	    	  $("#single_sp_id").val("");
	    	  $("#charge_time_show").show();
	    	  $("#sp_id_show").hide();
	      }
     });
      
      $("#singleSearchBtn").click(function(){
    	  $("#orderForm")[0].reset();
    	  var selectField = $('#selected_field').val();
    	  if(selectField=='sp_id'){
	    	 $("#sp_id").val($("#single_sp_id").val());
	    	 $("#sp_id_input").val($("#single_sp_id_input").val());
	      }
	      if(selectField=='charge_time'){
	    	 $("#charge_time_begin_time").val($("#single_charge_time_begin_time").val());
	    	 $("#charge_time_end_time").val($("#single_charge_time_end_time").val());
	      }
	      $('#searchBtn').click();
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
          var sp_id = $("#sp_id").val();
          var sp_name = $("#sp_id_input").val();
          var charge_time_begin_time = $("#charge_time_begin_time").val();
          var charge_time_end_time = $("#charge_time_end_time").val();
          
        //合计字段
          $.post('transChargeBalanceReport/listTotal',{
	    	  sp_id:sp_id,
	    	  charge_time_begin_time:charge_time_begin_time,
        	  charge_time_end_time:charge_time_end_time
	      },function(data){
	    	  var charge_cny = parseFloat(data.CHARGE_CNY).toFixed(2);
	    	  var charge_usd = parseFloat(data.CHARGE_USD).toFixed(2);
	    	  var charge_jpy = parseFloat(data.CHARGE_JPY).toFixed(2);
	    	  var charge_hkd = parseFloat(data.CHARGE_HKD).toFixed(2);
	    	  var total_charge = parseFloat(data.TOTAL_CHARGE).toFixed(2);
	    	  var uncharge_cny = parseFloat(data.UNCHARGE_CNY).toFixed(2);
	    	  var uncharge_usd = parseFloat(data.UNCHARGE_USD).toFixed(2);
	    	  var uncharge_jpy = parseFloat(data.UNCHARGE_JPY).toFixed(2);
	    	  var uncharge_hkd = parseFloat(data.UNCHARGE_HKD).toFixed(2);
	    	  var total_uncharge = parseFloat(data.TOTAL_UNCHARGE).toFixed(2);
	    	  var total=parseFloat(data.TOTAL);
	    	  $('#CNY_charge_tatol').text(eeda.numFormat(charge_cny,3));
	    	  $('#USD_charge_tatol').text(eeda.numFormat(charge_usd,3));
	    	  $('#JPY_charge_tatol').text(eeda.numFormat(charge_jpy,3));
	    	  $('#HKD_charge_tatol').text(eeda.numFormat(charge_hkd,3));
	    	  $('#total_charge').text(eeda.numFormat(total_charge,3));
	    	  $('#CNY_uncharge_tatol').text(eeda.numFormat(uncharge_cny,3)).css('color','red');
	    	  $('#USD_uncharge_tatol').text(eeda.numFormat(uncharge_usd,3)).css('color','red');
	    	  $('#JPY_uncharge_tatol').text(eeda.numFormat(uncharge_jpy,3)).css('color','red');
	    	  $('#HKD_uncharge_tatol').text(eeda.numFormat(uncharge_hkd,3)).css('color','red');
	    	  $('#total_uncharge').text(eeda.numFormat(total_uncharge,3)).css('color','red');
	    	  
	    	  /*$($('.dataTables_scrollFoot tr')[0]).find('th[class=abbr]').html('共'+total+'项汇总：');
	    	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_cny]').html("CNY:"+eeda.numFormat(charge_cny,3));
	    	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_usd]').html("USD:"+eeda.numFormat(charge_usd,3));
	    	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_jpy]').html("JPY:"+eeda.numFormat(charge_jpy,3));
	    	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_hkd]').html("HKD:"+eeda.numFormat(charge_hkd,3));
	    	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_rmb]').html("应收折合(CNY):"+eeda.numFormat(total_charge,3));
	    	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncharge_cny]').html("CNY:"+eeda.numFormat(uncharge_cny,3)).css('color','red');
	    	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncharge_usd]').html("USD:"+eeda.numFormat(uncharge_usd,3)).css('color','red');
	    	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncharge_jpy]').html("JPY:"+eeda.numFormat(uncharge_jpy,3)).css('color','red');
	    	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncharge_hkd]').html("HKD:"+eeda.numFormat(uncharge_hkd,3)).css('color','red');
	    	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncharge_rmb]').html("未收折合(CNY):"+eeda.numFormat(total_uncharge,3)).css('color','red');*/
	    	  
	    	  var total_profit=parseFloat(total_charge-total_uncharge).toFixed(2);
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
          var url = "/transChargeBalanceReport/list?sp_id="+sp_id
          +"&p.abbr_like="+sp_name
          +"&charge_time_begin_time="+charge_time_begin_time
          +"&charge_time_end_time="+charge_time_end_time;
          dataTable.ajax.url(url).load();
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
            	dataTable.fnSetColumnVis(8, false);
        }
      
      //导出excel
      $('#exportTotaledExcel1').click(function(){
    	  $(this).attr('disabled', true);
          var sp_id = $("#single_sp_id").val();
          var begin_time = $("#single_charge_time_begin_time").val();
          var end_time = $("#single_charge_time_end_time").val();
          excel_method(sp_id,begin_time,end_time);
      });
      $('#exportTotaledExcel').click(function(){
    	  $(this).attr('disabled', true);
          var sp_id = $("#sp_id").val();
          var begin_time = $("#charge_time_begin_time").val();
          var end_time = $("#charge_time_end_time").val();
          excel_method(sp_id,begin_time,end_time);
      });
      var excel_method = function(sp_id,begin_time,end_time){
		  $.post('/transChargeBalanceReport/downloadExcelList',{sp_id:sp_id,begin_time:begin_time,end_time:end_time}, function(data){
	          $('#exportTotaledExcel1').prop('disabled', false);
	          $('#exportTotaledExcel').prop('disabled', false);
	          $('#singlexportTotaledExcel').prop('disabled', false);
	          $.scojs_message('生成Excel成功', $.scojs_message.TYPE_OK);
	          window.open(data);
	      }).fail(function() {
	          $('#exportTotaledExcel').prop('disabled', false);
	          $.scojs_message('生成Excel失败', $.scojs_message.TYPE_ERROR);
	      });
      }
  });
});