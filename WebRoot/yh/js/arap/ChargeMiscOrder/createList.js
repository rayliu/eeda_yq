
$(document).ready(function() {
    $('#menu_charge').addClass('active').find('ul').addClass('in');
    
	//datatable, 动态处理
	var uncheckedChargeCheckTable=$('#uncheckedChargeMisc-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bSort": false,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/chargeMiscOrder/chargeCheckOrderList",
        "aoColumns": [   
            { "mDataProp": null,
	            "fnRender": function(obj) {
	              return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
	            }
	        },  
	        {"mDataProp":"ID", "bVisible": false},
	        {"mDataProp":"ORDER_NO", "sWidth":"100px",
	        	"fnRender": function(obj) {
	      		return "<a href='/chargeCheckOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
	      	}},
	        {"mDataProp":"STATUS","sWidth":"50px",
	            "fnRender": function(obj) {
	                if(obj.aData.STATUS=='new'){
	                    return '新建';
	                }else if(obj.aData.STATUS=='checking'){
	                    return '已发送对帐';
	                }else if(obj.aData.STATUS=='confirmed'){
	                    return '已审核';
	                }else if(obj.aData.STATUS=='completed'){
	                    return '已结算';
	                }else if(obj.aData.STATUS=='cancel'){
	                    return '取消';
	                }
	                return obj.aData.STATUS;
	            }
	        },
	        {"mDataProp":null,"sWidth":"80px"},
	        {"mDataProp":null,"sWidth":"80px"},
	        {"mDataProp":"CNAME","sWidth":"200px"},
	        {"mDataProp":null,"sWidth":"80px"},
	        {"mDataProp":null,"sWidth":"80px"},
	        {"mDataProp":null,"sWidth":"80px"},
	        {"mDataProp":null,"sWidth":"80px"},
	        {"mDataProp":null,"sWidth":"80px"},
	        {"mDataProp":null,"sWidth":"80px"},
	        {"mDataProp":null,"sWidth":"80px"},
	        {"mDataProp":null,"sWidth":"80px"},
	        {"mDataProp":null,"sWidth":"80px"},
	        {"mDataProp":"REMARK","sWidth":"200px"},
	        {"mDataProp":"CREATOR_NAME","sWidth":"80px"},        	
	        {"mDataProp":"CREATE_STAMP","sWidth":"80px"}                         
        ]      
    });	
	 
    var checkedChargeMiscTable = $('#checkedChargeMisc-table').dataTable({
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 25,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"bServerSide": true,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	"sAjaxSource": "/chargePreInvoiceOrder/chargeCheckOrderList2",
    	"aoColumns": [ 
	          {"mDataProp": null},  
	          {"mDataProp":null, "bVisible": false},
	          {"mDataProp":null},
	          {"mDataProp":null, "sWidth":"120px"},
			  {"mDataProp":null, "sWidth":"200px"},
			  {"mDataProp":null, "sWidth":"150px"},
			  {"mDataProp":null, "sWidth":"200px"},
			  {"mDataProp":null, "sWidth":"200px"},
			  {"mDataProp":null, "sWidth":"200px"},        	
			  {"mDataProp":null, "sWidth": "120px"},           
			  {"mDataProp":null, "sWidth":"150px"},        	
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"100px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"150px"},                        
			  {"mDataProp":null, "sWidth":"200px"}                      
		]          
    });
    
    var ids = [];
    // 未选中列表
	$("#uncheckedChargeMisc-table").on('click', '.checkedOrUnchecked', function(e){
		if($(this).prop("checked") == true){
			$(this).parent().parent().appendTo($("#checkedChargeMiscList"));
			ids.push($(this).val());
			$("#checkedChargeCheckOrderId").val(ids);
		}			
	});
	
	// 已选中列表
	$("#checkedChargeMisc-table").on('click', '.checkedOrUnchecked', function(e){
		if($(this).prop("checked") == false){
			$(this).parent().parent().appendTo($("#uncheckedChargeMiscList"));
			if(ids.length != 0){
				ids.splice($.inArray($(this).val(),ids),1);
				$("#checkedChargeCheckOrderId").val(ids);
			}
		}			
	});
	
	$('#saveBtn').click(function(e){
        e.preventDefault();
        $('#createForm').submit();
    });
	
	$("#checkedChargeCheckOrder").click(function(){
		$("#checked").show();
	});
    
    //获取客户的list，选中信息自动填写其他信息
    $('#companyName').on('keyup click', function(){
        var inputStr = $('#companyName').val();
        
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

        if(inputStr==''){
        	chargeCheckTable.fnFilter('', 2);
        }
        
    });    
    
    $('#companyName,#beginTime_filter,#endTime_filter,#beginTime,#endTime').on( 'keyup', function () {
    	var companyName = $('#companyName').val();
		var beginTime = $("#beginTime_filter").val();
		var endTime = $("#endTime_filter").val();
		var receiptBegin = $("#beginTime").val();
		var receiptEnd = $("#endTime").val();
		uncheckedChargeCheckTable.fnSettings().sAjaxSource = "/chargeCheckOrder/createList?companyName="+companyName+"&beginTime="+beginTime+"&endTime="+endTime+"&receiptBegin="+receiptBegin+"&receiptEnd="+receiptEnd;
		uncheckedChargeCheckTable.fnDraw();
	} );

    $('#companyList').on('click', '.fromLocationItem', function(e){        
        $('#companyName').val($(this).text());
        $("#companyList").hide();
        var companyId = $(this).attr('partyId');
        $('#customerId').val(companyId);
        //过滤回单列表
        //chargeCheckTable.fnFilter(companyId, 2);
        var inputStr = $('#companyName').val();
        if(inputStr!=null){
        	console.log(inputStr);
        	uncheckedChargeCheckTable.fnSettings().sAjaxSource = "/chargeCheckOrder/createList?companyName="+inputStr;
        	
    		uncheckedChargeCheckTable.fnDraw();
        }
    });
    // 没选中客户，焦点离开，隐藏列表
    $('#companyName').on('blur', function(){
        $('#companyList').hide();
    });

    //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
    $('#companyList').on('blur', function(){
        $('#companyList').hide();
    });

    $('#companyList').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    });

    $('#createBtn').click(function(e){
        e.preventDefault();
        //获取选取回单的ID, 放到数组中
        var chk_value =[];    
        $('input[name="order_check_box"]:checked').each(function(){    
           chk_value.push($(this).val());    
        }); 

        var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
                        '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
                        'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
                    '</div>';
        $('body').append(alerMsg);
        $('#message_trigger_err').on('click', function(e) {
            e.preventDefault();
            
        });
        //$(body).append(chk_value.length==0 ?'你还没有选择任何应付单据！':chk_value);

        $('#ids').val(chk_value);
        if(!$('#returnOrderSearchForm').valid()){
            return false;
        }
        if(chk_value.length==0 ){
            $.scojs_message('你还没有勾选任何应收回单.', $.scojs_message.TYPE_ERROR);
            return false;
        }

        $('#createForm').submit();
    });

    $('#beginTime_filter').on('keyup', function () {
        var orderNo = $("#orderNo_filter").val();
        var status = $("#status_filter").val();
        var address = $("#address_filter").val();
        var customer = $("#customer_filter").val();
        var sp = $("#sp_filter").val();
        var beginTime = $("#beginTime_filter").val();
        $("#beginTime_filter").val(beginTime);
        var endTime = $("#endTime_filter").val();
        var officeName = $("#officeName_filter").val();
        // transferOrder.fnSettings().sAjaxSource = "/transferOrder/list?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime+"&officeName="+officeName;
        // transferOrder.fnDraw();
    } );    
    
    $('#endTime_filter').on( 'keyup click', function () {
        var orderNo = $("#orderNo_filter").val();
        var status = $("#status_filter").val();
        var address = $("#address_filter").val();
        var customer = $("#customer_filter").val();
        var sp = $("#sp_filter").val();
        var beginTime = $("#beginTime_filter").val();
        var endTime = $("#endTime_filter").val();
        $("#endTime_filter").val(endTime);
        var officeName = $("#officeName_filter").val();
        // transferOrder.fnSettings().sAjaxSource = "/transferOrder/list?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime+"&officeName="+officeName;
        // transferOrder.fnDraw();
    } );
    $('#beginTime').on('keyup', function () {
        var orderNo = $("#orderNo_filter").val();
        var status = $("#status_filter").val();
        var address = $("#address_filter").val();
        var customer = $("#customer_filter").val();
        var sp = $("#sp_filter").val();
        var beginTime = $("#beginTime").val();
        $("#beginTime").val(beginTime);
        var endTime = $("#endTime").val();
        var officeName = $("#officeName_filter").val();
        // transferOrder.fnSettings().sAjaxSource = "/transferOrder/list?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime+"&officeName="+officeName;
        // transferOrder.fnDraw();
    } );    
    
    $('#endTime').on( 'keyup click', function () {
        var orderNo = $("#orderNo_filter").val();
        var status = $("#status_filter").val();
        var address = $("#address_filter").val();
        var customer = $("#customer_filter").val();
        var sp = $("#sp_filter").val();
        var beginTime = $("#beginTime").val();
        var endTime = $("#endTime").val();
        $("#endTime").val(endTime);
        var officeName = $("#officeName_filter").val();
        // transferOrder.fnSettings().sAjaxSource = "/transferOrder/list?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime+"&officeName="+officeName;
        // transferOrder.fnDraw();
    } );

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
    $('#datetimepicker3').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#beginTime').trigger('keyup');
    });


    $('#datetimepicker4').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#endTime').trigger('keyup');
    });
} );