define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) { 
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
            {"data": "ID",
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
                    var str = '<button type="button" class="delete btn btn-danger btn-default btn-xs" style="width:50px" >删除</button>';
                     if($("#status").val()=='已确认'){
                        return '<button type="button" class="delete btn btn-danger btn-default btn-xs" style="width:50px" disabled>删除</button>';
                     }
                    return str;
                }
            },
            { "data": "ORDER_NO", "width": "100px",
		    	  "render": function ( data, type, full, meta ) {
                      return "<a href='/jobOrder/edit?id="+full.JOB_ORDER_ID+"'target='_blank'>"+data+"</a>";
                  }
            },
            { "data": "CREATE_STAMP"},
            { "data": "SP_NAME"},
            { "data": "CURRENCY_NAME",'class':'currency_name'},
            { "data": "TOTAL_AMOUNT",'class':'total_amount',
            	"render": function ( data, type, full, meta ) {
            		var total_str=parseFloat(data).toFixed(2);
            		if(full.ORDER_TYPE=='cost'){
	            		return '<span style="color:red;">'+'-'+total_str+'</span>';
	            	}
                    return total_str;
                  }
            },
            { "data": "EXCHANGE_RATE","visible":false},
            { "data": "AFTER_TOTAL","visible":false,
            	"render": function ( data, type, full, meta ) {
            		if(full.ORDER_TYPE=='cost'){
	            		return '<span style="color:red;">'+'-'+data+'</span>';
	            	}
                    return data;
                  }
            },
            { "data": "NEW_RATE",'class':'new_rate',"visible": false},
            { "data": "AFTER_RATE_TOTAL",'class':'after_rate_total',"visible": false,
            	"render": function ( data, type, full, meta ) {
            		if(full.ORDER_TYPE=='cost'){
	            		return '<span style="color:red;">'+'-'+data+'</span>';
	            	}
                    return data;
                  }
            },
            { "data": "EXCHANGE_CURRENCY_NAME"}, 
            { "data": "EXCHANGE_CURRENCY_RATE",
            	 "render": function ( data, type, full, meta ) {
            		 var exchange_currency_str=parseFloat(data).toFixed(2);
            		 return exchange_currency_str;
            	 }
            	 },
            { "data": "EXCHANGE_TOTAL_AMOUNT", 
                "render": function ( data, type, full, meta ) {
                	var exchange_tota_str=(Math.round(data*100)/100).toFixed(2);
                    if(full.ORDER_TYPE=='cost'){
                        return '<span style="color:red;">'+'-'+exchange_tota_str+'</span>';
                    }
                    return exchange_tota_str;
                  }
            },
            { "data": "VGM"},
            { "data": "CONTAINER_AMOUNT"},
            { "data": "GROSS_WEIGHT"},
            { "data": "CONTAINER_NO"},
            { "data": "REF_NO"},
            { "data": "MBL_NO"},
            { "data": "HBL_NO"},
            { "data": "ORDER_TYPE", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            },
            { "data": "JOB_ORDER_ID", "visible": false}
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
                        return "<a href='/jobOrder/edit?id="+full.JOBID+"'target='_blank'>"+data+"</a>";
                    }
              },
              { "data": "ORDER_EXPORT_DATE", "width": "100px"},
              { "data": "CREATE_STAMP", "width": "100px"},
              { "data": "TYPE", "width": "60px"},
              { "data": "FEE_NAME", "width": "60px",
                "render": function ( data, type, full, meta ) {
                  return data;
                }
              },
              { "data": "CUSTOMER_NAME", "width": "100px"},
              { "data": "SP_NAME", "width": "100px","class":"SP_NAME"},
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
              { "data": "EXCHANGE_CURRENCY_NAME", "width": "60px"},
              { "data": "EXCHANGE_CURRENCY_RATE", "width": "60px"},
              { "data": "EXCHANGE_TOTAL_AMOUNT", "width": "60px",
                "render": function ( data, type, full, meta ) {
                  if(full.SQL_TYPE=='cost'){
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
              { "data": "VOLUME", "width": "60px",
                  "render": function ( data, type, full, meta ) {
                      return "";
                  }
              },
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
              { "data": "REF_NO", "width": "60px"},
              { "data": "MBL_NO", "width": "60px"},
              { "data": "HBL_NO", "width": "60px"},
              { "data": "CONTAINER_NO", "width": "100px"},
              { "data": "TRUCK_TYPE", "width": "100px"}
            ]
        });


           
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
  		var numbleValue = parseFloat(numValue).toFixed(2)
  		return numbleValue;
	}
	var currency=new Array('cny','usd','jpy','hkd')
		for(var i=0;i<currency.length;i++){
			var cujh=currency[i];
			var stringNum=cujh;
			var cujh= $('#'+stringNum).val();
			$('#'+stringNum).val(refleshNum(cujh));
		}
    
    
    //刷新明细表
    itemOrder.refleshTable = function(order_id){
    	var url = "/chargeCheckOrder/tableList?order_id="+order_id
        +"&table_type=item";
    	itemTable.ajax.url(url).load();
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
   
   //全选
   $('#allcheck').click(function(){
  	 var f = false;
	 var flag = 0;//当第一个币制名与下个币制名不同时，flag
	 	   $("#eeda-table .checkBox").each(function(){
	 		  var currency_name = $(this).parent().siblings('.currency_name')[0].textContent;
	 		  if(cnames[0]==undefined){
	 			 cnames.push(currency_name);
	 			 f = true;
	 		  }
	 		  if(cnames[0]!=currency_name){
	 			  flag++;
	 		  }
	 	    })
  	 if(this.checked==true){
		 	    if(flag>0){
		 	    	$.scojs_message('不能全选，包含不同对账币制', $.scojs_message.TYPE_ERROR);
		 	    	$(this).prop('checked',false);
		 	    	if(f==true){
		 	    		cnames=[];
		 	    	}
		 	    }else{
		 	    	 $("#eeda-table .checkBox").each(function(){
		 	    		if(!$(this).prop('checked')){
		 	    			$(this).prop('checked',true);
		 	    			var id = $(this).parent().parent().attr('id');
			 	    		 var currency_name = $(this).parent().siblings('.currency_name')[0].textContent;
			 	    		 ids.push(id);
			 	    		 cnames.push(currency_name);
		 	    		}
		 	    	 });
		 	    }
  	 }else{
  		 $("#eeda-table .checkBox").prop('checked',false);
  		 if(flag==0){
	 	    	 $("#eeda-table .checkBox").each(function(){
	 	    		 var id = $(this).parent().parent().attr('id');
	 	    		 var currency_name = $(this).parent().siblings('.currency_name')[0].textContent;
	 	    		 ids.splice(0,ids.length);
	 	    		cnames.splice(0,cnames.length);
	 	    		//cnames.pop(currency_name);
	 	    	 })
  		 }
  	 }
//	   $('.checkBox').prop("checked",true);
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
        var que_currency=$("#query_currency").val();
       	if(rate==''||isNaN(rate)){
       		$.scojs_message('请输入正确的汇率进行兑换', $.scojs_message.TYPE_ERROR);
       		return;
       	}
       	var currency_name = cnames[0];
     	
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
  	    
  	    $.post('/chargeCheckOrder/exchange_currency', 
             {   charge_order_id: $('#order_id').val(),
                 ids:ids.toString(), 
                 rate:rate, 
                 ex_currency_name: $('#exchange_currency').val()}, function(data){
      	    	        $('#exchange').attr('disabled',false);
            	    	var order_id = $('#order_id').val();
                    
            	    	 var url = "/chargeCheckOrder/tableList?order_id="+order_id
            	         +"&table_type=item"
            	         +"&query_currency="+que_currency;
            	         
            	         itemTable.ajax.url(url).load();
            //	    	itemOrder.refleshTable(order_id,ids.toString());
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
    
 
    
 //查询币制按钮
   $('#query_listCurrency').click(function(){
	     searchData(); 
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
       var url = "/chargeCheckOrder/tableList?order_id="+order_id
       +"&table_type=item"
       +"&query_currency="+que_currency;
       
       itemTable.ajax.url(url).load();
    }
      //添加明细
      if($("#status").val()=='已确认'){
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
          $('#que_order_export_date_begin_time').val('');
          $('#que_order_export_date_end_time').val('');
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
          var order_export_date_begin_time = $("#que_order_export_date_begin_time").val();
          var order_export_date_end_time = $("#que_order_export_date_end_time").val();
          
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/chargeCheckOrder/list?checked="+checked
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
          $.post('/chargeCheckOrder/insertChargeItem',{order_id:order_id,charge_itemlist:charge_itemlist.toString()},function(data){
                itemOrder.refleshTable(data.chargeOrderId.toString());
                 $('#cny').val((parseFloat(data.CNY)).toFixed(2));
                 $('#usd').val((parseFloat(data.USD)).toFixed(2));
                 $('#hkd').val((parseFloat(data.HKD)).toFixed(2));
                 $('#jpy').val((parseFloat(data.JPY)).toFixed(2));
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
            var id=$(this).parent().parent().attr('id');
            var order_id=$('#order_id').val();
             $.post('/chargeCheckOrder/deleteChargeItem', {charge_itemid:id,order_id:order_id},function(data){
                 itemOrder.refleshTable(data.chargeOrderId.toString());
                 $('#cny').val((parseFloat(data.CNY)).toFixed(2));
                 $('#usd').val((parseFloat(data.USD)).toFixed(2));
                 $('#hkd').val((parseFloat(data.HKD)).toFixed(2));
                 $('#jpy').val((parseFloat(data.JPY)).toFixed(2));
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