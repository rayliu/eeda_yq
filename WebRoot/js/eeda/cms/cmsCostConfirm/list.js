define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','sco'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '应付明细查询 | '+document.title;

    	$('#menu_charge').addClass('active').find('ul').addClass('in');

        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: false, //不打开会出现排序不对 
            ajax: "/cmsCostConfirm/list?audit_flag_notequals="+$("#audit_flag").val(),
            columns: [
					{ "width": "10px",
					    "render": function ( data, type, full, meta ) {
					    	if(full.AUDIT_FLAG != 'Y')
					    		return '<input type="checkbox" class="checkBox">';
					    	else 
					    		return '<input type="checkbox" disabled>';
					    }
					},
					{ "data": "ORDER_NO", "width": "100px",
						"render": function ( data, type, full, meta ) {
			            	if(data){
			            		return '<a href="/customPlanOrder/edit?id='+full.CPOBID+'" target="_blank">'+data+'</a>';
			            	}else{
			            		return '';
			            	}
						}
					},
					{ "data": "AUDIT_FLAG", "width": "60px",
					"render": function ( data, type, full, meta ) {
						if(data != 'Y')
							return '未确认';
						else 
							return '已确认';
					}
					},
					{ "data": "CREATE_STAMP", "width": "100px"},
					{ "data": "JOB_TYPE", "width": "60px"},
					{ "data": "SP_NAME", "width": "60px"},
					{ "data": "CHARGE_NAME", "width": "100px"},
					{ "data": "PRICE", "width": "60px"},
					{ "data": "AMOUNT", "width": "60px"},
					{ "data": "CURRENCY_NAME","width": "60px"},
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
          var customer = $("#customer").val(); 
          var sp = $("#sp").val(); 
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          var audit_flag = $("#audit_flag").val();
       
          var url = "/cmsCostConfirm/list?order_no="+order_no
			           +"&customer_id="+customer
			           +"&sp_id="+sp
		               +"&create_stamp_begin_time="+start_date
		               +"&create_stamp_end_time="+end_date
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
	    	 $.post('/cmsCostConfirm/chargeConfirm?itemIds='+itemIds, function(data){
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