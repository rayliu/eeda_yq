define(['jquery', 'metisMenu', 'sb_admin','dataTables',  'dataTablesBootstrap', 'validate_cn', 'sco', 'jq_blockui'], function ($, metisMenu) {
$(document).ready(function() {
	  //datatable, 动态处理
	var itemTable = eeda.dt({
        id: 'item_table',
        paging: true,
        serverSide: false, //不打开会出现排序不对
        scrollX:true,
        pageLength:100,
        //ajax: "/inventory/list",
        "drawCallback": function( settings ) {
	        $.unblockUI();
	    },
        columns:[
 				 { "data": "SHELVES","width":"100px"},
 				 { "data": "QUANTITY","width":"100px"},
 				 { "data": "QR_CODE","width":"300px"},
 				 { "data": "CREATE_TIME","width":"100px"},
 				 { "data": "CREATOR_NAME","width":"100px"}
        ]
    });
	
	$("#part-table").on('click', '.partDetail', function(e){
      	var part_no = $(this).attr("part_no");
      	var part_name = $($(this).parent().parent()).find('.part_name').text();
      	var item_nos = $($(this).parent().parent()).find('.item_nos').text();
      	var totalBox = $($(this).parent().parent()).find('.totalBox').text();
      	var totalPiece = $($(this).parent().parent()).find('.totalPiece').text();
      	$('#partNo').text(part_no);
      	$('#partName').text(part_name);
      	$('#itemNos').text(item_nos);
      	$('#totalBox').text(totalBox);
      	$('#totalPiece').text(totalPiece);
      	$('#photo').html('<img src="/images/product/'+part_no+'.jpg" width="70%" style="box-shadow:0 0 20px #4F4F4F"  border="1px solid #F00" onerror="javascript:this.src=\'/images/product/no_photo.jpg\'"/>');
      	
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