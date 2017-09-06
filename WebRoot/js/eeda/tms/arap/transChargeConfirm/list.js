define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','sco', 'dtColReorder','pageguide'], function ($, metisMenu) { 

    $(document).ready(function() {
    	tl.pg.init({
            pg_caption: '本页教程'
        });

        var dataTable = eeda.dt({
            id: 'eeda_table',
            autoWidth: false,
            
            initComplete: function (settings) {
              eeda.dt_float_header('eeda_table');
            },
            colReorder: true,
            // paging: true,
            serverSide: true, //不打开会出现排序不对 
            // ajax: "/transChargeConfirm/list?audit_flag_notequals="+$("#audit_flag").val(),
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
					{ "data": "CABINET_DATE", "width":"72px"//提柜、提货时间
		            },
					{ "data": "CHARGE_TIME", "width": "65px"},
					{ "data": "CONTAINER_NO", "width": "60px"},
					{ "data": "CABINET_TYPE", "width": "60px"},
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
					{ "data": "PRICE", "width": "40px",
						"render":function(data,type,full,meta){
							  data = (parseFloat(data)).toFixed(2)
			                    if(isNaN(data)){
			                    	data = "";
			                    }
							  return data;
						}
					},
					{ "data": "AMOUNT","width": "40px"},
					{ "data": "UNIT_NAME", "width": "40px"},
					{ "data": "TOTAL_AMOUNT", "width": "60px","class":"total_amount",
						"render":function(data,type,full,meta){
							  data = (parseFloat(data)).toFixed(2)
			                    if(isNaN(data)){
			                    	data = "";
			                    }
							  return data;
						}
					},
					{ "data": "CURRENCY_NAME", "width": "60px","class":"currency_name"},
					{ "data": "EXCHANGE_RATE", "width": "60px"},
					{ "data": "CURRENCY_TOTAL_AMOUNT", "width": "70px",
						"render":function(data,type,full,meta){
							  data = (parseFloat(data)).toFixed(2)
			                    if(isNaN(data)){
			                    	data = "";
			                    }
							  return data;
						}
					},
					{ "data": "REMARK", "width": "60px"},
            ]
        });
        
        //base on config hide cols
      dataTable.columns().eq(0).each( function(index) {
          var column = dataTable.column(index);
          $.each(cols_config, function(index, el) {
              
              if(column.dataSrc() == el.COL_FIELD){
                
                if(el.IS_SHOW == 'N'){
                  column.visible(false, false);
                }else{
                  column.visible(true, false);
                }
              }
          });
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
        	cal();
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
//          var customer = $("#customer").val();
//          var customer_name = $("#customer_input").val(); 
          var sp_name = $("#sp_id_input").val(); 
          var start_date = $("#charge_time_begin_time").val();
          var end_date = $("#charge_time_end_time").val();
          var cabinet_date_begin_time = $("#cabinet_date_begin_time").val();
          var cabinet_date_end_time = $("#cabinet_date_end_time").val();
          var audit_flag = $("#audit_flag").val();
          var cabinet_type=$("#cabinet_type").val();
       
          var url = "/transChargeConfirm/list?order_no="+order_no
//			           +"&customer_id="+customer
//			           +"&customer_name_like="+customer_name
//			           +"&sp_id="+sp
			           +"&sp_name="+sp_name
		               +"&charge_time_begin_time="+start_date
		               +"&charge_time_end_time="+end_date
		               +"&cabinet_date_begin_time="+cabinet_date_begin_time
		          	   +"&cabinet_date_end_time="+cabinet_date_end_time
          			   +"&audit_flag="+audit_flag
          			   +"&cabinet_type="+cabinet_type;

          dataTable.ajax.url(url).load();
      };
      
      	//checkbox选中则button可点击
		$('#eeda_table').on('click','.checkBox',function(){
			var checked=$('#eeda_table input[type="checkbox"]:checked').length;
			$('#confirmBtn').attr('disabled',checked==0);
			cal();
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
      
      //计算表的对账总额
      var cal=function(){
      	var cny_total_amount=0;
      	$('#eeda_table tbody tr').each(function(){
      		var currency_name=$(this).find('.currency_name').text();
      		var total_amount=$(this).find('.total_amount').text();
      		var checked=$(this).find('input[type=checkbox]').prop('checked');
      		if(currency_name=='CNY' && total_amount!='' && checked){
      			 cny_total_amount=parseFloat(cny_total_amount)+parseFloat(total_amount);
      		}
      	});
      	$('#cny_totalAmountSpan').html(eeda.numFormat(parseFloat(cny_total_amount).toFixed(2),3));
      }
      
    	
    });
});