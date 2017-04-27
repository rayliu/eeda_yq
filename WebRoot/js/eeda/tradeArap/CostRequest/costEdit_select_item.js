define(['jquery', 'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
    
var itemIds=[];
var checkIds=[];
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
                    	var checkedVal = "";
                    	if($.inArray(full.ID.toString(),itemOrder.checkIds)!='-1'){
                    		checkedVal = "checked"
                    	}
                        str = '<input id="checkbox_'+full.ID+'" type="checkbox" '+checkedVal+' style="width:30px">';
                        return str;
                    }
                },
                { "data": "CHECK_ORDER_NO"},
                {"width":"30px",
                    "render": function ( data, type, full, meta ) {
                          var str = '<button type="button" class="delete btn table_btn  delete_btn btn-xs" style="width:50px" >删除</button>';
                          if($("#status").val()=='已复核'|| $("#status").val()=='已付款'){
                              return '<button type="button" class="delete btn table_btn  delete_btn btn-xs" style="width:50px" disabled>删除</button>';
                          }
                          return str;
                      }
                },
                { "data": "ORDER_NO"},
                { "data": "TYPE"},
                { "data": "CREATE_STAMP", visible: false},
                { "data": "CUSTOMER_NAME"},
                { "data": "SP_NAME"},
                { "data":"FIN_NAME"},
                { "data": "CURRENCY_NAME","class":"currency_name"},
                { "data": "TOTAL_AMOUNT","class":"total_amount", 
                    "render": function ( data, type, full, meta ) {
                    	var str =  eeda.numFormat(parseFloat(data).toFixed(2),3);
                        if(full.ORDER_TYPE=='charge'){
                            return '<span style="color:red;">'+'-'+str+'</span>';
                        }
                        return str;
                      }
                },
                { "data": "EXCHANGE_RATE", "visible": false},
                { "data": "AFTER_TOTAL", "visible": false, 
                    "render": function ( data, type, full, meta ) {
                    	var after_str =  eeda.numFormat(parseFloat(data).toFixed(2),3);
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
                    	var exchange_total_str = eeda.numFormat(parseFloat(data).toFixed(2),3);
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
        
        var dataTable = eeda.dt({
          id: 'eeda_cost_table',
          serverSide: true, //不打开会出现排序不对
          ajax:{
                //url: "/tradeCostCheckOrder/list",
                type: 'POST'
          },
          // drawCallback: function( settings ) {
          //     flash();
          // },
          columns: [
                { "width": "10px","orderable": false,
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
                          return "<a href='/trJobOrder/edit?id="+full.JOBID+"'target='_blank'>"+data+"</a>";
                      }
                },
                { "data": "ORDER_EXPORT_DATE", "width": "100px"},
                { "data": "CREATE_STAMP", "width": "100px"},
                { "data": "TYPE", "width": "60px"},
                { "data": "FEE_NAME", "width": "60px"},
                { "data": "CUSTOMER_NAME", "width": "100px"},
                { "data": "SP_NAME", "width": "100px","class":"SP_NAME"},
                { "data": "TOTAL_AMOUNT", "width": "60px",'class':'TOTAL_AMOUNT',
                    "render": function ( data, type, full, meta ) {
                        if(full.SQL_TYPE=='charge'){
                            return '<span style="color:red;">'+'-'+data+'</span>';
                        }
                        return data;
                      }
                },
                { "data": "CURRENCY_NAME", "width": "60px",'class':'CURRENCY_NAME'},
                { "data": "EXCHANGE_RATE", "width": "60px" },
                { "data": "AFTER_TOTAL", "width": "60px" ,'class':'AFTER_TOTAL',
                    "render": function ( data, type, full, meta ) {
                        if(full.SQL_TYPE=='charge'){
                            return '<span style="color:red;">'+'-'+data+'</span>';
                        }
                        return data;
                      }
                },              
                { "data": "EXCHANGE_CURRENCY_NAME", "width": "60px"},
                { "data": "EXCHANGE_CURRENCY_RATE", "width": "60px" },
                { "data": "EXCHANGE_TOTAL_AMOUNT", "width": "60px" ,
                    "render": function ( data, type, full, meta ) {
                        if(full.SQL_TYPE=='charge'){
                            return '<span style="color:red;">'+'-'+data+'</span>';
                        }
                        return data;
                    }
                },              
                { "data": "FND", "width": "60px",
                    "render": function ( data, type, full, meta ) {
                        if(data)
                                 return data;
                        else
                                 return full.DESTINATION;
                    }
                },
                { "data": "VOLUME", "width": "60px"},
                { "data": "CONTAINER_AMOUNT","width": "60px",
                    "render": function ( data, type, full, meta ) {
                        if(data){
                            var dataArr = data.split(",");
                            var a = 0;
                            var b = 0;
                            var c = 0;
                            var dataStr = "";
                            for(var i=0;i<dataArr.length;i++){
                                if(dataArr[i]=="20GP"){
                                    a++;
                                }
                                if(dataArr[i]=="40GP"){
                                    b++;
                                }
                                if(dataArr[i]=="45GP"){
                                    c++;
                                }
                            }
                            if(a>0){
                                dataStr+="20GPx"+a+";"
                            }
                            if(b>0){
                                dataStr+="40GPx"+b+";"
                            }
                            if(c>0){
                                dataStr+="45GPx"+c+";"
                            }
                            return dataStr;
                        }else{
                            return '';
                        }
                    }
                },
                { "data": "NET_WEIGHT", "width": "60px"},
                { "data": "REF_NO", "width": "200px"},
                { "data": "MBL_NO", "width": "60px"},
                { "data": "HBL_NO", "width": "60px"},
                { "data": "CONTAINER_NO", "width": "100px"},
                { "data": "TRUCK_TYPE", "width": "100px"},
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
            	var url = "/tradeCostCheckOrder/tableList?order_ids="+order_ids+"&order_id=N";
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
            var query_exchange_currency=$('#query_currency').val();
            var fin_name=$('#query_fin').val();
            var url = "/tradeCostCheckOrder/tableList?order_ids="+ids+"&order_id=N"
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
        $('#add_cost').attr('disabled',true);
      }

      $('#add_cost').click(function(){
            $('#allcost').prop('checked',false);
            $('#add_cost_item').prop('disabled',true);
            $('#cost_table_msg_btn').click();
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
          var url = "/tradeCostRequest/itemList?checked="+checked
               +"&order_no="+order_no
               +"&sp_name="+sp_name
               +"&customer_name="+customer_name
               +"&order_export_date_end_time="+order_export_date_end_time
               +"&order_export_date_begin_time="+order_export_date_begin_time;


          dataTable.ajax.url(url).load();
        }
    
      //添加新的明细
      $('#add_cost_item').on('click', function(){
          insertcostItem();
          searchData3(); 
      });
      var insertcostItem=function(){

          var order_id=$('#order_id').val();
           var cost_itemlist=[];
          $('#eeda_cost_table input[name=order_check_box]:checked').each(function(){
                var id=$(this).val();
                cost_itemlist.push(id);
          });
          if(cost_itemlist.length==0){
            $('#add_cost_item').attr('disabled',true);
          }
          $.post('/tradeCostRequest/insertCostItem',{order_id:order_id,cost_itemlist:cost_itemlist.toString()},function(data){
                refleshCreateTable(data.appOrderId);
                 $('#modal_cny').val((parseFloat(data.MODAL_CNY)).toFixed(2));
                 $('#modal_usd').val((parseFloat(data.MODAL_USD)).toFixed(2));
                 $('#modal_hkd').val((parseFloat(data.MODAL_HKD)).toFixed(2));
                 $('#modal_jpy').val((parseFloat(data.MODAL_JPY)).toFixed(2));
                 $('#add_cost_item').attr('disabled',true);
                 

          },'json').fail(function() {
               $.scojs_message('添加失败', $.scojs_message.TYPE_ERROR);
          });
      }

      //添加明细的全选
      $('#allcost').click(function(){
          var itemIds=[];
          
          if($(this).prop('checked')){
            $("#eeda_cost_table input[name=order_check_box]").prop('checked',true);
             $("#eeda_cost_table input[name=order_check_box]:checked").each(function(){                     
                     itemIds.push($(this).val());
                  });
          }else{
             $("#eeda_cost_table input[name=order_check_box]").prop('checked',false);
          }
         if(itemIds!=''){ 
                $('#add_cost_item').attr('disabled',false);
           }else{
                $('#add_cost_item').attr('disabled',true);
           }
     });

      $('#eeda_cost_table').on('click',"input[name='order_check_box']",function () {
        var  flag=0;
          $("input[name='order_check_box']").each(function(){
              if($(this).prop('checked')){
                flag++;
              }
          });
          if(flag>0){
               $('#add_cost_item').attr('disabled',false);
          }else{
                $('#add_cost_item').attr('disabled',true);
          }
       });

		  //全选
        $('#coR_allcheck').on('click',function(){
             var table = $('#select_item_table').DataTable();
             var self= this;
            var selected_ids=[];
            if($(self).prop("checked")){
                  table.data().each(function(item, index) {
                      selected_ids.push(item.ID);
                    });
                 $('#select_item_table input[type="checkbox"]').prop('checked',true);   
                 $('#createSave').attr('disabled',false);
            }else{
                $('#select_item_table input[type="checkbox"]').prop('checked',false);
                $('#createSave').attr('disabled',true);
            }
             calcTotal();
             var selected_ids_str=selected_ids.toString();
             $('#selected_ids').val(selected_ids_str);
             
        });
        $("#select_item_table").on('click','input[type=checkbox]',function(){
              $("#coR_allcheck").prop("checked",$("#select_item_table input[type=checkbox]").length-1 == $("#select_item_table input[type=checkbox]:checked").length ? true : false);
        });

      
         //查看应收应付对账结果
      $('#checkOrderAll').click(function(){
         $('#searchBtn2').click();
        });
      $("#eeda_cost_table").on('click','input[type=checkbox]',function(){
              $("#allcost").prop("checked",$("#eeda_cost_table input[type=checkbox]").length-1 == $("#eeda_cost_table input[type=checkbox]:checked").length ? true : false);
        });

     //删除明细
      $('#select_item_table').on('click',".delete",function(){
            var id=$(this).parent().parent().attr('id');
            var order_id=$('#order_id').val();
             $.post('/tradeCostRequest/deleteCostItem', {cost_itemid:id,order_id:order_id},function(data){
                 refleshCreateTable(data.appOrderId);
                 $('#modal_cny').val((parseFloat(data.MODAL_CNY)).toFixed(2));
                 $('#modal_usd').val((parseFloat(data.MODAL_USD)).toFixed(2));
                 $('#modal_hkd').val((parseFloat(data.MODAL_HKD)).toFixed(2));
                 $('#modal_py').val((parseFloat(data.MODAL_JPY)).toFixed(2));
             },'json').fail(function() {
               $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
          });

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

    var refleshCreateTable = function(appApplication_id){
        var url = "/tradeCostCheckOrder/tableList?appApplication_id="+appApplication_id+"&order_id=N&bill_flag=create";
        itemTable.ajax.url(url).load();
        };
        
    
   
    $('#select_item_table').on('click','[type=checkbox]',function(){
    	var checked = $(this).prop('checked');
    	var item_id = $(this).parent().parent().attr('id');
    	if(checked){
    		checkIds.push(item_id)
    	}else{
    		var index = $.inArray(item_id,checkIds);
    		checkIds.splice(index,1);
    	}
    	itemOrder.checkIds = checkIds;
    });
    
    $('#coR_allcheck').on('click',function(){
    	var checked = $(this).prop('checked');
    	var table = $('#select_item_table').DataTable();
    	if(checked){
    		table.data().each(function(item, index) {
    			checkIds.push(item.ID);
            });
    	}else{
    		checkIds=[];
    	}
    	itemOrder.checkIds = checkIds;
    });
        		

    return {
        refleshSelectTable: refleshSelectTable,
        refleshCreateTable:refleshCreateTable,
        calcTotal: calcTotal
    };

});