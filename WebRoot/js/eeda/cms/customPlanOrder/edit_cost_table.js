define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'jq_blockui'], function ($, metisMenu, template) {
$(document).ready(function() {

	var deletedTableIds=[];
	
	//删除一行
    $("#cost_table").on('click', '.delete', function(e){
    	e.preventDefault();
    	var tr = $(this).parent().parent().parent();
    	deletedTableIds.push(tr.attr('id'))
    	
    	costTable.row(tr).remove().draw();
    }); 
    //添加一行
    $('#add_cost').on('click', function(){
    	var item={};
    	costTable.row.add(item).draw(true);
    });
    
    //费用明细确认按钮动作
    $("#cost_table").on('click', '.costConfirm', function(){
    	var id = $(this).parent().parent().parent().attr('id');
    	$.post('/customPlanOrder/feeConfirm',{id:id},function(joa){
    		var order_id = $('#order_id').val();
	    	var url = "/customPlanOrder/tableList?order_id="+order_id+"&type=cost&showHide="+is_show_hide_charge_col;
	    	costTable.ajax.url(url).load();    		
    		$.scojs_message('确认成功', $.scojs_message.TYPE_OK);
    	},'json').fail(function() {
            $.scojs_message('确认失败', $.scojs_message.TYPE_ERROR);
       });
    });
    

    salesOrder.buildCostDetail=function(){
        var cargo_table_rows = $("#cost_table tr");
        var cargo_items_array=[];
        for(var index=0; index<cargo_table_rows.length; index++){
            if(index==0)
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
            item.id = id;
            item.order_type = "cost";//应付
            for(var i = 0; i < row.childNodes.length; i++){
            	var el = $(row.childNodes[i]).find('input,select');
            	var name = el.attr('name'); 
            	var type = el.attr('type');
                
                if(type=='checkbox'){
                    if(el.prop('checked')){
                        item[name] = 'Y';
                    }else{
                        item[name] = 'N';
                    }
                }else if(el && name){
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
        eeda.bindTableField('cost_table','SP_ID','/serviceProvider/searchCompany','');
        eeda.bindTableField('cost_table','CHARGE_ID','/finItem/search','');
        eeda.bindTableField('cost_table','CHARGE_ENG_ID','/finItem/search_eng','');
        eeda.bindTableField('cost_table','UNIT_ID','/serviceProvider/searchChargeUnit','');
        eeda.bindTableFieldCurrencyId('cost_table','CURRENCY_ID','/serviceProvider/searchCurrency','');
    };
    
    var costTable = eeda.dt({
        id: 'cost_table',
        autoWidth: false,
        drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
            bindFieldEvent();

            $.unblockUI();
        },
        columns:[
			{"data": "HIDE_FLAG", "width": "30px", visible: is_show_hide_charge_col,
			    "render": function ( data, type, full, meta ) {
                    if(full.HIDE_FLAG=='Y')
			    	    return '<input type="checkbox" class="checkBox" name="hide_flag" checked>';
                    else
                        return '<input type="checkbox" class="checkBox" name="hide_flag">';
			    }
			},
			{ "data": "ID","width": "110px",
                "render": function ( data, type, full, meta ) {
                	var str="<nobr>";
                	if(data&&full.AUDIT_FLAG == 'Y'){
                		str+= '<button type="button" class="delete btn btn-default btn-xs" style="width:50px" disabled>删除</button>&nbsp';
                		str+= '<button type="button" class="btn btn-success btn-xs" style="width:50px"  disabled>确认</button> '; 
                		}
                	else if(data){
                		str+= '<button type="button" class="delete btn btn-default btn-xs" style="width:50px" >删除</button>&nbsp';
                		str+= '<button type="button" class=" btn btn-success btn-xs costConfirm" style="width:50px" value="'+data+'" >确认</button> ';		
                	}else{
                		str+= '<button type="button" class="delete btn btn-default btn-xs" style="width:50px">删除</button>&nbsp';
                		str+= '<button type="button" class="btn btn-success btn-xs" style="width:50px"  disabled>确认</button> ';
                	}
                	str +="</nobr>";
                    return str;
                }
            },
            { "data": "TYPE","width": "80px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		var str = '<select name="type" class="form-control search-control" style="width:100px" disabled>'
	                        +'<option value="海关" '+(data=='海关' ? 'selected':'')+'>海关</option>'
	                        +'<option value="码头" '+(data=='码头' ? 'selected':'')+'>码头</option>'
	                        +'</select>';
	                	return str;
                	}else{
	                	var str = '<select name="type" class="form-control search-control" style="width:100px">'
	                        +'<option value="海关" '+(data=='海关' ? 'selected':'')+'>海关</option>'
	                        +'<option value="码头" '+(data=='码头' ? 'selected':'')+'>码头</option>'
	                        +'</select>';
	                	return str;
                	}
                }
            },
            { "data": "SP_ID","width": "180px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		if(!data)
                            data='';
                        var field_html = template('table_dropdown_template',
                            {
                                id: 'SP_ID',
                                value: data,
                                display_value: full.SP_NAME,
                                style:'width:200px',
                                disabled:'disabled'
                            }
                        );
                        return field_html;
                     }else{
                    if(!data)
                        data='';
                    var field_html = template('table_dropdown_template',
                        {
                            id: 'SP_ID',
                            value: data,
                            display_value: full.SP_NAME,
                            style:'width:200px'
                        }
                    );
                    return field_html;
                 }
               }
            },
            { "data": "CHARGE_ID","width": "180px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		if(!data)
                            data='';
                        var field_html = template('table_dropdown_template',
                            {
                                id: 'CHARGE_ID',
                                value: data,
                                display_value: full.CHARGE_NAME,
                                style:'width:200px',
                                disabled:'disabled'
                            }
                        );
                        return field_html;
                     }else{
                    if(!data)
                        data='';
                    var field_html = template('table_dropdown_template',
                        {
                            id: 'CHARGE_ID',//对应数据库字段
                            value: data,
                            display_value: full.CHARGE_NAME,
                            style:'width:200px'
                        }
                    );
                    return field_html;
                }
              }
            },
            { "data": "CHARGE_ENG_ID","width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(full.AUDIT_FLAG == 'Y'){
            			if(!data)
            				data='';
            			var field_html = template('table_dropdown_template',
            					{
            				id: 'CHARGE_ENG_ID',
            				value: data,
            				display_value: full.CHARGE_NAME_ENG,
            				style:'width:200px',
            				disabled:'disabled'
            					}
            			);
            			return field_html;
            		}else{
            			if(!data)
            				data='';
            			var field_html = template('table_dropdown_template',
            					{
            				id: 'CHARGE_ENG_ID',
            				value: data,
            				display_value: full.CHARGE_NAME_ENG,
            				style:'width:200px',
            				disabled:'disabled'
            					}
            			);
            			return field_html;
            		}
            	}
            },
            { "data": "PRICE", "width": "60px",
                "render": function ( data, type, full, meta ) {
                	if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                    	str = '';
                	if(full.AUDIT_FLAG == 'Y'){
                    		return '<input type="text" name="price" style="width:80px" value="'+str+'" class="form-control" disabled />';
                     }else{
                 			return '<input type="text" name="price" style="width:80px" value="'+str+'" class="form-control" />';
                     }
               }
            },
            { "data": "AMOUNT","width": "60px",
                "render": function ( data, type, full, meta ) {
                	if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                    	str = '';
                	if(full.AUDIT_FLAG == 'Y'){
                        	return '<input type="text" name="amount" style="width:80px" value="'+str+'" class="form-control" disabled />';
                     }else{
                         	return '<input type="text" name="amount" style="width:80px" value="'+str+'" class="form-control" />';
	                 }
              }
            },
            { "data": "UNIT_ID","width": "60px","visible":false,
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                	 if(!data)
                         data='';
                     var field_html = template('table_dropdown_template',
                         {
                             id: 'UNIT_ID',
                             value: data,
                             display_value: full.UNIT_NAME,
                             style:'width:80px',
                             disabled:'disabled'
                         }
                     );
                     return field_html;
                }else{
            	   if(!data)
                       data='';
                   var field_html = template('table_dropdown_template',
                       {
                           id: 'UNIT_ID',
                           value: data,
                           display_value: full.UNIT_NAME,
                           style:'width:80px'
                       }
                   );
                   return field_html;
                }
              }
            },
            { "data": "CURRENCY_ID", "width":"60px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		if(!data){
                    		var field_html = template('table_dropdown_template',
        	                        {
        	                            id: 'CURRENCY_ID',
        	                            value: '3',
        	                            display_value: "人民币",
        	                            style:'width:80px',
        	                            disabled:'disabled'
        	                        }
        	                    );
                    	}else{
                    		var field_html = template('table_dropdown_template',
        	                        {
        	                            id: 'CURRENCY_ID',
        	                            value: data,
        	                            display_value: "人民币",
        	                            style:'width:80px',
        	                            disabled:'disabled'
        	                        }
        	                    );
                    	}
                        return field_html;
                }else{
                	if(!data){
                		var field_html = template('table_dropdown_template',
    	                        {
    	                            id: 'CURRENCY_ID',
    	                            value: '3',
    	                            display_value: "人民币",
    	                            style:'width:80px',
    	                            disabled:'disabled'
    	                        }
    	                    );
                	}else{
                		var field_html = template('table_dropdown_template',
    	                        {
    	                            id: 'CURRENCY_ID',
    	                            value: data,
    	                            display_value: "人民币",
    	                            style:'width:80px',
    	                            disabled:'disabled'
    	                        }
    	                    );
                	}
                    return field_html;
                }
              }
            },
            { "data": "TOTAL_AMOUNT", "width": "60px","className":"currency_total_amount",
                "render": function ( data, type, full, meta ) {
                	if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                    	str = '';
                	if(full.AUDIT_FLAG == 'Y'){
                        	return '<input type="text" name="total_amount" style="width:80px" value="'+str+'" class="form-control" disabled />';
                	}else{
                        	return '<input type="text" name="total_amount" style="width:80px" value="'+str+'" class="form-control" disabled/>';
                	}
                }
            },
            { "data": "EXCHANGE_RATE", "width": "60px", "className":"currency_rate","visible":false,
                "render": function ( data, type, full, meta ) {
                	if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                    	str = '';
                if(full.AUDIT_FLAG == 'Y'){
                    	return '<input type="text" name="exchange_rate" style="width:80px" value="'+str+'" class="form-control" disabled />';
                }else{
                    	return '<input type="text" name="exchange_rate" style="width:80px" value="'+str+'" class="form-control" />';
               }
              }
            },
            { "data": "CURRENCY_TOTAL_AMOUNT", "width": "60px","className":"cny_total_amount","visible":false,
                "render": function ( data, type, full, meta ) {
                	if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                    	str = '';
	                if(full.AUDIT_FLAG == 'Y'){
	                    	return '<input type="text" name="currency_total_amount" style="width:80px" value="'+str+'" class="form-control" disabled />';
	                }else{
	                    	return '<input type="text" name="currency_total_amount" style="width:80px" value="'+str+'" class="form-control" disabled />';
	                }
              }
            },
            { "data": "REMARK","width": "180px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="remark" style="width:200px" value="'+data+'" class="form-control" disabled />';
	                }else{
	            	   if(!data)
	                       data='';
	                   return '<input type="text" name="remark" style="width:200px" value="'+data+'" class="form-control" />';
	                }
               }
            }, 
            { "data": "SP_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            }, 
            { "data": "CHARGE_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            },
            { "data": "CHARGE_NAME_ENG", "visible": false,
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return data;
            	}
            },
            { "data": "UNIT_NAME", "visible": false,
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
            { "data": "AUDIT_FLAG", "visible": false,
            	"render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            }
        ]
    });

    //刷新明细表
    salesOrder.refleshCostTable = function(order_id){
    	var url = "/customPlanOrder/tableList?order_id="+order_id+"&type=cost&showHide="+is_show_hide_charge_col;
    	costTable.ajax.url(url).load();
    }
   
    //输入 数量*单价的时候，计算金额
    $('#cost_table ').on('keyup', ' [name=price],[name=amount], [name=exchange_rate]', function(){
    	var row = $(this).parent().parent();
    	var price = $(row.find('[name=price]')).val()
    	var amount = $(row.find('[name=amount]')).val()
    	var exchange_rate = $(row.find('[name=exchange_rate]')).val()
    	if(price==''||amount==''){
    		$(row.find('[name=total_amount]')).val('');
    		$(row.find('[name=currency_total_amount]')).val('');
    	}
    	if(price!=''&&amount!=''&&!isNaN(price)&&!isNaN(amount)){
    		var total_amount = parseFloat(price*amount);
    		$(row.find('[name=total_amount]')).val(total_amount);
    		if(exchange_rate!=''&&!isNaN(exchange_rate)){
    			$(row.find('[name=currency_total_amount]')).val(parseFloat(total_amount*exchange_rate));
    			getTotalCost();
    		  }
    				
    	}
    })
   
    
    var getTotalCost=function(){
       //计算应付字段
       var totalCostRMB = 0; 
       var totalCostUSD = 0;
       var totalCostJPY = 0; 
       var totalCostHKD = 0;
       var profitTotalCost = 0;
       
       var tableCur =$('#cost_table').find('[name=CURRENCY_ID_input]');
       var tableAmount =$('#cost_table').find('[name=total_amount]');
       var currencyTotalAmountCost = $('#cost_table').find('[name=currency_total_amount]');
       for(var i = 0;i<tableCur.length;i++){
           if(tableCur[i].value=='CNY'){               
               totalCostRMB += parseFloat(tableAmount[i].value);   //parseFloat(data)
           }else if(tableCur[i].value=='USD'){
               totalCostUSD += parseFloat(tableAmount[i].value);
           }else if(tableCur[i].value=='JPY'){
               totalCostJPY += parseFloat(tableAmount[i].value);
           }else if(tableCur[i].value=='HKD'){
               totalCostHKD += parseFloat(tableAmount[i].value);
           }
           profitTotalCost+= parseFloat(currencyTotalAmountCost[i].value);
       }
       //隐藏应付人民币汇总字段
       $('[name=profitTotalCost]').text(profitTotalCost).hide();   
       //赋值
       $('.costRMB').text(totalCostRMB);
       if(totalCostRMB !=""&&!isNaN(totalCostRMB)){
    	   $('.costRMB').text('CNY '+eeda.numFormat(parseFloat(totalCostRMB).toFixed(2),3));
       }else{
           $('.costRMB').text('CNY '+eeda.numFormat(parseFloat(0).toFixed(2),3));
        }
       
       if(totalCostUSD !=''&&!isNaN(totalCostUSD)){
          $('.costUSD').text('USD '+eeda.numFormat(parseFloat(totalCostUSD).toFixed(2),3));          
       }else{
          $('.costUSD').text('USD '+eeda.numFormat(parseFloat(0).toFixed(2),3));
        }

       if(totalCostJPY !=""&&!isNaN(totalCostJPY)){
           $('.costJPY').text('JPY '+eeda.numFormat(parseFloat(totalCostJPY).toFixed(2),3));
       	}else{
               $('.costJPY').text('JPY '+eeda.numFormat(parseFloat(0).toFixed(2),3));
        }
       
       if(totalCostHKD !=''&&!isNaN(totalCostHKD)){
              $('.costHKD').text('HKD '+eeda.numFormat(parseFloat(totalCostHKD).toFixed(2),3));          
        }else{
              $('.costHKD').text('HKD '+eeda.numFormat(parseFloat(0).toFixed(2),3));
         }

       calcCurrency();
   }

    getTotalCost();
//       var totalCharge = $('[name=chargeRMB]').text().toString();
//       var totalChargeU = $('[name=chargeUSD]').text().toString();        
//       var totalChargeRMB=parseFloat(totalCharge.replace('RMB',''));        
//       var totalChargeUSD=parseFloat(totalChargeU.replace('USD',''));        
//       if(isNaN(totalChargeRMB)|| totalChargeRMB =='' ){
//           totalChargeRMB=0;
//         }        
//       if(isNaN(totalChargeUSD)|| totalChargeUSD =='' ){
//          totalChargeUSD=0;
//         }
//    
//    var costRMB = $('[name=costRMB]').text();    		        

  });
});