define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	document.title = '复核收款| '+document.title;
    $('#menu_finance').addClass('active').find('ul').addClass('in');
                
    var application_table = eeda.dt({
    	id: 'application_table',
    	autoWidth: false,
        paging: true,
        serverSide: true, 
    	ajax: "/chargeRequest/applicationList",
		  columns: [
		    {"data":"APPLICATION_ORDER_NO",
            	 "render": function(data, type, full, meta) {
            			return "<a href='/chargeRequest/edit?id="+full.ID+"'target='_self'>"+data+"</a>";
            	 }
            },
            {"data":"STATUS"},
            {"data":"PAYEE_COMPANY"},
            {"data":"BILL_TYPE",
            	"render": function(data,type,full,mate){
            		var strBillType = "无发票";
	        		if(data=="ordinarybill"){
	        			strBillType="增值税普通发票";
	        		}else if(data=="specialbill"){
	        			strBillType="增值税专用发票";
	        		}else if(data=="dbill"){
	        			strBillType="代开发票(垫付款)";
	        		}else if(data=="HKINVbill"){
	        			strBillType="HK INV";
	        		}
	        			
	        		return strBillType;
        		}	
            },
            {"data":"FEE_TYPE",
            	"render": function(data,type,full,mate){
        		var strFeeType = "";
        		if(data=="transFee"){
        			strFeeType="代理货运服务费";
        		}else if(data=="customFee"){
        			strFeeType="代理报关服务费";
        		}else if(data=="consultFee"){
        			strFeeType="咨询服务费";
        		}else if(data=="internationalFee"){
        			strFeeType="国际货代服务费";
        		}
        			
        		return strFeeType;
    			}	
            },
            {"data":"SERVICE_STAMP","width":"80px"},
            {"data":"MODAL_CNY",'class':'cny',
            	"render": function(data, type, full, meta) {
            		if(data)
            			return parseFloat(data).toFixed(2);
            		else
            			return '';
            	}
            },
            {"data":"MODAL_USD",'class':'usd',
            	"render": function(data, type, full, meta) {
            		if(data)
            			return parseFloat(data).toFixed(2);
            		else
            			return '';
            	}
            },
            {"data":"MODAL_JPY",'class':'jpy',
            	"render": function(data, type, full, meta) {
            		if(data)
            			return parseFloat(data).toFixed(2);
            		else
            			return '';
            	}
            },
            {"data":"MODAL_HKD",'class':'hkd',
            	"render": function(data, type, full, meta) {
            		if(data)
            			return parseFloat(data).toFixed(2);
            		else
            			return '';
            	}
            },
            {"data":"CHARGE_ORDER_NO"},
            {"data":"PAYMENT_METHOD",
                "render": function(data, type, full, meta) {
                    if(data == 'cash'){
                    	 return '现金';
                    }else if(data == 'transfers'){
                    	  return '转账';
                    }else if(data =='checkTransfers'){
                    	  return '支票转账';
                    }else{
                    	  return data;
                    }
                    	
                }
            },
            {"data":"C_NAME"},
            {"data":"CREATE_STAMP",
        		"render":function(data, type, full, meta){
        			if(data)
        				return data.substr(0,10);
        			else 
        				return '';
    			}
    		},
        	{"data":"CHECK_STAMP",
        		"render":function(data, type, full, meta){
        			if(data)
        				return data.substr(0,10);
        			else 
        				return '';
    			}
        	},
        	{"data":"PAY_TIME",
        		"render":function(data, type, full, meta){
        			if(data)
        				return data.substr(0,10);
        			else 
        				return '';
    			}
        	},
        	{"data":"ORDER_TYPE"},
            {"data":"INVOICE_NO"}
		]      
    });
    
    //点击查询后金额汇总

    var currenryTotalAmount = function(){
    	var cny_totalAmount = 0.0;
        var usd_totalAmount = 0.0;
        var hkd_totalAmount = 0.0;
        var jpy_totalAmount = 0.0;
    	$("#application_table tbody tr").each(function(){
			 	    		var currency_cny = $(this).find('.cny').text();
			 	    		var currency_usd = $(this).find('.usd').text();
			 	    		var currency_jpy = $(this).find('.jpy').text();
			 	    		var currency_hkd = $(this).find('.hkd').text();
								if(currency_cny==''){
									currency_cny=0.00;
								}
								cny_totalAmount += parseFloat(currency_cny);
								
							    if(currency_usd==''){
							    	currency_usd=0.00;
							    }
								usd_totalAmount += parseFloat(currency_usd);
								if(currency_jpy==''){
									currency_jpy=0.00;
								}
								jpy_totalAmount += parseFloat(currency_jpy);
							    if(currency_hkd==''){
							    	currency_hkd=0.00;
								}
							     hkd_totalAmount += parseFloat(currency_hkd);
    					});
    	 $('#cny_totalAmountSpan').html(cny_totalAmount.toFixed(2));
		 $('#usd_totalAmountSpan').html(usd_totalAmount.toFixed(2));
		 $('#hkd_totalAmountSpan').html(hkd_totalAmount.toFixed(2));
		 $('#jpy_totalAmountSpan').html(jpy_totalAmount.toFixed(2));
    	}
    
      
      //查询已申请单
    $("#searchBtn1").click(function(){
        refreshData();
    });

    $("#resetBtn1").click(function(){
        $('#applicationForm')[0].reset();
        saveConditions();
    });

    var saveConditions=function(){
        var conditions={
        		sp_id:$('#sp_id').val(),
        		
        		payee_company:$('#sp_id_input').val().trim(),
        	  	  
				charge_order_no : $('#orderNo').val().trim(), 
                applicationOrderNo : $('#applicationOrderNo').val(),
                status2 : $('#status2').val().trim(),
                
                service_stamp : $('#service_stamp').val(),
                
                begin_date_begin_time : $('#begin_date_begin_time').val(),
                begin_date_end_time : $('#begin_date_end_time').val(),
                
                check_begin_date_begin_time : $('#check_begin_date_begin_time').val(),
                check_begin_date_end_time : $('#check_begin_end_begin_time').val(),
                
                confirmBegin_date_begin_time : $('#confirmBegin_date_begin_time').val(),
                confirmBegin_date_end_time : $('#confirmBegin_date_end_time').val()
        };
        if(!!window.localStorage){//查询条件处理
            localStorage.setItem("query_to", JSON.stringify(conditions));
        }
    };
    var refreshData=function(){
    	var sp_id = $('#sp_id').val();
    	  var payee_company = $('#sp_id_input').val().trim();
    	  
          var charge_order_no = $('#orderNo').val().trim(); 
          var applicationOrderNo = $('#applicationOrderNo').val();
          var status2 = $('#status2').val().trim();
          
          var service_stamp = $('#service_stamp').val();
          
          var begin_date_begin_time = $('#begin_date_begin_time').val();
          var begin_date_end_time = $('#begin_date_end_time').val();
          
          var check_begin_date_begin_time = $('#check_begin_date_begin_time').val();
          var check_begin_date_end_time = $('#check_begin_end_begin_time').val();
          
          var confirmBegin_date_begin_time = $('#confirmBegin_date_begin_time').val();
          var confirmBegin_date_end_time = $('#confirmBegin_date_end_time').val();

          var url = "/chargeRequest/applicationList?sp_id="+sp_id
     	   +"&payee_company_equals="+payee_company  
            +"&charge_order_no="+charge_order_no
            +"&application_order_no="+applicationOrderNo
            +"&status="+status2
            
            +"&service_stamp_between="+service_stamp

            +"&create_stamp_begin_time="+begin_date_begin_time
            +"&create_stamp_end_time="+begin_date_end_time
            
            +"&check_stamp_begin_time="+check_begin_date_begin_time
            +"&check_stamp_end_time="+check_begin_date_end_time
            
            +"&pay_time_begin_time="+confirmBegin_date_begin_time
            +"&pay_time_end_time="+confirmBegin_date_end_time;
       application_table.ajax.url(url).load(currenryTotalAmount);
       saveConditions();
    };

    var loadConditions=function(){
        if(!!window.localStorage){//查询条件处理
            var query_to = localStorage.getItem('query_to');
            if(!query_to)
                return;

            var conditions = JSON.parse(query_to);
            $("#sp_id").val(conditions.sp_id);
            $("#sp_id_input").val(conditions.payee_company);
            $("#orderNo").val(conditions.charge_order_no);
            $("#applicationOrderNo").val(conditions.applicationOrderNo);
            $("#status2").val(conditions.status2);
            $("#service_stamp").val(conditions.service_stamp);
            $("#begin_date_begin_time").val(conditions.begin_date_begin_time);
            $("#begin_date_end_time").val(conditions.begin_date_end_time);
            $("#check_begin_date_begin_time").val(conditions.check_begin_date_begin_time);
            $("#check_begin_date_end_begin_time").val(conditions.check_begin_date_end_begin_time);
            $("#confirmBegin_date_begin_time").val(conditions.confirmBegin_date_begin_time);
            $("#confirmBegin_date_end_time").val(conditions.confirmBegin_date_end_time);
        }
    };

    loadConditions();
    
    //浏览器回退按钮,加载页面
    $(window).on('beforeunload', function(e) {
    	$.load(function(){
    		refreshData();
    	})
    });
    
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
      	$('#costAccept_table input[type="checkbox"]:checked').each(function(){
      			var itemId = $(this).parent().parent().attr('id');
//      			var order_type = $(this).parent().parent().find(".order_type").text();
      			idsArray.push(itemId);
      	});
      	$('#idsArray').val(idsArray);
      	
      	$('#billForm').submit();
	})
      
	refreshData();
});
});