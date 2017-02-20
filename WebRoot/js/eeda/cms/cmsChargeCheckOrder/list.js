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
        
        var dataTable = eeda.dt({
            id: 'uncheckedEeda-table',
            paging: true,
            serverSide: false,
            ajax: "/cmsChargeCheckOrder/list",
            columns:[
			      { "width": "10px", "orderable": false,
				    "render": function ( data, type, full, meta ) {
			            var strcheck='<input type="checkbox" class="checkBox" name="order_check_box" id="checkbox_'+full.CPOID+'" value="'+full.CPOID+'">';
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
	            { "data": "CREATE_STAMP", "width": "100px"},
	            { "data": "BOOKING_NO", "width": "200px"},
	            { "data": "SP_NAME", "width": "120px","class":"COMPANY_ABBR",
	            	"render": function ( data, type, full, meta ) {
	            		return data;
	            	}
	            },
	            { "data": "BOOKING_NO", "width": "200px"},//装箱放式
	            { "data": "BOOKING_NO", "width": "200px"},//报关单录入
	            { "data": "G_ZLSCF", "width": "160px",
	            	"render": function ( data, type, full, meta ) {
	            		if(full.G_ZLSCF==null){
	            			return '0.00';
	            		}
	            	 var  aa=parseFloat(full.G_ZLSCF).toFixed(2);
	            		return aa;
	            	}
	            },
	            { "data": "G_FTF", "width": "100px",
	            	"render": function ( data, type, full, meta ) {
	            		if(full.G_FTF==null){
	            			return '0.00';
	            		}
	            		 var  aa=parseFloat(full.G_FTF).toFixed(2);
	            		return aa;
	            	}
	            },
	            { "data": "G_PZF", "width": "100px",
	            	"render": function ( data, type, full, meta ) {
	            		if(full.G_PZF==null){
	            			return '0.00';
	            		}
	            		var  aa=parseFloat(full.G_PZF).toFixed(2);
	            		return aa;
	            	}
	            },
	            { "data": "G_XDF", "width": "100px",
	            	"render": function ( data, type, full, meta ) {
	            		if(full.G_XDF==null){
	            			return '0.00';
	            		}
	            		var  aa=parseFloat(full.G_XDF).toFixed(2);
	            		return aa;
	            	}
	            },
	            { "data": "G_WLDLF", "width": "100px",
	            	"render": function ( data, type, full, meta ) {
	            		if(full.G_WLDLF==null){
	            			return '0.00';
	            		}
	            		var  aa=parseFloat(full.G_WLDLF).toFixed(2);
	            		return aa;
	            	}
	            },
	            { "data": "G_GKF", "width": "100px",
	            	"render": function ( data, type, full, meta ) {
	            		if(full.G_GKF==null){
	            			return '0.00';
	            		}
	            		var  aa=parseFloat(full.G_GKF).toFixed(2);
	            		return aa;
	            	}
	            },
	            { "data": "G_MTF", "width": "100px",
	            	"render": function ( data, type, full, meta ) {
	            		if(full.G_MTF==null){
	            			return '0.00';
	            		}
	            		var  aa=parseFloat(full.G_MTF).toFixed(2);
	            		return aa;
	            	}
	            },
	            { "width": "100px","class":'matou_fine',
	            	"render": function ( data, type, full, meta ) {
	            		if(full.ORDER_TYPE=='cost'){
	            			return '<span style="color:red;">'+'-'+'</span>';
		            	}
		            	var matoufine=0,wldlf,gkf,mtf;
		            	wldlf=parseFloat(full.G_WLDLF) ;gkf=parseFloat(full.G_GKF); mtf=parseFloat(full.G_MTF);
		            	if(full.G_WLDLF==null){wldlf=0;}
		            	if(full.G_GKF==null){gkf=0;}
		            	if(full.G_MTF==null){mtf=0;}
		            	matoufine=parseFloat(wldlf+gkf+mtf).toFixed(2);
	                    return matoufine;
	                  }
	            },//码头费小计(前三项，G_LHF不)
	            { "data": "G_ZHF","width": "60px",
	            	"render": function ( data, type, full, meta ) {
	            		if(full.G_ZHF==null){
	            			return '0.00';
	            		}
	            		var  aa=parseFloat(full.G_ZHF).toFixed(2);
	            		return aa;
	            	}
	              },
	            { "data": "G_LXF", "width": "60px",
	            	"render": function ( data, type, full, meta ) {
	            		if(full.G_LXF==null){
	            			return '0.00';
	            		}
	            		var  aa=parseFloat(full.G_LXF).toFixed(2);
	            		return aa;
	            	}
	            },
	            { "data": "G_AC", "width": "60px",
	            	"render": function ( data, type, full, meta ) {
	            		if(full.G_AC==null){
	            			return '0.00';
	            		}
	            		var  aa=parseFloat(full.G_AC).toFixed(2);
	            		return aa;
	            	}
	            },
	            { "data": "G_WJF", "width": "60px",
	            	"render": function ( data, type, full, meta ) {
	            		if(full.G_WJF==null){
	            			return '0.00';
	            		}
	            		var  aa=parseFloat(full.G_WJF).toFixed(2);
	            		return aa;
	            	}
	            },
	            { "width": "60px","class":'gongben_fine',
	            	"render": function ( data, type, full, meta ) {
	            		if(full.ORDER_TYPE=='cost'){
	            			return '<span style="color:red;">'+'-'+'</span>';
		            	}
		            	var gongbenfine=0,lxf,ac,wjf;
		            	lxf=parseFloat(full.G_LXF) ;ac=parseFloat(full.G_AC); wjf=parseFloat(full.G_WJF);
		            	if(full.G_LXF==null){lxf=0;}
		            	if(full.G_AC==null){ac=0;}
		            	if(full.G_WJF==null){wjf=0;}
		            	gongbenfine=parseFloat(lxf+ac+wjf).toFixed(2);
	                    return gongbenfine;
	                  }
	             }, //工本费小计（前三项）
	            { "data": "G_PZF", "width": "60px",
	            	"render": function ( data, type, full, meta ) {
	            		if(full.G_PZF==null){
	            			return '0.00';
	            		}
	            		var  aa=parseFloat(full.G_PZF).toFixed(2);
	            		return aa;
	            	}
	            },
	            { "data": "G_RZF", "width": "100px",
	            	"render": function ( data, type, full, meta ) {
	            		if(full.G_RZF==null){
	            			return '0.00';
	            		}
	            		var  aa=parseFloat(full.G_RZF).toFixed(2);
	            		return aa;
	            	}
	            },
	            { "data": "G_YF", "width": "60px",
	            	"render": function ( data, type, full, meta ) {
	            		if(full.G_YF==null){
	            			return '0.00';
	            		}
	            		var  aa=parseFloat(full.G_YF).toFixed(2);
	            		return aa;
	            	}
	            },
	            { "data": "G_DTF", "width": "120px",
	            	"render": function ( data, type, full, meta ) {
	            		
	            		var  aa=parseFloat(full.G_DTF).toFixed(2);
	            		return '小计';
	            	}
	            },//小计
	            { "data": "G_BGF", "width": "60px",
	            	"render": function ( data, type, full, meta ) {
	            		if(full.G_BGF==null){
	            			return '0.00';
	            		}
	            		var  aa=parseFloat(full.G_BGF).toFixed(2);
	            		return aa;
	            	}
	            },
	            { "data": "G_DTF", "width": "120px",
	            	"render": function ( data, type, full, meta ) {
	            		if(full.G_DTF==null){
	            			return '0.00';
	            		}
	            		var  aa=parseFloat(full.G_DTF).toFixed(2);
	            		return aa;
	            	}
	            },
	            { "data": "TOTAL", "width": "120px"},//合计
	            { "data": "TOTAL", "width": "120px"}//备注
	          ]
	      });
        //计算总额
        var calculate=function(){
        	var total=0;
        	dataTable.data().each(function(item,index){
        		if($('#checkbox_'+item.CPOID).prop('checked')){
        			total+=item.TOTAL;
        		}
        	});
        	$('#cny_totalAmountSpan').html(parseFloat(total).toFixed(2));
        	$('#cny_totalAmountSpan').val(parseFloat(total).toFixed(2));

        }

//        var checkedDataTable = eeda.dt({
//            id: 'checkedEeda-table',
////            pageLength: 25,
//            paging: true,
//            serverSide: true, //不打开会出现排序不对
//            ajax: "/chargeCheckOrder/list2",
//            columns:[
//			      { "width": "10px",
//				    "render": function ( data, type, full, meta ) {
//				    	if(full.BILL_FLAG != ''){
//					        if(full.BILL_FLAG != 'Y')
//					    		return '<input type="checkbox" class="checkBox" name="order_check_box" value="'+full.ID+'" >';
//					    	else
//					    		return '<input type="checkbox" class="checkBox" disabled>';
//				    	}else{
//				    		return '';
//				    	}
//				    }
//			      },
//	            { "data": "ORDER_NO", "width": "100px",
//			    	  "render": function ( data, type, full, meta ) {
//	                      return "<a href='/jobOrder/edit?id="+full.JOBID+"'target='_blank'>"+data+"</a>";
//	                  }
//	            },
//	            { "data": "ORDER_EXPORT_DATE", "width": "100px"},
//	            { "data": "CREATE_STAMP", "width": "100px"},
//	            { "data": "TYPE", "width": "60px"},
//	            { "data": "FEE_NAME", "width": "60px"},
//	            { "data": "CUSTOMER_NAME", "width": "100px"},
//	            { "data": "SP_NAME", "width": "100px","sClass":"SP_NAME"},
//	            { "data": "CURRENCY_NAME", "width": "60px"},
//	            { "data": "TOTAL_AMOUNT", "width": "60px"},
//	            { "data": "EXCHANGE_RATE", "width": "60px" },
//	            { "data": "AFTER_TOTAL", "width": "60px" ,'class':'total_amount'},
//	            { "data": "FND", "width": "60px",
//	            	"render": function ( data, type, full, meta ) {
//	            		if(data)
//				    		     return data;
//	            		else
//				    		     return full.DESTINATION;
//	            	}
//	            },
//	            { "data": "VOLUME", "width": "60px",
//	                "render": function ( data, type, full, meta ) {
//	                    return "";
//	                }
//	            },
//	            { "data": "CONTAINER_AMOUNT","width": "60px",
//	            	"render": function ( data, type, full, meta ) {
//		            	if(data){
//		            		var dataArr = data.split(",");
//		            		var a = 0;
//		            		var b = 0;
//		            		var c = 0;
//		            		var dataStr = "";
//		            		for(var i=0;i<dataArr.length;i++){
//		            			if(dataArr[i]=="20GP"){
//		            				a++;
//		            			}
//		            			if(dataArr[i]=="40GP"){
//		            				b++;
//		            			}
//		            			if(dataArr[i]=="45GP"){
//		            				c++;
//		            			}
//		            		}
//		            		if(a>0){
//		            			dataStr+="20GPx"+a+";"
//		            		}
//		            		if(b>0){
//		            			dataStr+="40GPx"+b+";"
//		            		}
//		            		if(c>0){
//		            			dataStr+="45GPx"+c+";"
//		            		}
//		            		return dataStr;
//		            	}else{
//		            		return '';
//		            	}
//	            	}
//	            },
//	            { "data": "NET_WEIGHT", "width": "60px"},
//	            { "data": "REF_NO", "width": "60px"},
//	            { "data": "MBL_NO", "width": "60px"},
//	            { "data": "HBL_NO", "width": "60px"},
//	            { "data": "CONTAINER_NO", "width": "100px"},
//	            { "data": "TRUCK_TYPE", "width": "100px"},
//	            { "data": "BILL_FLAG", "width": "60px",
//	                "render": function ( data, type, full, meta ) {
//	                		if(data){
//	      	            		if(data != 'Y')
//	      				    		    return '未创建对账单';
//	      				    	   else 
//	      				    		  return '已创建对账单';
//	                  	}else{
//	                			return '';
//	                		}
//	    			       }
//	            },
//	            { "data": null, "width": "60px",
//	                "render": function ( data, type, full, meta ) {
//	                    return "";
//	                }
//	            }
//	          ]
//	      });

		//选择是否是同一个客户
		$('#uncheckedEeda-table').on('click',"input[name='order_check_box']",function () {
			var cname = $(this).parent().siblings('.COMPANY_ABBR')[0].textContent;
			if(cnames.length>0){
				if(cnames[0]==cname){
					if($(this).prop('checked')==true){
						itemIds.push($(this).val());
						cnames.push(cname);
					}else{
					    itemIds.splice($.inArray($(this).val(), itemIds), 1);
					    cnames.splice($.inArray(cname, cnames), 1);
					}
				}else{
					$.scojs_message('请选择同一个结算公司', $.scojs_message.TYPE_ERROR);
					$(this).attr('checked',false);
					return false;
				}
			}else{
				if($(this).prop('checked')==true){
					itemIds.push($(this).val());
					cnames.push(cname);
				}else{
				    itemIds.splice($.inArray($(this).val(), itemIds), 1);
				    cnames.splice($.inArray(cname, cnames), 1);
				}
			}
			
			$('#idsArray').val(itemIds);
			// calcTotal();
			calculate();
		});

//				var cname = $(this).parent().siblings('.COMPANY_ABBR')[0].textContent;
//				var after_total = $(this).parent().siblings('.AFTER_TOTAL')[0].textContent;
//				var total_amount = $(this).parent().siblings('.TOTAL_AMOUNT')[0].textContent;
//				var currency_name = $(this).parent().siblings('.CURRENCY_NAME')[0].textContent;
//				if($(this).prop('checked')==true){	
//					if(cnames.length > 0 ){
//							if(cnames[0]==cname){
//								if(total_amount!=''&&!isNaN(total_amount)){}
//							if(after_total!=''&&!isNaN(after_total)){
//								totalAmount += parseFloat(after_total);
//							}
//							cnames.push(cname);
////							$(this).parent().parent().clone().appendTo($("#checkedEeda-table"));
//							if($(this).val() != ''){
//								itemIds.push($(this).val());
//							}
////							$('#checkedEeda-table input[name="order_check_box"]').css("display","none");
//						}else{
//							$.scojs_message('请选择同一个结算公司', $.scojs_message.TYPE_ERROR);
//							$(this).attr('checked',false);
//							return false;
//						}
//					}else{
//						cnames.push(cname);
////						$(this).parent().parent().clone().appendTo($("#checkedEeda-table"));
//						if($(this).val() != ''){
//							itemIds.push($(this).val());
//						}
////						$('#checkedEeda-table input[name="order_check_box"]').css("display","none");
//					}
//
//				}else{
//					itemIds.splice($.inArray($(this).val(), itemIds), 1);
////					var id=$(this)[0].value;
////					var rows = $("#checkedEeda-table").children().children();
////					for(var i=0; i<rows.length ;i++){
////						var row = rows[i];
////						if(id==$(row).find('input').attr('value')){
////							row.remove();
////							$("#checkedCostCheckList").children().splice(i,1);
////						}
////					}
//					if(total_amount!=''&&!isNaN(total_amount)){
//						if(currency_name=='CNY'){
//							cny_totalAmount -= parseFloat(total_amount);
//						}else if(currency_name=='USD'){
//							usd_totalAmount -= parseFloat(total_amount);
//						}else if(currency_name=='HKD'){
//							hkd_totalAmount -= parseFloat(total_amount);
//						}else if(currency_name=='JPY'){
//							jpy_totalAmount -= parseFloat(total_amount);
//						}
//					}
//					if(after_total!=''&&!isNaN(after_total)){
//						totalAmount -= parseFloat(after_total);
//					}
//					cnames.pop(cname);
//			 }
//				
//			 $('#cny_totalAmountSpan').html(cny_totalAmount.toFixed(2));
//			 $('#usd_totalAmountSpan').html(usd_totalAmount.toFixed(2));
//			 $('#hkd_totalAmountSpan').html(hkd_totalAmount.toFixed(2));
//			 $('#jpy_totalAmountSpan').html(jpy_totalAmount.toFixed(2));
//			 $('#totalAmount').val(totalAmount.toFixed(2));
//			 $('#cny_totalAmount').val(cny_totalAmount.toFixed(2));
//			 $('#usd_totalAmount').val(usd_totalAmount.toFixed(2));
//			 $('#hkd_totalAmount').val(hkd_totalAmount.toFixed(2));
//			 $('#jpy_totalAmount').val(jpy_totalAmount.toFixed(2));
//    	 });
		
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