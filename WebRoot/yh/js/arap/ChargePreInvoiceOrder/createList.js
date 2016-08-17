
$(document).ready(function() {
    $('#menu_charge').addClass('active').find('ul').addClass('in');
    $('#saveBtn').attr('disabled', true);
	//datatable, 动态处理
	var uncheckedChargeCheckTable=$('#uncheckedChargeCheck-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bSort": false,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/chargePreInvoiceOrder/chargeCheckOrderList",
        "aoColumns": [   
            { "mDataProp": null,
	            "fnRender": function(obj) {
	              return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
	            }
	        },  
	        {"mDataProp":"ID", "bVisible": false},
	        {"mDataProp":"ORDER_NO", "sWidth":"100px",
	        	"fnRender": function(obj) {
	        		if(ChargeCheck.isUpdate || ChargeCheck.isAffirm){
	        			return "<a href='/chargeCheckOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
	        		}else{
	        			return obj.aData.ORDER_NO;
	        		}
	      		
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
	        {"mDataProp":"C_STAMP","sWidth":"80px"},
	        {"mDataProp":"ONAME","sWidth":"80px"},
	        {"mDataProp":"CNAME","sWidth":"200px"},
	        {"mDataProp":"SP","sWidth":"200px"},
	        {"mDataProp":"CHARGE_AMOUNT","sWidth":"80px"},
	        {"mDataProp":"TOTAL_RECEIVE","sWidth":"80px"},
	        {"mDataProp":"TOTAL_NORECEIVE","sWidth":"80px" },
	        {"mDataProp":"TOTAL_AMOUNT","sWidth":"80px"},
	        {"mDataProp":null,"sWidth":"80px"},
	        {"mDataProp":"DEBIT_AMOUNT","sWidth":"80px"},
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
	
    var ids = [];
    // 未选中列表
	$("#uncheckedChargeCheck-table").on('click', '.checkedOrUnchecked', function(e){
		if($(this).prop("checked") == true){
			$(this).parent().parent().appendTo($("#checkedChargeCheckList"));
			ids.push($(this).val());
			$("#checkedReturnOrder").val(ids);
			$('#saveBtn').attr('disabled', false);
		}			
	});
	
	// 已选中列表
	$("#checkedChargeCheck-table").on('click', '.checkedOrUnchecked', function(e){
		if($(this).prop("checked") == false){
			$(this).parent().parent().appendTo($("#uncheckedChargeCheckList"));
			if(ids.length != 0){
				ids.splice($.inArray($(this).val(),ids),1);
				$("#checkedReturnOrder").val(ids);
				if(ids.length == 0){
					$('#saveBtn').attr('disabled', true);
				}
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
    var refreshCreate = function(){
    	var customer = $('#customer_filter').val();
		var beginTime = $("#beginTime_filter").val();
		var endTime = $("#endTime_filter").val();
		var office = $("#officeSelect").val();
		var status = $("#status_filter").val();
		var orderNo = $("#orderNo_filter").val();
		uncheckedChargeCheckTable.fnSettings().sAjaxSource = "/chargePreInvoiceOrder/chargeCheckOrderList?customer="+customer
															+"&beginTime="+beginTime
															+"&endTime="+endTime
															+"&office="+office
															+"&status="+status
															+"&orderNo="+orderNo;
		uncheckedChargeCheckTable.fnDraw();
    };
    
    
    $('#beginTime_filter,#endTime_filter,#orderNo_filter').on( 'keyup', function () {
    	refreshCreate();
	} );
    $('#officeSelect,#status_filter').on( 'change', function () {
    	refreshCreate();
	} );
    
    $.post('/transferOrder/searchPartOffice',function(data){
	   	 if(data.length > 0){
	   		 var officeSelect = $("#officeSelect");
	   		 officeSelect.empty();
	   		 officeSelect.append("<option ></option>");
	   		 for(var i=0; i<data.length; i++){
	   			 officeSelect.append("<option value='"+data[i].OFFICE_NAME+"'>"+data[i].OFFICE_NAME+"</option>");					    		
	   		 }
   		
   		 }
   	 },'json');
    //获取客户的list，选中信息自动填写其他信息
    $('#customer_filter').on('keyup click', function(){
        var inputStr = $('#customer_filter').val();
        $.get("/customerContract/search", {locationName:inputStr}, function(data){
            var companyList =$("#companyList");
            companyList.empty();
            for(var i = 0; i < data.length; i++)
            {
                companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
            }
            if(data.length>0)
                companyList.show();
            
        },'json');
        refreshCreate();
    });    
    $('#companyList').on('click', '.fromLocationItem', function(e){        
        $('#customer_filter').val($(this).text());
        $("#companyList").hide();
        var companyId = $(this).attr('partyId');
        $('#customerId').val(companyId); 
        
        refreshCreate();
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


    $('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
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
    
} );