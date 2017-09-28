define(['jquery','metisMenu', 'template', 'sb_admin', 'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
    
var itemIds=[];

        var itemTable = eeda.dt({
            id: 'invoice_item_table',
            drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
                bindFieldEvent();
            },
            columns:[
                { "data": "ID", visible: false},
                { "data": "INV_NO",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" name="inv_no" value="'+data+'" class="form-control" style="width:300px"/>';
                    }
                },
                { "data": "AMOUNT",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" name="amount" value="'+data+'" class="form-control" style="width:150px"/>';
                    }
                },
                { "data": "CURRENCY_ID",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                       data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'CURRENCY_ID',
                               value: data,
                               display_value: full.CURRENCY_NAME,
                               style:'width:70px'
                           }
                       );
                       return field_html; 
                    }
                },
                { "data": "REMARK",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" name="remark" value="'+data+'" class="form-control" style="width:300px"/>';
                    }
                },
                { "data": "CREATOR"},
                { "data": "CREATE_STAMP"},
                { "data": "CURRENCY_NAME", visible: false}
            ]
        });

      var bindFieldEvent=function(){
          eeda.bindTableFieldCurrencyId('invoice_item_table','CURRENCY_ID','/serviceProvider/searchCurrency','');
      };

         

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
        


      //添加明细
      if($("#status").val()=='已收款' || $("#status").val()=='该笔为坏账'){
        $('#add_invoice_btn').attr('disabled',true);
      }

      //添加新的明细
      $('#add_invoice_btn').click(function(){
          itemTable.row.add({}).draw(false);
      }) 

      
      $('#add_charge_item').on('click', function(){
          insertChargeItem();
          searchData3(); 
      });

      var insertChargeItem=function(){
          var order_id=$('#order_id').val();
           var charge_itemlist=[];
          $('#eeda_charge_table input[name=order_check_box]:checked').each(function(){
                var id=$(this).val();
                charge_itemlist.push(id);
          });
          if(charge_itemlist.length==0){
            $('#add_charge_item').attr('disabled',true);
          }
          $.post('/chargeRequest/insertChargeItem',{order_id:order_id,charge_itemlist:charge_itemlist.toString()},function(data){
                refleshCreateTable(data.appOrderId);
                 $('#modal_cny').val((parseFloat(data.MODAL_CNY)).toFixed(2));
                 $('#modal_usd').val((parseFloat(data.MODAL_USD)).toFixed(2));
                 $('#modal_hkd').val((parseFloat(data.MODAL_HKD)).toFixed(2));
                 $('#modal_jpy').val((parseFloat(data.MODAL_JPY)).toFixed(2));
                 $('#add_charge_item').attr('disabled',true);
                 

          },'json').fail(function() {
               $.scojs_message('添加失败', $.scojs_message.TYPE_ERROR);
          });
      }

     
    
    return {
        refleshSelectTable:refleshSelectTable
    };

});