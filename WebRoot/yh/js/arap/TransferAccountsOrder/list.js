$(document).ready(function() {
	 document.title = '转账单查询 | '+document.title;
	 $('#menu_finance').addClass('active').find('ul').addClass('in');
    
    //datatable, 动态处理
    var expenseAccountTbody = $('#expenseAccountTbody').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "bSort": false, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bRetrieve": true,
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/transferAccountsOrder/list",
        "aoColumns": [ 
			{"mDataProp":null,"sWidth":"120px",
				"fnRender": function(obj) {
					return "<a href='/transferAccountsOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
				}
			},
            {"mDataProp":"TRANSFER_STATUS", "sWidth":"200px"},
            {"mDataProp":"TRANSFER_STAMP", "sWidth":"200px"}, 
            {"mDataProp":null, "sWidth": "120px", 
	              "fnRender": function(obj) {
	                  if(obj.aData.TRANSFER_METHOD=='in'){
	                      return '存款';
	                  }else if(obj.aData.TRANSFER_METHOD=='out'){
	                      return '取款';
	                  }else if(obj.aData.TRANSFER_METHOD=='transfer'){
	                	  return '转账';
	                  }
	                  return obj.aData.TRANSFER_METHOD;
	              }
	          },
            {"mDataProp":"ACCOUNT_IN", "sWidth":"100px"},                        
            {"mDataProp":"ACCOUNT_OUT", "sWidth":"100px"},
            {"mDataProp":"AMOUNT", "sWidth":"150px"},
            {"mDataProp":"C_NAME", "sWidth":"100px"},
            {"mDataProp":"REMARK", "sWidth":"150px"}                         
        ]      
    });	
    var refreshData=function(){
        var orderNo=$("#orderNo").val();
        var transfer_method=$("#transfer_method").val();
        expenseAccountTbody.fnSettings().sAjaxSource = "/transferAccountsOrder/list?orderNo="+orderNo +"&transfer_method="+transfer_method;
        expenseAccountTbody.fnDraw(); 
    };
    $("#orderNo").on('keyup click', function () {  
    	refreshData();
    }); 	 	
    $('#transfer_method').on( 'change', function () {
    	refreshData();
    });
});