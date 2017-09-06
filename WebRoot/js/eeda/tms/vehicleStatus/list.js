define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn','./import', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {

  	  if(type!=""){
  		  $('#menu_todo_list').addClass('active').find('ul').addClass('in');
  		  $('#menu_order').removeClass('active').find('ul').removeClass('in');
  	  }else{
  		$('#menu_todo_list').removeClass('active').find('ul').removeClass('in');
		  $('#menu_order').addClass('active').find('ul').addClass('in');
  	  }
  	  if(type!=""){
  		$('#orderTabs').css('display','none');
  	  }
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
          sort:true,
          paging: true,
          serverSide: true, //不打开会出现排序不对
          ajax: "/vehicleStatus/list?type="+type,
          "drawCallback": function( settings ) {
              $('.other').popover({
                  html: true,
                  container: 'body',
                  placement: 'right',
                  trigger: 'hover'
              });
		  },
          columns: [
              { "width": "60px",
                  "render": function ( data, type, full, meta ) {
                	  var str="<nobr>";
                	  if(full.MONITOR_STATUS=="N"){
                          str += "<button class='monitor btn table_btn btn_green' >"+
                                  "改为受控车辆"+
                              "</button>";
                      }else{
                          str += "<button class='monitor btn btn-danger table_btn' >"+
                                  "改为未受控车辆"+
                              "</button>";
                      }
                	  str+="</nobr>";
                      return str;
                  }
              },
              { "data": "CAR_NO",  "width": "80px"},
              { "data": "VEHICLE_STATUS","width":"60px"},
              { "data": "MONITOR_STATUS","width":"50px",
                  "render": function ( data, type, full, meta ) {
	                    if(data=="Y"){
	                    	return "<span>受控车辆<span>";
	                    }else{
	                    	return "<span style='color:red'>不受控车辆<span>";
	                    }
                  }
              },
              { "data": "DRIVER","width":"60px"},
              { "data": "PHONE","width":"60px"}
          ]
      });
      
      $('.complex_search').click(function(event) {
          if($('.search_single').is(':visible')){
            $('.search_single').hide();
          }else{
            $('.search_single').show();
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
          var url = "/dispatchSend/list?order_no="+order_no
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
    //监控按钮控制
      $("#eeda-table").on('click', '.monitor', function(e){
      	var selfId = $(this).parent().parent().parent().attr('id');
      	$.post('/vehicleStatus/changeMonitorStatus',{selfId:selfId},function(data){
      		if(data){
  	    			var url = "/vehicleStatus/list";
  	                dataTable.ajax.url(url).load();
  	                $.scojs_message('更改成功', $.scojs_message.TYPE_OK);
                  }
      		},'json').fail(function() {
                  $.scojs_message('更改失败', $.scojs_message.TYPE_ERROR);
          });
      });
      
  });
});