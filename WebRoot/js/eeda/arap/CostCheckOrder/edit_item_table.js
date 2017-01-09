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
            		var str = '<input type="checkbox" class="checkBox" style="width:30px">';
            		for(var i=0;i<ids.length;i++){
                        if(ids[i]==data){
                       	 str = '<input type="checkbox" class="checkBox" style="width:30px" checked>';
                        }
                    }
            		return str;
			    }
            },
            { "data": "ORDER_NO"},
            { "data": "TYPE"},
            { "data": "CREATE_STAMP", visible: false},
            { "data": "CUSTOMER_NAME"},
            { "data": "SP_NAME"},
            { "data": "CURRENCY_NAME","class":"currency_name"},
            { "data": "TOTAL_AMOUNT","class":"total_amount", 
            	"render": function ( data, type, full, meta ) {
            		if(full.ORDER_TYPE=='charge'){
	            		return '<span style="color:red;">'+'-'+parseFloat(data).toFixed(2)+'</span>';
	            	}
            		return parseFloat(data).toFixed(2);
                  }
            },
            { "data": "EXCHANGE_RATE", "visible": false},
            { "data": "AFTER_TOTAL", "visible": false, 
            	"render": function ( data, type, full, meta ) {
            		if(full.ORDER_TYPE=='charge'){
	            		return '<span style="color:red;">'+'-'+parseFloat(data).toFixed(2)+'</span>';
	            	}else{
	            		return parseFloat(data).toFixed(2);
	            	} 
            	}
            },
            { "data": "NEW_RATE","class":"new_rate", "visible": false },
            { "data": "AFTER_RATE_TOTAL","class":"after_rate_total", "visible": false,
            	"render": function ( data, type, full, meta ) {
            		if(full.ORDER_TYPE=='charge'){
	            		return '<span style="color:red;">'+'-'+parseFloat(data).toFixed(2)+'</span>';
	            	}else{
	            		return parseFloat(data).toFixed(2);
	            	}
            	}
            },
            { "data": "EXCHANGE_CURRENCY_NAME"},
            { "data": "EXCHANGE_CURRENCY_RATE"},
            { "data": "EXCHANGE_TOTAL_AMOUNT",
                "render": function ( data, type, full, meta ) {
                    if(full.ORDER_TYPE=='charge'){
                        return '<span style="color:red;">'+'-'+parseFloat(data).toFixed(2)+'</span>';
                    }
                    return parseFloat(data).toFixed(2);
                  }
            },
            { "data": "ORDER_TYPE", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            },
        ]
    });
        
        
        //选择是否是同一币种
        var cnames = [];
    	$('#eeda-table').on('click',"input[type=checkbox]",function () {
    			var cname = $(this).parent().siblings('.currency_name')[0].textContent;
    			var id=$(this).parent().parent().attr('id')
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
    	$('#cost_amount').val(totalAmount.toFixed(2));
    	
    });
    
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
    itemOrder.refleshTable = function(order_id,ids){
    	var url = "/costCheckOrder/tableList?order_id="+order_id+"&ids="+ids;
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
    
    
    $('#exchange').click(function(){
    	$(this).attr('disabled',true);
    	var rate = $('#exchange_rate').val();
        var que_currency= $('#exchange_currency').val();

        var currency_name = cnames[0];
    	if(rate==''||isNaN(rate)){
    		$.scojs_message('请输入正确的汇率进行兑换', $.scojs_message.TYPE_ERROR);
    		return;
    	}
//    	var total = 0;
//	    $('#eeda-table input[type=checkbox]:checked').each(function(){
//	    	var tr = $(this).parent().parent();
//	    	var id = tr.attr('id');
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
	    
	    $.post('/costCheckOrder/exchange_currency', 
            {   cost_order_id: $('#order_id').val(),
                ids:ids.toString(), 
                rate:rate, 
                ex_currency_name: $('#exchange_currency').val()}, function(data){
	    	$('#exchange').attr('disabled',false);
            var order_id = $('#order_id').val();
             var url = "/costCheckOrder/tableList?order_id="+order_id
             +"&table_type=item"
             +"&query_currency="+que_currency;
             
             itemTable.ajax.url(url).load();
	    	// itemOrder.refleshTable(order_id,ids.toString());
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
    });
    // //点击td，checkbox修改checked
    //  $('#eeda-table').on('click',"td[class=sorting_1]",function (){
    //     if($(this).find('input').prop('checked')){
    //       $(this).find('input').prop("checked",false);
    //    }else{
    //       $(this).find('input').prop("checked",true);
    //    }
    // });


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
       var url = "/costCheckOrder/tableList?order_id="+order_id
       +"&table_type=item"
       +"&query_currency="+que_currency;
       
       itemTable.ajax.url(url).load();
    }

} );    
} );