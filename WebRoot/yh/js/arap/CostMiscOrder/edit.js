$(document).ready(function() {
	if(order_no){
		document.title = order_no +' | '+document.title;
	}
	$('#menu_cost').addClass('active').find('ul').addClass('in');
	
	
	//from表单验证
/*	var validate = $('#costMiscOrderForm').validate({
        rules: {
       	customer_filter: {required: true}
        },
        messages : {	             
        	customer_filter : {required:  "请选择一个客户"}
        }
    });*/
	
	
	
	 //打印
  	 $("#printBtn").on('click',function(){
  	    	var order_no = $("#arapMiscCostOrderNo").html();
  	    	if(order_no != null && order_no != ""){
  		    	$.post('/report/printArapMiscCost', {order_no:order_no}, function(data){
  		    		if(data.indexOf(",")>=0){
  						var file = data.substr(0,data.length-1);
  		    			var str = file.split(",");
  		    			for(var i = 0 ;i<str.length;i++){
  		    				window.open(str[i]);
  		    			}
  					}else{
  						window.open(data);
  					}
  		    	});
  	    	}else{
  	    		$.scojs_message('当前单号为空', $.scojs_message.TYPE_ERROR);
  	    	}
  	    	

  	    });
  	if($("#audit_status").val()!="new"&&$("#audit_status").val()!="新建"){
  		$("#saveCostMiscOrderBtn").attr("disabled",true);
  	}
  	$('input[name="biz_type"],input[name="cost_to_type"]').on('click',function(){
  		var biz_type=$('input[name="biz_type"]:checked').val();
  		var type = $('input[name="cost_to_type"]:checked').val();
  		if(biz_type=='biz'&& type=='insurance'){
  			$.scojs_message('保险只能做非业务', $.scojs_message.TYPE_WARN);
  			$('[name=biz_type]:eq(1)').prop('checked', true)
  			$("input[name=cost_to_type][value='insurance']").attr("checked",'checked');
  		}
  	});
	var saveCostMiscOrder = function(e, callback){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#costMiscOrderForm").valid()){
	       	return;
        }
        
        
        var type = $('input[name="cost_to_type"]:checked').val();
        if(type=='customer'){
        	if($('#customer_filter').val()==''){
        		$.scojs_message('客户不能为空', $.scojs_message.TYPE_WARN);
        		$("#saveCostMiscOrderBtn").attr("disabled",false);
            	return;
        	}
        	
        }else if(type=='sp'){
        	if($('#sp_filter').val()==''){
        		$.scojs_message('供应商不能为空', $.scojs_message.TYPE_WARN);
        		$("#saveCostMiscOrderBtn").attr("disabled",false);
            	return;
        	}
        	
        }else if(type=='insurance'){
        	if($('#insurance_filter').val()==''){
        		$.scojs_message('保险公司不能为空', $.scojs_message.TYPE_WARN);
        		$("#saveCostMiscOrderBtn").attr("disabled",false);
            	return;
        	}
        	
        }else{
        	if($('#others_name').val()==''){
        		$.scojs_message('收款人不能为空', $.scojs_message.TYPE_WARN);
        		$("#saveCostMiscOrderBtn").attr("disabled",false);
            	return;
        	}
        	
        }
        
        
        
        var biz_type = $('input[name="biz_type"]:checked').val();
        if(biz_type=='biz'){
        	if($('#sp_filter').val()==''){
        		$.scojs_message('供应商不能为空', $.scojs_message.TYPE_WARN);
        		$("#saveCostMiscOrderBtn").attr("disabled",false);
            	return;
        	}
        }
        
        
        
        
        var tableRows = $("#feeItemList-table tr");
        var itemsArray=[];
        for(var index=0; index<tableRows.length; index++){
        	if(index==0)
        		continue;

        	var row = tableRows[index];
        	var id = $(row).attr('id');
        	if(!id){
        		id='';
        	}
        	
        	var item={
        		CUSTOMER_ORDER_NO: $(row.children[0]).find('input').val(), 
			 	ITEM_DESC: $(row.children[1]).find('input').val(),
			 	NAME: $(row.children[2]).find('select').val(),
			 	AMOUNT: $(row.children[3]).find('input').val(),
			 	STATUS: '新建',
			 	ID: id,
			 	ACTION: 'create'
        	};
        	itemsArray.push(item);
        }

		var amount = 0.00;
        for(var i=0; i<itemsArray.length; i++){
        	amount+=Number(itemsArray[i].AMOUNT);
        	$('#totalAmountSpan').html(amount);
        }
        
        
      //add deleted items
        for(var index=0; index<deletedIds.length; index++){
        	var id = deletedIds[index];
        	var item={
        		ID: id,
			 	ACTION: 'delete'
        	};
        	itemsArray.push(item);
        }

        var order={
        	costMiscOrderId: $('#costMiscOrderId').val(),
        	biz_type: $('input[name="biz_type"]:checked').val(),
        	cost_to_type: $('input[name="cost_to_type"]:checked').val(),
        	customer_id: $('#customer_id').val(),
        	sp_id: $('#sp_id').val(),
        	insurance_id: $('#insurance_id').val(),
        	route_from: $('#locationForm').val(),
        	route_to: $('#locationTo').val(),
        	others_name: $('#others_name').val(),
        	ref_no: $('#ref_no').val(),
        	remark: $('#remark').val(),
        	amount: amount,
        	items: itemsArray
        };
  

       
        
		//异步向后台提交数据
		$.post('/costMiscOrder/save',{params:JSON.stringify(order)}, function(data){
			var order = data.order;
			if(order.ID>0){
				$("#ref_order_no").text(order.REF_ORDER_NO);
				$("#arapMiscCostOrderNo").html(order.ORDER_NO);
				$("#create_time").html(order.CREATE_STAMP);
				$("#costMiscOrderId").val(order.ID);
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				$("#saveCostMiscOrderBtn").attr("disabled",false);
				contactUrl("edit?id", order.ID);
				feeTable.fnClearTable();

				for (var i = 0; i < data.itemList.length; i++) {
					var item = data.itemList[i];
					feeTable.fnAddData({
						ID: item.ID,
					 	CUSTOMER_ORDER_NO: item.CUSTOMER_ORDER_NO,
					 	ITEM_DESC: item.ITEM_DESC,
					 	NAME: item.NAME,
					 	AMOUNT: item.AMOUNT,
					 	CHANGE_AMOUNT: item.CHANGE_AMOUNT,
					 	STATUS: '新建'
					 });
				};
				window.location.reload();
			}else{
				$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
				$("#saveCostMiscOrderBtn").attr("disabled",false);
			}
		},'json');
	};
   
	/*--------------------------------------------------------------------*/
	var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
	    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
	    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
	    '</div>';
	$('body').append(alerMsg);

	$('#message_trigger_err').on('click', function(e) {
		e.preventDefault();
	});
	
	
	//获取省份的城市
    $('#mbProvince').on('change', function(){
    	alert("dd");
    	//var inputStr = $(this).parent("option").attr('id'); 
			var inputStr = $(this).val();
			$.get('/serviceProvider/city', {id:inputStr}, function(data){
				var cmbCity =$("#cmbCity");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
				toLocationList.show();
			},'json');
		});
    //获取城市的区县
    $('#cmbCity').on('change', function(){
    	//var inputStr = $(this).parent("option").attr('id'); 
			var inputStr = $(this).val();
			var code = $("#notify_location").val(inputStr);
			$.get('/serviceProvider/area', {id:inputStr}, function(data){
				var cmbArea =$("#cmbArea");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");	
				}
				toLocationList.show();
			},'json');
		});
    
    $('#cmbArea').on('change', function(){
     	//var inputStr = $(this).parent("option").attr('id'); 
			var inputStr = $(this).val();
			var code = $("#notify_location").val(inputStr);
		});         

     // 回显城市
     var hideProvince = $("#hideProvince").val();
     $.get('/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
			if(data.length > 0){
				var cmbCity =$("#cmbCity");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				var hideCity = $("#hideCity").val();
				for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideCity){
						cmbCity.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
					}else{
						cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
			}
		},'json');

     // 回显区
     var hideCity = $("#hideCity").val();
     $.get('/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
			if(data.length > 0){
				var cmbArea =$("#cmbArea");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				var hideDistrict = $("#hideDistrict").val();
				for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideDistrict){
						cmbArea.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
					}else{
						cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
			}
		},'json');
    
    //获取全国省份
    $(function(){
     	var province = $("#mbProvinceFrom");
     	$.post('/serviceProvider/province',function(data){
     		province.append("<option>--请选择省份--</option>");
				var hideProvince = $("#hideProvinceFrom").val();
     		for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideProvince){
     				province.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");     				
					}else{
     				province.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
     		
     	},'json');
    });
    
    //获取省份的城市
    $('#mbProvinceFrom').on('change', function(){
			var inputStr = $(this).val();
			$.get('/serviceProvider/city', {id:inputStr}, function(data){
				var cmbCity =$("#cmbCityFrom");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
				
			},'json');
		});
    //获取城市的区县
    $('#cmbCityFrom').on('change', function(){
			var inputStr = $(this).val();
			var code = $("#locationForm").val(inputStr);
			$.get('/serviceProvider/area', {id:inputStr}, function(data){
				var cmbArea =$("#cmbAreaFrom");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");	
				}
				
			},'json');
		});
    
    $('#cmbAreaFrom').on('change', function(){
			var inputStr = $(this).val();
			var code = $("#locationForm").val(inputStr);
		});         
    

    //获取全国省份
    $(function(){
     	var province = $("#mbProvinceTo");
     	$.post('/serviceProvider/province',function(data){
     		province.append("<option>--请选择省份--</option>");
				var hideProvince = $("#hideProvinceTo").val();
     		for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideProvince){
     				province.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
     				
     				
					}else{
     				province.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
     		
     	},'json');
    });
    
    //获取省份的城市
    $('#mbProvinceTo').on('change', function(){
			var inputStr = $(this).val();
			$.get('/serviceProvider/city', {id:inputStr}, function(data){
				var cmbCity =$("#cmbCityTo");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
				
			},'json');
		});
    
    //获取城市的区县
    $('#cmbCityTo').on('change', function(){
			var inputStr = $(this).val();
			var code = $("#locationTo").val(inputStr);
			$.get('/serviceProvider/area', {id:inputStr}, function(data){
				var cmbArea =$("#cmbAreaTo");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");	
				}
				
			},'json');
		});
    
    $('#cmbAreaTo').on('change', function(){
			var inputStr = $(this).val();
			var code = $("#locationTo").val(inputStr);
		});  
    

    // 回显城市
    var hideProvince = $("#hideProvinceFrom").val();
    $.get('/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
			if(data.length > 0){
				var cmbCity =$("#cmbCityFrom");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				var hideCity = $("#hideCityFrom").val();
				for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideCity){
						cmbCity.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
					}else{
						cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
			}
		},'json');

    // 回显区
    var hideCity = $("#hideCityFrom").val();
    $.get('/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
			if(data.length > 0){
				var cmbArea =$("#cmbAreaFrom");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				var hideDistrict = $("#hideDistrictFrom").val();
				for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideDistrict){
						cmbArea.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
					}else{
						cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
			}
		},'json');
    

    // 回显城市
    var hideProvince = $("#hideProvinceTo").val();
    $.get('/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
			if(data.length > 0){
				var cmbCity =$("#cmbCityTo");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				var hideCity = $("#hideCityTo").val();
				for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideCity){
						cmbCity.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
					}else{
						cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
			}
		},'json');

    // 回显区
    var hideCity = $("#hideCityTo").val();
    $.get('/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
			if(data.length > 0){
				var cmbArea =$("#cmbAreaTo");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				var hideDistrict = $("#hideDistrictTo").val();
				for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideDistrict){
						cmbArea.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
					}else{
						cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
			}
		},'json');
	
	
	
	//设置一个变量值，用来保存当前的ID
	var parentId = "costMiscOrderbasic";
	$("#transferOrderMilestoneList").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	$("#costMiscOrderbasic").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	/*--------------------------------------------------------------------*/
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveCostMiscOrderBtn").click(function(e){
 		$("#saveCostMiscOrderBtn").attr("disabled", true);
 		saveCostMiscOrder(e);
	});
	
    if($("#costMiscOrderStatus").text() == 'new'){
    	$("#costMiscOrderStatus").text('新建');
	}
    
    var feeTable = $('#feeItemList-table').dataTable({    	
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"bPaginate": false, //翻页功能
        "bInfo": false,//页脚信息
        "bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	//"sAjaxSource": "/costMiscOrder/costMiscOrderItemList?costMiscOrderId="+$("#costMiscOrderId").val(),
        "fnRowCallback": function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},		
        "aoColumns": [ 
          	{"mDataProp":"CUSTOMER_ORDER_NO", "sWidth": "100px",
          	 "fnRender": function(obj) {
		        if(obj.aData.CUSTOMER_ORDER_NO!='' && obj.aData.CUSTOMER_ORDER_NO != null){
		            return "<input type='text' name='customer_order_no' value='"+obj.aData.CUSTOMER_ORDER_NO+"' class='form-control search-control'>";
		        }else{
		        	 return "<input type='text' name='customer_order_no' class='form-control search-control'>";
		        }
		     }
            },
            {"mDataProp":"ITEM_DESC","sWidth": "130px",
        	    "fnRender": function(obj) {
			        if(obj.aData.ITEM_DESC!='' && obj.aData.ITEM_DESC != null){
			            return "<input type='text' name='item_desc' value='"+obj.aData.ITEM_DESC+"'  class='form-control search-control'>";
			        }else{
			        	 return "<input type='text'  name='item_desc' class='form-control search-control'>";
			        }
			    }
        	},
			{"mDataProp":"NAME","sWidth": "70px",
			    "fnRender": function(obj) {
			        if(obj.aData.NAME!='' && obj.aData.NAME != null){
			        	var str="";
			        	$("#receivableItemList").children().each(function(){
			        		if(obj.aData.NAME == $(this).text()){
			        			str+="<option value='"+$(this).val()+"' selected = 'selected'>"+$(this).text()+"</option>";   
			        			$("#receivableTotal").val(obj.aData.NAME);
			        		}else{
			        			str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        		}
			        	});
			            return "<select name='fin_item_id' class='form-control search-control'>"+str+"</select>";
			        }else{
			        	var str="";
			        	$("#receivableItemList").children().each(function(){
			        		str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        	});
			        	return "<select name='fin_item_id' class='form-control search-control'>"+str+"</select>";
			        }
			 }},
			 {"mDataProp":"AMOUNT",
			    "fnRender": function(obj) {
			        if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
			        	var val = parseFloat(obj.aData.AMOUNT).toFixed(2);
			            return "<input type='text' name='amount' value="+val+" class='form-control search-control'>";
			        }else{
			        	return "<input type='text' name='amount' class='form-control search-control'>";
			        }
			}},
			{"mDataProp":"CHANGE_AMOUNT"},
			{"mDataProp":"STATUS"},
			{"mDataProp": null,
                "fnRender": function(obj) {
               		 return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'><i class='fa fa-trash-o fa-fw'> </i>删除</a>";
                }
            }   
        ]      
    });
   
    
    
	  //删除一行
	    var deletedIds=[];
		$("#feeItemList-table").on('click', '.finItemdel', function(e){
			e.preventDefault();
			var tr = $(this).parent().parent();
			deletedIds.push(tr.attr('id'));
			tr.remove();
		});	
		
		 
		
		//添加一行
		$("#addFee").click(function(){
			 feeTable.fnAddData({
			 	CUSTOMER_ORDER_NO:'',
			 	ITEM_DESC:'',
			 	NAME:'',
			 	AMOUNT: '0',
			 	CHANGE_AMOUNT: '',
			 	STATUS: '新建'
			 });
			 feeTable.fnDraw(); 
		});	
		
	    
	    //获取客户列表，自动填充
	    $('#customer_filter').on('keyup click', function(){
	        var inputStr = $('#customer_filter').val();
	        var companyList =$("#companyList");
	        $.get("/transferOrder/searchPartCustomer", {input:inputStr}, function(data){
	            companyList.empty();
	            for(var i = 0; i < data.length; i++){
	                var abbr = data[i].ABBR;
					var company_name = data[i].COMPANY_NAME;
					if(abbr == null) 
						abbr = '';
					if(company_name == null)
						company_name = '';
					companyList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' company_name='"+company_name+"'>"+abbr+" "+company_name+"</a></li>");
	            }
	        },'json');
	        companyList.css({left:$(this).position().left+"px",top:$(this).position().top+32+"px"}).show();
	    });
	    $('#companyList').on('click', '.fromLocationItem', function(e){        
	        $('#customer_filter').val($(this).attr("company_name"));
	        $("#companyList").hide();
	        var companyId = $(this).attr('partyId');
	        $('#customer_id').val(companyId);
	        //savePartyInfo(companyId,"CUSTOMER");
	    });
	    // 没选中客户，焦点离开，隐藏列表
	    $('#customer_filter').on('blur', function(){
	        $('#companyList').hide();
	    });
	
	    //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
	    $('#customer_filter').on('blur', function(){
	        $('#companyList').hide();
	    });
	
	    $('#companyList').on('mousedown', function(){
	        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	    });

	    //保险查询
	    //获取保险的list，选中信息在下方展示其他信息
	    $('#insurance_filter').on('keyup click', function(){
			var inputStr = $('#insurance_filter').val();
			var insuranceList =$("#insuranceList");
			$.post('/serviceProvider/searchInsurance', {input:inputStr}, function(data){
				insuranceList.empty();
				for(var i = 0; i < data.length; i++){
					var abbr = data[i].ABBR;
					var company_name = data[i].COMPANY_NAME;
					if(abbr == null) 
						abbr = '';
					if(company_name == null)
						company_name = '';
					insuranceList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' company_name='"+company_name+"'>"+abbr+" "+company_name+"</a></li>");
				}
			},'json');
			insuranceList.css({left:$(this).position().left+"px",top:$(this).position().top+32+"px"}).show();
	    });
	    
	    // 没选中保险，焦点离开，隐藏列表
		$('#insurance_filter').on('blur', function(){
	 		$('#insuranceList').hide();
	 	});
	
		//当用户只点击了滚动条，没选保险，再点击页面别的地方时，隐藏列表
		$('#insuranceList').on('blur', function(){
	 		$('#insuranceList').hide();
	 	});
	
		$('#insuranceList').on('mousedown', function(){
			return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
		});
	
		// 选中保险
		$('#insuranceList').on('mousedown', '.fromLocationItem', function(e){
			$('#insurance_filter').val($(this).attr('company_name'));
			var provider = $(this).attr('partyId');
			$('#insurance_id').val(provider);
	        $('#insuranceList').hide();
	        //savePartyInfo(provider,"INSURANCE_PARTY");
	    });
	    
	});
		//供应商查询
		//获取供应商的list，选中信息在下方展示其他信息
		$('#sp_filter').on('keyup click', function(){
			var inputStr = $('#sp_filter').val();
			var spList =$("#spList");
			$.get('/serviceProvider/searchSp', {input:inputStr}, function(data){
				spList.empty();
				for(var i = 0; i < data.length; i++){
					var abbr = data[i].ABBR;
					var company_name = data[i].COMPANY_NAME;
					if(abbr == null) 
						abbr = '';
					if(company_name == null)
						company_name = '';
					spList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' company_name='"+company_name+"'>"+abbr+" "+company_name+"</a></li>");
				}
			},'json');
			spList.css({left:$(this).position().left+"px",top:$(this).position().top+32+"px"}).show();
		});
		
		// 没选中供应商，焦点离开，隐藏列表
		$('#sp_filter').on('blur', function(){
				$('#spList').hide();
			});
		
		//当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
		$('#spList').on('blur', function(){
				$('#spList').hide();
			});
		
		$('#spList').on('mousedown', function(){
			return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
		});
		
		// 选中供应商
		$('#spList').on('mousedown', '.fromLocationItem', function(e){
			$('#sp_filter').val($(this).attr('company_name'));
			var provider = $(this).attr('partyId');
			$('#sp_id').val(provider);
		    $('#spList').hide();
		    //savePartyInfo(provider,"SERVICE_PROVIDER");
		});

function datetimepicker(data){
	if(!$("#saveCarSummaryBtn").prop("disabled")){
		$('.input-append').datetimepicker({  
			    format: 'yyyy-MM-dd',  
			    language: 'zh-CN',
			    autoclose: true,
			    pickerPosition: "bottom-left"
			}).on('changeDate', function(ev){
				$(".bootstrap-datetimepicker-widget").hide();
				$(data).parent().prev("input").focus();
		});
	}
}


//按钮控制
var status = $("#status").val();
if(status == '新建' || status == 'new'){
	$('#addFee').show();  
	$("#saveCostMiscOrderBtn").attr("disabled",false);	
}else{
	$('#addFee').hide();  
	$("#saveCostMiscOrderBtn").attr("disabled",true);
}

