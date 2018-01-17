define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco',  'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {

      eeda.hideSideBar();//打开报表时自动收起左边菜单


      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });
      

      var getConditon = function(){
    	  //获取条件列
    	  var order = {};
    	  var show_line = [];
    	  $('#show_line :checked').each(function(){
    		  var value = $(this).parent().text().trim();
    		  show_line.push(value);
    	  });
    	  var summary = [];
    	  $('#summary :checked').each(function(){
    		  var value = $(this).parent().text().trim();
    		  summary.push(value);
    	  });
    	  var summary_show = [];
//    	  $('#summary_show :checked').each(function(){
//    		  var value = $(this).parent().text().trim();
//    		  summary_show.push(value);
//    	  });
    	  var search_condition = [];
    	  $('#search_condition :checked').each(function(){
    		  var value = $(this).parent().text().trim();
    		  search_condition.push(value);
    	  });
    	  order.show_line = show_line;
    	  order.summary = summary;
    	  order.summary_show = summary_show;
    	  order.search_condition = search_condition;
    	  return JSON.stringify(order);
      }
      
      $('#confirmBtn').on('click',function(){
    	  var  self= this;
    	  var order = getConditon();
    	  
    	  self.disabled = true;
    	  $.post('/manualReport/save',{jsonStr:order},function(data){
    		  if(data){
    			  $.scojs_message('操作成功', $.scojs_message.TYPE_OK);
    			  window.setTimeout(function(){
    				  location.reload();
    			  },500); 
    			  
    		  }else{
    			  $.scojs_message('操作失败', $.scojs_message.TYPE_FALSE);
    		  }
    		  self.disabled = false;
    	  }).fail(function(){
    		  alert("后台报错报错！！！");
    		  self.disabled = false;
    	  });
      });
      
      var show_line_input = function(value){
    	  $('#show_line_div .inputDiv').each(function(){
    		  var label = $(this).find('label').text().trim()
    		  if(label == value){
    			  $(this).show();
    		  }
    	  });
      }
      
      
      var search_condition_value = $('#search_condition_value').val();
      if(search_condition_value != ''){
    	  var search_condition = search_condition_value.split(',');
    	  for(var int = 0;int < search_condition.length; int++){
    		  $('#search_condition [type=checkbox]').each(function(){
    			  var value = $(this).parent().text().trim();
    			  if(search_condition[int].trim() == value){
    				  this.checked = true;
    			  }
    		  });
    		  
    		  //显示查询条件
    		  show_line_input(search_condition[int].trim());
    	  }
      }
      
      //回显自定义信息
      var summary_value = $('#summary_value').val();
      if(summary_value != ''){
    	  var summary = summary_value.split(',');
    	  for(var int = 0;int < summary.length; int++){
    		  $('#summary [type=checkbox]').each(function(){
    			  var value = $(this).parent().text().trim();
    			  if(summary[int].trim() == value){
    				  this.checked = true;
    			  }
    		  });
    	  }
      }
      
      
      var windowHeight = $(window).height();        //获取浏览器窗口高度
      var headerHeight =  $("#eeda_table").offset().top;//判断是否到达窗口顶部
      var page = windowHeight - headerHeight-50;
      $('.paDiv').css("height",windowHeight - headerHeight);
      var dataTable = eeda.dt({
          id: 'eeda_table',
          colReorder: true,
          scrollY: page,
          paging: true,
          serverSide: true, //不打开会出现排序不对 
          ajax: "/manualReport/list?summary="+summary_value,
          initComplete: function( settings ) {
        	  resetCol();
        	  showTableCol();
        	  
        	  if($('#manual_id').val() != ''){
        		  $('#tableDiv').show();
        	  }
          },
          columns: [
      			{ "data": "ORDER_NO", "width": "80px",
      				"render": function ( data, type, full, meta ) {
      					return data;
      				}
      			},
      			{ "data": "CUSTOMER_NAME", "width": "120px"},
      			{ "data": "EXPORT_DATE", "width": "80px"},
      			{ "data": "BOOKING_AGENT_NAME", "width": "100px"},
      			{ "data": "CARRIER_NAME", "width": "100px"},
      			{ "data": "HEAD_CARRIER_NAME", "width": "100px"},
      			{ "data": "SONO", "width": "100px"},
      			{ "data": "HBL_NO", "width": "100px"},
      			{ "data": "MBL_NO", "width": "100px"},
      			{ "data": "VESSEL", "width": "100px"},
      			{ "data": "VOYAGE", "width": "100px"},
      			{ "data": "ROUTE", "width": "100px"},
      			{ "data": "ETD", "width": "100px"},
      			{ "data": "ETA", "width": "100px"},
      			{ "data": "SP_NAME", "width": "120px"},
      			{ "data": "FIN_NAME", "width": "80px"},
      			{ "data": "COST_CHECK_AMOUNT", "width": "80px"},
      			{ "data": "CHARGE_CHECK_AMOUNT", "width": "80px"},
      			{ "data": "COST_TOTAL_AMOUNT", "width": "80px"},
      			{ "data": "CHARGE_TOTAL_AMOUNT", "width": "80px"},
      			{ "data": "CREATOR_NAME", "width": "80px"},
      			{ "data": "CREATE_STAMP", "width": "80px"}
      			
          ]
      });
      
      var resetCol = function(){
    	  var table = $('#eeda_table').dataTable();
    	  for(var i =0; i < 22; i++){
    		  table.fnSetColumnVis(i, false);
    	  }
      }
      
      var showCol = function(value){
    	  for(var i = 0; i < 22; i++){
    		  var colText = $($('#eeda_table').DataTable().column(i).header()).text();
    		  if(colText == value){
    			  $('#eeda_table').dataTable().fnSetColumnVis(i, true);
    		  }
    	  }
      }
      
      var showTableCol = function(){
    	  var show_line_value = $('#show_line_value').val();
          if(show_line_value != ''){
        	  var show_line = show_line_value.split(',');
        	  for(var int = 0;int < show_line.length; int++){
        		  $('#show_line [type=checkbox]').each(function(){
        			  var value = $(this).parent().text().trim();
        			  if(show_line[int].trim() == value){
        				  this.checked = true;
        			  }
        		  });
        		  
        		  showCol(show_line[int].trim());
        	  } 
          }
      }
     
      
      $('#searchBtn').click(function(){
    	  searchData(); 
      })
      

     var searchData = function(){
          var customer_id = $("#customer_id").val();  
          var sp_id = $("#sp_id").val();  
          var charge_id = $("#charge_id").val();  
          var order_no = $("#order_no").val();  
          var export_date_begin_time = $("#export_date_begin_time").val();  
          var export_date_end_time = $("#export_date_end_time").val();  
          var create_stamp_begin_time = $("#create_stamp_begin_time").val();  
          var create_stamp_end_time = $("#create_stamp_end_time").val();  
          
          var url = "/manualReport/list?customer_id="+customer_id
          				  +"&sp_id="+sp_id  
          				  +"&charge_id="+charge_id
          				  +"&order_no="+order_no
				          +"&export_date_begin_time="+export_date_begin_time
				          +"&export_date_end_time="+export_date_end_time
				          +"&create_stamp_begin_time="+create_stamp_begin_time
				          +"&create_stamp_end_time="+create_stamp_end_time
				          +"&summary="+summary_value;//汇总单位
          dataTable.ajax.url(url).load();
      };
      
      
      //导出excel利润表
      $('#exportTotaledExcel').click(function(){
    	  var self = this;
    	  self.disabled = true;
          
          var customer_id = $("#customer_id").val();  
          var sp_id = $("#sp_id").val();  
          var order_no = $("#order_no").val();  
          var export_date_begin_time = $("#export_date_begin_time").val();  
          var export_date_end_time = $("#export_date_end_time").val();  
          var create_stamp_begin_time = $("#create_stamp_begin_time").val();  
          var create_stamp_end_time = $("#create_stamp_end_time").val();  
          
          $.post('/manualReport/exportExcel',{customer_id:customer_id,sp_id:sp_id,order_no:order_no,
        	  export_date_begin_time:export_date_begin_time,
        	  export_date_end_time:export_date_end_time,
        	  create_stamp_begin_time:create_stamp_begin_time,
        	  create_stamp_end_time:create_stamp_end_time,
        	  summary:$('#summary_value').val(),
        	  show_line:$('#show_line_value').val()}, function(data){
        	  if(data){
                  $.scojs_message('导出excel文件成功', $.scojs_message.TYPE_OK);
                  window.open(data);
        	  }
        	  self.disabled = false;
          }).fail(function() {
        	  self.disabled = false;
              $.scojs_message('导出excel文件失败', $.scojs_message.TYPE_ERROR);
          });
      });
      
  });
});