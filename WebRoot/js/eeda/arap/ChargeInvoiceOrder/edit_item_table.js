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
    var itemTable = $("#"+tableName).DataTable({
        "processing": true,
        "searching": false,
        "paging": false,
        "autoWidth": true,
        "language": {
            "url": "/js/lib/datatables/i18n/Chinese.json"
        },
        "createdRow": function ( row, data, index ) {
            $(row).attr('id', data.ID);
        },
        "columns": [
	            { "data": "ID","visible":false},
	            { "data": "INVOICE_NO","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                     if(!data)
	                         data='';
	                     return '<input type="text" name="invoice_no" value="'+data+'" class="form-control search-control" style="width:200px"/>';
	                }
	            },
	            { "data": "ORDER_NO"},
	            { "data": "PAYEE_NAME"},
	            { "data": "STATUS"},
	            { "data": "CREATE_STAMP"}
	        ]
	    });

    
    //刷新明细表
    itemOrder.refleshTable = function(order_id){
        var url = "/chargeInvoiceOrder/tableList?order_id="+order_id;
        itemTable.ajax.url(url).load();
    }
    
    
});    
});