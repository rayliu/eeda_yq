define(['jquery', 'metisMenu', 'sb_admin','dataTables',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	  //datatable, 动态处理
	var itemTable = eeda.dt({
        id: 'item_table',
        paging: true,
        serverSide: false, //不打开会出现排序不对
        scrollX:true,
        //ajax: "/inventory/list",
        columns:[
                 {"data": "ITEM_NAME", 
               	    "render": function ( data, type, full, meta ) {
               		    return data;
               	    }
                 },
                 { "data": "ITEM_NO"}, 
 				{ "data": "PART_NAME"}, 
 				{ "data": "PART_NO"}, 
 				{ "data": "SHELVES"},
 				{ "data": "QUANTITY"}
        ]
    });
	
	$("#order_table").on('click', '.item_detail', function(e){
      	var order_id = $(this).attr("order_id");
      	searchData(order_id);
    });
	
	
	var searchData=function(order_id){
     	var url = "/gateOutOrder/orderItemList?order_id="+order_id;
     	itemTable.ajax.url(url).load();
    };

});
});