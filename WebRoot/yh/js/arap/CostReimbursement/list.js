$(document).ready(function() {
	 document.title = '报销单查询 | '+document.title;
	 $('#menu_finance').addClass('active').find('ul').addClass('in');
    
    //datatable, 动态处理
    var costExpenseAccountTbody = $('#costExpenseAccountTbody').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/costReimbursement/reimbursementList",
        "aoColumns": [ 
			{"mDataProp":"ORDER_NO","sWidth":"120px",
				"fnRender": function(obj) {
					return "<a href='/costReimbursement/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
				}
			},
            {"mDataProp":"STATUS", "sWidth":"200px"},
            {"mDataProp":"ACCOUNT_NAME", "sWidth":"200px"},
            {"mDataProp":"ACCOUNT_NO", "sWidth":"200px"},
            {"mDataProp":"ACCOUNT_BANK", "sWidth":"200px"},
            {"mDataProp":"F_NAME", "sWidth":"200px"},
            {"mDataProp":"PAYMENT_TYPE", "sWidth":"200px"},
            {"mDataProp":"CREATENAME", "sWidth":"200px"},
            {"mDataProp":"CREATE_STAMP", "sWidth":"200px"},
            {"mDataProp":"AMOUNT", "sWidth":"150px"},
            {"mDataProp":"REMARK", "sWidth":"150px"}
        ]
    });	

    $("#resetBtn").click(function(){
        $('#searchForm')[0].reset();
    });

    $("#searchBtn").on('click', function () {
    	refreshData();
    });

    var refreshData= function(){
        var orderNo = $("#orderNo").val();
        var status = $("#status").val();
        var auditName = $("#auditName").val();
        var accountName = $("#account_name").val();
        costExpenseAccountTbody.fnSettings().sAjaxSource = "/costReimbursement/reimbursementList?orderNo="+orderNo+"&status="+status+"&accountName="+accountName;
        costExpenseAccountTbody.fnDraw();
    }
    //获取未审核供应商的list，选中信息在下方展示其他信息
    $('#account_name').on('click input', function(){
        var me = this;
        var inputStr = $('#account_name').val();
        var spList2 =$("#account_list");
        $.get('/transferOrder/searchSp', {input:inputStr}, function(data){
            if(inputStr!=$('#account_name').val()){//查询条件与当前输入值不相等，返回
                return;
            }
            spList2.empty();
            for(var i = 0; i < data.length; i++){
                var abbr = data[i].ABBR;
                var company_name = data[i].COMPANY_NAME;
                var contact_person = data[i].CONTACT_PERSON;
                var phone = data[i].PHONE;
                
                if(abbr == null) 
                    abbr = '';
                if(company_name == null)
                    company_name = '';
                if(contact_person == null)
                    contact_person = '';
                if(phone == null)
                    phone = '';
                
                spList2.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+abbr+" "+company_name+" "+contact_person+" "+phone+"</a></li>");
            }
            spList2.css({ 
                left:$(me).position().left+"px", 
                top:$(me).position().top+28+"px" 
            }); 
            spList2.show();
        },'json');
        
        
    });
    
    // 没选中供应商，焦点离开，隐藏列表
    $('#account_name').on('blur', function(){
        $('#account_list').hide();
    });

    //当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
    $('#account_list').on('blur', function(){
        $('#account_list').hide();
    });

    $('#account_list').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    });

    // 选中供应商
    $('#account_list').on('mousedown', '.fromLocationItem', function(e){
        console.log($('#account_list').is(":focus"));
        var message = $(this).text();
        $('#account_name').val($(this).attr("company_name"));
        var pageSpName = $("#pageSpName");
        pageSpName.empty();
        var pageSpAddress = $("#pageSpAddress");
        pageSpAddress.empty();
        pageSpAddress.append($(this).attr('address'));
        var contact_person = $(this).attr('contact_person');
        if(contact_person == 'null')
            contact_person = '';
        pageSpName.append(contact_person+'&nbsp;');
        var phone = $(this).attr('phone');
        if(phone == 'null')
            phone = '';
        pageSpName.append(phone); 
        pageSpAddress.empty();
        var address = $(this).attr('address');
        if(address == 'null')
            address = '';
        pageSpAddress.append(address);
        $('#account_list').hide();
    });
    
});