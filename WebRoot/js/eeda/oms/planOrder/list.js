define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'dtColReorder'], function ($, metisMenu) {

    $(document).ready(function() {
    	document.title = '计划订单查询 | '+document.title;

      $("#breadcrumb_li").text('计划订单');

    	if(type != ""){
    		$('#menu_order').removeClass('active').find('ul').removeClass('in');
            $('#menu_todo_list').addClass('active').find('ul').addClass('in');
          }
    	else{
        	$('#menu_order').addClass('active').find('ul').addClass('in');
            $('#menu_todo_list').removeClass('active').find('ul').removeClass('in');
    	 }
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            colReorder: true,
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/planOrder/list?type="+type,
            columns:[
                { "width": "30px",
                    "render": function ( data, type, full, meta ) {
                      return '<button type="button" class="btn table_btn delete_btn btn-xs" disabled>'+
                        '<i class="fa fa-trash-o"></i> 删除</button>';
                    }
                },
                {"data": "ORDER_NO", 
              	  "render": function ( data, type, full, meta ) {
              		  return "<a href='/planOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
              	  }
                },
	              { "data": "TYPE",
                    "render": function ( data, type, full, meta ) {
                      var str = '';
                      if(data == '出口散货'){
                    	str = '出口散货'; 
                      }else if(data == '出口柜货'){
                        str = '出口柜货'; 
                      }else if(data == '进口柜货'){
                        str = '进口柜货';
                      }else if(data == '进口散货'){
                        str = '进口散货';
                      }else if(data == '进口散货'){
                        str = '出口空运';
                      }else if(data == '进口空运'){
                        str = '进口空运'; 
                      }else if(data == '香港头程'){
                        str = '香港头程';
                      }else if(data == '香港游'){
                        str = '香港游';
                      }else if(data == '陆运'){
                        str = '陆运';
                      }else if(data == '报关'){
                        str = '报关'; 
                      }else if(data == '快递'){
                        str = '快递';
                      }else if(data == '加贸'){
                        str = '加贸';
                      }else if(data == '贸易'){
                        str = '贸易';
                      }else if(data == '园区游'){
                        str = '园区游';
                      }
                      return str;
                    }
                }, 
	              { "data": "CUSTOMER_NAME"}, 
	              { "data": "ORDER_STATUS"}, 
	              {"data": "ITEM_STATUS" },
	              { "data": ""}, 
	              { "data": "CREATOR_NAME"}, 
	              { "data": "CREATE_STAMP"}
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
      
      if(type!=""){
    	  $('#orderTabs').css('display','none');
      }

     var searchData=function(){
          var order_no = $.trim($("#order_no").val()); 
          var status = $('#status').val();
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          var customer_code = $("#customer_code").val().trim();
          var customer_name = $("#customer_name_input").val().trim();
          
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/planOrder/list?order_no="+order_no
               +"&status="+status
               +"&customer_code_like="+customer_code
               +"&customer_name_like="+customer_name
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
      };
      
      $("#eeda-table").on('click', '.delete', function(){
    	  var tr = $(this).parent().parent();
          var id = tr.attr('id');
    	  $('#delete_id').val(id);
    	  $('#deleteReasonDetailAlert').click();
      })
      $("#deleteReasonDetail").on('click', '.deleteReason', function(){
    	  $('#deleteReason').val($(this).val());
      })
       $("#deleteReasonDetail").on('click', '.confirm', function(){
    	   if(!$("#deleteReasonDetailForm").valid()){
               return;
           }
    	   var id = $('#delete_id').val();
           var deleteReason = $('#deleteReason').val();
           var tr = $('#'+id+'');
          $.post('/planOrder/deleteOrder', {id:id,delete_reason:deleteReason}, function(data){
        	  $('#deleteReasonDetail .return').click();
        	  tr.hide();
        	  $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
          },'json').fail(function() {
              $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
          });
      });
      
    	
    });
});