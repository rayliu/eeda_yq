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
                  var strcheck='<input type="checkbox" class="checkBox" name="order_check_box" id="checkbox_'+full.CPOID+'" value="'+full.CPOID+'">';
                for(var i=0;i<itemIds.length;i++){
                           if(itemIds[i]==full.CPOID){
                             strcheck= '<input type="checkbox" class="checkBox" checked="checked"  name="order_check_box" value="'+full.CPOID+'">';
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
              { "data": "COMPANY_ABBR", "width": "120px","class":"COMPANY_ABBR",
                "render": function ( data, type, full, meta ) {
                  return data;
                }
              },
              { "data": "BOOKING_NO", "width": "200px"},//装箱放式
              { "data": "BOOKING_NO", "width": "200px"},//报关单录入
              { "data": "ZLSCF", "width": "160px",
                "render": function ( data, type, full, meta ) {
                  if(full.ZLSCF==null){
                    return '0.00';
                  }
                 var  aa=parseFloat(full.ZLSCF).toFixed(2);
                  return aa;
                }
              },
              { "data": "FTF", "width": "100px",
                "render": function ( data, type, full, meta ) {
                  if(full.FTF==null){
                    return '0.00';
                  }
                   var  aa=parseFloat(full.FTF).toFixed(2);
                  return aa;
                }
              },
              { "data": "PZF", "width": "100px",
                "render": function ( data, type, full, meta ) {
                  if(full.PZF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.PZF).toFixed(2);
                  return aa;
                }
              },
              { "data": "XDF", "width": "100px",
                "render": function ( data, type, full, meta ) {
                  if(full.XDF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.XDF).toFixed(2);
                  return aa;
                }
              },
              { "data": "WLDLF", "width": "100px",
                "render": function ( data, type, full, meta ) {
                  if(full.WLDLF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.WLDLF).toFixed(2);
                  return aa;
                }
              },
              { "data": "GKF", "width": "100px",
                "render": function ( data, type, full, meta ) {
                  if(full.GKF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.GKF).toFixed(2);
                  return aa;
                }
              },
              { "data": "MTF", "width": "100px",
                "render": function ( data, type, full, meta ) {
                  if(full.MTF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.MTF).toFixed(2);
                  return aa;
                }
              },
              { "width": "100px","class":'matou_fine',
                "render": function ( data, type, full, meta ) {
                  if(full.ORDER_TYPE=='cost'){
                    return '<span style="color:red;">'+'-'+'</span>';
                  }
                  var matoufine=0,wldlf,gkf,mtf;
                  wldlf=parseFloat(full.WLDLF) ;gkf=parseFloat(full.GKF); mtf=parseFloat(full.MTF);
                  if(full.WLDLF==null){wldlf=0;}
                  if(full.GKF==null){gkf=0;}
                  if(full.MTF==null){mtf=0;}
                  matoufine=parseFloat(wldlf+gkf+mtf).toFixed(2);
                    return matoufine;
                    }
              },//码头费小计(前三项，LHF不)
              { "data": "ZHF","width": "60px",
                "render": function ( data, type, full, meta ) {
                  if(full.ZHF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.ZHF).toFixed(2);
                  return aa;
                }
                },
              { "data": "LXF", "width": "60px",
                "render": function ( data, type, full, meta ) {
                  if(full.LXF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.LXF).toFixed(2);
                  return aa;
                }
              },
              { "data": "AC", "width": "60px",
                "render": function ( data, type, full, meta ) {
                  if(full.AC==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.AC).toFixed(2);
                  return aa;
                }
              },
              { "data": "WJF", "width": "60px",
                "render": function ( data, type, full, meta ) {
                  if(full.WJF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.WJF).toFixed(2);
                  return aa;
                }
              },
              { "width": "60px","class":'gongben_fine',
                "render": function ( data, type, full, meta ) {
                  if(full.ORDER_TYPE=='cost'){
                    return '<span style="color:red;">'+'-'+'</span>';
                  }
                  var gongbenfine=0,lxf,ac,wjf;
                  lxf=parseFloat(full.LXF) ;ac=parseFloat(full.AC); wjf=parseFloat(full.WJF);
                  if(full.LXF==null){lxf=0;}
                  if(full.AC==null){ac=0;}
                  if(full.WJF==null){wjf=0;}
                  gongbenfine=parseFloat(lxf+ac+wjf).toFixed(2);
                      return gongbenfine;
                    }
               }, //工本费小计（前三项）
              { "data": "PZF", "width": "60px",
                "render": function ( data, type, full, meta ) {
                  if(full.PZF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.PZF).toFixed(2);
                  return aa;
                }
              },
              { "data": "RZF", "width": "100px",
                "render": function ( data, type, full, meta ) {
                  if(full.RZF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.RZF).toFixed(2);
                  return aa;
                }
              },
              { "data": "YF", "width": "60px",
                "render": function ( data, type, full, meta ) {
                  if(full.YF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.YF).toFixed(2);
                  return aa;
                }
              },
              { "data": "DTF", "width": "120px",
                "render": function ( data, type, full, meta ) {
                  
                  var  aa=parseFloat(full.DTF).toFixed(2);
                  return '小计';
                }
              },//小计
              { "data": "BGF", "width": "60px",
                "render": function ( data, type, full, meta ) {
                  if(full.BGF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.BGF).toFixed(2);
                  return aa;
                }
              },
              { "data": "DTF", "width": "120px",
                "render": function ( data, type, full, meta ) {
                  if(full.DTF==null){
                    return '0.00';
                  }
                  var  aa=parseFloat(full.DTF).toFixed(2);
                  return aa;
                }
              },
              { "data": "TOTAL","class":"total", "width": "120px",
                  "render": function ( data, type, full, meta ) {
                  var total=0.0;
                   total+=parseFloat(full.ZLSCF)+parseFloat(full.FTF)+parseFloat(full.PZF)+parseFloat(full.XDF)  +parseFloat(full.WLDLF)+parseFloat(full.GKF)+parseFloat(full.MTF)+
                  parseFloat(full.ZHF)+parseFloat(full.LXF)+parseFloat(full.AC)+parseFloat(full.WJF)+
                  parseFloat(full.RZF)+parseFloat(full.YF)+parseFloat(full.ZLSCF)+
                  parseFloat(full.BGF)+parseFloat(full.DTF);
                  return total.toFixed(2);
                }
              },//合计
              { "data": "TOTAL", "width": "120px",
                  "render": function ( data, type, full, meta ) {
                  var total=0.0;
                   total+=parseFloat(full.ZLSCF)+parseFloat(full.FTF)+parseFloat(full.PZF)+parseFloat(full.XDF)  +parseFloat(full.WLDLF)+parseFloat(full.GKF)+parseFloat(full.MTF)+
                  parseFloat(full.ZHF)+parseFloat(full.LXF)+parseFloat(full.AC)+parseFloat(full.WJF)+
                  parseFloat(full.RZF)+parseFloat(full.YF)+parseFloat(full.ZLSCF)+
                  parseFloat(full.BGF)+parseFloat(full.DTF);
                  return total.toFixed(2);
                }},//备注,
              { "data": "CPOID", "visible": false}
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