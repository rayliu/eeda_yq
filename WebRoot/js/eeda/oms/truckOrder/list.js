define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco' ], function ($, metisMenu) {
$(document).ready(function() {
	document.title = '派车单查询   | '+document.title;
    $('#menu_truck').addClass('active').find('ul').addClass('in');
    $('#menu_todo_list').removeClass('active').find('ul').removeClass('in');
    
    //datatable, 动态处理
	var dataTable = eeda.dt({
	    id: 'truck_table',
        paging: true,
	    serverSide: true, //不打开会出现排序不对
	    ajax: "/truckOrder/list",
	    columns:[
	            { "data": "ORDER_NO","width":"180px",
	                "render": function ( data, type, full, meta ) {
	                    return "<a href='/truckOrder/edit?id="+full.ID+"&order_no="+full.ORDER_NO+"&order_id="+full.ORDER_ID+" 'target='_blank'>"+data+"</a>";
	                }
	            },
	            { "data": "CREATE_STAMP","width":"180px"}, 
	            { "data": "STATUS","width":"180px"}, 
	            { "data": "UNLOAD_TYPE","width":"180px"}, 
	            { "data": "TRANSPORT_COMPANY","width":"180px"}, 
	            { "data": "DRIVER","width":"180px"}, 
	            { "data": "DRIVER_TEL","width":"180px"}, 
	            { "data": "TRUCK_TYPE","width":"180px"},
	            { "data": "CAR_NO","width":"180px"},
	            { "data": "CONSIGNOR","width":"180px"},
	            { "data": "CONSIGNOR_PHONE","width":"180px"},
	            { "data": "TAKE_ADDRESS","width":"180px"},
	            { "data": "CONSIGNEE","width":"180px"},
	            { "data": "CONSIGNEE_PHONE","width":"180px"},
	            { "data": "DELIVERY_ADDRESS","width":"180px"},
	            { "data": "ETA","width":"180px"},
	            { "data": "CARGO_INFO","width":"180px"},
	            { "data": "REQUIRED_TIME_REMARK","width":"180px"},
	            { "data": "SIGN_DESC","width":"180px"},
	            { "data": "SIGN_STATUS","width":"180px"}
	        ]
	    });

    
    $('#resetBtn').click(function(e){
        $("#orderForm")[0].reset();
    });

    $('#searchBtn').click(function(){
        searchData(); 
    })

   var searchData=function(){
        var order_no = $.trim($("#order_no").val()); 
        var status = $("#status").val(); 
        var start_date = $("#create_stamp_begin_time").val();
        var end_date = $("#create_stamp_end_time").val();
        
        /*  
            查询规则：参数对应DB字段名
            *_no like
            *_id =
            *_status =
            时间字段需成双定义  *_begin_time *_end_time   between
        */
        var url = "/truckOrder/list?order_no="+order_no
        	 		+"&status="+status
        	 		+"&create_stamp_begin_time="+start_date
        	 		+"&create_stamp_end_time="+end_date;

        dataTable.ajax.url(url).load();
    };
});

});