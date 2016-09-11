define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'datetimepicker_CN'], function ($, metisMenu) { 

    $(document).ready(function() {
    	
    	document.title = ' | ' + document.title;
        $('#menu_charge').addClass('active').find('ul').addClass('in');
        
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/chargeCheckOrder/editList",
            columns:[
					  { "data": null,"width": "10px",
						  "render": function ( data, type, full, meta ) {
							  if(full.BILL_FLAG != 'Y')
							  return '<input type="checkbox" class="checkBox" name="order_check_box" order_type="'+full.SP_NAME+'" value="'+full.ID+'">';
							  else 
						    		return '<input type="checkbox" disabled  value="'+full.ID+'" checked="checked">';
						  }
					    },		                
					  { "data": "ORDER_NO" },
					  { "data": "CREATE_TIME"},  
					  { "data": "SP_NAME","sClass":"SP_NAME"}, 
					  { "data": "BILL_FLAG","width": "60px",
						 "render":function(data){
							 if(data !='Y')
								 return '新建';
							 else
								 return '已创建';
						  } 
					     },
					  { "data": "RMB",
				    	 "render":function(data, type, full, meta){
				    		 if(data<0){
				    			 return '<span style="color:red">'+data+'</span>';
				    		 }else{
				    			 return data;
				    		 }
				    	 }
					     },
	                  { "data": "USD",
			    		 "render":function(data, type, full, meta){
			    			 if(data<0){
			    				 return '<span style="color:red">'+data+'</span>';
			    			 }else{
			    				 return data;
			    			 }
				    	}
					    },
//		              { "data": "HKD"}, 
//		              { "data": "JPY"}, 
		              { "data": "FND"}, 
		              { "data": "VGM"}, 
		              { "data": "CONTAINER_AMOUNT",
		            	  "render":function(data, type, full, meta){
		            		  if(data){
		            			  var dataArr = data;
		            			  var Arr = dataArr.split(",");
		            			  var a = 0;
			  	            	  var b = 0;
			  	            	  var c = 0;
			  	            	  var dataStr = "";
			  	            	for(var i=0;i<Arr.length;i++){
			            			if(Arr[i]=="20GP"){
			            				a++;
			            			}
			            			if(Arr[i]=="40GP"){
			            				b++;
			            			}
			            			if(Arr[i]=="45GP"){
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
                      { "data": "GROSS_WEIGHT"}, 
		              { "data": "CONTAINER_NO"},
		              { "data": "REF_NO"}, 
		              { "data": "MBL_NO"},
		              { "data": "HBL_NO"},
		              { "data": "TRUCK_TYPE"}
            ]
        });
        
        
        
        //------------save
        $('#saveBtn').click(function(e){
            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            $(this).attr('disabled', true);

            var order = {
                id: $('#id').val(),
                ids: $('#ids').val(),
                remark: $('#remark').val(),
                charge_amount: $('#charge_amount').text(),
                sp_id: $('#sp_id').val(),
                begin_time:$('#begin_time').val(),
                end_time:$('#end_time').val()
                billing_unit:$('#billing_unit').val();
                payee:$('#payee').val();
                total_profitRMB:$('#total_profitRMB').val();
                total_profitTotalCost:$('#total_profitTotalCost').val();
                total_profitTotalRMB:$('#total_profitTotalRMB').val();
            };

            //异步向后台提交数据
            $.post('/chargeCheckOrder/save', {params:JSON.stringify(order)}, function(data){
                var order = data;
                if(order.CHARGE.ID){
                	$('#id').val(order.CHARGE.ID);
                	$('#sp_id').val(order.CHARGE.SP_ID);
                	$('#order_no').text(order.CHARGE.ORDER_NO);
                	$('#status').text(order.CHARGE.STATUS);
                	$('#create_stamp').text(order.CHARGE.CREATE_STAMP);
                	$('#remark').text(order.CHARGE.REMARK);
                	$('#company').text(order.CHARGE.SP_NAME);
                	$('#total_profitRMB').text(order.CHARGE.TOTAL_PROFITRMB);
                	$('#total_profitTotalCost').text(order.CHARGE.TOTAL_PROFITTOTALCOST);
                	$('#total_profitTotalRMB').text(order.CHARGE.TOTAL_PROFITTOTALRMB);
                	
                	$('#begin_time').val(order.CHARGE.BEGIN_TIME);
                	$('#end_time').val(order.CHARGE.END_TIME);
                	$('#login_user').text(order.LOGINUSER);
                	$('#billing_unit').val(order.CHARGE.BILLING_UNIT);
                	$('#payee').text(order.CHARGE.PAYEE);
                    
                    eeda.contactUrl("edit?id",order.CHARGE.ID);
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                    $('#saveBtn').attr('disabled', false);
                    
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
        var status = $('#status').text();
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
    			 $.scojs_message('确认成功', $.scojs_message.TYPE_OK);
    			 $('#saveBtn').attr('disabled', true);
    			 $(this).attr('disabled', true);
    			 $('#deleteBtn').attr('disabled', false);
	         },'json').fail(function() {
	        	 $.scojs_message('确认失败', $.scojs_message.TYPE_ERROR);
                 $(this).attr('disabled', false);
                 $('#saveBtn').attr('disabled', false);
                 $('#deleteBtn').attr('disabled', true);
	           });
        })
    
        
});
});