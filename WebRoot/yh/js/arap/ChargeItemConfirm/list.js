define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	document.title = '应收明细确认 | '+document.title;
    $('#menu_charge').addClass('active').find('ul').addClass('in');
    $("input[name='allCheck']").click(function(){
    	$("input[name='order_check_box']").each(function () {  
            this.checked = !this.checked;  
         });  

    });
    
    //datatable, 动态处理
    var chargeConfiremTable = eeda.dt({
        id: 'chargeConfirm-table',
        paging: true,
        serverSide: true, //不打开会出现排序不对
        ajax: "/chargeConfirmList/list",
        columns: [
                  
            { "width": "20px",
              "render": function(data, type, full, meta) {
                return '<input type="checkbox" name="order_check_box" order_type="'+data.ORDER_TP+'" value="'+data.ID+'">';
              }
            },  
            {"data":"ORDER_NO", "width":"120px",
            	"render": function(data, type, full, meta) {
            		return eeda.getUrlByNo(data.ID, data.ORDER_NO);
        		}},
            {"data":"SERIAL_NO", "width":"120px"},
            {"data":"CUSTOMER_ORDER_NO", "width":"120px"}, 
            {"data":"TOTAL_AMOUNT", "width":"120px", 
                "render": function(data, type, full, meta) {
                    if(data.TOTAL_AMOUNT==null){
                        return '0';
                    }
                    else{
                    	return data.TOTAL_AMOUNT;
                    }
                }
            }, 
            {"data":"CHANGE_AMOUNT", "width":"120px", 
                "render": function(data, type, full, meta) {
                    if(data.CHANGE_AMOUNT!=null&&data.ORDER_TP=='回单'){
                        return "<input type='text' style='width:60px' name='change_amount' id='change' value='"+data.CHANGE_AMOUNT+"'/>";
                    }
                    else if(data, type, full, meta.aData.CHANGE_AMOUNT==null&&data, type, full, meta.aData.ORDER_TP=='回单'){
                    	return "<input type='text' style='width:60px' name='change_amount' id='change' value='"+data.TOTAL_AMOUNT+"'/>";
                    }
                    else{
                    	return data.TOTAL_AMOUNT;
                    }
                }
            }, 
            {"data":"ADDRESS", "width":"150px"},
            {"data":"REF_NO", "width":"150px"},
            {"data":"PLANNING_TIME", "width":"150px"},
            {"data":"CNAME", "width":"100px"},
            {"data":"SP", "width":"150px"},
            {"data":null, "width":"120px",
                "render": function(data, type, full, meta) {
                    return "未收款";
            }},
            {"data":"DEPART_TIME", "width":"130px"},
            {"data":"TRANSFER_ORDER_NO", "width":"120px"},
            {"data":"DELIVERY_ORDER_NO", "width":"120px"},        	
            {"data":null, "width": "120px", 
                "render": function(data, type, full, meta) {
                    if(data.TRANSACTION_STATUS=='new'){
                        return '新建';
                    }else if(data.TRANSACTION_STATUS=='checking'){
                        return '已发送对帐';
                    }else if(data.TRANSACTION_STATUS=='confirmed'){
                        return '已审核';
                    }else if(data.TRANSACTION_STATUS=='completed'){
                        return '已结算';
                    }else if(data.TRANSACTION_STATUS=='cancel'){
                        return '取消';
                    }
                    return data.TRANSACTION_STATUS;
                }
            },            
            {"data":"RECEIPT_DATE", "width":"150px"},        	
            {"data":"ROUTE_FROM", "width":"100px"},                        
            {"data":"ROUTE_TO", "width":"100px"},                        
            /*{"data":null, "width":"150px"},                         
            {"data":null, "width":"100px"},*/                        
            {"data":"CONTRACT_AMOUNT", "width":"80px"},
            {"data":"TRANSFER_AMOUNT", "width":"150px"},
            //{"data":"PICKUP_AMOUNT", "width":"100px"},                        
            {"data":"PICKUP_AMOUNT", "width":"80px"},                        
            {"data":"SEND_AMOUNT", "width":"80px"},                        
            {"data":"INSURANCE_AMOUNT", "width":"80px"},                        
            {"data":"SUPER_MILEAGE_AMOUNT", "width":"80px"},                        
            {"data":"STEP_AMOUNT", "width":"80px"},                        
            {"data":"INSTALLATION_AMOUNT", "width":"80px"},                        
            {"data":"LOAD_AMOUNT", "width":"150px"},                        
            {"data":"WAREHOUSE_AMOUNT", "width":"80px"},                        
            {"data":"WAIT_AMOUNT", "width":"80px"},                        
            {"data":"OTHER_AMOUNT", "width":"80px"},  
            {"data":null, "width":"80px"},                        
            {"data":"REMARK", "width":"200px"}                       
        ]      
    });	
    
    $("#chargeConfirmBtn").click(function(e){
        e.preventDefault();
    	var trArr=[];
    	var orderNoArr=[];
        $("input[name='order_check_box']").each(function(){
        	if($(this).prop('checked') == true){
        		trArr.push($(this).val());
        		orderNoArr.push($(this).attr('order_type'));
        	}
        });     
        console.log(trArr);
        var returnOrderIds = trArr.join(",");
        var orderno=orderNoArr.join(",");
        $.post("/chargeConfirmList/chargeConfirmReturnOrder", {returnOrderIds:returnOrderIds,orderno:orderno}, function(data){
        	if(data.success){
        		chargeConfiremTable.fnSettings().sAjaxSource = "/chargeConfirmList/list";
        		chargeConfiremTable.fnDraw(); 
        	}
        },'json');
    });
    $("#chargeConfirm-table").on('blur', 'input:text', function(e){
		e.preventDefault();
		var order_id = $(this).parent().parent().attr("id");
		var order_ty = $(this).parent().parent().attr("order_ty");
		var name = $(this).attr("name");
		var value = $(this).val();
		if(value==0){      
			$.scojs_message('调整金额失败,金额不能为0', $.scojs_message.TYPE_ERROR);
			chargeConfiremTable.fnDraw();
			 return false; 
		 }
		 if(isNaN(value)){      
			 alert("调整金额为数字类型");
			 chargeConfiremTable.fnDraw();
			 return false;
		 }

		 else{
			 $.post('/chargeConfirmList/updateOrderFinItem', {order_ty:order_ty,order_id:order_id, name:name, value:value}, function(data){
				 if(data.success){
					 $.scojs_message('调整金额成功', $.scojs_message.TYPE_OK);
                 }
				 else{
					 $.scojs_message('调整金额失败', $.scojs_message.TYPE_ERROR);
				 }
		    	},'json');
		 }
	});
    //获取所有客户
    $('#customer_filter').on('keyup click', function(){
           var inputStr = $('#customer_filter').val();
           
           $.get("/customerContract/search", {locationName:inputStr}, function(data){
               console.log(data);
               var companyList =$("#companyList");
               companyList.empty();
               for(var i = 0; i < data.length; i++)
               {
                   companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
               }
               if(data.length>0)
                   companyList.show();
               
           },'json');
           //refreshCreateList();
       });
    
    var refreshCreateList = function(){
    	  //获取所有的条件
        var customer = $('#customer_filter').val();
		var beginTime = $("#beginTime_filter").val();
		var endTime = $("#endTime_filter").val();
		var orderNofilter = $("#orderNo_filter").val();
		var transferOrderNo = $("#transfer_Order_filter").val();
		var serial_no = $("#serial_no").val();
		var ref_no = $("#ref_no").val();
		var customerNo = $("#customerNo_filter").val();
		var start = $("#start_filter").val();
		var status = $("#shouru_filter").val();
		
	    chargeConfiremTable.fnSettings().sAjaxSource = "/chargeConfirmList/list?customer="+customer
	   												+"&beginTime="+beginTime
	   												+"&endTime="+endTime
	   												+"&transferOrderNo="+transferOrderNo
	   												+"&customerNo="+customerNo
	   												+"&orderNo="+orderNofilter
	   												+"&start="+start
	   												+"&serial_no="+serial_no
	   												+"&ref_no="+ref_no
	   												+"&status="+status;
	    saveConditions();
		chargeConfiremTable.fnDraw(); 
    };
   //选中某个客户时候
      $('#companyList').on('click', '.fromLocationItem', function(e){        
           $('#customer_filter').val($(this).text());
           $("#companyList").hide();
           var companyId = $(this).attr('partyId');
           $('#customerId').val(companyId);

          // refreshCreateList();
           
       });
      // 没选中客户，焦点离开，隐藏列表
       $('#customer_filter').on('blur', function(){
           $('#companyList').hide();
       });

       //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
       $('#companyList').on('blur', function(){
           $('#companyList').hide();
       });

       $('#companyList').on('mousedown', function(){
           return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
       });
       
       //过滤客户
       $('#beginTime_filter,#endTime_filter,#orderNo_filter,#transfer_Order_filter,#customerNo_filter,#serial_no,#ref_no,#start_filter').on( 'keyup ',function(){
    	   //refreshCreateList();
       });
       $("#shouru_filter").on('change',function(){
    	   //refreshCreateList();
       });
       
       $('#searchBtn').on('click',function(){
    	   refreshCreateList();  
       });
       
       
       $('#datetimepicker').datetimepicker({  
           format: 'yyyy-MM-dd',  
           language: 'zh-CN'
       }).on('changeDate', function(ev){
           $(".bootstrap-datetimepicker-widget").hide();
           $('#beginTime_filter').trigger('keyup');
       });


       $('#datetimepicker2').datetimepicker({  
           format: 'yyyy-MM-dd',  
           language: 'zh-CN', 
           autoclose: true,
           pickerPosition: "bottom-left"
       }).on('changeDate', function(ev){
           $(".bootstrap-datetimepicker-widget").hide();
           $('#endTime_filter').trigger('keyup');
       });
       
       
       var saveConditions=function(){
           var conditions={
           	customer : $('#customer_filter').val()//申请单号
//               orderNo : $("#orderNo").val(),//业务单号
//               status : $("#status2").val(),
//               sp : $("#sp_id_input").val(),
//               beginTime : $("#begin_date").val(),
//               endTime : $("#end_date").val(),
//               check_begin_date : $("#check_begin_date").val(),
//               check_end_date : $("#check_end_date").val(),
//               confirmBeginTime : $("#confirmBegin_date").val(),
//               confirmEndTime : $("#confirmEnd_date").val(),
//               insurance : $("#insurance").val()
           };
           if(!!window.localStorage){//查询条件处理
               localStorage.setItem("query_chargeItemConfirm", JSON.stringify(conditions));
           }
       };
       
       
       //未申请界面
       var loadConditions=function(){
           if(!!window.localStorage){//查询条件处理
               var query_json = localStorage.getItem('query_chargeItemConfirm');
               if(!query_json)
                   return;

               var conditions = JSON.parse(query_json);

               $("#customer_filter").val(conditions.customer);//单号
//               $("#status_filter1").val(conditions.status);
//               //var customer = $("#customer_filter").val();
//               $("#sp_filter1").val(conditions.sp);
//               $("#beginTime_filter2").val(conditions.beginTime);
//               $("#endTime_filter2").val(conditions.endTime);
           }
       };
       loadConditions();
       
       
  
} );