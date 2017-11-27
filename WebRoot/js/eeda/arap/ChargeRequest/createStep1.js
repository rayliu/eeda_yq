define(['jquery', 'metisMenu', 'sb_admin', './chargeEdit_select_item',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu,sb,selectContr) {

	document.title = '复核收款| '+document.title;
    $('#menu_finance').addClass('active').find('ul').addClass('in');
    
    var billIds=[];
    var pageBoolean=false;
    var chargeAccept_table_url = "";
    var orderUrl = "";
    var new_process_flag = $("#newProcessFlag").prop("checked");
    if(new_process_flag){
    	chargeAccept_table_url = "/chargeRequest/newOrderList";
    	orderUrl = "/invoiceApply/edit";
    }else{
    	chargeAccept_table_url = "/chargeRequest/OrderList";
    	orderUrl = "/chargeCheckOrder/edit";
    }
    
	var costAccept_table = eeda.dt({
	    id: 'chargeAccept_table',
	    autoWidth: true,
	    paging: pageBoolean,
	    serverSide: true, //不打开会出现排序不对 
	    ajax: chargeAccept_table_url,
	    columns: [
				{ 
				    "render": function(data, type, full, meta) {
				    	if(full.GREATE_FLAG=='Y'){
				    		return '<input type="checkbox" class="checkBox" name="order_check_box" disabled>';
				    	}else{
				    		return '<input type="checkbox" name="order_check_box" class="checkBox" >';
				    	}
//				    	var strcheck='<input type="checkbox" class="checkBox" name="order_check_box" value="'+full.ID+'">';
//			        	for(var i=0;i<billIds.length;i++){
//	                         if(billIds[i]==full.ID){
//	                        	 if(full.GREATE_FLAG=='Y'){
//	                        	 strcheck= '<input type="checkbox"  class="checkBox" checked="checked"  name="order_check_box" value="'+full.ID+'">';
//	                        	 }else{
//	                        		 strcheck= '<input type="checkbox"  class="checkBox" checked="checked"  name="order_check_box" value="'+full.ID+'">';
//	                        	 }
//	                         }
//	                     }
//			        	return strcheck;
				        
				    }
				},
				{"data":"ORDER_NO","width":"90px",
					"render": function(data, type, full, meta) {
						return "<a href='"+orderUrl+"?id="+full.ID+"'  target='_blank'>"+data+"</a>";
					}
				},
				{"width":"70px",
					"render": function(data, type, full, meta) {
						return full.BEGIN_TIME.substring(0,10)+'到'+full.END_TIME.substring(0,10);
					}
				},  
				{"data":"STATUS","width":"30px"},
				{"data":"SP_NAME","sClass":"SP_NAME","width":"60px"},
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
						return "<span style='width:70px'>"+parseFloat(full.CNY - full.PAID_CNY).toFixed(2)+"</span>";	
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
				{    "width":"70px",
					"render": function(data, type, full, meta) {
						return "<span style='width:70px'>"+parseFloat(full.USD - full.PAID_USD).toFixed(2)+"</span>";	
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
						return "<span style='width:70px'>"+parseFloat(full.JPY - full.PAID_JPY).toFixed(2)+"</span>";	
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
						return "<span style='width:80px'>"+parseFloat(full.HKD - full.PAID_HKD).toFixed(2)+"</span>";	
					}
				},				
				{"data":"APP_MSG","width":"120px"},
				{"data":"ORDER_TYPE","class":"order_type","width":"60px"},
				{"data":"BEGIN_TIME","width":"70px","visible":false,
					"render": function(data, type, full, meta) {
						return data;
					}
				}, 
				{"data":"END_TIME","width":"70px","visible":false,
					"render": function(data, type, full, meta) {
						return data;
					}
				} 	
        ]      
    });
                      
    var application_table = eeda.dt({
    	id: 'application_table',
    	autoWidth: true,
        paging: true,
        serverSide: true, 
    	ajax: "/chargeAcceptOrder/applicationList",
		  columns: [
		    {"data":"APPLICATION_ORDER_NO",
            	 "render": function(data, type, full, meta) {
            			return "<a href='/chargeAcceptOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
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
		$("#sp").val('');
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
	    var check_time_1_begin_time = $("#check_time_1_begin_time").val();
	    var check_time_1_end_time = $("#check_time_1_end_time").val();
   
        var url = chargeAccept_table_url+"?sp_id="+sp
      	   +"&order_no="+order_no
           +"&status="+status
           +"&order_type="+orderType
           +"&check_time_1_begin="+check_time_1_begin_time
           +"&check_time_1_end="+check_time_1_end_time;

        costAccept_table.ajax.url(url).load(allCheckbtn);
        
    };
    
    var allCheckbtn = function(){
    	if($("#chargeAccept_table tr:has(td) input[type=checkbox]").length>0){
      	  $("#allCheck1").prop("checked",$("#chargeAccept_table tr:has(td) input[type=checkbox]").length == $("#chargeAccept_table tr:has(td) input[type=checkbox]:checked").length ? true : false);
        }else{
      	  $("#allCheck1").prop("checked",false);
        }
    }
    

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
   
          var url = "/chargeRequest/applicationList?sp_id="+sp_id
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
    	

	//选择是否是同一个付款对象
	$('#chargeAccept_table').on('click',"input[name='order_check_box']",function () {
			var cname = $(this).parent().siblings('.SP_NAME')[0].textContent;
			if($(this).prop('checked')==true){	
				if(cnames.length > 0 ){
					if(cnames[0]==cname){
							cnames.push(cname);
							if($(this).val() != ''){
								billIds.push($(this).val());
							}
					}else{
						$.scojs_message('请选择同一个付款对象', $.scojs_message.TYPE_ERROR);
						$(this).attr('checked',false);
						return false;
					}
				}else{
					cnames.push(cname);
					if($(this).val() != ''){
						billIds.push($(this).val());
					}
				}

			}else{
				billIds.splice($.inArray($(this).val(), billIds), 1);
				cnames.pop(cname);
		 }
			
	 });
	
	
	
	
  	//checkbox选中则button可点击
	$('#chargeAccept_table').on('click',"input[name='order_check_box']",function () {
		if(billIds.length>0){
			$('#createSave').attr('disabled',false);
		}else{
			$('#createSave').attr('disabled',true);
		}
	});
		

	
	
    var refleshSelectTable = function(){
	    var url = chargeAccept_table_url;
	    costAccept_table.ajax.url(url).load();
    }

	var sp_name='';
	var sp_id='';
	
	$('#chargeAccept_table').on('click', 'input[type="checkbox"]',function(){
		var idsArray=[];
		var rowindex=$(this).parent().parent().index();
		
		if($(this).prop('checked')){
			 sp_id=costAccept_table.row(rowindex).data().SP_ID.toString();
			 sp_name=costAccept_table.row(rowindex).data().SP_NAME.toString();
			 begin_time=costAccept_table.row(rowindex).data().BEGIN_TIME.toString();
			 end_time=costAccept_table.row(rowindex).data().END_TIME.toString();
			 
		}

      	$('#chargeAccept_table input[type="checkbox"]:checked').each(function(){
      			var itemId = $(this).parent().parent().attr('id');
      			if(itemId){
      				idsArray.push(itemId);
      			}
      	});
      	if(idsArray==''){
      		 $('#sp_id_input').val('');
      		 $('#sp_id').val('');
      		 $('#check_time_begin_time').val('');
        	 $('#check_time_end_time').val('');
//      	 $("#orderForm")[0].reset();
//      	 $("#invoiceDiv,#transfers_massage,#projectFee").hide();
  		     selectContr.refleshSelectTable(idsArray);
  		     $("#allCheck1").prop("checked",$("#chargeAccept_table tr:has(td) input[type=checkbox]").length == $("#chargeAccept_table tr:has(td) input[type=checkbox]:checked").length ? true : false);
  		    return;
      	}
      	 $('#sp_id_input').val(sp_name);
      	 $('#sp_id').val(sp_id);
      	 $('#check_time_begin_time').val(begin_time.substring(0,10));
     	 $('#check_time_end_time').val(end_time.substring(0,10));
  		 $('#ids').val(idsArray);
  		 selectContr.refleshSelectTable(idsArray);
  		 $("#allCheck1").prop("checked",$("#chargeAccept_table tr:has(td) input[type=checkbox]").length == $("#chargeAccept_table tr:has(td) input[type=checkbox]:checked").length ? true : false);

	})
	
	var checkRequest1 = function(){
		var idsArray=[];
      	$('#chargeAccept_table input[type="checkbox"]:checked').each(function(){
      			var itemId = $(this).parent().parent().attr('id');
      			if(itemId){
      				var rowindex=$(this).parent().parent().index();
      				
	      			if($(this).prop('checked')){
	   				 sp_id=costAccept_table.row(rowindex).data().SP_ID.toString();
	   				 sp_name=costAccept_table.row(rowindex).data().SP_NAME.toString();	   			
      			}
	      		idsArray.push(itemId);	
   			}
      	});
      	if(idsArray==''){
      		 $('#sp_id_input').val('');
      		 $('#sp_id').val('');
      		 $('#createSave').attr('disabled',true);
      		 
      		 billIds=[];
      		 cnames=[];
  		     selectContr.refleshSelectTable(idsArray);
  		     return;
      	}
      	 $('#sp_id_input').val(sp_name);
      	 $('#sp_id').val(sp_id);
  		 $('#ids').val(idsArray);
  		 $('#createSave').attr('disabled',false);
  		 selectContr.refleshSelectTable(idsArray);
  		 $("#allCheck1").prop("checked",$("#chargeAccept_table tr:has(td) input[type=checkbox]").length == $("#chargeAccept_table tr:has(td) input[type=checkbox]:checked").length ? true : false);

	}
	
	$('#allCheck1').on('click',function(){
		var check = $(this).prop('checked');
		if(check){
			$('#chargeAccept_table input[type=checkbox]').prop('checked',true);
			$('#chargeAccept_table input[type="checkbox"]:checked').each(function(){
				var itemId = $(this).parent().parent().attr('id');
      			if(itemId){
				var cname = $(this).parent().siblings('.SP_NAME')[0].textContent;
				if($(this).prop('checked')){
					if(cnames.length > 0 ){
	   					if(cnames[0]==cname){
	   							cnames.push(cname);
	   							if($(this).val() != ''){
	   								billIds.push($(this).val());
	   							}
	   					}else{
	   						$.scojs_message('请选择同一个付款对象', $.scojs_message.TYPE_ERROR);
	   						$(this).attr('checked',false);
	   						$('#chargeAccept_table input[type=checkbox]').prop('checked',false);
	   						$('#allCheck1').prop('checked',false);
	   						return ;
	   					}
	   				}else{
	   					cnames.push(cname);
	   					if($(this).val() != ''){
	   						billIds.push($(this).val());
	   					}
	   				}	   			
      			
				}
			  }
			});
			checkRequest1();			
		}else{
			$('#chargeAccept_table input[type=checkbox]').prop('checked',false);
			checkRequest1();	
		}
	});

       
		return {
			refleshStep1Table: refleshSelectTable
    };
	  

});