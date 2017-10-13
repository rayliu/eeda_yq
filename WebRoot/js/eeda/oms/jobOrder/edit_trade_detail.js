define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
    $(document).ready(function() {
    	
    	itemOrder.buildTradeDetail=function(){
    		var arrays = [];
        	var item = {};
        	item['id'] = $('#trade_id').val();
        	var shipmentForm = $('#tradeForm input,#tradeForm select,#tradeForm textarea,#tradeForm span');
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

        //校验
        $("#contract_number").on("blur",function(){
    		self = $(this);
    		data = self.val();
    		len = $.trim(data).length;
    		var re = /^.{255,}$/g;
    		if(re.test(data)&&len!=0){
    			self.parent().append("<span style='color:red;width:322px;margin-left:160px;' class='error_span'>请输入长度255以内的字符串</span>")
    		}
    	});
        $("#cost_currency_rate").on("blur",function(){
    		self = $(this);
    		data = $.trim(self.val());
    		var re = /^\d{0,4}(\.\d{1,5})?$/g;
    		if(!re.test(data)){
    			self.parent().append("<span style='color:red;width:322px;margin-left:160px;' class='error_span'>请输入合法数字</span>")
    		}
        });
        $("#contract_number,#cost_currency_rate").on("focus",function(){
    		self = $(this)
    		self.parent().find("span").remove()
    	})

    });
});