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
            		if(full.ORDER_TYPE=='cost'){
	            		return '<span style="color:red;">'+'-'+data+'</span>';
	            	}
                    return data;
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
            { "data": "EXCHANGE_CURRENCY_RATE"}, 
            { "data": "EXCHANGE_TOTAL_AMOUNT", 
                "render": function ( data, type, full, meta ) {
                    if(full.ORDER_TYPE=='cost'){
                        return '<span style="color:red;">'+'-'+data+'</span>';
                    }
                    return data;
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
	   	 var flag = 0;
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
	 	    		 ids.pop(id);
	 	    		cnames.pop(currency_name);
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
	    
	    $.post('/chargeCheckOrder/exchange_currency', 
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
    
 
//	 	 $('#allcheck').click(function(){
//	 		 #('checkbox').attr("checked",true);
//	 		 
//	 	 })  
	 	  
			 
//	 	   if(cnames.length>0){
//	 		  $("#exchange").prop('disabled',false);
//	 	   }else{
//	 		  $("#exchange").prop('disabled',true);
//	 	   }
//   })
  
    
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
    
  
    
} );    
} );