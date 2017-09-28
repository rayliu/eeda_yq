define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','dtColReorder','validate_cn', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	document.title = '报关申请单锁单 | '+document.title;
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
            ajax: "/customPlanOrder/list",
            "drawCallback": function( settings ) {
                $('.other').popover({
                    html: true,
                    container: 'body',
                    placement: 'right',
                    trigger: 'hover'
                });
  		  	},
  		  	initComplete:function(settings){
            hideColumn();
            },
            columns:[
					{ "width": "20px",
						 "render": function ( data, type, full, meta ) {
						    	/*if($("#status").val()=='新建'){
						    		$('[name=allCheckBox]').prop("checked",true);
						    		return '<input type = "checkBox" name = "checkBox" checked>';
						    	}
						    	if($("#status").val()=='已完成'){
						    		$('[name=allCheckBox]').prop("checked",true);
						    		return '<input type = "checkBox" name = "checkBox" checked>';
						    		
						    	}*/
						    	return '<input type = "checkBox" name = "checkBox" >';
						    }
				   },
                  {"data": "ORDER_NO", 
                	  "render": function ( data, type, full, meta ) {
                		  var other = '';
                    	  if(full.OTHER_FLAG=='other'){
                    		  other = ' <span class="badge">外</span>';
                    	  }
                		  return "<a href='/customPlanOrder/edit?id="+full.ID+"'target='_blank'>"+data+other+"</a>";
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
	              { "data": null,
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
                  { "data": "DATE_CUSTOM",
                	"render":function(data,type,full,meta){
                		if(data){
                			return data.substring(0,10);
                		}
                		return '';
                	}
                  }, 
	              { "data": "CREATOR_NAME"}, 
	              { "data": "CREATE_STAMP",
	            	  "render": function ( data, type, full, meta ) {
	            		  if(data)
	            			  return data.substring(0, 10);
	            		  else
	            			  return '';
                	  }
	              },
	              { "data": "LOCK_BILL_STATUS"}
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

     var searchData=function(){
          var order_no = $.trim($("#order_no").val());
          var customer_name = $('#customer_input').val();
          var status = $('#status').val();
          var custom_state = $('#custom_state').val();
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          var custom_start_date = $("#date_custom_begin_time").val();
          var custom_end_date = $("#date_custom_end_time").val();
          var booking_no = $("#booking_no").val().trim();
          var type = $("#type").val();
          var lock_bill_status = $("#lock_bill_status").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/customPlanOrder/list?order_no="+order_no
               +"&status_equals="+status
               +"&lock_bill_status_equals="+lock_bill_status
               +"&booking_no_equals="+booking_no
               +"&custom_state_equals="+custom_state
               +"&receive_company_name="+customer_name
               +"&type_equals="+type
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date
               +"&date_custom_begin_time="+custom_start_date
               +"&date_custom_end_time="+custom_end_date
               +"&date_custom_end_time="+custom_end_date
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
      
      //锁单功能
      $("#lockBtn").click(function(){
    	  var idArray = [];
    	  var action = 'lock';
    	  $('#eeda-table input[name="checkBox"]').each(function(){
    		  var checkbox = $(this).prop('checked');
    		  if(checkbox){
    			  idArray.push($(this).parent().parent().attr("id"));
    		  }
    	  })
    	  $.post("customPlanOrder/lockRelease",{id:idArray.toString(),action:action},function(data){
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
    	  })
    	  $.post("customPlanOrder/lockRelease",{id:idArray.toString(),action:action},function(data){
    		  if(data){
    			  $.scojs_message('解锁成功', $.scojs_message.TYPE_OK);
    			  refreshDetail();
    		  }else{
    			  $.scojs_message('解锁失败', $.scojs_message.TYPE_ERROR);
    		  }
    	  });
      });
      
      //button禁用启用判断
      var btnStatus = function(){
    	  var hava_check = 0;
    	  $('#eeda-table input[name="checkBox"]').each(function(){	
    		  var checkbox = $(this).prop('checked');
    		  if(checkbox){
    			  hava_check = 1;
    		  }	
    	  })
    	  if(hava_check>0){
    		  $("#lockBtn").prop("disabled",false);
    		  $("#unLockBtn").prop("disabled",false);
    	  }else{
    		  $("#lockBtn").prop("disabled",true);
    		  $("#unLockBtn").prop("disabled",true);
    	  }
      }
      
      //没有权限的不可以看报关申请单的费用
      var hideColumn = function(){}
      if(!$("#jurisdiction").val()){
		  var hideColumn = function(){         	
			  //隐藏对usd和jpy列
			  var dataTable = $('#eeda-table').dataTable();
			  dataTable.fnSetColumnVis(8, false);
		  }
      }
      
      //刷新明细表
      var refreshDetail = function(){
    	  var url = "/customPlanOrder/list";
    	  dataTable.ajax.url(url).load();
      }
	});
});