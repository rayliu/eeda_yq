define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','dtColReorder', 'sco'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '报关申请单列表 | '+document.title;
    	$('#breadcrumb_li').html('报关申请单列表');
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
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/customPlanOrder/list?confirmFee=unConfirmFee",
            columns:[
					{ "width": "30px",
					    "render": function ( data, type, full, meta ) {
					    	var to_office_id=full.TO_OFFICE_ID;
					    	if(to_office_id){
					    		return '<button type="button" class="btn table_btn delete btn-xs" disabled>'+
					            '<i class="fa fa-trash-o"></i> 删除</button>';
					    	}
					    		return '<button type="button" class="btn table_btn delete btn-xs" >'+
				            '<i class="fa fa-trash-o"></i> 删除</button>';
					    }
				   },
                  {"data": "ORDER_NO", 
                	  "render": function ( data, type, full, meta ) {
                		  return "<a href='/customPlanOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
                	  }
                  },
                  { "data": "TRACKING_NO"},
                  { "data": "CUSTOMS_BILLCODE"},
                  { "data": "STATUS", 
  	            	"render": function(data, type, full, meta){
  	            		$("#waitAuditing").text(full.WAITAUDITING);
  	            		if(data=="审核不通过"){
  	            			return "<span style='color:red'>"+data+"</span>";
  	            		}else{
  	            			return data;
  	            		}	            		
  	            	}  
  	              },
  	              { "data": "CUSTOM_STATE", 
  	            	"render": function(data, type, full, meta){
  	            		$("#release").text(full.PASS);
  	            		$("#checked").text(full.CHECKED);
  	            		$("#handing").text(full.HANDLING);
  	            		$("#abnormal").text(full.ABNORMAL);
  	            		if(data=="异常"){
  	            			return "<span style='color:red'>"+data+"</span>";
  	            		}else if(data=="异常待处理"||data=="查验"){
  	            			return "<span style='color:#f7b314'>"+data+"</span>";
  	            		}else if(data=="放行"){
  	            			return "<span style='color:green'>"+data+"</span>";
  	            		}else{
  	            			return data;
  	            		}	            		
  	            	}  
  	              },
	              { "data": "TYPE"}, 
	              { "data": "RECEIVE_COMPANY_NAME"}, 
	              { "data": "CREATOR_NAME"}, 
	              { "data": "CREATE_STAMP",
	            	  "render": function ( data, type, full, meta ) {
	            		  if(data)
	            			  return data.substring(0, 10);
	            		  else
	            			  return '';
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
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(paraStr){
          var order_no = $.trim($("#order_no").val());
          var customer_name = $('#customer_name').val().trim();
          var status = $('#status').val();
          var custom_state = $('#custom_state').val();
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          var booking_no = $("#booking_no").val().trim();
          var type = $("#type").val();
          if(paraStr=="待审核"&&paraStr!=undefined){
        	  status = paraStr;
          }else{
        	  
	    	  if(paraStr!='全部'&&paraStr!=undefined){
	    		  custom_state=paraStr;
	    	  }
          }
          
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/customPlanOrder/list?order_no="+order_no
               +"&status_equals="+status
               +"&booking_no_equals="+booking_no
               +"&custom_state_equals="+custom_state
               +"&application_company_name="+customer_name
               +"&type_equals="+type
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
      };
      
      $("#eeda-table").on('click', '.delete', function(){
    	  var tr = $(this).parent().parent();
          var id = tr.attr('id');
          $.post('/customPlanOrder/deleteOrder', {id:id}, function(data){
        	  $.scojs_message('单据删除成功', $.scojs_message.TYPE_OK);
        	  tr.hide();
          },'json').fail(function() {
              $.scojs_message('单据删除失败', $.scojs_message.TYPE_ERROR);
            });
       });
      
      
      $('#orderTabs a').click(function(){
    	  var custom_state = $(this).attr("name");
    	  
    	  if(custom_state=="未完成费用确认"){
    		  var url = "/customPlanOrder/list?confirmFee=unConfirmFee";
        	  dataTable.ajax.url(url).load();
    	  }else if(custom_state=="已完成费用确认"){
    		  var url = "/customPlanOrder/list?confirmFee=confirmFee";
        	  dataTable.ajax.url(url).load();
    	  }else{
        	  searchData(custom_state);
    	  }

      })

    });
});