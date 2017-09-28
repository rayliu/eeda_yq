define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn','./import', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
          paging: true,
          serverSide: true, //不打开会出现排序不对
          ajax: "/transJobOrder/list?status=新建",
          "drawCallback": function( settings ) {
              $('.other').popover({
                  html: true,
                  container: 'body',
                  placement: 'right',
                  trigger: 'hover'
              });
		  },
          columns: [
              { "width": "10px",
                  "render": function ( data, type, full, meta ) {
                	  return '<input type = "checkBox" name = "checkBox" >';
                  }
              },
              { "data": "ORDER_NO",  "width": "80px",
                  "render": function ( data, type, full, meta ) {
                      return "<a href='/transJobOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
                  }
              },
              { "data": "CUSTOMER_NAME","width":"60px"},
              { "data": "TYPE","width":"50px",
                  "render": function ( data, type, full, meta ) {
	                    if(!data)
	                    	data='';
	                    return data;
                  }
              },
              { "data": "CONTAINER_NO","width":"60px"}, 
              { "data": "SO_NO","width":"60px"}, 
              { "data": "CABINET_DATE", "width":"72px",//提柜、提货时间
            	  "render": function(data){
            		  if(data)
            			  return data;
            		  return '';
            	  }
              },
              { "data": "CHARGE_TIME","width":"60px"},
              { "data": "CABINET_TYPE","width":"30px",
            	  "render":function(data, type, full, meta ) {
            		  if(!data){
            			  return  data="";
            		  }else{
            			  if(full.CABINET_TYPE==full.TRUCK_TYPE){
                			  return data;
                		  }else{
                			  data="<span style='color:red;'>"+full.CABINET_TYPE+"</span>";
                		  }
                		  return data; 
            		  }            		  
            	  }
              },
              { "data": "HEAD_CARRIER_NAME","width":"55px"}, 
              { "data": "YUNFEI","width":"45px",
                  "render": function ( data, type, full, meta ) {
                    if(data)
                      return eeda.numFormat(parseFloat(data).toFixed(2),3)
                    else
                      return '';
                    }
              },
              { "data": null,"width":"65px",
                  "render": function ( data, type, full, meta ) {
                	  if(data){
                		  data = '';
                	  }
                	  
                	  var cost = full.COST;
                	  var charge = full.CHARGE;
                	  var costShow="";
                	  var chargeShow="";
                	  if(cost){
                		  var costArray = cost.split(',');
                		  costShow='<h5><strong>应付费用</strong></h5>';
                		  for (var i = 0; i < costArray.length; i++) {
                			  costShow += '<li>'+costArray[i]+'</li>';
						  }
                	  }
                	  if(charge){
                		  chargeShow='<h5><strong>应收费用</strong></h5>';
                		  var chargeArray = charge.split(',');
                		  for (var i = 0; i < chargeArray.length; i++) {
                			  chargeShow += '<li>'+chargeArray[i]+'</li>';
  							
						  }
                	  }
                	  if(cost){
                		  data += '<span class="other" width="50" '
                          +' data-content="<div'
                          +' height=&quot;140&quot; >'+costShow+'</div>" ><span class="badge" style="">￥付</span></span>';
                	  }
                	  if(charge){
                		  data += ' <span class="other" width="50" '
                              +' data-content="<div'
                              +' height=&quot;140&quot; >'+chargeShow+'</div>" ><span class="badge" style="">￥收</span></span>';
                	  }
                	  return data;
                  }
              },
              { "data": "CREATOR_NAME","width":"40px"},
              { "data": "CREATE_STAMP","width":"60px"}, 
              { "data": "STATUS","width":"30px"}
              
          ]
      });
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
          var order_no = $.trim($("#order_no").val()); 
          var sp_id = $("#sp_id").val(); 
          var start_date = $("#charge_time_begin_time").val();
          var end_date = $("#charge_time_end_time").val();
          var cabinet_date_begin_time = $("#cabinet_date_begin_time").val();
          var cabinet_date_end_time = $("#cabinet_date_end_time").val();
          var status = $('#status').val();
          var car_id = $("#car_id").val().trim();
          var customer_name = $("#customer_name_input").val().trim();
          var cabinet_type=$("#cabinet_type").val().trim();
          var container_no= $.trim($("#container_no").val());
          //增加出口日期查询
          var url = "/transJobOrder/list?order_no="+order_no
          	   +"&sp_id="+sp_id
          	   +"&status="+status
          	   +"&car_id="+car_id
               +"&customer_name_like="+customer_name
               +"&charge_time_begin_time="+start_date
               +"&charge_time_end_time="+end_date
          	   +"&cabinet_date_begin_time="+cabinet_date_begin_time
          	   +"&cabinet_date_end_time="+cabinet_date_end_time
          	   +"&cabinet_type="+cabinet_type
          	   +"&container_no="+container_no;
          dataTable.ajax.url(url).load();
      };
      
      //全选
      $('#allCheckBox').click(function(){
    	  $("#eeda-table [name=checkBox]").prop("checked",this.checked);
    	  btnStatus();
      });
      //单选
      $('#eeda-table').on('click','[name=checkBox]',function(){
    	  btnStatus();
      });
      //button禁用启用判断
      var btnStatus = function(){
    	  var hava_check = 0;
    	  $('#eeda-table input[name="checkBox"]').each(function(){	
    		  var checkbox = $(this).prop('checked');
    		  if(checkbox){
    			  hava_check = 1;
    		  }	
    	  });
    	  if(hava_check>0){
    		  $("#lockBtn").prop("disabled",false);
    		  $("#unLockBtn").prop("disabled",false);
    	  }else{
    		  $("#lockBtn").prop("disabled",true);
    		  $("#unLockBtn").prop("disabled",true);
    	  }
      }
      
      //锁单功能
      $("#lockBtn").click(function(){
    	  var idArray = [];
    	  var action = 'lock';
    	  $('#eeda-table input[name="checkBox"]').each(function(){
    		  var checkbox = $(this).prop('checked');
    		  if(checkbox){
    			  idArray.push($(this).parent().parent().attr("id"));
    		  }
    	  });
    	  $.post("transJobOrder/lockRelease",{id:idArray.toString(),action:action},function(data){
    		  if(data){
    			  $.scojs_message('锁单成功', $.scojs_message.TYPE_OK);
    			  refreshDetail();
    		  }else{
    			  $.scojs_message('锁单失败', $.scojs_message.TYPE_ERROR);
    		  }
    	  });
      });
      //解锁功能
      $("#unLockBtn").click(function(){
    	  var idArray = [];
    	  var action = 'unLock';
    	  $('#eeda-table input[name="checkBox"]').each(function(){
    		  var checkbox = $(this).prop('checked');
    		  if(checkbox){
    			  idArray.push($(this).parent().parent().attr("id"));
    		  }
    	  });
    	  $.post("transJobOrder/lockRelease",{id:idArray.toString(),action:action},function(data){
    		  if(data){
    			  $.scojs_message('解锁成功', $.scojs_message.TYPE_OK);
    			  refreshDetail();
    		  }else{
    			  $.scojs_message('解锁失败', $.scojs_message.TYPE_ERROR);
    		  }
    	  });
      });
      
    //刷新明细表
      var refreshDetail = function(){
    	  var url = "/transJobOrder/list?status=新建";
    	  dataTable.ajax.url(url).load();
      }
  });
});