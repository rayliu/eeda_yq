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
                  var strcheck='<input type="checkbox" class="checkBox" name="order_check_box" id="checkbox_'+full.ID+'" value="'+full.ID+'">';
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
                        return "<a href='/customPlanOrder/edit?id="+full.CPOID+"' target='_blank'>"+data+"</a>";
                    }
              },
              { "data": "CREATE_STAMP", "width": "100px"},
              { "data": "BOOKING_NO", "width": "200px"},
              { "data": "SP_NAME", "width": "120px","class":"COMPANY_ABBR",
                "render": function ( data, type, full, meta ) {
                  return data;
                }
              },
              { "data": "BOOKING_NO", "width": "200px"},//装箱放式
              { "data": "BOOKING_NO", "width": "200px"},//报关单录入
              { "data": "G_ZLSCF", "width": "160px",
                "render": function ( data, type, full, meta ) {
                  if(full.G_ZLSCF==null){
                    return '0.00';
                  }
                 var  aa=parseFloat(full.G_ZLSCF).toFixed(2);
                  return aa;
                }
              },
              { "data": "G_FTF", "width": "100px",
                "render": function ( data, type, full, meta ) {
                  if(full.G_FTF==null){
                    return '0.00';
                  }
                   var  aa=parseFloat(full.G_FTF).toFixed(2);
                  return aa;
                }
              },
              { "data": "G_PZF", "width": "100px",
                "render": function ( data, type, full, meta ) {
                  if(full.G_PZF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.G_PZF).toFixed(2);
                  return aa;
                }
              },
              { "data": "G_XDF", "width": "100px",
                "render": function ( data, type, full, meta ) {
                  if(full.G_XDF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.G_XDF).toFixed(2);
                  return aa;
                }
              },
              { "data": "G_WLDLF", "width": "100px",
                "render": function ( data, type, full, meta ) {
                  if(full.G_WLDLF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.G_WLDLF).toFixed(2);
                  return aa;
                }
              },
              { "data": "G_GKF", "width": "100px",
                "render": function ( data, type, full, meta ) {
                  if(full.G_GKF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.G_GKF).toFixed(2);
                  return aa;
                }
              },
              { "data": "G_MTF", "width": "100px",
                "render": function ( data, type, full, meta ) {
                  if(full.G_MTF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.G_MTF).toFixed(2);
                  return aa;
                }
              },
              { "width": "100px","class":'matou_fine',
                "render": function ( data, type, full, meta ) {
                  if(full.ORDER_TYPE=='cost'){
                    return '<span style="color:red;">'+'-'+'</span>';
                  }
                  var matoufine=0,wldlf,gkf,mtf;
                  wldlf=parseFloat(full.G_WLDLF) ;gkf=parseFloat(full.G_GKF); mtf=parseFloat(full.G_MTF);
                  if(full.G_WLDLF==null){wldlf=0;}
                  if(full.G_GKF==null){gkf=0;}
                  if(full.G_MTF==null){mtf=0;}
                  matoufine=parseFloat(wldlf+gkf+mtf).toFixed(2);
                    return matoufine;
                    }
              },//码头费小计(前三项，G_LHF不)
              { "data": "G_ZHF","width": "60px",
                "render": function ( data, type, full, meta ) {
                  if(full.G_ZHF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.G_ZHF).toFixed(2);
                  return aa;
                }
                },
              { "data": "G_LXF", "width": "60px",
                "render": function ( data, type, full, meta ) {
                  if(full.G_LXF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.G_LXF).toFixed(2);
                  return aa;
                }
              },
              { "data": "G_AC", "width": "60px",
                "render": function ( data, type, full, meta ) {
                  if(full.G_AC==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.G_AC).toFixed(2);
                  return aa;
                }
              },
              { "data": "G_WJF", "width": "60px",
                "render": function ( data, type, full, meta ) {
                  if(full.G_WJF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.G_WJF).toFixed(2);
                  return aa;
                }
              },
              { "width": "60px","class":'gongben_fine',
                "render": function ( data, type, full, meta ) {
                  if(full.ORDER_TYPE=='cost'){
                    return '<span style="color:red;">'+'-'+'</span>';
                  }
                  var gongbenfine=0,lxf,ac,wjf;
                  lxf=parseFloat(full.G_LXF) ;ac=parseFloat(full.G_AC); wjf=parseFloat(full.G_WJF);
                  if(full.G_LXF==null){lxf=0;}
                  if(full.G_AC==null){ac=0;}
                  if(full.G_WJF==null){wjf=0;}
                  gongbenfine=parseFloat(lxf+ac+wjf).toFixed(2);
                      return gongbenfine;
                    }
               }, //工本费小计（前三项）
              { "data": "G_PZF", "width": "60px",
                "render": function ( data, type, full, meta ) {
                  if(full.G_PZF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.G_PZF).toFixed(2);
                  return aa;
                }
              },
              { "data": "G_RZF", "width": "100px",
                "render": function ( data, type, full, meta ) {
                  if(full.G_RZF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.G_RZF).toFixed(2);
                  return aa;
                }
              },
              { "data": "G_YF", "width": "60px",
                "render": function ( data, type, full, meta ) {
                  if(full.G_YF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.G_YF).toFixed(2);
                  return aa;
                }
              },
              { "data": "G_DTF", "width": "120px",
                "render": function ( data, type, full, meta ) {
                  
                  var  aa=parseFloat(full.G_DTF).toFixed(2);
                  return '小计';
                }
              },//小计
              { "data": "G_BGF", "width": "60px",
                "render": function ( data, type, full, meta ) {
                  if(full.G_BGF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.G_BGF).toFixed(2);
                  return aa;
                }
              },
              { "data": "G_DTF", "width": "120px",
                "render": function ( data, type, full, meta ) {
                  if(full.G_DTF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.G_DTF).toFixed(2);
                  return aa;
                }
              },
              { "data": "TOTAL", "width": "120px"},//合计
              { "data": "TOTAL", "width": "120px"}//备注
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
                var id = item.ID;

                if(!$('#checkbox_'+id).prop('checked'))
                    return;

                if(item.ORDER_TYPE == 'cost'){
                	CNY_cost+=item.MTF+item.MTF+item.MTF+item.MTF+item.MTF+item.MTF
                				+item.ZLSCF+item.FTF+item.PZF+item.XDF+item.WLDLF
                				+item.LXF+item.AC+item.WJF+item.RZF+item.YF+item.BGF+item.DTF;
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
                	CNY_charge+=item.MTF+item.YJ+item.ZHF+item.GKF+item.LHF+item.SCF
    				+item.ZLSCF+item.FTF+item.PZF+item.XDF+item.WLDLF
    				+item.LXF+item.AC+item.WJF+item.RZF+item.YF+item.BGF+item.DTF;
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
	

     
   
//   $('#exchange').click(function(){
//   	$(this).attr('disabled',true);
//   	var rate = $('#exchange_rate').val();
//   	if(rate==''||isNaN(rate)){
//   		$.scojs_message('请输入正确的汇率进行兑换', $.scojs_message.TYPE_ERROR);
//   		return;
//   	}
//   	var currency_name = cnames[0];
//   	var ex_currency_name = $('#exchange_currency').val();
////   	var total = 0;
////	    $('#eeda-table input[type=checkbox]:checked').each(function(){
////	    	var tr = $(this).parent().parent();
////	    	
////	    	var total_amount = tr.find(".total_amount").text();
////	    	if(total_amount!=''&&!isNaN(total_amount)){
////	    		total +=parseFloat(total_amount);
////	    	}
////	    })
//	    if(ids.length==0){
//	    	$.scojs_message('请选择一条费用明细进行兑换', $.scojs_message.TYPE_ERROR);
//	    	$('#exchange').attr('disabled',false);
//	    	return;
//	    }
//	    
//	    $.post('/cmsChargeCheckOrder/exchange_currency', 
//           {   charge_order_id: $('#order_id').val(),
//               ids:ids.toString(), 
//               rate:rate, 
//               ex_currency_name:ex_currency_name}, function(data){
//	    	$('#exchange').attr('disabled',false);
//	    	var order_id = $('#order_id').val();
//	    	itemOrder.refleshTable(order_id,ids.toString());
//	    	$.scojs_message('兑换成功', $.scojs_message.TYPE_OK);
//           $('#cny').val((parseFloat(data.CNY)).toFixed(2));
//           $('#usd').val((parseFloat(data.USD)).toFixed(2));
//           $('#hkd').val((parseFloat(data.HKD)).toFixed(2));
//           $('#jpy').val((parseFloat(data.JPY)).toFixed(2));
//	    },'json').fail(function() {
//	    	$('#exchange').attr('disabled',false);
//           $.scojs_message('发生异常，兑换失败', $.scojs_message.TYPE_ERROR);
//	    });
//   })
    
    
    
    
    
    
    
} );    
} );