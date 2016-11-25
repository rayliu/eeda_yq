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
            {
            	"render": function ( data, type, full, meta ) {
            		return '<input type="checkbox" style="width:30px">';
			    }
            },
            { "data": "ORDER_NO"},
            { "data": "TYPE"},
            { "data": "CREATE_STAMP"},
            { "data": "CUSTOMER_NAME"},
            { "data": "SP_NAME"},
            { "data": "CURRENCY_NAME","class":"currency_name"},
            { "data": "TOTAL_AMOUNT","class":"total_amount"},
            { "data": "EXCHANGE_RATE"},
            { "data": "AFTER_TOTAL"},
            { "data": "NEW_RATE","class":"new_rate"},
            { "data": "AFTER_RATE_TOTAL","class":"after_rate_total"},
            { "data": "EXCHANGE_CURRENCY_NAME"},
            { "data": "EXCHANGE_CURRENCY_RATE"},
            { "data": "EXCHANGE_TOTAL_AMOUNT"}
        ]
    });
        
        
        //选择是否是同一币种
        var cnames = [];
		$('#eeda-table').on('click',"input[type=checkbox]",function () {
				var cname = $(this).parent().siblings('.currency_name')[0].textContent;
				if($(this).prop('checked')==true){	
					if(cnames.length > 0 ){
						if(cnames[0]==cname){
							cnames.push(cname);
						}else{
							$.scojs_message('请选择同一币种进行兑换', $.scojs_message.TYPE_ERROR);
							$(this).attr('checked',false);
							return false;
						}
					}else{
						cnames.push(cname);
					}
				}else{
					cnames.pop(cname);
			 }
		});
    
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
    	$('#cost_amount').val(totalAmount.toFixed(2));
    	
    })

    
    //刷新明细表
    itemOrder.refleshTable = function(order_id){
    	var url = "/costCheckOrder/tableList?order_id="+order_id
        +"&table_type=item";
    	itemTable.ajax.url(url).load();
    }
} );    
} );