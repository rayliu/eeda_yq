
$(document).ready(function() {
    $('#menu_cost').addClass('active').find('ul').addClass('in');
    var ids = [];
    var orderNos = [];
    var change_amount = [];
    //var j_amount = [];
    var sum = 0;
    var j_sum = 0;
    var spType =[];
    var allType =[];
    $("input[name='allCheck']").click(function(){
    	$("#uncheckedCostCheck-table input[name='order_check_box']").each(function () { 
            if(allType.length!=0){
                if(allType[0] != $(this).attr("spname")){
                alert("请选择相同的供应商!");
                return false;
                }
             }
             if(spType.length!=0){
                if(spType[0] != $(this).attr("spname")){
                alert("请选择相同的供应商!");
                return false;
                }
             }
             this.checked = !this.checked;
    		if($(this).prop("checked") == true){
    			$(this).parent().parent().clone().appendTo($("#checkedCostCheckList"));
    			ids.push($(this).attr("id"));
    			orderNos.push($(this).attr("order_no"));
    			change_amount.push($(this).attr("change_amount"));
                allType.push($(this).attr("spname"));
                spType.push($(this).attr("spname"));
    			sum =eval(change_amount.join("+"));//求和
    			$("#checkedOrderId").val(ids);
    			$("#checkedOrderNo").val(orderNos);
    			$("#amount").html(parseFloat(sum).toFixed(2));
    			if(ids.length>0){
    				$("#saveBtn").attr("disabled",false);
    			}
    		}
    		else{
    			ids.splice($.inArray($(this).attr('id'),ids),1);
    			change_amount.splice($.inArray($(this).attr('change_amount'),change_amount),1);
    			orderNos.splice($.inArray($(this).attr('id'),orderNos),1);
                allType.splice($.inArray($(this).attr('spname'),allType),1);
                spType.splice($.inArray($(this).attr('spname'),spType),1);
    			//$("#checkedCostCheckList").remove("tbody",$(this).parent().parent());
    			//$("#checkedCostCheckList").$(this).parent().parent().remove();
    			var id=$(this)[0].id;
    			var rows = $("#checkedCostCheckList").children();
    			for(var i=0; i<rows.length;i++){
    				var row = rows[i];
    				if(id==$(row).find("input").attr("id")){
    					row.remove();
    					//$("#checkedCostCheckList").children().splice(i,1);
    				}
    			}
    			var sum_f = $(this).attr("change_amount");
    			var x_sum=parseInt(sum)-parseInt(sum_f);
    			sum=parseInt(sum)-parseInt(sum_f);
    			$("#checkedOrderId").val(ids);
    			$("#checkedOrderNo").val(orderNos);
    			$("#amount").html(parseFloat(x_sum).toFixed(2));
    			if(ids.length<=0){
    				$("#saveBtn").attr("disabled",true);
    			}
    		}    
  
         });

    });
	//datatable, 动态处理
    var uncheckedCostCheckTable = $('#uncheckedCostCheck-table').dataTable({
        "bProcessing": true, 
        "bFilter": false, //不需要默认的搜索框
        "bSort": true, 
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.DID);
			$(nRow).attr('ids', aData.ID);
			$(nRow).attr('order_ty', aData.BUSINESS_TYPE);
			return nRow;
		},
        "sAjaxSource": "/costCheckOrder/unSelectedList",
        "aoColumns": [ 
            { "mDataProp": null, "sWidth":"20px", "bSortable": false,
                "fnRender": function(obj) {
                	//alert(obj.aData.CHANGE_AMOUNT.find("change_amount"));
                	var amount = 0;
	               	 if($(obj.aData.CHANGE_AMOUNT).val()!=null){
	               		 amount = $(obj.aData.CHANGE_AMOUNT).val();
	               	 }else{
	               		amount = obj.aData.PAY_AMOUNT;
	               	 }
	               	 
                	var strcheck='<input  type="checkbox" name="order_check_box" change_amount="'+amount+'" spname="'+obj.aData.SPNAME+'" id="'+obj.aData.ID+'" class="checkedOrUnchecked" order_no="'+obj.aData.BUSINESS_TYPE+'">';;
                	//判断obj.aData.ID 是否存在 list id 
                	console.log(ids);
                	 for(var i=0;i<ids.length;i++){
                		 console.log(i + ":"+ids[i]);
                		 console.log("obj.aData.ID="+obj.aData.ID);
                         if(ids[i]==obj.aData.ID){                        	 
                        	 return strcheck= '<input   checked="checked" type="checkbox" name="order_check_box" change_amount="'+amount+'" spname="'+obj.aData.SPNAME+'" id="'+obj.aData.ID+'" class="checkedOrUnchecked" order_no="'+obj.aData.BUSINESS_TYPE+'">'; 
                        	
                         }
                     }
                	 return strcheck;
                }
            },
            {"mDataProp":"BUSINESS_TYPE", "sWidth":"80px","class":"order_type"},            	
            {"mDataProp":"BOOKING_NOTE_NUMBER", "sWidth":"200px"},
            {"mDataProp":"SERIAL_NO", "sWidth":"200px"},
            {"mDataProp":"REF_NO", "sWidth":"200px"},
            {"mDataProp":"TO_NAME", "sWidth":"130px"},
            {"mDataProp":"PLANNING_TIME", "sWidth":"140px"},
            {"mDataProp":"AMOUNT", "sWidth":"40px"},
            {"mDataProp":"PAY_AMOUNT", "sWidth":"60px"},
            {"mDataProp":"CHANGE_AMOUNT","sWidth":"60px",
            	"fnRender": function(obj) {
            		if(obj.aData.BUSINESS_TYPE!="成本单"){
            			if(obj.aData.CHANGE_AMOUNT!=''&& obj.aData.CHANGE_AMOUNT != null){
            				return "<input type='text' style='width:60px' name='change_amount' id='change' value='"+obj.aData.CHANGE_AMOUNT+"'/>";   
            			}
            			else {
            				if(obj.aData.PAY_AMOUNT!=null){
            					return "<input type='text' style='width:60px' name='change_amount' value='"+obj.aData.PAY_AMOUNT+"'/>";
            				}
            				else{
            					return "<input type='text' style='width:60px' name='change_amount' value='0'/>";
            				}
            			}
              	  	}
            		else{
            			return ""+obj.aData.PAY_AMOUNT;
            		}
                }
            },
            {"mDataProp":"OFFICE_NAME", "sWidth":"90px"}, 
            {"mDataProp":"SPNAME","sClass":"spname", "sWidth":"200px"},
            {"mDataProp":"CUSTOMER_NAME", "sWidth":"120px"},
            {"mDataProp":"CUSTOMER_ORDER_NO", "sWidth":"200px"},
            {"mDataProp":"ORDER_NO", "sWidth":"210px", 
                    "fnRender": function(obj) {
                      var order_no = obj.aData.ORDER_NO;
                      var suborder_no=order_no.substring(order_no.length-2,order_no.length);
                      if(suborder_no=='DB'){
                        return order_no;
                      }else{
                          return eeda.getUrlByNo(obj.aData.ID, obj.aData.ORDER_NO);
                      }
                    }
                },
            {"mDataProp":"RECEIVINGUNIT", "sWidth":"130px"},
            {"mDataProp":"TRANSFER_ORDER_NO", "sWidth":"200px"},
            {"mDataProp":"CREATE_STAMP", "sWidth":"180px"}, 
            {"mDataProp":"RETURN_ORDER_COLLECTION", "sWidth":"90px"},  
		    {"mDataProp":null, "sWidth":"90px",
                "fnRender": function(obj) {
                    return "未收款";
            }},
            {"mDataProp":null, "sWidth": "160px", 
                "fnRender": function(obj) {
                    if(obj.aData.STATUS=='new'){
                        return '新建';
                    }else if(obj.aData.STATUS=='checking'){
                        return '已发送对帐';
                    }else if(obj.aData.STATUS=='confirmed'){
                        return '已审核';
                    }else if(obj.aData.STATUS=='completed'){
                        return '已结算';
                    }else if(obj.aData.STATUS=='cancel'){
                        return '取消';
                    }
                    return obj.aData.STATUS;
                }
            },                        
            {"mDataProp":"FROM_NAME", "sWidth":"150px"},   
            {"mDataProp":"VOLUME", "sWidth":"50px"},                        
            {"mDataProp":"WEIGHT", "sWidth":"40px"},                                           
            {"mDataProp":"REMARK", "sWidth":"150px",
            	 "fnRender": function(obj) {
            		 var remark = obj.aData.REMARK;
            		 if(remark==null)
            			 remark='';
            		 return "<textarea style='' name='remark'/>"+remark+"</textarea>";
            	 }
            },
            {"mDataProp": null, 
                "sWidth": "20px",                
                "fnRender": function(obj) {
                	return "<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"' order_type='"+obj.aData.BUSINESS_TYPE+"'><i class='fa fa-trash-o fa-fw'> </i>删除</a>";
                }
            }            
        ]     
    });		
    
    //动态更改备注
    $("#uncheckedCostCheck-table").on('blur', 'textarea', function(e){
		e.preventDefault();
		var order_type = $(this).parent().parent().attr('order_ty');
		var remark = $(this).val();
		var order_id = $(this).parent().parent().attr('ids');
		$.post('/costCheckOrder/updateOrderRemark', {order_type:order_type,remark:remark,order_id:order_id}, function(data){
			 if(data.success){
	            $.scojs_message('备注给更新成功', $.scojs_message.TYPE_OK);
	         }else{
	            $.scojs_message('备注给更新失败', $.scojs_message.TYPE_ERROR);
	         }
	    },'json');
		uncheckedCostCheckTable.fnDraw();
	});
    
    
    $("#uncheckedCostCheck-table").on('click', '.finItemdel', function(e){
		e.preventDefault();
		var id = $(this).attr('code');
		var order_type = $(this).attr('order_type');
		$.get('/costCheckOrder/delete',{id:id,order_type:order_type},function(data){
			if(data.success){
				$.scojs_message('撤销成功', $.scojs_message.TYPE_OK);
				uncheckedCostCheckTable.fnDraw();
			}else{
				$.scojs_message('撤销成功', $.scojs_message.TYPE_ERROR);
			}
			
		});
	});
    
    

    var checkedCostCheckTable = $('#checkedCostCheck-table').dataTable({
        "bProcessing": true, 
    	"bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"iDisplayLength": 25,
    	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	//"bServerSide": true,
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	//"sAjaxSource": "/chargeCheckOrder/createList2",
    	"aoColumns": [ 
			  {"mDataProp":null, "sWidth":"20px"},                        
			  {"mDataProp":null, "sWidth":"80px"},                        
			  {"mDataProp":null, "sWidth":"200px"},                        
			  {"mDataProp":null, "sWidth":"200px"},                        
			  {"mDataProp":null, "sWidth":"200px"},                        
			  {"mDataProp":null, "sWidth":"40px"},                        
			  {"mDataProp":null, "sWidth":"60px"},                        
			  {"mDataProp":null, "sWidth":"90px"},                        
			  {"mDataProp":null, "sWidth":"200px"},                        
			  {"mDataProp":null, "sWidth":"180px"},                        
			  {"mDataProp":null, "sWidth":"180px"},                        
			  {"mDataProp":null, "sWidth":"90px"},                        
			  {"mDataProp":null, "sWidth":"90px"},                        
			  {"mDataProp":null, "sWidth":"160px"},
			  {"mDataProp":null, "sWidth":"150px"},
			  {"mDataProp":null, "sWidth":"150px"},
			  {"mDataProp":null, "sWidth":"50px"},
			  {"mDataProp":null, "sWidth":"40px"},
			  {"mDataProp":null, "sWidth":"150px"}                      
		] 
    	
    });
    // 未选中列表
	$("#uncheckedCostCheck-table").on('click', '.checkedOrUnchecked', function(e){	
		
		if($(this).prop("checked") == true){
			if(spType.length!=0){
				if(spType[0] != $(this).parent().siblings('.spname')[0].innerHTML){
					alert("请选择相同的供应商!");
					return false;
				}
			}
			if($(this).parent().siblings('.spname')[0].innerHTML != ''){
				spType.push($(this).parent().siblings('.spname')[0].innerHTML);
			}
			$(this).parent().parent().clone().appendTo($("#checkedCostCheckList"));
			ids.push($(this).attr('id'));
			orderNos.push($(this).attr('order_no'));
			change_amount.push($(this).attr('change_amount'));
			sum =eval(change_amount.join("+"));//求和
			$("#checkedOrderId").val(ids);
			$("#checkedOrderNo").val(orderNos);
			$("#amount").html(parseFloat(sum).toFixed(2));
			if(ids.length>0){
				$("#saveBtn").attr("disabled",false);
			}
		}
		else{
			ids.splice($.inArray($(this).attr('id'),ids),1);
			change_amount.splice($.inArray($(this).attr('change_amount'),change_amount),1);
			orderNos.splice($.inArray($(this).attr('order_no'),orderNos),1);
			if(spType.length != 0){
				spType.splice($(this).parent().siblings('.spname')[0].innerHTML, 1);
			}
			//$("#checkedCostCheckList").remove("tbody",$(this).parent().parent());
			//$("#checkedCostCheckList").$(this).parent().parent().remove();
			var id=$(this)[0].id;
			var rows = $("#checkedCostCheckList").children();
			for(var i=0; i<rows.length;i++){
				var row = rows[i];
				if(id==$(row).find('input').attr('id')){
					row.remove();
					//$("#checkedCostCheckList").children().splice(i,1);
				}
			}
			var sum_f = $(this).attr('change_amount');
			var x_sum=parseFloat(sum)-parseFloat(sum_f);
			sum=parseFloat(sum)-parseFloat(sum_f);
			$("#checkedOrderId").val(ids);
			$("#checkedOrderNo").val(orderNos);
			$("#amount").html(parseFloat(x_sum).toFixed(2));
			if(ids.length<=0){
				$("#saveBtn").attr("disabled",true);
			}
		}
	});
	// 已选中列表
	$("#checkedCostCheck-table").on('click', '.checkedOrUnchecked', function(e){
        $(this).parent().parent().appendTo($("#uncheckedCostCheckList"));
		if($(this).prop("checked") == false){
			//j_amount.push($(this).attr('amount'));
			j_sum =$(this).attr('change_amount');//eval(j_amount.join("+"));
			var xj_sum=parseFloat(sum)-parseFloat(j_sum);
			sum=parseFloat(sum)-parseFloat(j_sum);
			//amount=parseFloat(a);
			$("#amount").html(parseFloat(xj_sum).toFixed(2));
			//j_amount.splice($.inArray($(this).attr('id'),j_amount),1);
			if(ids.length != 0){
				ids.splice($.inArray($(this).attr('id'),ids),1);
				change_amount.splice($.inArray($(this).attr('change_amount'),change_amount),1);
				$("#checkedOrderId").val(ids);
				if(ids.length<=0){
					$("#saveBtn").attr("disabled",true);
				}
			}
			if(orderNos.length != 0){
				orderNos.splice($.inArray($(this).attr('order_no'),orderNos),1);
				$("#checkedOrderNo").val(orderNos);
			}
		}
	});
	
	$('#saveBtn').click(function(e){
        e.preventDefault();
        $('#createForm').submit();
    });
	$("#uncheckedCostCheck-table").on('blur', 'input:text', function(e){
		e.preventDefault();
		var paymentId = $(this).parent().parent().attr("id");
		var departId = $(this).parent().parent().attr("ids");
		var ty = $(this).parent().parent().attr("order_ty");
		var name = $(this).attr("name");
		var value = $(this).val();
		if(value==0){      
			$.scojs_message('调整金额失败,金额不能为0', $.scojs_message.TYPE_ERROR);
			uncheckedCostCheckTable.fnDraw();
			 return false; 
		 }
		 if(isNaN(value)){      
			 alert("调整金额为数字类型");
		 }
		 else{
			 $.post('/costCheckOrder/updateDepartOrderFinItem', {ty:ty,departId:departId,paymentId:paymentId, name:name, value:value}, function(data){
				 if(data.success){
                    $.scojs_message('调整金额成功', $.scojs_message.TYPE_OK);
                 }else{
                    $.scojs_message('调整金额失败', $.scojs_message.TYPE_ERROR);
                 }
		    	},'json');
			 uncheckedCostCheckTable.fnDraw();
		 }
	});
	$("#checkedCostCheckOrder").click(function(){
		$("#checked").show();
	});
	$("#uncheckedCostCheckOrder").click(function(){
		uncheckedCostCheckTable.fnDraw();
	});
	
	$("#uncheckedCostCheck-table_wrapper").on('blur', 'input', function(e){
		e.preventDefault();
		var orderNos = $("#orderNos").val();
		var ids=$("#orderIds").val();
		var paymentId = $(this).parent().parent().attr("id");
		var departId = $(this).parent().parent().attr("ids");
		var ty = $(this).parent().parent().attr("order_ty");
		var name = $(this).attr("name");
		var value = $(this).val();
		 if(isNaN(value)){      
			 alert("调整金额为数字类型");
		 }else{
			 /*$.post('/costCheckOrder/updateDepartOrderFinItem', {orderNos:orderNos,ty:ty,departId:departId,paymentId:paymentId,ids:ids, name:name, value:value}, function(data){
				 $.scojs_message('调整金额成功', $.scojs_message.TYPE_OK);
				 $("#debitAmount").html(data.changeAmount);
				 $("# costAmount").html(data.actualAmount); 
				 $("#total_amount").val(data.changeAmount);
		    	},'json');*/
		 }
	}); 
    	// 没选中供应商，焦点离开，隐藏列表
    	$('#sp_id_input').on('blur', function(){
     		$('#sp_id_list').hide();
            refreshCreateList();
     	});

    	//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
    	$('#sp_id_list').on('blur', function(){
     		$('#sp_id_list').hide();
     	});

    	$('#sp_id_list').on('mousedown', function(){
    		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    	});

    	// 选中供应商
    	$('#sp_id_list').on('mousedown', '.fromLocationItem', function(e){
            refreshCreateList();
            
    });
    	


    $('#createBtn').click(function(e){
        e.preventDefault();
        //获取选取回单的ID, 放到数组中
        var chk_value =[];    
        $('input[name="order_check_box"]:checked').each(function(){    
           chk_value.push($(this).val());    
        }); 

        var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable">'+
                        '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
                        'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
                    '</div>';
        $('body').append(alerMsg);
        $('#message_trigger_err').on('click', function(e) {
            e.preventDefault();
            
        });
        //$(body).append(chk_value.length==0 ?'你还没有选择任何应付单据！':chk_value);

        $('#ids').val(chk_value);
        if(!$('#returnOrderSearchForm').valid()){
            return false;
        }
        if(chk_value.length==0 ){
            $.scojs_message('你还没有勾选任何应收回单.', $.scojs_message.TYPE_ERROR);
            return false;
        }

        $('#createForm').submit();
    });


    $('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN',
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#beginTime_filter').trigger('keyup');
    });
    $('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#endTime_filter').trigger('keyup');
    });
    var refreshCreateList = function() {
    	var booking_id = $("#booking_id").val();
    	var orderNo = $("#orderNo_filter").val();
    	var sp = $("#sp_id_input").val();
        var sp_id2 = $("#sp_id").val();
    	var no = $("#no").val();
    	var beginTime = $("#beginTime_filter").val();
    	var endTime = $("#endTime_filter").val();
    	var type = $("#order_type_filter").val();
    	var status = $("#order_status_filter").val();
    	var serial_no = $("#serial_no").val();
    	uncheckedCostCheckTable.fnSettings().sAjaxSource = "/costCheckOrder/unSelectedList?sp_id2="+sp_id2
														+"&beginTime="+beginTime
														+"&endTime="+endTime
														+"&orderNo="+orderNo
														+"&no="+no
														+"&type="+type
														+"&status="+status
    	                                                +"&booking_id="+booking_id
    													+"&serial_no="+serial_no;
    	uncheckedCostCheckTable.fnDraw();
    	
    	
    };
    $("#orderNo_filter,#booking_id,#no,#beginTime_filter,#endTime_filter,#serial_no").on('keyup',function(){
    	refreshCreateList();
    });
    $("#order_type_filter,#order_status_filter").on('change',function(){
    	
    	refreshCreateList();
    });
} );