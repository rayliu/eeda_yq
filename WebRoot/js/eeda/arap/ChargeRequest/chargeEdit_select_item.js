define(['jquery','metisMenu', 'sb_admin', 'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
    

        var itemTable = eeda.dt({
            id: 'select_item_table',
            initComplete: function( settings ) {
                ids = [];
                cnames = [];
            },
            columns:[
                { "data": "ID", visible: false},
                { "data": null,
                    "render": function ( data, type, full, meta ) {
                        var str = '<input id="checkbox_'+full.ID+'" type="checkbox" style="width:30px" checked>';
                        return str;
                    }
                },
                { "data": "CHECK_ORDER_NO"},
                { "data": "ORDER_NO"},
                { "data": "TYPE"},
                { "data": "CREATE_STAMP", visible: false},
                { "data": "CUSTOMER_NAME"},
                { "data": "SP_NAME"},
                { "data": "FIN_NAME"},
                { "data": "CURRENCY_NAME","class":"currency_name"},
                { "data": "TOTAL_AMOUNT","class":"total_amount", 
                    "render": function ( data, type, full, meta ) {
                    	var str =  parseFloat(data).toFixed(2);
                        if(full.ORDER_TYPE=='cost'){
                            return '<span style="color:red;">'+'-'+str+'</span>';
                        }
                        return str;
                      }
                },
                { "data": "EXCHANGE_RATE", "visible": false},
                { "data": "AFTER_TOTAL", "visible": false, 
                    "render": function ( data, type, full, meta ) {
                    	var after_str =  parseFloat(data).toFixed(2);
                        if(full.ORDER_TYPE=='cost'){
                            return '<span style="color:red;">'+'-'+after_str+'</span>';
                        }
                        return after_str;
                      }
                },
                { "data": "NEW_RATE","class":"new_rate", "visible": false },
                { "data": "AFTER_RATE_TOTAL","class":"after_rate_total", "visible": false,
                    "render": function ( data, type, full, meta ) {
                        if(full.ORDER_TYPE=='cost'){
                            return '<span style="color:red;">'+'-'+data+'</span>';
                        }
                        return data;
                      }
                },
                { "data": "EXCHANGE_CURRENCY_NAME"},
                { "data": "EXCHANGE_CURRENCY_RATE"},
                { "data": "EXCHANGE_TOTAL_AMOUNT",
                    "render": function ( data, type, full, meta ) {
                    	var exchange_total_str = parseFloat(data).toFixed(2);
                        if(full.ORDER_TYPE=='cost'){
                            return '<span style="color:red;">'+'-'+exchange_total_str+'</span>';
                        }
                        return exchange_total_str;
                      }
                },
                { "data": "ORDER_TYPE", "visible": false,
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                }
            ]
        });

        var refleshSelectTable = function(order_ids, ids){
            //ids为选中的item id
            var callback=function(){
                if(ids){
                    //清除标记位
                    $('#select_item_table input[type=checkbox]').prop('checked', false);
                    itemTable.data().each(function(item, index) {
                        var cur_id = item.ID;
                        if(!$.isArray(ids))
                            ids = ids.split(',');
                        var $checkbox = $('#checkbox_'+cur_id);
                        var cell = itemTable.cell($checkbox.parent());//  td
                        cell.data('N').draw();//清除标记位
                        $.each(ids, function(i, id) {
                            var $checkbox = $('#checkbox_'+id);
                            //注意 - call draw() 更新table.data()中的数据
                            if(cur_id==id){
                                cell.data('Y').draw();
                            }

                            //UI checkbox改变
                            $checkbox.prop('checked', true);
                        });
                    });
                }
                calcTotal();
                
                var selected_ids=[];
                $('#select_item_table input[type="checkbox"]:checked').each(function(){
          			var selectId = $(this).parent().parent().attr('id');
          			selected_ids.push(selectId);
                });
          		$('#selected_ids').val(selected_ids);
                
            };
            	var url = "/chargeCheckOrder/tableList?order_ids="+order_ids+"&order_id=N";
                itemTable.ajax.url(url).load(callback);
        };
        
        var calcTotal=function() {
            //$("#ChargeOrder-table").DataTable()
            var CNY_cost=0, CNY_charge=0;
            var USD_cost=0, USD_charge=0;
            var HKD_cost=0, HKD_charge=0;
            var JPY_cost=0, JPY_charge=0;

            //data table data 对象
            itemTable.data().each(function(item, index) {
                //dom 对象的checkbox 是否勾上了？
                var id = item.ID;

                if(!$('#checkbox_'+id).prop('checked'))
                    return;

                if(item.ORDER_TYPE == 'cost'){
                    if(item.EXCHANGE_CURRENCY_NAME=='CNY'){
                        CNY_cost+=item.EXCHANGE_TOTAL_AMOUNT;
                    }else if(item.EXCHANGE_CURRENCY_NAME=='USD'){
                        USD_cost+=item.EXCHANGE_TOTAL_AMOUNT;
                    }else if(item.EXCHANGE_CURRENCY_NAME=='HKD'){
                        HKD_cost+=item.EXCHANGE_TOTAL_AMOUNT;
                    }else if(item.EXCHANGE_CURRENCY_NAME=='JPY'){
                        JPY_cost+=item.EXCHANGE_TOTAL_AMOUNT;
                    }
                }else{
                    if(item.EXCHANGE_CURRENCY_NAME=='CNY'){
                        CNY_charge+=item.EXCHANGE_TOTAL_AMOUNT;
                    }else if(item.EXCHANGE_CURRENCY_NAME=='USD'){
                        USD_charge+=item.EXCHANGE_TOTAL_AMOUNT;
                    }else if(item.EXCHANGE_CURRENCY_NAME=='HKD'){
                        HKD_charge+=item.EXCHANGE_TOTAL_AMOUNT;
                    }else if(item.EXCHANGE_CURRENCY_NAME=='JPY'){
                        JPY_charge+=item.EXCHANGE_TOTAL_AMOUNT;
                    }
                }
            });
            $('#modal_cny').val((parseFloat(CNY_charge - CNY_cost)).toFixed(2));
            $('#modal_usd').val((parseFloat(USD_charge - USD_cost)).toFixed(2));
            $('#modal_hkd').val((parseFloat(HKD_charge - HKD_cost)).toFixed(2));
            $('#modal_jpy').val((parseFloat(JPY_charge - JPY_cost)).toFixed(2));

        }
        
        
        
        var refleshCreateTable = function(appApplication_id){
    		var url = "/chargeCheckOrder/tableList?appApplication_id="+appApplication_id+"&order_id=N&bill_flag=create";
            itemTable.ajax.url(url).load();
		    };
		    


    return {
        refleshSelectTable: refleshSelectTable,
        refleshCreateTable:refleshCreateTable,
        calcTotal: calcTotal
    };

});