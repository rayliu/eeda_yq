define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	document.title = '工作单查询   | '+document.title;

	  //datatable, 动态处理
    var dataTable = $('#eeda-table').DataTable({
        "processing": true,
        "searching": false,
        //"serverSide": false,
        "scrollX": true,
        //"scrollY": "300px",
        "scrollCollapse": true,
        "autoWidth": false,
        "language": {
            "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
        },
        "ajax": "/truckOrder/list",
        "columns": [
            { "data": "ORDER_NO", 
                "render": function ( data, type, full, meta ) {
                    return "<a href='/truckOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
                }
            },
            { "data": "TYPE"}, 
            { "data": "CUSTOMER_NAME"}, 
            { "data": "SP_NAME"}, 
            { "data": "CREATOR_NAME"}, 
            { "data": "CREATE_STAMP"},
            { "data": "STATUS"}
        ]
    });

    
    $('#resetBtn').click(function(e){
        $("#orderForm")[0].reset();
    });

    $('#searchBtn').click(function(){
        searchData(); 
    })

   var searchData=function(){
        var order_no = $("#order_no").val(); 
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