define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	
	itemOrder.buildCustomDetail=function(){
		var arrays = [];
    	var item = {};
    	item['id'] = $('#custom_id').val();
    	item['type'] = $('#customDetailExportForm input[name="type"]:checked').val();
    	var form = $('#customDetailExportForm input,#customDetailExportForm select,#customDetailExportForm textarea');
    	for(var i = 0; i < form.length; i++){
    		var name = form[i].id;
        	var value =form[i].value;
        	if(name){
        		if(name.indexOf("custom_")==0){
        			var rName = name.replace("custom_","");
        			item[rName] = value;
        		}else{
        			item[name] = value;
        		}
        	}
    	}
    	arrays.push(item);
        return arrays;
    };
    
    var dataTable = eeda.dt({
        id: 'custom_table',
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
	  //刷新明细表
	itemOrder.refleshCustomTable = function(order_id){
		var url = "/customJobOrder/tableList?order_id="+order_id+"&type=custom";
		dataTable.ajax.url(url).load();
	}
        
});
});