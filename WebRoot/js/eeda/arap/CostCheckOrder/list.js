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
            { "data": null, "width": "60px",
                "render": function ( data, type, full, meta ) {
                    return "";
                }
            },
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
            { "data": null, "width": "60px",
                "render": function ( data, type, full, meta ) {
                    return "";
                }
            },
            { "data": "MBL_NO", "width": "60px"},
            { "data": "CONTAINER_NO", "width": "100px"},
          ]
      });

      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
          var order_no = $.trim($("#order_no").val()); 
          var customer = $("#customer").val(); 
          var sp = $("#sp").val(); 
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
			           +"&customer_id="+customer
			           +"&sp_id="+sp
		               +"&create_stamp_begin_time="+start_date
		               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
      };
      
      //计算创建对账单的总额
      var amount = 0;
      var sum = 0;
      $('#eeda_table').on('click','.checkBox',function(){
    	  var id = $(this).parent().parent().attr('id');
    	  var amountStr = $($('#'+id+' td')[8]).text();
    	  if(amountStr!=''){
	    	  amount = parseFloat( amountStr );
	    	  if(this.checked==true){
	    		  sum+=amount;
	    	  }else{
	    		  sum-=amount;
	    	  }
	    	  $("#totalAmountSpan").text(parseFloat(sum).toFixed(2));
    	  }
      })
      
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
        	var itemIds=[];
        	var abbrs=[];
        	$('#eeda_table input[type="checkbox"]').each(function(){ 
        		if(this.checked==true){
        			var itemId = $(this).parent().parent().attr('id');
        			var abbr = $($('#'+itemId+' td')[7]).text();
        			itemIds.push(itemId);
        			abbrs.push(abbr);
        		}
        	});
        	var a = abbrs[0];
        	var b = 0;
        	for(var i=1;i<abbrs.length;i++){
        		if(abbrs[i]!=a){
        			b++;
        		}
        	}
        	if(b>0){
        		alert("请选择相同结算公司");
        		$('#createBtn').attr('disabled',false);
        	}else{
	        	var totalAmount = parseFloat($("#totalAmountSpan").text());
	        	location.href ="/costCheckOrder/create?totalAmount="+totalAmount+"&itemIds="+itemIds;
        	}
        })
      
  });
});