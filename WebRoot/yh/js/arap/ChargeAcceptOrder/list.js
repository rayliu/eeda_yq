
$(document).ready(function() {
	document.title = '待申请列表 | '+document.title;
	
	if($("#page").val()=='return'){
    	$('a[href="#panel-2"]').tab('show');
    }
	
    $('#menu_finance').addClass('active').find('ul').addClass('in');
   
	//datatable, 动态处理
    var chargeNoAcceptOrderTab = $('#chargeNoAccept-table').dataTable({
    	"bProcessing": true, //table载入数据时，是否显示‘loading...’提示
        "bFilter": false, //不需要默认的搜索框
        "bSort": true, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bServerSide": true,
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
        "sAjaxSource": "/chargeAcceptOrder/list",
        "aoColumns": [   
	        { "mDataProp": null, "sWidth":"20px",
	            "fnRender": function(obj) {
	            	return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';
	            }
            }, 
            {"mDataProp":"ORDER_NO","sWidth":"150px",
            	"fnRender": function(obj) {
            		return eeda.getUrlByNo(obj.aData.ID, obj.aData.ORDER_NO);
        		}},
            {"mDataProp":"ORDER_TYPE","sWidth":"120px",
        			"sClass":"order_type"
        	},   
            {"mDataProp":"TOTAL_AMOUNT","sWidth":"120px","sClass":"total_amount",
            	"fnRender": function(obj) {
            		return "<p align='right'>"+parseFloat(obj.aData.TOTAL_AMOUNT).toFixed(2)+"</p>";
            	}  
            },
            {"mDataProp":"RECEIVE_AMOUNT","sWidth":"120px","sClass":"receive_amount",
            	"fnRender": function(obj) {
            		return "<p align='right'>"+parseFloat(obj.aData.RECEIVE_AMOUNT).toFixed(2)+"</p>";
            	}  
            },  
            {"mDataProp":"NORECEIVE_AMOUNT","sWidth":"120px","sClass":"noreceive_amount",
            	"fnRender": function(obj) {
            		return "<p align='right'>"+parseFloat(obj.aData.NORECEIVE_AMOUNT).toFixed(2)+"</p>";
            	}  
            },    
            {"mDataProp":"CNAME","sWidth":"180px","sClass":"cname"},   
            {"mDataProp":"PAYEE","sWidth":"120px","sClass":"payee"}, 
            {"mDataProp":"INVOICE_NO","sWidth":"120px"},     
            {"mDataProp":null,"sWidth":"120px"},  
            {"mDataProp":"STATUS","sWidth":"120px",
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
            {"mDataProp":"REMARK","sWidth":"180px"},                       
        ]      
    });
    
    
   var clean = function(){
   	sids = [];
   	payee_names = [];   
    cnames = [];
   };
   
   
   //待申请列表
   var sids = [];
   var cnames = [];
	var payee_names = [];
   $("#chargeNoAccept-table").on('click', '.checkedOrUnchecked', function(e){
   	var this_id = $(this).val();
		var this_order_type = $(this).parent().parent().attr('order_type');
		var this_cname = $(this).parent().siblings('.cname')[0].textContent;
		if($(this).prop("checked") == true){
			if($(this).parent().siblings('.total_amount')[0].textContent == 0){
				$.scojs_message('金额不能为0!', $.scojs_message.TYPE_FALSE);
				return false;
			}
			sids.push(this_id+':'+this_order_type+':'+this_cname);
			if(sids.length>0){
				$("#createBtn").attr("disabled",false);
				if(sids.length > 1){
					if(cnames[0] != $(this).parent().siblings('.cname')[0].textContent){
						$.scojs_message('请选择相同的付款单位!', $.scojs_message.TYPE_FALSE);
						var tmpArr1 = [];
						for(id in sids){
							if(sids[id] != this_id+':'+this_order_type+':'+this_cname){
								tmpArr1.push(sids[id]);
							}
						}
						sids = tmpArr1;
						return false;
					}else if(payee_names[0] != $(this).parent().siblings('.payee')[0].textContent){
						$.scojs_message('请选择相同的付款人!', $.scojs_message.TYPE_FALSE);
						var tmpArr2 = [];
						for(id in sids){
							if(sids[id] != this_id+':'+this_order_type+':'+this_cname){
								tmpArr2.push(sids[id]);
							}
						}
						sids = tmpArr2;
						return false;
					}
				}
				cnames.push($(this).parent().siblings('.cname')[0].textContent);
				payee_names.push($(this).parent().siblings('.payee')[0].textContent);
			}
			$('#sids').val(sids);
		}else if($(this).prop("checked") == false){
			var tmpArr = [];
			for(id in sids){
				if(sids[id] != this_id+':'+this_order_type+':'+this_cname){
					tmpArr.push(sids[id]);
				}
			}
			sids = tmpArr;
		}
		
		$("#sids").val(sids);
		if(sids.length == 0){
			$("#createBtn").attr("disabled",true);
			 cnames = [];
			 payee_names = [];
		}
	});
   
   
   $("#createBtn").on('click', function(){
   		clean();
		$("#createBtn").attr("disabled",true);
		$('#confirmForm').submit();
	});
   
   
 
	
	$("#status_filter").on('change',function(){
		var status = $("#status_filter").val();
		chargeNoAcceptOrderTab.fnSettings().sAjaxSource = "/chargeAcceptOrder/list?status="+status;
		chargeNoAcceptOrderTab.fnDraw(); 
	});
	
	
	//**
	//*****
	//*********
	//*************已申请列表
	//*********
	//*****
	//**
	
	
	//datatable, 动态处理
    var chargeAcceptOrderTab = $('#chargeAccept-table').dataTable({
    	"bProcessing": true, //table载入数据时，是否显示‘loading...’提示
        "bFilter": false, //不需要默认的搜索框
        "bSort": true, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bServerSide": true,
        "iDisplayLength": 100,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID}); 
			return nRow;
		},
        "sAjaxSource": "/chargeAcceptOrder/applicationList",
        "aoColumns": [   
	        { "mDataProp": null, "sWidth":"20px",
	            "fnRender": function(obj) {	
	            	return '<input type="checkbox" name="order_check_box" class="checkedOrUnchecked" value="'+obj.aData.ID+'">';     
	            }
            }, 
            {"mDataProp":"APPLICATION_ORDER_NO","sWidth":"150px",
            	"fnRender": function(obj) {
        			return "<a href='/chargePreInvoiceOrder/edit?id="+obj.aData.ID+"' target='_blank'>"+obj.aData.APPLICATION_ORDER_NO+"</a>";
            	}
    		},
            {"mDataProp":"ORDER_TYPE","sWidth":"120px",
        			"sClass":"order_type"
        	},   
        	{"mDataProp":"ORDER_NO","sWidth":"120px"},
            {"mDataProp":"INVOICE_NO","sWidth":"120px"},
            {"mDataProp":"STATUS","sWidth":"120px",
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
            {"mDataProp":"APPLICATION_AMOUNT","sWidth":"80px",
            	"fnRender": function(obj) {
            		return "<p align='right'>"+parseFloat(obj.aData.APPLICATION_AMOUNT).toFixed(2)+"</p>";
            	}  
            },         
            {"mDataProp":null,"sWidth":"100px"},     
            {"mDataProp":null,"sWidth":"100px"},
            {"mDataProp":null,"sWidth":"100px"},
            {"mDataProp":"CNAME","sWidth":"200px"},   
            {"mDataProp":"REMARK","sWidth":"180px"},                     
        ]      
    });
	
	
    var ids2 = [];
    // 未选中列表
	$("#chargeAccept-table").on('click', '.checkedOrUnchecked', function(e){
		$("#confirmBtn").attr('disabled',false);
		if($(this).prop("checked") == true){
            var orderNo = $(this).parent().parent().find('.order_type').text();
            var orderObj=$(this).val()+":"+orderNo;
            //var order = ids.pop();
			ids2.push(orderObj);
		}else{
			var array = [];
			for(id in ids2){
				if($(this).val()+":"+$(this).parent().parent().find('.order_type').text()!= ids2[id]){
					array.push(ids2[id]);
				}
			}
			ids2 = array;
		}	
		$("#chargeIds2").val(ids2);
		if(ids2.length != 0 ){
			$("#confirmBtn").attr('disabled',false);
		}else{
			$("#confirmBtn").attr('disabled',true);
		}
	});	
	
	
	$('#confirmBtn').click(function(e){
		$("#checkBtn").attr('disabled',true);
        e.preventDefault();
        $('#confirmForm').submit();
        ids = [];
    });
	
	
	//未复核数据条件查询
	var refreshData=function(){
		var orderNo_filter =  $("#orderNo_filter").val();
		var customer_filter =  $("#customer_filter").val();
		var beginTime_filter =  $("#beginTime_filter").val();
		var endTime_filter =  $("#endTime_filter").val();

        chargeNoAcceptOrderTab.fnSettings().sAjaxSource="/chargeAcceptOrder/list?orderNo_filter="+orderNo_filter
        		                                       +"&customer_filter="+customer_filter
        		                                       +"&beginTime_filter="+beginTime_filter
        		                                       +"&endTime_filter="+endTime_filter;
		chargeNoAcceptOrderTab.fnDraw();
    };
    
    
    $("#orderNo_filter,#customer_filter,#beginTime_filter,#endTime_filter").on('keyup',function(){
    	refreshData();
    });
    
    
    
    
    
    
  //已申请数据条件查询
	var refreshData2=function(){
		var orderNo_filter =  $("#orderNo_filter2").val();
		var customer_filter =  $("#customer_filter2").val();
		var beginTime_filter =  $("#beginTime_filter2").val();
		var endTime_filter =  $("#endTime_filter2").val();
		var status_filter =  $("#status_filter8").val();
		var applicationOrderNo =  $("#applicationOrderNo").val();

		chargeAcceptOrderTab.fnSettings().sAjaxSource="/chargeAcceptOrder/applicationList?applicationOrderNo="+applicationOrderNo
        		                                       +"&cname="+customer_filter
        		                                       +"&orderNo="+orderNo_filter
        		                                       +"&status="+status_filter
        		                                       +"&beginTime="+beginTime_filter
        		                                       +"&endTime="+endTime_filter;
       
		chargeAcceptOrderTab.fnDraw();
    };
    
    
    $("#search2Btn").on('click', function () {
    	refreshData2();
    });
//    $("#orderNo_filter2,#customer_filter2,#beginTime_filter2,#endTime_filter2").on('keyup blur',function(){
//    	refreshData2();
//    });
    

	
	//获取客户列表，自动填充
    $('#customer_filter').on('keyup click', function(event){
        var me = this;
        var inputStr = $('#customer_filter').val();
        var companyList =$("#companyList");
        $.get("/transferOrder/searchCustomer", {input:inputStr}, function(data){
            companyList.empty();
            for(var i = 0; i < data.length; i++)
                companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
                
            companyList.css({ 
		    	left:$(me).position().left+"px", 
		    	top:$(me).position().top+32+"px" 
		    });
	        companyList.show();    
        },'json');  
    });
    
    $('#companyList').on('click', '.fromLocationItem', function(e){        
        $('#customer_filter').val($(this).text());
        $("#companyList").hide();
//        var companyId = $(this).attr('partyId');
//        $('#customerId').val(companyId);
    	refreshData();
    });
    // 没选中客户，焦点离开，隐藏列表
    $('#customer_filter').on('blur', function(){
        $('#companyList').hide();
    });

    //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
    $('#customer_filter').on('blur', function(){
        $('#companyList').hide();
    });

    $('#companyList').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    });
    
    
    //已复核界面的客户
  //获取客户列表，自动填充
    $('#customer_filter2').on('keyup click', function(event){
        var me = this;
        var inputStr = $('#customer_filter2').val();
        var companyList2 =$("#companyList2");
        $.get("/transferOrder/searchCustomer", {input:inputStr}, function(data){
            companyList2.empty();
            for(var i = 0; i < data.length; i++)
                companyList2.append("<li><a tabindex='-1' class='fromLocationItem2' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
                
            companyList2.css({ 
		    	left:$(me).position().left+"px", 
		    	top:$(me).position().top+32+"px" 
		    });
	        companyList2.show();    
        },'json');  
    });
    
    $('#companyList2').on('click', '.fromLocationItem2', function(e){        
        $('#customer_filter2').val($(this).text());
        $("#companyList2").hide();
    	refreshData2();
    });
    // 没选中客户，焦点离开，隐藏列表
    $('#customer_filter2').on('blur', function(){
        $('#companyList2').hide();
    });

    //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
    $('#customer_filter2').on('blur', function(){
        $('#companyList2').hide();
    });

    $('#companyList2').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    });
    
    


      //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
      $('#customer_filter3').on('blur', function(){
          $('#companyList3').hide();
      });

      $('#companyList3').on('mousedown', function(){
          return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
      });
    
	
} );