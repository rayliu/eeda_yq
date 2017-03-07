define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','sco','validate_cn'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '报关费用明细录入 | '+document.title;
    	$("#breadcrumb_li").text('报关费用明细录入');

    	$('#menu_charge').addClass('active').find('ul').addClass('in');

    	
    	//datatable, 动态处理
		var cnames = [];
		var itemIds=[];
        var totalAmount = 0.0;
        
        var dataTable = eeda.dt({
            id: 'uncheckedEeda-table',
             paging: true,
            serverSide: false,
            ajax: "/expenseEntry/list",
            columns:[
			      { "width": "10px", "orderable": false,
				    "render": function ( data, type, full, meta ) {
			            var strcheck='<input type="checkbox" class="checkBox" name="order_check_box" value="'+full.ORDER_ID+'">';
			        	for(var i=0;i<itemIds.length;i++){
	                         if(itemIds[i]==full.ID){
	                        	 strcheck= '<input type="checkbox" class="checkBox" checked="checked"  name="order_check_box" value="'+full.ORDER_ID+'">';
	                         }
	                     }
			        	return strcheck;
				    }
			      },
	            { "data": "TRACKING_NO", "width": "100px",
			    	  "render": function ( data, type, full, meta ) {
	                      return "<a href='/customPlanOrder/edit?id="+full.ORDER_ID+"' target='_blank'>"+data+"</a>";
	                  }
	            },
	            { "data": "CUSTOME_NAME", "width": "100px"},
	            { "data": "ORDER_NO", "width": "100px"},
	            { "width": "180px",
	            	"render": function ( data, type, full, meta ) {
			            var strcheck='';
			            if(full.CHARGE_MSG) {strcheck+='<span style="color:red">应收：</span><br/>'+full.CHARGE_MSG};
			            if(full.COST_MSG) {strcheck+='<br/><span style="color:red">应付：</span><br/>'+full.CHARGE_MSG};
			        	return strcheck;
			        }
	        	},
	          { "data": "CREATE_STAMP", "width": "100px"},
	          { "data": "COST_MSG", "width": "100px","visible":false},
	          { "data": "CHARGE_MSG", "width": "100px","visible":false}
	          ]
	      });


  //     //选择是否是同一个结算公司
		// $('#uncheckedEeda-table').on('click',"input[name='order_check_box']",function () {
		// 		var cname = $(this).parent().siblings('.SP_NAME')[0].textContent;
		// 		var total_amount = $(this).parent().siblings('.TOTAL_AMOUNT')[0].textContent;
		// 		if($(this).prop('checked')==true){	
		// 			if(cnames.length > 0 ){
		// 					if(cnames[0]==cname){
		// 						if(total_amount!=''&&!isNaN(total_amount)){
		// 							totalAmount += parseFloat(total_amount);
		// 						}
		// 					cnames.push(cname);
		// 					if($(this).val() != ''){
		// 						itemIds.push($(this).val());
		// 					}
		// 				}else{
		// 					$.scojs_message('请选择同一个结算公司', $.scojs_message.TYPE_ERROR);
		// 					$(this).attr('checked',false);
		// 					return false;
		// 				}
		// 			}else{
		// 				if(total_amount!=''&&!isNaN(total_amount)){
		// 					totalAmount += parseFloat(total_amount);
		// 				}
		// 				cnames.push(cname);
		// 				if($(this).val() != ''){
		// 					itemIds.push($(this).val());
		// 				}
		// 			}
		// 		}else{
		// 			itemIds.splice($.inArray($(this).val(), itemIds), 1);
		// 			if(total_amount!=''&&!isNaN(total_amount)){
		// 				totalAmount -= parseFloat(total_amount);
		// 			}
		// 			cnames.pop(cname);
		// 	 }
		// 	//对账
		// 	 $('#totalAmount').val(totalAmount.toFixed(2));
		// 	 $('#totalAmount_val').text(totalAmount.toFixed(2));
  //   	 });
		
		// //查看应收应付对账结果
  //   	$('#checkOrderAll').click(function(){
  //   		searchData(); 
  //   	});
		
  //     	//checkbox选中则button可点击   创建对账单
		// $('#uncheckedEeda-table').on('click',"input[name='order_check_box']",function () {
		// 	if(itemIds.length>0){
		// 		$('#createBtn').attr('disabled',false);
		// 	}else{
		// 		$('#createBtn').attr('disabled',true);
		// 	}
		// });
		
		// $('#createBtn').click(function(){
		// 	$('#createBtn').attr('disabled',true);
			
  //       	$('#idsArray').val(itemIds);
  //       	$('#billForm').submit();
  //       });
  
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
          var customer_name = $('#customer_input').val().trim();
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          var tracking_no= $("#tracking_no").val().trim(); 
          var url = "/expenseEntry/list?checked="+checked
          	   +"&order_no="+order_no
               +"&custome_name="+customer_name
               +"&tracking_no="+tracking_no
	           +"&create_stamp_begin_time="+start_date
	           +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
        }
     
  //    //全选
  //    $('#allCheck').click(function(){
  //   	 if(this.checked==true){
 	//     	 $("#uncheckedEeda-table .checkBox").each(function(){
 	//     		var id = $(this).val();
 	//    		 	var sp_name = $(this).parent().siblings('.SP_NAME')[0].textContent;

 	//    		    var total_amount = $(this).parent().siblings('.TOTAL_AMOUNT')[0].textContent;
 	//    		    if(total_amount == '' || total_amount == null ){
 	//    		    	total_amount = 0.0;
 	//    		    }
 	    		 
 	//     		if(cnames.length==0 && itemIds.length==0){
 	//     			 cnames.push(sp_name);
 	//     		 }else{
 	//     			 if(cnames[0] != sp_name){
 	//     				$.scojs_message('不能全选，包含不同结算公司', $.scojs_message.TYPE_ERROR);
 	//     				$("#uncheckedEeda-table .checkBox").prop('checked',false);
 	//     				cnames = [];
 	//     				itemIds = [];
 	//     				$('#allCheck').prop('checked',false);
 	//     				totalAmount = 0.0;
 	//     				 $('#totalAmount').val(totalAmount.toFixed(2));
 	//     				 $('#totalAmount_val').text(totalAmount.toFixed(2));
 	//     				return false;
 	//     			 }
 	//     		 }
 	//     		 itemIds.push(id);
 	//     		 $(this).prop('checked',true);
 	//     		 totalAmount += parseFloat(total_amount);
 	//     	 })
  //   	 }else{
		// 	 $("#uncheckedEeda-table .checkBox").prop('checked',false);
		// 	 cnames = [];
		// 	 itemIds = [];
		// 	 totalAmount = 0.0;
  //   	 }
	 // 	 if(cnames.length>0){
	 // 		 $("#createBtn").prop('disabled',false);
	 // 	 }else{
	 // 		 $("#createBtn").prop('disabled',true);
	 // 	 }
	 	 
	 // 	 $('#totalAmount').val(totalAmount.toFixed(2));
		//  $('#totalAmount_val').text(totalAmount.toFixed(2));
  //    });   
       
    });
});