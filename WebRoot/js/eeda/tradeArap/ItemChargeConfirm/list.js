define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco',  'dtColReorder'], function ($, metisMenu) {
	$(document).ready(function() {
		var dataTable = eeda.dt({
	          id: 'eeda_table',
	          colReorder: true,
	          serverSide: true, //不打开会出现排序不对 
	          initComplete: function (settings) {
	              eeda.dt_float_header('eeda_table');        
	              
	          },
	          "drawCallback": function( settings ) {
	        	  if($('#AllCheck').prop("checked")){
	            	  $('#AllCheck').prop("checked",false)
	              }
	              $('#AllCheck').click();
			  },
	          columns: [
	      			{ "width": "10px",
	      				    "render": function ( data, type, full, meta ) {
	      				    	if(full.AUDIT_FLAG != 'Y')
	      				    		return '<input type="checkbox" checked class="checkBox">';
	      				    	else 
	      				    		return '<input type="checkbox" disabled>';
	      				    }
	      			},
	      			{ "data": "ORDER_NO", "width": "100px",
	      		    	  "render": function ( data, type, full, meta ) {
	                          return "<a href='/trJobOrder/edit?id="+full.ORDER_ID+"'target='_blank'>"+data+"</a>";
	                      }
	      			},
	      			{ "data": "ORDER_EXPORT_DATE", "width": "100px"},
	                  { "data": "CREATE_STAMP", "width": "100px"},
	                  { "data": "AUDIT_FLAG", "width": "60px",
	                  	"render": function ( data, type, full, meta ) {
	      			    	if(data != 'Y')
	      			    		return '未确认';
	      			    	else 
	      			    		return '已确认';
	      			    }
	            },
	            { "data": "CUSTOMER_NAME", "width": "100px"},
	            { "data": "TYPE", "width": "60px"},
	            { "data": "SP_NAME", "width": "100px","class":"SP_NAME"},
	            { "data": "CHARGE_NAME", "width": "60px"},
	            { "data": "PRICE", "width": "60px","class":"PRICE flag"},
	            { "data": "AMOUNT","width": "60px"},
	            { "data": "UNIT_NAME", "width": "60px"},
	            { "data": "TOTAL_AMOUNT", "width": "60px"},
	            { "data": "CURRENCY_NAME", "width": "60px","class":"CURRENCY_NAME"},
	            { "data": "EXCHANGE_RATE", "width": "60px"},
	            { "data": "CURRENCY_TOTAL_AMOUNT", "width": "60px"},
	            { "data": "EXCHANGE_CURRENCY_NAME", "width": "60px"},
	            { "data": "EXCHANGE_CURRENCY_RATE", "width": "60px" },
	            { "data": "EXCHANGE_TOTAL_AMOUNT", "width": "60px"},
	            { "data": "REMARK", "width": "180px"}
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
	    
	    $('#resetBtn').click(function(e){
	    	$("#orderForm")[0].reset();
	    });

	    $('#searchBtn').click(function(){
	    	searchData()
	    })

	    var searchData=function(){
	    	var order_no = $.trim($("#order_no").val()); 
	    	var order_export_date_begin_time = $("#order_export_date_begin_time").val();
	    	var order_export_date_end_time = $("#order_export_date_end_time").val();
	    	var customer = $("#customer").val();
	    	var customer_name = $("#customer_input").val().trim(); 
	    	var sp = $("#sp").val();
	    	var sp_name = $("#sp_input").val().trim(); 
	        var start_date = $("#create_stamp_begin_time").val();
	        var end_date = $("#create_stamp_end_time").val();
	        var audit_flag = $("#audit_flag").val();
	        if(audit_flag=='Y'){
	        	$("#confirmBtn").prop("disabled",true);
	        	$("#AllCheck").attr("checked",false);
	        }else{
	        	$("#confirmBtn").prop("disabled",false);
	        }
	        /*  
			查询规则：参数对应DB字段名
	         *_no like
	         *_id =
	         *_status =
			时间字段需成双定义  *_begin_time *_end_time   between
	         */
	        var url = "/tradeItemChargeConfirm/list?order_no="+order_no
	        +"&order_export_date_begin_time="+order_export_date_begin_time
	        +"&order_export_date_end_time="+order_export_date_end_time
	        +"&customer_id="+customer
	        +"&customer_name_like="+customer_name
	        +"&sp_id="+sp
	        +"&sp_name_like="+sp_name
	        +"&create_stamp_begin_time="+start_date
	        +"&create_stamp_end_time="+end_date
	        +"&audit_flag="+audit_flag;
	        dataTable.ajax.url(url).load(false);
	        $("#cny_totalAmountSpan").text(0.00)
	        $("#usd_totalAmountSpan").text(0.00)
	        $("#hkd_totalAmountSpan").text(0.00)
	        $("#jpy_totalAmountSpan").text(0.00)
	    };
	    
	    var count_total = function(){
	    	var sum_cny = 0.00;
			var sum_usd = 0.00;
			var sum_hkd = 0.00;
			var sum_jpy = 0.00;
			$("#eeda_table").find(".PRICE").each(function(){
				self = $(this)
				var price = parseFloat(self.text());
				var currency_name = self.parent().find(".CURRENCY_NAME").text();
				if(!isNaN(price)){
					if(currency_name=='CNY'){
						sum_cny += price;
					}else if(currency_name=='USD'){
						sum_usd += price;
					}else if(currency_name=='HKD'){
						sum_hkd += price;
					}else if(currency_name=='JPY'){
						sum_jpy += price;
					}
				}
			})
			$("#cny_totalAmountSpan").text(sum_cny.toFixed(2))
			$("#usd_totalAmountSpan").text(sum_usd.toFixed(2))
			$("#hkd_totalAmountSpan").text(sum_hkd.toFixed(2))
			$("#jpy_totalAmountSpan").text(sum_jpy.toFixed(2))
		}
	    
	    $("#eeda_table").on("change",".checkBox",function(){
			self = $(this)
			if(!self.prop("checked")){
				self.parent().parent().find(".flag").removeClass('PRICE')
			}else{
				self.parent().parent().find(".flag").addClass('PRICE')
			}
			count_total()
		})
		
		//全选
		$('#AllCheck').click(function(){
			$(".checkBox").prop("checked",this.checked);
			if($('#AllCheck').prop('checked')){
				$(".flag").addClass("PRICE");
			}else{
				$('#confirmBtn').attr('disabled',true);
				$(".flag").removeClass("PRICE");
			}
			count_total();
		});
	    
	    $("#eeda_table").on('click','.checkBox',function(){
	    	$("#AllCheck").prop("checked",$(".checkBox").length == $(".checkBox:checked").length ? true : false);
	    });
	    
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
		
		//确认费用
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
      	$.post('/tradeItemChargeConfirm/costConfirm?itemIds='+itemIds, function(data){
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