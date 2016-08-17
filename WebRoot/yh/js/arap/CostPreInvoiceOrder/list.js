
$(document).ready(function() {
	document.title = '付款申请 | '+document.title;

    $('#menu_cost').addClass('active').find('ul').addClass('in');

	//datatable, 动态处理
    var datatable=$('#costPreInvoiceOrderList-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/costPreInvoiceOrder/list",
        "aoColumns": [   
            {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
        			return "<a href='/costPreInvoiceOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
        		}},
            {"mDataProp":"STATUS", "sWidth":"80px",
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
            {"mDataProp":"CNAME",
            	"sClass": "cname"
            },
            {"mDataProp":"PAYMENT_METHOD", "sWidth":"80px",
                "fnRender": function(obj) {
                    if(obj.aData.PAYMENT_METHOD == 'cash')
                        return '现金';
                    else if(obj.aData.PAYMENT_METHOD == 'transfers')
                        return '转账';
                    else
                    	return '';
                }
            },
            {"mDataProp":"PAYEE_NAME",
            	"sClass": "payee_name"
            },
            {"mDataProp":"C_STAMP"},
            {"mDataProp":"TOTAL_AMOUNT"},
            {"mDataProp":"PAY_AMOUNT"},
            {"mDataProp":"COST_ORDER_NO"},
            {"mDataProp":"ONAME"},
            {"mDataProp":"COMPANY_NAME"},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":"REMARK"},
            {"mDataProp":"CREATE_B"},
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
    
    
    
   
    
    
    /*--------------------------------------------------------------------*/
    //获取所有客户
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
	   $("#companyList").css({ 
			left:$(this).position().left+"px", 
			top:$(this).position().top+32+"px" 
	   }); 
	   
    });

    //选中某个客户时候
    $('#companyList').on('click', '.fromLocationItem', function(e){        
       $('#customer_filter').val($(this).text());
       $("#companyList").hide();
       var companyId = $(this).attr('partyId');
       $('#companyList').val(companyId);
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
   
   //获取所有客户
   $('#customer_filter2').on('keyup click', function(){
	   var inputStr = $('#customer_filter2').val();
	   $.get("/customerContract/search", {locationName:inputStr}, function(data){
	  
	   var companyList =$("#companyList2");
	   companyList.empty();
	   for(var i = 0; i < data.length; i++)
	   {
	       companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
	       }
	       if(data.length>0)
	           companyList.show();
	   },'json');
	   $("#companyList2").css({ 
			left:$(this).position().left+"px", 
			top:$(this).position().top+32+"px" 
	   }); 
	  
   });

   //选中某个客户时候
   $('#companyList2').on('click', '.fromLocationItem', function(e){        
      $('#customer_filter2').val($(this).text());
      $("#companyList2").hide();
      var companyId = $(this).attr('partyId');
      $('#companyList2').val(companyId);
      refreshList();
  });
  // 没选中客户，焦点离开，隐藏列表
  $('#customer_filter2').on('blur', function(){
      $('#companyList2').hide();
  });

  //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
  $('#companyList2').on('blur', function(){
      $('#companyList2').hide();
  });

  $('#companyList2').on('mousedown', function(){
      return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
  });
   
   //获取供应商的list，选中信息在下方展示其他信息
   $('#sp_filter').on('input click', function(){
   		var me=this;
		var inputStr = $('#sp_filter').val();
		if(inputStr == ""){
			var pageSpName = $("#pageSpName");
			pageSpName.empty();
			var pageSpAddress = $("#pageSpAddress");
			pageSpAddress.empty();
			$('#sp_id').val($(this).attr(''));
		}
		$.get('/transferOrder/searchSp', {input:inputStr}, function(data){
			if(inputStr!=$('#sp_filter').val()){//查询条件与当前输入值不相等，返回
				return;
			}
			var spList =$("#spList");
			spList.empty();
			for(var i = 0; i < data.length; i++){
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
			$("#spList").css({ 
				left:$(me).position().left+"px", 
				top:$(me).position().top+32+"px" 
			}); 
		    $('#spList').show();
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
   		
   		var message = $(this).text();
   		$('#sp_filter').val(message.substring(0, message.indexOf(" ")));
   		$('#spList').val($(this).attr('partyId'));
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
   	var refreshList  = function(){
   	  var sp = $("#sp_filter").val();
      var customer = $("#customer_filter2").val();
      var orderNo = $("#select_orderNo_filter").val();
      var beginTime = $("#kaishi_filter").val();
      var endTime = $("#jieshu_filter").val();
      var status = $("#status_filter").val();
      var selectOrderNO = $("#orderNo").val(); 
      datatable.fnSettings().sAjaxSource = "/costPreInvoiceOrder/list?orderNo="+orderNo       
												      +"&customer="+customer
												      +"&sp="+sp
												      +"&beginTime="+beginTime
												      +"&endTime="+endTime
												      +"&status="+status
												      +"&selectOrderNO="+selectOrderNO;
      datatable.fnDraw();
   	};
   	$("#sp_filter,#orderNo,#customer_filter2,#select_orderNo_filter,#kaishi_filter,#jieshu_filter,#status_filter").on('keyup input',function(){
   		refreshList();
   	});
  
    $('#datetimepicker3').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#kaishi_filter').trigger('keyup');
    });

    $('#datetimepicker4').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#jieshu_filter').trigger('keyup');
    });
} );