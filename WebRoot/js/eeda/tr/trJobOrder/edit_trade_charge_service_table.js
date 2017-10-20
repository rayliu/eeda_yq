define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
$(document).ready(function() {

    var deletedTableIds=[];
    //删除一行
    $("#charge_service_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))
        
        cargoTable.row(tr).remove().draw();
    }); 
    
    itemOrder.buildTradeServiceItem=function(){
        var cargo_table_rows = $("#charge_service_table tr");
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
        // eeda.bindTableField('charge_service_table','CHARGE_ID','/finItem/search','');
        eeda.bindTableFieldChargeId('charge_service_table','CHARGE_ID','/finItem/search','');
        eeda.bindTableField('charge_service_table','SP_ID','/serviceProvider/searchCompany','');
        eeda.bindTableFieldCurrencyId('charge_service_table','CURRENCY_ID','/serviceProvider/searchCurrency','');
        eeda.bindTableFieldCurrencyId('charge_service_table','exchange_currency_id','/serviceProvider/searchCurrency','');
        eeda.bindTableField('charge_service_table','CHARGE_ENG_ID','/finItem/search_eng','');
        eeda.bindTableField('charge_service_table','UNIT_ID','/serviceProvider/searchChargeUnit','');
    };
    var cargoTable = eeda.dt({
        id: 'charge_service_table',
        autoWidth: false,
        scrollY: 530,
        drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
            bindFieldEvent();
            $.unblockUI();
        },
        columns:[
            { "width": "30px",
                "render": function ( data, type, full, meta ) {
                    return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:50px"><i class="fa fa-trash-o"></i> 删除</button></button> ';
                }
            },
            { "data": "TYPE", "width": "80px",  "visible":false,
                "render": function ( data, type, full, meta ) {
                    if(full.AUDIT_FLAG == 'Y'){
                        var str = '<select name="type" class="form-control search-control notsave" style="width:50px" disabled>'  
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
                        return '<input type="text" name="price"  value="'+str+'" class="form-control notsave" style="width:60px" disabled />';
                     }else{
                        return '<input type="text" name="price" value="'+str+'" class="form-control notsave" style="width:60px"/>';
                     }
                  }
            },
            { "data": "AMOUNT","width": "50px","className":"amount",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='1';
                    if(full.AUDIT_FLAG == 'Y'){
                        return '<input type="text" name="amount"  value="'+data+'" class="form-control notsave" style="width:60px" disabled/>';
                     }else{
                        return '<input type="text" name="amount"  value="'+data+'" class="form-control notsave" style="width:60px"/>';
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
                        var str =  parseFloat(data).toFixed(2);
                    else
                        str = '';
                    return '<input type="text" name="total_amount" style="width:80px" value="'+str+'" class="form-control notsave" disabled />';
                    
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
                        return '<input type="text" name="exchange_rate" style="width:100px" value="'+str+'" class="form-control notsave" disabled />';
                }else{
                        return '<input type="text" name="exchange_rate" style="width:100px" value="'+str+'" class="form-control notsave" />';
               }
              }
            },
            { "data": "CURRENCY_TOTAL_AMOUNT", "width": "100px","className":"cny_total_amount",
                "render": function ( data, type, full, meta ) {
                    if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                        str = '';
                    return '<input type="text" name="currency_total_amount" style="width:120px" value="'+str+'" class="form-control notsave" disabled />';
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
                        return '<input type="text" name="exchange_currency_rate" style="width:100px" value="'+str+'" class="form-control notsave" disabled />';
                    }else{
                        return '<input type="text" name="exchange_currency_rate" style="width:100px" value="'+str+'" class="form-control notsave" />';
                    }
                }
            },
            { "data": "EXCHANGE_TOTAL_AMOUNT", "width": "80px","className":"exchange_total_amount",
                "render": function ( data, type, full, meta ) {
                    if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                        str = '';
                    return '<input type="text" name="exchange_total_amount" style="width:100px" value="'+str+'" class="form-control notsave" disabled />';
                }
            },
            { "data": "EXCHANGE_CURRENCY_RATE_RMB", "width": "80px", "className":"exchange_currency_rate_rmb",
                "render": function ( data, type, full, meta ) {
                    if(data)
                        var str =  parseFloat(data).toFixed(6);
                    else
                        str = '';
                    if(full.AUDIT_FLAG == 'Y'){
                        return '<input type="text" name="exchange_currency_rate_rmb" style="width:120px" value="'+str+'" class="form-control notsave" disabled />';
                    }else{
                        return '<input type="text" name="exchange_currency_rate_rmb" style="width:120px" value="'+str+'" class="form-control notsave" />';
                    }
                }
            },
            { "data": "EXCHANGE_TOTAL_AMOUNT_RMB", "width": "100px","className":"exchange_total_amount_rmb",
                "render": function ( data, type, full, meta ) {
                    if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                        str = '';
                    return '<input type="text" name="exchange_total_amount_rmb" style="width:120px" value="'+str+'" class="form-control notsave" disabled />';
                }
            },
            { "data": "RMB_DIFFERENCE", "width": "80px","className":"rmb_difference",
                "render": function ( data, type, full, meta ) {
                    if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                        str = '0.00';
                    return '<input type="text" name="rmb_difference" style="width:100px" value="'+str+'" class="form-control notsave" disabled />';
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

    $('#add_charge_service_table').on('click', function(){
        if($('#rateExpired').val()=='Y'){
            $.scojs_message('当前汇率已过期，请更新汇率才能进行添加费用', $.scojs_message.TYPE_ERROR);
            return;
        }
        var item={};
        cargoTable.row.add(item).draw(true);
    });
    
    //获取回填到下表的汇率
    $('#service_currency_id_list').on('mousedown','a',function(){
          $('#service_currency_rate').val( $(this).attr('rate'));
       if($('#service_exchange_currency_input').val()==''){
          $('#service_exchange_currency_input').val($(this).text());
          $('#service_exchange_currency').val($(this).attr('id'));
       }
       if($('#service_exchange_currency_rate').val()==''){
          $('#service_exchange_currency_rate').val($(this).attr('rate'));
       }
    });
    
    
    //刷新明细表
    itemOrder.refleshTradeServiceItemTable = function(order_id){
        var url = "/trJobOrder/tableList?order_id="+order_id+"&type=trade_service";
        cargoTable.ajax.url(url).load();
    }
    
    if($('#charge_service_table td').length>1){
        var col = [6,11,,16,17];
        for (var i=0;i<col.length;i++){
            var arr = cargoTable.column(col[i]).data();
            $('#charge_service_table tfoot').find('th').eq(col[i]).html(
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

    $('#charge_service_table').on('keyup','[name=price],[name=amount],[name=exchange_rate],[name=exchange_currency_rate],[name=exchange_currency_rate_rmb]', function(){
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
        var amount = 0;
        $('#charge_service_table [name=amount]').each(function(){
            var a = this.value;
            if(a!=''&&!isNaN(a)){
            	amount+=parseFloat(a);
            }
        })
        $($('.dataTables_scrollFoot tr')[1]).find('.amount').html(amount.toFixed(2));
        

        var total_cny_total_amount = 0;
        $('#charge_service_table [name=currency_total_amount]').each(function(){
            var a = this.value;
            if(a!=''&&!isNaN(a)){
                total_cny_total_amount+=parseFloat(a);
            }
        })
        $($('.dataTables_scrollFoot tr')[1]).find('.cny_total_amount').html(total_cny_total_amount.toFixed(3));
        
        var total_exchange_total_amount_rmb = 0;
        $('#charge_service_table [name=exchange_total_amount_rmb]').each(function(){
            var a = this.value;
            if(a!=''&&!isNaN(a)){
                total_exchange_total_amount_rmb+=parseFloat(a);
            }
        })
        $($('.dataTables_scrollFoot tr')[1]).find('.exchange_total_amount_rmb').html(total_exchange_total_amount_rmb.toFixed(3));
        
        var total_rmb_difference = 0;
        $('#charge_service_table [name=rmb_difference]').each(function(){
            var a = this.value;
            if(a!=''&&!isNaN(a)){
                total_rmb_difference+=parseFloat(a);
            }
        })
        $($('.dataTables_scrollFoot tr')[1]).find('.rmb_difference').html(total_rmb_difference.toFixed(3));
        
        
        
        $("#trade_cost_table [name=number]").each(function(){
            $(this).keyup();
        });
        itemOrder.count_difference();
        
        $("#trade_cost_table [name=tax_refund_rate_customer]").each(function(){
            $(this).blur();
        });
    })

           //贸易常用模板
      //------------------费用明细
    $('#collapseChargeServiceInfo').on('show.bs.collapse', function () {
        var thisType = $(this).attr('id');
        var type = 'Charge';
        if('collapseChargeServiceInfo'!=thisType){
            type='Cost';
        }
        var div = $('#'+type+'ServiceDiv').empty();
        $('#collapse'+type+'ServiceIcon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
        var order_type = $('#type').val();
        var customer_id = $('#customer_id').val();
        if(order_type.trim() == '' || customer_id == ''){
            $.scojs_message('请先选择类型和客户', $.scojs_message.TYPE_ERROR);
            return
        }else{
            $.post('/trJobOrder/getTradeServiceTemplate', {order_type:order_type,customer_id:customer_id,arap_type:type}, function(data){
                if(data){
                    for(var i = 0;i<data.length;i++){
                        var json_obj = JSON.parse(data[i].JSON_VALUE);
                        var li = '';
                        var li_val = '';
                        for(var j = 0;j<json_obj.length;j++){
                            li +='<li '
                                +' sp_name="'+json_obj[j].sp_name+'" '
                                +'charge_eng_id="'+json_obj[j].CHARGE_ENG_ID+'" '
                                +'charge_id="'+json_obj[j].CHARGE_ID+'" '
                                +'currency_id="'+json_obj[j].CURRENCY_ID+'" '
                                +'sp_id="'+json_obj[j].SP_ID+'" '
                                +'unit_id="'+json_obj[j].UNIT_ID+'" '
                                +'amount="'+json_obj[j].amount+'" '
                                +'charge_name="'+json_obj[j].charge_name+'" '
                                +'charge_name_eng="'+json_obj[j].charge_eng_name+'" '
                                +'currency_name="'+json_obj[j].currency_name+'" '
                                +'currency_total_amount="'+json_obj[j].currency_total_amount+'" '
                                +'exchange_currency_id="'+json_obj[j].exchange_currency_id+'" '
                                +'exchange_currency_name="'+json_obj[j].exchange_currency_name+'" '
                                +'exchange_currency_rate="'+json_obj[j].exchange_currency_rate+'" '
                                +'exchange_rate="'+json_obj[j].exchange_rate+'" '
                                +'exchange_total_amount="'+json_obj[j].exchange_total_amount+'" '
                                +'exchange_currency_rate_rmb="'+json_obj[j].exchange_currency_rate_rmb+'" '
                                +'exchange_total_amount_rmb="'+json_obj[j].exchange_total_amount_rmb+'" '
                                +'rmb_difference="'+json_obj[j].rmb_difference+'" '
                                +'order_type="'+json_obj[j].order_type+'" '
                                +'price="'+json_obj[j].price+'" '
                                +'remark="'+json_obj[j].remark+'" '
                                +'total_amount="'+json_obj[j].total_amount+'" '
                                +'type="'+json_obj[j].type+'" '
                                +'unit_name="'+json_obj[j].unit_name+'" '
                                +'></li>';
                            li_val += '<span></span> '+json_obj[j].sp_name+' , '+json_obj[j].charge_name+' , '+json_obj[j].total_amount+' , '+json_obj[j].currency_name+'<br/>';
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
 
    $('#collapseChargeServiceInfo').on('hide.bs.collapse', function () {
        var thisType = $(this).attr('id');
        var type = 'Charge';
        if('collapseChargeServiceInfo'!=thisType){
            type='Cost';
        }
        $('#collapse'+type+'ServiceIcon').removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
    });
  
    $('#collapseChargeServiceInfo').on('click', '.deleteChargeTemplate,.deleteCostTemplate', function(){
        $(this).attr('disabled', true);
        var ul = $(this).parent().parent();
        var id = ul.attr('id');
        $.post('/trJobOrder/deleteTradeServiceTemplate', {id:id}, function(data){
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
    $('#ChargeServiceDiv').on('click', '.selectChargeTemplate,.selectCostTemplate', function(){
        $(this).parent().find('[type=radio]').prop('checked',true)
        
        var thisType = $(this).attr('class');
        var type = 'Charge';
        var table = 'charge_service_table';
        // if('selectChargeTemplate'!=thisType){
        //     type='Cost';
        //     table='trade_sale_table';
        // }
        
        var li = $(this).parent().parent().find('li');
        var dataTable = $('#'+table).DataTable();
        
         for(var i=0; i<li.length; i++){
            var row = $(li[i]);
            var item={};
            item.ID='';
            item.TYPE=row.attr('type');
            item.SP_ID=row.attr('sp_id');
            item.CHARGE_ID= row.attr('charge_id');
            item.CHARGE_ENG_ID= row.attr('charge_eng_id');
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
            item.EXCHANGE_CURRENCY_RATE_RMB= row.attr('exchange_currency_rate_rmb');
            item.EXCHANGE_TOTAL_AMOUNT_RMB= row.attr('exchange_total_amount_rmb');
            item.RMB_DIFFERENCE= row.attr('rmb_difference');
            item.REMARK= row.attr('remark');
            item.SP_NAME=row.attr('sp_name');
            item.CHARGE_NAME=row.attr('charge_name');
            item.CHARGE_NAME_ENG=row.attr('charge_name_eng');
            item.UNIT_NAME=row.attr('unit_name');
            item.CURRENCY_NAME=row.attr('currency_name');
            item.EXCHANGE_CURRENCY_ID_NAME=row.attr('exchange_currency_name');
            item.AUDIT_FLAG='';
            dataTable.row.add(item).draw();
            var row = $("#charge_service_table tbody tr");
            total_calculation(row);
        }
    });
    
    var total_calculation = function(row){
 	   var amount = 0;
 	   var total_cny_total_amount = 0;
 	   var total_exchange_total_amount_rmb = 0;
 	   var total_rmb_difference = 0;
 	   
 	   for(var i = 0;i<row.length;i++){
 		   amount += row.eq(i).find('[name=amount]').val()*1;
 		   total_cny_total_amount += row.eq(i).find('[name=currency_total_amount]').val()*1;
 		   total_exchange_total_amount_rmb += row.eq(i).find('[name=exchange_total_amount_rmb]').val()*1;
 		   total_rmb_difference += row.eq(i).find('[name=rmb_difference]').val()*1;
 	   }
 	   
 	   $($('.dataTables_scrollFoot tr')[1]).find('.amount').html(amount.toFixed(1));
 	   $($('.dataTables_scrollFoot tr')[1]).find('.cny_total_amount').html(total_cny_total_amount.toFixed(3));
 	   $($('.dataTables_scrollFoot tr')[1]).find('.rmb_difference').html(total_rmb_difference.toFixed(3));
       $($('.dataTables_scrollFoot tr')[1]).find('.exchange_total_amount_rmb').html(total_exchange_total_amount_rmb.toFixed(3));
    }
   
    itemOrder.buildChargeServiceTemplate=function(){
        var cargo_table_rows = $("#charge_service_table tr");
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
                        if(name=='rmb_difference'&&el.val()==''){
                        el.val(0.00);
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

     itemOrder.buildAllChargeServiceTemplate=function(){
        var cargo_table_rows = $("#charge_service_table tr");
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
    
  //保存应收服务费用模板
    $('#chargeServiceBtnTemplet').click(function(){
    	var chargeServiceOrderTemplet={};
    	chargeServiceOrderTemplet.order_type = $('#type').val();
    	chargeServiceOrderTemplet.customer_id = $('#customer_id').val();
    	chargeServiceOrderTemplet.chargeService_template = itemOrder.buildChargeServiceTemplate();
    	chargeServiceOrderTemplet.allChargeService_template = itemOrder.buildAllChargeServiceTemplate();
    	$.post('/trJobOrder/chargeServiceTemplet',{params:JSON.stringify(chargeServiceOrderTemplet)},function(data){
    		if(data){
    			$.scojs_message('应收服务费用模板保存成功', $.scojs_message.TYPE_OK);
    		}else{
    			$.scojs_message('应收服务费用模板保存失败', $.scojs_message.TYPE_ERROR);
    		}
    	});
    });
    
    //校验
    $('#charge_service_table').on("blur","[name=price],[name=amount],[name=exchange_rate],[name=exchange_currency_rate],[name=exchange_currency_rate_rmb],"
		    		+"[name=remark]",function(){
		var data = $(this).val();
		var name = $(this).attr("name");
		var len = $.trim(data).length;
		if(name=="amount"){
			var re = /^\d{0,8}(\d{1}\.\d{1,3})?$/g;
			if(!re.test(data)&&len!=0){
				$(this).parent().append("<span style='color:red;' class='error_span'>请输入合法的数字</span>");
				return;
			}
		}
		if(name=="exchange_rate"||name=="exchange_currency_rate"||name=="exchange_currency_rate_rmb"||name=="price"){
			var re = /^\d{0,2}(\d{1}\.\d{1,6})?$/g;
			if(!re.test(data)&&len!=0){
				$(this).parent().append("<span style='color:red;' class='error_span'>请输入合法的数字</span>");
				return;
			}
		}
		if(name=="remark"){
			var re = /^.{500,}$/g;
			if(re.test(data)&&len>0){
				$(this).parent().append("<span style='color:red;' class='error_span'>请输入长度500以内的字符串</span>");
				return;
			}
		}
		
	});
    $('#charge_service_table').on("focus","[name=price],[name=amount],[name=exchange_rate],[name=exchange_currency_rate],[name=exchange_currency_rate_rmb],"
    		+"[name=remark]",function(){
		$(this).parent().find("span").remove();
	});
});
});