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
            {"data":"ORDER_TYPE", "class":"order_type","width":"80px"},   
            {"data":"PAYEE_NAME",  "width":"150px",'class':'payee_name'},
            {"data":"TOTAL_AMOUNT", "width":"70px"},  
            {"data":"RECEIVE_AMOUNT", "width":"70px" },
            {"width":"70px",
            	"render": function(data, type, full, meta) {
            		return full.TOTAL_AMOUNT - full.RECEIVE_AMOUNT;	
            	}
            },
            {"data":"STATUS", "width":"70px"},   
            {"data":"APP_MSG", "width":"120px"},   
            {"data":"REMARK",  "width":"150px"},
        ]      
    });
                      
    var application_table = eeda.dt({
    	id: 'application_table',
    	paging: true,
    	serverSide: false, //不打开会出现排序不对 
    	ajax: "/chargeAcceptOrder/applicationList",
		  columns: [
		    {"data":"ORDER_NO","width":"120px",
				"render": function(data, type, full, meta) {
					return "<a href='/chargeAcceptOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
				}
			},
			{"data":"ORDER_TYPE", "width":"70px"},
			{"data":"STATUS", "width":"50px"},    
			{"data":"TOTAL_AMOUNT", "width":"70px" },
			{"data":"CREATE_NAME", "width":"60px"},
			{"data":"CREATE_STAMP", "width":"60px"},
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
		var payee_names = '';
		var self = this;
		$('#costAccept_table input[type="checkbox"]').each(function(){	
			var checkbox = $(this).prop('checked');
			var payee_name = $(this).parent().parent().find('.payee_name').text();
    		if(checkbox){
    			if(payee_name != payee_names && payee_names != ''){
    				$.scojs_message('请选择同一个收款对象', $.scojs_message.TYPE_ERROR);
    				$(self).attr('checked',false);
    				return false;
    			}else{
    				payee_names = payee_name;
    				hava_check++;
    			}
    		}	
		})
		if(hava_check>0){
			$('#createBtn').attr('disabled',false);
		}else{
			$('#createBtn').attr('disabled',true);
			var payee_names = '';
		}
	});
	
	
	$('#createBtn').click(function(){
		$('#createBtn').attr('disabled',true);
      	var idsArray=[];
      	$('#costAccept_table input[type="checkbox"]').each(function(){
      		var checkbox = $(this).prop('checked');
      		if(checkbox){
      			var itemId = $(this).parent().parent().attr('id');
      			var order_type = $(this).parent().parent().find(".order_type").text();
      			idsArray.push(itemId+":"+order_type);
      		}
      	});
      	$('#idsArray').val(idsArray);
      	
      	$('#billForm').submit();
	})
      
});
});