define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '应付余额汇总表  | '+document.title;
  	  
  	  $("#breadcrumb_li").text('应付余额汇总表');
  	  $('#menu_cost').addClass('active').find('ul').removeClass('in');
  	  
  	  
      var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: false,
          serverSide: true, //不打开会出现排序不对 
          ajax: "/costBalanceReport/list",
          initComplete:function(settings){
        	  cssTd();
              },
          columns: [
          			{ "data": "ABBR", "width": "120px"},
          			{ "data": "COST_CNY", "width": "100px",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data,3);
    				  }
    	            },
    	            { "data": "COST_USD", "width": "100px"  ,
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data,3);
    				  }
    	            },
    	            { "data": "COST_JPY", "width": "100px",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data,3);
    				  }
    	            },
    	            { "data": "COST_HKD", "width": "100px",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data,3);
    				  }
    	            },
    	            { "data": "COST_RMB", "width": "100px",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data,3);
    				  }
    	            },
    	            { "data": "UNCOST_CNY", "width": "100px",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data,3);
    				  }
    	            },
    	            { "data": "UNCOST_USD", "width": "100px"  ,
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data,3);
    				  }
    	            },
    	            { "data": "UNCOST_JPY", "width": "100px",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data,3);
    				  }
    	            },
    	            { "data": "UNCOST_HKD", "width": "100px",
    	            	"render": function(data, type, full, meta) {
                	    if(data==0){
                	    	return '';
                	    }
    					return eeda.numFormat(data,3);
    				  }
    	            },
    	            { "data": "UNCOST_RMB", "width": "100px",
    	            	"render": function(data, type, full, meta) {
    					return '<span style="color:red;">'+eeda.numFormat(data,3)+'</span>';
    				  }
    	            },
    	            {
    					"render": function(data, type, full, meta) {
    						return ((parseFloat((full.COST_RMB-full.UNCOST_RMB) / full.COST_RMB).toFixed(4))*100).toFixed(2);
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
      
      var cssTd=function(){
    	  $("#eeda_table td:nth-child(6)").css('background-color','#f5f5dc');
    	  $("#eeda_table td:nth-child(11)").css('background-color','#f5f5dc');
      }

     var searchData=function(){
    	 var sp_id = $("#sp_id").val();
         var abbr_name=$('#sp_id_input').val();
         var order_export_date_begin_time = $("#order_export_date_begin_time").val();
         var order_export_date_end_time = $("#order_export_date_end_time").val();
         
         
         
       //合计字段
         $.post('costBalanceReport/listTotal',{
       	  sp_id:sp_id,
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
  	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(0).html('共'+total+'项汇总：');
  	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(1).html("CNY:"+eeda.numFormat(cost_cny,3));
  	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(2).html("USD:"+eeda.numFormat(cost_usd,3));
  	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(3).html("JPY:"+eeda.numFormat(cost_jpy,3));
  	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(4).html("HKD:"+eeda.numFormat(cost_hkd,3));
  	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(5).html("应付折合(CNY):"+eeda.numFormat(total_cost,3));
  	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(6).html("CNY:"+eeda.numFormat(uncost_cny,3)).css('color','red');
  	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(7).html("USD:"+eeda.numFormat(uncost_usd,3)).css('color','red');
  	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(8).html("JPY:"+eeda.numFormat(uncost_jpy,3)).css('color','red');
  	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(9).html("HKD:"+eeda.numFormat(uncost_hkd,3)).css('color','red');
  	  $($('.dataTables_scrollFoot tr')[0]).find('th').eq(10).html("未付折合(CNY):"+eeda.numFormat(total_uncost,3)).css('color','red');
       	  
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
          var url = "/costBalanceReport/list?sp="+sp_id
          				+"&abbr_equals="+abbr_name
          				+"&order_export_date_begin_time="+order_export_date_begin_time
				        +"&order_export_date_end_time="+order_export_date_end_time;
          dataTable.ajax.url(url).load(cssTd);
          
      };
      
      searchData();
  });
});