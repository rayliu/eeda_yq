define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
$(document).ready(function() {
    

	var deletedTableIds=[];
	
    //删除一行
    $("#land_charge_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent().parent();
        deletedTableIds.push(tr.attr('id'));
        
        chargeTable.row(tr).remove().draw();
    }); 
    //添加一行
    $('#land_add_charge').on('click', function(){
        var item={};
        chargeTable.row.add(item).draw(true);
    });
    
    itemOrder.buildLandChargeDetail=function(){
        var cargo_table_rows = $("#land_charge_table tr");
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
    
  //弹出此陆运相关的费用
	var bindFieldEvent=function(){
    	eeda.bindTableField('land_charge_table','SP_ID','/serviceProvider/searchCompany','');
        eeda.bindTableField('land_charge_table','CHARGE_ID','/finItem/search','');
        eeda.bindTableField('land_charge_table','CHARGE_ENG_ID','/finItem/search_eng','');
        eeda.bindTableField('land_charge_table','UNIT_ID','/serviceProvider/searchChargeUnit','');
        eeda.bindTableFieldCurrencyId('land_charge_table','CURRENCY_ID','/serviceProvider/searchCurrency','');
        eeda.bindTableFieldCurrencyId('land_charge_table','exchange_currency_id','/serviceProvider/searchCurrency','');
    };
    
    //------------事件处理
    var chargeTable = eeda.dt({
        id: 'land_charge_table',
        autoWidth: false,
        drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
            bindFieldEvent();
        },
        columns:[
			{"data": "ID","width": "10px",
			    "render": function ( data, type, full, meta ) {
			    	if(data)
			    		return '<input type="checkbox" class="checkBoxOfLandChargeTable" style="width:30px" checked>';
			    	else 
			    		return '<input type="checkbox" style="width:30px" disabled>';
			    }
			},
            { "width": "110px",
                "render": function ( data, type, full, meta ) {
                	var str="<nobr>";
                	if(full.ID&&full.AUDIT_FLAG == 'Y'){
                		str+= '<button type="button" class="delete btn btn-default btn-xs" style="width:50px" disabled>删除</button>&nbsp';
                		str+= '<button type="button" class="btn btn-success btn-xs" style="width:50px"  disabled>确认</button> '; 
                		}
                	else if(full.ID){
                		str+= '<button type="button" class="delete btn btn-default btn-xs" style="width:50px" >删除</button>&nbsp';
                		str+= '<button type="button" class="chargeConfirm btn btn-success btn-xs" style="width:50px" value="'+full.ID+'" >确认</button> ';		
                	}else{
                		str+= '<button type="button" class="delete btn btn-default btn-xs" style="width:50px">删除</button>&nbsp';
                		str+= '<button type="button" class="btn btn-success btn-xs" style="width:50px"  disabled>确认</button> ';
                	}
                	str +="</nobr>";
                    return str;
                }
            },
            { "data": "TYPE", "width": "80px", 
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		var str = '<select name="type" class="form-control search-control" style="width:100px" disabled>'
	                        +'<option value="陆运" '+(data=='陆运' ? 'selected':'')+'>陆运</option>'
	                        +'</select>';
	                	return str;
                	}else{
                    var str = '<select name="type" class="form-control search-control" style="width:100px">'
                               +'<option value="陆运" '+(data=='陆运' ? 'selected':'')+'>陆运</option>'
                               +'</select>';
                    return str;
                  }
                }
            },
            { "data": "SP_ID", "width": "180px",
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
                            id: 'SP_ID',//对应数据库字段
                            value: data,
                            display_value: full.SP_NAME,
                            style:'width:200px'
                        }
                    );
                    return field_html;
                  }
                }
            },
            { "data": "CHARGE_ID", "width": "180px",
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
            { "data": "CHARGE_ENG_ID", "width": "180px",
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
            { "data": "PRICE", "width": "120px",
                "render": function ( data, type, full, meta ) {
                	if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                    	str = '';
                	if(full.AUDIT_FLAG == 'Y'){
                    	return '<input type="text" name="price" style="width:120px" value="'+str+'" class="form-control" disabled />';
                     }else{
	                    return '<input type="text" name="price" style="width:120px" value="'+str+'" class="form-control" />';
	                 }
                  }
            },
            { "data": "AMOUNT","width": "80px",
                "render": function ( data, type, full, meta ) {
                	if(!data)
                        data='';
                	if(full.AUDIT_FLAG == 'Y'){
                        return '<input type="text" name="amount" style="width:80px" value="'+data+'" class="form-control " disabled/>';
                     }else{
	                    return '<input type="text" name="amount" style="width:80px" value="'+data+'" class="form-control"/>';
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
            { "data": "TOTAL_AMOUNT", "width": "150px","class":"currency_total_amount",
                "render": function ( data, type, full, meta ) {
                	if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                    	str = '';
	                return '<input type="text" name="total_amount" style="width:150px" value="'+str+'" class="form-control" disabled/>';
                }
            },
            { "data": "CURRENCY_ID", "width": "60px",
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
                        var str =  parseFloat(data).toFixed(2);
                    else
                    	str = '';
                	if(full.AUDIT_FLAG == 'Y'){
                        return '<input type="text" name="exchange_rate" style="width:80px" value="'+str+'" class="form-control" disabled />';
                    } else{
	                    return '<input type="text" name="exchange_rate" style="width:80px" value="'+str+'" class="form-control" />';
	                }
                }
            },
            { "data": "CURRENCY_TOTAL_AMOUNT", "width": "150px","class":"cny_total_amount",
                "render": function ( data, type, full, meta ) {
                	if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                    	str = '';
	                return '<input type="text" name="currency_total_amount" style="width:150px" value="'+str+'" class="form-control" disabled/>';
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
            { "data": "EXCHANGE_CURRENCY_RATE", "width": "60px", 
            	"render": function ( data, type, full, meta ) {
            		if(data)
            			var str =  parseFloat(data).toFixed(2);
            		else
            			str = '';
            		if(full.AUDIT_FLAG == 'Y'){
            			return '<input type="text" name="exchange_currency_rate" style="width:80px" value="'+str+'" class="form-control" disabled />';
            		}else{
            			return '<input type="text" name="exchange_currency_rate" style="width:80px" value="'+str+'" class="form-control" />';
            		}
            	}
            },
            { "data": "EXCHANGE_TOTAL_AMOUNT", "width": "150px",
            	"render": function ( data, type, full, meta ) {
            		if(data)
            			var str =  parseFloat(data).toFixed(2);
            		else
            			str = '';
            		return '<input type="text" name="exchange_total_amount" style="width:150px" value="'+str+'" class="form-control" disabled />';
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
	
    $('#land_charge_table_msg').on('click','.save',function(){
    	$(this).attr('disabled', true);
    	var order = {};
    	order.order_id = $('#order_id').val();
    	order.land_item_id = $('#land_item_id').val();
    	order.land_charge_item = itemOrder.buildLandChargeDetail();
    	$.post('/jobOrder/saveLandCharge', {params:JSON.stringify(order)}, function(data){
    		 
    		 var url = "/jobOrder/tableListOfLandCharge?order_id="+order_id+"&land_item_id="+land_item_id;
        	 chargeTable.ajax.url(url).load();
        	 itemOrder.refleshChargeTable(order.ID);
             itemOrder.refleshCostTable(order.ID);
    		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
    		$('#land_charge_table_msg .save').attr('disabled', false);
    	 },'json').fail(function() {
             $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
             $('#land_charge_table_msg .save').attr('disabled', false);
         });
    })
    
    $('#land_table').on('click','.land_charge',function(){
    		var land_item_id = $(this).parent().parent().attr('id');
    		$('#land_item_id').val(land_item_id);
    		var order_id = $('#order_id').val();
    		var url = "/jobOrder/tableListOfLandCharge?order_id="+order_id+"&land_item_id="+land_item_id;
        	chargeTable.ajax.url(url).load();
        	$('#land_charge_table_msg_btn').click();
    })
    
  //数量和单价自动补零
    $('#land_charge_table').on('blur','[name=price],[name=amount]',function(){
    	var amount = $(this).val();
    	if(amount!=''&&!isNaN(amount)){
    		$(this).val(itemOrder.returnFloat(amount));
    	}
    })
    
    //输入 数量*单价的时候，计算金额
    $('#land_charge_table').on('keyup','[name=price],[name=amount],[name=exchange_rate],[name=exchange_currency_rate]',function(){
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
    			$(row.find('[name=currency_total_amount]')).val((total_amount*parseFloat(exchange_rate)).toFixed(3));
    			if(exchange_currency_rate==''){
        			$(row.find('[name=exchange_total_amount]')).val('');
        		}
    			if(exchange_currency_rate!=''&&!isNaN(exchange_currency_rate)){
    				$(row.find('[name=exchange_total_amount]')).val((total_amount*parseFloat(exchange_currency_rate).toFixed(3)));
        		}
    		}
    	}
    });
    
  //选择是否是同一结算公司
	var cnames = [];
	$('#land_charge_table').on('click','input[type="checkbox"]',function () {
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
    $('#land_AllCheckOfChargeTable').click(function(){
	    $(".checkBoxOfLandChargeTable").prop("checked",this.checked);
	    
	    var hava_check = 0;
    	$('#land_charge_table input[type="checkbox"]:checked').each(function(){	
    		hava_check++;
    	})
	    if(this.checked==true&&$('#land_charge_table td').length>1&&hava_check>0){
	    	$('#print_debit_note').attr('disabled',false);
	    }else{
	    	$('#print_debit_note').attr('disabled',true);
	    }
    });
    $("#land_charge_table").on('click','.checkBoxOfLandChargeTable',function(){
		  $("#land_AllCheckOfChargeTable").prop("checked",$(".checkBoxOfLandChargeTable").length == $(".checkBoxOfLandChargeTable:checked").length ? true : false);
    });
    
	
  });
});