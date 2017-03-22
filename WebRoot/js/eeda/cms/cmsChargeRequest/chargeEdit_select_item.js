define(['jquery','metisMenu', 'sb_admin', 'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
    
var itemIds=[];

        var itemTable = eeda.dt({
            id: 'select_item_table',
            initComplete: function( settings ) {
                ids = [];
                cnames = [];
                hideColumn();
            },
            columns:[
                { "data": "ID", visible: false},
                { "data": null,
                    "render": function ( data, type, full, meta ) {
                        var str = '<input id="checkbox_'+full.ID+'" class="checkbox2" type="checkbox" style="width:30px" checked>';
                        return str;
                    }
                },
                { "data": "CHECK_ORDER_NO"},
                {"width":"30px",
                    "render": function ( data, type, full, meta ) {
                          var str = '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:50px" >删除</button>';
                          if($("#status").val()=='已复核'|| $("#status").val()=='已付款'){
                              return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:50px" disabled>删除</button>';
                          }
                          return str;
                      }
                },
                { "data": "ORDER_NO","width": "100px",
                      "render": function ( data, type, full, meta ) {
                        return data;
                    }},
                { "data": "TYPE"},
                { "data": "CREATE_STAMP", visible: false},
                { "data": "CUSTOMER_NAME"},
                { "data": "SP_NAME"},
                { "data": "FIN_NAME"},
                { "data": "AMOUNT", "width": "80px"},
                { "data": "PRICE", "width": "80px"},
                { "data": "CURRENCY_NAME","class":"currency_name"},
                { "data": "TOTAL_AMOUNT","class":"total_amount", 
                    "render": function ( data, type, full, meta ) {
                    	var str =  eeda.numFormat(parseFloat(data).toFixed(2),3);
                        if(full.ORDER_TYPE=='cost'){
                            return '<span style="color:red;">'+'-'+str+'</span>';
                        }
                        return str;
                      }
                },
              { "data": "CUSTOMS_BILLCODE", "width": "120px"},
              { "data": "CREATE_STAMP", "width": "100px"},
              { "data": "ORDER_TYPE", "visible": false,
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
              }
            ]
        });
        
        var hideColumn = function(){         	
         	//隐藏对账单号和checkBox列
            if($('#order_id').val()!=""&&$('#order_id').val()!=undefined){
            	var itemTable = $('#select_item_table').dataTable();
            	itemTable.fnSetColumnVis(1, false);
            	itemTable.fnSetColumnVis(2, false);
            }else{
            	//隐藏删除列
            	var itemTable = $('#select_item_table').dataTable();
             	itemTable.fnSetColumnVis(3, false);
            }
    	}

        

         var dataTable = eeda.dt({
            id: 'eeda_charge_table',
            // drawCallback: function( settings ) {
            //     flash();
            // },
            ajax:{
                //url: "/cmsChargeCheckOrder/list",
                type: 'POST'
            }, 
            columns:[
            { "width": "10px", "orderable": false,
            "render": function ( data, type, full, meta ) {
                  var strcheck='<input type="checkbox" class="checkBox" name="order_check_box" value="'+full.ID+'">';
                for(var i=0;i<itemIds.length;i++){
                           if(itemIds[i]==full.ID){
                             strcheck= '<input type="checkbox" class="checkBox" checked="checked"  name="order_check_box" value="'+full.ID+'">';
                           }
                       }
                return strcheck;
            }
            },
              { "data": "ORDER_NO", "width": "100px",
              "render": function ( data, type, full, meta ) {
                        return "<a href='/customPlanOrder/edit?id="+full.ORDER_ID+"'target='_blank'>"+data+"</a>";
                    }
              },
                { "data": "CPO_TYPE"},
                { "data": "CUSTOMER_NAME"},
                { "data": "SP_NAME"},
                { "data": "FIN_NAME"},
                { "data": "PRICE", "width": "80px"},
                { "data": "AMOUNT", "width": "80px"},
                { "data": "CURRENCY_NAME","class":"currency_name"},
                { "data": "TOTAL_AMOUNT","class":"total_amount", 
                    "render": function ( data, type, full, meta ) {
                      var str =  eeda.numFormat(parseFloat(data).toFixed(2),3);
                        if(full.ORDER_TYPE=='cost'){
                            return '<span style="color:red;">'+'-'+str+'</span>';
                        }
                        return str;
                      }
                },
              { "data": "CUSTOMS_BILLCODE", "width": "120px"},
              { "data": "CREATE_STAMP", "width": "100px"},
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
            	var url = "/cmsChargeCheckOrder/tableList?order_ids="+order_ids+"&order_id=N";
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
                    if(item.CURRENCY_NAME=='CNY'){
                        CNY_cost+=item.TOTAL_AMOUNT;
                    }else if(item.EXCHANGE_CURRENCY_NAME=='USD'){
                        USD_cost+=item.EXCHANGE_TOTAL_AMOUNT;
                    }else if(item.EXCHANGE_CURRENCY_NAME=='HKD'){
                        HKD_cost+=item.EXCHANGE_TOTAL_AMOUNT;
                    }else if(item.EXCHANGE_CURRENCY_NAME=='JPY'){
                        JPY_cost+=item.EXCHANGE_TOTAL_AMOUNT;
                    }
                }else{
                    if(item.CURRENCY_NAME=='CNY'){
                        CNY_charge+=item.TOTAL_AMOUNT;
                    }else if(item.EXCHANGE_CURRENCY_NAME=='USD'){
                        USD_charge+=item.EXCHANGE_TOTAL_AMOUNT;
                    }else if(item.EXCHANGE_CURRENCY_NAME=='HKD'){
                        HKD_charge+=item.EXCHANGE_TOTAL_AMOUNT;
                    }else if(item.EXCHANGE_CURRENCY_NAME=='JPY'){
                        JPY_charge+=item.EXCHANGE_TOTAL_AMOUNT;
                    }
                }
            });
            $('#modal_cny').val(eeda.numFormat(parseFloat((CNY_charge - CNY_cost)).toFixed(2),3));
            $('#modal_usd').val((parseFloat(USD_charge - USD_cost)).toFixed(2));
            $('#modal_hkd').val((parseFloat(HKD_charge - HKD_cost)).toFixed(2));
            $('#modal_jpy').val((parseFloat(JPY_charge - JPY_cost)).toFixed(2));

        }
        
         //查询选中币种
        $('#query_listCurrency').click(function(){
            searchData2(); 
        });

        var searchData2=function(){
            var ids=$('#ids').val();
            var query_exchange_currency=$('#query_currency').val();
            var fin_name=$('#query_fin').val();
            var url = "/cmsChargeCheckOrder/tableList?order_ids="+ids+"&order_id=N"
                            +"&table_type=item"
                            +"&query_exchange_currency="+query_exchange_currency
                            +"&query_fin_name="+fin_name;
           itemTable.ajax.url(url).load(function(){
              var a=[];
              $('#select_item_table input[type=checkbox]:checked').each(function(){
                    var id=$(this).parent().parent().attr('id');
                     a.push(id);
              }); 
              $('#selected_ids').val(a);
              calcTotal();
           });
         };
        

         //添加明细
      if($("#status").val()=='已复核' || $("#status").val()=='已收款'){
        $('#add_charge').attr('disabled',true);
      }

      $('#add_charge').click(function(){
            $('#allCharge').prop('checked',false);
            $('#add_charge_item').prop('disabled',true);
            $('#charge_table_msg_btn').click();
             $('#searchBtn2').click();
             
        }) 
      $('#resetBtn2').click(function(e){
          $('#que_sp_input').val('');
          $('#que_order_no').val('');
          $('#que_order_export_date_begin_time').val('');
          $('#que_order_export_date_end_time').val('');
          $('#que_customer_input').val('');
      });
         

      $('#searchBtn2').click(function(){
          searchData3(); 
      });

     var searchData3=function(){
          var checked = '';
           if($('#checkOrderAll').prop('checked')==true){
             checked = 'Y';
            }
          var order_no = $("#que_order_no").val().trim(); 
          var sp_name = $('#company_abbr').val();
          var sp_id=$('#sp_id').val();
          $('#que_sp_input').val(sp_name);
          $('#que_sp_input').attr('disabled',true);
          if(!sp_name){
              $.scojs_message('请选择结算公司', $.scojs_message.TYPE_ERROR);
              return;
          }
          var customer_name = $('#que_customer_input').val().trim();
          var order_export_date_begin_time = $("#que_order_export_date_begin_time").val();
          var order_export_date_end_time = $("#que_order_export_date_end_time").val();
          
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/cmsChargeRequest/itemList?checked="+checked
               +"&order_no="+order_no
               +"&sp_name="+sp_name
               +"&customer_name="+customer_name
               +"&order_export_date_end_time="+order_export_date_end_time
               +"&order_export_date_begin_time="+order_export_date_begin_time;


          dataTable.ajax.url(url).load();
        }
    
      //添加新的明细
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
          $.post('/cmsChargeRequest/insertChargeItem',{order_id:order_id,charge_itemlist:charge_itemlist.toString()},function(data){
                refleshCreateTable(data.appOrderId);
                 $('#modal_cny').val( eeda.numFormat(parseFloat(data.MODAL_CNY).toFixed(2),3));
                 $('#modal_usd').val((parseFloat(data.MODAL_USD)).toFixed(2));
                 $('#modal_hkd').val((parseFloat(data.MODAL_HKD)).toFixed(2));
                 $('#modal_jpy').val((parseFloat(data.MODAL_JPY)).toFixed(2));
                 $('#add_charge_item').attr('disabled',true);
                 

          },'json').fail(function() {
               $.scojs_message('添加失败', $.scojs_message.TYPE_ERROR);
          });
      }

      //添加明细的全选
      $('#allCharge').click(function(){
          var itemIds=[];
          
          if($(this).prop('checked')){
              $("#eeda_charge_table input[name=order_check_box]").prop('checked',true);
              $("#eeda_charge_table input[name=order_check_box]:checked").each(function(){                     
                       itemIds.push($(this).val());
                    });
          }else{
             $("#eeda_charge_table input[name=order_check_box]").prop('checked',false);
          }
         if(itemIds!=''){
                $('#add_charge_item').attr('disabled',false);
           }else{
                $('#add_charge_item').attr('disabled',true);
           }
     });

      $("#eeda_charge_table").on('click','input[type=checkbox]',function(){
              $("#allCharge").prop("checked",$("#eeda_charge_table input[type=checkbox]").length-1 == $("#eeda_charge_table input[type=checkbox]:checked").length ? true : false);
        });

      $('#eeda_charge_table').on('click',"input[name='order_check_box']",function () {
        var  flag=0;
          $("input[name='order_check_box']").each(function(){
              if($(this).prop('checked')){
                flag++;
              }
          });
          if(flag>0){
               $('#add_charge_item').attr('disabled',false);
          }else{
                $('#add_charge_item').attr('disabled',true);
          }
       });

        var refleshCreateTable = function(appApplication_id){
    		var url = "/cmsChargeCheckOrder/tableList?appApplication_id="+appApplication_id+"&order_id=N&bill_flag=create";
            itemTable.ajax.url(url).load();
		    };
		//全选  
        $('#allcheck2') .on('click',function(){
            var ids=[];
            if($(this).prop('checked')==true){
                $('.checkbox2').prop('checked',true);
                $('#select_item_table tbody tr').each(function(){
                    ids.push($(this).attr('id'));
                });
            }else{
                $('.checkbox2').prop('checked',false);
                // ids.splice(0,ids.length);
            }
            calcTotal();
            $("#selected_ids").val(ids);
        })
        //查看应收应付对账结果
      $('#checkOrderAll').click(function(){
         $('#searchBtn2').click();
        });

      //删除明细
      $('#select_item_table').on('click',".delete",function(){
            var id=$(this).parent().parent().attr('id');
            var order_id=$('#order_id').val();
             $.post('/cmsChargeRequest/deleteChargeItem', {charge_itemid:id,order_id:order_id},function(data){
                 refleshCreateTable(data.appOrderId);
                 
                 $('#modal_cny').val(eeda.numFormat(parseFloat(data.MODAL_CNY).toFixed(2),3));
                 $('#modal_usd').val((parseFloat(data.MODAL_USD)).toFixed(2));
                 $('#modal_hkd').val((parseFloat(data.MODAL_HKD)).toFixed(2));
                 $('#modal_jpy').val((parseFloat(data.MODAL_JPY)).toFixed(2));
             },'json').fail(function() {
               $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
          });

      });
       $("#select_item_table").on('click','input[type=checkbox]',function(){
              $("#allcheck2").prop("checked",$("#select_item_table input[type=checkbox]").length-1 == $("#select_item_table input[type=checkbox]:checked").length ? true : false);
        });
       //清空条件
       $("#clear_fin").click(function(){
            $('#query_fin').val('');
            $('#query_fin_input').val('');
       });
        $("#clear_query").click(function(){
            $('#query_currency').val('');
            $('#query_fin').val('');
            $('#query_fin_input').val('');
       });
    
    return {
        refleshSelectTable: refleshSelectTable,
        refleshCreateTable:refleshCreateTable,
        calcTotal: calcTotal
    };

});