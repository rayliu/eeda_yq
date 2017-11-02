define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','jq_blockui'], function ($, metisMenu, template) { 
$(document).ready(function() {
	var tableName = 'eeda-table';
	var itemIds=[];
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
    
    var chargeTable = eeda.dt({
        id: 'charge_table',
        autoWidth: false,
        drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
            bindFieldEvent();
            $.unblockUI();
        },
        columns:[
                 { "data": "SP_ID","width": "100px",
                    "render": function ( data, type, full, meta ) {
                    	if(full.AUDIT_FLAG == 'Y'){
                    		if(!data)
                                data='';
                            var field_html = template('table_dropdown_template',
                                {
                                    id: 'SP_ID',
                                    value: data,
                                    display_value: full.SP_NAME,
                                    style:'width:100px',
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
                                style:'width:100px',
                                disabled:'disabled'
                            }
                        );
                        return field_html;
                     }
                   }
                },
                 { "data": "CONTAINER_NO", "width": "80px",
                	 "render": function ( data, type, full, meta ) {
                		 if(data)
                             var str =  data;
                         else
                         	str = '';
                		 return '<input type="text" name="container_no" tjo_id = '+full.TJO_ID+' style="width:80px" value="'+str+'" class="form-control notsave" />';
                	 }
                 },
                 { "data": "SO_NO", "width": "80px",
                	 "render": function ( data, type, full, meta ) {
                		 if(data)
                             var str =  data;
                         else
                         	str = '';
                		 return '<input type="text" name="so_no" style="width:80px" value="'+str+'" class="form-control notsave" />';
                	 }
                 },
                 { "data": "CUSTOMER_ID","width": "100px",
                     "render": function ( data, type, full, meta ) {
                     	if(full.AUDIT_FLAG == 'Y'){
                     		if(!data)
                                 data='';
                             var field_html = template('table_dropdown_template',
                                 {
                                     id: 'CUSTOMER_ID',
                                     value: data,
                                     display_value: full.CUSTOMER_NAME,
                                     style:'width:100px',
                                     disabled:'disabled'
                                 }
                             );
                             return field_html;
                          }else{
                         if(!data)
                             data='';
                         var field_html = template('table_dropdown_template',
                             {
                                 id: 'CUSTOMER_ID',
                                 value: data,
                                 display_value: full.CUSTOMER_NAME,
                                 style:'width:100px',
                                 disabled:'disabled'
                             }
                         );
                         return field_html;
                      }
                    }
                 },
                 { "data": "CHARGE_ID","width": "80px",
                     "render": function ( data, type, full, meta ) {
                     	if(full.AUDIT_FLAG == 'Y'){
                     		if(!data)
                                 data='';
                             var field_html = template('table_dropdown_template',
                                 {
                                     id: 'CHARGE_ID',
                                     value: data,
                                     display_value: full.FIN_NAME,
                                     style:'width:80px'
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
                                 display_value: full.FIN_NAME,
                                 style:'width:80px'
                             }
                         );
                         return field_html;
                     }
                   }
                 },
                 { "data": "CURRENCY_ID", "width":"70px",
                     "render": function ( data, type, full, meta ) {
                     	if(full.AUDIT_FLAG == 'Y'){
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
                     }else{
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
                   }
                 },
                 { "data": "TOTAL_AMOUNT",
                	 "render": function ( data, type, full, meta ) {
                		 if(data)
                             var str =  data;
                         else
                         	str = '';
                		 return '<input type="text" name="total_amount" style="width:80px" value="'+str+'" class="form-control notsave" />';
                	 }
                 },
                 { "data": "EXCHANGE_RATE", "width": "80px",
                	 "render": function ( data, type, full, meta ) {
                		 if(data)
                             var str =  data;
                         else
                         	str = '';
                		 return '<input type="text" name="exchange_rate" disabled style="width:80px" value="'+str+'" class="form-control notsave" />';
                	 }
                 },
                 { "data": "AFTER_TOTAL", "width": "100px",
                	 "render": function ( data, type, full, meta ) {
                		 if(data)
                             var str =  data;
                         else
                         	str = '';
                		 return '<input type="text" name="after_total" style="width:100px" disabled value="'+str+'" class="form-control notsave" />';
                	 }
                 },
                 { "data": "REMARK", "width": "80px",
                	 "render": function ( data, type, full, meta ) {
                		 if(data)
                             var str =  data;
                         else
                         	str = '';
                		 return '<input type="text" name="remark" style="width:80px" value="'+str+'" class="form-control notsave" />';
                	 }
                 },
                 { "data": "ORDER_TYPE", "visible": false},
                 { "data": "JOB_ORDER_ID", "visible": false}
        ]
    });
    
    var bindFieldEvent=function(){	
        eeda.bindTableField('charge_table','SP_ID','/serviceProvider/searchCompany','');
        eeda.bindTableField('charge_table','CUSTOMER_ID','/serviceProvider/searchCompany','');
        // eeda.bindTableField('charge_table','CHARGE_ID','/finItem/search','');
        eeda.bindTableFieldChargeId('charge_table','CHARGE_ID','/finItem/search','');
        //eeda.bindTableField('charge_table','CHARGE_ENG_ID','/finItem/search_eng','');
        eeda.bindTableFieldCurrencyId('charge_table','CURRENCY_ID','/serviceProvider/searchCurrency','');
        eeda.bindTableFieldCurrencyId('charge_table','exchange_currency_id','/serviceProvider/searchCurrency','');
    };
    var ids = [];
    var cnames = [];
    //------------事件处理
        var itemTable = eeda.dt({
            id: 'eeda-table',
            initComplete: function( settings ) {
            	ids = [];
            	cnames = [];
            },
            columns:[
            {"data": "ID","width":"5px",
            	"render": function ( data, type, full, meta ) {
            		var str = '<input type="checkbox" class="checkBox" style="width:30px" value="'+data+'">';
            		for(var i=0;i<ids.length;i++){
                        if(ids[i]==full.ID){
                       	 str = '<input type="checkbox" class="checkBox" style="width:30px" value="'+data+'" checked>';
                        }
                    }
            		return str;
			          }
            },
            {"width":"30px",
              "render": function ( data, type, full, meta ) {
            	  var str = '';
                     if($("#status").val()=='已确认'){
                    	 str += '<nobr><button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:40px;" disabled >删除</button>&nbsp'
                         str += '<button type="button" class="itemEdit btn table_btn btn_green btn-xs" style="width:40px;" disabled >编辑</button></nobr>';                         
                     }else{                    	
                        str += '<nobr><button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:40px" >删除</button>&nbsp'
                        str += '<button type="button" class="itemEdit btn table_btn btn_green btn-xs" style="width:40px;"  >编辑</button></nobr>';
                     }
                    return str;
                }
            },
            { "data": "ORDER_NO", "width": "80px",
		    	  "render": function ( data, type, full, meta ) {
                      return "<a href='/transJobOrder/edit?id="+full.JOB_ORDER_ID+"'target='_blank'>"+data+"</a>";
                  }
            },
            { "data": "CREATE_STAMP", "width": "70px"},
            { "data": "CONTAINER_NO", "width": "70px" },
            { "data": "COMBINE_WHARF", "width": "70px" },
            { "data": "SO_NO", "width": "70px"},
            { "data": "CABINET_TYPE", "width": "70px"},
             { "data": "CABINET_DATE", "width": "70px", 
               render: function(data){
                 if(data)
                   return data.substr(0,10);
                 return '';
               }
             },
            { "data": "CUSTOMER_NAME", "width": "70px"},
            { "data": "SP_NAME", "width": "70px"},
            { "data": "FIN_NAME","width": "70px"},
            { "data": "CURRENCY_NAME",'class':'currency_name', "width": "70px"},

            { "data": "TOTAL_AMOUNT",'class':'total_amount', "width": "70px",
            	"render": function ( data, type, full, meta ) {
            		var total_str=eeda.numFormat(parseFloat(data).toFixed(2),3);
            		if(full.ORDER_TYPE=='cost'){
	            		return '<span style="color:red;">'+'-'+total_str+'</span>';
	            	}
                    return total_str;
                  }
            },
            { "data": "EXCHANGE_RATE", "width": "90px"},
            { "data": "AFTER_TOTAL", "width": "90px",
            	"render": function ( data, type, full, meta ) {
                if(!data)
                  data=0.00;
            		if(full.ORDER_TYPE=='cost'){
	            		return '<span style="color:red;">'+'-'+eeda.numFormat(parseFloat(data).toFixed(2),3)+'</span>';
	            	}
                    return eeda.numFormat(parseFloat(data).toFixed(2),3);
                  }
            },
            { "data": "REMARK", "width": "70px"},
            { "data": "ORDER_TYPE", "visible": false},
            { "data": "JOB_ORDER_ID", "visible": false}
        ]
    }); 
        
        var dataTable = eeda.dt({
            id: 'eeda_charge_table',
            // drawCallback: function( settings ) {
            //     flash();
            // },
            ajax:{
                //url: "/transChargeCheckOrder/list",
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
              { "data": "ORDER_NO", "width": "80px",
              "render": function ( data, type, full, meta ) {
                        return "<a href='/transJobOrder/edit?id="+full.JOBID+"'target='_blank'>"+data+"</a>";
                    }
              },
              { "data": "CREATE_STAMP", "width": "100px"},
              { "data": "CUSTOMER_NAME", "width": "100px"},
              { "data": "SP_NAME", "width": "100px","class":"SP_NAME"},
              { "data": "FEE_NAME", "width": "60px",
                "render": function ( data, type, full, meta ) {
                  return data;
                }
              },
              { "data": "TOTAL_AMOUNT", "width": "60px",'class':'TOTAL_AMOUNT',
                "render": function ( data, type, full, meta ) {
                  if(full.SQL_TYPE=='cost'){
                    return '<span style="color:red;">'+'-'+data+'</span>';
                  }
                      return data;
                    }
              },
              { "data": "CURRENCY_NAME", "width": "60px",'class':'CURRENCY_NAME'},
              { "data": "EXCHANGE_RATE", "width": "60px"},
              { "data": "AFTER_TOTAL", "width": "60px" ,'class':'AFTER_TOTAL',
                "render": function ( data, type, full, meta ) {
                  if(full.SQL_TYPE=='cost'){
                    return '<span style="color:red;">'+'-'+data+'</span>';
                  }
                      return data;
                    }
              },
              { "data": "REMARK", "width": "100px"}
            ]
        });
  
       //对账金额汇总
    var cal=function(){
        var totalAmount = 0.0;
        var cny_totalAmount = 0.0;
        var usd_totalAmount = 0.0;
        var hkd_totalAmount = 0.0;
        var jpy_totalAmount = 0.0;
        itemTable.data().each(function(item,index){
        var total_amount = item.TOTAL_AMOUNT;
        var currency_name = item.CURRENCY_NAME;
        if(total_amount!=''&&!isNaN(total_amount)){
           if(item.ORDER_TYPE=='charge'){
                if(currency_name=='CNY'){
                    cny_totalAmount += parseFloat(total_amount);
                }else if(currency_name=='USD'){
                    usd_totalAmount += parseFloat(total_amount);
                }else if(currency_name=='HKD'){
                    hkd_totalAmount += parseFloat(total_amount);
                }else if(currency_name=='JPY'){
                    jpy_totalAmount += parseFloat(total_amount);
                }
            }else if(item.ORDER_TYPE=='cost'){
                if(currency_name=='CNY'){
                    cny_totalAmount -= parseFloat(total_amount);
                }else if(currency_name=='USD'){
                    usd_totalAmount -= parseFloat(total_amount);
                }else if(currency_name=='HKD'){
                    hkd_totalAmount -= parseFloat(total_amount);
                }else if(currency_name=='JPY'){
                    jpy_totalAmount -= parseFloat(total_amount);
                }
            }
        }
    });
      $('#cny').html(cny_totalAmount.toFixed(2));
       $('#usd').html(usd_totalAmount.toFixed(2));
       $('#hkd').html(hkd_totalAmount.toFixed(2));
       $('#jpy').html(jpy_totalAmount.toFixed(2));
       $('#totalAmount').val(totalAmount.toFixed(2));
       $('#cny').val(cny_totalAmount.toFixed(2));
       $('#usd').val(usd_totalAmount.toFixed(2));
       $('#hkd').val(hkd_totalAmount.toFixed(2));
       $('#jpy').val(jpy_totalAmount.toFixed(2));
};
    cal();

           
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
    
    //结算金额汇总取两位小数
    var refleshNum = function(numValue){
  		var numbleValue = eeda.numFormat(parseFloat(numValue).toFixed(2),3);
  		return numbleValue;
	}
	var currency=new Array('cny','usd','jpy','hkd')
		for(var i=0;i<currency.length;i++){
			var cujh=currency[i];
			var stringNum=cujh;
			var cujh= $('#exchange_'+stringNum).val();
			$('#exchange_'+stringNum).val(refleshNum(cujh));
		}
    
	//编辑
  	$("#eeda-table").on('click','.itemEdit',function(){
  		var tjor_id = $(this).parent().parent().parent().attr('id');
  		$("#tjor_id").val(tjor_id);
  		var url = "/transChargeCheckOrder/chargeEdit?tjor_id="+tjor_id;
  		chargeTable.ajax.url(url).load();
  		$("#charge_editBtn").click();
  	});
  //编辑按钮里面的保存
  	$("#charge_saveBtn").click(function(){
  		var order = {}
  		order.tjor_id = $("#tjor_id").val();
  		order.sp_id = $("#charge_table input[name='SP_ID']").val();
  		order.container_no = $("#charge_table input[name='container_no']").val();
  		order.so_no = $("#charge_table input[name='so_no']").val();
  		order.customer_id = $("#charge_table input[name='CUSTOMER_ID']").val();
  		order.charge_id = $("#charge_table input[name='CHARGE_ID']").val();
  		order.currency_id = $("#charge_table input[name='CURRENCY_ID']").val();
  		order.total_amount = $("#charge_table input[name='total_amount']").val();
  		order.exchange_rate = $("#charge_table input[name='exchange_rate']").val();
  		order.after_total = $("#charge_table input[name='after_total']").val();
  		order.remark = $("#charge_table input[name='remark']").val();
  		order.order_id = $("#order_id").val();
  		order.tjo_id = $("#charge_table input[name='container_no']").attr("tjo_id");
  		
  		$.post("/transChargeCheckOrder/chargeSave",{params:JSON.stringify(order)},function(data){
	  			itemOrder.refleshTable(data.chargeOrderId.toString());
	            $('#cny').val((parseFloat(data.CNY)).toFixed(2));
	            $('#usd').val((parseFloat(data.USD)).toFixed(2));
	            $('#hkd').val((parseFloat(data.HKD)).toFixed(2));
	            $('#jpy').val((parseFloat(data.JPY)).toFixed(2));
	            $('#total_amount').val((parseFloat(data.CNY)).toFixed(2));
  				$.scojs_message('编辑成功', $.scojs_message.TYPE_OK);
  			},'json').fail(function() {
                $.scojs_message('编辑失败', $.scojs_message.TYPE_ERROR);
  		});
  	});
  	$("#charge_table").on('keyup','[name=total_amount]',function(){
  		var total_amount = $("#charge_table input[name='total_amount']").val();
  		var exchange_rate = $("#charge_table input[name='exchange_rate']").val();
  		$("#charge_table input[name='after_total']").val(total_amount*exchange_rate);
  	});
    //刷新明细表
    itemOrder.refleshTable = function(order_id){
    	var url = "/transChargeCheckOrder/tableList?order_id="+order_id
        +"&table_type=item";
    	itemTable.ajax.url(url).load(function(){
        cal();
      });
    }
    
    
    if($('#order_id').val()==''){
   	 $('#exchange').attr('disabled',true);
   }
   
   if($('#exchange_rate').val()==''){
  	 $('#exchange').attr('disabled',true);
  }
   
   
   $('#exchange_rate').on('keyup',function(){
   	 $('#exchange').attr('disabled',false);
   });
   
  


  var searchData=function(){
       var order_id = $("#order_id").val();
       var que_currency=$("#query_currency").val();
     
       /*  
           查询规则：参数对应DB字段名
           *_no like
           *_id =
           *_status =
           时间字段需成双定义  *_begin_time *_end_time   between
       */
       var url = "/transChargeCheckOrder/tableList?order_id="+order_id
       +"&table_type=item"
       +"&query_currency="+que_currency;
       
       itemTable.ajax.url(url).load();
    }
      //添加明细
      if($("#status").val()!='新建'){
        $('#add_charge').attr('disabled',true);
    }
     $('#add_charge').click(function(){
            $('#allCharge').prop('checked',false);
            $('#add_charge_item').prop('disabled',true);
            $('#charge_table_msg_btn').click();
            $('#searchBtn').click();
         
      }) 
      $('#resetBtn').click(function(e){
          $('#que_sp_input').val('');
          $('#que_order_no').val('');
          $('#que_create_stamp_begin_time').val('');
          $('#que_create_stamp_end_time').val('');
          $('#que_customer_input').val('');
      });
         

      $('#searchBtn').click(function(){
          searchData1(); 
      });

     var searchData1=function(){
          var checked = '';
           if($('#checkOrderAll').prop('checked')==true){
             checked = 'Y';
            }
          var order_no = $("#que_order_no").val().trim(); 
          var sp_name = $('#company_abbr').val();
          $('#que_sp_input').val(sp_name);
          $('#que_sp_input').attr('disabled',true);
          if(!sp_name){
              $.scojs_message('请选择结算公司', $.scojs_message.TYPE_ERROR);
              return;
          }
          var customer_name = $('#que_customer_input').val().trim();
          var que_create_stamp_begin_time = $("#que_create_stamp_begin_time").val();
          var que_create_stamp_end_time = $("#que_create_stamp_end_time").val();
          
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/transChargeCheckOrder/list?checked="+checked
               +"&order_no="+order_no
               +"&sp_name="+sp_name
               +"&customer_name="+customer_name
               +"&create_stamp_begin_time="+que_create_stamp_begin_time
               +"&create_stamp_end_time="+que_create_stamp_end_time;


          dataTable.ajax.url(url).load();
        }
    
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
          $.post('/transChargeCheckOrder/insertChargeItem',{order_id:order_id,charge_itemlist:charge_itemlist.toString()},function(data){
                itemOrder.refleshTable(data.chargeOrderId.toString());
                 $('#cny').val((parseFloat(data.CNY)).toFixed(2));
                 $('#usd').val((parseFloat(data.USD)).toFixed(2));
                 $('#hkd').val((parseFloat(data.HKD)).toFixed(2));
                 $('#jpy').val((parseFloat(data.JPY)).toFixed(2));

                 $('#total_amount').val((parseFloat(data.CNY)).toFixed(2));
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

       //删除明细
      $('#eeda-table').on('click',".delete",function(){
            var id=$(this).parent().parent().parent().attr('id');
            var order_id=$('#order_id').val();
             $.post('/transChargeCheckOrder/deleteChargeItem', {charge_itemid:id,order_id:order_id},function(data){
                 itemOrder.refleshTable(data.chargeOrderId.toString());
                 $('#cny').val((parseFloat(data.CNY)).toFixed(2));
                 $('#usd').val((parseFloat(data.USD)).toFixed(2));
                 $('#hkd').val((parseFloat(data.HKD)).toFixed(2));
                 $('#jpy').val((parseFloat(data.JPY)).toFixed(2));

                 $('#total_amount').val((parseFloat(data.CNY)).toFixed(2));
                 
             },'json').fail(function() {
               $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
          });

      });
      //查看应收应付对账结果
      $('#checkOrderAll').click(function(){
        searchData1();
         });
    // var flash = function(){    
    //    // $("#allCharge").prop("checked",$("#eeda_charge_table .checkBox").length == $("#eeda_charge_table .checkBox:checked").length ? true : false);
    //  };
} );    
} );