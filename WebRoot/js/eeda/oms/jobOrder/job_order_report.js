define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	//未保存或数据不足不生成PDF
	if($('#order_id').val()==''){
		$("#oceanPDF").hide();
		$("#airPDF").hide();
		$("#truckOrderPDF").hide();
	}
	
	$('#printOceanWord').click(function(){
		var order_id = $("#order_id").val();
    	$.post('/jobOrderReport/printOceanWord', {order_id:order_id}, function(data){
    		if(data){
                window.open(data);
    		  }else{
    			  $.scojs_message('生成海运电放保涵word失败', $.scojs_message.TYPE_ERROR);
               }
    	}); 
	})
	
	//生成海运MBLSI
	$('#printOceanSI').click(function(){
		//数据不足提示
    	var alert = '';
    	if($('#ocean_MBLshipper_input').val()==''){
    		alert+='发货人Shipper<br><br>';
    	}
    	if($('#ocean_MBLshipper_info').val()==''){
    		alert+='发货人备注<br><br>';
    	}
    	if($('#ocean_MBLconsignee_input').val()==''){
    		alert+='收货人Consignee<br><br>';
    	}
    	if($('#ocean_MBLconsignee_info').val()==''){
    		alert+='收货人备注<br><br>';
    	}
    	if($('#ocean_MBLnotify_party_info').val()==''){
    		alert+='通知人备注<br><br>';
    	}
    	if($('#vessel').val()==''){
    		alert+='船名<br><br>';
    	}
    	if($('#voyage').val()==''){
    		alert+='航次<br><br>';
    	}
    	if($('#por').val()==''){
    		alert+='收货港 POR<br><br>';
    	}
    	if($('#pol').val()==''){
    		alert+='启运港 POL<br><br>';
    	}
    	if($('#pod').val()==''){
    		alert+='卸货港 POD<br><br>';
    	}
    	if($('#fnd').val()==''){
    		alert+='目的地 FND<br><br>';
    	}
    	if($('#ocean_shipping_mark').val()==''){
    		alert+='唛头 <br><br>';
    	}
    	if($('#ocean_cargo_desc').val()==''){
    		alert+='货物描述 ';
    	}
    	
		if(alert!=''){
			$('#pdfAlertContent').html("以下字段未填，请先填好才能生成PDF<br><br>"+alert);
			$('#pdfAlert').click();
		}else{
		    	var order_id = $("#order_id").val();
		    	$.post('/jobOrderReport/printOceanSI', {order_id:order_id}, function(data){
		    		if(data){
		                window.open(data);
		                $.post('/jobOrder/siflag', {order_id:order_id}, function(data){
		    		    
		                       });
		    		  }else{
		               $.scojs_message('生成海运MBLSI PDF失败', $.scojs_message.TYPE_ERROR);
		               }
		    	}); 
    	}
		
	})
	    //生成海运HBLSI
		$('#printOceanHBLSI').click(function(){
		//数据不足提示
    	var alert = '';
    	if($('#ocean_HBLshipper_input').val()==''){
    		alert+='发货人Shipper<br><br>';
    	}
    	if($('#ocean_HBLshipper_info').val()==''){
    		alert+='发货人备注<br><br>';
    	}
    	if($('#ocean_HBLconsignee_input').val()==''){
    		alert+='收货人Consignee<br><br>';
    	}
    	if($('#ocean_HBLconsignee_info').val()==''){
    		alert+='收货人备注<br><br>';
    	}
    	if($('#ocean_HBLnotify_party_info').val()==''){
    		alert+='通知人备注<br><br>';
    	}
    	if($('#vessel').val()==''){
    		alert+='船名<br><br>';
    	}
    	if($('#voyage').val()==''){
    		alert+='航次<br><br>';
    	}
    	if($('#por').val()==''){
    		alert+='收货港 POR<br><br>';
    	}
    	if($('#pol').val()==''){
    		alert+='启运港 POL<br><br>';
    	}
    	if($('#pod').val()==''){
    		alert+='卸货港 POD<br><br>';
    	}
    	if($('#fnd').val()==''){
    		alert+='目的地 FND<br><br>';
    	}
    	if($('#ocean_shipping_mark').val()==''){
    		alert+='唛头 <br><br>';
    	}
    	if($('#ocean_cargo_desc').val()==''){
    		alert+='货物描述 ';
    	}
    	
		if(alert!=''){
			$('#pdfAlertContent').html("以下字段未填，请先填好才能生成PDF<br><br>"+alert);
			$('#pdfAlert').click();
		}else{
		    	var order_id = $("#order_id").val();
		    	$.post('/jobOrderReport/printOceanHBLSI', {order_id:order_id}, function(data){
		    		if(data){
		                window.open(data);
		                $.post('/jobOrder/siflag', {order_id:order_id}, function(data){
		    		    
		                       });
		    		  }else{
		               $.scojs_message('生成海运HBLSI PDF失败', $.scojs_message.TYPE_ERROR);
		               }
		    	}); 
    	}
		
	})
	
	
	//生成海运HBL PDF
    $('#printOceanHBL,#prinTelextOceanHBL,#printKFHBL,#printKFAgentHBL').click(function(){
    	//数据不足提示
    	var alert = '';
    
    	if($('#ocean_shipper_input').val()==''){
    		alert+='发货人Shipper<br><br>';
    	}
    	if($('#ocean_shipper_info').val()==''){
    		alert+='发货人备注<br><br>';
    	}
    	if($('#ocean_consignee_input').val()==''){
    		alert+='收货人Consignee<br><br>';
    	}
    	if($('#ocean_consignee_info').val()==''){
    		alert+='收货人备注<br><br>';
    	}
    	if($('#ocean_notify_party_info').val()==''){
    		alert+='通知人备注<br><br>';
    	}
    	if($('#hbl_no').val()==''){
    		alert+='HBL号码<br><br>';
    	}
    	if($('#vessel').val()==''){
    		alert+='船名<br><br>';
    	}
    	if($('#voyage').val()==''){
    		alert+='航次<br><br>';
    	}
    	if($('#por').val()==''){
    		alert+='收货港 POR<br><br>';
    	}
    	if($('#pol').val()==''){
    		alert+='启运港 POL<br><br>';
    	}
    	if($('#pod').val()==''){
    		alert+='卸货港 POD<br><br>';
    	}
    	if($('#fnd').val()==''){
    		alert+='目的地 FND<br><br>';
    	}
    	if($('#etd').val()==''){
    		alert+='ETD<br><br>';
    	}
    	if($('#ocean_shipping_mark').val()==''){
    		alert+='唛头 <br><br>';
    	}
    	if($('#ocean_cargo_desc').val()==''){
    		alert+='货物描述 ';
    	}
    	
		if(alert!=''){
			$('#pdfAlertContent').html("以下字段未填，请先填好才能生成PDF<br><br>"+alert);
			$('#pdfAlert').click();
		}else{
	    	var order_id = $("#order_id").val();
	    	var hbl_no = $("#hbl_no").val();
	    	var printHBL = $(this).attr('id');
	    	$.post('/jobOrderReport/printOceanHBL', {order_id:order_id,hbl_no:hbl_no,printHBL:printHBL}, function(data){
	    		if(data){
	                window.open(data);
	             }else{
	               $.scojs_message('生成海运HBL PDF失败', $.scojs_message.TYPE_ERROR);
	               }
	    	}); 
    	}
    		
    });
    
    //生成海运booking PDF
    $('#printOceanBooking').click(function(){
    	//数据不足提示
    	var alert = '';
 
    	if($('#ocean_shipper_input').val()==''){
    		alert+='发货人Shipper<br><br>';
    	}
    	if($('#ocean_shipper_info').val()==''){
    		alert+='发货人备注<br><br>';
    	}
    	if($('#ocean_consignee_input').val()==''){
    		alert+='收货人Consignee<br><br>';
    	}
    	if($('#ocean_consignee_info').val()==''){
    		alert+='收货人备注<br><br>';
    	}
    	if($('#ocean_notify_party_info').val()==''){
    		alert+='通知人备注<br><br>';
    	}
    	if($('#hbl_no').val()==''){
    		alert+='HBL号码<br><br>';
    	}    	
    	if($('#por').val()==''){
    		alert+='收货港 POR<br><br>';
    	}
    	if($('#pol').val()==''){
    		alert+='启运港 POL<br><br>';
    	}
    	if($('#pod').val()==''){
    		alert+='目的港 POD<br><br>';
    	}
    	if($('#etd').val()==''){
    		alert+='ETD<br><br>';
    	}
    	if($('#ocean_shipping_mark').val()==''){
    		alert+='唛头<br><br>';
    	}
    	if($('#ocean_cargo_desc').val()==''){
    		alert+='货物描述 ';
    	}
    	
    	if(alert!=''){
    		$('#pdfAlertContent').html("以下字段未填，请先填好才能生成PDF<br><br>"+alert);
			$('#pdfAlert').click();
		}else{
	    	var order_no = $("#order_no").val();
	    	$.post('/jobOrderReport/printOceanBooking', {order_no:order_no}, function(data){
	    		if(data){
	    			window.open(data);
	    		}else{
	    			$.scojs_message('生成海运booking PDF失败', $.scojs_message.TYPE_ERROR);
	    		}
	    	});    	
    	}
    });
    
    //生成海运头程资料数据不足判断
    $('#oceanHeadDetailBtn').click(function(){
    	//数据不足提示
    	var alert = '';
    	if($('#SONO').val()==''){
    		alert+='SO NO<br><br>';
    	}
    	if($('#head_carrier').val()==''){
    		alert+='头程船公司<br><br>';
    	}
    	if($('#carrier').val()==''){
    		alert+='船公司<br><br>';
    	}
    	if($('#vessel').val()==''){
    		alert+='船名<br><br>';
    	}
    	if($('#pol').val()==''){
    		alert+='启运港 POL<br><br>';
    	}
    	if($('#pod').val()==''){
    		alert+='目的港<br><br>';
    	}
    	if($('#closing_date').val()==''){
    		alert+='截关日期<br><br>';
    	}
    	if($('#etd').val()==''){
    		alert+='ETD<br><br>';
    	}
    	if($('#eta').val()==''){
    		alert+='ETA';
    	}
    	if(alert!=''){
    		$('#pdfAlertContent').html("以下字段未填，请先填好才能生成PDF<br><br>"+alert);
			$('#pdfAlert').click();
		}else{
			//取客户信息
			var customer_id = $('#customer_id').val();
			
			$.post('/customer/search_party_id', {customer_id:customer_id}, function(data){
				$('#head_endPlace').val(data.COMPANY_NAME);
				$('#head_attn').val(data.CONTACT_PERSON);
				$('#head_customer_tel').val(data.PHONE);
				$('#head_fax').val(data.FAX);
			});
			if($('#head_endPlace').val()==""||$('#head_endPlace').val()==undefined){
    			$('#head_endPlace').val($('#ocean_HBLshipper_input').val());
			}
			$('#head_startPlace').val(loginUserName);
			$('#head_date').val(eeda.getDate());
			  
			   
			var container_type =  $($("#ocean_cargo_table tr:eq(1) td:nth-child(4)").find('[name=container_type]')).val();
			var por_input = $("#por_input").val();
			var pod_input = $("#pod_input").val();
			var head_title = $('#head_title').val();
			var container_amount = $('#ocean_cargo_table tr:has(td)').size();
			if(head_title==""){
			    head_title = container_amount+'X'+container_type+' FM '+por_input+' TO '+pod_input;
				$('#head_title').val(head_title);
			}
				
		    var order_export_date = $("#order_export_date").val();
			
		    var head_remark = $("#head_remark").val();
		    if(head_remark==""){
		    	head_remark = "请安排 "+order_export_date+" 西域港报关出口";
		    	$("#head_remark").val(head_remark)
		    }

			$('#oceanHeadDetailBtn1').click();
		}
    })
    
    //生成海运头程资料
    $('#printOceanHead').click(function(){
    	var oceanHead = {}
    	var form = $('#oceanHeadForm input');
    	for(var i = 0; i < form.length; i++){
    		var name = form[i].id;
        	var value =form[i].value;
        	if(name){
        		oceanHead[name] = value;
        	}
    	}
    	oceanHead.id = $('#oceanHeadId').val();
    	oceanHead.order_id = $('#order_id').val();
    	oceanHead.SONO = $('#SONO').val();
    	
		$.post('/jobOrderReport/printOceanHead', {params:JSON.stringify(oceanHead)}, function(data){
				$("#oceanHeadId").val(data.OCEANHEADID);
				if(data){
	                window.open(data.DOWN_URL);
                    var order_id = $('#order_id').val();
                    itemOrder.refleshZeroDocTable(order_id);
	             }else{
	               $.scojs_message('生成海运头程资料失败', $.scojs_message.TYPE_ERROR);
	             }
				
		},'json').fail(function(){
		    	$.scojs_message('生成海运头程资料失败', $.scojs_message.TYPE_ERROR);
		  });
		
    });
    
    //确认MBL标记
    var mblflag=$('#oceanMBLHide').val();
    if(mblflag=='Y'){
    	$('#oceanMBL').attr('disabled',true);
    }
    $('#oceanMBL').click(function(){
    	var order_id = $("#order_id").val();
    	$.post('/jobOrder/mblflag',{order_id:order_id},function(data){
    		if(data.result==true){
    			$.scojs_message('MBL确认成功', $.scojs_message.TYPE_OK);
    		    $('#oceanMBL').attr('disabled',true);
    		}
    		else
    			$.scojs_message('MBL确认失败', $.scojs_message.TYPE_ERROR);
    			
    		},'json').fail(function(){
		    	$.scojs_message('MBL确认失败', $.scojs_message.TYPE_ERROR);
  		  });
    	
    });
    
    if($('#oceanMBLHidden').val()=="Y"){
    	$('#oceanMBL').attr('disabled',true);
    }
    
    
    //确认AFR/AMS标记
    $('#alreadyAFR_AMS').click(function(){
    	var order_id = $("#order_id").val();
    	$.post('/jobOrder/aframsflag',{order_id:order_id},function(data){
    		if(data.result==true){
			    $.scojs_message('AFR/AMS确认成功', $.scojs_message.TYPE_OK);
			    $('#alreadyAFR_AMS').attr('disabled',true);
    		}
		else
			$.scojs_message('AFR/AMS确认失败', $.scojs_message.TYPE_ERROR);
			
		},'json').fail(function(){
	    	$.scojs_message('AFR/AMS确认失败', $.scojs_message.TYPE_ERROR);
		  });
    	
    });
    if($('#alreadyAFR_AMShide').val()=="Y"){
    	$('#alreadyAFR_AMS').attr('disabled',true);
    }
    
    //确认已电放
    $('#alreadyInline').click(function(){
    	var order_id = $("#order_id").val();
    	$.post('/jobOrder/alreadyInlineFlag',{order_id:order_id},function(data){
    		if(data.result==true){
    			$.scojs_message('已电放确认成功', $.scojs_message.TYPE_OK);
    			$('#alreadyInline').attr('disabled',true);
    		}
		else
			$.scojs_message('已电放确认失败', $.scojs_message.TYPE_ERROR);
			
		},'json').fail(function(){
	    	$.scojs_message('已电放确认失败', $.scojs_message.TYPE_ERROR);
		  });
    	
    });
    if($('#alreadyInlineHide').val()=="Y"){
    	$('#alreadyInline').attr('disabled',true);
    }
    
    
    //生成空运booking PDF
    $('#printAirBooking').click(function(){
    			//数据不足提示
    	    	var alert = '';
    	    	if($('#shipper_input').val()==''){
    	    		alert+='发货人Shipper<br><br>';
    	    	}
    	    	if($('#shipper_info').val()==''){
    	    		alert+='发货人备注<br><br>';
    	    	}
    	    	if($('#consignee_input').val()==''){
    	    		alert+='收货人Consignee<br><br>';
    	    	}
    	    	if($('#consignee_info').val()==''){
    	    		alert+='收货人备注<br><br>';
    	    	}
    	    	if($('#notify_party_info').val()==''){
    	    		alert+='通知人备注<br><br>';
    	    	}
    	    	if( $('#air_gross_weight').val()==''){
    	    		alert+='毛重<br><br>';
    	    	}
    	    	if($('#air_net_weight').val()==''){
    	    		alert+='净重';
    	    	}
    	    	if($('#air_volume').val()==''){
    	    		alert+='体积<br><br>';
    	    	}
    	    	if($('#shipping_mark').val()==''){
    	    		alert+='唛头<br><br>';
    	    	}
    	    	if(alert!=''){
    				$('#pdfAlertContent').html("以下字段未填，请先填好才能生成PDF<br><br>"+alert);
    				$('#pdfAlert').click();
    			}else{
		    		var order_no = $("#order_no").val();
		    		$.post('/jobOrderReport/printAirBooking', {order_no:order_no}, function(data){
		    			if(data){
		    				window.open(data);
		    			}else{
		    				$.scojs_message('生成空运booking PDF失败', $.scojs_message.TYPE_ERROR);
		    			}
		    		});    	
    			}
    	});

    //打印生成陆运派车单truckOrderPDF
    $('#truckOrderPDF').click(function(){
    	//判断table是否添加了一行
    	var btnId = $(this).attr("id");
    	if($('#land_table td').length==1){
    		$.scojs_message('请添加一行地点', $.scojs_message.TYPE_ERROR);
    	}else if(btnId=="truckOrderPDF") {
	    	var alert = '';
	    	var a=0;
	    	var b=0;
	    	var c=0;
	    	var d=0;
	    	var e=0;
	    	var f=0;
	    	var g=0;
	    	var h=0;
	    	var i=0;
	    	$('#land_table [name=consignor]').each(function(){
	    		if($.trim($(this).val())==''){
	    			a++;
	    		}
	    	})
	    	$('#land_table [name=consignor_phone]').each(function(){
	    		if($.trim($(this).val())==''){
	    			b++;
	    		}
	    	})
	    	$('#land_table [name=take_address]').each(function(){
	    		if($.trim($(this).val())==''){
	    			c++;
	    		}
	    	})
	    	$('#land_table [name=consignee]').each(function(){
	    		if($.trim($(this).val())==''){
	    			d++;
	    		}
	    	})
	    	$('#land_table [name=consignee_phone]').each(function(){
	    		if($.trim($(this).val())==''){
	    			e++;
	    		}
	    	})
	    	$('#land_table [name=delivery_address]').each(function(){
	    		if($.trim($(this).val())==''){
	    			f++;
	    		}
	    	})
	    	$('#land_table [name=eta]').each(function(){
	    		if($.trim($(this).val())==''){
	    			g++;
	    		}
	    	})
	    	$('#land_table [name=cargo_info]').each(function(){
	    		if($.trim($(this).val())==''){
	    			h++;
	    		}
	    	})
	    	$('#land_table [name=required_time_remark]').each(function(){
	    		if($.trim($(this).val())==''){
	    			i++;
	    		}
	    	})
	    	
	    	if(a>0){
	    		alert+='发货人<br><br>';
	    	}
	    	if(b>0){
	    		alert+='发货人电话<br><br>';
	    	}
	    	if(c>0){
	    		alert+='发货地点<br><br>';
	    	}
	    	if(d>0){
	    		alert+='收货人<br><br>';
	    	}
	    	if(e>0){
	    		alert+='收货人电话<br><br>';
	    	}
	    	if(f>0){
	    		alert+='收货地点<br><br>';
	    	}
	    	if(g>0){
	    		alert+='预计到达时间<br><br>';
	    	}
	    	if(h>0){
	    		alert+='货品信息<br><br>';
	    	}
	    	if(i>0){
	    		alert+='运输及时间要求<br><br>';
	    	}
	    	if(alert!=''){
				$('#pdfAlertContent').html("以下字段未填，请先填好才能生成PDF<br><br>"+alert);
				$('#pdfAlert').click();
			}else{
				var itemIds=[];
				var order_id = $("#order_id").val();
				var k = 0;
				$('#land_table input[type="checkbox"]').each(function(){
					var checkbox = $(this).prop('checked');
					if(checkbox){
						var itemId = $(this).parent().parent().attr('id');
						k++;
				    	$.post('/jobOrderReport/printTruckOrderPDF', {itemId:itemId}, function(data){
							if(data){
								 window.open(data);	
								 $.post('/jobOrder/truckOrderflag', {itemId:itemId,order_id:order_id}, function(data){
									 $.scojs_message('已完成本次派车工作', $.scojs_message.TYPE_OK);
					                });
							}else{
								$.scojs_message('所选中的里面第'+k+'条生成派车单 PDF失败', $.scojs_message.TYPE_ERROR);
							}
						}); 
						itemIds.push(itemId);
					}
				});
			}
    	}
    });
    
    //打印Debit_note
	$('#printDebitNoteBtn').click(function(){
		$(this).attr('disabled', true);
		
			var debit_note = $('input[name=debit_note]:checked').val();
	    	var invoiceNo = $('#invoiceNo').val();
	    	var order_type = $('#type').val();
	      	var itemIds=[];
	      	$('#charge_table input[type="checkbox"]:checked').each(function(){
      			var itemId = $(this).parent().parent().attr('id');
      			itemIds.push(itemId);
	      	 });
	      	var order_no=$('#order_no').val();
	      	var itemIdsStr = itemIds.toString();
	    	 $.post('/jobOrder/saveDebitNote', {itemIds:itemIdsStr,invoiceNo:invoiceNo}, function(data){
	    		 if(data.result==true){
			    	$.post('/jobOrderReport/printDebitNotePDF', {debit_note:debit_note, itemIds:itemIdsStr,order_no:order_no,order_type:order_type}, function(data){
			    		   $('#printDebitNoteBtn').attr('disabled', false);
			                window.open(data);
			    	}).fail(function() { 
		                $.scojs_message('生成DebitNote PDF失败', $.scojs_message.TYPE_ERROR);
		                $('#printDebitNoteBtn').attr('disabled', false);
		              });
	    		 }
	    	 },'json').fail(function() { 
	                $.scojs_message('生成PDF失败', $.scojs_message.TYPE_ERROR);
	                $('#printDebitNoteBtn').attr('disabled', false);
	         });
      
	});   
	
	//打印Invoice(分单)
	$('#land_printDebit .btn-primary').click(function(){
		$(this).attr('disabled', true);
		
		var invoice_land_hbl_no = $('#invoice_land_hbl_no').val();
		var land_ref_no = $('#land_ref_no').val();
		var landIds=[];
		$('#land_table input[type="checkbox"]:checked').each(function(){
			var itemId = $(this).parent().parent().attr('id');
			landIds.push(itemId);
		});
		var landIdsStr = landIds.toString();
		$.post('/jobOrder/saveDebitNoteOfLand', {landIds:landIdsStr,invoice_land_hbl_no:invoice_land_hbl_no,land_ref_no:land_ref_no}, function(data){
			if(data.result==true){
				$.post('/jobOrderReport/printDebitNotePDF', {debit_note:'land_invoice', landIds:landIdsStr}, function(data){
					if(data.result==false){
						$.scojs_message('生成Invoice(分单)PDF失败,没有已选陆运的费用', $.scojs_message.TYPE_ERROR);
					}else{
						window.open(data);
					}
					$('#land_printDebit .btn-primary').attr('disabled', false);
				}).fail(function() { 
					$.scojs_message('生成Invoice(分单)PDF失败', $.scojs_message.TYPE_ERROR);
					$('#land_printDebit .btn-primary').attr('disabled', false);
				});
			}
		},'json').fail(function() { 
			$.scojs_message('生成PDF失败', $.scojs_message.TYPE_ERROR);
			$('#land_printDebit .btn-primary').attr('disabled', false);
		});
		
	});   
	
	//生成陆运的柜货派车单PDF
	$('#cabinet_truck').click(function(){
		var btn_type = $(this).attr('id');
		$("#generate_show").text("生成柜货派车单PDF详情");
		$("#printCabinetTruckMBL").hide();
		$("#printCabinetTruck").show();
		type(btn_type);
	});
	//生成陆运的柜货派车单MBL
	$('#cabinet_truckMBL').click(function(){
		var btn_type = $(this).attr('id');
		$("#generate_show").text("生成柜货派车单MBL详情");
		$("#printCabinetTruck").hide();
		$("#printCabinetTruckMBL").show();
		type(btn_type);
	});
	var type = function(btn_type){
        var cabinet_arrive_date=$('#land_table tbody [type=checkbox]:checked').first().parents('tr').find('[name=ETA]').val();
        var TRANSPORT_COMPANY = $('#land_table tbody [type=checkbox]:checked').first().parents('tr').find('[name=TRANSPORT_COMPANY]').val();
        var CONSIGNEE_input = $('#land_table tbody [type=checkbox]:checked').first().parents('tr').find('[name=CONSIGNEE_input]').val();
        var TAKE_ADDRESS_input = $('#land_table tbody [type=checkbox]:checked').first().parents('tr').find('[name=TAKE_ADDRESS_input]').val();
        
        var order_export_date = new Date($('#order_export_date').val());
        
        var format_order_export_date_dd =formatDate(order_export_date,"dd");
        var format_order_export_date_MMM =formatDate(order_export_date,"MM");
        var format_order_export_date_date = format_order_export_date_dd+'/'+format_order_export_date_MMM;
		$.post('/serviceProvider/searchTruckCompany_id', {TRANSPORT_COMPANY_id:TRANSPORT_COMPANY}, function(data){
			$('#truck_head_end_place').val(data.ABBR);
			$('#truck_head_attn').val(data.CONTACT_PERSON);
			$('#truck_head_customer_tel').val(data.PHONE);
			$('#truck_head_fax').val(data.FAX);
		});
		var cabinet_arrive_remark = "";
		if(btn_type=="cabinet_truckMBL"){
			cabinet_arrive_remark = "请安排于"+cabinet_arrive_date+"到"+TAKE_ADDRESS_input+",待通知收柜。\n";
			cabinet_arrive_remark+="1、进港箱单全部打好。\n";
			cabinet_arrive_remark+="2、请务必准时到达，如迟到，请提前 1 小时联系工厂负责人或者通知我公司!\n";
			cabinet_arrive_remark+="3、请提清洁无损坏集装箱!如发生集装箱破损有污染不适合装货，造成重新提箱或者船期延迟，所产生的费用与责任都有贵司无条件承担!\n";
			cabinet_arrive_remark+="4、 请装柜日9:00之前提供箱封号及皮重!\n";
		}else{
			cabinet_arrive_remark = "请安排"+format_order_export_date_date+"报关出口，吉柜于"+cabinet_arrive_date+"到"+TAKE_ADDRESS_input+",待通知收柜。";
		}
        
        $('#cabinet_arrive_remark').val(cabinet_arrive_remark);
        
        
		$('#truck_head_start_place').val(loginUserName);
		var truck_head_tel=$('#truck_head_tel').val();
		if(!truck_head_tel){
			$('#truck_head_tel').val(loginUserPhone);
		}
		var truck_head_email=$('#truck_head_email').val();
		if(!truck_head_email){
			$('#truck_head_email').val(loginUserEmail);
		}
		$('#truck_head_date').val(eeda.getDate());
	}
	
	//日期格式化 解析网址：http://www.cnblogs.com/xuyangblog/p/4878043.html
	var formatDate= function (now,mask)
    {
        var d = now;
        var zeroize = function (value, length)
        {
            if (!length) length = 2;
            value = String(value);
            for (var i = 0, zeros = ''; i < (length - value.length); i++)
            {
                zeros += '0';
            }
            return zeros + value;
        };
        
        return mask.replace(/"[^"]*"|'[^']*'|\b(?:d{1,4}|m{1,4}|yy(?:yy)?|([hHMstT])\1?|[lLZ])\b/g, function ($0)
        {
            switch ($0)
            {
                case 'd': return d.getDate();
                case 'dd': return zeroize(d.getDate());
                case 'ddd': return ['Sun', 'Mon', 'Tue', 'Wed', 'Thr', 'Fri', 'Sat'][d.getDay()];
                case 'dddd': return ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'][d.getDay()];
//                case 'M': return d.getMonth() + 1;
                case 'M': return zeroize(d.getMonth() + 1);
                case 'MM': return ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'][d.getMonth()];
                case 'MMMM': return ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'][d.getMonth()];
                case 'yy': return String(d.getFullYear()).substr(2);
                case 'yyyy': return d.getFullYear();
                case 'h': return d.getHours() % 12 || 12;
                case 'hh': return zeroize(d.getHours() % 12 || 12);
                case 'H': return d.getHours();
                case 'HH': return zeroize(d.getHours());
                case 'm': return d.getMinutes();
                case 'mm': return zeroize(d.getMinutes());
                case 's': return d.getSeconds();
                case 'ss': return zeroize(d.getSeconds());
                case 'l': return zeroize(d.getMilliseconds(), 3);
                case 'L': var m = d.getMilliseconds();
                    if (m > 99) m = Math.round(m / 10);
                    return zeroize(m);
                case 'tt': return d.getHours() < 12 ? 'am' : 'pm';
                case 'TT': return d.getHours() < 12 ? 'AM' : 'PM';
                case 'Z': return d.toUTCString().match(/[A-Z]+$/);
                // Return quoted strings with the surrounding quotes removed
                default: return $0.substr(1, $0.length - 2);
            }
        });
    };
	

    $('#printCabinetTruck').click(function(){
    	var noType = $('#cabinet_truck_detail input[name="so_no"]').val();
    	pdf_btn(noType);
    });
    $('#printCabinetTruckMBL').click(function(){
    	var noType = $('#cabinet_truck_detail input[name="mbl_no"]').val();
    	pdf_btn(noType);
    });
    var pdf_btn = function(noType){
    	var truckHead = {}
    	var form = $('#truckHeadForm input,#truckHeadForm textarea');
    	for(var i = 0; i < form.length; i++){
    		var name = form[i].id;
        	var value =form[i].value;
        	
        	if(name.indexOf("truck_")==0){
    			var rName = name.replace("truck_","");
    			truckHead[rName] = value;
    		}else{
    			truckHead[name] = value;
    		}
    	}
    	var k = 0;
		$('#land_table input[type="checkbox"]:checked').each(function(){ //land_shipment_table
				truckHead.item_id = $(this).parent().parent().attr('id');
				truckHead.id = $('#truckHeadId').val();
		    	truckHead.order_id = $('#order_id').val(); 
				k++;
				$.post('/jobOrderReport/printCabinetTruck', {params:JSON.stringify(truckHead),noType:noType}, function(data){
					$("#truckHeadId").val(data.TRUCKHEADID);
					if(data){
		                window.open(data.DOWN_URL);
		             }else{
		            	 $.scojs_message('所选中的里面第'+k+'条生成柜货派车单 PDF失败', $.scojs_message.TYPE_ERROR);
		             }
				},'json').fail(function(){
			    	$.scojs_message('生成柜货派车单PDF失败', $.scojs_message.TYPE_ERROR);
			  }); 
		});
    }
    
//    	
//		$.post('/jobOrderReport/printCabinetTruck', {params:JSON.stringify(truckHead)}, function(data){
//				$("#truckHeadId").val(data.TRUCKHEADID);
//				if(data){
//	                window.open(data.DOWN_URL);
//	             }else{
//	               $.scojs_message('生成柜货派车单PDF失败', $.scojs_message.TYPE_ERROR);
//	             }
//				
//		},'json').fail(function(){
//		    	$.scojs_message('生成柜货派车单PDF失败', $.scojs_message.TYPE_ERROR);
//		  });
//		
//    });
    
	
});
});