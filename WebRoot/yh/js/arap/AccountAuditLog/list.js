
$(document).ready(function() {
	document.title = '出纳日记账查询 | '+document.title;

    $('#menu_finance').addClass('active').find('ul').addClass('in');
    
    $("#beginTime_filter").val(new Date().getFullYear()+'-'+ (new Date().getMonth()+1));
    
	//datatable, 动态处理
    var checkedIds = [];
    var accountAuditLogTable = $('#accountAuditLog-table').dataTable({
        "bProcessing": true, //table载入数据时，是否显示‘loading...’提示
    	"bFilter": false, //不需要默认的搜索框
        "bSort": true, 
        "iDisplayLength": 100,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/accountAuditLog/list?beginTime="+$("#beginTime_filter").val(),
        "aoColumns": [
            {"mDataProp":null, "sWidth":"20px",
                "fnRender": function(obj) {
                	var str = "";
                	if(checkedIds.length>0){
                		for(id in checkedIds){
                    		if(checkedIds[id] == obj.aData.ID){
                    			return str =  '<input type="checkbox" id="'+obj.aData.ID+'" class="invoice" order_no="'+obj.aData.ORDER_NO+'" checked = "checked">';
                           }else{
                        	    str =  '<input type="checkbox" id="'+obj.aData.ID+'" class="invoice" order_no="'+obj.aData.ORDER_NO+'">';
                            }
                    	}
                	}else{
                 	   str = '<input type="checkbox" id="'+obj.aData.ID+'" class="invoice" order_no="'+obj.aData.ORDER_NO+'">';
                    }
                	return str;
                }
            },
            {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
            		if(obj.aData.SOURCE_ORDER=='应付开票申请单'||obj.aData.SOURCE_ORDER=='应收开票申请单'||obj.aData.SOURCE_ORDER=='转账单'){
            			return eeda.getUrlByNo(obj.aData.INVOICE_ORDER_ID, obj.aData.ORDER_NO);
            		}else{
            			return  obj.aData.ORDER_NO;
            		}
            		
            	}
            },
            {"mDataProp":"SOURCE_ORDER"},
            {"mDataProp":"CHARGE_AMOUNT",
            	"fnRender": function(obj) {
            		if(obj.aData.CHARGE_AMOUNT!=null)
            			return "<p align='right'>"+eeda.numFormat(parseFloat(obj.aData.CHARGE_AMOUNT).toFixed(2),3)+"</p>";
            		else
            			return obj.aData.CHARGE_AMOUNT;
            	}  
            },   
            {"mDataProp":"PAYEE_NAME_IN"},
            {"mDataProp":"COST_AMOUNT",
            	"fnRender": function(obj) {
            		if(obj.aData.COST_AMOUNT!=null)
            			return "<p align='right'>"+eeda.numFormat(parseFloat(obj.aData.COST_AMOUNT).toFixed(2),3)+"</p>";
            		else
            			return obj.aData.COST_AMOUNT;
            	}  
            },     
            {"mDataProp":"PAYEE_NAME_OUT"},
            {"mDataProp":"BANK_NAME"},
            {"mDataProp":"CREATE_DATE"},
            {"mDataProp":"USER_NAME"}
        ]      
    });

    //accountAuditLogTable.fnSetColumnVis(4, false );
    
    var accountTable = $('#account-table').dataTable({
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, 
        // "bPaginate": false, //翻页功能
        "bInfo": false,//页脚信息
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"bServerSide": true,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	"fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			$(nRow).attr('account_id', aData.ID);
			return nRow;
		},
    	"sAjaxSource": "/accountAuditLog/accountList?beginTime="+ $("#beginTime_filter").val(),
    	"aoColumns": [   
	        { "mDataProp": null, "sWidth":"30px",
              "fnRender": function(obj) {
                return '<input type="checkbox" name="order_check_box" value="'+obj.aData.ID+'">';
              }
            },
	        {"mDataProp":"BANK_NAME"},
	        {"mDataProp":"DATE"},
	        {"mDataProp": "INIT_AMOUNT",  //期初
	        	"fnRender": function(obj) {
	        		return eeda.numFormat(obj.aData.INIT_AMOUNT,3);
	        	}
    		},
            {"mDataProp": "TOTAL_CHARGE",  //本期收入
	        	"fnRender": function(obj) {
	        		return eeda.numFormat(obj.aData.TOTAL_CHARGE,3);
	        	}
    		}, 
	        {"mDataProp":"TOTAL_COST",  //本期支出
	        	"fnRender": function(obj) {
	        		return eeda.numFormat(obj.aData.TOTAL_COST,3);
	        	}
    		},  
            {"mDataProp":"BALANCE_AMOUNT",   //期末结余    
	        	"fnRender": function(obj) {
	        		return eeda.numFormat(obj.aData.BALANCE_AMOUNT,3);
	        	}
    		}    
	     ]      
    });
    
    $('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM',  
        viewMode: "months",
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#beginTime_filter').trigger('keyup');
        
       	var idArr=[];
   	    $("input[name='order_check_box']").each(function(){
   	    	if($(this).prop('checked') == true){
   	    		idArr.push($(this).parent().parent().attr("account_id"));
   	    	}
   	    });
   	    var ids = idArr.toString();
   		var beginTime = ev.date.getFullYear()+'-'+(ev.date.getMonth()+1);

   		accountTable.fnSettings().sAjaxSource = "/accountAuditLog/accountList?beginTime="+beginTime;
   		accountTable.fnDraw();
   		
   		accountAuditLogTable.fnSettings().sAjaxSource = "/accountAuditLog/list?ids="+ids+"&beginTime="+beginTime;
   		accountAuditLogTable.fnDraw();
    });
    
    $("#account-table").on('click', function(e){
    	var idArr=[];
   	    $("input[name='order_check_box']").each(function(){
   	    	if($(this).prop('checked') == true){
   	    		idArr.push($(this).parent().parent().attr("account_id"));
   	    	}
   	    });
   	    var ids = idArr.toString();
   		var beginTime =$("#beginTime_filter").val();

   		//accountTable.fnSettings().sAjaxSource = "/accountAuditLog/accountList?beginTime="+beginTime;
   		//accountTable.fnDraw();
   		
   		accountAuditLogTable.fnSettings().sAjaxSource = "/accountAuditLog/list?ids="+ids+"&beginTime="+beginTime;
   		accountAuditLogTable.fnDraw();
    });


    $('#datetimepicker3').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#beginTime').trigger('keyup');
    });

    $('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#endTime').trigger('keyup');
    });
    
    
    var find = function(){
    	var source_order = $('#source_order').val();
    	var orderNo = $('#orderNo').val();
    	var beginTime = $('#beginTime').val();
    	var endTime = $('#endTime').val();
    	var bankName = $('#bankName').val();
    	var money = $('#money').val();
    	accountAuditLogTable.fnSettings().sAjaxSource = "/accountAuditLog/list?source_order="+source_order
										            +"&orderNo="+orderNo
										            +"&bankName="+bankName
										            +"&money="+money
										            +"&begin="+beginTime
										            +"&end="+endTime;
       	accountAuditLogTable.fnDraw();
    };
    
    $('#source_order,#orderNo,#beginTime,#endTime,#bankName,#money').on('blur',function(){
    	find();
    });
    
    
    
    // 未选中列表
	$("#accountAuditLog-table").on('click', '.invoice', function(e){	
		if($(this).prop("checked") == true){
			checkedIds.push($(this).attr('id'));
		}else{		
			var tmpArr1 = [];
			for(id in checkedIds){
				if(checkedIds[id] != $(this).attr('id')){
					tmpArr1.push(checkedIds[id]);
				}
			}
			checkedIds = tmpArr1;
		}
		$("#checkedId").val(checkedIds);
	});

    
    
} );