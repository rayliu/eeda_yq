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
    	if($('#ocean_MBLnotify_party_input').val()==''){
    		alert+='通知人NotifyParty<br><br>';
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
    		alert+='装货港 POL<br><br>';
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
    	if($('#ocean_HBLnotify_party_input').val()==''){
    		alert+='通知人NotifyParty<br><br>';
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
    		alert+='装货港 POL<br><br>';
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
    $('#printOceanHBL').click(function(){
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
    	if($('#ocean_notify_party_input').val()==''){
    		alert+='通知人NotifyParty<br><br>';
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
    		alert+='装货港 POL<br><br>';
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
	    	$.post('/jobOrderReport/printOceanHBL', {order_id:order_id,hbl_no:hbl_no}, function(data){
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
    	if($('#ocean_notify_party_input').val()==''){
    		alert+='通知人NotifyParty<br><br>';
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
    	if($('#route').val()==''){
    		alert+='航线<br><br>';
    	}
    	if($('#voyage').val()==''){
    		alert+='航次<br><br>';
    	}
    	if($('#por').val()==''){
    		alert+='收货港 POR<br><br>';
    	}
    	if($('#pol').val()==''){
    		alert+='装货港 POL<br><br>';
    	}
    	if($('#pod').val()==''){
    		alert+='卸货港 POD<br><br>';
    	}
    	if($('#fnd').val()==''){
    		alert+='目的地 FND<br><br>';
    	}
    	if($('#hub').val()==''){
    		alert+='转运港 HUB<br><br>';
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
			var arrStr = $('#ocean_HBLshipper_info').val();
			var arry = arrStr.split("\n");
			if(arry.length>=2){
				$('#head_attn').val(arry[1]);
			}
			if(arry.length>=3){
				var arr = arry[2].split(" ");
			}
			if(arr!=undefined&&arr.length>=1){
				$('#head_customerTel').val(arr[0].replace("TEL:",""));
			}
			if(arr!=undefined&&arr.length>=2){
				$('#head_fax').val(arr[1].replace("FAX:",""));
			}
			$('#head_endPlace').val($('#ocean_HBLshipper_input').val());
			$('#head_startPlace').val(loginUserName);
			$('#head_date').val(eeda.getDate());
			
			
			 //把提柜码头和还柜码头带到table中
//			   $('#take_wharf,#back_wharf').keyup(function(){
//				   if($('#take_wharf').val()!=''){
//					   $($("#land_table tr:eq(1) td:nth-child(7)").find('input')).val($('#take_wharf').val());
//				   }
//				   if($('#back_wharf').val()!=''){
//					   $($("#land_table tr:eq(2) td:nth-child(8)").find('input')).val($('#back_wharf').val());
//				   }
//			   
//			   });
			
			   
			   
			var container_type =  $($("#ocean_cargo_table tr:eq(1) td:nth-child(4)").find('[name=container_type]')).val();
			var por_input = $("#por_input").val();
			var pod_input = $("#pod_input").val();
			var head_title = $('#head_title').val();

			if(head_title==""){
			    head_title = '1X'+container_type+' FM '+por_input+' TO '+pod_input;
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
    	
		$.post('/jobOrderReport/printOceanHead', {params:JSON.stringify(oceanHead)}, function(data){
				$("#oceanHeadId").val(data.OCEANHEADID);
				if(data){
	                window.open(data.DOWN_URL);
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
    	    	if($('#notify_party_input').val()==''){
    	    		alert+='通知人NotifyParty<br><br>';
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
    	if($('#land_table td').length==1){
    		$.scojs_message('请添加一行地点', $.scojs_message.TYPE_ERROR);
    	}else {
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
				var k = 0;
				$('#land_table input[type="checkbox"]').each(function(){
					var checkbox = $(this).prop('checked');
					if(checkbox){
						var itemId = $(this).parent().parent().attr('id');
						k++;
				    	$.post('/jobOrderReport/printTruckOrderPDF', {itemId:itemId}, function(data){
							if(data){
								 window.open(data);	
								 $.post('/jobOrder/truckOrderflag', {itemId:itemId}, function(data){
						    		    
					                });
							}else{
								$.scojs_message('所选中的里面第'+k+'条生成派车单 PDF失败', $.scojs_message.TYPE_ERROR);
							}
						}); 
						itemIds.push(itemId);
					}
				});
//		    	var order_id = $("#order_id").val();
//				$.post('/jobOrderReport/printTruckOrderPDF', {order_id:order_id}, function(data){
//					if(data){
//					window.open(data);	
//						 $.post('/jobOrder/truckOrderflag', {order_id:order_id}, function(data){
//				    		    
//			                });
//					}else{
//						$.scojs_message('生成派车单 PDF失败', $.scojs_message.TYPE_ERROR);
//					}
//				}); 
			}
    	}
    });
    
    //打印Debit_note
	$('#printDebitNoteBtn').click(function(){
		$(this).attr('disabled', true);
		
			var debit_note = $('input[name=debit_note]:checked').val();
	    	var invoiceNo = $('#invoiceNo').val();
	      	var itemIds=[];
	      	$('#charge_table input[type="checkbox"]:checked').each(function(){
      			var itemId = $(this).parent().parent().attr('id');
      			itemIds.push(itemId);
	      	 });
	      	var order_no=$('#order_no').val();
	      	var itemIdsStr = itemIds.toString();
	    	 $.post('/jobOrder/saveDebitNote', {itemIds:itemIdsStr,invoiceNo:invoiceNo}, function(data){
	    		 if(data.result==true){
			    	$.post('/jobOrderReport/printDebitNotePDF', {debit_note:debit_note, itemIds:itemIdsStr,order_no:order_no}, function(data){
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
		var arrStr = $('#ocean_HBLshipper_info').val();
		var arry = arrStr.split("\n");
		$('#truck_head_attn').val(arry[1]);
		if(arry.length>=2){
			var arr = arry[2].split(" "); 
			if(arr.length>=1){
				$('#truck_head_customerTel').val(arr[0].replace("TEL:",""));
			}
			if(arr.length>=2){
				$('#truck_head_fax').val(arr[1].replace("FAX:",""));
			}
		}
		$('#truck_head_end_place').val($('#ocean_HBLshipper_input').val());
		$('#truck_head_start_place').val(loginUserName);
		$('#truck_head_date').val(eeda.getDate());
	})
    $('#printCabinetTruck').click(function(){
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
		$('#land_table input[type="checkbox"]:checked').each(function(){
				truckHead.item_id = $(this).parent().parent().attr('id');
				truckHead.id = $('#truckHeadId').val();
		    	truckHead.order_id = $('#order_id').val(); 
				k++;
				$.post('/jobOrderReport/printCabinetTruck', {params:JSON.stringify(truckHead)}, function(data){
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
		
    });
    	
    	
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