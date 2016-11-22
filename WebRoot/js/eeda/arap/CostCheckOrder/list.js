define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '应付对账单查询  | '+document.title;
  	  $('#menu_cost').addClass('active').find('ul').removeClass('in');
  	  
      var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: true,
          serverSide: true, //不打开会出现排序不对 
          ajax: "/costCheckOrder/list",
          columns: [
      			{ "width": "10px","orderable": false,
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
	            },
	            { "data": "TYPE", "width": "60px"},
	            { "data": "FEE_NAME", "width": "60px"},
	            { "data": "CUSTOMER_NAME", "width": "100px"},
	            { "data": "SP_NAME", "width": "100px","class":"SP_NAME"},
	            { "data": "CURRENCY_NAME", "width": "60px"},
	            { "data": "TOTAL_AMOUNT", "width": "60px",
	            	"render": function ( data, type, full, meta ) {
	            		if(full.SQL_TYPE=='charge'){
		            		return '<span style="color:red;">'+'-'+data+'</span>';
		            	}
	                    return data;
	                  }
	            },
	            { "data": "EXCHANGE_RATE", "width": "60px" },
	            { "data": "AFTER_TOTAL", "width": "60px" ,'class':'total_amount',
	            	"render": function ( data, type, full, meta ) {
	            		if(full.SQL_TYPE=='charge'){
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
	            { "data": "REF_NO", "width": "200px"},
	            { "data": "MBL_NO", "width": "60px"},
	            { "data": "HBL_NO", "width": "60px"},
	            { "data": "CONTAINER_NO", "width": "100px"},
	            { "data": "TRUCK_TYPE", "width": "100px"},
	          ]
	      });

      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })
      
      //查看应收应付对账结果
    	$('#checkOrderAll').click(function(){
    		searchData(); 
    	 });

     var searchData=function(){
    	  var checked = '';
	     	 if($('#checkOrderAll').prop('checked')==true){
	     		 checked = 'Y';
	     	 }
    	  
          var order_no = $.trim($("#order_no").val()); 
          var customer = $("#customer").val(); 
          var customer_input = $("#customer_input").val().trim(); 
          var sp = $("#sp").val(); 
          var sp_input = $("#sp_input").val().trim(); 
          var type = $("#type").val(); 
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/costCheckOrder/list?order_no="+order_no
          			   +"&checked="+checked
          			   +"&customer_id="+customer
			           +"&customer_id="+customer
			           +"&customer_name_like="+customer_input
			           +"&sp_id="+sp
			           +"&sp_name_like="+sp_input
			           +"&type_equals="+type
		               +"&create_stamp_begin_time="+start_date
		               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
      };
      
      //计算创建对账单的总额
      var amount = 0;
      var sum = 0;
      	//选择是否是同一个客户
      	var itemIds=[];
		var cnames = [];
		$('#eeda_table').on('click','input[type="checkbox"]',function () {
				var cname = $(this).parent().siblings('.SP_NAME')[0].textContent;
				var id = $(this).parent().parent().attr('id');
				var amountStr = $(this).parent().siblings('.total_amount')[0].textContent;
		    	  if(amountStr!=''){
			    	  amount = parseFloat(amountStr);
		    	  }
				if($(this).prop('checked')==true){	
						if(cnames.length > 0 ){
							if(cnames[0]!=cname){
								$.scojs_message('请选择同一个结算公司', $.scojs_message.TYPE_ERROR);
								$(this).attr('checked',false);
								return false;
							}else{
								sum+=amount;
								cnames.push(cname);
							}
						}else{
							sum+=amount;
							cnames.push(cname);	
						}
						itemIds.push(id);
				}else{
					cnames.pop(cname);
					itemIds.pop(id);
					sum-=amount;
				}
				$("#totalAmountSpan").text(parseFloat(sum).toFixed(2));
    	 });
		
      	//checkbox选中则button可点击
		$('#eeda_table').on('click','.checkBox',function(){
			
			var hava_check = 0;
			$('#eeda_table input[type="checkbox"]').each(function(){	
				var checkbox = $(this).prop('checked');
	    		if(checkbox){
	    			hava_check=1;
	    		}	
			})
			if(hava_check>0){
				$('#createBtn').attr('disabled',false);
			}else{
				$('#createBtn').attr('disabled',true);
			}
		});
      
		$('#createBtn').click(function(){
			$('#createBtn').attr('disabled',true);
        	
        	var total = parseFloat($('#totalAmountSpan').text());
        	$('#itemId').val(itemIds);
        	$('#totalAmount').val(total);
        	$('#createForm').submit();
        })
        
        
        //全选
        $('#allCheck').click(function(){
       	 var f = false;
     	   	 var flag = 0;
   	 	   $("#eeda_table .checkBox").each(function(){
   	 		  var sp_name = $(this).parent().siblings('.SP_NAME')[0].textContent;
   	 		  if(cnames[0]==undefined){
   	 			 cnames.push(sp_name);
   	 			 f = true;
   	 		  }
   	 		  if(cnames[0]!=sp_name){
   	 			  flag++;
   	 		  }
   	 	    })
       	 if(this.checked==true){
   		 	    if(flag>0){
   		 	    	$.scojs_message('不能全选，包含不同结算公司', $.scojs_message.TYPE_ERROR);
   		 	    	$(this).prop('checked',false);
   		 	    	if(f==true){
   		 	    		cnames[0]=undefined;
   		 	    	}
   		 	    }else{
   		 	    	 $("#eeda_table .checkBox").prop('checked',true);
   		 	    	 $("#eeda_table .checkBox").each(function(){
   		 	    		 var id = $(this).parent().parent().attr('id');
   		 	    		 var sp_name = $(this).parent().siblings('.SP_NAME')[0].textContent;
   		 	    		 itemIds.push(id);
   		 	    		 cnames.push(sp_name);
   		 	    	 })
   		 	    }
       	 }else{
       		 $("#eeda_table .checkBox").prop('checked',false);
       		 if(flag==0){
   	 	    	 $("#eeda_table .checkBox").each(function(){
   	 	    		 var id = $(this).parent().parent().attr('id');
   	 	    		 var sp_name = $(this).parent().siblings('.SP_NAME')[0].textContent;
   	 	    		 itemIds.pop(id);
   	 	    		cnames.pop(sp_name);
   	 	    	 })
       		 }
       	 }
   	 	   if(cnames.length>0){
   	 		  $("#createBtn").prop('disabled',false);
   	 	   }else{
   	 		  $("#createBtn").prop('disabled',true);
   	 	   }
        })
        $("#eeda_table").on('click','.checkBox',function(){
   		   $("#allCheck").prop("checked",$("#eeda_table .checkBox").length == $("#eeda_table .checkBox:checked").length ? true : false);
        });
		$("body").on('click',function(){
			$("#allCheck").prop("checked",$("#eeda_table .checkBox").length == $("#eeda_table .checkBox:checked").length ? true : false);
		});
        
      
  });
});