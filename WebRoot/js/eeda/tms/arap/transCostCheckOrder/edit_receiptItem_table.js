define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) { 
$(document).ready(function() {
	var tableName = 'receip-table';
	
	itemOrder.buildReceipItemDetail=function(){
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
    var ids = [];
    var itemIds=[];
    var cnames = [];
    //------------事件处理
    var reciveitemTable = eeda.dt({
        id: 'receip-table',
        columns:[
        { "data": "CURRENCY_NAME", "width": "100px"},
        { "data": "TOTAL_AMOUNT", "width": "180px"},
        { "data": "RECEIVE_CNY", "width": "120px"},
        { "data": "RESIDUAL_CNY", "width": "200px"},
        { "data": "RECEIVE_TIME", "width": "80px"},
        { "data": "RECEIVE_NAME", "width": "80px"}
        ]
    }); 
    
    //刷新明细表
    itemOrder.refleshReciveTable = function(order_id){
    	var url = "/transCostCheckOrder/tableList?order_id="+order_id
        +"&table_type=receive";
    	reciveitemTable.ajax.url(url).load();
    }
    
   
} );    
} );