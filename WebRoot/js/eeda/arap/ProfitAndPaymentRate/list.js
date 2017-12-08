define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco',  'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {
  	  
      eeda.hideSideBar();//打开报表时自动收起左边菜单
      
      var windowHeight = $(window).height();        //获取浏览器窗口高度
      var headerHeight =  $("#eeda_table").offset().top;//判断是否到达窗口顶部
      var page = windowHeight - headerHeight-80;
      $('.paDiv').css("height",windowHeight - headerHeight);
      var dataTable = eeda.dt({
          id: 'eeda_table',
          colReorder: true,
          scrollY: page,
          paging: false,
          serverSide: false, //不打开会出现排序不对 
/*//          ajax: "/profitAndPaymentRate/list",
          ajax:{
              //url: "/chargeCheckOrder/list",
              type: 'POST'
          },*/
          initComplete:function(settings){
        	  tableStyle();
          },
          columns: [
      			{ "data": "ABBR", "width": "100px","class":"abbr"},
	            { "data": "CHARGE_CNY", "width": "80px","class":"charge_cny",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            { "data": "CHARGE_USD", "width": "100px"  ,"class":"charge_usd",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            { "data": "CHARGE_JPY", "width": "100px","class":"charge_jpy",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            { "data": "CHARGE_HKD", "width": "100px","class":"charge_hkd",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            { "data": "CHARGE_RMB", "width": "120px","class":"charge_rmb",
	            	"render": function(data, type, full, meta) {
            	    if(!data){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            { "data": "COST_CNY", "width": "100px" ,"class":"cost_cny",
	            	"render": function(data, type, full, meta) {
            	    if(!data){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            { "data": "COST_USD", "width": "100px","class":"cost_usd",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            { "data": "COST_JPY", "width": "100px","class":"cost_jpy",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            { "data": "COST_HKD", "width": "100px","class":"cost_hkd",
	            	"render": function(data, type, full, meta) {
            	    if(data==0){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            { "data": "COST_RMB", "width": "120px" ,"class":"cost_rmb",
	            	"render": function(data, type, full, meta) {
            	    if(!data){
            	    	return '';
            	    }
					return eeda.numFormat(data.toFixed(2),3);
				  }
	            }]
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
	    	  $("#single_order_export_date_begin_time").val("");
	    	  $("#single_order_export_date_end_time").val("");
	    	  $("#order_export_date_show").hide();
	    	  $("#sp_id_show").show();
	      }
	      if(selectField=="order_export_date"){
	    	  $("#single_sp_id_input").val("");
	    	  $("#single_sp_id").val("");
	    	  $("#sp_id_show").hide();
	    	  $("#order_export_date_show").show();
	      }
     });
	
	$('#singleSearchBtn').click(function(){
		$("#orderForm")[0].reset();
	     var selectField = $('#selected_field').val();
	     if(selectField=='sp_id'){
	    	 $("#sp_id").val($("#single_sp_id").val());
	    	 $("#sp_id_input").val($("#single_sp_id_input").val());
	      }
	      if(selectField=="order_export_date"){
	    	 $("#order_export_date_begin_time").val($("#single_order_export_date_begin_time").val());
	    	 $("#order_export_date_end_time").val($("#single_order_export_date_end_time").val());
	      }
	      $('#searchBtn').click();
	}); 
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
          var sp_id = $("#sp_id").val(); 
          var sp_name = $("#sp_id_input").val(); 
          var order_export_date_begin_time = $("#order_export_date_begin_time").val();
          var order_export_date_end_time = $("#order_export_date_end_time").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          
          //合计字段
          $.post('profitAndPaymentRate/listTotal',{
        	  sp_id:sp_id,
        	  abbr_like:sp_name,
        	  order_export_date_begin_time:order_export_date_begin_time,
        	  order_export_date_end_time:order_export_date_end_time
          },function(data){
        	  var charge_cny = parseFloat(data.CHARGE_CNY).toFixed(2);
        	  var charge_usd = parseFloat(data.CHARGE_USD).toFixed(2);
        	  var charge_jpy = parseFloat(data.CHARGE_JPY).toFixed(2);
        	  var charge_hkd = parseFloat(data.CHARGE_HKD).toFixed(2);
        	  var total_charge = parseFloat(data.TOTAL_CHARGE).toFixed(2);
        	  var cost_cny = parseFloat(data.COST_CNY).toFixed(2);
        	  var cost_usd = parseFloat(data.COST_USD).toFixed(2);
        	  var cost_jpy = parseFloat(data.COST_JPY).toFixed(2);
        	  var cost_hkd = parseFloat(data.COST_HKD).toFixed(2);
        	  var total_cost = parseFloat(data.TOTAL_COST).toFixed(2);
        	  $('#CNY_charge_tatol').text(charge_cny);
        	  $('#USD_charge_tatol').text(charge_usd);
        	  $('#JPY_charge_tatol').text(charge_jpy);
        	  $('#HKD_charge_tatol').text(charge_hkd);
        	  $('#total_charge').text(total_charge);
        	  $('#CNY_cost_tatol').text(cost_cny);
        	  $('#USD_cost_tatol').text(cost_usd);
        	  $('#JPY_cost_tatol').text(cost_jpy);
        	  $('#HKD_cost_tatol').text(cost_hkd);
        	  $('#total_cost').text(total_cost);
        	  var total=parseFloat(data.TOTAL);
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=abbr]').html('共'+total+'项汇总：');
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_cny]').html("应收CNY:<br>"+eeda.numFormat(charge_cny,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_usd]').html("应收USD:<br>"+eeda.numFormat(charge_usd,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_jpy]').html("应收JPY:<br>"+eeda.numFormat(charge_jpy,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_hkd]').html("应收HKD:<br>"+eeda.numFormat(charge_hkd,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_cny]').html("应付CNY:<br>"+eeda.numFormat(cost_cny,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_usd]').html("应付USD:<br>"+eeda.numFormat(cost_usd,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_jpy]').html("应付JPY:<br>"+eeda.numFormat(cost_jpy,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_hkd]').html("应付HKD:<br>"+eeda.numFormat(cost_hkd,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=charge_rmb]').html("折合应收(RMB):<br>"+eeda.numFormat(total_charge,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_rmb]').html("折合应付(RMB):<br>"+eeda.numFormat(total_cost,3));
        	  
        	  var total_profit=parseFloat(total_charge-total_cost).toFixed(2);
        	  if(total_profit<0){
        		  $('#total_profit').text(total_profit).css('color','red');
        	  }else(
        		  $('#total_profit').text(total_profit)
        	  )

          });
          
          var url = "/profitAndPaymentRate/list?sp_id="+sp_id
          				  +"&abbr_like="+sp_name
				          +"&order_export_date_begin_time="+order_export_date_begin_time
				          +"&order_export_date_end_time="+order_export_date_end_time;
          dataTable.ajax.url(url).load(tableStyle);
          
      };
      
    //  searchData(); 
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
      
      //导出excel
      $('#exportTotaledExcel1').click(function(){
    	  $(this).attr('disabled', true);
          var sp_id = $("#single_sp_id").val();
          var begin_time = $("#single_order_export_date_begin_time").val();
          var end_time = $("#single_order_export_date_end_time").val();
          excel_method(sp_id,begin_time,end_time);
      });
      $('#exportTotaledExcel').click(function(){
    	  $(this).attr('disabled', true);
          var sp_id = $("#sp_id").val();
          var begin_time = $("#order_export_date_begin_time").val();
          var end_time = $("#order_export_date_end_time").val();
          excel_method(sp_id,begin_time,end_time);
      });
      var excel_method = function(sp_id,begin_time,end_time){
		  $.post('/profitAndPaymentRate/downloadExcelList',{sp_id:sp_id,begin_time:begin_time,end_time:end_time}, function(data){
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
      
    //表头浮动
	  $(function(){
          var scroll_bar = $("#eeda_table");//表格的id
          var bar_head = $("#eeda_table_head");//表头
          var head_top_height = 0.0;
          $(window).scroll(function(){
        	  if(head_top_height == 0){
        	  	head_top_height = bar_head.offset().top;
        	  }
              var scroll_top = $('body').scrollTop() - head_top_height;//判断是否到达窗口顶部
              var scroll_botton =$('body').scrollTop() - $("#eeda_table_info").offset().top+50 ;
              if (scroll_top > 0 && scroll_botton < 0) {
            	  bar_head.css({'z-index':'1','border-radius':'15px 15px 0 0','position':'fixed','top':'0','width':$("#eeda_table").width()});
              }else {
            	  bar_head.css({'z-index':'auto','position':'static','top':'auto','width':$("#eeda_table").width()});
              }
          });
      });
  });
});