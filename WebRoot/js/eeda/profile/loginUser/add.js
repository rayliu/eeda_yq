define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 
        'validate_cn', 'sco', 'datetimepicker_CN', 'jq_blockui'], function ($, metisMenu) { 

var queryOffice=function(){
	var offices=[];
	$.post('/loginUser/searchAllOffice',function(data){
   		 if(data.length > 0){
   			 var officeSelect = $("select[name='officeSelect']");
   			$("select[name='officeSelect']").each(function(){
   				if($(this).val()!=null&&$(this).val()!=""){
   					var value=$(this).val();
   					var txt = $(this).find("option:selected").text();
   					offices.push($(this).val());
   					$(this).empty();
					$(this).append("<option ></option>");
   					$(this).append("<option value='"+value+"' selected='selected'>"+txt+"</option>");
   				}else{
   					$(this).empty();
   					$(this).append("<option ></option>");
   				}
	   		});
   			
   			for(var i=0; i<data.length; i++){
   				var n=0;
   				for(var j=0;j<offices.length;j++){
   					if(data[i].ID==offices[j]){
   						n=offices[j];
   					}
   				}
		 		if(data[i].ID!=n){
		 			if(data[i].IS_STOP != true){
		 				officeSelect.append("<option value='"+data[i].ID+"'>"+data[i].OFFICE_NAME+"</option>");
		 			}
							 		
				}
   				
   			}
   			
   		 }
	 },'json');
};

var queryCustomer=function(){
	var customers=[];
	$.post('/loginUser/searchAllCustomer',function(data){
   		 if(data.length > 0){
   			 var customerSelect = $("select[name='customerSelect']");
   			$("select[name='customerSelect']").each(function(){
   				if($(this).val()!=null&&$(this).val()!=""){
   					var value=$(this).val();
   					var txt = $(this).find("option:selected").text();
   					customers.push($(this).val());
   					$(this).empty();
					$(this).append("<option ></option>");
   					$(this).append("<option value='"+value+"' selected='selected'>"+txt+"</option>");
   					
   				}else{
   					$(this).empty();
   					$(this).append("<option > </option>");
   				}
	   		});
   			for(var i=0; i<data.length; i++){
   				var n=0;
   				for(var j=0;j<customers.length;j++){
   					if(data[i].PID==customers[j]){
   						n=customers[j];
   					}
   				}
		 		if(data[i].PID!=n){
		 			if(data[i].IS_STOP != true){
		 				customerSelect.append("<option value='"+data[i].PID+"'>"+data[i].COMPANY_NAME+"</option>");
		 			}
		 					 		
				}
   				
   			}
   			
   		 }
	 },'json');
};

var officeList = function(){
	var userId = $("#userId").val();
	$.post('/loginUser/officeList',{userId:userId},function(data){
		var tobdy = $("#tobdy");
		tobdy.empty();
		if(data.userOffice!=null){
			if(data.userOffice.length>0){
				for(var i =0;i<data.userOffice.length;i++){
					//console.log(data.userOffice[i].IS_MAIN);
					if(data.userOffice[i].IS_MAIN !=null && data.userOffice[i].IS_MAIN != false){
						tobdy.append('<tr><td><select class="form-control sOffice" name="officeSelect"></select>'
			                    +' </td><td><input type="radio" checked class="is_main" name="isMain_radio" value="'+data.userOffice[i].OFFICE_ID+'"></td>'
			                    +' <td><a class="btn removeOffice" title="删除"><i class="fa fa-trash-o fa-fw"></i></a></td></tr>');
					}else{
						tobdy.append('<tr><td><select class="form-control sOffice" name="officeSelect"></select>'
			                    +' </td><td><input type="radio"  class="is_main" name="isMain_radio" value="'+data.userOffice[i].OFFICE_ID+'"></td>'
			                    +' <td><a class="btn removeOffice" title="删除"><i class="fa fa-trash-o fa-fw"></i></a></td></tr>');
					}
					$("select[name='officeSelect']:last").append("<option value='"+data.userOffice[i].OFFICE_ID+"'>"+data.userOffice[i].OFFICE_NAME+"</option>");
					queryOffice();
				}
				
			}
		}
	},'json');
};
var customerList = function(){
	var userId = $("#userId").val();
	$.post('/loginUser/customerList',{userId:userId},function(data){
		
		var tobdy = $("#customerTbody");
		tobdy.empty();
		if(data.customerlist!=null){
			if(data.customerlist.length>0){
				for(var i =0;i<data.customerlist.length;i++){
					tobdy.append('<tr><td>'
						+'<select class="form-control customer" name="customerSelect"></select></td>'
		                +' <td><a class="btn removeCustomer" title="删除"><i class="fa fa-trash-o fa-fw"></i></a></td></tr>');
					
					$("select[name='customerSelect']:last").append("<option value='"+data.customerlist[i].CUSTOMER_ID+"'>"+data.customerlist[i].COMPANY_NAME+"</option>");
					
				}
				
			}
			queryCustomer();
		}
	},'json');
};
$(document).ready(function(){
  	$("#addOffice").on('click',function(){
		$("#tobdy").append('<tr><td><select class="form-control sOffice" name="officeSelect"></select>'
                   +' </td><td><input type="radio"  class="is_main" name="isMain_radio" value=""></td>'
                   +' <td><a class="btn removeOffice" title="删除"><i class="fa fa-trash-o fa-fw"></i></a></td></tr>');
		queryOffice();
	});
	
	//添加客户
	$("#addCustomer").on('click',function(){
		$("#customerTbody").append('<tr><td>'
				+'<select class="form-control customer" name="customerSelect"></select></td>'
                +' <td><a class="btn removeCustomer" title="删除"><i class="fa fa-trash-o fa-fw"></i></a></td></tr>');
		queryCustomer();
	});
	/*---移除---*/
	//移除网点
	$("#tobdy").on('click','.removeOffice',function(){
		
		if($(this).parent().parent().find("input[type='radio']").prop("checked")==false){
			$(this).parent().parent().remove();
		}
		
		queryOffice();
	});
	//移除客户
	$("#customerTbody").on('click','.removeCustomer',function(){
		$(this).parent().parent().remove();
		$(this).parent().parent().remove();
		queryCustomer();
	});
	/*下拉框选择、点击*/
	$("#tobdy").on('change','.sOffice',function(){
		$(this).parent().next().find("input[type=radio]").prop("value",$(this).val());		
		queryOffice();
	});
	//查询
	var userId = $("#userId").val();
	officeList();
	customerList();
	//选择默认的网点
	$("#tobdy").on('click','.is_main',function(){
		//保存默认网点
		var id = $("#userId").val();
		var office_id =$(this).val();
		
		if(id!=null&&id!=""&&office_id!=""){
			
			$.post('/loginUser/saveIsmain',{id:id,office_id:office_id},function(){
				queryOffice();
			});
		};
	});
	
	$("#customerTbody").on('change','.customer',function(){
		queryCustomer();
		
	});
	$.post('/loginUser/isSelectAll',{userId:userId},function(data){
		if(data == "checked"){
			$("#selectAllOffice").prop("checked",true);
		}else{
			$("#selectAllOffice").prop("checked",false);
		};
	});
	$.post('/loginUser/isSelectAllCustomer',{userId:userId},function(data){
		if(data == "checked"){
			$("#selectAllCustomer").prop("checked",true);
		}else{
			$("#selectAllCustomer").prop("checked",false);
		};
	});
	//添加全部网点
	$("#selectAllOffice").on('click',function(){
		var is_check = $("#selectAllOffice").prop("checked");
		var userId = $("#userId").val();
		if(userId != null && userId !=""){
			$.post('/loginUser/OfficeAllSelect',{is_check:is_check,userId:userId},function(data){
				officeList();
			},'json');
		};
		
	});
	//添加全部客户
	$("#selectAllCustomer").on('click',function(){
		var is_check = $("#selectAllCustomer").prop("checked");
		var userId = $("#userId").val();
		if(userId != null && userId !=""){
			$.blockUI({ 
                message: '<h4><img src="/images/loading.gif" style="height: 20px; margin-top: -3px;"/> 正在提交...</h4>' 
            });
			$.post('/loginUser/selectAllCustomer',{is_check:is_check,userId:userId},function(data){
				customerList();
				$.scojs_message('更新成功', $.scojs_message.TYPE_OK);
				$.unblockUI();
			},'json');
		};
	});
	//点击保存按钮时，将用户网点和用户可见客户的值传到后台中
	$("#saveBtn").click(function(){
		//检测验证是否通过
//		if(!$('#leadsForm').valid()){
//			return false;
//		}
		var officeIds=[];
		var customerIds=[];
		$("select[name='officeSelect']").each(function(){
			if($(this).val()!=null&&$(this).val()!=""){
				officeIds.push($(this).val());
			}		
   		});
		$("select[name='customerSelect']").each(function(){
			if($(this).val()!=null&&$(this).val()!=""){
				customerIds.push($(this).val());
			}
   		});
		$("#officeIds").val(officeIds.toString());
		$("#customerIds").val(customerIds.toString());
		
		$("#leadsForm").submit();
		
	});
	if($("#userId").val() != "" && $("#userId").val() != null){
		$("#assigning_role").show();
	}
	});
	$.unblockUI();
	
	var refreshUrl=function(url){
	  	var state = window.history.state;
	  	if(state){
	  		window.history.replaceState(state, "", url);
	  	}else{
	  		window.history.pushState({}, "", url);
	  	}
	 };
	 
	 if(window.location.pathname.substring(window.location.pathname.length-8, window.location.pathname.length)=='saveUser')
		 refreshUrl(window.location.protocol + "//" + window.location.host+window.location.pathname.substring(0, window.location.pathname.lastIndexOf('/')+1)+'edit/'+$('#userId').val());
});