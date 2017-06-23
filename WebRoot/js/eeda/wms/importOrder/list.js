define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','app/wms/importOrder/upload', 'jq_blockui'], function ($, metisMenu) {
	$(document).ready(function() {
    	document.title = '导入数据 | '+document.title;

    	$("#breadcrumb_li").text('导入数据');

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/importOrder/list",
            "drawCallback": function( settings ) {
		        $.unblockUI();
		    },
            columns:[
                { "data": "DOC_NAME"}, 
                { "data": "USER_NAME"},  
                { "data": "COMPLETE_TIME"},
                { "data": "IMPORT_TIME"}
            ]
        });
        
        $("#stopBtn").click(function(){
        	$.post('/importOrder/stopImport',function(data){
        		if(!data){
        			$.scojs_message('已停', $.scojs_message.TYPE_OK);
        		}
        	}
        });
        
        
        order.refleshTable = function(){
        	dataTable.ajax.url("/importOrder/list").load();
        }
	});
});