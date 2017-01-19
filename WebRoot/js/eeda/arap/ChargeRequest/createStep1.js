define(['jquery', 'metisMenu', 'sb_admin', './chargeEdit_select_item',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu,sb,selectContr) {

	document.title = '复核收款| '+document.title;
    $('#menu_finance').addClass('active').find('ul').addClass('in');
    
    var billIds=[];
    
	var costAccept_table = eeda.dt({
	    id: 'chargeAccept_table',
	    autoWidth: true,
	    paging: true,
	    serverSide: true, //不打开会出现排序不对 
	    ajax: "/chargeRequest/OrderList",
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
						return "<a href='/chargeCheckOrder/edit?id="+full.ID+"'  target='_blank'>"+data+"</a>";
					}
				},
				{"data":"ORDER_TYPE","class":"order_type","width":"60px"},   
				{"data":"STATUS","width":"30px"},
				{"data":"SP_NAME","sClass":"SP_NAME","width":"60px"},
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
   
        var url = "/chargeRequest/OrderList?sp_id="+sp
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
	//						$(this).parent().parent().clone().appendTo($("#checkedEeda-table"));
							if($(this).val() != ''){
								billIds.push($(this).val());
							}
	//						$('#checkedEeda-table input[name="order_check_box"]').css("display","none");
					}else{
						$.scojs_message('请选择同一个付款对象', $.scojs_message.TYPE_ERROR);
						$(this).attr('checked',false);
						return false;
					}
				}else{
					cnames.push(cname);
//					$(this).parent().parent().clone().appendTo($("#checkedEeda-table"));
					if($(this).val() != ''){
						billIds.push($(this).val());
					}
//					$('#checkedEeda-table input[name="order_check_box"]').css("display","none");
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
	    var url = "/chargeRequest/OrderList";
	    costAccept_table.ajax.url(url).load();
    }
	
	$('#chargeAccept_table').on('click', 'input[type="checkbox"]',function(){
		var idsArray=[];
		var sp_name='';
		var sp_id='';
		var rowindex=$(this).parent().parent().index();
		if($(this).prop('checked')){
			 sp_id=costAccept_table.row(rowindex).data().SP_ID.toString();
			 sp_name=costAccept_table.row(rowindex).data().SP_NAME.toString();
		}

      	$('#chargeAccept_table input[type="checkbox"]:checked').each(function(){
      			var itemId = $(this).parent().parent().attr('id');
//      			var order_type = $(this).parent().parent().find(".order_type").text();
      			idsArray.push(itemId);
      	});
      	 $('#sp_id_input').val(sp_name);
      	 $('#sp_id').val(sp_id);
  		$('#ids').val(idsArray);
  		selectContr.refleshSelectTable(idsArray);
	})
	
	
    return {
    	refleshStep1Table: refleshSelectTable
    };
	  

});