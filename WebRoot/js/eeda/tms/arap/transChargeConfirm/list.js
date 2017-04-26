define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','sco'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '应收明细查询 | '+document.title;

    	$("#breadcrumb_li").text('应收明细');
    	 
    	$('#menu_charge').addClass('active').find('ul').addClass('in');

        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: false, //不打开会出现排序不对 
            ajax: "/transChargeConfirm/list?audit_flag="+$("#audit_flag").val(),
            columns: [
					{ "width": "10px",
					    "render": function ( data, type, full, meta ) {
					    	if(full.AUDIT_FLAG != 'Y')
					    		return '<input type="checkbox" class="checkBox">';
					    	else 
					    		return '<input type="checkbox" disabled>';
					    }
					},
					{ "data": "ORDER_NO", "width": "80px",
						"render": function ( data, type, full, meta ) {
			            	if(data){
			            		return '<a href="/transJobOrder/edit?id='+full.JOBID+'" target="_blank">'+data+'</a>';
			            	}else{
			            		return '';
			            	}
						}
					},
					{ "data": "CREATE_STAMP", "width": "60px"},
					{ "data": "CONTAINER_NO", "width": "60px"},
					{ "data": "SO_NO", "width": "80px"},
					{ "data": "AUDIT_FLAG", "width": "40px",
					"render": function ( data, type, full, meta ) {
						if(data != 'Y')
							return '未确认';
						else 
							return '已确认';
					}
					},
					{ "data": "CUSTOMER", "width": "70px"},
					{ "data": "SP_NAME", "width": "70px"},
					{ "data": "CHARGE_NAME", "width": "60px"},
					{ "data": "PRICE", "width": "40px"},
					{ "data": "AMOUNT","width": "40px"},
					{ "data": "UNIT_NAME", "width": "40px"},
					{ "data": "TOTAL_AMOUNT", "width": "40px"},
					{ "data": "CURRENCY_NAME", "width": "40px"},
					{ "data": "EXCHANGE_RATE", "width": "40px"},
					{ "data": "CURRENCY_TOTAL_AMOUNT", "width": "60px"},
					{ "data": "REMARK", "width": "60px"},
            ]
        });
        
        //全选
        $('#AllCheck').click(function(){
      	  var ischeck = this.checked;
        	$(".checkBox").each(function () {  
                this.checked = ischeck;  
             });  
        	if(ischeck==true){
        		$('#confirmBtn').attr('disabled',false);
        	}else{
        		$('#confirmBtn').attr('disabled',true);
        	}
        });
        
        $("#eeda_table").on('click','.checkBox',function(){
		    $("#AllCheck").prop("checked",$(".checkBox").length == $(".checkBox:checked").length ? true : false);
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
          var audit_flag = $("#audit_flag").val();
       
          var url = "/transChargeConfirm/list?order_no="+order_no
			           +"&customer_id="+customer
			           +"&sp_id="+sp
		               +"&create_stamp_begin_time="+start_date
		               +"&create_stamp_end_time="+end_date
          			   +"&audit_flag="+audit_flag;

          dataTable.ajax.url(url).load();
      };
      
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
				$('#confirmBtn').attr('disabled',false);
			}else{
				$('#confirmBtn').attr('disabled',true);
			}
		});
		
		$('#confirmBtn').click(function(){
			$('#confirmBtn').attr('disabled',true);
	      	var itemIds=[];
	      	$('#eeda_table input[type="checkbox"]').each(function(){
	      		var checkbox = $(this).prop('checked');
	      		if(checkbox){
	      			var itemId = $(this).parent().parent().attr('id');
	      			itemIds.push(itemId);
	      		}
	      	});
	    	 $.post('/transChargeConfirm/chargeConfirm?itemIds='+itemIds, function(data){
	    		 if(data.result==true){
	    			 $.scojs_message('单据确认成功', $.scojs_message.TYPE_OK);
	    			 searchData();
	    			 $('#confirmBtn').attr('disabled', false);
	    		 }
	    	 },'json').fail(function() {
	                $.scojs_message('单据确认失败', $.scojs_message.TYPE_ERROR);
	                $('#confirmBtn').attr('disabled', false);
	              });
      })
      
      
    	
    });
});