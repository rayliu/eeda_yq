
$(document).ready(function() {

    $('#menu_cost').addClass('active').find('ul').addClass('in');
    
	//datatable, 动态处理
    var invoiceApplicationOrderIds = $("#invoiceApplicationOrderIds").val();
    var total = 0.00;
    var datatable=$('#costConfirm_table').dataTable({
    	"bProcessing": true, //table载入数据时，是否显示‘loading...’提示
    	"bSort": true, // 不要排序
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 100,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	"oLanguage": {
            //"sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/costConfirm/list",
        "aoColumns": [
            {"mDataProp":"ORDER_NO", "sWidth": "100px", //付款确认单号
            	"fnRender": function(obj) {
        			return "<a href='/costConfirm/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
    				$("#total_amount").val(total);
        		}
            },
            {"mDataProp": null , "sWidth": "100px",//付款申请单号
            	"fnRender": function(obj) {
            		if(obj.aData.FKSQ_NO != null){
            			return obj.aData.FKSQ_NO;
            		}else if(obj.aData.REIMBURSEMENT_NO != null){
            			return obj.aData.REIMBURSEMENT_NO;
            		}else if(obj.aData.CAR_NO != null){
            			return obj.aData.CAR_NO;
            		}else if(obj.aData.WLPJ_NO != null){
            			return obj.aData.WLPJ_NO;
            		}else{
            			return obj.aData.MISC_NO;
            		}
            	}
            }, 
            {"mDataProp":"STATUS", "sWidth": "60px"},//状态
            {"mDataProp":"SP_NAME", "sWidth": "180px"},//供应商
            {"mDataProp":"RECEIVE_PERSON","sWidth": "80px"},//收款人
            {"mDataProp":"PAY_AMOUNT","sWidth": "60px"},//付款金额  
            {"mDataProp":"ALREADY_PAY","sWidth": "60px"},//已付金额
            {"mDataProp":null,"sWidth": "60px", //未付金额
                "fnRender": function(obj) {
                    return obj.aData.PAY_AMOUNT - obj.aData.ALREADY_PAY;
                }    
            },
            {"mDataProp":"CONFIRM_TIME","sWidth": "100px"},//备注
            {"mDataProp":"USER_NAME","sWidth": "60px"},//创建人
            {"mDataProp":"CREATE_DATE","sWidth": "80px",
                "fnRender":function(obj){
                    var create_stamp=obj.aData.CREATE_DATE;
                    var str=create_stamp.substr(0,10);
                    return str;
                }
            },//创建时间           
        ]      
    });	


    $('#datetimepicker5').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();   
        $('#beginTime_filter3').trigger('keyup');
    });

    $('#datetimepicker6').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#endTime_filter3').trigger('keyup');
    });

    $('#query_btn').click(function(){
        var order_no = $('#order_no_filter').val();
        var status = $('#status_filter').val();
        var sp_name = $('#sp_filter2').val();
        var receiverName = $('#receiver_filter').val();
        var beginTime = $('#beginTime_filter').val();
        var endTime = $('#endTime_filter').val();
        datatable.fnSettings().sAjaxSource = "/costConfirm/list?orderNo="+order_no
            +"&status="+status
            +"&sp_name="+sp_name
            +"&receiverName="+receiverName
            +"&beginTime="+beginTime
            +"&endTime="+endTime;
            
        datatable.fnDraw(); 
    });
  //供应商查询
    //获取未审核供应商的list，选中信息在下方展示其他信息
    $('#sp_filter2').on('click input', function(){
        var me = this;
        var inputStr = $('#sp_filter2').val();
        var spList2 =$("#spList2");
        $.get('/transferOrder/searchSp', {input:inputStr}, function(data){
            if(inputStr!=$('#sp_filter2').val()){//查询条件与当前输入值不相等，返回
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
            refreshData();
            spList2.show();
            
        },'json');
        
        
    });
    
    // 没选中供应商，焦点离开，隐藏列表
    $('#sp_filter2').on('blur', function(){
        $('#spList2').hide();
    });

    //当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
    $('#spList2').on('blur', function(){
        $('#spList2').hide();
    });

    $('#spList2').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    });

    // 选中供应商
    $('#spList2').on('mousedown', '.fromLocationItem', function(e){
        console.log($('#spList2').is(":focus"));
        var message = $(this).text();
        $('#sp_filter2').val(message.substring(0, message.indexOf(" ")));
        $('#sp_id').val($(this).attr('partyId'));
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
        $('#spList2').hide();
        refreshData();
    });
    $("#status_filter2").on('change',function(){
    	refreshData();	
	});
    $('#order_no_filter2,#endTime_filter3,#beginTime_filter3,#receiver_filter,#business_no_filter2,#sp_filter2').on('keyup', function () {
    	refreshData();
    } );
    var refreshData=function(){
        var orderNo = $("#order_no_filter2").val();//付款确认单号
        var businessNo= $("#business_no_filter2").val();//付款申请单号
        var status = $("#status_filter2").val();
        //var customer = $("#customer_filter").val();
        var receiver = $("#receiver_filter").val();
        var sp = $("#sp_filter2").val();
        var beginTime = $("#beginTime_filter3").val();
        var endTime = $("#endTime_filter3").val();
        datatable.fnSettings().sAjaxSource = "/costConfirm/list?status="+status+"&beginTime="+beginTime+"&endTime="+endTime+"&orderNo="+orderNo+"&sp="+sp+"&receiver="+receiver+"&businessNo="+businessNo;
        datatable.fnDraw(); 
    };
} );