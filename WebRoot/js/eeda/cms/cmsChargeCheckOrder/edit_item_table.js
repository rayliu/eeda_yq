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
            { "data": "ORDER_NO", "width": "100px",
              "render": function ( data, type, full, meta ) {
                        return "<a href='/customPlanOrder/edit?id="+full.ORDER_ID+"' target='_blank'>"+data+"</a>";
                    }
            },
            { "data": "DATE_CUSTOM", "width": "100px"},
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
    
    
//    if($('#order_id').val()==''){
//   	 $('#exchange').attr('disabled',true);
//   }
//   
//   if($('#exchange_rate').val()==''){
//  	 $('#exchange').attr('disabled',true);
//  }
//   
//   
//   $('#exchange_rate').on('keyup',function(){
//   	 $('#exchange').attr('disabled',false);
//   });
   
   //选择是否是同一结算公司
	$('#eeda-table').on('click',"input[type=checkbox]",function () {
//			var cname = $(this).parent().siblings('.currency_name')[0].textContent;
			var id=$(this).val();
			if($(this).prop('checked')==true){	
//				if(cnames.length > 0 ){
//					if(cnames[0]==cname){
//						cnames.push(cname);
//						ids.push(id);
//					}else{
//						$.scojs_message('请选择同一结算公司', $.scojs_message.TYPE_ERROR);
//						$(this).attr('checked',false);
//						return false;
//					}
//				}else{
//					cnames.push(cname);
					ids.push(id);
//				}
			}else{
//				cnames.pop(cname);
				ids.splice($.inArray(id, ids), 1);
			}
			calcTotal();
	}); 
    
} );    
} );