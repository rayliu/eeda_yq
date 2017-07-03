define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','sco'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '应收费用明细确认 | '+document.title;
    	$('#breadcrumb_li').text('应收费用明细确认 ');

    	$('#menu_charge').addClass('active').find('ul').addClass('in');

        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: false, //不打开会出现排序不对 
            ajax: "/cmsChargeConfirm/list?audit_flag_notequals="+$("#audit_flag").val(),
            columns: [
					{ "width": "10px",
					    "render": function ( data, type, full, meta ) {
					    	if(full.AUDIT_FLAG != 'Y')
					    		return '<input type="checkbox" class="checkBox">';
					    	else 
					    		return '<input type="checkbox" disabled>';
					    }
					},
					{ "data": "ORDER_NO", "width": "70px",
						"render": function ( data, type, full, meta ) {
			            	if(data){
			            		return '<a href="/customPlanOrder/edit?id='+full.CPOBID+'" target="_blank">'+data+'</a>';
			            	}else{
			            		return '';
			            	}
						}
					},
					{ "data": "AUDIT_FLAG", "width": "50px",
						"render": function ( data, type, full, meta ) {
							if(data != 'Y')
								return '未确认';
							else 
								return '已确认';
						}
					},
					{ "data": "DATE_CUSTOM", "width": "60px",
						"render":function(data,type,full,meta){
							if(!data){
								return '';
							}
							return data.substring(0,10);
						}
					},					
					{ "data": "JOB_TYPE", "width": "60px"},
					{ "data": "SP_NAME", "width": "100px"},
					{ "data": "CHARGE_NAME", "width": "100px"},
					{ "data": "PRICE", "width": "50px"},
					{ "data": "AMOUNT", "width": "50px"},
					{ "data": "CURRENCY_NAME","width": "40px"},
					{ "data": "TOTAL_AMOUNT", "width": "60px"},
					{ "data": "REMARK", "width": "60px"},
					{ "data": "CREATE_STAMP", "width": "60px"}
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
        
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

      var searchData=function(){
          var order_no = $.trim($("#order_no").val()); 
//          var customer = $("#customer").val(); 
          var sp_name = $("#sp_input").val().trim();
          var date_custom_begin_time = $("#date_custom_begin_time").val();
          var date_custom_end_time = $("#date_custom_end_time").val();
          var create_stamp_begin_time = $("#create_stamp_begin_time").val();
          var create_stamp_end_time = $("#create_stamp_end_time").val();
          var audit_flag = $("#audit_flag").val();
       
          var url = "/cmsChargeConfirm/list?order_no="+order_no
//			           +"&customer_id="+customer
			           +"&sp_name="+sp_name
			           +"&date_custom_begin_time="+date_custom_begin_time
		               +"&date_custom_end_time="+date_custom_end_time
		               +"&create_stamp_begin_time="+create_stamp_begin_time
		               +"&create_stamp_end_time="+create_stamp_end_time
          			   +"&audit_flag_notequals="+audit_flag;

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
	      			if(itemId!=undefined){
	      				itemIds.push(itemId);
	      			}
	      		}
	      	});
	      	if(itemIds.length==0){
	      		$.scojs_message('该单据没有费用，请先录入费用', $.scojs_message.TYPE_ERROR);
	      		return;
	      	}
	    	 $.post('/cmsChargeConfirm/chargeConfirm?itemIds='+itemIds, function(data){
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