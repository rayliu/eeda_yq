define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'datetimepicker_CN'], function ($, metisMenu) { 

    $(document).ready(function() {
    	
    	document.title = '创建应付对账单 | ' + document.title;
        $('#menu_cost').addClass('active').find('ul').addClass('in');
        
        //datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: false, //不打开会出现排序不对 
            ajax: "/costCheckOrder/createList?itemIds="+$('#ids').val(),
            columns: [
  			{ "width": "10px",
  				    "render": function ( data, type, full, meta ) {
  				    	if(full.BILL_FLAG != ''){
  					        if(full.BILL_FLAG != 'Y')
  					    		return '<input type="checkbox" class="checkBox">';
  					    	else
  					    		return '<input type="checkbox" class="checkBox" disabled>';
  				    	}else{
  				    		return '';
  				    	}
  				    }
  			},
              { "data": "ORDER_NO", "width": "100px"},
              { "data": "CREATE_STAMP", "width": "100px"},
              { "data": "BILL_FLAG", "width": "60px",
              	"render": function ( data, type, full, meta ) {
              		if(data){
  	            		if(data != 'Y')
  				    		return '未创建对账单';
  				    	else 
  				    		return '已创建对账单';
              		}else{
              			return '';
              		}
  			    }
              },
              { "data": null, "width": "60px"},
              { "data": "TYPE", "width": "60px"},
              { "data": "CUSTOMER_NAME", "width": "100px"},
              { "data": "SP_NAME", "width": "100px"},
              { "data": "CURRENCY_TOTAL_AMOUNT", "width": "60px"},
              { "data": "CURRENCY_NAME", "width": "60px",
              	"render": function ( data, type, full, meta ) {
  	            	if(data == 'USD')
  	            		return full.TOTAL_AMOUNT;
  	            	else 
  	            		return '';
              	}
              },
              { "data": "CURRENCY_NAME", "width": "60px",
              	"render": function ( data, type, full, meta ) {
  	            	if(data == 'HKD')
  	            		return full.TOTAL_AMOUNT;
  	            	else 
  	            		return '';
              	}
              },
              { "data": "CURRENCY_NAME", "width": "60px",
              	"render": function ( data, type, full, meta ) {
  	            	if(data == 'JPY')
  	            		return full.TOTAL_AMOUNT;
  	            	else 
  	            		return '';
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
              { "data": null, "width": "60px"},
              { "data": "MBL_NO", "width": "60px"},
              { "data": "CONTAINER_NO", "width": "100px"},
	          
            ]
        });
        
        
        //------------save
        $('#saveBtn').click(function(e){
            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            
            if($('#begin_time').val()==""){
            	$.scojs_message('对账开始日期不能为空', $.scojs_message.TYPE_ERROR);
            	return;
            }
            if($('#end_time').val()==""){
            	$.scojs_message('对账结束日期不能为空', $.scojs_message.TYPE_ERROR);
            	return;
            }
            $(this).attr('disabled', true);

            var order = {
                id: $('#id').val(),
                ids: $('#ids').val(),
                remark: $('#remark').val(),
                total_amount: parseFloat($('#total_amount').text()).toFixed(2),
                sp_id: $('#sp_id').val(),
                begin_time:$('#begin_time').val(),
                end_time:$('#end_time').val()
            };

            //异步向后台提交数据
            $.post('/costCheckOrder/save', {params:JSON.stringify(order)}, function(data){
                var order = data;
                if(order.COST.ID){
                	$('#id').val(order.COST.ID);
                	$('#sp_id').val(order.COST.SP_ID);
                	$('#order_no').val(order.COST.ORDER_NO);
                	$('#status').val(order.COST.STATUS);
                	$('#create_by').val(order.LOGINUSER);
                	$('#create_stamp').text(order.COST.CREATE_STAMP);
                	$('#remark').text(order.COST.REMARK);
                	$('#company').text(order.COST.SP_NAME);
                	$('#cost_amount').text(order.COST.COST_AMOUNT);
                	$('#begin_time').val(order.COST.BEGIN_TIME);
                	$('#end_time').val(order.COST.END_TIME);
                    
                    eeda.contactUrl("edit?id",order.COST.ID);
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                    $('#saveBtn').attr('disabled', false);
                    $('#confirmBtn').attr('disabled', false);
                    
                    //异步刷新明细表
                    
                }else{
                    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                    $('#saveBtn').attr('disabled', false);
                }
            },'json').fail(function() {
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
              });
        });  
        
        //按钮状态
        var status = $('#status').val();
        if(status=='新建'){
        	$('#confirmBtn').attr('disabled', false);
        }else if(status=='已确认'){
        	$('#saveBtn').attr('disabled', true);
        	$('#confirmBtn').attr('disabled', true);
        	$('#deleteBtn').attr('disabled', false);
        }
        
        $('#confirmBtn').click(function(){
        	$(this).attr('disabled', true);
        	var id = $('#id').val();
        	 $.post('/costCheckOrder/confirm', {id:id}, function(data){
        		 if(data){
	    			 $.scojs_message('确认成功', $.scojs_message.TYPE_OK);
	    			 $('#saveBtn').attr('disabled', true);
	    			 $(this).attr('disabled', true);
	    			 $('#deleteBtn').attr('disabled', false);
	    			 $('#confirm_name').text(data.CONFIRM_BY_NAME);
	    			 $('#confirm_stamp').text(data.CONFIRM_STAMP);
        		 }
	         },'json').fail(function() {
	        	 $.scojs_message('确认失败', $.scojs_message.TYPE_ERROR);
                 $(this).attr('disabled', false);
                 $('#saveBtn').attr('disabled', false);
                 $('#deleteBtn').attr('disabled', true);
	           });
        })
        
});
});