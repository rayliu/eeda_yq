define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
	$(document).ready(function() {
    	document.title = '出库记录 | '+document.title;

    	$("#breadcrumb_li").text('出库记录 ');

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/gateOut/list?error_flag=N",
            columns:[
                { "width": "30px",
                    "render": function ( data, type, full, meta ) {
                      return '<button type="button" class="btn table_btn delete_btn btn-xs" disabled>'+
                        '<i class="fa fa-trash-o"></i> 删除</button>';
                    }
                },
                {"data": "ITEM_NAME", 
              	    "render": function ( data, type, full, meta ) {
              		    return data;
              	    }
                },
                { "data": "QR_CODE"}, 
				{ "data": "PART_NAME"}, 
				{ "data": "PART_NO"}, 
				{ "data": "QUANTITY"},
				{ "data": "MOVE_FLAG"}, 
				{ "data": "CREATE_TIME"},
				{ "data": "CREATOR_NAME"}
            ]
        });
        
        var errorTable = eeda.dt({
            id: 'error-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/gateOut/list?error_flag=Y",
            columns:[
                { "width": "30px",
                    "render": function ( data, type, full, meta ) {
                      return '<button type="button" class="btn table_btn delete_btn btn-xs" disabled>'+
                        '<i class="fa fa-trash-o"></i> 删除</button>';
                    }
                },
				{ "data": "ERROR_MSG",
					"render": function ( data, type, full, meta ) {
              	  		return "<span style='color:red;'>"+data+"</span>";
              	  	}	
				}, 
                {"data": "ITEM_NAME", 
              	    "render": function ( data, type, full, meta ) {
              		    return data;
              	    }
                },
                { "data": "QR_CODE"}, 
				{ "data": "PART_NAME"}, 
				{ "data": "PART_NO"}, 
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
        	var url = "/gateOut/list?error_flag=N&jsonStr="+JSON.stringify(itemJson);
        	dataTable.ajax.url(url).load();
        };
        
        order.refleshTable = function(){
        	dataTable.ajax.url("/gateOut/list?error_flag=N").load();
        	errorTable.ajax.url("/gateOut/list?error_flag=Y").load();
        }
        
	});
});