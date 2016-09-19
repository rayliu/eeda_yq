define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) { 
$(document).ready(function() {
	var tableName = 'eeda-table';
	
	itemOrder.buildItemDetail=function(){
        var item_table_rows = $("#"+tableName+" tr");
        var items_array=[];
        for(var index=0; index<item_table_rows.length; index++){
            if(index==0)
                continue;

            var row = item_table_rows[index];
            var empty = $(row).find('.dataTables_empty').text();
            if(empty)
            	continue;
            
            var id = $(row).attr('id');
            if(!id){
                id='';
            }
            var item={}
            item.id = id;
            for(var i = 1; i < row.childNodes.length; i++){
            	var name = $(row.childNodes[i]).find('input').attr('name');
            	var value = $(row.childNodes[i]).find('input').val();
            	if(name){
            		item[name] = value;
            	}
            }
            item.action = $('#order_id').val() != ''?'UPDATE':'CREATE';
            items_array.push(item);
        }

        return items_array;
    };
    
    //------------事件处理
        var itemTable = eeda.dt({
            id: 'eeda-table',
            columns:[
            { "data": "ID","visible":false},
            { "data": "ORDER_NO"},
            { "data": "CREATE_STAMP"},
            { "data": "SP_NAME"},
            { "data": "RMB"},
            { "data": "USD"},
            { "data": "FND"},
            { "data": "VGM"},
            { "data": "CONTAINER_AMOUNT"},
            { "data": "GROSS_WEIGHT"},
            { "data": "CONTAINER_NO"},
            { "data": "REF_NO"},
            { "data": "MBL_NO"},
            { "data": "HBL_NO"},
            { "data": "TRUCK_TYPE"}
        ]
    });

    
    //刷新明细表
    itemOrder.refleshTable = function(order_id){
    	var url = "/chargeCheckOrder/tableList?order_id="+order_id
        +"&table_type=item";
    	itemTable.ajax.url(url).load();
    }
} );    
} );