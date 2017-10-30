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
		              	return strcheck;
		        	}
		        },
		        {"width":"80px",
	                "render": function ( data, type, full, meta ) {
	                      var str = '';
	                       if($("#status").val()=='新建'){
	                          str = '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:40px" >删除</button>';
	                          str += '<button type="button" class="itemEdit btn table_btn btn_green btn-xs" style="width:40px;" >编辑</button>';  
	                       }else{
	                    	  str = '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:40px" disabled>删除</button>';
	                    	  str += '<button type="button" class="itemEdit btn table_btn btn_green btn-xs" style="width:40px;" disabled>编辑</button>'; 
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
		        	"render": function(data,type,full,meta){
		                if(!data){
		                  data='';
		                  return data;
		                }
			        	return data.substring(0,10);
		        	}
		        },
		        { "data": "TRACKING_NO", "width": "150px"},
		        { "data": "ABBR_NAME", "width": "120px"},
		        { "data": "FIN_NAME", "width": "120px"},
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
        id: 'eeda_cost_table',
        // drawCallback: function( settings ) {
        //     flash();
        // },
        ajax:{
            //url: "/costCheckOrder/list",
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
            		if(full.ORDER_TYPE=='charge'){
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
    var costTable = eeda.dt({
        id: 'cost_table',
        autoWidth: false,
        drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
            bindFieldEvent();
            $.unblockUI();
        },
        columns:[
			{ "data": "SP_ID","width": "120px",
			    "render": function ( data, type, full, meta ) {
			    	if(full.AUDIT_FLAG == 'Y'){
			    		if(!data)
			                data='';
			            var field_html = template('table_dropdown_template',
			                {
			                    id: 'SP_ID',
			                    value: data,
			                    display_value: full.SP_NAME,
			                    style:'width:120px',
			                    disabled:'disabled'
			                }
			            );
			            return field_html;
			         }else{
			        if(!data)
			            data='';
			        var field_html = template('table_dropdown_template',
			            {
			                id: 'SP_ID',
			                value: data,
			                display_value: full.SP_NAME,
			                style:'width:120px',
			                disabled:'disabled'
			            }
			        );
			        return field_html;
			     }
			   }
			},
            { "data": "CHARGE_ID","width": "120px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		if(!data)
                            data='';
                        var field_html = template('table_dropdown_template',
                            {
                                id: 'CHARGE_ID',
                                value: data,
                                display_value: full.CHARGE_NAME,
                                style:'width:120px'
                            }
                        );
                        return field_html;
                     }else{
                    if(!data)
                        data='';
                    var field_html = template('table_dropdown_template',
                        {
                            id: 'CHARGE_ID',//对应数据库字段
                            value: data,
                            display_value: full.CHARGE_NAME,
                            style:'width:120px'
                        }
                    );
                    return field_html;
                }
              }
            },
            { "data": "PRICE", "width": "70px",
                "render": function ( data, type, full, meta ) {
                	if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                    	str = '';
                	if(full.AUDIT_FLAG == 'Y'){
                    		return '<input type="text" name="price" style="width:70px" value="'+str+'" class="form-control notsave" />';
                     }else{
                 			return '<input type="text" name="price" style="width:70px" value="'+str+'" class="form-control notsave" />';
                     }
               }
            },
            { "data": "AMOUNT","width": "70px",
                "render": function ( data, type, full, meta ) {
                	if(!data)
                        data='1';
                	if(full.AUDIT_FLAG == 'Y'){
                        	return '<input type="text" name="amount" style="width:70px" value="'+data+'" class="form-control notsave" />';
                     }else{
                         	return '<input type="text" name="amount" style="width:70px" value="'+data+'" class="form-control notsave" />';
	                 }
              }
            },
            { "data": "CURRENCY_ID", "width":"70px","className":"currency_name",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
	                	if(!data)
	                        data='';
	                    var field_html = template('table_dropdown_template',
	                        {
	                            id: 'CURRENCY_ID',
	                            value: data,
	                            display_value: full.CURRENCY_NAME,
	                            style:'width:70px',
	                            disabled:'disabled'
	                        }
	                    );
	                    return field_html;
                }else{
            	   if(!data)
                       data='';
                   var field_html = template('table_dropdown_template',
                       {
                           id: 'CURRENCY_ID',
                           value: data,
                           display_value: full.CURRENCY_NAME,
                           style:'width:70px',
                           disabled:'disabled'
                       }
                   );
                   return field_html; 
                }
              }
            },
            { "data": "TOTAL_AMOUNT", "width": "70px","className":"currency_total_amount",
                "render": function ( data, type, full, meta ) {
                	if(data)
                        var str =  parseFloat(data).toFixed(3);
                    else
                    	str = '';
                	return '<input type="text" name="total_amount" style="width:70px" value="'+str+'" class="form-control notsave" disabled/>';
                	
                }
            },
            { "data": "REMARK","width": "225px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="remark" style="width:225px" value="'+data+'" class="form-control notsave" />';
	                }else{
	            	   if(!data)
	                       data='';
	                   return '<input type="text" name="remark" style="width:225px" value="'+data+'" class="form-control notsave" />';
	                }
               }
            },
            { "data": "SP_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            }, 
            { "data": "CHARGE_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            },
            { "data": "CURRENCY_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            },
            { "data": "AUDIT_FLAG", "visible": false,
            	"render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            }
        ]
    });
    var bindFieldEvent=function(){	
        eeda.bindTableField('charge_table','SP_ID','/serviceProvider/searchCompany','');
        // eeda.bindTableField('charge_table','CHARGE_ID','/finItem/search','');
        eeda.bindTableFieldChargeId('charge_table','CHARGE_ID','/finItem/search','');
        eeda.bindTableFieldCurrencyId('charge_table','CURRENCY_ID','/serviceProvider/searchCurrency','');
    };
    
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
//                if(item.EXCHANGE_CURRENCY_NAME=='CNY'){
//                    CNY_cost+=item.EXCHANGE_TOTAL_AMOUNT;
//                }else if(item.EXCHANGE_CURRENCY_NAME=='USD'){
//                    USD_cost+=item.EXCHANGE_TOTAL_AMOUNT;
//                }else if(item.EXCHANGE_CURRENCY_NAME=='HKD'){
//                    HKD_cost+=item.EXCHANGE_TOTAL_AMOUNT;
//                }else if(item.EXCHANGE_CURRENCY_NAME=='JPY'){
//                    JPY_cost+=item.EXCHANGE_TOTAL_AMOUNT;
//                }
            }else{
            	  temp1=parseFloat(item.ZLSCF)+parseFloat(item.FTF)+parseFloat(item.PZF)+parseFloat(item.XDF)  +parseFloat(item.WLDLF)+parseFloat(item.GKF)+parseFloat(item.MTF)+
                        parseFloat(item.ZHF)+parseFloat(item.LXF)+parseFloat(item.AC)+parseFloat(item.WJF)+
                        parseFloat(item.RZF)+parseFloat(item.YF)+parseFloat(item.ZLSCF)+
                        parseFloat(item.BGF)+parseFloat(item.DTF);
                   CNY_charge=parseFloat(CNY_charge)+parseFloat(temp1)
//                if(item.EXCHANGE_CURRENCY_NAME=='CNY'){
//                    CNY_charge+=item.EXCHANGE_TOTAL_AMOUNT;
//                }else if(item.EXCHANGE_CURRENCY_NAME=='USD'){
//                    USD_charge+=item.EXCHANGE_TOTAL_AMOUNT;
//                }else if(item.EXCHANGE_CURRENCY_NAME=='HKD'){
//                    HKD_charge+=item.EXCHANGE_TOTAL_AMOUNT;
//                }else if(item.EXCHANGE_CURRENCY_NAME=='JPY'){
//                    JPY_charge+=item.EXCHANGE_TOTAL_AMOUNT;
//                }
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
    
	//编辑
   	$("#eeda-table").on('click','.itemEdit',function(){
   		var cpoa_id = $(this).parent().parent().attr('id');
   		$("#cpoa_id").val(cpoa_id);
   		var url = "/cmsCostCheckOrder/costEdit?cpoa_id="+cpoa_id;
   		costTable.ajax.url(url).load();
   		$("#cost_editBtn").click();
   	});
   	
   	//编辑按钮里面的保存
   	$("#cost_saveBtn").click(function(){
 		var order = {}
 		order.cpoa_id = $("#cpoa_id").val();
 		order.sp_id = $("#cost_table input[name='SP_ID']").val();
 		order.charge_id = $("#cost_table input[name='CHARGE_ID']").val();
 		order.price = $("#cost_table input[name='price']").val();
 		order.amount = $("#cost_table input[name='amount']").val();
 		order.total_amount = $("#cost_table input[name='total_amount']").val();
 		order.currency_id = $("#cost_table input[name='CURRENCY_ID']").val();
 		order.remark = $("#cost_table input[name='remark']").val();
 		order.customChargeOrderId = $("#order_id").val();
 		
 		$.post("/cmsCostCheckOrder/costSave",{params:JSON.stringify(order)},function(data){
	  			itemOrder.refleshTable($("#order_id").val());
	  			 $('#total_amount').val((parseFloat(data.total_amount)).toFixed(2));
 				$.scojs_message('编辑成功', $.scojs_message.TYPE_OK);
 			},'json').fail(function() {
               $.scojs_message('编辑失败', $.scojs_message.TYPE_ERROR);
 		});
 	});
 	//输入单价或数量时计算金额
    $('#cost_table').on('keyup','[name=price],[name=amount]',function(){
    	var row = $(this).parent().parent();
    	var price = $(row.find('[name=price]')).val();
    	var amount = $(row.find('[name=amount]')).val();
    	if(amount!=''&&price!=''&&!isNaN(amount)&&!isNaN(price)){
    		$(row.find('[name=total_amount]')).val(price*amount);
    	}
    })
 	//数量和单价自动补零
    $('#cost_table').on('blur','[name=price],[name=amount]',function(){
    	var amount = $(this).val();
    	if(amount!=''&&!isNaN(amount)){
    		$(this).val(itemOrder.returnFloat(amount));
    	}
    })
    //整数自动补零
    itemOrder.returnFloat = function(value){
    	 var xsd=value.toString().split(".");
    	 if(xsd.length==1){
    		 value=value.toString()+".00";
    		 return value;
    	 	}
    	 if(xsd.length>1){
    		 if(xsd[1].length<2){
    			 value=value.toString()+"0";
    		 }
    		 return value;
    	 	}
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
    	var url = "/cmsCostCheckOrder/tableList?order_id="+order_id
        +"&table_type=item";
    	itemTable.ajax.url(url).load();
    }
    
    
    var searchData1=function(){
        var checked = '';
         if($('#checkOrderAll').prop('checked')==true){
           checked = 'Y';
          }
        var order_no = $("#que_order_no").val().trim(); 
        var sp_name = $('#sp_name').val();
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
        var url = "/cmsCostCheckOrder/list?checked="+checked
             +"&order_no="+order_no
             +"&sp_name="+sp_name
             +"&customer_name="+customer_name
             +"&date_custom_begin_time="+date_custom_begin_time
             +"&date_custom_end_time="+date_custom_end_time;


        dataTable.ajax.url(url).load();
      }
    
    
    //添加明细
    $('#add_cost').click(function(){
        $('#allCharge').prop('checked',false);
        $('#add_cost_item').prop('disabled',true);
        $('#cost_table_msg_btn').click();
        $('#searchBtn').click();
     
    })
    
    //点击查询
    $('#searchBtn').click(function(){
          searchData1(); 
    });
    
    
    //添加新的明细
    $('#add_cost_item').on('click', function(){
        insertChargeItem();
        searchData1(); 
    });
    
    var insertChargeItem=function(){

        var order_id=$('#order_id').val();
         var cost_itemlist=[];
        $('#eeda_cost_table input[name=order_check_box]:checked').each(function(){
              var id=$(this).val();
              cost_itemlist.push(id);
        });
        if(cost_itemlist.length==0){
          $('#add_cost_item').attr('disabled',true);
        }
        $.post('/cmsCostCheckOrder/insertChargeItem',{order_id:order_id,cost_itemlist:cost_itemlist.toString()},function(data){
              itemOrder.refleshTable(data.customCostOrderId.toString());
               $('#total_amount').val((parseFloat(data.total_amount)).toFixed(2));
               
        },'json').fail(function() {
             $.scojs_message('添加失败', $.scojs_message.TYPE_ERROR);
        });
    }
    
    //添加明细的全选
    $('#allCharge').click(function(){
        var itemIds=[];
        
        if($(this).prop('checked')){
          $("#eeda_cost_table input[name=order_check_box]").prop('checked',true);
        }else{
           $("#eeda_cost_table input[name=order_check_box]").prop('checked',false);
        }
       if($(this).prop('checked')){
               $("#eeda_cost_table input[name=order_check_box]:checked").each(function(){                     
                   itemIds.push($(this).val());
                });
               $('#add_cost_item').attr('disabled',false);
         }else{
              $('#add_cost_item').attr('disabled',true);
         }
     });
    
    //单选明细
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
    
    //删除明细
    $('#eeda-table').on('click',".delete",function(){
          var id=$(this).parent().parent().attr('id');
          var order_id=$('#order_id').val();
           $.post('/cmsCostCheckOrder/deleteChargeItem', {cost_itemid:id,order_id:order_id},function(data){
               itemOrder.refleshTable(data.customChargeOrderId.toString());
               $('#total_amount').val((parseFloat(data.total_amount)).toFixed(2));               
               
           },'json').fail(function() {
             $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
        });

    });
    
   
   //选择是否是同一币种
   var cnames = [];
	$('#eeda-table').on('click',"input[type=checkbox]",function () {
			var cname = $(this).parent().siblings('.currency_name')[0].textContent;
			var id=$(this).val();
			if($(this).prop('checked')==true){	
				if(cnames.length > 0 ){
					if(cnames[0]==cname){
						cnames.push(cname);
						ids.push(id);
					}else{
						$.scojs_message('请选择同一币种进行兑换', $.scojs_message.TYPE_ERROR);
						$(this).attr('checked',false);
						return false;
					}
				}else{
					cnames.push(cname);
					ids.push(id);
				}
			}else{
				cnames.pop(cname);
				ids.splice($.inArray(id, ids), 1);
		 }
	}); 
	
//		   ids.splice($.inArray(id, ids), 1);

     
   
   $('#exchange').click(function(){
   	$(this).attr('disabled',true);
   	var rate = $('#exchange_rate').val();
   	if(rate==''||isNaN(rate)){
   		$.scojs_message('请输入正确的汇率进行兑换', $.scojs_message.TYPE_ERROR);
   		return;
   	}
   	var currency_name = cnames[0];
   	var ex_currency_name = $('#exchange_currency').val();
//   	var total = 0;
//	    $('#eeda-table input[type=checkbox]:checked').each(function(){
//	    	var tr = $(this).parent().parent();
//	    	
//	    	var total_amount = tr.find(".total_amount").text();
//	    	if(total_amount!=''&&!isNaN(total_amount)){
//	    		total +=parseFloat(total_amount);
//	    	}
//	    })
	    if(ids.length==0){
	    	$.scojs_message('请选择一条费用明细进行兑换', $.scojs_message.TYPE_ERROR);
	    	$('#exchange').attr('disabled',false);
	    	return;
	    }
	    
	    $.post('/cmsCostCheckOrder/exchange_currency', 
           {   charge_order_id: $('#order_id').val(),
               ids:ids.toString(), 
               rate:rate, 
               ex_currency_name:ex_currency_name}, function(data){
	    	$('#exchange').attr('disabled',false);
	    	var order_id = $('#order_id').val();
	    	itemOrder.refleshTable(order_id,ids.toString());
	    	$.scojs_message('兑换成功', $.scojs_message.TYPE_OK);
           $('#cny').val((parseFloat(data.CNY)).toFixed(2));
           $('#usd').val((parseFloat(data.USD)).toFixed(2));
           $('#hkd').val((parseFloat(data.HKD)).toFixed(2));
           $('#jpy').val((parseFloat(data.JPY)).toFixed(2));
	    },'json').fail(function() {
	    	$('#exchange').attr('disabled',false);
           $.scojs_message('发生异常，兑换失败', $.scojs_message.TYPE_ERROR);
	    });
   })
    
    
    
    
    
    
    
} );    
} );