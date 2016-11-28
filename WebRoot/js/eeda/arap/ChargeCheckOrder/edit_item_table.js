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
            { "data": "CURRENCY_NAME",'class':'currency_name'},
            { "data": "TOTAL_AMOUNT",'class':'total_amount',
            	"render": function ( data, type, full, meta ) {
            		if(full.ORDER_TYPE=='cost'){
	            		return '<span style="color:red;">'+'-'+data+'</span>';
	            	}
                    return data;
                  }
            },
            { "data": "EXCHANGE_RATE"},
            { "data": "AFTER_TOTAL",
            	"render": function ( data, type, full, meta ) {
            		if(full.ORDER_TYPE=='cost'){
	            		return '<span style="color:red;">'+'-'+data+'</span>';
	            	}
                    return data;
                  }
            },
            { "data": "NEW_RATE",'class':'new_rate'},
            { "data": "AFTER_RATE_TOTAL",'class':'after_rate_total',
            	"render": function ( data, type, full, meta ) {
            		if(full.ORDER_TYPE=='cost'){
	            		return '<span style="color:red;">'+'-'+data+'</span>';
	            	}
                    return data;
                  }
            },
            { "data": "FND"},
            { "data": "VGM"},
            { "data": "CONTAINER_AMOUNT"},
            { "data": "GROSS_WEIGHT"},
            { "data": "CONTAINER_NO"},
            { "data": "REF_NO"},
            { "data": "MBL_NO"},
            { "data": "HBL_NO"},
            { "data": "ORDER_TYPE", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            },
        ]
    });

    
    //刷新明细表
    itemOrder.refleshTable = function(order_id){
    	var url = "/chargeCheckOrder/tableList?order_id="+order_id
        +"&table_type=item";
    	itemTable.ajax.url(url).load();
    }
    
    
    $('input[name=new_rate]').on('keyup',function(){
    	var totalAmount = 0.00;
    	var row = $(this).parent().parent();
    	var rate = row.find('[name=rate]').val();
    	var new_rate = row.find('[name=new_rate]').val();
    	var currency_name = row.find('[name=new_rate]').attr('currency_name');
    	var row = $('#eeda-table tr');
    	
    	if(new_rate == ''){
    		new_rate = rate;
    	}
    	for(var i = 1;i<row.length;i++){
    		var row_currency_name = $(row[i]).find('.currency_name').text();
    		var total_amount = $(row[i]).find('.total_amount').text();
    		if(currency_name == row_currency_name){
    			$(row[i]).find('.new_rate').text(new_rate);
    			$(row[i]).find('.after_rate_total').text(parseFloat(total_amount*new_rate).toFixed(2));
    		}
    		
    		var total_amount = $(row[i]).find('.after_rate_total').text();
    		totalAmount += parseFloat(total_amount);
    	}
    	$('#check_amount').val(totalAmount.toFixed(2));
    })
} );    
} );