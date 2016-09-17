define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'datetimepicker_CN'], function ($, metisMenu) {
    $(document).ready(function() {
    	document.title = '出纳日记账查询 | '+document.title;

        $('#menu_finance').addClass('active').find('ul').addClass('in');
        
        $("#beginTime_filter").val(new Date().getFullYear()+'-'+ (new Date().getMonth()+1));
        
    	//datatable, 动态处理
        var checkedIds = [];
        var accountAuditLogTable = eeda.dt({
            id: 'accountAuditLog-table',
            paging: true,
            serverSide: true, 
            ajax: "/accountAuditLog/list?beginTime="+$("#beginTime_filter").val(),
            columns:[
                {   "width":"70px",
                    "render": function(data, type, full, meta) {
                        var str = "";
                        if(checkedIds.length>0){
                            for(id in checkedIds){
                                if(checkedIds[id] == full.ID){
                                    return str =  '<input type="checkbox" id="'+full.ID+'" class="invoice" order_no="'+full.ORDER_NO+'" checked = "checked">';
                               }else{
                                    str =  '<input type="checkbox" id="'+full.ID+'" class="invoice" order_no="'+full.ORDER_NO+'">';
                                }
                            }
                        }else{
                           str = '<input type="checkbox" id="'+full.ID+'" class="invoice" order_no="'+full.ORDER_NO+'">';
                        }
                        return str;
                    }
                },
                {
                    "data":"ORDER_NO","width":"70px",
                    "render": function(data, type, full, meta) {
                        if(full.SOURCE_ORDER=='应付开票申请单' || full.SOURCE_ORDER=='应收开票申请单' || full.SOURCE_ORDER=='转账单'){
                            return eeda.getUrlByNo(full.INVOICE_ORDER_ID, full.ORDER_NO);
                        }else{
                            return full.ORDER_NO;
                        }
                    }
                },
                {"data":"SOURCE_ORDER"},
                {"data":"CHARGE_AMOUNT",
                    "render": function(data, type, full, meta) {
                        if(data!=null)
                            return "<p align='right'>"+eeda.numFormat(parseFloat(data).toFixed(2),3)+"</p>";
                        else
                            return data;
                    }
                },
                {"data":"PAYEE_NAME_IN"},
                {"data":"COST_AMOUNT",
                    "render": function(data, type, full, meta) {
                        if(data!=null)
                            return "<p align='right'>"+eeda.numFormat(parseFloat(data).toFixed(2),3)+"</p>";
                        else
                            return data;
                    }
                },
                {"data":"PAYEE_NAME_OUT"},
                {"data":"BANK_NAME"},
                {"data":"CREATE_DATE"},
                {"data":"USER_NAME"}
            ]
        });
        
        var accountTable = eeda.dt({
            id: 'account-table',
            paging: true,
            serverSide: true, 
            ajax: "/accountAuditLog/accountList?beginTime="+ $("#beginTime_filter").val(),
            columns:[
                { "data": null, "sWidth":"30px",
                  "render": function(data, type, full, meta) {
                    return '<input type="checkbox" name="order_check_box" value="'+full.ID+'">';
                  }
                },
                {"data":"BANK_NAME"},
                {"data":"DATE"},
                {"data": "INIT_AMOUNT",  //期初
                    "render": function(data, type, full, meta) {
                        return eeda.numFormat(data,3);
                    }
                },
                {"data": "TOTAL_CHARGE",  //本期收入
                    "render": function(data, type, full, meta) {
                        return eeda.numFormat(data,3);
                    }
                }, 
                {"data":"TOTAL_COST",  //本期支出
                    "render": function(data, type, full, meta) {
                        return eeda.numFormat(data,3);
                    }
                },  
                {"data":"BALANCE_AMOUNT",   //期末结余    
                    "render": function(data, type, full, meta) {
                        return eeda.numFormat(data,3);
                    }
                }
            ]
        });
      //   var accountTable = $('#account-table').dataTable({
      //   	"bFilter": false, //不需要默认的搜索框
      //   	"bSort": false, 
      //       // "bPaginate": false, //翻页功能
      //       "bInfo": false,//页脚信息
      //   	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
      //   	"bServerSide": true,
      //   	"oLanguage": {
      //   		"sUrl": "/eeda/dataTables.ch.txt"
      //   	},
      //   	"fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
    		// 	$(nRow).attr('id', aData.ID);
    		// 	$(nRow).attr('account_id', aData.ID);
    		// 	return nRow;
    		// },
      //   	"sAjaxSource": "/accountAuditLog/accountList?beginTime="+ $("#beginTime_filter").val(),
      //   	"aoColumns": [   
    	        
    	 //     ]      
      //   });
        
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

       		var accoutUrl = "/accountAuditLog/accountList?beginTime="+beginTime;
       		accountTable.ajax.url(accoutUrl).load();
       		
       		var accountAuditLogUrl = "/accountAuditLog/list?ids="+ids+"&beginTime="+beginTime;
       		accountAuditLogTable.ajax.url(accountAuditLogUrl).load();
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
       		
       		var url= "/accountAuditLog/list?ids="+ids+"&beginTime="+beginTime;
            accountAuditLogTable.ajax.url(url).load();
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
        	var url = "/accountAuditLog/list?source_order="+source_order
    										            +"&orderNo="+orderNo
    										            +"&bankName="+bankName
    										            +"&money="+money
    										            +"&begin="+beginTime
    										            +"&end="+endTime;
            accountAuditLogTable.ajax.url(url).load();
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

        
        
    });
});