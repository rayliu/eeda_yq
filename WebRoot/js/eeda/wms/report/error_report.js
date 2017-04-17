define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
	$(document).ready(function() {
    	document.title = '错误报表 | '+document.title;

    	$("#breadcrumb_li").text('错误报表');

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/errorReport/list",
            columns:[
                {"data": "ORDER_TYPE"},
				{ "data": "ERROR_MSG",
					"render": function ( data, type, full, meta ) {
					  		return "<span style='color:red;'>"+data+"</span>";
					  	}	
				}, 
				{"data": "ITEM_NO", 
					  "render": function ( data, type, full, meta ) {
						  //return "<a href='/wmsproduct/edit?id="+full.PRODUCT_ID+"'target='_blank'>"+data+"</a>";
						  return data;
					  }
				},
				{"data": "ITEM_NAME"},
				{ "data": "QR_CODE"}, 
				{ "data": "PART_NO"}, 
				{ "data": "PART_NAME"}, 
				{ "data": "SHELVES"},
				{ "data": "QUANTITY"},
				{ "data": "MOVE_FLAG"}, 
				{ "data": "CREATE_TIME"},
				{ "data": "CREATOR_NAME"}
            ]
        });
      
        $('#resetBtn').click(function(e){
        	$("#orderForm")[0].reset();
        });

        $('#searchBtn').click(function(){
        	searchData(); 
        })
 
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
      
        var searchData=function(){
        	var itemJson = buildCondition();
        	var url = "/errorReport/list?jsonStr="+JSON.stringify(itemJson);
        	dataTable.ajax.url(url).load();
        };
        
        
	});
});