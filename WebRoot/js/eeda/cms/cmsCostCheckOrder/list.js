define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','sco','validate_cn'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '报关应付对账单 | '+document.title;
    	$("#breadcrumb_li").text('报关应付对账单');

    	$('#menu_charge').addClass('active').find('ul').addClass('in');

    	
    	//datatable, 动态处理
		var cnames = [];
		var itemIds=[];
        var totalAmount = 0.0;
        
        var dataTable = eeda.dt({
            id: 'uncheckedEeda-table',
//            paging: true,
            serverSide: true,
//            ajax: "/cmsCostCheckOrder/list",
            ajax:{
                //url: "/chargeCheckOrder/list",
                type: 'POST'
            },
            columns:[
			      { "width": "10px", "orderable": false,
				    "render": function ( data, type, full, meta ) {
			            var strcheck='<input type="checkbox" class="checkBox" name="order_check_box" value="'+full.ARAP_ID+'">';
			        	for(var i=0;i<itemIds.length;i++){
	                         if(itemIds[i]==full.ID){
	                        	 strcheck= '<input type="checkbox" class="checkBox" checked="checked"  name="order_check_box" value="'+full.ARAP_ID+'">';
	                         }
	                     }
			        	return strcheck;
				    }
			      },
	            { "data": "ORDER_NO", "width": "100px",
			    	  "render": function ( data, type, full, meta ) {
	                      return "<a href='/customPlanOrder/edit?id="+full.ORDER_ID+"' target='_blank'>"+data+"</a>";
	                  }
	            },
	            { "data": "DATE_CUSTOM", "width": "100px"},
	            { "data": "TRACKING_NO", "width": "180px"},
	            { "data": "SP_NAME", "width": "120px","class":"SP_NAME"},
	            { "data": "FIN_NAME", "width": "200px"},
	            { "data": "AMOUNT", "width": "80px"},
	            { "data": "PRICE", "width": "80px"},
	            { "data": "CURRENCY_NAME", "width": "100px"},
	            { "data": "TOTAL_AMOUNT", "width": "100px","class":"TOTAL_AMOUNT",
	            	"render": function ( data, type, full, meta ) {
	            		if(data==null){
	            			data = 0.0;
	            		}
	            		var str = '';
	            		if(full.ORDER_TYPE=='charge'){
	            			str='<span style="color:red">'+(0.0-parseFloat(data))+'</span>';
	            		}else{
	            			str = data;
	            		}
	                    return str
	            	}	
	            },
	            { "data": "REMARK", "width": "100px"},
	            { "data": "CUSTOMS_BILLCODE", "width": "120px"},
	            { "data": "CREATE_STAMP", "width": "100px"}
	          ]
	      });
        
        //选择是否是同一个客户
		$('#uncheckedEeda-table').on('click',"input[name='order_check_box']",function () {
				var cname = $(this).parent().siblings('.SP_NAME')[0].textContent;
				var total_amount = $(this).parent().siblings('.TOTAL_AMOUNT')[0].textContent;
				if($(this).prop('checked')==true){	
					if(cnames.length > 0 ){
						if(cnames[0]==cname){
							if(total_amount!=''&&!isNaN(total_amount)){
								totalAmount += parseFloat(total_amount);
							}
							cnames.push(cname);
							if($(this).val() != ''){
								itemIds.push($(this).val());
							}
						}else{
							$.scojs_message('请选择同一个结算公司', $.scojs_message.TYPE_ERROR);
							$(this).attr('checked',false);
							return false;
						}
					}else{
						if(total_amount!=''&&!isNaN(total_amount)){
							totalAmount += parseFloat(total_amount);
						}
						cnames.push(cname);
						if($(this).val() != ''){
							itemIds.push($(this).val());
						}
					}

				}else{
					itemIds.splice($.inArray($(this).val(), itemIds), 1);
					
					if(total_amount!=''&&!isNaN(total_amount)){
						totalAmount -= parseFloat(total_amount);
					}
					cnames.pop(cname);
			 }
				
			 
			 $('#totalAmount').val(totalAmount.toFixed(2));
			 $('#totalAmount_val').text(totalAmount.toFixed(2));
			
    	 });
		
		//查看应收应付对账结果
    	$('#checkOrderAll').click(function(){
    		searchData(); 
    	});
		
      	//checkbox选中则button可点击   创建对账单
		$('#uncheckedEeda-table').on('click',"input[name='order_check_box']",function () {
			
			if(itemIds.length>0){
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
     	 
           var order_no = $("#order_no").val().trim(); 
           var sp_name = $('#sp_input').val().trim();
           var start_date = $("#create_stamp_begin_time").val();
           var end_date = $("#create_stamp_end_time").val();
           var custom_start_date = $("#date_custom_begin_time").val();
           var custom_end_date = $("#date_custom_end_time").val();
           if(!sp_name){
               $.scojs_message('请选择结算公司', $.scojs_message.TYPE_ERROR);
               return;
           }

           var url = "/cmsCostCheckOrder/list?checked="+checked
           	   +"&order_no="+order_no
           	   +"&sp_name="+sp_name
 	           +"&create_stamp_begin_time="+start_date
 	           +"&create_stamp_end_time="+end_date
 	           +"&date_custom_begin_time="+custom_start_date
	           +"&date_custom_end_time="+custom_end_date;

           dataTable.ajax.url(url).load();
      }
     
	     //全选
	     $('#allCheck').click(function(){
	    	 if(this.checked==true){
	 	    	 $("#uncheckedEeda-table .checkBox").each(function(){
	 	    		var id = $(this).val();
	 	   		 	var sp_name = $(this).parent().siblings('.SP_NAME')[0].textContent;
	
	 	   		    var total_amount = $(this).parent().siblings('.TOTAL_AMOUNT')[0].textContent;
	 	   		    if(total_amount == '' || total_amount == null ){
	 	   		    	total_amount = 0.0;
	 	   		    }
	 	    		 
	 	    		if(cnames.length==0 && itemIds.length==0){
	 	    			 cnames.push(sp_name);
	 	    		 }else{
	 	    			 if(cnames[0] != sp_name){
	 	    				$.scojs_message('不能全选，包含不同结算公司', $.scojs_message.TYPE_ERROR);
	 	    				$("#uncheckedEeda-table .checkBox").prop('checked',false);
	 	    				cnames = [];
	 	    				itemIds = [];
	 	    				$('#allCheck').prop('checked',false);
	 	    				totalAmount = 0.0;
	 	    				 $('#totalAmount').val(totalAmount.toFixed(2));
	 	    				 $('#totalAmount_val').text(totalAmount.toFixed(2));
	 	    				return false;
	 	    			 }
	 	    		 }
	 	    		 itemIds.push(id);
	 	    		 $(this).prop('checked',true);
	 	    		 totalAmount += parseFloat(total_amount);
	 	    	 })
	    	 }else{
				 $("#uncheckedEeda-table .checkBox").prop('checked',false);
				 cnames = [];
				 itemIds = [];
				 totalAmount = 0.0;
	    	 }
		 	 if(cnames.length>0){
		 		 $("#createBtn").prop('disabled',false);
		 	 }else{
		 		 $("#createBtn").prop('disabled',true);
		 	 }
		 	 
		 	 $('#totalAmount').val(totalAmount.toFixed(2));
			 $('#totalAmount_val').text(totalAmount.toFixed(2));
	     });   
    });
});