define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
    $(document).ready(function() {
    	
    	itemOrder.buildTradeDetail=function(){
    		var arrays = [];
        	var item = {};
        	item['id'] = $('#trade_id').val();
        	var shipmentForm = $('#tradeForm input,#tradeForm select,#tradeForm textarea');
        	for(var i = 0; i < shipmentForm.length; i++){
        		var name = shipmentForm[i].id;
            	var value =shipmentForm[i].value;
            	if(name){
            			item[name] = value;
            	}
        	}
        	arrays.push(item);
            return arrays;
        };
        
       $('#cost_currency_list').on('mousedown','a',function(){
    	   $('#cost_currency_rate').val( $(this).attr('rate'));
       });
       
        var clearOrderToLocalstorage=function(order_id){

            if(!!window.localStorage){//查询条件处理
                var err_temp_job_order_str = localStorage.getItem('err_temp_job_order_'+order_id);
                if(!err_temp_job_order_str)
                    return;
                //删除数据缓存
                localStorage.removeItem('err_temp_job_order_'+order_id);
            }
        };

        $("#trade_recover_no_need").click(function(event) {
            event.preventDefault();
            $('#trade_recover_div').hide();
            var order_id = $('#order_id').val();
            clearOrderToLocalstorage(order_id);
        });

        $("#trade_recover").click(function(event) {
            event.preventDefault();
            $('#trade_recover_div').hide();
            var order_id = $('#order_id').val();
            if(!!window.localStorage){//查询条件处理
                var err_temp_job_order_str = localStorage.getItem('err_temp_job_order_'+order_id);
                if(!err_temp_job_order_str)
                    return;
                
                order = $.parseJSON(err_temp_job_order_str);
                recover_trade_detail(order);//form input
                recover_trade_cost(order);//商品信息table
                recover_trade_service_charge(order);//服务费用table
                recover_trade_sale(order);//销售应收费用table
                //删除数据缓存
                clearOrderToLocalstorage(order_id);
            }
        });

        var recover_trade_detail=function(order){
            var trade_detail = order.trade_detail[0];
            var trade_detail_form = $('#tradeForm input,#tradeForm select,#tradeForm textarea');
            for(var i = 0; i < trade_detail_form.length; i++){
                var name = trade_detail_form[i].id;
                var value =trade_detail[name];
                if(value){
                    trade_detail_form[i].value = value;
                }
            }
        };

        var recover_trade_cost=function(order){
            var trade_cost_list = order.trade_cost;
            var new_trade_cost_list = [];
            $.each(trade_cost_list, function(index, row) {
                for(var attr in row){
                    var name = attr.toUpperCase().replace('_INPUT', '_NAME');
                    row[name] = row[attr];
                }
                if(row.action != 'DELETE'){
                    new_trade_cost_list.push(row);
                }
            });
            var trade_cost_table = $('#trade_cost_table').DataTable();
            trade_cost_table.clear().rows.add(new_trade_cost_list).draw();
        };

        var recover_trade_service_charge=function(order){
            var trade_service_charge_list = order.trade_service;
            var new_trade_service_charge_list = [];
            $.each(trade_service_charge_list, function(index, row) {
                for(var attr in row){
                    var name = attr.toUpperCase().replace('_INPUT', '_NAME');
                    row[name] = row[attr];
                }
                if(row.action != 'DELETE'){
                    new_trade_service_charge_list.push(row);
                }
            });
            var charge_service_table = $('#charge_service_table').DataTable();
            charge_service_table.clear().rows.add(new_trade_service_charge_list).draw();
        };

        var recover_trade_sale=function(order){
            var trade_cost_list = order.trade_sale;
            var new_trade_cost_list = [];
            $.each(trade_cost_list, function(index, row) {
                for(var attr in row){
                    var name = attr.toUpperCase().replace('_INPUT', '_NAME');
                    row[name] = row[attr];
                }
                if(row.action != 'DELETE'){
                    new_trade_cost_list.push(row);
                }
            });
            var trade_cost_table = $('#trade_sale_table').DataTable();
            trade_cost_table.clear().rows.add(new_trade_cost_list).draw();
        }

    //贸易常用模板
      //------------------费用明细
    $('#collapseChargeServiceInfo,#collapseChargeTradeSaleInfo').on('show.bs.collapse', function () {
        var thisType = $(this).attr('id');
        var type = 'Service';
        if('collapseChargeServiceInfo'!=thisType){
            type='TradeSale';
        }
        var div = $('#'+type+'Div').empty();
        $('#collapse'+type+'Icon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
        var order_type = $('#type').val();
        var customer_id = $('#customer_id').val();
        if(order_type.trim() == '' || customer_id == ''){
            $.scojs_message('请先选择类型和客户', $.scojs_message.TYPE_ERROR);
            return
        }else{
            $.post('/jobOrder/getTradeArapTemplate', {order_type:order_type,customer_id:customer_id,arap_type:type}, function(data){
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
                                +'order_type="'+json_obj[j].order_type+'" '
                                +'price="'+json_obj[j].price+'" '
                                +'remark="'+json_obj[j].remark+'" '
                                +'total_amount="'+json_obj[j].total_amount+'" '
                                +'type="'+json_obj[j].type+'" '
                                +'unit_name="'+json_obj[j].unit_name+'" '
                                +'></li>';
                            li_val += '<span></span> '+json_obj[j].sp_name+' , '+json_obj[j].charge_name+' , '+json_obj[j].charge_eng_name+'<br/>';
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
 
    $('#collapseChargeServiceInfo,#collapseChargeTradeSaleInfo').on('hide.bs.collapse', function () {
        var thisType = $(this).attr('id');
        var type = 'ChargeService';
        if('collapseChargeInfo'!=thisType){
            type='ChargeTradeSale';
        }
        $('#collapse'+type+'Icon').removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
    });
  
    $('#ChargeServiceDiv,#ChargeTradeSaleDiv').on('click', '.deleteChargeTemplate,.deleteCostTemplate', function(){
        $(this).attr('disabled', true);
        var ul = $(this).parent().parent();
        var id = ul.attr('id');
        $.post('/jobOrder/deleteArapTemplate', {id:id}, function(data){
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
    $('#ChargeServiceDiv,#ChargeTradeSaleDiv').on('click', '.selectChargeTemplate,.selectCostTemplate', function(){
        $(this).parent().find('[type=radio]').prop('checked',true)
        
        var thisType = $(this).attr('class');
        var type = 'ChargeService';
        var table = 'charge_service_table';
        if('selectChargeTemplate'!=thisType){
            type='ChargeTradeSale';
            table='trade_sale_table';
        }
        
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
            item.REMARK= row.attr('remark');
            item.SP_NAME=row.attr('sp_name');
            item.CHARGE_NAME=row.attr('charge_name');
            item.CHARGE_NAME_ENG=row.attr('charge_name_eng');
            item.UNIT_NAME=row.attr('unit_name');
            item.CURRENCY_NAME=row.attr('currency_name');
            item.EXCHANGE_CURRENCY_ID_NAME=row.attr('exchange_currency_name');
            item.AUDIT_FLAG='';
            dataTable.row.add(item).draw();
        }
    });
    });
});