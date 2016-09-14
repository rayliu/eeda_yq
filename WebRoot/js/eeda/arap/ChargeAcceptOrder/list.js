define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	document.title = '复核收款| '+document.title;
    $('#menu_finance').addClass('active').find('ul').addClass('in');
    
	var costAccept_table = eeda.dt({
	    id: 'costAccept_table',
	    paging: true,
	    serverSide: false, //不打开会出现排序不对 
	    ajax: "/chargeAcceptOrder/list",
	    columns: [
			{ "width":"10px", 
			    "render": function(data, type, full, meta) {
			        return '<input type="checkbox" class="checkBox" >';
			    }
			},
            {"data":"ORDER_NO","width":"70px",
            	"render": function(data, type, full, meta) {
            		return "<a href='/chargeCheckOrder/edit?id="+full.ID+"'  target='_blank'>"+data+"</a>";
        		}
            },
            {"data":"ORDER_TYPE", "width":"70px"},   
            {"data":"TOTALCHARGEAMOUNT", "width":"70px"},  
            {"data":"RECEIVE_AMOUNT", "width":"70px" ,
            	"render": function(data, type, full, meta) {
            		if(data)
            			return parseFloat(data).toFixed(2);	
            		else 
            			return '';
            	}
            },
            {"width":"70px",
            	"render": function(data, type, full, meta) {
            		return full.TOTALCHARGEAMOUNT - full.RECEIVE_AMOUNT;	
            	}
            },
            {"data":"SP_NAME",  "width":"150px"},
            {"data":"PAYEE",  "width":"150px"},
            {"data":"INVOICE_NO",  "width":"150px"},
            {"data":"STATUS", "width":"70px"},   
            {"data":"REMARK",  "width":"150px"},
        ]      
    });
                      
      var application_table = eeda.dt({
          id: 'application_table',
          paging: true,
          serverSide: false, //不打开会出现排序不对 
          ajax: "/chargeAcceptOrder/applicationList",
          columns: [
            {"data":"APPLICATION_ORDER_NO","width":"120px",
            	 "render": function(data, type, full, meta) {
            			return "<a href='/chargePreInvoiceOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
            	 }
            },
            {"data":"ORDER_TYPE", "width":"70px"},
            {"data":"INVOICE_NO", "width":"70px"},
            {"data":"STATUS", "width":"50px"},    
            {"data":"TOTAL_PROFITRMB", "width":"70px",
            	"render": function(data, type, full, meta) {
            		return parseFloat(data).toFixed(2);	
            	}
            },
      
            {"data":"PAYEE", "width":"60px"},
            {"data":"SP_NAME", "width":"60px"},
            {"data":"REMARK", "width":"200px"}
        ]      
    });
      
      //查询待申请单
      $('#searchBtn').click(function(){
          searchData(); 
      })
      $('#resetBtn').click(function(e){
    	  $("#costAcceptForm")[0].reset();
      });
      var searchData=function(){
    	  var sp = $("#sp").val(); 
          var order_no = $("#orderNo_filter1").val().trim(); 
          var status = $('#status_filter1').val();
          var orderType = $('#orderType').val();
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
   
          var url = "/chargeAcceptOrder/list?sp_id="+sp
          	   +"&order_no="+order_no
               +"&status="+status
               +"&order_type="+orderType
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date;

          costAccept_table.ajax.url(url).load();
      };
      
      //查询已申请单
      $('#searchBtn1').click(function(){
          searchData1(); 
      })
       
      $('#resetBtn1').click(function(e){
          $("#applicationForm")[0].reset();
      });
     var searchData1=function(){
    	  var sp_id = $('#sp_id').val();
          var order_no = $("#orderNo").val().trim(); 
          var applicationOrderNo = $('#applicationOrderNo').val();
          var status2 = $("#status2").val();
          var begin_date_begin_time = $("#begin_date_begin_time").val();
          var begin_date_end_time = $("#begin_date_end_time").val();
          var check_begin_date_begin_time = $("#check_begin_date_begin_time").val();
          var check_begin_date_end_time = $("#check_begin_end_begin_time").val();
          var confirmBegin_date_begin_time = $("#confirmBegin_date_begin_time").val();
          var confirmBegin_date_end_time = $("#confirmBegin_date_end_time").val();
   
          var url = "/chargeAcceptOrder/applicationList?sp_id="+sp_id
               +"&order_no="+order_no
               +"&application_order_no="+applicationOrderNo
               +"&STATUS="+status2
               +"&create_stamp_begin_time="+begin_date_begin_time
               +"&create_stamp_end_time="+begin_date_end_time
               +"&check_stamp_begin_time="+check_begin_date_begin_time
               +"&check_stamp_end_time="+check_begin_date_end_time
               +"&pay_time_begin_time="+confirmBegin_date_begin_time
               +"&pay_time_end_time="+confirmBegin_date_end_time;

          application_table.ajax.url(url).load();
      };
    	
      	//checkbox选中则button可点击
		$('#costAccept_table').on('click','.checkBox',function(){
			var hava_check = 0;
			$('#costAccept_table input[type="checkbox"]').each(function(){	
				var checkbox = $(this).prop('checked');
	    		if(checkbox){
	    			hava_check++;
	    		}	
			})
			if(hava_check>0){
				$('#createBtn').attr('disabled',false);
			}else{
				$('#createBtn').attr('disabled',true);
			}
		});
		$('#createBtn').click(function(){
			$('#createBtn').attr('disabled',true);
	      	var itemIds=[];
	      	$('#costAccept_table input[type="checkbox"]').each(function(){
	      		var checkbox = $(this).prop('checked');
	      		if(checkbox){
	      			var itemId = $(this).parent().parent().attr('id');
	      			itemIds.push(itemId);
	      		}
	      	});
	      	location.href ="/chargePreInvoiceOrder/create?sids="+itemIds;
		})
      
});
});