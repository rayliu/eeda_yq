define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','sco','validate_cn'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '应收对账单查询 | '+document.title;

    	$('#menu_charge').addClass('active').find('ul').addClass('in');

    	
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/chargeCheckOrder/list?checked="+checked,
            columns:[
			      { "width": "10px",
				    "render": function ( data, type, full, meta ) {
				    	if(full.BILL_FLAG != ''){
					        if(full.BILL_FLAG != 'Y')
					    		return '<input type="checkbox" class="checkBox" name="order_check_box">';
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
	            { "data": null, "width": "60px",
	                "render": function ( data, type, full, meta ) {
	                    return "";
	                }
	            },
	            { "data": "TYPE", "width": "60px"},
	            { "data": "CUSTOMER_NAME", "width": "100px"},
	            { "data": "SP_NAME", "width": "100px","sClass":"SP_NAME"},
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
	          ]
	      });
        
		//选择是否是同一个客户
		var cnames = [];
        var totalrmb = 0.0;
        var totalusd = 0.0;
		$('#eeda-table').on('click',"input[name='order_check_box']",function () {
				var cname = $(this).parent().siblings('.SP_NAME')[0].textContent;
				var rbm_amount = $(this).parent().siblings('.rmb')[0].textContent;
				var usd_amount = $(this).parent().siblings('.usd')[0].textContent;

				if($(this).prop('checked')==true){	
					if(cnames.length > 0 ){
						if(cnames[0]==cname){
							totalrmb += parseFloat(rbm_amount);
							totalusd += parseFloat(usd_amount);
							cnames.push(cname);
						}else{
							$.scojs_message('请选择同一个结算公司', $.scojs_message.TYPE_ERROR);
							$(this).attr('checked',false);
							return false;
						}
					}else{
						totalrmb += parseFloat(rbm_amount);
						totalusd += parseFloat(usd_amount);
						cnames.push(cname);	
					}
				}else{
					totalrmb -= parseFloat(rbm_amount);
					totalusd -= parseFloat(usd_amount);
					cnames.pop(cname);
			 }
				
			 $('#totalAmountSpan').html(totalrmb);
	    	 $('#totalAmountUSDSpan').html(totalusd);
    	 });
		
		//查看应收应付对账结果
		var checked = '';
    	$('#checkOrderAll').click(function(){
    		 checked = '';
	         if($('#checkOrderAll').prop('checked')==true){
	        	 checked = $('#checkOrderAll').val();	        	
	            }
	         var url = "/chargeCheckOrder/list?checked="+checked;	         
	         dataTable.ajax.url(url).load();
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
		$('#eeda-table').on('click',"input[name='order_check_box']",function () {
			
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
			
        	var itemIds=[];
        	$('.checkBox').each(function(){
        		var checkbox = $(this).prop('checked');
        		if(checkbox){
        			var itemId = $(this).parent().parent().attr('id');
        			itemIds.push(itemId);
        		}
        	});
        	
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
          var order_no = $("#order_no").val(); 
          var sp_name = $('#sp_input').val();
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/chargeCheckOrder/list?order_no="+order_no
               +"&sp_name="+sp_name
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
        }
       
    });
});