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
    $('#add_charge_cost').on('click', function(){
    	if($('#rateExpired').val()=='Y'){
    		$.scojs_message('当前汇率已过期，请更新汇率才能进行添加费用', $.scojs_message.TYPE_ERROR);
    		return;
    	}
    	var item={};
    	costTable.row.add(item).draw(true);
    	
    });
    
    //费用明细 应付信息 确认按钮动作
    $("#cost_table").on('click', '.costConfirm', function(){
    	var id = $(this).parent().parent().parent().attr('id');
    	$.post('/jobOrder/feeConfirm',{id:id},function(data){
    		var order_id = $('#order_id').val();
    		itemOrder.refleshCostTable(order_id); 		
    		$.scojs_message('确认成功', $.scojs_message.TYPE_OK);
    	},'json').fail(function() {
            $.scojs_message('确认失败', $.scojs_message.TYPE_ERROR);
       });
    });
    
  //费用明细取消确认按钮动作
    $("#cost_table").on('click', '.cancelCostConfirm', function(){
    	var id = $(this).parent().parent().parent().attr('id');
    	$.post('/jobOrder/feeCancelConfirm',{id:id},function(data){
    		if(data.BILL_FLAG == 'Y'){
    			$.scojs_message('该单据已生成对账单，不能取消确认', $.scojs_message.TYPE_ERROR);
    		}
    		else{
	    		var order_id = $('#order_id').val();
	    		itemOrder.refleshCostTable(order_id); 
	    		$.scojs_message('取消确认成功', $.scojs_message.TYPE_OK);
    		}
    	},'json').fail(function() {
            $.scojs_message('取消确认失败', $.scojs_message.TYPE_ERROR);
       });
    });
    
    itemOrder.buildChargeCostDetail=function(){
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
            		if(name=='rmb_difference'&&el.val()==''){
                        el.val(0.00);
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
    
    
    itemOrder.buildCostTemplate=function(){
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
            item.order_type = "cost";//应付
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
    
    itemOrder.buildAllCostTemplate=function(){
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
            item.order_type = "cost";//应付
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
    
    
    //------------事件处理
    var bindFieldEvent=function(){	
        eeda.bindTableField('cost_table','SP_ID','/serviceProvider/searchCompany','');
        eeda.bindTableField('cost_table','CHARGE_ID','/finItem/search','');
        eeda.bindTableField('cost_table','CHARGE_ENG_ID','/finItem/search_eng','');
        eeda.bindTableField('cost_table','UNIT_ID','/serviceProvider/searchChargeUnit','');
        eeda.bindTableFieldCurrencyId('cost_table','CURRENCY_ID','/serviceProvider/searchCurrency','');
        eeda.bindTableFieldCurrencyId('cost_table','exchange_currency_id','/serviceProvider/searchCurrency','');
    };
    
    var costTable = eeda.dt({
        id: 'cost_table',
        autoWidth: false,
        drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
            bindFieldEvent();

            $.unblockUI();
        },
        columns:[
			{ "data": "ID",
			    "render": function ( data, type, full, meta ) {
			    	if(data)
			    		return '<input type="checkbox"  class="checkBoxOfCostTable" >';
			    	else 
			    		return '<input type="checkbox"  disabled>';
			    }
			},
			{ "width": "50px",
                "render": function ( data, type, full, meta ) {
                	var str="<nobr>";
                	if(full.ID&&full.AUDIT_FLAG == 'Y'){
                		str+= '<button type="button" class="delete btn table_btn delete_btn btn-xs" disabled><i class="fa fa-trash-o"></i> 删除</button></button>&nbsp';
                		str+= '<button type="button" class="cancelCostConfirm btn table_btn btn-danger btn-xs">取消确认</button> '; 
                		}
                	else if(full.ID){
                		str+= '<button type="button" class="delete btn table_btn delete_btn btn-xs" ><i class="fa fa-trash-o"></i> 删除</button></button>&nbsp';
                		str+= '<button type="button" class="costConfirm btn table_btn btn_green btn-xs" value="'+full.ID+'" >确认</button> ';		
                	}else{
                		str+= '<button type="button" class="delete btn table_btn delete_btn btn-xs"><i class="fa fa-trash-o"></i> 删除</button></button>&nbsp';
                		str+= '<button type="button" class="btn table_btn btn_green btn-xs"  disabled>确认</button> ';
                	}
                	str +="</nobr>";
                    return str;
                }
            },
            { "data": "TYPE","width": "50px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		var str = '<select name="type" class="form-control search-control notsave" style="width:100px" disabled>'
	                        +'<option value="海运" '+(data=='海运' ? 'selected':'')+'>海运</option>'
	                        +'<option value="空运" '+(data=='空运' ? 'selected':'')+'>空运</option>'
	                        +'<option value="陆运" '+(data=='陆运' ? 'selected':'')+'>陆运</option>'
	                        +'<option value="贸易" '+(data=='贸易' ? 'selected':'')+'>贸易</option>'
	                        +'<option value="报关" '+(data=='报关' ? 'selected':'')+'>报关</option>'
	                        +'<option value="保险" '+(data=='保险' ? 'selected':'')+'>保险</option>'
	                        +'</select>';
	                	return str;
                	}else{
	                	var str = '<select name="type" class="form-control search-control notsave" style="width:100px">'
	                        +'<option value="海运" '+(data=='海运' ? 'selected':'')+'>海运</option>'
	                        +'<option value="空运" '+(data=='空运' ? 'selected':'')+'>空运</option>'
	                        +'<option value="陆运" '+(data=='陆运' ? 'selected':'')+'>陆运</option>'
	                        +'<option value="贸易" '+(data=='贸易' ? 'selected':'')+'>贸易</option>'
	                        +'<option value="报关" '+(data=='报关' ? 'selected':'')+'>报关</option>'
	                        +'<option value="保险" '+(data=='保险' ? 'selected':'')+'>保险</option>'
	                        +'</select>';
	                	return str;
                	}
                }
            },
            { "data": "SP_ID","width": "80px",
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
            { "data": "CHARGE_ID","width": "80px",
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
            { "data": "CHARGE_ENG_ID","width": "80px",
            	"render": function ( data, type, full, meta ) {
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
            },
            { "data": "PRICE", "width": "50px",
                "render": function ( data, type, full, meta ) {
                	if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                    	str = '';
                	if(full.AUDIT_FLAG == 'Y'){
                    		return '<input type="text" name="price" style="width:120px" value="'+str+'" class="form-control notsave" disabled />';
                     }else{
                 			return '<input type="text" name="price" style="width:120px" value="'+str+'" class="form-control notsave" />';
                     }
               }
            },
            { "data": "AMOUNT","width": "60px",
                "render": function ( data, type, full, meta ) {
                	if(!data)
                        data='1';
                	if(full.AUDIT_FLAG == 'Y'){
                        	return '<input type="text" name="amount" style="width:120px" value="'+data+'" class="form-control notsave" disabled />';
                     }else{
                         	return '<input type="text" name="amount" style="width:120px" value="'+data+'" class="form-control notsave" />';
	                 }
              }
            },
            { "data": "UNIT_ID","width": "60px",
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
            	   if(!data){
                            data='33';
                            full.UNIT_NAME="B/L";
                        }
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
                        var str =  parseFloat(data).toFixed(3);
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
                        var str =  (Math.round(data*100)/100).toFixed(2);
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
            { "data": "EXCHANGE_CURRENCY_RATE", "width": "80px","className":"exchange_currency_rate",
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
            			var str =  (Math.round(data*100)/100).toFixed(2);
            		else
            			str = '';
            		return '<input type="text" name="exchange_total_amount" style="width:150px" value="'+str+'" class="form-control notsave" disabled />';
            	}
            },
            { "data": "EXCHANGE_CURRENCY_RATE_RMB", "width": "80px", "className":"exchange_currency_rate_rmb",
                "render": function ( data, type, full, meta ) {
                    if(data)
                        var str =  parseFloat(data).toFixed(6);
                    else
                        str = '';
                    if(full.AUDIT_FLAG == 'Y'){
                        return '<input type="text" name="exchange_currency_rate_rmb" style="width:100px" value="'+str+'" class="form-control" disabled />';
                    }else{
                        return '<input type="text" name="exchange_currency_rate_rmb" style="width:100px" value="'+str+'" class="form-control" />';
                    }
                }
            },
            { "data": "EXCHANGE_TOTAL_AMOUNT_RMB", "width": "80px","className":"exchange_total_amount_rmb",
                "render": function ( data, type, full, meta ) {
                    if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                        str = '';
                    return '<input type="text" name="exchange_total_amount_rmb" style="width:150px" value="'+str+'" class="form-control notsave" disabled />';
                }
            },
            { "data": "RMB_DIFFERENCE", "width": "80px","className":"rmb_difference",
                "render": function ( data, type, full, meta ) {
                    if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                        str = '0.00';
                    return '<input type="text" name="rmb_difference" style="width:150px" value="'+str+'" class="form-control notsave" disabled />';
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
    
    //计算对账金额（人民币）与。。差
   $(function(){
       var cargo_table_rows= $('#cost_table tr');
       var total_rmb=0;
       var exchange_total_rmb=0;
       for (var index = 1; index < cargo_table_rows.length; index++) {
           var row= cargo_table_rows[index];
           var empty = $(row).find('.dataTables_empty').text();
            if(empty)
                continue;

           var total=$(row).find('[name=currency_total_amount]').val();
           var exchange_total=$(row).find('[name=exchange_total_amount_rmb]').val();
           total_rmb+=parseFloat(total);
           exchange_total_rmb+=parseFloat(exchange_total);
       };
       var difference=parseFloat(total_rmb)-parseFloat(exchange_total_rmb);
    $('#totalcost_rmb_difference_span').text(eeda.numFormat(parseFloat(difference).toFixed(2),3));
    });

    //刷新明细表
    itemOrder.refleshCostTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=cost";
    	costTable.ajax.url(url).load();
    }
    
   //数量和单价自动补零
    $('#cost_table').on('blur','[name=price],[name=amount]',function(){
    	var amount = $(this).val();
    	if(amount!=''&&!isNaN(amount)){
    		$(this).val(itemOrder.returnFloat(amount));
    	}
    })
    
    //整数自动补零
    itemOrder.returnFloat = function(value){
    	 var xsd=value.toString().split(".");
    	 if(xsd.length==1){
    	 value=value.toString()+".00";
    	 return value;
    	 }
    	 if(xsd.length>1){
    	 if(xsd[1].length<2){
    	 value=value.toString()+"0";
    	 }
    	 return value;
    	 }
    	}
    
    //输入 数量*单价的时候，计算金额
    $('#cost_table').on('keyup','[name=price],[name=amount],[name=exchange_rate],[name=exchange_currency_rate],[name=exchange_currency_rate_rmb]',function(){
        var row = $(this).parent().parent();
        var price = $(row.find('[name=price]')).val();
        var amount = $(row.find('[name=amount]')).val();
        var exchange_rate = $(row.find('[name=exchange_rate]')).val();
        var exchange_currency_rate = $(row.find('[name=exchange_currency_rate]')).val();
        var exchange_currency_rate_rmb = $(row.find('[name=exchange_currency_rate_rmb]')).val();
        if(price==''||amount==''){
            $(row.find('[name=total_amount]')).val('');
            $(row.find('[name=currency_total_amount]')).val('');
            $(row.find('[name=exchange_total_amount]')).val('');
            $(row.find('[name=exchange_total_amount_rmb]')).val('');
            $(row.find('[name=rmb_difference]')).val('');
        }
        if(price!=''&&amount!=''&&!isNaN(price)&&!isNaN(amount)){
            var total_amount = parseFloat(price)*parseFloat(amount);
            $(row.find('[name=total_amount]')).val(total_amount);
            if(exchange_rate==''){
                $(row.find('[name=currency_total_amount]')).val('');
            }
            if(exchange_rate!=''&&!isNaN(exchange_rate)){
                $(row.find('[name=currency_total_amount]')).val((total_amount*parseFloat(exchange_rate)).toFixed(2));
                getTotalCost();
                if(exchange_currency_rate==''){
                    $(row.find('[name=exchange_total_amount]')).val('');
                    $(row.find('[name=exchange_total_amount_rmb]')).val('');
                    $(row.find('[name=rmb_difference]')).val('');
                }
                if(exchange_currency_rate!=''&&!isNaN(exchange_currency_rate)){
                    $(row.find('[name=exchange_total_amount]')).val((total_amount*parseFloat(exchange_currency_rate)).toFixed(2));
                      if(exchange_currency_rate_rmb==''){
                         $(row.find('[name=exchange_total_amount_rmb]')).val('');
                         $(row.find('[name=rmb_difference]')).val('');
                    }
                    if(exchange_currency_rate_rmb!=''&&!isNaN(exchange_currency_rate_rmb)){
                        var exchange_total_amount = parseFloat($(row.find('[name=exchange_total_amount]')).val());
                        var currency_total_amount = parseFloat($(row.find('[name=currency_total_amount]')).val());
                        $(row.find('[name=exchange_total_amount_rmb]')).val((exchange_total_amount*parseFloat(exchange_currency_rate_rmb)).toFixed(2));
                        var exchange_total_amount_rmb = parseFloat($(row.find('[name=exchange_total_amount_rmb]')).val());
                        $(row.find('[name=rmb_difference]')).val((parseFloat(exchange_total_amount_rmb-currency_total_amount)).toFixed(2));
                      }
                }
            }
        }
    });
   
    
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
    
    
    //全选
    $('#AllCheckOfCostTable').click(function(){
	    $(".checkBoxOfCostTable").prop("checked",this.checked);
    });
    $("#cost_table").on('click','.checkBoxOfCostTable',function(){
		  $("#AllCheckOfCostTable").prop("checked",$(".checkBoxOfCostTable").length == $(".checkBoxOfCostTable:checked").length ? true : false);
    });

  });
});