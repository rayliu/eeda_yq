define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) { 
$(document).ready(function() {
	var tableName = 'eeda-table';
	
	itemOrder.buildItemDetail=function(){
        var item_table_rows = $("#"+tableName+" tr");
        var items_array=[];
        for(var index=0; index<item_table_rows.length; index++){
            if(index==0)
                continue;

            var row = item_table_rows[index];
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
            	var name = $(row.childNodes[i]).find('input').attr('name');
            	var value = $(row.childNodes[i]).find('input').val();
            	if(name){
            		item[name] = value;
            	}
            }
            item.action = $('#order_id').val() != ''?'UPDATE':'CREATE';
            items_array.push(item);
        }

        return items_array;
    };
    var ids = [];
    var itemIds=[];
    var cnames = [];
    //------------事件处理
        var itemTable = eeda.dt({
            id: 'eeda-table',
            initComplete: function( settings ) {
            	ids = [];
            	cnames = [];
            },
            columns:[
            { "width": "10px", "orderable": false,
            	"render": function ( data, type, full, meta ) {
                  var strcheck='<input type="checkbox" class="checkBox" name="order_check_box" value="'+full.ID+'">';
                  	for(var i=0;i<itemIds.length;i++){
                       if(itemIds[i]==full.CPOID){
                           strcheck= '<input type="checkbox" class="checkBox" checked="checked"  name="order_check_box" value="'+full.ID+'">';
                       }
                    }
                  	return strcheck;
            	}
            },
            {"width":"30px",
                "render": function ( data, type, full, meta ) {
                      var str = '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:50px" >删除</button>';
                       if($("#status").val()=='已确认'){
                          return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:50px" disabled>删除</button>';
                       }
                      return str;
                  }
            },
            { "data": "ORDER_NO", "width": "100px",
              "render": function ( data, type, full, meta ) {
                        return "<a href='/customPlanOrder/edit?id="+full.ORDER_ID+"' target='_blank'>"+data+"</a>";
                    }
            },
            { "data": "DATE_CUSTOM", "width": "100px",
            	"render":function(data,type,full,meta){
            		if(!data){
  	                  data='';
  	                  return data;
  	                }
            		return data.substr(0,10);
            	}
            },
            { "data": "TRACKING_NO", "width": "180px"},
            { "data": "ABBR_NAME", "width": "120px"},
            { "data": "FIN_NAME", "width": "200px"},
            { "data": "AMOUNT", "width": "80px"},
            { "data": "PRICE", "width": "80px"},
            { "data": "CURRENCY_NAME", "width": "100px"},
            { "data": "TOTAL_AMOUNT", "width": "100px"},
            { "data": "REMARK", "width": "100px"},
            { "data": "CUSTOMS_BILLCODE", "width": "120px"},
            { "data": "CREATE_STAMP", "width": "100px"},
            { "data": "ORDER_ID", "visible": false}
            ]
    }); 
        
        
        var dataTable = eeda.dt({
            id: 'eeda_charge_table',
            // drawCallback: function( settings ) {
            //     flash();
            // },
            ajax:{
                //url: "/chargeCheckOrder/list",
                type: 'POST'
            }, 
            columns:[
            { "width": "10px", "orderable": false,
            "render": function ( data, type, full, meta ) {
                  var strcheck='<input type="checkbox" class="checkBox" name="order_check_box" value="'+full.ARAP_ID+'">';
                for(var i=0;i<itemIds.length;i++){
                           if(itemIds[i]==full.ID){
                             strcheck= '<input type="checkbox" class="checkBox" checked="checked"  name="order_check_box" value="'+full.ARAP_ID+'">';
                           }
                       }
                return strcheck;
            }
            },
              { "data": "ORDER_NO", "width": "80px",
              "render": function ( data, type, full, meta ) {
                        return "<a href='/customPlanOrder/edit?id="+full.ORDER_ID+"'target='_blank'>"+data+"</a>";
                    }
              },
              { "data": "DATE_CUSTOM", "width": "70px",
	            	"render":function(data,type,full,meta){
	            		if(!data){
	            			return '';
	            		}
	            		return data.substring(0,10);
	            	}
	          },
              { "data": "TRACKING_NO", "width": "120px"},
              { "data": "SP_NAME", "width": "120px","class":"SP_NAME"},
              { "data": "FIN_NAME", "width": "100px"},
              { "data": "AMOUNT", "width": "60px"},
              { "data": "PRICE", "width": "60px",
	            	"render": function ( data, type, full, meta ) {
						return eeda.numFormat(parseFloat(data).toFixed(2),3);
	            	}
	          },
	          { "data": "CURRENCY_NAME", "width": "60px"},
	          { "data": "TOTAL_AMOUNT", "width": "60px","class":"TOTAL_AMOUNT",
	            	"render": function ( data, type, full, meta ) {
	            		if(data==null){
	            			data = 0.0;
	            		}
	            		var str = '';
	            		if(full.ORDER_TYPE=='cost'){
	            			str='<span style="color:red">'+eeda.numFormat(parseFloat(0.0-parseFloat(data)).toFixed(2),3)+'</span>';
	            		}else{
	            			str = eeda.numFormat(parseFloat(data).toFixed(2),3);
	            		}
	                    return str;
	            	}
	          },
	          { "data": "REMARK", "width": "100px"},
	          { "data": "CUSTOMS_BILLCODE", "width": "100px"},
	          { "data": "CREATE_STAMP", "width": "100px"}
            ]
        });
        
        
        
        
       
      //计算总额
		var calcTotal=function() {
            var CNY_cost=0, CNY_charge=0;
            var USD_cost=0, USD_charge=0;
            var HKD_cost=0, HKD_charge=0;
            var JPY_cost=0, JPY_charge=0;
            itemTable.data().each(function(item, index) {
                //dom 对象的checkbox 是否勾上了？
                var id = item.CPOID;

                if(!$('#checkbox_'+id).prop('checked'))
                    return;

                if(item.ORDER_TYPE == 'cost'){
                    temp=parseFloat(item.ZLSCF)+parseFloat(item.FTF)+parseFloat(item.PZF)+parseFloat(item.XDF)  +parseFloat(item.WLDLF)+parseFloat(item.GKF)+parseFloat(item.MTF)+
                             parseFloat(item.ZHF)+parseFloat(item.LXF)+parseFloat(item.AC)+parseFloat(item.WJF)+
                             parseFloat(item.RZF)+parseFloat(item.YF)+parseFloat(item.ZLSCF)+
                              parseFloat(item.BGF)+parseFloat(item.DTF);
                       CNY_cost=parseFloat(CNY_cost)+parseFloat(temp)
//                    if(item.EXCHANGE_CURRENCY_NAME=='CNY'){
//                        CNY_cost+=item.EXCHANGE_TOTAL_AMOUNT;
//                    }else if(item.EXCHANGE_CURRENCY_NAME=='USD'){
//                        USD_cost+=item.EXCHANGE_TOTAL_AMOUNT;
//                    }else if(item.EXCHANGE_CURRENCY_NAME=='HKD'){
//                        HKD_cost+=item.EXCHANGE_TOTAL_AMOUNT;
//                    }else if(item.EXCHANGE_CURRENCY_NAME=='JPY'){
//                        JPY_cost+=item.EXCHANGE_TOTAL_AMOUNT;
//                    }
                }else{
                	  temp1=parseFloat(item.ZLSCF)+parseFloat(item.FTF)+parseFloat(item.PZF)+parseFloat(item.XDF)  +parseFloat(item.WLDLF)+parseFloat(item.GKF)+parseFloat(item.MTF)+
                            parseFloat(item.ZHF)+parseFloat(item.LXF)+parseFloat(item.AC)+parseFloat(item.WJF)+
                            parseFloat(item.RZF)+parseFloat(item.YF)+parseFloat(item.ZLSCF)+
                            parseFloat(item.BGF)+parseFloat(item.DTF);
                       CNY_charge=parseFloat(CNY_charge)+parseFloat(temp1)
//                    if(item.EXCHANGE_CURRENCY_NAME=='CNY'){
//                        CNY_charge+=item.EXCHANGE_TOTAL_AMOUNT;
//                    }else if(item.EXCHANGE_CURRENCY_NAME=='USD'){
//                        USD_charge+=item.EXCHANGE_TOTAL_AMOUNT;
//                    }else if(item.EXCHANGE_CURRENCY_NAME=='HKD'){
//                        HKD_charge+=item.EXCHANGE_TOTAL_AMOUNT;
//                    }else if(item.EXCHANGE_CURRENCY_NAME=='JPY'){
//                        JPY_charge+=item.EXCHANGE_TOTAL_AMOUNT;
//                    }
                }
            });
            $('#cny').html((parseFloat(CNY_charge - CNY_cost)).toFixed(2));
            $('#usd_totalAmountSpan').html((parseFloat(USD_charge - USD_cost)).toFixed(2));
            $('#hkd_totalAmountSpan').html((parseFloat(HKD_charge - HKD_cost)).toFixed(2));
            $('#jpy_totalAmountSpan').html((parseFloat(JPY_charge - JPY_cost)).toFixed(2));
            $('#cny').val((parseFloat(CNY_charge - CNY_cost)).toFixed(2));
            $('#usd_totalAmountSpan').val((parseFloat(USD_charge - USD_cost)).toFixed(2));
            $('#hkd_totalAmountSpan').val((parseFloat(HKD_charge - HKD_cost)).toFixed(2));
            $('#jpy_totalAmountSpan').val((parseFloat(JPY_charge - JPY_cost)).toFixed(2));

        }
           
    $('input[name=new_rate]').on('keyup',function(){
    	var totalAmount = 0.00;
    	var row = $(this).parent().parent();
    	var rate = row.find('[name=rate]').val();
    	var new_rate = row.find('[name=new_rate]').val();
    	var currency_name = row.find('[name=new_rate]').attr('currency_name');
    	var row = $('#eeda-table tr');
    	
    	if(new_rate == ''){
    		new_rate = rate;
    	}
    	for(var i = 1;i<row.length;i++){
    		var row_currency_name = $(row[i]).find('.currency_name').text();
    		var total_amount = $(row[i]).find('.total_amount').text();
    		if(currency_name == row_currency_name){
    			$(row[i]).find('.new_rate').text(new_rate);
    			$(row[i]).find('.after_rate_total').text(parseFloat(total_amount*new_rate).toFixed(2));
    		}
    		
    		var total_amount = $(row[i]).find('.after_rate_total').text();
    		totalAmount += parseFloat(total_amount);
    	}
    	$('#check_amount').val(totalAmount.toFixed(2));
    })
    
    
    //刷新明细表
    itemOrder.refleshTable = function(order_id){
    	var url = "/cmsChargeCheckOrder/tableList?order_id="+order_id
        +"&table_type=item";
    	itemTable.ajax.url(url).load();
    }
    
    var searchData1=function(){
        var checked = '';
         if($('#checkOrderAll').prop('checked')==true){
           checked = 'Y';
          }
        var order_no = $("#que_order_no").val().trim(); 
        var sp_name = $('#company_name').val();
        $('#que_sp_input').val(sp_name);
        $('#que_sp_input').attr('disabled',true);
        if(!sp_name){
            $.scojs_message('请选择结算公司', $.scojs_message.TYPE_ERROR);
            return;
        }
        var customer_name = $('#que_customer_input').val().trim();
        var date_custom_begin_time = $("#que_date_custom_begin_time").val();
        var date_custom_end_time = $("#que_date_custom_end_time").val();
        
        /*  
            查询规则：参数对应DB字段名
            *_no like
            *_id =
            *_status =
            时间字段需成双定义  *_begin_time *_end_time   between
        */
        var url = "/cmsChargeCheckOrder/list?checked="+checked
             +"&order_no="+order_no
             +"&sp_name="+sp_name
             +"&customer_name="+customer_name
             +"&date_custom_begin_time="+date_custom_begin_time
             +"&date_custom_end_time="+date_custom_end_time;


        dataTable.ajax.url(url).load();
      }
    
    
    //添加明细
    $('#add_charge').click(function(){
        $('#allCharge').prop('checked',false);
        $('#add_charge_item').prop('disabled',true);
        $('#charge_table_msg_btn').click();
        $('#searchBtn').click();
     
    })    
    
    //点击查询
    $('#searchBtn').click(function(){
          searchData1(); 
    });
    
    
    //添加新的明细
    $('#add_charge_item').on('click', function(){
        insertChargeItem();
        searchData1(); 
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
        $.post('/cmsChargeCheckOrder/insertChargeItem',{order_id:order_id,charge_itemlist:charge_itemlist.toString()},function(data){
              itemOrder.refleshTable(data.customChargeOrderId.toString());
               $('#total_amount').val((parseFloat(data.total_amount)).toFixed(2));
               
        },'json').fail(function() {
             $.scojs_message('添加失败', $.scojs_message.TYPE_ERROR);
        });
    }
    
    //添加明细的全选
    $('#allCharge').click(function(){
        var itemIds=[];
        
        if($(this).prop('checked')){
          $("#eeda_charge_table input[name=order_check_box]").prop('checked',true);
        }else{
           $("#eeda_charge_table input[name=order_check_box]").prop('checked',false);
        }
       if($(this).prop('checked')){
               $("#eeda_charge_table input[name=order_check_box]:checked").each(function(){                     
                   itemIds.push($(this).val());
                });
               $('#add_charge_item').attr('disabled',false);
         }else{
              $('#add_charge_item').attr('disabled',true);
         }
     });
    
    //单选明细
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

    
   
   //选择是否是同一结算公司
	$('#eeda-table').on('click',"input[type=checkbox]",function () {
			var id=$(this).val();
			if($(this).prop('checked')==true){	
					ids.push(id);
			}else{
				ids.splice($.inArray(id, ids), 1);
			}
			calcTotal();
	});
	
	//删除明细
    $('#eeda-table').on('click',".delete",function(){
          var id=$(this).parent().parent().attr('id');
          var order_id=$('#order_id').val();
           $.post('/cmsChargeCheckOrder/deleteChargeItem', {charge_itemid:id,order_id:order_id},function(data){
               itemOrder.refleshTable(data.customChargeOrderId.toString());
               $('#total_amount').val((parseFloat(data.total_amount)).toFixed(2));               
               
           },'json').fail(function() {
             $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
        });

    });
	
	
	
    
} );    
} );