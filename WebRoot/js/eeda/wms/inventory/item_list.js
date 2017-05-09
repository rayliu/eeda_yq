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
               		    return data;
               	    }
                 },
                 { "data": "ITEM_NAME", visible: false,
                    "render": function ( data, type, full, meta ) {
                      return data;
                    }
                 }, 
                 { "data": "PART_NO", visible: false,
                      "render": function ( data, type, full, meta ) {
                          //$('#partNo').text(data);
                          return data;
                       }
                 }, 
 				 { "data": "PART_NAME", visible: false,
	                  "render": function ( data, type, full, meta ) {
	                      //$('#partName').text(data);
	                      return data;
	                  }
                 }, 
 				 { "data": "SHELVES"},
 				 { "data": "QUANTITY"},
 				 { "data": "CREATE_TIME"},
 				 { "data": "CREATOR_NAME"}
        ]
    });
	
	$("#eeda-table").on('click', '.partDetail', function(e){
      	var part_no = $(this).attr("part_no");
      	var part_name = $($(this).parent().parent()).find('.part_name').text();
      	var totalBox = $($(this).parent().parent()).find('.totalBox').text();
      	var totalPiece = $($(this).parent().parent()).find('.totalPiece').text();
      	$('#partNo').text(part_no);
      	$('#partName').text(part_name);
      	$('#totalBox').text(totalBox);
      	$('#totalPiece').text(totalPiece);
      	$('#photo').html('<img src="/images/product/'+part_no+'.png" height="70%" width="70%" border="1px solid #F00" onerror="javascript:this.src=\'/images/product/no_photo.png\'"/>');
      	
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