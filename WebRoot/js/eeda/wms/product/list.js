define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn','./item_list', 'sco', 'jq_blockui'], function ($, metisMenu) {
	$(document).ready(function() {
    	document.title = '产品BOM列表 | '+document.title;

    	$("#breadcrumb_li").text('产品BOM列表');

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/wmsproduct/list",
            "drawCallback": function( settings ) {
		        $.unblockUI();
		    },
            columns:[
                
                { "data": "ITEM_NO","class":"item_no", 
              	    "render": function ( data, type, full, meta ) {
              	    		if(!data)
                   	    		data = "<i class='glyphicon glyphicon-th-list'></i>";
                   	    	return "<a class='partDetail' item_no='"+data+"' data-target='#partDetail' data-toggle='modal' style='cursor: pointer;'>"+data+"</a>";
                        
              	    }
                }, 
                { "data": "ITEM_NAME","class":"item_name" },
				{ "data": "PARTAMOUNT","class":"part_amount" }
            ]
        });
        

      
        $('#resetBtn').click(function(e){
        	$("#orderForm")[0].reset();
        	searchData();
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
      
        var searchData=function(type){
        	$.blockUI({ 
                message: '<h1><img src="/images/loading.gif" style="height: 50px; margin-top: -3px;"/> LOADING...</h1>' 
            });
        	
        	var itemJson = buildCondition();
        	url = "/wmsproduct/list?jsonStr="+JSON.stringify(itemJson);
        	
        	dataTable.ajax.url(url).load();
        };
	});
});