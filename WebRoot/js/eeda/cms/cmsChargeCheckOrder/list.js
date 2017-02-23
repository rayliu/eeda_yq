define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','sco','validate_cn'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '应收对账单查询 | '+document.title;

    	$('#menu_charge').addClass('active').find('ul').addClass('in');

    	
    	//datatable, 动态处理
		var cnames = [];
		var itemIds=[];
        var totalAmount = 0.0;
        var cny_totalAmount = 0.0;
        var usd_totalAmount = 0.0;
        var hkd_totalAmount = 0.0;
        var jpy_totalAmount = 0.0;
		var exchange_totalAmount = 0.0;
        var exchange_cny_totalAmount = 0.0;
        var exchange_usd_totalAmount = 0.0;
        var exchange_hkd_totalAmount = 0.0;
        var exchange_jpy_totalAmount = 0.0;
        
        
        var dataTable = eeda.dt({
            id: 'uncheckedEeda-table',
            paging: true,
            serverSide: false,
            ajax: "/cmsChargeCheckOrder/list",
            columns:[
			      { "width": "10px", "orderable": false,
				    "render": function ( data, type, full, meta ) {
			            var strcheck='<input type="checkbox" class="checkBox" name="order_check_box" id="'+full.CPOID+'" value="'+full.CPOID+'">';
			        	for(var i=0;i<itemIds.length;i++){
	                         if(itemIds[i]==full.ID){
	                        	 strcheck= '<input type="checkbox" class="checkBox" checked="checked"  name="order_check_box" value="'+full.CPOID+'">';
	                         }
	                     }
			        	return strcheck;
				    }
			      },
	            { "data": "ORDER_NO", "width": "100px",
			    	  "render": function ( data, type, full, meta ) {
	                      return "<a href='/customPlanOrder/edit?id="+full.CPOID+"' target='_blank'>"+data+"</a>";
	                  }
	            },
	            { "data": "DATE_CUSTOM", "width": "100px"},
	            { "data": "BOOKING_NO", "width": "180px"},
	            { "data": "ABBR_NAME", "width": "120px","class":"SP_NAME"},
	            { "data": "FIN_NAME", "width": "200px"},
	            { "data": "AMOUNT", "width": "80px"},
	            { "data": "PRICE", "width": "80px"},
	            { "data": "CURRENCY_NAME", "width": "100px"},
	            { "data": "TOTAL_AMOUNT", "width": "100px","class":"TOTAL_AMOUNT"},
	            { "data": "REMARK", "width": "100px"},
	            { "data": "CUSTOMS_BILLCODE", "width": "120px"},
	            { "data": "CREATE_STAMP", "width": "100px"}
	          ]
	      });
        //计算总额，4+码头费小计+1+工本费+3+2
//        var calculate=function(){
//        	var total=0.0;
//        	dataTable.data().each(function(item,index){
//        		if($(item.CPOID).prop('checked')){
//        			total = item.TOTAL_AMOUNT+total;
//        		}
//        	});
//        	$('#cny_totalAmountSpan').html(parseFloat(total).toFixed(2));
//        	$('#cny_totalAmount').val(parseFloat(total).toFixed(2));
//
//        }

      //选择是否是同一个结算公司
		$('#uncheckedEeda-table').on('click',"input[name='order_check_box']",function () {
				var cname = $(this).parent().siblings('.SP_NAME')[0].textContent;
				var total_amount = $(this).parent().siblings('.TOTAL_AMOUNT')[0].textContent;
//				var currency_name = $(this).parent().siblings('.CURRENCY_NAME')[0].textContent;
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
			//对账
			 $('#cny_totalAmountSpan').html(totalAmount.toFixed(2));
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
          var customer_name = $('#customer_input').val().trim();
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
          var url = "/cmsChargeCheckOrder/list?checked="+checked
          	   +"&order_no="+order_no
               +"&sp_name="+sp_name
               +"&customer_name="+customer_name
               +"&type_equals="+type
               +"&custom_export_date_end_time="+order_export_date_end_time
               +"&custom_export_date_begin_time="+order_export_date_begin_time
	           +"&create_stamp_begin_time="+start_date
	           +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
        }
     
     //全选
     $('#allCheck').click(function(){
    	 var f = false;
  	   	 var flag = 0;
	 	   $("#uncheckedEeda-table .checkBox").each(function(){
	 		  var sp_name = $(this).parent().siblings('.COMPANY_ABBR')[0].textContent;
	 		  if(cnames[0]==undefined){
	 			 cnames.push(sp_name);
	 			 f = true;
	 		  }
	 		  if(cnames[0]!=sp_name){
	 			  flag++;
	 		  }
	 	    });
    	 if(this.checked==true){
		 	    if(flag>0){
		 	    	$.scojs_message('不能全选，包含不同结算公司', $.scojs_message.TYPE_ERROR);
		 	    	$(this).prop('checked',false);
		 	    	if(f==true){
		 	    		cnames=[];
		 	    	}
		 	    }else{
		 	    	 $("#uncheckedEeda-table .checkBox").prop('checked',true);
		 	    	 $("#uncheckedEeda-table .checkBox").each(function(){
		 	    		 var id = $(this).parent().parent().val();
		 	    		 var sp_name = $(this).parent().siblings('.COMPANY_ABBR')[0].textContent;
		 	    		 itemIds.push(id);
		 	    		 cnames.push(sp_name);
		 	    	 })
		 	    }
    	 }else{
    		 $("#uncheckedEeda-table .checkBox").prop('checked',false);
    		 if(flag==0){
	 	    	 $("#uncheckedEeda-table .checkBox").each(function(){
	 	    		 var id = $(this).parent().parent().val();
	 	    		 var sp_name = $(this).parent().siblings('.COMPANY_ABBR')[0].textContent;
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
     });


     $("#uncheckedEeda-table").on('click','.checkBox',function(){
		   $("#allCheck").prop("checked",$("#uncheckedEeda-table .checkBox").length == $("#uncheckedEeda-table .checkBox:checked").length ? true : false);
     });
     
     $("body").on('click',function(){
    	 $("#allCheck").prop("checked",$("#uncheckedEeda-table .checkBox").length == $("#uncheckedEeda-table .checkBox:checked").length ? true : false);
     });
     
    	 
     
       
    });
});