define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {

	document.title = '复核收款| '+document.title;
    $('#menu_finance').addClass('active').find('ul').addClass('in');
    
	var costAccept_table = eeda.dt({
	    id: 'costAccept_table',
	    autoWidth: true,
	    paging: true,
	    serverSide: true, //不打开会出现排序不对 
	    ajax: "/costRequest/OrderList",
	    columns: [
				{ 
				    "render": function(data, type, full, meta) {
				    	if(full.GREATE_FLAG=='Y'){
				    		return '<input type="checkbox" class="checkBox" disabled>';
				    	}else{
				    		return '<input type="checkbox" class="checkBox" >';
				    	}
				        
				    }
				},
				{"data":"ORDER_NO","width":"90px",
					"render": function(data, type, full, meta) {
						return "<a href='/costCheckOrder/edit?id="+full.ID+"'  target='_blank'>"+data+"</a>";
					}
				},
				{"data":"ORDER_TYPE","class":"order_type","width":"60px"},   
				{"data":"STATUS","width":"30px"},
				{"data":"SP_NAME","class":"sp_name","width":"60px"},
				{"data":"APP_MSG","width":"120px"},
				{"data":"CNY","width":"70px",
					"render":function(data,type,full,meta){
						if(data==''){
							data=0.00;
						}
						return parseFloat(data).toFixed(2);
					}
				},
				{"data":"PAID_CNY","width":"70px",
					"render": function(data, type, full, meta) {
							if(data==''){
								data=0.00;
							   }
							return parseFloat(data).toFixed(2);
					}
				},
				{	"width":"70px",
					"render": function(data, type, full, meta) {
						return parseFloat(full.CNY - full.PAID_CNY).toFixed(2);	
					}
				},
				{"data":"USD","width":"70px",
					"render": function(data, type, full, meta) {
						if(data==''){
							data=0.00;
						}
							return parseFloat(data).toFixed(2);	
					}
				},
				{"data":"PAID_USD","width":"70px",
					"render": function(data, type, full, meta) {
						if(data==''){
							data=0.00;
						}
							return parseFloat(data).toFixed(2);	
					}
				},
				{	"width":"70px",
					"render": function(data, type, full, meta) {
						return parseFloat(full.USD - full.PAID_USD).toFixed(2);	
					}
				},
				{"data":"JPY","width":"70px",
					"render": function(data, type, full, meta) {
						if(data==''){
							data=0.00;
						}
							return parseFloat(data).toFixed(2);	
					}
				},
				{"data":"PAID_JPY","width":"70px",
					"render": function(data, type, full, meta) {
						if(data==''){
							data=0.00;
						}
							return parseFloat(data).toFixed(2);	
					}
				},
				{	"width":"70px",
					"render": function(data, type, full, meta) {
						return parseFloat(full.JPY - full.PAID_JPY).toFixed(2);	
					}
				},
				{"data":"HKD","width":"70px",
					"render": function(data, type, full, meta) {
						if(data==''){
							data=0.00;
						}
							return parseFloat(data).toFixed(2);	
					}
				},  
				{"data":"PAID_HKD","width":"70px",
					"render": function(data, type, full, meta) {
						if(data==''){
							data=0.00;
						}
							return parseFloat(data).toFixed(2);	
					}
				}, 
				{	"width":"70px",
					"render": function(data, type, full, meta) {
						return parseFloat(full.HKD - full.PAID_HKD).toFixed(2);	
					}
				} 	
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
	  	$("#sp_input").val('');
	  	$("#orderNo_filter1").val('');
	  	$("#create_stamp_begin_time").val('');
	  	$("#create_stamp_end_time").val('');
  	});
	
	
	var searchData=function(){
		var sp = $("#sp").val(); 
	    var order_no = $("#orderNo_filter1").val().trim(); 
	    var status = $('#status_filter1').val();
	    var orderType = $('#orderType').val();
	    var start_date = $("#create_stamp_begin_time").val();
	    var end_date = $("#create_stamp_end_time").val();
   
        var url = "/costRequest/OrderList?sp_id="+sp
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
   
          var url = "/costRequest/applicationList?sp_id="+sp_id
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
		var sp_names = '';
		var self = this;
		$('#costAccept_table input[type="checkbox"]').each(function(){	
			var sp_name = $(this).parent().parent().find('.sp_name').text();
    		if($(this).prop('checked')){
    			if(sp_name != sp_names && sp_names != ''){
    				$.scojs_message('请选择同一个付款对象', $.scojs_message.TYPE_ERROR);
    				$(self).attr('checked',false);
    				return false;
    			}else{
    				sp_names = sp_name;
    				hava_check++;
    			}
    		}	
		})
		if(hava_check>0){
			$('#createBtn').attr('disabled',false);
		}else{
			$('#createBtn').attr('disabled',true);
			var sp_names = '';
		}
	});
	
	
    var refleshSelectTable = function(){
	    var url = "/costRequest/OrderList";
	    costAccept_table.ajax.url(url).load();
    }
	
	
    return {
    	refleshStep1Table: refleshSelectTable
    };
	  

});