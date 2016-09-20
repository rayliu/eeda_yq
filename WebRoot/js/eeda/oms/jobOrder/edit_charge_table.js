define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
    window.calcCurrency =  function (){
        var chargeRMB = $('[name=chargeRMB]').text().replace('RMB','');
        var costRMB = $('[name=costRMB]').text().replace('RMB','');
        var profitRMB = parseFloat(chargeRMB)-parseFloat(costRMB);
           if(profitRMB>=0){
              $('[name=profitRMB]').text(profitRMB+"RMB");
           }else if(profitRMB<0){
              $('[name=profitRMB]').html("<span style='color:red'>"+profitRMB+"RMB</span>");
           } else{
              $('[name=profitRMB]').text(0+"RMB");
           }

        var chargeUSD = $('[name=chargeUSD]').text().replace('RMB','');
        var costUSD = $('[name=costUSD]').text().replace('RMB','');
        var profitUSD = parseFloat(chargeUSD)-parseFloat(costUSD);
           if(profitUSD>=0){
              $('[name=profitUSD]').text(profitUSD+"USD");
           }else if(profitUSD<0){
              $('[name=profitUSD]').html("<span style='color:red'>"+profitUSD+"USD</span>");
           } else{
              $('[name=profitUSD]').text(0+"USD");
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
        $('[name=profitTotalRMB]').text(profitTotalRMB+"RMB");
        }else if(profitTotalRMB<0){
            $('[name=profitTotalRMB]').html("<span style='color:red'>"+profitTotalRMB+"RMB</span>");
        } else{
            $('[name=profitTotalRMB]').text(0+"RMB");
        }
    };

$(document).ready(function() {
    

	var deletedTableIds=[];
	
    //删除一行
    $("#charge_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))
        
        chargeTable.row(tr).remove().draw();
    }); 
    //添加一行
    $('#add_charge').on('click', function(){
        var item={};
        chargeTable.row.add(item).draw(true);
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
    	eeda.bindTableField('SP_ID','/serviceProvider/searchCompany','');
        eeda.bindTableField('CHARGE_ID','/finItem/search','');
        eeda.bindTableField('UNIT_ID','/serviceProvider/searchUnit','');
        eeda.bindTableField('CURRENCY_ID','/serviceProvider/searchCurrency','');
    };

    
    //------------事件处理
    var chargeTable = eeda.dt({
        id: 'charge_table',
        autoWidth: false,
        drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
            bindFieldEvent();
        },
        columns:[
            { "width": "30px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		return '<button type="button" class="delete btn btn-default btn-xs" style="width:50px" disabled>删除</button> ';
                	}else{
                		return '<button type="button" class="delete btn btn-default btn-xs" style="width:50px">删除</button> ';
                	}
                }
            },
            { "data": "TYPE", "width": "80px", 
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		var str = '<select name="type" class="form-control search-control" style="width:100px" disabled>'
	                        +'<option value="海运" '+(data=='海运' ? 'selected':'')+'>海运</option>'
	                        +'<option value="空运" '+(data=='空运' ? 'selected':'')+'>空运</option>'
	                        +'<option value="陆运" '+(data=='陆运' ? 'selected':'')+'>陆运</option>'
	                        +'<option value="报关" '+(data=='报关' ? 'selected':'')+'>报关</option>'
	                        +'<option value="保险" '+(data=='保险' ? 'selected':'')+'>保险</option>'
	                        +'</select>';
	                	return str;
                	}else{
                    var str = '<select name="type" class="form-control search-control" style="width:100px">'
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
            { "data": "PRICE", "width": "60px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		if(!data)
                            data='';
                        return '<input type="text" name="price" style="width:80px" value="'+data+'" class="form-control" disabled />';
                     }else{
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="price" style="width:80px" value="'+data+'" class="form-control" />';
	                 }
                  }
            },
            { "data": "AMOUNT","width": "60px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		if(!data)
                            data='';
                        return '<input type="text" name="amount" style="width:80px" value="'+data+'" class="form-control " disabled/>';
                     }else{
	                    if(!data)
	                        data='';
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
            { "data": "TOTAL_AMOUNT", "width": "60px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                        if(!data)
                            data='';
                        return '<input type="text" name="exchange_rate" style="width:80px" value="'+data+'" class="form-control" disabled/>';
                    }else{
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="total_amount" style="width:80px" value="'+data+'" class="form-control" />';
	                }
                }
            },
            { "data": "EXCHANGE_RATE", "width": "60px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                        if(!data)
                            data='';
                        return '<input type="text" name="currency_total_amount" style="width:80px" value="'+data+'" class="form-control" disabled />';
                    } else{
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="exchange_rate" style="width:80px" value="'+data+'" class="form-control"/>';
	                }
                }
            },
            { "data": "CURRENCY_TOTAL_AMOUNT", "width": "60px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                        if(!data)
                            data='';
                        return '<input type="text" name="currency_total_amount" style="width:80px" value="'+data+'" class="form-control" disabled />';
                    } else{
	                    if(!data)
	                        data='';			     		   
	                    return '<input type="text" name="currency_total_amount" style="width:80px" value="'+data+'" class="form-control" />';
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
            }, { "data": "SP_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            }, { "data": "CHARGE_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            },{ "data": "UNIT_NAME", "visible": false,
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
    itemOrder.refleshChargeTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=charge";
    	chargeTable.ajax.url(url).load();
    }
    
    //输入 数量*单价的时候，计算金额
    $('#charge_table').on('keyup','[name=price],[name=amount],[name=exchange_rate],[name=CURRENCY_ID_input]',function(){
    	var row = $(this).parent().parent();
    	var price = $(row.find('[name=price]')).val()
    	var amount = $(row.find('[name=amount]')).val()
    	var exchange_rate = $(row.find('[name=exchange_rate]')).val()
    	if(price!=''&&amount!=''&&!isNaN(price)&&!isNaN(amount)){
    		var total_amount = parseFloat(price)*parseFloat(amount);
    		$(row.find('[name=total_amount]')).val(total_amount);
    		if(exchange_rate!=''&&!isNaN(exchange_rate)){
    			
    			$(row.find('[name=currency_total_amount]')).val(total_amount*parseFloat(exchange_rate));
    			
    		}
    		getTotalCharge();
    	}
    })
    

    
	    //计算应收字段
    var getTotalCharge= function(){
    	var totalChargeRMB = 0; 
	    var totalChargeUSD = 0;
	    var profitTotalCharge = 0;
	    var tableCurCharge =$('#charge_table').find('[name=CURRENCY_ID_input]');
	    var tableAmountCharge =$('#charge_table').find('[name=total_amount]');
	    var currencyTotalAmount = $('#charge_table').find('[name=currency_total_amount]');
	    for(var i = 0;i<tableCurCharge.length;i++){
	        if(tableCurCharge[i].value=='CNY'){               
	           totalChargeRMB += parseFloat(tableAmountCharge[i].value);   //parseFloat(data)
	        }else if(tableCurCharge[i].value=='USD'){
	           totalChargeUSD += parseFloat(tableAmountCharge[i].value);
	        }
	        profitTotalCharge += parseFloat(currencyTotalAmount[i].value);
	    }
	    //隐藏字段   应收人民币汇总字段
	    $('.profitTotalCharge').text(profitTotalCharge).hide();
	    
	    if(totalChargeRMB!=""&&!isNaN(totalChargeRMB)){
	           $('.chargeRMB').text(totalChargeRMB+"RMB");  
	       }else{
	           $('.chargeRMB').text(0+"RMB");  
	        }
	    
	    if(totalChargeUSD!=""&&!isNaN(totalChargeUSD)){
	        $('.chargeUSD').text(totalChargeUSD+"USD");  
	    }else{
	        $('.chargeUSD').text(0+"USD");  
	     }
	    
	    calcCurrency();
    }
    
    
    
   // getTotalCharge();
    
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
    
    $('#print_debit_note').click(function(){
    	buildSpList();
    })
    
	     
    
  });
});