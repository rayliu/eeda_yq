define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'dtColReorder'], function ($, metisMenu) {

    $(document).ready(function() {


      $('.complex_search').click(function(event) {
          if($('.search_single').is(':visible')){
            $('.search_single').hide();
          }else{
            $('.search_single').show();
          }
      });

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
            sort: true,
            colReorder: true,
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/planOrder/list?type_="+type,
            columns:[
                { "width": "30px",
                    "render": function ( data, type, full, meta ) {
                      return '<button type="button" class="btn table_btn delete_btn btn-xs delete">'+
                        '<i class="fa fa-trash-o"></i> 删除</button>';
                    }
                },
                {"data": "ORDER_NO", 
              	  "render": function ( data, type, full, meta ) {
                     var office_type = $('#office_type').val();
                     var str='';
                     if(office_type == 'forwarderCompany' && full.NEW_SUBMIT_FLAG=='Y'){
                       str='<span class="badge" style="background-color:white;color:red;margin-left:5px;">新</span>';;
                     }
              		  return "<a name='order_no' href='/planOrder/edit?id="+full.ID+"'target='_blank'>"+data+str+"</a>";
              	  }
                },
	              { "data": "JOB_ORDER_TYPE",
                    "render": function ( data, type, full, meta ) {
                      if(!data){
                    	 return ''; 
                      }
                      return data;
                    }
                }, 
	              { "data": "SP_NAME"}, 
	              { "data": "ORDER_STATUS"}, 
	              { "data": "ITEM_STATUS" },
	              { "data": ""}, 
	              { "data": "CREATOR_NAME"}, 
	              { "data": "CREATE_STAMP"},
	              { "data": "STATUS"}
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
      });

      $('#selected_field').change(function(event) {
          var selectField = $('#selected_field').val();
          if(selectField == 'sp_name'){//被委托方
        	  $('#single_order_no').hide();
        	  $('#single_order_status_list').hide();
              $('#sp_name_show').show();
              $('#create_stamp_show').hide();
              $('#single_status_list').hide();
          }else if(selectField == 'order_no'){//计划订单号
        	  $('#single_order_no').show();
              $('#single_order_status_list').hide();
              $('#sp_name_show').hide();
              $('#create_stamp_show').hide();
              $('#single_status_list').hide();
          }else if(selectField=='order_status'){//业务状态
        	  $('#single_order_no').hide();
              $('#sp_name_show').hide();
              $('#create_stamp_show').hide();
              $('#single_status_list').hide();
              $('#single_order_status_list').show();
          }else if(selectField=='status'){//锁单状态
        	  $('#single_order_no').hide();
              $('#sp_name_show').hide();
              $('#single_order_status_list').hide();
              $('#create_stamp_show').hide();
              $('#single_status_list').show();
          }else if(selectField=='create_stamp'){//创建时间
        	  $('#single_order_no').hide();
              $('#sp_name_show').hide();
              $('#single_order_status_list').hide();
              $('#single_status_list').hide();
              $('#create_stamp_show').show();
          }
      });

      $('#singleSearchBtn').click(function(){
    	  $("#orderForm")[0].reset();
          var selectField = $('#selected_field').val();
          var selectFieldValue ="";
          if(selectField == 'order_no'){//计划订单号
            selectFieldValue = $('#single_order_no').val();
            $("#order_no").val($('#single_order_no').val());
          }else if(selectField == 'order_status'){
            selectFieldValue = $('#single_order_status_list').val();
            $("#order_status").val($('#single_order_status_list').val());
          }else if(selectField == 'status'){
            selectFieldValue = $('#single_status_list').val();
            $("#status").val($('#single_status_list').val());
          }else if(selectField == 'sp_name'){//被委托方
            selectFieldValue = $('#single_sp_name_input').val();
            selectFieldValue+="&partyId="+$("#single_sp_name").val().trim();
            $("#sp_name_input").val($('#single_sp_name_input').val());
          }else if(selectField == 'create_stamp'){//创建时间
        	var start_date = $("#single_create_stamp_begin_time").val();
        	var end_date = $("#single_create_stamp_end_time").val();
        	$("#create_stamp_begin_time").val($("#single_create_stamp_begin_time").val());
      	  $("#create_stamp_end_time").val($("#single_create_stamp_end_time").val());
          }
          
          var url = "/planOrder/list?"+selectField+"="+selectFieldValue
          			+"&type_="+type
          			+"&create_stamp_begin_time="+start_date
          			+"&create_stamp_end_time="+end_date;;

          dataTable.ajax.url(url).load();
      });
      
      if(type!=""){
    	  $('#orderTabs').css('display','none');
      }

     var searchData=function(){
          var order_no = $.trim($("#order_no").val()); 
          var order_status = $('#order_status').val();
          var status = $('#status').val();
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          var sp_name = $("#sp_name_input").val().trim();
          var partyId = $("#sp_name").val().trim();
          
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/planOrder/list?order_no="+order_no
               +"&order_status="+order_status
               +"&status="+status
               +"&sp_name_like="+sp_name
               +"&partyId="+partyId
               +"&type_="+type
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
      
      $('#eeda-table').on('click','[name=order_no]',function(){
           $(this).children().remove('.badge');
      });

    	
    });
});