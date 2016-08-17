
$(document).ready(function() {
	document.title = '应收对账单查询 | '+document.title;

    $('#menu_charge').addClass('active').find('ul').addClass('in');

	//datatable, 动态处理
    var datatable=$('#chargeCheckList-table').dataTable({
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/chargeCheckOrder/list",
        "aoColumns": [   
            {"mDataProp":"ID", "bVisible": false},
            {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
            		if(ChargeCheck.isUpdate || ChargeCheck.isAffirm){
            			return "<a href='/chargeCheckOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
            		}else{
            			return obj.aData.ORDER_NO;
            		}
        			
        		}},
            /*{"mDataProp":"STATUS",
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
            },*/
        	{"mDataProp":"ORDER_STATUS"},
            {"mDataProp":"MONTH_STAMP"},
            {"mDataProp":null},
            {"mDataProp":"CNAME"},
            {"mDataProp":"SP"},
            {"mDataProp":null},
            {"mDataProp":"TOTAL_AMOUNT"},
            {"mDataProp":"CHARGE_AMOUNT"},
            {"mDataProp":"REMARK"},
            {"mDataProp":"C_NAME"},        	
            {"mDataProp":"CREATE_STAMP"},
            { 
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {
                    return	"<a class='btn btn-danger' href='#'"+obj.aData.ID+"'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                "取消"+
                            "</a>";
                }
            }                         
        ]      
    });	
    
    $('#searchBtn').on('click',function(){
    	var orderNo = $('#select_orderNo_filter').val();
		var beginTime = $("#kaishi_filter").val();
		var endTime = $("#jieshu_filter").val();
		var status = $("#status_filter").val();
		var customer = $("#select_customer_filter").val();
		var sp = $("#sp_filter").val();
		var tihuo = $("#tihuo_filter").val();
		var office = $("#officeName_filter").val();
		var transferOrderNo = $("#transferOrderNo_filter").val();
		var refNo = $("#refNo_filter").val();
		var serialNo = $("#serialNo_filter").val();
		datatable.fnSettings().sAjaxSource = "/chargeCheckOrder/list?orderNo="+orderNo
															+"&beginTime="+beginTime
															+"&endTime="+endTime
															+"&status="+status
															+"&customer="+customer
															+"&sp="+sp
															+"&tihuo="+tihuo
															+"&transferOrderNo="+transferOrderNo
															+"&refNo="+refNo
															+"&serialNo="+serialNo
															+"&office="+office;
		datatable.fnDraw();
    });
   
    /*--------------------------------------------------------------------*/
    //获取客户的list，选中信息自动填写其他信息
    $('#select_customer_filter').on('keyup click', function(){
        var inputStr = $('#select_customer_filter').val();
        $.get("/customerContract/search", {locationName:inputStr}, function(data){
           
            var companyList =$("#select_companyList");
            companyList.empty();
            for(var i = 0; i < data.length; i++)
            {
                companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
            }
            if(data.length>0)
                companyList.show();
            
        },'json');

    });    
    // 没选中客户，焦点离开，隐藏列表
    $('#select_customer_filter').on('blur', function(){
        $('#select_companyList').hide();
    });

    //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
    $('#select_companyList').on('blur', function(){
        $('#select_companyList').hide();
    });

    $('#select_companyList').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    });
    $('#select_companyList').on('click', '.fromLocationItem', function(e){        
        $('#select_customer_filter').val($(this).text());
        $("#select_companyList").hide();
        var companyId = $(this).attr('partyId');
        $('#customerId').val(companyId);
        refreshList();
    });
   
     //获取供应商的list，选中信息在下方展示其他信息
       $('#sp_filter').on('keyup click', function(){
       		var inputStr = $('#sp_filter').val();
       		if(inputStr == ""){
       			var pageSpName = $("#pageSpName");
       			pageSpName.empty();
       			var pageSpAddress = $("#pageSpAddress");
       			pageSpAddress.empty();
       			$('#sp_id').val($(this).attr(''));
       		}
       		$.get('/transferOrder/searchSp', {input:inputStr}, function(data){
       			console.log(data);
       			var spList =$("#spList");
       			spList.empty();
       			for(var i = 0; i < data.length; i++)
       			{
       				var abbr = data[i].ABBR;
    				if(abbr == null){
    					abbr = '';
    				}
    				var company_name = data[i].COMPANY_NAME;
    				if(company_name == null){
    					company_name = '';
    				}
    				var contact_person = data[i].CONTACT_PERSON;
    				if(contact_person == null){
    					contact_person = '';
    				}
    				var phone = data[i].PHONE;
    				if(phone == null){
    					phone = '';
    				}
    				spList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+abbr+" "+company_name+" "+contact_person+" "+phone+"</a></li>");
    			}
       		},'json');

       		$("#spList").css({ 
               	left:$(this).position().left+"px", 
               	top:$(this).position().top+32+"px" 
               }); 
               $('#spList').show();
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
       		console.log($('#spList').is(":focus"))
       		var message = $(this).text();
       		$('#sp_filter').val(message.substring(0, message.indexOf(" ")));
       		$('#sp_id').val($(this).attr('partyId'));
       		var pageSpName = $("#pageSpName");
       		pageSpName.empty();
       		var pageSpAddress = $("#pageSpAddress");
       		pageSpAddress.empty();
       		pageSpAddress.append($(this).attr('address'));
       		var contact_person = $(this).attr('contact_person');
       		if(contact_person == 'null'){
       			contact_person = '';
       		}
       		pageSpName.append(contact_person+'&nbsp;');
       		var phone = $(this).attr('phone');
       		if(phone == 'null'){
       			phone = '';
       		}
       		pageSpName.append(phone); 
       		pageSpAddress.empty();
       		var address = $(this).attr('address');
       		if(address == 'null'){
       			address = '';
       		}
       		pageSpAddress.append(address);
               $('#spList').hide();
               refreshList();
               
           });
        $('#datetimepickerK').datetimepicker({  
            format: 'yyyy-MM-dd',  
            language: 'zh-CN'
        }).on('changeDate', function(ev){
            $(".bootstrap-datetimepicker-widget").hide();
            $('#kaishi_filter').trigger('keyup');
           
        });


        $('#datetimepickerJ').datetimepicker({  
            format: 'yyyy-MM-dd',  
            language: 'zh-CN', 
            autoclose: true,
            pickerPosition: "bottom-left"
        }).on('changeDate', function(ev){
            $(".bootstrap-datetimepicker-widget").hide();
            $('#jieshu_filter').trigger('keyup');
           
        });
        //获取所有的网点

    	$.post('/transferOrder/searchPartOffice',function(data){
    		if(data.length > 0){
	    		 var officeSelect = $("#officeName_filter");
	    		 officeSelect.empty();
	    		 officeSelect.append("<option ></option>");
	    		 for(var i=0; i<data.length; i++){
	    			 officeSelect.append("<option value='"+data[i].OFFICE_NAME+"'>"+data[i].OFFICE_NAME+"</option>");					 
	    		
	    		 }
    		
	    	 }
	     },'json');
} );