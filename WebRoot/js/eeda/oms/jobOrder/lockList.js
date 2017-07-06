define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'dtColReorder', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
    
  	  
  	  if(type!=""){
  		  $('#orderTabs').css('display','none');
  	  }
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
          colReorder: true,
          // "pagingType":"full_numbers",
          serverSide: true, //不打开会出现排序不对
//          ajax: "/jobOrder/list",
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
              $('[name=allCheckBox]').attr("checked",true);
				    	if(full.STATUS!='已完成'){
				    		return '<input type = "checkBox" name = "checkBox" checked>';
				    	}else{
				    		return '<input type = "checkBox" disabled name = "checkBox">';
				    	}

				    }
				},
              { "width": "30px",
                  "render": function ( data, type, full, meta ) {
                    var str=""
                    if(full.STATUS=='已完成') str="disabled";
                    return '<button type="button" class="btn table_btn delete btn-xs" '+ str+'>'+
                          '<i class="fa fa-trash-o"></i> 删除</button>';
                  }
              },
              { "data": "ORDER_NO", 
                  "render": function ( data, type, full, meta ) {
                	  var other = '';
                	  if(full.OTHER_FLAG=='other'){
                		  other = ' <span class="badge">外</span>';
                	  }
                      return "<a href='/jobOrder/edit?id="+full.ID+"'target='_blank'>"+data+other+"</a>";
                  }
              },
              { "data": "TYPE",
                  "render": function ( data, type, full, meta ) {
	                    if(!data)
	                    	data='';
	                    return data;
                  }
              }, 
              { "data": "ORDER_EXPORT_DATE", 
            	  render: function(data){
            		  if(data)
            			  return data;
            		  return '';
            	  }
              }, 
              { "data": "CUSTOMER_NAME"}, 
              { "data": "SONO"},
              { "data": "CONTAINER_NO"},
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
                      costShow='<h5><strong>应付费用</strong>  <strong>对账币制</strong></h5>';
                      for (var i = 0; i < costArray.length; i++) {
                        costShow += '<li>'+costArray[i]+'</li>';
              }
                    }
                    if(charge){
                      chargeShow='<h5><strong>应收费用</strong>  <strong>对账币制</strong></h5>';
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

                    $('.other').popover({
                        html: true,
                        container: 'body',
                        placement: 'right',
                        trigger: 'hover'
                    });
                    return data;
                  }
              },
              { "data": "CREATOR_NAME"}, 
              { "data": "CREATE_STAMP"}, 
              { "data": "STATUS"},
              
          ]
      });
          $('#eeda-table').on('click',' [type=checkBox]',function(){
              $('[name=allCheckBox]').prop("checked",$('#eeda-table tbody [type=checkBox]:unchecked').size()==0)
              //全选
              if($('#eeda-table tbody [type=checkBox]:checked').size()>0){
                  $('#lockBtn').attr('disabled',false);
              }else{
                $('#lockBtn').attr('disabled',true);
              }
          });
          $('#eeda-table [name=allCheckBox]').on('click',function(){
                  $('#eeda-table [type=checkBox]').prop('checked',this.checked);
                  if($('#eeda-table tbody [type=checkBox]').size()>0){
                        $('#lockBtn').attr('disabled',!this.checked);
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
      
     
      
      $('#lockBtn').on('click',function(){
    	  checkNum = 0;
    	  var self = this;
    	  self.disabled = true;
    	  
    	  var idArray = [];
    	  $('#eeda-table [name=checkBox]:checked').each(function(){
    		  var id  = $(this).parent().parent().attr('id');
    		  idArray.push(id);
    	  });
    	  
    	  
    	  $.post('/jobOrder/confirmCompleted',{id:idArray.toString()},function(data){
    		  if(data.result){
    			  $.scojs_message('锁单成功', $.scojs_message.TYPE_OK);
    			  searchData();
    		  }else{
    			  $.scojs_message('锁单失败', $.scojs_message.TYPE_ERROR);
    			  self.disabled = false;
    		  }
    		  //$.unblockUI();
    	  }).fail(function() {
    		  //$.unblockUI();
    		  self.disabled = false;
              $.scojs_message('后台出错', $.scojs_message.TYPE_ERROR);
          });
      });

      
      
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
          var transport_type = $("#transport_type option:selected").text();
          $('#orderTabs .active').removeClass('active');
          $('#orderTabs a').each(function(){
        	  var value = $(this).text();
        	  if(value==transport_type){
        		  $(this).parent().addClass('active');
        	  }
          })
      })

     var searchData=function(type){
          var order_no = $.trim($("#order_no").val()); 
          var old_order_no = $.trim($("#old_order_no").val()); 
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          var sent_out_time_begin_time = $("#sent_out_time_begin_time").val();
          var sent_out_time_end_time = $("#sent_out_time_end_time").val();
          var status = $('#status').val();
          var customer_code = $("#customer_code").val().trim();
          var customer_name = $("#customer_name_input").val().trim();
          var sono = $("#sono").val().trim();
          var container_no = $("#container_no").val().trim();
          var transport_type = type;
          //增加出口日期查询
          var url = "/jobOrder/list?order_no="+order_no+"&type=lock"
               +"&old_order_no="+old_order_no
          	   +"&status="+status
               +"&sono_like="+sono
               +"&container_no="+container_no
          	   +"&transport_type_like="+transport_type
          	   +"&customer_code_like="+customer_code
               +"&customer_name_like="+customer_name
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date
          	   +"&order_export_date_begin_time="+sent_out_time_begin_time
          	   +"&order_export_date_end_time="+sent_out_time_end_time;

          dataTable.ajax.url(url).load(function(){
             // if($('#eeda-table tbody [type=checkBox]:checked').size()>0){
             //      $('#lockBtn').attr('disabled',false);
             //  }else{
             //    $('#lockBtn').attr('disabled',true);
             //  }
          });
      };
      
      $('#orderTabs a').click(function(){
    	  var value = $(this).text();
    	  var transport_type = "";
    	  if(value=="报关"){
    		  transport_type = "custom";
    	  }else if(value=="陆运"){
    		  transport_type = "land";
    	  }else if(value=="空运"){
    		  transport_type = "air";
    	  }else if(value=="海运"){
    		  transport_type = "ocean";
    	  }else if(value=="保险"){
    		  transport_type = "insurance";
    	  }else if(value=="贸易"){
    		  transport_type = "trade";
    	  }else if(value=="快递"){
    		  transport_type = "express";
    	  }
    	  searchData(transport_type);

      })
      
      
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
          $.post('/jobOrder/deleteOrder', {id:id,delete_reason:deleteReason}, function(data){
        	  $('#deleteReasonDetail .return').click();
        	  tr.hide();
        	  $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
          },'json').fail(function() {
              $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
          });
      });
      
  });
});