define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'datetimepicker_CN'], function ($, metisMenu) { 

    $(document).ready(function() {
    	  
        var order_no = $('#order_no').val();
        if(order_no){
            document.title = order_no + ' | ' + document.title;
        }else{
            document.title = '创建应付对账单 | ' + document.title;
        }
        console.log('1111');
        //datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            serverSide: false, //不打开会出现排序不对 
            //ajax: "/costCheckOrder/createList?itemIds="+$('#ids').val(),
            columns: [
              { "data": "ID", "visible": false},
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
              { "data": "TYPE", "width": "60px",
                  "render": function ( data, type, full, meta ) {
                      if(data){
                        return data;
                      }else
                        return "";
                  }
              },
              { "data": "TYPE", "width": "60px"},
              { "data": "CUSTOMER_NAME", "width": "100px"},
              { "data": "SP_NAME", "width": "100px"},
              { "data": "TOTAL_COSTRMB", "width": "60px",
                  "render": function ( data, type, full, meta ) {
                      if(data){
                        return data;
                      }else
                        return "";
                  }
              }/*,
              { "data": "TYPE", "width": "60px",
                  "render": function ( data, type, full, meta ) {
                      if(data){
                        return data;
                      }else
                        return "";
                  }
              },
              { "data": "TYPE", "width": "60px",
                  "render": function ( data, type, full, meta ) {
                      if(data){
                        return data;
                      }else
                        return "";
                  }
              },
              { "data": "TYPE", "width": "60px",
                  "render": function ( data, type, full, meta ) {
                      if(data){
                        return data;
                      }else
                        return "";
                  }
              },
              { "data": "TYPE", "width": "60px",
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
      			    		return "";
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
              { "data": "MBL_NO", "width": "60px",
                  "render": function ( data, type, full, meta ) {
                      if(data){
                        return data;
                      }else
                        return "";
                  }
              },
              { "data": "MBL_NO", "width": "60px"},
              { "data": "CONTAINER_NO", "width": "100px",
                  "render": function ( data, type, full, meta ) {
                      if(data){
                        return data;
                      }else
                        return "";
                  }
              }*/
	          
            ]
        });
        
        console.log('2222');
        //------------save
        $('#saveBtn').click(function(e){
            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            
            $(this).attr('disabled', true);

            var order = {
                id: $('#id').val(),
                ids: $('#ids').val(),
                remark: $('#remark').val(),
                total_amount: parseFloat($('#total_amount').val()).toFixed(2),
                sp_id: $('#sp_id').val(),
                begin_time:$('#audit_begin_time').val(),
                end_time:$('#audit_end_time').val()
            };

            //异步向后台提交数据
            $.post('/costCheckOrder/save', {params:JSON.stringify(order)}, function(data){
                var order = data;
                if(order.ID){
                	$('#id').val(order.ID);
                	$('#sp_id').val(order.SP_ID);
                	$('#order_no').val(order.ORDER_NO);
                	$('#status').val(order.STATUS);
                	$('#creator').val(order.CREATOR_NAME);
                	$('#create_stamp').val(order.CREATE_STAMP);
                	$('#company').text(order.SP_NAME);
                	$('#cost_amount').text(order.COST_AMOUNT);
                	$('#audit_begin_time').val(order.BEGIN_TIME);
                	$('#audit_end_time').val(order.END_TIME);
                	$('#remark').text(order.REMARK);
                    
                    eeda.contactUrl("edit?id",order.ID);
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                    $('#saveBtn').attr('disabled', false);
                    $('#confirmBtn').attr('disabled', false);
                    
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
	    			 $('#confirm_name').val(data.CONFIRM_BY_NAME);
	    			 $('#confirm_stamp').val(data.CONFIRM_STAMP);
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