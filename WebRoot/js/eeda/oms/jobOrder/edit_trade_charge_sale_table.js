define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
$(document).ready(function() {

	var deletedTableIds=[];
    //删除一行
    $("#trade_sale_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))
        
        cargoTable.row(tr).remove().draw();
    }); 
    
    itemOrder.buildTradeSaleItem=function(){
        var cargo_table_rows = $("#trade_sale_table tr");
        var cargo_items_array=[];
        for(var index=2; index<cargo_table_rows.length; index++){

            var row = cargo_table_rows[index];
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
            	var el = $(row.childNodes[i]).find('input, select');
            	var name = el.attr('name'); //name='abc'
            	
            	if(el && name){
                	var value = el.val();//元素的值
                	item[name] = value;
            	}
            }
            item.action = id.length > 0?'UPDATE':'CREATE';
            cargo_items_array.push(item);
        }

        //add deleted items
        for(var index=0; index<deletedTableIds.length; index++){
            var id = deletedTableIds[index];
            var item={
                id: id,
                action: 'DELETE'
            };
            cargo_items_array.push(item);
        }
        deletedTableIds = [];
        return cargo_items_array;
    };

    //------------事件处理
    var bindFieldEvent=function(){	
    	eeda.bindTableField('trade_sale_table','CHARGE_ID','/finItem/search','');
	    eeda.bindTableField('trade_sale_table','SP','/serviceProvider/searchCompany','');
	    eeda.bindTableFieldCurrencyId('trade_sale_table','CURRENCY','/serviceProvider/searchCurrency','');
    };
    var cargoTable = eeda.dt({
	    id: 'trade_sale_table',
	    autoWidth: false,
	    drawCallback: function( settings ) {
            bindFieldEvent();
            $.unblockUI();
        },
        columns:[
     			{ "width": "30px",
     			    "render": function ( data, type, full, meta ) {
     			    		return '<button type="button" class="delete btn btn-default btn-xs" style="width:50px">删除</button> ';
     			    }
     			},
     			{ "data": "SP", "width": "180px",
     				"render": function ( data, type, full, meta ) {
                     		if(!data)
                                 data='';
                             var field_html = template('table_dropdown_template',
                                 {
                                     id: 'SP',
                                     value: data,
                                     display_value: full.SP_NAME,
                                     style:'width:200px'
                                 }
                             );
                             return field_html;
     				}
     			},
     			{ "data": "CHARGE_ID","width": "180px",
                     "render": function ( data, type, full, meta ) {
                         if(!data)
                             data='';
                         var field_html = template('table_dropdown_template',
                             {
                                 id: 'CHARGE_ID',
                                 value: data,
                                 display_value: full.CHARGE_NAME,
                                 style:'width:200px'
                             }
                         );
                         return field_html;
                   }
                 },
                 { "data": "FEE_AMOUNT", "width": "180px","className":"currency_total_amount",
                     "render": function ( data, type, full, meta ) {
                         if(!data)
                             data='';
                         return '<input type="text" name="fee_amount" value="'+data+'" class="form-control" style="width:200px"/>';
                     }
                 },
     			{ "data": "CURRENCY", "width": "60px",
                 	"render": function ( data, type, full, meta ) {
     	                	if(!data)
     	                        data='';
     	                    var field_html = template('table_dropdown_template',
     	                        {
     	                            id: 'CURRENCY',
     	                            value: data,
     	                            display_value: full.CURRENCY_NAME,
     	                            style:'width:80px'
     	                        }
     	                    );
     	                    return field_html;
                 	}
                 },
     			{ "data": "RATE", "width": "180px",
     	        	"render": function ( data, type, full, meta ) {
     	        		if(!data)
     	        			data='';
     	        		return '<input type="text" name="rate" value="'+data+'" class="form-control" style="width:200px"/>';
     	        	}
                 },
                 { "data": "FEE_AMOUNT_CNY", "width": "180px",
                     "render": function ( data, type, full, meta ) {
                         if(!data)
                             data='';
                         return '<input type="text" name="fee_amount_cny" value="'+data+'" class="form-control" style="width:200px"/>';
                     }
                 },
                 { "data": "CHARGE_NAME", "visible": false,
                     "render": function ( data, type, full, meta ) {
                         if(!data)
                             data='';
                         return data;
                     }
                 },
                 { "data": "CURRENCY_NAME", "visible": false,
                     "render": function ( data, type, full, meta ) {
                         if(!data)
                             data='';
                         return data;
                     }
                 },
                 { "data": "SP_NAME", "visible": false,
                     "render": function ( data, type, full, meta ) {
                         if(!data)
                             data='';
                         return data;
                     }
                 }
             ]
    });

    $('#add_trade_sale_table').on('click', function(){
        var item={};
        cargoTable.row.add(item).draw(true);
    });
    
    //刷新明细表
    itemOrder.refleshTradeSaleItemTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=trade_sale";
    	cargoTable.ajax.url(url).load();
    }
    
    if($('#trade_sale_table td').length>1){
    	var col = [3,6];
    	for (var i=0;i<col.length;i++){
	    	var arr = cargoTable.column(col[i]).data();
    		$('#trade_sale_table tfoot').find('th').eq(col[i]).html(
	    		arr.reduce(function (a, b) {
		    		a = parseFloat(a);
		    		if(isNaN(a)){ a = 0; }                   
		    		b = parseFloat(b);
		    		if(isNaN(b)){ b = 0; }
		    		return (a + b).toFixed(3);
		    	})
	    	);
    	}
    }
    
    $('#trade_sale_table').on('keyup', '[name=fee_amount],[name=fee_amount_cny],[name=rate]', function(){
    	var name = $(this).attr('name');
    	var row = $(this).parent().parent();
    	var fee_amount_cny = $(row.find('[name=fee_amount_cny]')).val();
    	var fee_amount = $(row.find('[name=fee_amount]')).val();
    	var rate = $(row.find('[name=rate]')).val();
    	
    	if(name=='fee_amount_cny'){
        	if(fee_amount_cny==''||rate==''){
        		$(row.find('[name=fee_amount]')).val('');
        	}else if(!isNaN(fee_amount_cny)&&!isNaN(rate)){
        		$(row.find('[name=fee_amount]')).val((fee_amount_cny/rate).toFixed(3));
        	}
    	}
    	if(name=='rate'){
    		if(fee_amount!=''&&!isNaN(fee_amount)){
	    		$(row.find('[name=fee_amount_cny]')).val((fee_amount*rate).toFixed(3));
	    	}else if(fee_amount_cny!=''&&!isNaN(fee_amount_cny)){
	    		$(row.find('[name=fee_amount]')).val((fee_amount_cny/rate).toFixed(3));
	    	}
    	}
    	if(name=='fee_amount'){
	    	if(fee_amount==''||rate==''){
	    		$(row.find('[name=fee_amount_cny]')).val('');
	    	}else if(!isNaN(fee_amount)&&!isNaN(rate)){
	    		$(row.find('[name=fee_amount_cny]')).val((fee_amount*rate).toFixed(3));
	    	}
    	}
    	
    	var total_fee_amount_cny = 0;
		$('#trade_sale_table [name=fee_amount_cny]').each(function(){
			var a = this.value;
			if(a!=''&&!isNaN(a)){
				total_fee_amount_cny+=parseFloat(a);
			}
		})
		$($('.dataTables_scrollFoot tr')[2]).find('th').eq(6).html(total_fee_amount_cny.toFixed(3));
		
    	var total = 0;
		$('#trade_sale_table [name=fee_amount]').each(function(){
			var a = this.value;
			if(a!=''&&!isNaN(a)){
				total+=parseFloat(a);
			}
		})
		$($('.dataTables_scrollFoot tr')[2]).find('th').eq(3).html(total.toFixed(3));
    })

});
});