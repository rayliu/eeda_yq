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
        //反选
		$('#allCheck').click(function(){
	    	$("input[name='order_check_box']").each(function () {  
	            this.checked = !this.checked;  
	         });
		});
		//选择是否是同一个客户
		var cnames = [];
		$('#eeda-table').on('click',"input[name='order_check_box']",function () {
				var cname = $(this).parent().siblings('.SP_NAME')[0].textContent;
				
				if($(this).prop('checked')==true){	
					if(cnames.length > 0 ){
						if(cnames[0]!=cname){
							$.scojs_message('请选择同一个结算公司', $.scojs_message.TYPE_ERROR);
							$(this).attr('checked',false);
							return false;
						}else{
							cnames.push(cname);
						}
					}else{
						cnames.push(cname);	
					}
				}else{
					cnames.pop(cname);
			 }
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
		
		
		 //计算创建对账单的RMB总额
	      var amount = 0;
	      var amountUSD = 0;
	      var totalRBM = 0;
	      var sum = 0;
	      var sumUSD = 0;
	      var sumtotalRBM = 0;
	      $('#eeda-table').on('click',"input[name='order_check_box']",function () {
	    	  var id = $(this).parent().parent().attr('id');
	    	  var amountStr = $($('#'+id+' td')[5]).text();
	    	  var amounUSDtStr = $($('#'+id+' td')[6]).text();

	    	  if(amountStr!='' & amounUSDtStr!='' ){
		    	  amount = parseFloat( amountStr );
		    	  amountUSD = parseFloat( amounUSDtStr );

		    	  if(this.checked==true){
		    		  sum+=amount;
		    		  sumUSD+=amountUSD;

		    	  }else{
		    		  sum-=amount;
		    		  sumUSD-=amountUSD;

		    	  }
		    	  $('#totalAmountSpan').html(parseFloat(sum).toFixed(3));
		    	  $('#totalAmountUSDSpan').html(parseFloat(sumUSD).toFixed(3));

	    	  }
	      });		
		
        
        
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
        	var totalAmount = parseFloat($("#totalAmountSpan").text());
        	var totalAmount = parseFloat($("#totalAmountUSDSpan").text());
        	var OrderIds = itemIds.join(",");
        	location.href ="/chargeCheckOrder/create?totalAmount="+totalAmount+"&OrderIds="+OrderIds;
        	
        	

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