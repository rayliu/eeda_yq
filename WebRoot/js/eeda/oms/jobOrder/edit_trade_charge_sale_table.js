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
            	var els = $(row.childNodes[i]).find('input, select');
            	$.each(els, function(index, inputEl) {
                    var el = $(inputEl);
                    var name = el.attr('name'); //name='abc'
                
                    if(el && name){
                        var value = el.val();//元素的值
                        item[name] = value;
                    }
                });
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
	    eeda.bindTableField('trade_sale_table','SP_ID','/serviceProvider/searchCompany','');
	    eeda.bindTableFieldCurrencyId('trade_sale_table','CURRENCY_ID','/serviceProvider/searchCurrency','');
	    eeda.bindTableFieldCurrencyId('trade_sale_table','exchange_currency_id','/serviceProvider/searchCurrency','');
	    eeda.bindTableField('trade_sale_table','UNIT_ID','/serviceProvider/searchChargeUnit','');
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
     			    	return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:50px"><i class="fa fa-trash-o"></i> 删除</button></button> ';
     			    }
     			},
     			{ "data": "SP_ID", "width": "180px",
     				"render": function ( data, type, full, meta ) {
                     		if(!data)
                                 data='';
                             var field_html = template('table_dropdown_template',
                                 {
                                     id: 'SP_ID',
                                     value: data,
                                     display_value: full.SP_ID_NAME,
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
                                 display_value: full.CHARGE_ID_NAME,
                                 style:'width:200px'
                             }
                         );
                         return field_html;
                     }
                 },
                 { "data": "PRICE", "width": "120px",
                     "render": function ( data, type, full, meta ) {
                     	if(data)
                             var str =  parseFloat(data).toFixed(2);
                         else
                         	str = '';
                      	 return '<input type="text" name="price" style="width:120px" value="'+str+'" class="form-control notsave" />';
                          
                    }
                 },
                 { "data": "AMOUNT","width": "60px",
                     "render": function ( data, type, full, meta ) {
                     	if(!data)
                             data='';
                        return '<input type="text" name="amount" style="width:120px" value="'+data+'" class="form-control notsave" />'; 
                     }
                 },
                 { "data": "UNIT_ID","width": "60px",
                     "render": function ( data, type, full, meta ) {
                 	   if(!data)
                            data='';
                        var field_html = template('table_dropdown_template',
                            {
                                id: 'UNIT_ID',
                                value: data,
                                display_value: full.UNIT_ID_NAME,
                                style:'width:80px'
                            }
                        );
                        return field_html;
                     }
                 },
                 { "data": "TOTAL_AMOUNT", "width": "150px","className":"currency_total_amount",
                     "render": function ( data, type, full, meta ) {
                     	if(data)
                             var str =  parseFloat(data).toFixed(2);
                         else
                         	str = '';
                     	return '<input type="text" name="total_amount" style="width:150px" value="'+str+'" class="form-control notsave"  />';
                     	
                     }
                 },
                 { "data": "CURRENCY_ID", "width":"60px","className":"currency_name",
                     "render": function ( data, type, full, meta ) {
                     	if(full.AUDIT_FLAG == 'Y'){
     	                	if(!data)
     	                        data='';
     	                    var field_html = template('table_dropdown_template',
     	                        {
     	                            id: 'CURRENCY_ID',
     	                            value: data,
     	                            display_value: full.CURRENCY_ID_NAME,
     	                            style:'width:80px',
     	                            disabled:'disabled'
     	                        }
     	                    );
     	                    return field_html;
                     }else{
                    	 if(!data){
                  		   var field_html = template('table_dropdown_template',
                                     {
                                         id: 'CURRENCY_ID',
                                         value: $('#sale_currency_id').val(),
         	                               display_value: $('#sale_currency_id_input').val(),
                                         style:'width:80px'
                                     }
                                 );
                  	   }else{
	                        var field_html = template('table_dropdown_template',
	                            {
	                                id: 'CURRENCY_ID',
	                                value: data,
	                                display_value: full.CURRENCY_ID_NAME,
	                                style:'width:80px'
	                            }
	                          );
                        }
                        return field_html; 
                     }
                   }
                 },
                 { "data": "EXCHANGE_RATE", "width": "80px",
                     "render": function ( data, type, full, meta ) {
                    	 var str = '';
	                     	if(data){
	                                str =  parseFloat(data).toFixed(6);
	                     	}else{
	                     		if($('#sale_currency_rate').val()==''){
	                     			str=0;
	             				}else{
	             					str=$('#sale_currency_rate').val();
	             				}
	                     	}
                     if(full.AUDIT_FLAG == 'Y'){
                         	return '<input type="text" name="exchange_rate" style="width:100px" value="'+str+'" class="form-control notsave" disabled />';
                     }else{
                         	return '<input type="text" name="exchange_rate" style="width:100px" value="'+str+'" class="form-control notsave" />';
                    }
                   }
                 },
                 { "data": "CURRENCY_TOTAL_AMOUNT", "width": "150px","className":"cny_total_amount",
                     "render": function ( data, type, full, meta ) {
                     	if(data)
                             var str =  parseFloat(data).toFixed(2);
                         else
                         	str = '';
     	                return '<input type="text" name="currency_total_amount" style="width:150px" value="'+str+'" class="form-control notsave" disabled />';
                   }
                 },
                 { "data": "EXCHANGE_CURRENCY_ID", "width":"60px","className":"cny_to_other",
                 	"render": function ( data, type, full, meta ) {
                 		if(full.AUDIT_FLAG == 'Y'){
                 			if(!data)
                 				data='';
                 			var field_html = template('table_dropdown_template',
                 					{
     		            				id: 'exchange_currency_id',
     		            				value: data,
     		            				display_value: full.EXCHANGE_CURRENCY_ID_NAME,
     		            				style:'width:80px',
     		            				disabled:'disabled'
                 					}
                 			);
                 			return field_html;
                 		}else{
                 			if(!data){
                				var field_html = template('table_dropdown_template',
                    					{
        		            				id: 'exchange_currency_id',
        		            				value: $('#sale_exchange_currency').val(),
        	   	                            display_value: $('#sale_exchange_currency_input').val(),
        		            				style:'width:80px'
                    					}
                    			);
                			}else{
	                 			var field_html = template('table_dropdown_template',
	                 					{
	     		            				id: 'exchange_currency_id',
	     		            				value: data,
	     		            				display_value: full.EXCHANGE_CURRENCY_ID_NAME,
	     		            				style:'width:80px'
	                 					}
                 			  );
                 			}
                 			return field_html; 
                 		}
                 	}
                 },
                 { "data": "EXCHANGE_CURRENCY_RATE", "width": "80px", "className":"exchange_currency_rate",
                 	"render": function ( data, type, full, meta ) {
                 		var str='';
                		if(data){
                			    str =  parseFloat(data).toFixed(6);
                		}else{
                			if($('#sale_exchange_currency_rate').val()==''){
                    			str=0;
            				}else{
            					str=$('#sale_exchange_currency_rate').val();
            				}
                		}
                 		if(full.AUDIT_FLAG == 'Y'){
                 			return '<input type="text" name="exchange_currency_rate" style="width:100px" value="'+str+'" class="form-control notsave" disabled />';
                 		}else{
                 			return '<input type="text" name="exchange_currency_rate" style="width:100px" value="'+str+'" class="form-control notsave" />';
                 		}
                 	}
                 },
                 { "data": "EXCHANGE_TOTAL_AMOUNT", "width": "150px","className":"exchange_total_amount",
                 	"render": function ( data, type, full, meta ) {
                 		if(data)
                 			var str =  parseFloat(data).toFixed(2);
                 		else
                 			str = '';
                 		return '<input type="text" name="exchange_total_amount" style="width:150px" value="'+str+'" class="form-control notsave" disabled />';
                 	}
                 },
                 { "data": "SP_ID_NAME", "visible": false,
                     "render": function ( data, type, full, meta ) {
                         if(!data)
                             data='';
                         return data;
                     }
                 },
                 { "data": "CHARGE_ID_NAME", "visible": false,
                     "render": function ( data, type, full, meta ) {
                         if(!data)
                             data='';
                         return data;
                     }
                 },
                 { "data": "CURRENCY_ID_NAME", "visible": false,
                     "render": function ( data, type, full, meta ) {
                         if(!data)
                             data='';
                         return data;
                     }
                 },
                 { "data": "EXCHANGE_CURRENCY_ID_NAME", "visible": false,
                 	"render": function ( data, type, full, meta ) {
                 		if(!data)
                 			data='';
                 		return data;
                 	}
                 },
                 { "data": "UNIT_ID_NAME", "visible": false,
                  	"render": function ( data, type, full, meta ) {
                  		if(!data)
                  			data='';
                  		return data;
                  	 }
                  },
                 { "data": "AUDIT_FLAG", "visible": false,
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
    
    
    //获取回填到下表的汇率
    $('#sale_currency_id_list').on('mousedown','a',function(){
 	   	  $('#sale_currency_rate').val( $(this).attr('rate'));
 	   if($('#sale_exchange_currency_input').val()==''){
 		  $('#sale_exchange_currency_input').val($(this).text());
 		  $('#sale_exchange_currency').val($(this).attr('id'));
 	   }
 	   if($('#sale_exchange_currency_rate').val()==''){
 		  $('#sale_exchange_currency_rate').val($(this).attr('rate'));
 	   }
    });
    
    
    //刷新明细表
    itemOrder.refleshTradeSaleItemTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=trade_sale";
    	cargoTable.ajax.url(url).load();
    }
    
    if($('#trade_sale_table td').length>1){
    	var col = [6,9];
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
    
    $('#trade_sale_table').on('keyup', '[name=total_amount],[name=currency_total_amount],[name=exchange_rate]', function(){
    	var name = $(this).attr('name');
    	var row = $(this).parent().parent();
    	var currency_total_amount = $(row.find('[name=currency_total_amount]')).val();
    	var exchange_total_amount = $(row.find('[name=exchange_total_amount]')).val();
    	var total_amount = $(row.find('[name=total_amount]')).val();
    	var exchange_rate = $(row.find('[name=exchange_rate]')).val();
    	var exchange_currency_rate = $(row.find('[name=exchange_currency_rate]')).val();
    	
    	if(name=='currency_total_amount'){
        	if(currency_total_amount==''||exchange_rate==''){
        		$(row.find('[name=total_amount]')).val('');
        	}else if(!isNaN(currency_total_amount)&&!isNaN(exchange_rate)){
        		$(row.find('[name=total_amount]')).val((currency_total_amount/exchange_rate).toFixed(3));
        	}
    	}
    	if(name=='exchange_rate'){
    		if(total_amount!=''&&!isNaN(total_amount)){
	    		$(row.find('[name=currency_total_amount]')).val((total_amount*exchange_rate).toFixed(3));
	    	}else if(currency_total_amount!=''&&!isNaN(currency_total_amount)){
	    		$(row.find('[name=total_amount]')).val((currency_total_amount/exchange_rate).toFixed(3));
	    	}
    	}
    	if(name=='total_amount'){
	    	if(total_amount==''||exchange_rate==''){
	    		$(row.find('[name=currency_total_amount]')).val('');
	    		$(row.find('[name=exchange_total_amount]')).val('');
	    	}else if(!isNaN(total_amount)&&!isNaN(exchange_rate)){
	    		$(row.find('[name=currency_total_amount]')).val((total_amount*exchange_rate).toFixed(3));
	    		$(row.find('[name=exchange_total_amount]')).val((total_amount*exchange_currency_rate).toFixed(3));
	    	}
    	}
    	
    	var total_fee_amount_cny = 0;
		$('#trade_sale_table [name=currency_total_amount]').each(function(){
			var a = this.value;
			if(a!=''&&!isNaN(a)){
				total_fee_amount_cny+=parseFloat(a);
			}
		})
		$($('.dataTables_scrollFoot tr')[2]).find('th').eq(9).html(total_fee_amount_cny.toFixed(3));
		
		var total = 0;
		$('#trade_sale_table [name=total_amount]').each(function(){
			var a = this.value;
			if(a!=''&&!isNaN(a)){
				total+=parseFloat(a);
			}
		})
		$($('.dataTables_scrollFoot tr')[2]).find('th').eq(6).html(total.toFixed(3));
		
    })
   
     //贸易常用模板
    $('#collapseChargeSaleInfo').on('show.bs.collapse', function () {
        var thisType = $(this).attr('id');
        var type = 'Charge';
        var div = $('#'+type+'SaleDiv').empty();
        $('#collapse'+type+'SaleIcon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
        var order_type = $('#type').val();
        var customer_id = $('#customer_id').val();
        if(order_type.trim() == '' || customer_id == ''){
            $.scojs_message('请先选择类型和客户', $.scojs_message.TYPE_ERROR);
            return
        }else{
            $.post('/jobOrder/getTradeSaleTemplate', {order_type:order_type,customer_id:customer_id,arap_type:type}, function(data){
                if(data){
                    for(var i = 0;i<data.length;i++){
                        var json_obj = JSON.parse(data[i].JSON_VALUE);
                        var li = '';
                        var li_val = '';
                        for(var j = 0;j<json_obj.length;j++){
                            li +='<li '
                                +' sp_name="'+json_obj[j].sp_name+'" '
                                // +'charge_eng_id="'+json_obj[j].CHARGE_ENG_ID+'" '
                                +'charge_id="'+json_obj[j].CHARGE_ID+'" '
                                +'currency_id="'+json_obj[j].CURRENCY_ID+'" '
                                +'sp_id="'+json_obj[j].SP_ID+'" '
                                +'unit_id="'+json_obj[j].UNIT_ID+'" '
                                +'amount="'+json_obj[j].amount+'" '
                                +'charge_name="'+json_obj[j].charge_name+'" '
                                // +'charge_name_eng="'+json_obj[j].charge_eng_name+'" '
                                +'currency_name="'+json_obj[j].currency_name+'" '
                                +'currency_total_amount="'+json_obj[j].currency_total_amount+'" '
                                +'exchange_currency_id="'+json_obj[j].exchange_currency_id+'" '
                                +'exchange_currency_name="'+json_obj[j].exchange_currency_name+'" '
                                +'exchange_currency_rate="'+json_obj[j].exchange_currency_rate+'" '
                                +'exchange_rate="'+json_obj[j].exchange_rate+'" '
                                +'exchange_total_amount="'+json_obj[j].exchange_total_amount+'" '
                                +'order_type="'+json_obj[j].order_type+'" '
                                +'price="'+json_obj[j].price+'" '
                                // +'remark="'+json_obj[j].remark+'" '
                                +'total_amount="'+json_obj[j].total_amount+'" '
                                // +'type="'+json_obj[j].type+'" '
                                +'unit_name="'+json_obj[j].unit_name+'" '
                                +'></li>';
                            li_val += '<span></span> '+json_obj[j].sp_name+' , '+json_obj[j].charge_name+' , '+json_obj[j].price+'<br/>';
                        }
                        
                        div.append('<ul class="used'+type+'Info" id="'+data[i].ID+'">'
                                +li
                                +'<div class="radio">'
                                +'  <a class="delete'+type+'Template" style="margin-right: 10px;padding-top: 5px;float: left;">删除</a>'
                                +'  <div class="select'+type+'Template" style="margin-left: 60px;padding-top: 0px;">'
                                +'      <input type="radio" value="1" name="used'+type+'Info">'
                                +       li_val
                                +'  </div>'
                                +'</div><hr/>'
                                +'</ul>');
                        
                    }
                }
            });
        }
    });
 
    $('#collapseChargeSaleInfo').on('hide.bs.collapse', function () {
        var thisType = $(this).attr('id');
        var type = 'Charge';
        $('#collapse'+type+'SaleIcon').removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
    });
  
    $('#ChargeSaleDiv').on('click', '.deleteChargeTemplate,.deleteCostTemplate', function(){
        $(this).attr('disabled', true);
        var ul = $(this).parent().parent();
        var id = ul.attr('id');
        $.post('/jobOrder/deleteTradeSaleTemplate', {id:id}, function(data){
            if(data){
                $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
                $(this).attr('disabled', false);
                ul.css("display","none");
            }
        },'json').fail(function() {
            $(this).attr('disabled', false);
              $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
        });
    })
    
    
    //选中回显
    $('#ChargeSaleDiv').on('click', '.selectChargeTemplate,.selectCostTemplate', function(){
        $(this).parent().find('[type=radio]').prop('checked',true)
        
        var thisType = $(this).attr('class');
        var type = 'Charge';
        var table = 'trade_sale_table';
        // if('selectChargeTemplate'!=thisType){
        //     type='ChargeTradeSale';
        //     table='trade_sale_table';
        // }
        
        var li = $(this).parent().parent().find('li');
        var dataTable = $('#'+table).DataTable();
        
        for(var i=0; i<li.length; i++){
            var row = $(li[i]);
            var item={};
            item.ID='';
            item.SP_ID=row.attr('sp_id');
            item.CHARGE_ID= row.attr('charge_id');
            item.PRICE= row.attr('PRICE');
            item.AMOUNT= row.attr('amount');
            item.UNIT_ID= row.attr('unit_id');
            item.TOTAL_AMOUNT= row.attr('total_amount');
            item.CURRENCY_ID= row.attr('currency_id');
            item.EXCHANGE_RATE= row.attr('exchange_rate');
            item.CURRENCY_TOTAL_AMOUNT= row.attr('currency_total_amount');
            item.EXCHANGE_CURRENCY_ID= row.attr('exchange_currency_id');
            item.EXCHANGE_CURRENCY_RATE= row.attr('exchange_currency_rate');
            item.EXCHANGE_TOTAL_AMOUNT= row.attr('exchange_total_amount');
            item.SP_ID_NAME=row.attr('sp_name');
            item.CHARGE_ID_NAME=row.attr('charge_name');
            item.UNIT_ID_NAME=row.attr('unit_name');
            item.CURRENCY_ID_NAME=row.attr('currency_name');
            item.EXCHANGE_CURRENCY_ID_NAME=row.attr('exchange_currency_name');
            item.AUDIT_FLAG='';
            dataTable.row.add(item).draw();
        }
    });

    itemOrder.buildChargeSaleTemplate=function(){
        var cargo_table_rows = $("#trade_sale_table tr");
        var cargo_items_array=[];
        for(var index=0; index<cargo_table_rows.length; index++){
            if(index==0||index==1)
                continue;

            var row = cargo_table_rows[index];
            var empty = $(row).find('.dataTables_empty').text();
            if(empty)
                continue;
            
            var id = $(row).attr('id');
            if(!id){
                id='';
            }
            
            var item={}
            item.order_type = "charge";//应收
            for(var i = 1; i < row.childNodes.length; i++){
                if($(row.childNodes[i]).find('.notsave').size()==0&&$(row).find('.foot').size()==0){
                    var el = $(row.childNodes[i]).find('input,select');
                    var name = el.attr('name'); 
                    
                    if(el && name){
                        if(name=='exchange_currency_id'&&el.val()==''){
                            el.val(el.parent().parent().parent().find('[name=CURRENCY_ID]').val());
                        }
                        if(name=='exchange_currency_rate'&&el.val()==''){
                            el.val(1);
                        }
                        if(name=='exchange_total_amount'&&el.val()==''){
                            el.val(el.parent().parent().find('[name=total_amount]').val());
                        }
                        
                        if(name.toLowerCase()!='unit_id'){
                            var value = el.val();//元素的值
                            item[name] = value;
                        }
                        

                        if(name.toLowerCase().indexOf("_id")>=0){
                            var id_value = $(row.childNodes[i]).find('[name='+name+'_input]').val();
                            var abbr = name.toLowerCase().replace('id','name');
                            if(abbr!='unit_name'){
                                item[abbr] = id_value;
                            }
                        }
                    }
                }
            }
            cargo_items_array.push(item);
        }
        return cargo_items_array;
    };

     itemOrder.buildAllChargeSaleTemplate=function(){
        var cargo_table_rows = $("#trade_sale_table tr");
        var cargo_items_array=[];
        for(var index=0; index<cargo_table_rows.length; index++){
            if(index==0||index==1)
                continue;

            var row = cargo_table_rows[index];
            var empty = $(row).find('.dataTables_empty').text();
            if(empty)
                continue;
            var foot=$(row).find('.foot').text();
            if(foot)
                continue;

            var id = $(row).attr('id');
            if(!id){
                id='';
            }
            
            var item={}
            item.order_type = "charge";//应收
            for(var i = 1; i < row.childNodes.length; i++){
                    var el = $(row.childNodes[i]).find('input,select');
                    var name = el.attr('name'); 
                    
                    if(el && name){
                        if(name=='exchange_currency_id'&&el.val()==''){
                            el.val(el.parent().parent().parent().find('[name=CURRENCY_ID]').val());
                        }
                        if(name=='exchange_currency_rate'&&el.val()==''){
                            el.val(1);
                        }
                        if(name=='exchange_total_amount'&&el.val()==''){
                            el.val(el.parent().parent().find('[name=total_amount]').val());
                        }
                        var value = el.val();//元素的值
                        item[name] = value;

                        if(name.toLowerCase().indexOf("_id")>=0){
                            var id_value = $(row.childNodes[i]).find('[name='+name+'_input]').val();
                            var abbr = name.toLowerCase().replace('id','name');
                            item[abbr] = id_value;
                        }
                    }
            }
            cargo_items_array.push(item);
        }
        return cargo_items_array;
    };

});
});