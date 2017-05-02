define(['jquery', 'metisMenu', 'sb_admin','dataTables',  'dataTablesBootstrap', 'validate_cn', 'sco', 'jq_blockui'], function ($, metisMenu) {
$(document).ready(function() {
	  //datatable, 动态处理
	var itemTable = eeda.dt({
        id: 'item_table',
        paging: true,
        serverSide: false, //不打开会出现排序不对
        scrollX:true,
        //ajax: "/inventory/list",
        "drawCallback": function( settings ) {
	        $.unblockUI();
	    },
        columns:[
                 {"data": "ITEM_NO", visible: false,
               	    "render": function ( data, type, full, meta ) {
                      $('#itemNo').text(data);
               		    return data;
               	    }
                 },
                 { "data": "ITEM_NAME", visible: false,
                    "render": function ( data, type, full, meta ) {
                      $('#itemName').text(data);
                      return data;
                    }
                  }, 
                 { "data": "PART_NO"},
         				{ "data": "PART_NAME"}, 
         				{ "data": "SHELVES"},
         				{ "data": "QUANTITY"},
         				{ "data": "CREATE_TIME"},
         				{ "data": "CREATOR_NAME"}
        ]
    });
	
	$("#eeda-table").on('click', '.item_detail', function(e){
      	var part_no = $(this).attr("part_no");
      	searchData(part_no);
    });
	
	buildCondition=function(){
      	var item = {};
      	var orderForm = $('#orderForm input,select');
      	for(var i = 0; i < orderForm.length; i++){
      		var name = orderForm[i].id;
          	var value =orderForm[i].value;
          	if(name){
          		if(value)
          			value = value.trim();
          		item[name] = value;
          	}
      	}
        return item;
    };
	
	var searchData=function(part_no){
		$.blockUI({ 
            message: '<h1><img src="/images/loading.gif" style="height: 50px; margin-top: -3px;"/> LOADING...</h1>' 
        });
     	var itemJson = buildCondition();
     	var url = "/inventory/itemDetailList?part_no="+part_no+"&jsonStr="+JSON.stringify(itemJson);
     	itemTable.ajax.url(url).load();
    };

});
});