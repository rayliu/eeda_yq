define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
	$(document).ready(function() {
    	document.title = '产品列表 | '+document.title;

    	$("#breadcrumb_li").text('产品列表');

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/wmsproduct/list",
            columns:[
                { "width": "30px",
                    "render": function ( data, type, full, meta ) {
                      return '<button type="button" class="btn table_btn delete_btn btn-xs" disabled>'+
                        '<i class="fa fa-trash-o"></i> 删除</button>';
                    }
                },
                { "data": "ITEM_NO", 
					"render": function ( data, type, full, meta ) {
						return "<a href='/wmsproduct/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
					}
                }, 
                { "data": "ITEM_NAME" },
                { "data": "PART_NO" },
                { "data": "PART_NAME" },
				{ "data": "UNIT"}, 
				{ "data": "AMOUNT"}, 
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
	          		if(!value)
	          			value = null;
	          		item[name] = value;
	          	}
	      	}
	        return item;
        };
      
        var searchData=function(){
        	var itemJson = buildCondition();
        	var url = "/wmsproduct/list?jsonStr="+JSON.stringify(itemJson);
        	dataTable.ajax.url(url).load();
        };
        
        
	});
});