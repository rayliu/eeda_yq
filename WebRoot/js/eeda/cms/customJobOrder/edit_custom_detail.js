define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	
	itemOrder.buildCustomDetail=function(){
		var arrays = [];
    	var item = {};
    	item['id'] = $('#custom_id').val();
    	var form = $('#customDetailExportForm input,#customDetailExportForm select,#customDetailExportForm textarea');
    	for(var i = 0; i < form.length; i++){
    		var name = form[i].id;
        	var value =form[i].value;
        	if(name){
    			item[name] = value;
        	}
    	}
    	arrays.push(item);
        return arrays;
    };
    
    var dataTable = eeda.dt({
        id: 'custom_table',
        paging: false,
        serverSide: true, //不打开会出现排序不对
        ajax: "/customJobOrder/tableList?type=custom&order_id=1",
        columns: [
            { "width": "30px",
                "render": function ( data, type, full, meta ) {
                    return '<button type="button" class="delete btn btn-default btn-xs" style="width:50px">删除</button>';
                }
            },
            { "data": "ORDER_NO", 
                "render": function ( data, type, full, meta ) {
                    return "<a href='/customJobOrder/editCustomOrder?id="+full.ID+"'target='_blank'>"+data+"</a>";
                }
            },
            { "data": "TYPE", 
                "render": function ( data, type, full, meta ) {
                    if(data=='import'){
                        return '进口'
                    }
                    return "出口";
                }}, 
            { "data": "PORT"}, 
            { "data": "EXPORT_DATE"}, 
            { "data": "APPLY_DATE"}
          ]
  });
        
});
});