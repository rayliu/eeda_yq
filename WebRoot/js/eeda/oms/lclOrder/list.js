define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'dtColReorder', 'validate_cn', 'sco'], function ($, metisMenu) {
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
  	  
  	$('.complex_search').click(function(event) {
        if($('.search_single').is(':visible')){
          $('.search_single').hide();
        }else{
          $('.search_single').show();
        }
    });
  	
  	
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
          sort:true,
          colReorder: true,
          paging: true,
          serverSide: true, //不打开会出现排序不对
          ajax: "/lclOrder/list",
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
				    	if(full.STATUS!='已完成'){
				    		return '<input type = "checkBox" name = "checkBox">';
				    	}else{
				    		return '<input type = "checkBox" disabled name = "checkBox">';
				    	}
				      
				    }
				},
              { "width": "30px",
                  "render": function ( data, type, full, meta ) {
                    return '<button type="button" class="btn table_btn delete btn-xs" >'+
                          '<i class="fa fa-trash-o"></i> 删除</button>';
                  }
              },
              { "data": "ORDER_NO", 
                  "render": function ( data, type, full, meta ) {
                	  var other = '';
                    var new_count='';
                    if(full.NEW_COUNT>0){
                      new_count='style="background-color:white;color:red;"';
                    }
                	  if(full.OTHER_FLAG=='other'){
                		  other = ' <span class="" '+new_count
                          +'><img src="/images/order_from_outside.png" style="height:15px;" title="Outside Order"></span>';
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
              { "data": "SONO_MBL"},  
              { "data": "CONTAINER_NO"},
              {"data":"POD_NAME",
            	  "render":function(data,type,full,meta){
            		  if(data !=null)
            		  var index=data.indexOf(",")
            		  if(index>0){
            			  data=data.substr(0,index);
            		  }
            		  return data;
            	  	}
            	  },
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
              { "data": "UPDATOR_NAME"}, 
              { "data": "CREATE_STAMP"}, 
              { "data": "STATUS"},
              {"data": "NEW_COUNT","visible":false}
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
      
      var checkNum = 0;
      $('#eeda-table').on('click','[name=checkBox]',function(){
    	  if(this.checked){
    		  checkNum++;
    	  }else{
    		  checkNum--;
    	  }
    	  
    	  if(checkNum>0){
    		  $('.create_btn').attr('disabled',false);
    	  }else{
    		  $('.create_btn').attr('disabled',true);
    	  }
      });
      
      
      $('.create_btn').on('click',function(){
    	  var ids = [];
    	  $('#eeda-table [name=checkBox]:checked').each(function(){
    		  var order_id = $(this).parent().parent().attr("id");
    		  ids.push(order_id);
    	  });
    	  window.location.href= "lclOrder/create?ids="+ids;
      });
      
    
      //简单查询
      $('#selected_field').change(function(event) {
	      var selectField = $('#selected_field').val();
	      if(selectField == 'sono_like'||selectField == 'container_no'||selectField == 'old_order_no'||selectField == 'order_no'||selectField == 'customer_code_like'){
	    	  $('#public_text').val("");
	    	  $('#customer_name_show').hide();
	    	  $('#single_export_type_list').hide();
	    	  $('#public_time_show').hide();
	    	  $('#public_text').show();
	      }else if(selectField == 'type'){
	    	  $('#customer_name_show').hide();
	    	  $('#public_text').hide();
	    	  $('#public_time_show').hide();
	    	  $('#single_export_type_list').show();
	      }else if(selectField == 'customer_name'){
	    	  $('#single_export_type_list').hide();
	    	  $('#public_text').hide();
	    	  $('#public_time_show').hide();
	    	  $('#customer_name_show').show();
         }else if(selectField == 'sent_out_time'||selectField == 'create_stamp'){
        	  $('#public_time_begin_time').val("");
        	  $('#public_time_end_time').val("");
	    	  $('#single_export_type_list').hide();
	    	  $('#public_text').hide();
	    	  $('#customer_name_show').hide();
	    	  $('#create_stamp_show').hide();
	    	  $('#public_time_show').show();
         }
     });
	
	$('#singleSearchBtn').click(function(){
	     var selectField = $('#selected_field').val();
	     var selectFieldValue = '';
	     if(selectField == 'sono_like'||selectField == 'container_no'||selectField == 'old_order_no'||selectField == 'order_no'||selectField == 'customer_code_like'){
	    	  selectFieldValue = $("#public_text").val();
	      }else if(selectField == 'type'){
	    	  selectFieldValue = $("#single_export_type_list").val();
	    	  selectField="type_equals";
	      }else if(selectField == 'customer_name'){
	    	  selectFieldValue = $("#single_customer_name_input").val();
          }else if(selectField == 'create_stamp'){
        	  var start_date = $("#public_time_begin_time").val();
              var end_date = $("#public_time_end_time").val();
          }else if(selectField == 'sent_out_time'){
              var sent_out_time_begin_time = $("#public_time_begin_time").val();
              var sent_out_time_end_time = $("#public_time_end_time").val();
          }
	     
     
	     var url = "/jobOrder/list?"+selectField+"="+selectFieldValue
	     			+"&create_stamp_begin_time="+start_date
	     			+"&create_stamp_end_time="+end_date
	     			+"&type_="+type
	     			+"&custom_status_="+'待报关'
	     			+"&order_export_date_begin_time="+sent_out_time_begin_time
	     			+"&order_export_date_end_time="+sent_out_time_end_time;
	
	     dataTable.ajax.url(url).load();
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
          var export_type = $('#type').val();
          var customer_code = $("#customer_code").val().trim();
          var customer_name = $("#customer_name_input").val().trim();
          var sono = $("#sono").val().trim();
          var container_no = $("#container_no").val().trim();
          //var order_type = $("#order_type").val().trim();
          var custom_status = $("#custom_status").val();
          var transport_type = type;
          //增加出口日期查询
          var url = "/jobOrder/list?order_no="+order_no
               +"&old_order_no="+old_order_no
          	   +"&type_equals="+export_type
               +"&sono_like="+sono
               +"&container_no="+container_no
          	   +"&transport_type_like="+transport_type
          	   +"&customer_code_like="+customer_code
               +"&customer_name_like="+customer_name
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date
               +"&type_="+$('#order_type').val()
    		   +"&custom_status_="+custom_status
          	   +"&order_export_date_begin_time="+sent_out_time_begin_time
          	   +"&order_export_date_end_time="+sent_out_time_end_time;

          dataTable.ajax.url(url).load();
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
      });
      
      
      
      var dataTable = eeda.dt({
          id: 'lcl_table',
          sort:true,
          colReorder: true,
          paging: true,
          serverSide: true, //不打开会出现排序不对
          ajax: "/lclOrder/orderList",
          columns: [
              { "data": "ORDER_NO", 
                  "render": function ( data, type, full, meta ) {
                      return "<a href='/lclOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
                  }
              },
              { "data": "TRANSPORT_TYPE"}, 
              { "data": "TRANS_CLAUSE"}, 
              { "data": "TRADE_TYPE"}, 
              { "data": "MBLSHIPPER_NAME"}, 
              { "data": "MBLCONSIGNEE_NAME"}, 
              { "data": "MBLNOTIFY_PARTY_NAME"}, 
              { "data": "CREATOR_NAME"}, 
              { "data": "CREATE_TIME"}, 
              { "data": "STATUS"}
          ]
      });

  });
});