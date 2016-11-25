define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	document.title = '复核付款| '+document.title;

  $('#costCheckOrderbasic').click(function(event) {
      application_table.draw();
  });
    
	var costAccept_table = eeda.dt({
	    id: 'costAccept_table',
	    autoWidth: true,
	    paging: true,
	    serverSide: false, //不打开会出现排序不对 
	    ajax: "/costAcceptOrder/list",
	    columns: [
			{ 
			    "render": function(data, type, full, meta) {
			        return '<input type="checkbox" class="checkBox" >';
			    }
			},
            {"data":"ORDER_NO",
            	"render": function(data, type, full, meta) {
            		return "<a href='/costCheckOrder/edit?id="+full.ID+"'  target='_blank'>"+data+"</a>";
        		}
            },
            {"data":"ORDER_TYPE"},   
            {"data":"STATUS"},
            {"data":"SP_NAME","sClass":"SP_NAME"},
            {"data":"APP_MSG"},  
            {"data":"USD"},  
            {"data":"HKD"},  
            {"data":"JPY"},  
            {"data":"CNY"},  
            {"data":"PAID_USD",
            	"render": function(data, type, full, meta) {
            		if(data)
            			return parseFloat(data).toFixed(2);	
            		else 
            			return '';
            	}
            },
            {"data":"PAID_HKD",
            	"render": function(data, type, full, meta) {
            		if(data)
            			return parseFloat(data).toFixed(2);	
            		else 
            			return '';
            	}
            },
            {"data":"PAID_JPY",
            	"render": function(data, type, full, meta) {
            		if(data)
            			return parseFloat(data).toFixed(2);	
            		else 
            			return '';
            	}
            },
            {"data":"PAID_CNY",
            	"render": function(data, type, full, meta) {
            		if(data)
            			return parseFloat(data).toFixed(2);	
            		else 
            			return '';
            	}
            },
            {
            	"render": function(data, type, full, meta) {
            		return full.USD - full.PAID_USD;	
            	}
            },
            {
            	"render": function(data, type, full, meta) {
            		return full.HKD - full.PAID_HKD;	
            	}
            },
            {
            	"render": function(data, type, full, meta) {
            		return full.JPY - full.PAID_JPY;	
            	}
            },
            {
            	"render": function(data, type, full, meta) {
            		return full.CNY - full.PAID_CNY;	
            	}
            },
        ]      
    });
                      
      var application_table = eeda.dt({
          id: 'application_table',
          autoWidth: true,
          paging: true,
          serverSide: true, 
          ajax: "/costAcceptOrder/applicationList",
          columns: [
            {"data":"APPLICATION_ORDER_NO",
            	 "render": function(data, type, full, meta) {
            			return "<a href='/costAcceptOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
            	 }
            },
            {"data":"ORDER_TYPE"},
            {"data":"STATUS"},    
            {"data":"COST_ORDER_NO"},
            {"data":"APP_USD",
            	"render": function(data, type, full, meta) {
            		if(data)
            			return parseFloat(data).toFixed(2);
            		else
            			return '';
            	}
            },
            {"data":"APP_HKD",
            	"render": function(data, type, full, meta) {
            		if(data)
            			return parseFloat(data).toFixed(2);
            		else
            			return '';
            	}
            },
            {"data":"APP_CNY",
            	"render": function(data, type, full, meta) {
            		if(data)
            			return parseFloat(data).toFixed(2);
            		else
            			return '';
            	}
            },
            {"data":"APP_JPY",
            	"render": function(data, type, full, meta) {
            		if(data)
            			return parseFloat(data).toFixed(2);
            		else
            			return '';
            	}
            },
            {"data":"PAYEE_UNIT"},  
            {"data":"PAYEE_NAME"},
            {"data":"PAYMENT_METHOD",
                "render": function(data, type, full, meta) {
                    if(data == 'cash')
                        return '现金';
                    else if(data == 'transfers')
                        return '转账';
                    else
                    	return data;
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
            {"data":"REMARK"}
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
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
   
          var url = "/costAcceptOrder/list?sp_id="+sp
          	   +"&order_no="+order_no
               +"&status="+status
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date;

          costAccept_table.ajax.url(url).load();
      };
      
      //查询已申请单据
      $('#searchBtn1').click(function(){
          searchData1(); 
      })
       
      $('#resetBtn1').click(function(e){
          $("#applicationForm")[0].reset();
      });
     var searchData1=function(){
    	  var sp_id = $('#sp_id').val();
          var cost_order_no = $("#cost_order_no").val().trim(); 
          var applicationOrderNo = $('#applicationOrderNo').val();
          var status2 = $("#status2").val();
          var begin_date_begin_time = $("#begin_date_begin_time").val();
          var begin_date_end_time = $("#begin_date_end_time").val();
          var check_begin_date_begin_time = $("#check_begin_date_begin_time").val();
          var check_begin_date_end_time = $("#check_begin_end_begin_time").val();
          var confirmBegin_date_begin_time = $("#confirmBegin_date_begin_time").val();
          var confirmBegin_date_end_time = $("#confirmBegin_date_end_time").val();
   
          var url = "/costAcceptOrder/applicationList?sp_id="+sp_id
               +"&cost_order_no="+cost_order_no
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
    	
      	//选择是否是同一个客户
		var cnames = [];
		$('#costAccept_table').on('click','input[type="checkbox"]',function () {
				var cname = $(this).parent().siblings('.SP_NAME')[0].textContent;
				
				if($(this).prop('checked')==true){	
					if(cnames.length > 0 ){
						if(cnames[0]!=cname){
							$.scojs_message('请选择同一个结算公司', $.scojs_message.TYPE_ERROR);
							$(this).attr('checked',false);
							return false;
						}else{
							cnames.push(cname);
						}
					}else{
						cnames.push(cname);	
					}
				}else{
					cnames.pop(cname);
			 }
  	 });
      
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
      $('#costAccept_table input[type="checkbox"]:checked').each(function(){
        var itemId = $(this).parent().parent().attr('id');
        itemIds.push(itemId);
      });
      $("#itemIds").val(itemIds);
      $("#createForm").submit();
    });
		
});
});