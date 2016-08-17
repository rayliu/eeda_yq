
$(document).ready(function() {
	document.title = '复核付款| '+document.title;
    $('#menu_finance').addClass('active').find('ul').addClass('in');
   
	if($("#page").val()=='return'){
		$('a[href="#unpay"]').tab('show');
	}
    
    var costAcceptOrderTab = $('#costAccept-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "bSort": true, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bServerSide": false,
        "iDisplayLength": 100,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "order": [
            [1, 'desc']
        ],
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID}); 
			$(nRow).attr({order_type: aData.ORDER_TYPE}); 
			return nRow;
		},
        "sAjaxSource": "/costAcceptOrder/list",
        "aoColumns": [
			{ "mDataProp": null, "sWidth":"20px", "bSortable": false,
			    "fnRender": function(obj) {
                    if(obj.aData.STATUS=='付款确认中' || obj.aData.STATUS=='已付款确认'){
                        return '';
                    }
			        return '<input type="checkbox" name="order_check_box" id="'+obj.aData.ID+'" class="invoice" order_no="'+obj.aData.ORDER_NO+'">';
			    }
			},
            {"mDataProp":"ORDER_NO","sWidth":"70px",
            	"fnRender": function(obj) {
            		return eeda.getUrlByNo(obj.aData.ID, obj.aData.ORDER_NO);
        		}
            },
            {"mDataProp":"ORDER_TYPE", "sWidth":"70px","sClass":'order_type',
                "fnRender": function(obj) {
                	var A=$(obj.aData.ORDER_NO).text().substring(0, 4);
                	if(A=='YFBX')
        				return "报销单";
                	else if(A=='XCBX')
                		return "行车报销单";
                	else
            			return obj.aData.ORDER_TYPE;
                	}
                },   
            {"mDataProp":"TOTAL_AMOUNT", "sWidth":"70px",
            	"sClass":"pay_amount",
           	 	"fnRender": function(obj) {
           	 		return "<p align='right'>"+parseFloat(obj.aData.TOTAL_AMOUNT==null?0:obj.aData.TOTAL_AMOUNT).toFixed(2)+"</p>";	
           	 	}
            },  
            {"mDataProp":"PAID_AMOUNT", "sWidth":"70px" ,
            	"fnRender": function(obj) {
            		return "<p align='right'>"+parseFloat(obj.aData.PAID_AMOUNT).toFixed(2)+"</p>";	
            	}
            },
            {"mDataProp":"NOPAID_AMOUNT", "sWidth":"70px",
            	"fnRender": function(obj) {
            		return "<p align='right'>"+parseFloat(obj.aData.NOPAID_AMOUNT).toFixed(2)+"</p>";	
            	}
            },
            {"mDataProp":"CNAME",  "sWidth":"150px",
            	"sClass": "cname"
            },  
            {"mDataProp":"PAYEE_NAME", "sWidth":"120px",
            	"sClass": "payee_name"},
            {"mDataProp":"INVOICE_NO", "sWidth":"80px" },
            {"mDataProp":"PAYMENT_METHOD",  "sWidth":"60px",
                "fnRender": function(obj) {
                    if(obj.aData.PAYMENT_METHOD == 'cash')
                        return '现金';
                    else if(obj.aData.PAYMENT_METHOD == 'transfers')
                        return '转账';
                    else
                    	return obj.aData.PAYMENT_METHOD;
                }
            },
            {"mDataProp":"STATUS", "sWidth":"80px",
            	"sClass": "status",
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
            {"mDataProp":"REMARK"}               
        ]      
    });
    
    
    var applicationTab = $('#application-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "bSort": true, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bServerSide": false,
        "iDisplayLength": 100,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID}); 
			$(nRow).attr({order_type: aData.ORDER_TYPE}); 
			return nRow;
		},
        //"sAjaxSource": "/costAcceptOrder/applicationList",
        "aoColumns": [
            {"mDataProp":"APPLICATION_ORDER_NO","sWidth":"120px",
            	 "fnRender": function(obj) {
            			return "<a href='/costPreInvoiceOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.APPLICATION_ORDER_NO+"</a>";
            	 }
            },
            {"mDataProp":"ORDER_TYPE", "sWidth":"70px","sClass":'order_type'},
            {"mDataProp":"ORDER_NO", "sWidth":"120px"},  
            {"mDataProp":"STATUS", "sWidth":"50px"},    
            {"mDataProp":"APPLICATION_AMOUNT", "sWidth":"70px",
            	"sClass":"pay_amount",
            	"fnRender": function(obj) {
            		return "<p align='right'>"+parseFloat(obj.aData.APPLICATION_AMOUNT).toFixed(2)+"</p>";	
            	}
            },
            {"mDataProp":"CNAME",  "sWidth":"150px",
            	"sClass": "cname"
            },  
            {"mDataProp":"PAYEE_NAME", "sWidth":"100px",
            	"sClass": "payee_name"},
            {"mDataProp":"PAYMENT_METHOD",  "sWidth":"60px",
                "fnRender": function(obj) {
                    if(obj.aData.PAYMENT_METHOD == 'cash')
                        return '现金';
                    else if(obj.aData.PAYMENT_METHOD == 'transfers')
                        return '转账';
                    else
                    	return obj.aData.PAYMENT_METHOD;
                }
            },
            {"mDataProp":"CREATE_TIME", "sWidth":"60px",
        		"fnRender":function(obj){
    				var create_stamp=obj.aData.CREATE_TIME;
    				var str=create_stamp.substr(0,10);
    				return str;
    			}
    		},
        	{"mDataProp":null, "sWidth":"60px",
        		"fnRender":function(obj){
    				var create_stamp=obj.aData.CHECK_TIME;
    				var str='';
    				if(create_stamp){
    					str=create_stamp.substr(0,10);
    				}
    				return str;
    			}
        	},
        	{"mDataProp":null, "sWidth":"60px",
        		"fnRender":function(obj){
    				var create_stamp=obj.aData.CONFIRM_TIME;
    				var str='';
    				if(create_stamp){
	    				str=create_stamp.substr(0,10);
	    			}
	    			return str;
    			}
        	},
            {"mDataProp":"REMARK", "sWidth":"200px"},
                       
        ]      
    });
    
    
    var clean = function(){
    	ids = [];
    	ids2 = [];
    	sids = [];
    	
    	payee_names = [];
        payee_names2 = [];
        payee_names3 = [];
        
        cnames = [];
        cnames2 = [];
        cnames3 = [];
    };
    
    
    //待申请列表
    var sids = [];
    var cnames3 = [];
	var payee_names3 = [];
    $("#costAccept-table").on('click', '.invoice', function(e){
    	var this_id = $(this).attr('id');
		var this_order_type = $(this).parent().parent().attr('order_type');
		var this_cname =  $(this).parent().siblings('.cname')[0].textContent;
		if($(this).prop("checked") == true){
			if($(this).parent().siblings('.pay_amount')[0].textContent == 0){
				$.scojs_message('申请金额不能为0!', $.scojs_message.TYPE_FALSE);
				return false;
			}
			
			sids.push(this_id+':'+this_order_type +':'+this_cname);
			
			
			if(sids.length>0){
				$("#createBtn").attr("disabled",false);
				if(sids.length > 1){
					if(cnames3[0] != $(this).parent().siblings('.cname')[0].textContent){
						$.scojs_message('请选择相同的供应商!', $.scojs_message.TYPE_FALSE);
						var tmpArr1 = [];
						for(id in sids){
							if(sids[id] != this_id+':'+this_order_type +':'+this_cname){
								tmpArr1.push(sids[id]);
							}
						}
						sids = tmpArr1;
						return false;
					}else if(payee_names3[0] != $(this).parent().siblings('.payee_name')[0].textContent){
						$.scojs_message('请选择相同的收款人!', $.scojs_message.TYPE_FALSE);
						var tmpArr2 = [];
						for(id in sids){
							if(sids[id] != this_id+':'+this_order_type +':'+this_cname){
								tmpArr2.push(sids[id]);
							}
						}
						sids = tmpArr2;
						return false;
					}
				}
				cnames3.push($(this).parent().siblings('.cname')[0].textContent);
				payee_names3.push($(this).parent().siblings('.payee_name')[0].textContent);
				//$("#order_type").val();
			}
			$('#sids').val(sids);
		}else if($(this).prop("checked") == false){
			var tmpArr = [];
			for(id in sids){
				if(sids[id] != this_id+':'+this_order_type +':'+this_cname){
					tmpArr.push(sids[id]);
				}
			}
			sids = tmpArr;
		}
		
		$("#sids").val(sids);
		if(sids.length == 0){
			$("#createBtn").attr("disabled",true);
			 cnames3 = [];
			 payee_names3 = [];
		}
	});
    
    
    $("#createBtn").on('click', function(){
    	clean();
		$("#createBtn").attr("disabled",true);
		$('#confirmForm').submit();
	});
    
	
	$('#datetimepicker3').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#beginTime_filter2').trigger('keyup');
    });
	$('#datetimepicker4').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#endTime_filter2').trigger('keyup');
    });
	$('#datetimepicker1').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#beginTime_filter1').trigger('keyup');
    });
	$('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#endTime_filter1').trigger('keyup');
    });
	
	$('#confirmBtn').click(function(e){
        e.preventDefault();
        $('#confirmForm').submit();
    });
    
    
    
    
    $.post('/costMiscOrder/searchAllAccount',function(data){
		 if(data.length > 0){
			 var accountTypeSelect = $("#accountTypeSelect");
			 accountTypeSelect.empty();
			 var hideAccountId = $("#hideAccountId").val();
			 accountTypeSelect.append("<option ></option>");
			 for(var i=0; i<data.length; i++){
				 if(data[i].ID == hideAccountId){
					 accountTypeSelect.append("<option value='"+data[i].ID+"' selected='selected'>" + data[i].BANK_PERSON+ " " + data[i].BANK_NAME+ " " + data[i].ACCOUNT_NO + "</option>");
				 }else{
					 accountTypeSelect.append("<option value='"+data[i].ID+"'>" + data[i].BANK_PERSON+ " " + data[i].BANK_NAME+ " " + data[i].ACCOUNT_NO + "</option>");					 
				 }
			}
		}
	},'json');
    
	
	$("#checkBtn").on('click', function(){
		clean();
		$("#checkBtn").attr("disabled",true);
		var idArr=[];  	
		var orderArr=[];
        $("input[name='order_check_box']").each(function(){
        	if($(this).prop('checked') == true){
        		idArr.push($(this).attr('id'));
        		orderArr.push($(this).parent().parent().find('.order_type').text());
        	}
        });     
        console.log(idArr);
        var ids = idArr.join(",");
        var order= orderArr.join(",");
        $.post("/costAcceptOrder/checkStatus", {ids:ids,order:order}, function(data){
        	costAcceptOrderTab.fnDraw(); 
        	uncostAcceptOrderTab.fnDraw();
        },'json');
	});
	
	
	$('#customer_filter').on('keyup click', function(){
		var me = this;
        var inputStr = $('#customer_filter').val();
        $.get("/customerContract/search", {locationName:inputStr}, function(data){
            //console.log(data);
            var companyList =$("#companyList");
            companyList.empty();
            for(var i = 0; i < data.length; i++)
            {
                companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
            }
            if(data.length>0)
                companyList.show();
            companyList.css({ 
    	    	left:$(me).position().left+"px", 
    	    	top:$(me).position().top+30+"px" 
    	    }); 
        },'json');
    });
	//当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
    $('#companyList').on('blur', function(){
        $('#companyList').hide();
    });

    $('#companyList').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    });
    $('#companyList').on('click', '.fromLocationItem', function(e){        
        $('#customer_filter').val($(this).text());
        $("#companyList").hide();
        var companyId = $(this).attr('partyId');
        $('#customerId').val(companyId);
       
    });
 // 没选中客户，焦点离开，隐藏列表
    $('#customer_filter').on('blur', function(){
        $('#companyList').hide();
    });
    $('#customer_filter1').on('keyup click', function(){
    	var me = this;
        var inputStr = $('#customer_filter1').val();
        $.get("/customerContract/search", {locationName:inputStr}, function(data){
            //console.log(data);
            var companyList =$("#companyList1");
            companyList.empty();
            for(var i = 0; i < data.length; i++)
            {
                companyList.append("<li><a tabindex='-1' class='fromLocationItem1' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
            }
            if(data.length>0){
            	companyList.css({ 
	    	    	left:$(me).position().left+"px", 
	    	    	top:$(me).position().top+30+"px" 
	    	    }); 
                companyList.show();
            }
	            
        },'json');
    });
	//当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
    $('#companyList1').on('blur', function(){
        $('#companyList1').hide();
    });
  //供应商查询
    //获取未审核供应商的list，选中信息在下方展示其他信息
    $('#sp_filter').on('click input', function(){
        var me = this;
        var inputStr = $('#sp_filter').val();
        var spList =$("#spList");
        $.get('/transferOrder/searchSp', {input:inputStr}, function(data){
            if(inputStr!=$('#sp_filter').val()){//查询条件与当前输入值不相等，返回
                return;
            }
            spList.empty();
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
                
                spList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+abbr+" "+company_name+" "+contact_person+" "+phone+"</a></li>");
            }
            spList.css({ 
                left:$(me).position().left+"px", 
                top:$(me).position().top+28+"px" 
            }); 
            refreshData1();
            spList.show();
            
        },'json');
        
        
    });
    
    // 没选中供应商，焦点离开，隐藏列表
    $('#sp_filter').on('blur', function(){
        $('#spList').hide();
    });

    //当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
    $('#spList').on('blur', function(){
        $('#spList').hide();
    });

    $('#spList').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    });

    // 选中供应商
    $('#spList').on('mousedown', '.fromLocationItem', function(e){
        console.log($('#spList').is(":focus"));
        var message = $(this).text();
        $('#sp_filter').val(message.substring(0, message.indexOf(" ")));
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
        $('#spList').hide();
        refreshData1();
    });
  //供应商查询
    //获取已复核供应商的list，选中信息在下方展示其他信息
    $('#sp_filter1').on('input click', function(){
        var me = this;
        var inputStr = $('#sp_filter1').val();
        var spList =$("#spList1");
        $.get('/transferOrder/searchSp', {input:inputStr}, function(data){
            if(inputStr!=$('#sp_filter1').val()){//查询条件与当前输入值不相等，返回
                return;
            }
            spList.empty();
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
                
                spList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+abbr+" "+company_name+" "+contact_person+" "+phone+"</a></li>");
            }
            spList.css({ 
                left:$(me).position().left+"px", 
                top:$(me).position().top+28+"px" 
            }); 
            refreshData();
            spList.show();
            
        },'json');
        
        
    });
    
    // 没选中供应商，焦点离开，隐藏列表
    $('#sp_filter1').on('blur', function(){
        $('#spList1').hide();
    });

    //当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
    $('#spList1').on('blur', function(){
        $('#spList1').hide();
    });

    $('#spList1').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    });

    // 选中供应商
    $('#spList1').on('mousedown', '.fromLocationItem', function(e){
        console.log($('#spList1').is(":focus"));
        var message = $(this).text();
        $('#sp_filter1').val(message.substring(0, message.indexOf(" ")));
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
        $('#spList1').hide();
        refreshData();
    });
    $('#companyList1').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    });
    $('#companyList1').on('click', '.fromLocationItem1', function(e){        
        $('#customer_filter1').val($(this).text());
        $("#companyList1").hide();
        var companyId = $(this).attr('partyId');
        $('#customerId1').val(companyId);
       
    });
    // 没选中客户，焦点离开，隐藏列表
    $('#customer_filter1').on('blur', function(){
        $('#companyList1').hide();
    });
    
    $("#status_filter1").on('change', function () {
    	refreshData();
    });
    
    $("#orderType").on('change', function () {
    	refreshData();
    });
    
    $('#beginTime_filter2,#endTime_filter2,#orderNo_filter1,#orderType').on('keyup', function () {
    	refreshData();
    } );
    
   

    var saveConditions=function(){
        var conditions={
            orderNo : $("#orderNo_filter1").val(),
            status : $("#status_filter1").val(),
            sp : $("#sp_filter1").val(),
            orderTpye : $("#orderTpye").val(),
            beginTime : $("#beginTime_filter2").val(),
            endTime : $("#endTime_filter2").val()
        }
        if(!!window.localStorage){//查询条件处理
            localStorage.setItem("query_costAcceptOrder", JSON.stringify(conditions));
        }
    };

    //待申请页面
    var refreshData=function(){
        var orderNo = $("#orderNo_filter1").val();//单号
        var status = $("#status_filter1").val();
        //var customer = $("#customer_filter").val();
        var sp = $("#sp_filter1").val();
        var beginTime = $("#beginTime_filter2").val();
        var endTime = $("#endTime_filter2").val();
        var orderType = $("#orderType").val();

        costAcceptOrderTab.fnSettings().oFeatures.bServerSide = true;
        costAcceptOrderTab.fnSettings().sAjaxSource = "/costAcceptOrder/list?status="+status
            +"&beginTime="+beginTime+"&endTime="+endTime+"&orderNo="+orderNo+"&sp="+sp+"&orderType="+orderType;

        costAcceptOrderTab.fnDraw(); 

        saveConditions();
    };
    
    
    
    $("#search2Btn").on('click', function () {
    	refreshData2();
    	saveConditions2();
    });
    
    var saveConditions2=function(){
        var conditions={
        	applicationOrderNo : $("#applicationOrderNo").val(),//申请单号
            orderNo : $("#orderNo").val(),//业务单号
            status : $("#status2").val(),
            sp : $("#sp_id_input").val(),
            beginTime : $("#begin_date").val(),
            endTime : $("#end_date").val(),
            check_begin_date : $("#check_begin_date").val(),
            check_end_date : $("#check_end_date").val(),
            confirmBeginTime : $("#confirmBegin_date").val(),
            confirmEndTime : $("#confirmEnd_date").val(),
            insurance : $("#insurance").val()
        };
        if(!!window.localStorage){//查询条件处理
            localStorage.setItem("query_costAcceptOrder2", JSON.stringify(conditions));
        }
    };
    
    
  //待付款页面
    var refreshData2=function(){
        var applicationOrderNo = $("#applicationOrderNo").val();//申请单号
        var orderNo = $("#orderNo").val();//业务单号
        var status = $("#status2").val();
        var sp = $("#sp_id_input").val();
        var beginTime = $("#begin_date").val();
        var endTime = $("#end_date").val();
        var check_begin_date = $("#check_begin_date").val();
        var check_end_date = $("#check_end_date").val();
        var confirmBeginTime = $("#confirmBegin_date").val();
        var confirmEndTime = $("#confirmEnd_date").val();
        var insurance = $("#insurance").val();

        applicationTab.fnSettings().oFeatures.bServerSide = true;
        applicationTab.fnSettings().sAjaxSource = "/costAcceptOrder/applicationList?status="+status
            +"&beginTime="+beginTime+"&endTime="+endTime
            +"&check_begin_date="+check_begin_date+"&check_end_date="+check_end_date
            +"&confirmBeginTime="+confirmBeginTime+"&confirmEndTime="+confirmEndTime
            +"&applicationOrderNo="+applicationOrderNo+"&orderNo="+orderNo
            +"&sp="+sp+"&insurance="+insurance;;

        applicationTab.fnDraw(); 
    };
    //未申请界面
    var loadConditions=function(){
        if(!!window.localStorage){//查询条件处理
            var query_json = localStorage.getItem('query_costAcceptOrder');
            if(!query_json)
                return;

            var conditions = JSON.parse(query_json);

            $("#orderNo_filter1").val(conditions.orderNo);//单号
            $("#status_filter1").val(conditions.status);
            //var customer = $("#customer_filter").val();
            $("#sp_filter1").val(conditions.sp);
            $("#orderType").val(conditions.orderType);
            $("#beginTime_filter2").val(conditions.beginTime);
            $("#endTime_filter2").val(conditions.endTime);
        }
    };
    //已申请界面
    var loadConditions2=function(){
        if(!!window.localStorage){//查询条件处理
            var query_json = localStorage.getItem('query_costAcceptOrder2');
            if(!query_json)
                return;

            var conditions = JSON.parse(query_json);
            $("#applicationOrderNo").val(conditions.applicationOrderNo);//申请单号
            $("#orderNo").val(conditions.orderNo);//单号
            $("#status2").val(conditions.status);
            $("#sp_id_input").val(conditions.sp);
            $("#begin_date").val(conditions.beginTime);
            $("#end_date").val(conditions.endTime);
            $("#check_begin_date").val(conditions.check_begin_date);
            $("#check_end_date").val(conditions.check_end_date);
            $("#confirmBegin_date").val(conditions.confirmBeginTime);
            $("#confirmEnd_date").val(conditions.confirmBeginTime);
        }
    };
    loadConditions();
    loadConditions2();
    refreshData();
    refreshData2();
 
} );