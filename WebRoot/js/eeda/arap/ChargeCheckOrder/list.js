define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','sco','validate_cn'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '应收对账单查询 | '+document.title;

    	$('#menu_charge').addClass('active').find('ul').addClass('in');

    	
    	//datatable, 动态处理
		var cnames = [];
		var itemIds=[];
        var totalAmount = 0.0;
        
        var dataTable = eeda.dt({
            id: 'uncheckedEeda-table',
//            pageLength: 25,
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/chargeCheckOrder/list",
            columns:[
			      { "width": "10px",
				    "render": function ( data, type, full, meta ) {
				    	if(full.BILL_FLAG != ''){
				    		var strcheck = '';
					        if(full.BILL_FLAG != 'Y'){
					            strcheck='<input type="checkbox" class="checkBox" name="order_check_box" value="'+full.ID+'">';
					        	for(var i=0;i<itemIds.length;i++){
			                         if(itemIds[i]==full.ID){
			                        	 return strcheck= '<input type="checkbox" class="checkBox" checked="checked"  name="order_check_box" value="'+full.ID+'">';
			                         }
			                     }
					        }else{
					        	strcheck = '<input type="checkbox" class="checkBox" disabled>';
					    		}
				    	}else{
				    		strcheck = '';
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
	            { "data": "SP_NAME", "width": "100px","sClass":"SP_NAME"},
	            { "data": "CURRENCY_NAME", "width": "60px"},
	            { "data": "TOTAL_AMOUNT", "width": "60px",
	            	"render": function ( data, type, full, meta ) {
	            		if(full.SQL_TYPE=='cost'){
		            		return '<span style="color:red;">'+'-'+data+'</span>';
		            	}
	                    return data;
	                  }
	            },
	            { "data": "EXCHANGE_RATE", "width": "60px" },
	            { "data": "AFTER_TOTAL", "width": "60px" ,'class':'total_amount',
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
	            { "data": "TRUCK_TYPE", "width": "100px"},
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
	            { "data": null, "width": "60px",
	                "render": function ( data, type, full, meta ) {
	                    return "";
	                }
	            }
	          ]
	      });
        
        var checkedDataTable = eeda.dt({
            id: 'checkedEeda-table',
//            pageLength: 25,
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/chargeCheckOrder/list2",
            columns:[
			      { "width": "10px",
				    "render": function ( data, type, full, meta ) {
				    	if(full.BILL_FLAG != ''){
					        if(full.BILL_FLAG != 'Y')
					    		return '<input type="checkbox" class="checkBox" name="order_check_box" value="'+full.ID+'" >';
					    	else
					    		return '<input type="checkbox" class="checkBox" disabled>';
				    	}else{
				    		return '';
				    	}
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
	            { "data": "FEE_NAME", "width": "60px"},
	            { "data": "CUSTOMER_NAME", "width": "100px"},
	            { "data": "SP_NAME", "width": "100px","sClass":"SP_NAME"},
	            { "data": "CURRENCY_NAME", "width": "60px"},
	            { "data": "TOTAL_AMOUNT", "width": "60px"},
	            { "data": "EXCHANGE_RATE", "width": "60px" },
	            { "data": "AFTER_TOTAL", "width": "60px" ,'class':'total_amount'},
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
	            { "data": "TRUCK_TYPE", "width": "100px"},
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
	            { "data": null, "width": "60px",
	                "render": function ( data, type, full, meta ) {
	                    return "";
	                }
	            }
	          ]
	      });

		//选择是否是同一个客户
		$('#uncheckedEeda-table').on('click',"input[name='order_check_box']",function () {
				var cname = $(this).parent().siblings('.SP_NAME')[0].textContent;
				var total_amount = $(this).parent().siblings('.total_amount')[0].textContent;
				if($(this).prop('checked')==true){	
					if(cnames.length > 0 ){
						if(cnames[0]==cname){
							totalAmount += parseFloat(total_amount);
							cnames.push(cname);
							$(this).parent().parent().clone().appendTo($("#checkedEeda-table"));
							if($(this).val() != ''){
								itemIds.push($(this).val());
							}
							$('#checkedEeda-table input[name="order_check_box"]').css("display","none");
						}else{
							$.scojs_message('请选择同一个结算公司', $.scojs_message.TYPE_ERROR);
							$(this).attr('checked',false);
							return false;
						}
					}else{
						totalAmount += parseFloat(total_amount);
						cnames.push(cname);
						$(this).parent().parent().clone().appendTo($("#checkedEeda-table"));
						if($(this).val() != ''){
							itemIds.push($(this).val());
						}
						$('#checkedEeda-table input[name="order_check_box"]').css("display","none");
					}

				}else{
					itemIds.splice($.inArray($(this).val(), itemIds), 1);
					var id=$(this)[0].value;
					var rows = $("#checkedEeda-table").children().children();
					for(var i=0; i<rows.length ;i++){
						var row = rows[i];
						if(id==$(row).find('input').attr('value')){
							row.remove();
							//$("#checkedCostCheckList").children().splice(i,1);
						}
					}

					totalAmount -= parseFloat(total_amount);
					cnames.pop(cname);
			 }
				
			 $('#totalAmountSpan').html(totalAmount.toFixed(2));
			 $('#totalAmount').val(totalAmount.toFixed(2));
    	 });
		
		//查看应收应付对账结果
    	$('#checkOrderAll').click(function(){
    		searchData(); 
    	   });
		

    	
		 
//	      var totalRMB_USD=function(){
//	    	  var id = $("input[name='order_check_box']").parent().parent().attr('id');
//	    	  var amountStr = $($('#'+id+' td')[5]).text();
//	    	  var amounUSDtStr = $($('#'+id+' td')[6]).text();
//	    	  if(amountStr!='' & amounUSDtStr!='' ){
//		    	  amount = parseFloat( amountStr );
//		    	  amountUSD = parseFloat( amounUSDtStr );
//
//		    	  if($("input[name='order_check_box']").prop('checked')==true){
//		    		  sum+=amount;
//		    		  sumUSD+=amountUSD;
//
//		    	  }else{
//		    		  sum-=amount;
//		    		  sumUSD-=amountUSD;
//
//		    	  }
//		    	  $('#totalAmountSpan').html(parseFloat(sum).toFixed(3));
//		    	  $('#totalAmountUSDSpan').html(parseFloat(sumUSD).toFixed(3));
//
//	    	  }
//	
//	      }
        
        
      	//checkbox选中则button可点击   创建对账单
		$('#uncheckedEeda-table').on('click',"input[name='order_check_box']",function () {
			
			var hava_check = 0;
			$('.checkBox').each(function(){	
				var checkbox = $(this).prop('checked');
	    		if(checkbox){
	    			hava_check=1;
	    		}	
			});
			if(hava_check>0){
				$('#createBtn').attr('disabled',false);
			}else{
				$('#createBtn').attr('disabled',true);
			}
		});
		
		$('#createBtn').click(function(){
			$('#createBtn').attr('disabled',true);
			
        	
        	$('#idsArray').val(itemIds);
        	$('#billForm').submit();
        });
  
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      });

     var searchData=function(){
    	 var checked = '';
    	 if($('#checkOrderAll').prop('checked')==true){
    		 checked = 'Y';
    	 }
    	 
          var order_no = $("#order_no").val(); 
          var sp_name = $('#sp_input').val();
          var customer_name = $('#customer_input').val();
          var type = $('#type').val();
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          var order_export_date_begin_time = $("#order_export_date_begin_time").val();
          var order_export_date_end_time = $("#order_export_date_end_time").val();
          
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
               +"&type_equals="+type
               +"&order_export_date_end_time="+order_export_date_end_time
               +"&order_export_date_begin_time="+order_export_date_begin_time
	           +"&create_stamp_begin_time="+start_date
	           +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
        }
       
    });
});