define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco',  'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {
	  $('.search_single input,.search_single select').on('input',function(){
  		  $("#orderForm")[0].reset();
  	  });
	  
      eeda.hideSideBar();//打开报表时自动收起左边菜单
      
      var windowHeight = $(window).height();        //获取浏览器窗口高度
      var headerHeight =  $("#eeda_table").offset().top;//判断是否到达窗口顶部
      var page = windowHeight - headerHeight-100;
      $('.paDiv').css("height",windowHeight - headerHeight);
      var dataTable = eeda.dt({
          id: 'eeda_table',
          colReorder: true,
          paging: false,
          scrollY: page,
          serverSide: false, //不打开会出现排序不对 
         // ajax: "/costBalanceReport/list",
          initComplete:function(settings){
        	  tableStyle();
              },
          columns: [
          			{ "data": "ABBR", "width": "120px","class":"abbr"},
          			{ "data": "COST_CNY", "width": "100px","class":"cost_cny",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data.toFixed(2),3);
    				  }
    	            },
    	            { "data": "COST_USD", "width": "100px","class":"cost_usd"  ,
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
    	            { "data": "COST_RMB", "width": "100px","class":"cost_rmb",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data.toFixed(2),3);
    				  }
    	            },
    	            { "data": "UNCOST_CNY", "width": "100px","class":"uncost_cny",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data.toFixed(2),3);
    				  }
    	            },
    	            { "data": "UNCOST_USD", "width": "100px"  ,"class":"uncost_usd",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data.toFixed(2),3);
    				  }
    	            },
    	            { "data": "UNCOST_JPY", "width": "100px","class":"uncost_jpy",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data.toFixed(2),3);
    				  }
    	            },
    	            { "data": "UNCOST_HKD", "width": "100px","class":"uncost_hkd",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data.toFixed(2),3);
    				  }
    	            },
    	            { "data": "UNCOST_RMB", "width": "100px","class":"uncost_rmb",
    	            	"render": function(data, type, full, meta) {
    					return '<span style="color:red;">'+eeda.numFormat(data.toFixed(2),3)+'</span>';
    				  }
    	            },
    	            { "width": "80px",
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
	      if(selectField=='sp_id'){
	    	  $("#single_order_export_date_begin_time").val("");
	    	  $("#single_order_export_date_end_time").val("");
	    	  $("#order_export_date_show").hide();
	    	  $("#sp_id_show").show();
	      }
	      if(selectField=="order_export_date"){
	    	  $("#single_sp_id").val("");
	    	  $("#single_sp_id_input").val("");
	    	  $("#sp_id_show").hide();
	    	  $("#order_export_date_show").show();
	      }
     });
	
	$('#singleSearchBtn').click(function(){
		$("#orderForm")[0].reset();
		var selectField = $("#selected_field").val();
		if(selectField=='sp_id'){
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
      
      var cssTd=function(){
    	  $("#eeda_table td:nth-child(6)").css('background-color','#f5f5dc');
    	  $("#eeda_table td:nth-child(11)").css('background-color','#f5f5dc');
      }

     var searchData=function(){
    	 var sp_id = $("#sp_id").val();
         var abbr_name = $('#sp_id_input').val();
         var order_export_date_begin_time = $("#order_export_date_begin_time").val();
         var order_export_date_end_time = $("#order_export_date_end_time").val();
         
         
         
       //合计字段
         $.post('costBalanceReport/listTotal',{
       	  sp_id:sp_id,
       	  abbr_like:abbr_name,
       	  order_export_date_begin_time:order_export_date_begin_time,
       	  order_export_date_end_time:order_export_date_end_time
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
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=abbr]').html('共'+total+'项汇总：');
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_cny]').html("应付CNY:<br>"+eeda.numFormat(cost_cny,3));
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_usd]').html("应付USD:<br>"+eeda.numFormat(cost_usd,3));
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_jpy]').html("应付JPY:<br>"+eeda.numFormat(cost_jpy,3));
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_hkd]').html("应付HKD:<br>"+eeda.numFormat(cost_hkd,3));
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=cost_rmb]').html("应付折合(CNY):<br>"+eeda.numFormat(total_cost,3));
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncost_cny]').html("未付CNY:<br>"+eeda.numFormat(uncost_cny,3)).css('color','red');
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncost_usd]').html("未付USD:<br>"+eeda.numFormat(uncost_usd,3)).css('color','red');
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncost_jpy]').html("未付JPY:<br>"+eeda.numFormat(uncost_jpy,3)).css('color','red');
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncost_hkd]').html("未付HKD:<br>"+eeda.numFormat(uncost_hkd,3)).css('color','red');
  	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=uncost_rmb]').html("未付折合(CNY):<br>"+eeda.numFormat(total_uncost,3)).css('color','red');
       	  
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
          var url = "/costBalanceReport/list?sp_id="+sp_id
          				+"&abbr_like="+abbr_name
          				+"&order_export_date_begin_time="+order_export_date_begin_time
				        +"&order_export_date_end_time="+order_export_date_end_time;
          dataTable.ajax.url(url).load(tableStyle);
          
      };
      
     // searchData();
      
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
		  $.post('/costBalanceReport/downloadExcelList',{sp_id:sp_id,begin_time:begin_time,end_time:end_time}, function(data){
	          $('#exportTotaledExcel1').prop('disabled', false);
	          $('#exportTotaledExcel').prop('disabled', false);
	          $('#singlexportTotaledExcel').prop('disabled', false);
	          $.scojs_message('生成应收Excel对账单成功', $.scojs_message.TYPE_OK);
	          window.open(data);
	      }).fail(function() {
	          $('#exportTotaledExcel').prop('disabled', false);
	          $.scojs_message('生成应收Excel对账单失败', $.scojs_message.TYPE_ERROR);
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