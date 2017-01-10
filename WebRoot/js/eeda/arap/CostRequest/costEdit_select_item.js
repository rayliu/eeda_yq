define(['jquery', 'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
    

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
                        str = '<input id="checkbox_'+full.ID+'" type="checkbox" style="width:30px" checked>';
                        return str;
                    }
                },
                { "data": "CHECK_ORDER_NO"},
                { "data": "ORDER_NO"},
                { "data": "TYPE"},
                { "data": "CREATE_STAMP", visible: false},
                { "data": "CUSTOMER_NAME"},
                { "data": "SP_NAME"},
                { "data":"FIN_NAME"},
                { "data": "CURRENCY_NAME","class":"currency_name"},
                { "data": "TOTAL_AMOUNT","class":"total_amount", 
                    "render": function ( data, type, full, meta ) {
                    	var str =  parseFloat(data).toFixed(2);
                        if(full.ORDER_TYPE=='charge'){
                            return '<span style="color:red;">'+'-'+str+'</span>';
                        }
                        return str;
                      }
                },
                { "data": "EXCHANGE_RATE", "visible": false},
                { "data": "AFTER_TOTAL", "visible": false, 
                    "render": function ( data, type, full, meta ) {
                    	var after_str =  parseFloat(data).toFixed(2);
                        if(full.ORDER_TYPE=='charge'){
                            return '<span style="color:red;">'+'-'+after_str+'</span>';
                        }
                        return after_str;
                      }
                },
                { "data": "NEW_RATE","class":"new_rate", "visible": false },
                { "data": "AFTER_RATE_TOTAL","class":"after_rate_total", "visible": false,
                    "render": function ( data, type, full, meta ) {
                        if(full.ORDER_TYPE=='charge'){
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
                        if(full.ORDER_TYPE=='charge'){
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
            	var url = "/costCheckOrder/tableList?order_ids="+order_ids+"&order_id=N";
                itemTable.ajax.url(url).load(callback);
        };
        
        var calcTotal=function() {
            //$("#costOrder-table").DataTable()
            var CNY_cost=0, CNY_charge=0;
            var USD_cost=0, USD_charge=0;
            var HKD_cost=0, HKD_charge=0;
            var JPY_cost=0, JPY_charge=0;
            itemTable.data().each(function(item, index) {
                if(!$('#checkbox_'+item.ID).prop('checked'))
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
            $('#modal_cny').val((parseFloat(CNY_cost - CNY_charge)).toFixed(2));
            $('#modal_usd').val((parseFloat(USD_cost - USD_charge)).toFixed(2));
            $('#modal_hkd').val((parseFloat(HKD_cost - HKD_charge)).toFixed(2));
            $('#modal_jpy').val((parseFloat(JPY_cost - JPY_charge)).toFixed(2));

        }
        //查询选中币种
        $('#query_listCurrency').click(function(){
            searchData2(); 
        });

        var searchData2=function(){
            var ids=$('#ids').val();
            var query_currency=$('#query_currency').val();
            var url = "/costCheckOrder/tableList?order_ids="+ids+"&order_id=N"
                            +"&table_type=item"
                            +"&query_currency="+query_currency;
           itemTable.ajax.url(url).load(function(){
              var a=[];
              $('#select_item_table input[type=checkbox]:checked').each(function(){
                    var id=$(this).parent().parent().attr('id');
                     a.push(id);
              }); 
              $('#selected_ids').val(a);
           });
         };

         
        
        var refleshCreateTable = function(appApplication_id){
    		var url = "/costCheckOrder/tableList?appApplication_id="+appApplication_id+"&order_id=N&bill_flag=create";
            itemTable.ajax.url(url).load();
		    };

		  //全选
        $('#coR_allcheck').on('click',function(){
             var table = $('#select_item_table').DataTable();
            
           
            var selected_ids=[];
            if($('#coR_allcheck').prop("checked")){
                  table.data().each(function(item, index) {

                      selected_ids.push(item.ID);
                    });
                 $('#select_item_table input[type="checkbox"]').prop('checked',true);   
            }else{
                selected_ids=[];
                $('#select_item_table input[type="checkbox"]').prop('checked',false);
            }
             // selectContr.calcTotal();

             $('#selected_ids').val(selected_ids);
        });


    return {
        refleshSelectTable: refleshSelectTable,
        refleshCreateTable:refleshCreateTable,
        calcTotal: calcTotal
    };

});