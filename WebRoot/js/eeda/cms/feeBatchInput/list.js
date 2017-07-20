define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','dtColReorder','validate_cn', 'sco'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '报关申请单列表 | '+document.title;

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
            ajax: "/feeBatchInput/list?confirmFee=unConfirmFee",
            "drawCallback": function( settings ) {
                $('.other').popover({
                    html: true,
                    container: 'body',
                    placement: 'right',
                    trigger: 'hover'
                });
  		  },
            columns:[
					{ "width": "10px",
					    "render": function ( data, type, full, meta ) {
					    	if(full.AUDIT_FLAG != 'Y')
					    		return '<input type="checkbox" class="checkBox" name="checkBox">';
					    	else 
					    		return '<input type="checkbox" disabled name="checkBox">';
					    }
					},
                  {"data": "ORDER_NO", 
                	  "render": function ( data, type, full, meta ) {
                		  var other = '';
                    	  if(full.OTHER_FLAG=='other'){
                    		  other = ' <span class="badge">外</span>';
                    	  }
                		  return "<a href='/feeBatchInput/edit?id="+full.ID+"'target='_blank'>"+data+other+"</a>";
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
	              }
            ]
        });
        
        //全选
        $('#AllCheck').click(function(){
      	  var ischeck = this.checked;
      	  	  $(".checkBox").each(function () {  
                this.checked = ischeck;  
            });  
  	      if(ischeck==true){
  	      	  $('#confirmBtn').attr('disabled',false);
  	      }else{
  	      	  $('#confirmBtn').attr('disabled',true);
  	      }
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
    	  var orderStatus = $("#orderTabs li.active a").attr("name").trim();
          searchData(orderStatus); 
      })

     var searchData=function(paraStr){
          var order_no = $.trim($("#order_no").val());
          var customer_name = $('#customer_name_input').val().trim();
          var status = $('#status').val();
          var custom_state = $('#custom_state').val();
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          var custom_start_date = $("#date_custom_begin_time").val();
          var custom_end_date = $("#date_custom_end_time").val();
          var booking_no = $("#booking_no").val().trim();
          var type = $("#type").val();
          var confirmFee = "";
          if(paraStr=="未完成费用确认"){
        	  confirmFee ="unConfirmFee";
          }else if(paraStr=="已完成费用确认"){
        	  confirmFee="confirmFee"
          }else{
        	  if(paraStr=="待审核"&&paraStr!=undefined){
            	  status = paraStr;
              }else{
            	  
    	    	  if(paraStr!='全部'&&paraStr!=undefined){
    	    		  custom_state=paraStr;
    	    	  }
              }
          }
          
          
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/feeBatchInput/list?order_no="+order_no
               +"&status_equals="+status
               +"&booking_no_equals="+booking_no
               +"&custom_state_equals="+custom_state
               +"&receive_company_name="+customer_name
               +"&type_equals="+type
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date
               +"&date_custom_begin_time="+custom_start_date
               +"&date_custom_end_time="+custom_end_date
               +"&date_custom_end_time="+custom_end_date
               +"&confirmFee="+confirmFee;
          dataTable.ajax.url(url).load();
      };
      
      $('#orderTabs a').click(function(){
    	  var custom_state = $(this).attr("name");    	  
        	  searchData(custom_state);
      });

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
          $.post('/feeBatchInput/deleteOrder', {id:id,delete_reason:deleteReason}, function(data){
            $('#deleteReasonDetail .return').click();
            tr.hide();
            $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
          },'json').fail(function() {
              $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
          });
      });

      
      
      
     //应收应付常用费用
     $('#collapseChargeInfo,#collapseCostInfo').on('show.bs.collapse', function () {
     var thisType = $(this).attr('id');
     var type = 'Charge';
     if('collapseChargeInfo'!=thisType){
         type='Cost';
     }
     var div = $('#'+type+'Div').empty();
     $('#collapse'+type+'Icon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
     var order_type = $('#type').val();
     var customer_id = $('#customer_name').val();
     if( customer_id == '' || order_type == ''){
         $.scojs_message('客户和类型必填', $.scojs_message.TYPE_ERROR);
         return;
     }else{
         
         $.post('/customPlanOrder/getArapTemplate', {order_type:order_type,customer_id:customer_id,arap_type:type}, function(data){
             if(data){
                 for(var i = 0;i<data.length;i++){
                     var json_obj = JSON.parse(data[i].JSON_VALUE);
                     var li = '';
                     var li_val = '';
                     for(var j = 0;j<json_obj.length;j++){
                         li +='<li '
                             +' sp_name="'+json_obj[j].sp_name+'" '
                             +'charge_eng_id="'+json_obj[j].CHARGE_ENG_ID+'" '
                             +'charge_id="'+json_obj[j].CHARGE_ID+'" '
                             +'currency_id="'+json_obj[j].CURRENCY_ID+'" '
                             +'sp_id="'+json_obj[j].SP_ID+'" '
                             +'unit_id="'+json_obj[j].UNIT_ID+'" '
                             +'amount="'+json_obj[j].amount+'" '
                             +'charge_name="'+json_obj[j].charge_name+'" '
                             +'charge_name_eng="'+json_obj[j].charge_eng_name+'" '
                             +'currency_name="'+json_obj[j].currency_name+'" '
                             +'currency_total_amount="'+json_obj[j].currency_total_amount+'" '
                             +'exchange_currency_id="'+json_obj[j].exchange_currency_id+'" '
                             +'exchange_currency_name="'+json_obj[j].exchange_currency_name+'" '
                             +'exchange_currency_rate="'+json_obj[j].exchange_currency_rate+'" '
                             +'exchange_rate="'+json_obj[j].exchange_rate+'" '
                             +'exchange_total_amount="'+json_obj[j].exchange_total_amount+'" '
                             +'order_type="'+json_obj[j].order_type+'" '
                             +'price="'+json_obj[j].price+'" '
                             +'remark="'+json_obj[j].remark+'" '
                             +'total_amount="'+json_obj[j].total_amount+'" '
                             +'type="'+json_obj[j].type+'" '
                             +'unit_name="'+json_obj[j].unit_name+'" '
                             +'></li>';
                         li_val += '<span></span> '+json_obj[j].sp_name+' , '+json_obj[j].charge_name+' , '+json_obj[j].total_amount+'<br/>';
                     }
                     
                     div.append('<ul class="used'+type+'Info" id="'+data[i].ID+'">'
                             +li
                             +'<div class="radio">'
                             +'  <a class="delete'+type+'Template" style="margin-right: 10px;padding-top: 5px;float: left;">删除</a>'
                             +'  <div class="select'+type+'Template" style="margin-left: 60px;padding-top: 0px;">'
                             +'      <input type="radio" value="1" name="used'+type+'Info">'
                             +       li_val
                             +'  </div>'
                             +'</div><hr/>'
                             +'</ul>');
	                 }
	             }else{
	            	 div.append('<ul><li>无数据</li></ul>');
	             }
	         });
	     }
	 });
	
	  $('#collapseChargeInfo,#collapseCostInfo').on('hide.bs.collapse', function () {
	     var thisType = $(this).attr('id');
	     var type = 'Charge';
	     if('collapseChargeInfo'!=thisType){
	         type='Cost';
	     }
	     $('#collapse'+type+'Icon').removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
	 });
	  
	  
	  $('#ChargeDiv,#CostDiv').on('click', '.deleteChargeTemplate,.deleteCostTemplate', function(){
	        $(this).attr('disabled', true);
	        var ul = $(this).parent().parent();
	        var id = ul.attr('id');
	        $.post('/customPlanOrder/deleteArapTemplate', {id:id}, function(data){
	            if(data){
	                $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
	                $(this).attr('disabled', false);
	                ul.css("display","none");
	            }
	        },'json').fail(function() {
	            $(this).attr('disabled', false);
	              $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
	        });
	  });
	  
	    
      $('#importBtn').on('click',function(){
    	  var self = this;
    	  var ids = [];
    	  $('[name=checkBox]:checked').each(function(){
    		  var id = $(this).parent().parent().attr('id');
    		  ids.push(id); 
    	  });
    	
    	  var charge_id = $($('#ChargeDiv [type=radio]:checked').parent().parent().parent()).attr("id");
    	  var cost_id = $($('#CostDiv [type=radio]:checked').parent().parent().parent()).attr("id");
    	  
    	  if(ids.length && (charge_id>0 || cost_id>0)){
    		  //self.disabled = true;
    		  $.post('/feeBatchInput/importArapItem',{ids:ids.toString(),charge_id:charge_id,cost_id:cost_id},function(data){
    			  if(data){
    				  $.scojs_message('操作成功', $.scojs_message.TYPE_OK);
    				  refleshTable();
    			  }else{
    				  $.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
    			  }
    			  self.disabled = false;
    		  });
    	  }else{
    		  $.scojs_message('请勾选你要带的费用明细和单据', $.scojs_message.TYPE_ERROR);
    	  }
      });
      
      
      refleshTable = function(){
      	var url = "/feeBatchInput/list?confirmFee=unConfirmFee";
      	dataTable.ajax.url(url).load();
      }
      
      
    });
});