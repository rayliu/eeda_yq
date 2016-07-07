$(document).ready(function () {
    document.title = '管理看板 | ' + document.title;

    console.log("当前选择：" + $("input[name='optionsRadiosInline'][checked]").val());

    var todoListTable = $('#todo_list_table').dataTable({
		"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 10,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"bServerSide": true,
    	"bLengthChange":false,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	"sAjaxSource": "/getTodoList",
        "aoColumns": [
            { "mDataProp": "ORDER_NO", "sWidth":"100px",
            	"fnRender":function(obj){
                    if('PS' == obj.aData.TYPE){
                		return  "<a href='/delivery/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
                    }else{
                        return  "<a href='/transferOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
                    }
            	}},
            {"mDataProp":null, "sWidth":"100px",
                "fnRender": function(obj) {
                    return obj.aData.FROM_NAME  + " - "+obj.aData.TO_NAME;
                }
            },
            {"mDataProp":null,
            	"fnRender": function(obj) {
                    if('PS' == obj.aData.TYPE){
                    	var date = '';
                    	if(obj.aData.BUSINESS_STAMP!=null){
                    		var days = (new Date()- new Date(obj.aData.BUSINESS_STAMP.replace(/-/g, "\/")))/(1000 * 60 * 60 * 24);
                    		if(days < 2){
                    			date = "<span style = 'color:red'>"+obj.aData.BUSINESS_STAMP+"</span>";
                    		}else
                    			date = obj.aData.BUSINESS_STAMP;
                    	}
                        return " 该配送单业务要求配送时间为：" + date  + ", 请安排配送。序列号："+obj.aData.SERIAL_NO;
                    }else{
                        return " 该运输单为运输在途，请安排收货。";
                    }
            	}
            }
        ]
    });
	
	var findAllCount = function(pointInTime){
		//查询系统单据数量合计
		$.get('/statusReport/searchOrderCount', {pointInTime:pointInTime}, function(data){
			$("#transferOrderTotal").empty().text(data.transferOrderTotal);
			$("#pickupTotal").empty().text(data.pickupTotal);
			$("#departTotal").empty().text(data.departTotal);
			$("#deliveryTotal").empty().text(data.deliveryTotal);
			$("#returnTotal").empty().text(data.returnTotal);
			$("#insuranceTotal").empty().text(data.insuranceTotal);
		});
	};
	findAllCount($("input[name='optionsRadiosInline'][checked]").val());
	//切换查询
	$("input[name='optionsRadiosInline']").change(function() {
		findAllCount($(this).val());
		
		// deliveryOrderTypeTbody.fnSettings().sAjaxSource = "/delivery/findDeliveryOrderType?pointInTime="+$(this).val();
		// deliveryOrderTypeTbody.fnDraw();
	}); 
	
	
	
});