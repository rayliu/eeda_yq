$(document).ready(function() {
	document.title = '付款申请复核单| '+document.title;

    $('#menu_finance').addClass('active').find('ul').addClass('in');

	//datatable, 动态处理
    var id = $("#invoiceApplicationId").val();
    var total = 0.00;
    var nopay = 0.00;
    var pay = 0.00;
    $('#CostOrder-table').dataTable({
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/costAcceptOrder/costOrderList?id="+id,
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr({id: aData.ID}); 
		},
        "aoColumns": [   
             {"mDataProp":"TYPE","sWidth": "150px"},
             {"mDataProp":"ORDER_NO","sWidth": "150px",
            	"fnRender": function(obj) {
        			return "<a href='/costCheckOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
        		}},
    		{"mDataProp":"COST_AMOUNT","sWidth": "150px",
    			"fnRender": function(obj) {
					total = total + parseFloat(obj.aData.COST_AMOUNT) ;
					$("#total").html(total);
					return obj.aData.COST_AMOUNT;
    			}
    		},
    		{"mDataProp":"DAIFU","sWidth": "150px",
    			"fnRender": function(obj) {
					nopay = nopay + parseFloat(obj.aData.DAIFU) ;
					$("#nopay").html(nopay);
					return obj.aData.DAIFU;
    			}
    		},
    		{"mDataProp":"PAY_AMOUNT","sWidth": "150px",
    			"fnRender": function(obj) {
					pay = pay + parseFloat(obj.aData.PAY_AMOUNT) ;
					$("#pay").html(pay);
					return "<input type ='text' id = 'pay_amount' value='"+obj.aData.PAY_AMOUNT+"'>";
    			}
    		},    
        ]      
    });	
    
    
    $("#checkBtn").on('click',function(){
		$.get("/costAcceptOrder/checkStatus", {ids:id,order:$("#attribute").val()}, function(data){
			if(data.success){
				$.scojs_message('复核成功', $.scojs_message.TYPE_OK);
				$("#checkBtn").attr("disabled", true);
			}else{
				$.scojs_message('复核失败', $.scojs_message.TYPE_FALSE);
			}
		},'json');
	});
    
   
});