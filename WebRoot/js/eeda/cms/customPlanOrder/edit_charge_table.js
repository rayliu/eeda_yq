
    
define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
    

$(document).ready(function() {
    

	var deletedTableIds=[];

    salesOrder.buildChargeTemplate=function(){
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
    
    
    salesOrder.buildAllChargeTemplate=function(){
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
    	$.post('/customPlanOrder/feeConfirm',{id:id},function(data){
    		var order_id = $('#order_id').val();
	    	var url = "/customPlanOrder/tableList?order_id="+order_id+"&type=charge"+"&showHide="+is_show_hide_charge_col;
	    	chargeTable.ajax.url(url).load();
    		$.scojs_message('确认成功', $.scojs_message.TYPE_OK);
    	},'json').fail(function() {
            $.scojs_message('确认失败', $.scojs_message.TYPE_ERROR);
       });
    });
    	
    
    salesOrder.buildChargeDetail=function(){
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
    
    var bindFieldEvent=function(){
    	eeda.bindTableField('charge_table','SP_ID','/serviceProvider/searchCompany','');
        eeda.bindTableField('charge_table','CHARGE_ID','/finItem/search','');
        eeda.bindTableField('charge_table','CHARGE_ENG_ID','/finItem/search_eng','');
        eeda.bindTableField('charge_table','UNIT_ID','/serviceProvider/searchChargeUnit','');
        eeda.bindTableFieldCurrencyId('charge_table','CURRENCY_ID','/serviceProvider/searchCurrency','');
    };
    
    //------------事件处理
    var chargeTable = eeda.dt({
        id: 'charge_table',
        //autoWidth: false,
        drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
            bindFieldEvent();
        },
        columns:[
			{"data": "HIDE_FLAG", "width": "50px", visible: is_show_hide_charge_col,
		    	"render": function ( data, type, full, meta ) {
                    if(full.HIDE_FLAG=='N')
                        return '<input type="checkbox" class="checkBox" style="width:70px" name="hide_flag">';
                    else
                        return '<input type="checkbox" class="checkBox" style="width:70px" name="hide_flag" checked>';
                }
			},
            {"data": "ID", "width": "70px",
                "render": function ( data, type, full, meta ) {
                	var str="<nobr>";
                	if(data&&full.AUDIT_FLAG == 'Y'){
                		str+= '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:50px"  disabled>删除</button>&nbsp';
                		str+= '<button type="button" class="cancelChargeConfirm btn table_btn btn-danger btn-xs"  >取消确认</button> ';  
                	}else if(data){
                		str+= '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:50px">删除</button>&nbsp';
                		str+= '<button type="button" class="btn table_btn btn_green btn-xs chargeConfirm" style="width:50px" value="'+data+'" >确认</button> ';		
                	}else{
                		str+= '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:50px" >删除</button>&nbsp';
                		str+= '<button type="button" class="btn table_btn btn_green btn-xs" style="width:50px"   disabled>确认</button> ';
                	}
                	str +="</nobr>";
                    return str;
                }
            },
            { "data": "TYPE", "width": "80px", "visible":false,
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		var str = '<select name="type" class="form-control search-control notsave" style="width:100px" disabled>'
	                        +'<option value="海运" '+(data=='海运' ? 'selected':'')+'>海运</option>'
	                        +'<option value="空运" '+(data=='空运' ? 'selected':'')+'>空运</option>'
	                        +'<option value="陆运" '+(data=='陆运' ? 'selected':'')+'>陆运</option>'
	                        +'<option value="报关" '+(data=='报关' ? 'selected':'')+'>报关</option>'
	                        +'<option value="保险" '+(data=='保险' ? 'selected':'')+'>保险</option>'
	                        +'</select>';
	                	return str;
                	}else{
                    var str = '<select name="type" class="form-control search-control notsave" style="width:100px">'
                               +'<option value="海运" '+(data=='海运' ? 'selected':'')+'>海运</option>'
                               +'<option value="空运" '+(data=='空运' ? 'selected':'')+'>空运</option>'
                               +'<option value="陆运" '+(data=='陆运' ? 'selected':'')+'>陆运</option>'
                               +'<option value="报关" '+(data=='报关' ? 'selected':'')+'>报关</option>'
                               +'<option value="保险" '+(data=='保险' ? 'selected':'')+'>保险</option>'
                               +'</select>';
                    return str;
                  }
                }
            },
            { "data": "SP_ID", "width": "100px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		if(!data)
                            data='';
                        var field_html = template('table_dropdown_template',
                            {
                                id: 'SP_ID',
                                value: data,
                                display_value: full.SP_NAME,
                                style:'width:120px',
                                disabled:'disabled'
                            }
                        );
                        return field_html;
                  }else{
                    if(!data){
                         var field_html = template('table_dropdown_template',
                                {
                                    id: 'SP_ID',//对应数据库字段
                                    value: $('#sp').val(),
                                    display_value: $('#sp_input').val(),
                                    style:'width:120px'
                                }
                       );
                    }else{
                          var field_html = template('table_dropdown_template',
                              {
                                  id: 'SP_ID',//对应数据库字段
                                  value: data,
                                  display_value: full.SP_NAME,
                                  style:'width:120px'
                              }
                          );
                  }
                 return field_html;
                }
              }
            },
            { "data": "CHARGE_ID", "width": "100px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		if(!data)
                            data='';
                        var field_html = template('table_dropdown_template',
                            {
                                id: 'CHARGE_ID',
                                value: data,
                                display_value: full.CHARGE_NAME,
                                style:'width:120px',
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
                            style:'width:120px'
                        }
                    );
                    return field_html;
                }
              }
            },
            { "data": "CHARGE_ENG_ID","visible":false,
            	"render": function ( data, type, full, meta ) {
            		if(full.AUDIT_FLAG == 'Y'){
            			if(!data)
            				data='';
            			var field_html = template('table_dropdown_template',
            					{
            				id: 'CHARGE_ENG_ID',
            				value: data,
            				display_value: full.CHARGE_NAME_ENG,
            				style:'width:120px',
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
            				style:'width:120px',
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
                    	return '<input type="text" name="price" style="width:80px" value="'+str+'" class="form-control notsave" disabled />';
                     }else{
	                    return '<input type="text" name="price" style="width:80px" value="'+str+'" class="form-control notsave" />';
	                 }
                  }
            },
            { "data": "AMOUNT","width": "60px",
                "render": function ( data, type, full, meta ) {
                	if(data){
                        var str =  parseFloat(data).toFixed(2);
                	}else{
                    	str = '1';
                    	}
                	if(full.AUDIT_FLAG == 'Y'){
                        return '<input type="text" name="amount" style="width:80px" value="'+str+'" class="form-control notsave" disabled/>';
                     }else{
	                    return '<input type="text" name="amount" style="width:80px" value="'+str+'" class="form-control notsave"/>';
	                }
                }
            },
            { "data": "UNIT_ID", "width": "60px","visible":false,
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
            { "data": "CURRENCY_ID", "width": "60px",
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
                        return '<input type="text" name="total_amount" style="width:80px" value="'+str+'" class="form-control notsave" disabled/>';
                    }else{
	                    return '<input type="text" name="total_amount" style="width:80px" value="'+str+'" class="form-control notsave" disabled/>';
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
                        return '<input type="text" name="exchange_rate" style="width:80px" value="'+str+'" class="form-control notsave" disabled />';
                    } else{
	                    return '<input type="text" name="exchange_rate" style="width:80px" value="'+str+'" class="form-control notsave" />';
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
                        return '<input type="text" name="currency_total_amount" style="width:80px" value="'+str+'" class="form-control notsave" disabled />';
                    } else{
	                    return '<input type="text" name="currency_total_amount" style="width:80px" value="'+str+'" class="form-control notsave" disabled/>';
	                }
                }
            },
            { "data": "REMARK","width": "180px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="remark" style="width:200px" value="'+data+'" class="form-control notsave" disabled />';
	                }else{
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="remark" style="width:200px" value="'+data+'" class="form-control notsave" />';
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
    salesOrder.refleshChargeTable = function(order_id){
    	var url = "/customPlanOrder/tableList?order_id="+order_id+"&type=charge";
    	chargeTable.ajax.url(url).load();
    }
    
    //输入 数量*单价的时候，计算金额
    $('#charge_table').on('keyup','[name=price],[name=amount],[name=exchange_rate]',function(){
    	var row = $(this).parent().parent();
    	var price = $(row.find('[name=price]')).val()
    	var amount = $(row.find('[name=amount]')).val()
    	var exchange_rate = $(row.find('[name=exchange_rate]')).val()
    	if(price==''||amount==''){
    		$(row.find('[name=total_amount]')).val('');
    		$(row.find('[name=currency_total_amount]')).val('');
    	}
    	if(price!=''&&amount!=''&&!isNaN(price)&&!isNaN(amount)){
    		var total_amount = parseFloat(price)*parseFloat(amount);
    		$(row.find('[name=total_amount]')).val(total_amount);
    		if(exchange_rate!=''&&!isNaN(exchange_rate)){
    			
    			$(row.find('[name=currency_total_amount]')).val(total_amount*parseFloat(exchange_rate));
    			getTotalCharge();
    		}
    	}
    });
    

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
//	        profitTotalCharge += parseFloat(currencyTotalAmount[i].value);
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

	    window.calcCurrency();
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
    $('#charge_table').on('click','.checkBox',function(){
    	var hava_check = 0;
    	$('#charge_table input[type="checkbox"]').each(function(){	
    		var checkbox = $(this).prop('checked');
    		if(checkbox){
    			hava_check=1;
    		}	
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
    
    
    //费用明细取消确认按钮动作
    $("#charge_table").on('click', '.cancelChargeConfirm', function(){
    	var id = $(this).parent().parent().parent().attr('id');
    	$.post('/customPlanOrder/feeCancelConfirm',{id:id},function(data){
    		if(data==false){
    			$.scojs_message('该单据已生成对账单，不能取消确认', $.scojs_message.TYPE_ERROR);
    		}else{
	    		var order_id = $('#order_id').val();
	    		salesOrder.refleshChargeTable(order_id);   		
	    		$.scojs_message('取消确认成功', $.scojs_message.TYPE_OK);
    		}
    	})
    });
  
    
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

    $('#sp_list').on('click',function(){
         var val=$('#sp').val();
        $('input[name="SP_ID_input"]').each(function(){
            if(!$(this).val()&&val)
                $(this).val('val');
        });
    })
	
	
	
  });
});