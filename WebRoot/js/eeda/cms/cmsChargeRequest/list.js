define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'datetimepicker_CN'], function ($, metisMenu) {
$(document).ready(function() {
	document.title = '报关应收申请单| '+document.title;
	$("#breadcrumb_li").text('报关应收申请单');
                
    var application_table = eeda.dt({
    	id: 'application_table',
    	autoWidth: false,
        paging: true,
        drawCallback: function( settings ) {
      	    flash();
	    },
        serverSide: true, 
    	ajax: "/cmsChargeRequest/applicationList?status=新建",
		  columns: [
		  { "width": "10px", "orderable": false,
		    "render": function ( data, type, full, meta ) {
		        var strcheck='<input type="checkbox" class="checkBox" name="order_check_box" value="'+full.ID+'">';
		    	return strcheck;
		    }
		  },
          { "width": "100px",
                "render": function ( data, type, full, meta ) {
                    var str="<nobr>";
                    if(full.STATUS=="新建"){
                        str+= '<button type="button" class="checkBtn btn table_btn btn_green btn-xs " style="width:60px" >复核</button>&nbsp';
                            str+= '<button type="button" disabled class="confirmBtn btn table_btn btn_green btn-xs" data-toggle="modal" data-target=".bs-example-modal-sm" style="width:60px">收款确认</button> '; 
                    }else if(full.STATUS=="已复核"){
                             str+= '<button type="button" disabled class="checkBtn btn table_btn btn_green btn-xs" style="width:60px" >复核</button>&nbsp';
                             str+= '<button type="button" class="confirmBtn btn table_btn btn_green btn-xs" data-toggle="modal" data-target=".bs-example-modal-sm" style="width:60px">收款确认</button> '; 
                         }else if(full.STATUS=="已收款"){
                            str+= '<button type="button" disabled class="checkBtn btn table_btn btn_green btn-xs" style="width:60px" >复核</button>&nbsp';
                            str+= '<button type="button" disabled class="confirmBtn btn table_btn btn_green btn-xs" data-toggle="modal" data-target=".bs-example-modal-sm" style="width:60px">收款确认</button> '; 
                                }
                    str +="</nobr>";
                    return str;
                }
            },
		    {"data":"APPLICATION_ORDER_NO",
            	 "render": function(data, type, full, meta) {
            			return "<a href='/cmsChargeRequest/edit?id="+full.ID+"'target='_self'>"+data+"</a>";
            	 }
            },
            {"data":"STATUS"},
            {"data":"PAYEE_COMPANY","class":"SP_NAME"},
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
            			return eeda.numFormat(parseFloat(data).toFixed(2),3);
            		else
            			return '';
            	}
            },
            {"data":"CHARGE_ORDER_NO"},
            {"data":"PAYMENT_METHOD",'class':'payment_method',
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
        	{"data":"RECEIVE_TIME",
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
    	$("#application_table tbody tr").each(function(){
			 	    		var currency_cny = $(this).find('.cny').text();
								if(currency_cny==''){
									currency_cny=0.00;
								}
                                // currency_cny.replace(',','');
								cny_totalAmount += parseFloat(currency_cny);
								
    					});
    	 $('#cny_totalAmountSpan').html(eeda.numFormat(parseFloat(cny_totalAmount).toFixed(2),3));
    	}
    
    
    //返回标记
    var back=$('#back').val(); 
      //查询已申请单
    $("#searchBtn1").click(function(){
    	back="";
        refreshData(back);
    });

    $("#resetBtn1").click(function(){
        $('#applicationForm')[0].reset();
        saveConditions();
    });
    
    
    //保留查询条件
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

    //查询动作
    var refreshData=function(back){
    	 var sp_id = $('#sp_id').val();
    	  var payee_company = $('#sp_id_input').val().trim();
    	  
          var charge_order_no = $('#orderNo').val().trim(); 
          var applicationOrderNo = $('#applicationOrderNo').val();
          if(back=="true"){
          	  $('#status2').val("新建");
            }
          if(back=="confirmTrue"){
          	  $('#status2').val("已复核");
            }
          var status2 = $('#status2').val().trim();
          
          var service_stamp = $('#service_stamp').val();
          
          var begin_date_begin_time = $('#begin_date_begin_time').val();
          var begin_date_end_time = $('#begin_date_end_time').val();
          
          var check_begin_date_begin_time = $('#check_begin_date_begin_time').val();
          var check_begin_date_end_time = $('#check_begin_end_begin_time').val();
          
          var confirmBegin_date_begin_time = $('#confirmBegin_date_begin_time').val();
          var confirmBegin_date_end_time = $('#confirmBegin_date_end_time').val();

          var url = "/cmsChargeRequest/applicationList?sp_id="+sp_id
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
       application_table.ajax.url(url).load();
       saveConditions();
    };
    
    //查询条件处理
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
//    $(window).on('beforeunload', function(e) {
//    	$.load(function(){
//    		
//    		refreshData(this);
//    	})
//    });
    
    if(back=="true"||back=="confirmTrue"){
    	refreshData(back);
    }else{
    	$('#applicationForm')[0].reset();
    }
    
    
    //金额汇总
    var totalMoney = function(checkBox){
		var cny_totalAmount = $('#cny_totalAmountSpan').text();
		cny_totalAmount =parseFloat(cny_totalAmount);
        var currency_cny=0.00;
        
		if($(checkBox).prop('checked')==true){
			    currency_cny = $(checkBox).parent().parent().find('.cny').text();
                
				if(currency_cny==''){
					currency_cny=0.00;
				}
				cny_totalAmount += parseFloat(currency_cny);
						     
			}else{
				currency_cny = $(checkBox).parent().parent().find('.cny').text();
				if(currency_cny==''){
					currency_cny=0.00;
				}
				cny_totalAmount -= parseFloat(currency_cny);
				
			}
		
		 $('#cny_totalAmountSpan').html(eeda.numFormat(parseFloat(cny_totalAmount).toFixed(2),3))
	}
    
    
	
    //勾选进行金额汇总
	$('#application_table').on('click',"input[name='order_check_box']",function () {
		totalMoney(this);
	});
	$('#application_table').on('click',".checkBtn",function () {
		var tr = $(this).parent().parent().parent();
		var checkBox = tr.find('.checkBox');
		if($(checkBox).prop('checked')==true){
			$(checkBox).attr('checked',false);
			totalMoney(checkBox);
		}
	});

	
	$('#allCheck').click(function(){
		if(this.checked==true){
			currenryTotalAmount();
			$("#application_table .checkBox").each(function(){
				$(this).prop('checked',true);
			});
		}else{
			$("#application_table .checkBox").each(function(){
				$(this).prop('checked',false);
			});
			$('#cny_totalAmountSpan').text(0);
		}
	});
	$('#totalZero').click(function(){
			$("#application_table .checkBox").each(function(){
				$(this).prop('checked',false);
			});
			$('#cny_totalAmountSpan').text(0);
	    }
	);
	
	  $("#application_table").on('click','.checkBox,.checkBtn',function(){
		   $("#allCheck").prop("checked",$("#application_table .checkBox").length == $("#application_table .checkBox:checked").length ? true : false);
	  });
	  
	  var flash = function(){    
	 	 $("#allCheck").prop("checked",$("#application_table .checkBox").length == $("#application_table .checkBox:checked").length ? true : false);
	  };

      //复核
      $("#application_table").on('click','.checkBtn',function(){
            var td = $(this).parent().parent();
            var row = td.parent();
            var order_id=row.attr('id');
            var this_but=$(this);

            $.get("/cmsChargeRequest/checkOrder", {order_id:order_id,}, function(data){
                if(data.ID>0){
                    // var url = "/cmsChargeRequest/applicationList";
                    // application_table.ajax.url(url).load(currenryTotalAmount);
                    $(this_but).attr('disabled',true);
                    $(this_but).next().attr('disabled',false);
                    td.next().next().html(data.STATUS);
                    
                    $.scojs_message('复核成功', $.scojs_message.TYPE_OK);
                }else{
                    $.scojs_message('复核失败', $.scojs_message.TYPE_FALSE);
                }
            },'json');
        });

    //弹出下拉框 确认收款时间
      $("#application_table").on('click','.confirmBtn',function(){
            $('#chargeRe_table_msg_btn').click();
            var rowIndex= $(this).parent().parent().parent().index();
            $('#rowIndex').val(rowIndex);
            $('#confirmBtn').attr('disabled',true);
            $('#receive_time').val('');
             
        });
      //收款时间不能为空
      $('#receive_time_div').datetimepicker({
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
      }).on('changeDate', function(ev){
            $(".bootstrap-datetimepicker-widget").hide();
             if($('#receive_time').val()!=''){
                $('#confirmBtn').attr("disabled",false);
            }else{
                $('#confirmBtn').attr("disabled",true);
            }
        });


    //收款确认
     $("#confirmBtn").on('click',function(){
        var order={};
        var rowIndex =$('#rowIndex').val();
        var row = application_table.row(rowIndex ).data();
        var td1=$('tr[id$='+row.ID.toString()+'] ').children('.sorting_1');;

        order.id=row.ID.toString();
        order.receive_time=$('#receive_time').val();
        order.receive_bank_id=row.DEPOSIT_BANK;
        order.payment_method =row.PAYMENT_METHOD;
        order.payment_type="charge";
        $.post("/cmsChargeRequest/confirmOrder", {params:JSON.stringify(order)}, function(data){
                        if(data){
                            td1.next().next().next().html(data.STATUS);
                            td1.next().children().children(".confirmBtn").attr('disabled',true);
                            $('#application_table .confirmBtn');
                            
                            $.scojs_message('收款成功', $.scojs_message.TYPE_OK);

                        }else{
                            $("#application_table .confirmBtn").attr("disabled", false);
                            $.scojs_message('收款失败', $.scojs_message.TYPE_FALSE);
                        }
                    },'json');
     });

      
});
});