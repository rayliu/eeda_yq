window.calcCurrency =  function (){
        var chargeRMB = $('[name=chargeRMB]').text().replace('CNY','');
            chargeRMB = chargeRMB.replace(/,/g,'');
        var costRMB = $('[name=costRMB]').text().replace('CNY','');
            costRMB = costRMB.replace(/,/g,'');
        var profitRMB = parseFloat(chargeRMB)-parseFloat(costRMB);
           if(profitRMB>=0){
              $('[name=profitRMB]').text("CNY "+eeda.numFormat(parseFloat(profitRMB).toFixed(2),3));
           }else if(profitRMB<0){
              $('[name=profitRMB]').html("<span style='color:red'>CNY "+eeda.numFormat(parseFloat(profitRMB).toFixed(2),3)+"</span>");
           } else{
              $('[name=profitRMB]').text("CNY "+eeda.numFormat(parseFloat(0).toFixed(2),3));
              
           }

        var chargeUSD = $('[name=chargeUSD]').text().replace('USD','');
        	chargeUSD = chargeUSD.replace(/,/g,'');
        var costUSD = $('[name=costUSD]').text().replace('USD','');
        	costUSD = costUSD.replace(/,/g,'');
        var profitUSD = parseFloat(chargeUSD)-parseFloat(costUSD);
           if(profitUSD>=0){
              $('[name=profitUSD]').text("USD "+eeda.numFormat(parseFloat(profitUSD).toFixed(2),3));
           }else if(profitUSD<0){
              $('[name=profitUSD]').html("<span style='color:red'>USD "+eeda.numFormat(parseFloat(profitUSD).toFixed(2),3)+"</span>");
           } else{
              $('[name=profitUSD]').text("USD "+eeda.numFormat(parseFloat(0).toFixed(2),3));
           }
  
        var chargeJPY = $('[name=chargeJPY]').text().replace('JPY','');
       		chargeJPY = chargeJPY.replace(/,/g,'');
        var costJPY = $('[name=costJPY]').text().replace('JPY','');
       	    costJPY = costJPY.replace(/,/g,'');
        var profitJPY = parseFloat(chargeJPY)-parseFloat(costJPY);
          if(profitJPY>=0){
             $('[name=profitJPY]').text("JPY "+eeda.numFormat(parseFloat(profitJPY).toFixed(2),3));
          }else if(profitJPY<0){
             $('[name=profitJPY]').html("<span style='color:red'>JPY "+eeda.numFormat(parseFloat(profitJPY).toFixed(2),3)+"</span>");
          } else{
             $('[name=profitJPY]').text("JPY "+eeda.numFormat(parseFloat(0).toFixed(2),3));
          }
  
        var chargeHKD = $('[name=chargeHKD]').text().replace('HKD','');
         	chargeHKD = chargeHKD.replace(/,/g,'');
        var costHKD = $('[name=costHKD]').text().replace('HKD','');
         	costHKD = costHKD.replace(/,/g,'');
        var profitHKD = parseFloat(chargeHKD)-parseFloat(costHKD);
           if(profitHKD>=0){
               $('[name=profitHKD]').text("HKD "+eeda.numFormat(parseFloat(profitHKD).toFixed(2),3));
            }else if(profitHKD<0){
               $('[name=profitHKD]').html("<span style='color:red'>HKD "+eeda.numFormat(parseFloat(profitHKD).toFixed(2),3)+"</span>");
            } else{
               $('[name=profitHKD]').text("HKD "+eeda.numFormat(parseFloat(0).toFixed(2),3));
            }

        //人民币利润汇总字段
        var profitTotalCharge = $('[name=profitTotalCharge]').text();
        if(profitTotalCharge ==''&&isNaN(profitTotalCharge)){
            profitTotalCharge=0;
            $('[name=profitTotalCharge]').text(profitTotalCharge);
        }
        var profitTotalCost = $('[name=profitTotalCost]').text();
        if(profitTotalCost ==''&&isNaN(profitTotalCost)){  
              profitTotalCost = 0;
              $('[name=profitTotalCost]').text(profitTotalCost);
          }

        var profitTotalRMB = parseFloat(profitTotalCharge)-parseFloat(profitTotalCost);
        if(profitTotalRMB>=0){
        $('[name=profitTotalRMB]').text("CNY "+eeda.numFormat(parseFloat(profitTotalRMB).toFixed(2),3));
        }else if(profitTotalRMB<0){
            $('[name=profitTotalRMB]').html("<span style='color:red'>CNY "+eeda.numFormat(parseFloat(profitTotalRMB).toFixed(2),3)+"</span>");
        } else{
            $('[name=profitTotalRMB]').text("CNY "+eeda.numFormat(parseFloat(0).toFixed(2),3));
        }
    };
    
define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
    

$(document).ready(function() {
    

	var deletedTableIds=[];
	
    //删除一行
    $("#charge_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent().parent();
        deletedTableIds.push(tr.attr('id'));
        
        chargeTable.row(tr).remove().draw();
    }); 
    //添加一行
    $('#add_charge').on('click', function(){
        var item={};
        chargeTable.row.add(item).draw(true);
    });
    
    //费用明细确认按钮动作
    $("#charge_table").on('click', '.chargeConfirm', function(){
    	var id = $(this).parent().parent().parent().attr('id');
    	$.post('/jobOrder/feeConfirm',{id:id},function(data){
    		var order_id = $('#order_id').val();
    		itemOrder.refleshChargeTable(order_id);   		
    		$.scojs_message('确认成功', $.scojs_message.TYPE_OK);
    	},'json').fail(function() {
            $.scojs_message('确认失败', $.scojs_message.TYPE_ERROR);
       });
    });
    //费用明细取消确认按钮动作
    $("#charge_table").on('click', '.cancelChargeConfirm', function(){
    	var id = $(this).parent().parent().parent().attr('id');
    	$.post('/jobOrder/feeCancelConfirm',{id:id},function(data){
    		if(data.BILL_FLAG == 'Y'){
    			$.scojs_message('该单据已生成对账单，不能取消确认', $.scojs_message.TYPE_ERROR);
    		}
    		else{
	    		var order_id = $('#order_id').val();
	    		itemOrder.refleshChargeTable(order_id);   		
	    		$.scojs_message('取消确认成功', $.scojs_message.TYPE_OK);
    		}
    	},'json').fail(function() {
            $.scojs_message('取消确认失败', $.scojs_message.TYPE_ERROR);
       });
    });
    
    itemOrder.buildChargeDetail=function(){
        var cargo_table_rows = $("#charge_table tr");
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
    
    itemOrder.buildChargeTemplate=function(){
        var cargo_table_rows = $("#charge_table tr");
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
            item.order_type = "charge";//应收
            for(var i = 1; i < row.childNodes.length; i++){
            	if($(row.childNodes[i]).find('.notsave').size()==0){
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
    
    var bindFieldEvent=function(){
    	eeda.bindTableField('charge_table','SP_ID','/serviceProvider/searchCompany','');
        eeda.bindTableField('charge_table','CHARGE_ID','/finItem/search','');
        eeda.bindTableField('charge_table','CHARGE_ENG_ID','/finItem/search_eng','');
        eeda.bindTableField('charge_table','UNIT_ID','/serviceProvider/searchChargeUnit','');
        eeda.bindTableFieldCurrencyId('charge_table','CURRENCY_ID','/serviceProvider/searchCurrency','');
        eeda.bindTableFieldCurrencyId('charge_table','exchange_currency_id','/serviceProvider/searchCurrency','');
    };
    
    //------------事件处理
    var chargeTable = eeda.dt({
        id: 'charge_table',
        autoWidth: true,
        drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
            bindFieldEvent();
        },
        columns:[
			{"data": "ID","width": "10px",
			    "render": function ( data, type, full, meta ) {
			    	if(data)
			    		return '<input type="checkbox" class="checkBoxOfChargeTable">';
			    	else 
			    		return '<input type="checkbox" disabled>';
			    }
			},
            { "width": "50px",
                "render": function ( data, type, full, meta ) {
                	var str="<nobr>";
                	if(full.ID&&full.AUDIT_FLAG == 'Y'){
                		str+= '<button type="button" class="delete btn table_btn delete_btn btn-xs" disabled><i class="fa fa-trash-o"></i> 删除</button></button>&nbsp';
                		str+= '<button type="button" class="cancelChargeConfirm btn table_btn btn-danger btn-xs" >取消确认</button> '; 
                		}
                	else if(full.ID){
                		str+= '<button type="button" class="delete btn table_btn delete_btn btn-xs" ><i class="fa fa-trash-o"></i> 删除</button></button>&nbsp';
                		str+= '<button type="button" class="chargeConfirm btn table_btn btn-success btn-xs"  value="'+full.ID+'" >确认</button> ';		
                	}else{
                		str+= '<button type="button" class="delete btn table_btn delete_btn btn-xs"><i class="fa fa-trash-o"></i> 删除</button></button>&nbsp';
                		str+= '<button type="button" class="btn table_btn btn-success btn-xs" disabled>确认</button> ';
                	}
                	str +="</nobr>";
                    return str;
                }
            },
            { "data": "TYPE", "width": "80px", 
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		var str = '<select name="type" class="form-control search-control notsave" style="width:50px" disabled>'  +'<option value="" >空运</option>'
                			+'<option value="海运" '+(data=='海运' ? 'selected':'')+'> 海运 </option>'
	                        +'<option value="空运" '+(data=='空运' ? 'selected':'')+'> 空运 </option>'
	                        +'<option value="陆运" '+(data=='陆运' ? 'selected':'')+'> 陆运 </option>'
	                        +'<option value="贸易" '+(data=='贸易' ? 'selected':'')+'> 贸易 </option>'
	                        +'<option value="报关" '+(data=='报关' ? 'selected':'')+'> 报关 </option>'
	                        +'<option value="保险" '+(data=='保险' ? 'selected':'')+'> 保险 </option>'
	                        +'</select>';
	                	return str;
                	}else{
                			var trans_type=$('#trans_type').val();
                			var str = '<select name="type" class="form-control search-control notsave" style="width:50px">'
                               +'<option value='+trans_type+'>'+trans_type+'</option>'
                    		   +'<option value="海运" '+(data=='海运' ? 'selected':'')+'> 海运 </option>'
                               +'<option value="空运" '+(data=='空运' ? 'selected':'')+'> 空运 </option>'
                               +'<option value="陆运" '+(data=='陆运' ? 'selected':'')+'> 陆运 </option>'
                               +'<option value="贸易" '+(data=='贸易' ? 'selected':'')+'> 贸易 </option>'
                               +'<option value="报关" '+(data=='报关' ? 'selected':'')+'> 报关 </option>'
                               +'<option value="保险" '+(data=='保险' ? 'selected':'')+'> 保险 </option>'
                               +'</select>';
                    return str;
                  }
                }
            },
            { "data": "SP_ID", "width": "80px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		if(!data)
                            data='';
                        var field_html = template('table_dropdown_template',
                            {
                                id: 'SP_ID',
                                value: data,
                                display_value: full.SP_NAME,
                                disabled:'disabled'
                            }
                        );
                        return field_html;
                  }else{
                    if(!data){
                    	var field_html = template('table_dropdown_template',
    	                        {
    	                            id: 'SP_ID',
    	                            value: $('#sp').val(),
    	                            display_value: $('#sp_input').val()
    	                        }
                    	);
                       }else{
                    var field_html = template('table_dropdown_template',
                        {
                            id: 'SP_ID',
                            value: data,//对应数据库字段
                            display_value: full.SP_NAME
                        }
                     );
                   }
                    return field_html;
                  }
                }
            },
           
            { "data": "CHARGE_ID", "width": "80px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		if(!data)
                            data='';
                        var field_html = template('table_dropdown_template',
                            {
                                id: 'CHARGE_ID',
                                value: data,
                                display_value: full.CHARGE_NAME,
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
                            display_value: full.CHARGE_NAME
                        }
                    );
                    return field_html;
                }
              }
            },
            { "data": "CHARGE_ENG_ID", "width": "180px",
            	"render": function ( data, type, full, meta ) {
        			if(!data)
        				data='';
        			var field_html = template('table_dropdown_template',
        					{
        				id: 'CHARGE_ENG_ID',
        				value: data,
        				display_value: full.CHARGE_NAME_ENG,
        				
        				disabled:'disabled'
        					}
        			);
        			return field_html;
            	}
            },
            { "data": "PRICE", "width": "50px",
                "render": function ( data, type, full, meta ) {
                	if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                    	str = '';
                	if(full.AUDIT_FLAG == 'Y'){
                    	return '<input type="text" name="price"  value="'+str+'" class="form-control notsave" disabled />';
                     }else{
	                    return '<input type="text" name="price" value="'+str+'" class="form-control notsave" />';
	                 }
                  }
            },
            { "data": "AMOUNT","width": "50px",
                "render": function ( data, type, full, meta ) {
                	if(!data)
                        data='';
                	if(full.AUDIT_FLAG == 'Y'){
                        return '<input type="text" name="amount"  value="'+data+'" class="form-control notsave" disabled/>';
                     }else{
	                    return '<input type="text" name="amount"  value="'+data+'" class="form-control notsave"/>';
	                }
                }
            },
            { "data": "UNIT_ID", "width": "60px",
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
            { "data": "TOTAL_AMOUNT", "width": "80px","className":"currency_total_amount",
                "render": function ( data, type, full, meta ) {
                	if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                    	str = '';
                	return '<input type="text" name="total_amount" style="width:150px" value="'+str+'" class="form-control notsave" disabled />';
                	
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
	                            display_value: full.CURRENCY_NAME,
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
                           id: 'CURRENCY_ID',
                           value: data,
                           display_value: full.CURRENCY_NAME,
                           style:'width:80px'
                       }
                   );
                   return field_html; 
                }
              }
            },
            { "data": "EXCHANGE_RATE", "width": "80px",
                "render": function ( data, type, full, meta ) {
                	if(data)
                        var str =  parseFloat(data).toFixed(6);
                    else
                    	str = '';
                if(full.AUDIT_FLAG == 'Y'){
                    	return '<input type="text" name="exchange_rate" style="width:100px" value="'+str+'" class="form-control" disabled />';
                }else{
                    	return '<input type="text" name="exchange_rate" style="width:100px" value="'+str+'" class="form-control" />';
               }
              }
            },
            { "data": "CURRENCY_TOTAL_AMOUNT", "width": "80px","className":"cny_total_amount",
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
            			if(!data)
            				data='';
            			var field_html = template('table_dropdown_template',
            					{
		            				id: 'exchange_currency_id',
		            				value: data,
		            				display_value: full.EXCHANGE_CURRENCY_ID_NAME,
		            				style:'width:80px'
            					}
            			);
            			return field_html; 
            		}
            	}
            },
            { "data": "EXCHANGE_CURRENCY_RATE", "width": "80px", "className":"exchange_currency_rate",
            	"render": function ( data, type, full, meta ) {
            		if(data)
            			var str =  parseFloat(data).toFixed(6);
            		else
            			str = '';
            		if(full.AUDIT_FLAG == 'Y'){
            			return '<input type="text" name="exchange_currency_rate" style="width:100px" value="'+str+'" class="form-control" disabled />';
            		}else{
            			return '<input type="text" name="exchange_currency_rate" style="width:100px" value="'+str+'" class="form-control" />';
            		}
            	}
            },
            { "data": "EXCHANGE_TOTAL_AMOUNT", "width": "80px","className":"exchange_total_amount",
            	"render": function ( data, type, full, meta ) {
            		if(data)
            			var str =  parseFloat(data).toFixed(2);
            		else
            			str = '';
            		return '<input type="text" name="exchange_total_amount" style="width:150px" value="'+str+'" class="form-control notsave" disabled />';
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
            }, { "data": "SP_NAME", "visible": false,
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
            { "data": "EXCHANGE_CURRENCY_ID_NAME", "visible": false,
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
    itemOrder.refleshChargeTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=charge";
    	chargeTable.ajax.url(url).load();
    }
    
  //数量和单价自动补零
    $('#charge_table').on('blur','[name=price],[name=amount]',function(){
    	var amount = $(this).val();
    	if(amount!=''&&!isNaN(amount)){
    		$(this).val(itemOrder.returnFloat(amount));
    	}
    })
    
    //输入 数量*单价的时候，计算金额
    $('#charge_table').on('keyup','[name=price],[name=amount],[name=exchange_rate],[name=exchange_currency_rate]',function(){
    	var row = $(this).parent().parent();
    	var price = $(row.find('[name=price]')).val();
    	var amount = $(row.find('[name=amount]')).val();
    	var exchange_rate = $(row.find('[name=exchange_rate]')).val();
    	var exchange_currency_rate = $(row.find('[name=exchange_currency_rate]')).val();
    	if(price==''||amount==''){
    		$(row.find('[name=total_amount]')).val('');
    		$(row.find('[name=currency_total_amount]')).val('');
    		$(row.find('[name=exchange_total_amount]')).val('');
    	}
    	if(price!=''&&amount!=''&&!isNaN(price)&&!isNaN(amount)){
    		var total_amount = parseFloat(price)*parseFloat(amount);
    		$(row.find('[name=total_amount]')).val(total_amount);
    		if(exchange_rate==''){
    			$(row.find('[name=currency_total_amount]')).val('');
    		}
    		if(exchange_rate!=''&&!isNaN(exchange_rate)){
    			$(row.find('[name=currency_total_amount]')).val((total_amount*parseFloat(exchange_rate)).toFixed(2));
    			getTotalCharge();
    			if(exchange_currency_rate==''){
        			$(row.find('[name=exchange_total_amount]')).val('');
        		}
    			if(exchange_currency_rate!=''&&!isNaN(exchange_currency_rate)){
    				
    				$(row.find('[name=exchange_total_amount]')).val((total_amount*parseFloat(exchange_currency_rate)).toFixed(2));
        		}
    		}
    	}
    });
    
//    $('#table_currency_input_field_list').on('click', '.fromLocationItem', function(e){
//		  var inputField = eeda._inputField;
//		  var dataId = $(this).attr('dataId');
//		  var row = inputField.parent().parent().parent();
//		  var currency_rate = $(this).attr('currency_rate');
//          row.find('.currency_rate input').val(currency_rate);//选择币制则填入汇率
//          var total = row.find('[name=total_amount]').val();
//          if(currency_rate!=''&&total!=''){
//        	  row.find('[name=currency_total_amount]').val(currency_rate*total);//计算人民币金额
//        	  getTotalCharge();
//          }
//	  });
//	  $('#table_currency_input_field_list').on('keydown', 'li', function(e){
//          if (e.keyCode == 13) {
//        	  var inputField = eeda._inputField;
//              var $a = $(this).find('a');
//              var dataId = $a.attr('dataId');
//              var row = inputField.parent().parent().parent();
//              var currency_rate = $a.attr('currency_rate');
//              row.find('.currency_rate input').val(currency_rate);
//              var total = row.find('[name=total_amount]').val();
//              if(currency_rate!=''&&total!=''){
//            	  row.find('[name=currency_total_amount]').val(currency_rate*total);//计算人民币金额
//            	  getTotalCharge();
//              }
//          }
//      });
    

	//计算应收字段
    var getTotalCharge= function(){
    	var totalChargeRMB = 0; 
	    var totalChargeUSD = 0;
	    var totalChargeJPY = 0;
	    var totalChargeHKD = 0;
	    var profitTotalCharge = 0;
	    var tableCurCharge =$('#charge_table').find('[name=CURRENCY_ID_input]');
	    var tableAmountCharge =$('#charge_table').find('[name=total_amount]');
	    var currencyTotalAmount = $('#charge_table').find('[name=currency_total_amount]');
	    for(var i = 0;i<tableCurCharge.length;i++){
	        if(tableCurCharge[i].value=='CNY'){               
	           totalChargeRMB += parseFloat(tableAmountCharge[i].value);   //parseFloat(data)
	        }else if(tableCurCharge[i].value=='USD'){
	           totalChargeUSD += parseFloat(tableAmountCharge[i].value);
	        }else if(tableCurCharge[i].value=='JPY'){
	           totalChargeJPY += parseFloat(tableAmountCharge[i].value);
	        }else if(tableCurCharge[i].value=='HKD'){
	           totalChargeHKD += parseFloat(tableAmountCharge[i].value);
	        }
	        profitTotalCharge += parseFloat(currencyTotalAmount[i].value);
	    }
	    //隐藏字段   应收人民币汇总字段
	    $('.profitTotalCharge').text(profitTotalCharge).hide();
	    
	    if(totalChargeRMB!=""&&!isNaN(totalChargeRMB)){
	           $('.chargeRMB').text("CNY "+eeda.numFormat(parseFloat(totalChargeRMB).toFixed(2),3));  
	     }else{
	           $('.chargeRMB').text("CNY "+eeda.numFormat(parseFloat(0).toFixed(2),3));  
	      }
	    
	    if(totalChargeUSD!=""&&!isNaN(totalChargeUSD)){
	        $('.chargeUSD').text("USD "+eeda.numFormat(parseFloat(totalChargeUSD).toFixed(2),3));  
	    }else{
	        $('.chargeUSD').text("USD "+eeda.numFormat(parseFloat(0).toFixed(2),3));  
	     }

	    if(totalChargeJPY!=""&&!isNaN(totalChargeJPY)){
	        $('.chargeJPY').text("JPY "+eeda.numFormat(parseFloat(totalChargeJPY).toFixed(2),3));  
	    }else{
	        $('.chargeJPY').text("JPY "+eeda.numFormat(parseFloat(0).toFixed(2),3));  
	     }
	    
	    if(totalChargeHKD!=""&&!isNaN(totalChargeHKD)){
	        $('.chargeHKD').text("HKD "+eeda.numFormat(parseFloat(totalChargeHKD).toFixed(2),3));  
	    }else{
	        $('.chargeHKD').text("HKD "+eeda.numFormat(parseFloat(0).toFixed(2),3));  
	     }

	    calcCurrency();
    }
    getTotalCharge();
    
    //弹出框获取结算公司
    var buildSpList = function(){
    	//获取选中的结算公司
    	$("#spList").empty();
    	$("#spList").append('<option></option>');
        var nameArray = [];
    	$("#charge_table tr").each(function(e){
    		if(e.toString()==0)
    			return;
    		var sp_id = $(this).find('[name="SP_ID"]').val();
    		var sp_name = $(this).find('[name="SP_ID_input"]').val();

    		if(sp_name != ''){
    			for(name in nameArray){
    				if(nameArray[name]==sp_name){
    					return;
    				}
    			}
    			nameArray.push(sp_name);
    			
    			$("#spList").append('<option value='+sp_id+'>'+sp_name+'</option>');
    		}
    		
    	})
    }
    
    //checkbox选中则button可点击
    $('#charge_table').on('click','.checkBoxOfChargeTable',function(){
    	var hava_check = 0;
    	$('#charge_table input[type="checkbox"]:checked').each(function(){	
    		hava_check++;
    	})
    	if(hava_check>0){
    		$('#print_debit_note').attr('disabled',false);
    	}else{
    		$('#print_debit_note').attr('disabled',true);
    	}
    });
    $('#print_debit_note').click(function(){
    	buildSpList();
    	$('#invoiceNo').val($('#ref_no').val());
    })
  
    
  //选择是否是同一结算公司
	var cnames = [];
	$('#charge_table').on('click','input[type="checkbox"]',function () {
			var cname = $($(this).parent().parent().find('[name="SP_ID"]')).val();
			
			if($(this).prop('checked')==true){	
				if(cnames.length > 0 ){
					if(cnames[0]!=cname){
						$.scojs_message('打印PDF时,请选择同一个结算公司', $.scojs_message.TYPE_ERROR);
						$(this).attr('checked',false);
						return false;
					}else{
						cnames.push(cname);
					}
				}else{
					cnames.push(cname);	
				}
			}else{
				cnames.pop(cname);
		 }
	 });
	
	
	//全选
    $('#AllCheckOfChargeTable').click(function(){
	    $(".checkBoxOfChargeTable").prop("checked",this.checked);
	    
	    var hava_check = 0;
    	$('#charge_table input[type="checkbox"]:checked').each(function(){	
    		hava_check++;
    	})
	    if(this.checked==true&&$('#charge_table td').length>1&&hava_check>0){
	    	$('#print_debit_note').attr('disabled',false);
	    }else{
	    	$('#print_debit_note').attr('disabled',true);
	    }
    });
    $("#charge_table").on('click','.checkBoxOfChargeTable',function(){
		  $("#AllCheckOfChargeTable").prop("checked",$(".checkBoxOfChargeTable").length == $(".checkBoxOfChargeTable:checked").length ? true : false);
    });
    
    
	
  });
});