
$(document).ready(function() {
    $('#menu_charge').addClass('active').find('ul').addClass('in');
    
	//datatable, 动态处理
    var chargeCheckTable = $('#chargeCheck-table').dataTable({
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 25,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/chargeCheckOrder/createList",
        "aoColumns": [
            { "mDataProp": null,
                 "fnRender": function(obj) {
                    return '<input type="checkbox" name="order_check_box" value="'+obj.aData.ID+'">';
                 }
            },
            { "mDataProp": "ORDER_NO"},
            { "mDataProp": "COMPANY_ID", "bVisible": false},
            { "mDataProp": "COMPANY_NAME"},
            { "mDataProp": "ORDER_TYPE", "bVisible": false},
            { "mDataProp": "TRANSFER_ORDER_NO"},
            { "mDataProp": "DELIVERY_ORDER_NO"},
            { "mDataProp": "CREATOR" },
            { "mDataProp": "CREATE_DATE" },
            { "mDataProp": "TRANSACTION_STATUS",
                "fnRender": function(obj) {
                    if(obj.aData.TRANSACTION_STATUS=='new')
                        return '新建';
                    if(obj.aData.TRANSACTION_STATUS=='confirmed')
                        return '已确认';
                    return obj.aData.TRANSACTION_STATUS;
                 }
            },
            { "mDataProp": "REMARK" }                            
        ]      
    });	
    
    //获取客户的list，选中信息自动填写其他信息
    $('#companyName').on('keyup', function(){
        var inputStr = $('#companyName').val();
        
        $.get("/customerContract/search", {locationName:inputStr}, function(data){
            console.log(data);
            var companyList =$("#companyList");
            companyList.empty();
            for(var i = 0; i < data.length; i++)
            {
                companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].COMPANY_NAME+"</a></li>");
            }
            if(data.length>0)
                companyList.show();
        },'json');

        if(inputStr==''){
        	chargeCheckTable.fnFilter('', 2);
        }
    });

    $('#companyList').on('click', '.fromLocationItem', function(e){        
        $('#companyName').val($(this).text());
        $("#companyList").hide();
        var companyId = $(this).attr('partyId');
        $('#customerId').val(companyId);
        //过滤回单列表
        //chargeCheckTable.fnFilter(companyId, 2);
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

        var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable">'+
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

    $('input.beginTime_filter').on( 'change input', function () {
        var orderNo = $("#orderNo_filter").val();
        var status = $("#status_filter").val();
        var address = $("#address_filter").val();
        var customer = $("#customer_filter").val();
        var sp = $("#sp_filter").val();
        var beginTime = $("#beginTime_filter").val();
        var endTime = $("#endTime_filter").val();
        var officeName = $("#officeName_filter").val();
        // transferOrder.fnSettings().sAjaxSource = "/transferOrder/list?orderNo="+orderNo+"&status="+status+"&address="+address+"&customer="+customer+"&sp="+sp+"&beginTime="+beginTime+"&endTime="+endTime+"&officeName="+officeName;
        // transferOrder.fnDraw();
    } );
    
    $('#beginTime_filter').on('keyup', function () {
        var orderNo = $("#orderNo_filter").val();
        var status = $("#status_filter").val();
        var address = $("#address_filter").val();
        var customer = $("#customer_filter").val();
        var sp = $("#sp_filter").val();
        var beginTime = $("#beginTime_filter").val();
        $("#beginTime").val(beginTime);
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

    //from表单验证
    var validate = $('#returnOrderSearchForm').validate({
        rules: {
            companyName: {
            required: true
          }
        },
        messages : {                 
            companyName : {required:  "请选择一个客户"}
        }
    });
} );