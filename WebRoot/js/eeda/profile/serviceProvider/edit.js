define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','./account_item_table','./contacts_item_table','./car_item_table','./add_dock_item_table','./markList'], function ($, metisMenu) { 

  $(document).ready(function() {

	  
    	var id = $("#partyId").val();
    	if(id != null && id != ""){
    		$("#addChargeType").attr("disabled",false);
    	}
        var dataTable= $('#item_table').dataTable({
        	"bFilter": false, //不需要默认的搜索框
            "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
            "iDisplayLength": 10,
            "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
            "bServerSide": true,
        	"oLanguage": {
                "sUrl": "/eeda/dataTables.ch.txt"
            },
            "sAjaxSource": "/serviceProvider/chargeTypeList?typeId="+id,
            "aoColumns": [
                {"mDataProp":"CUSTOMER_NAME"},
                {"mDataProp":"CHARGE_TYPE",
                	"fnRender":function(obj){
                		var str= "";
                		if(obj.aData.CHARGE_TYPE == 'perUnit'){
                			str = '计件';
                		}else if(obj.aData.CHARGE_TYPE == 'perCar'){
                			str = '整车';
                		}else if(obj.aData.CHARGE_TYPE == 'perCargo'){
                			str = '零担'
                		}
                		return str;
                	}},
                {"mDataProp":"REMARK"},
                { 
                    "mDataProp": null, 
                    "sWidth": "8%",  
                    "bVisible":(true),
                    "fnRender": function(obj) {  
                    	var str ="<nobr>";
                    		str += "<a class='btn  btn-primary btn-sm itemEdit' code='"+obj.aData.ID+"'><i class='fa fa-edit fa-fw'></i>编辑</a> ";
		                    str += "<a class='btn btn-danger btn-sm itemDel' code='"+obj.aData.ID+"'>"+
		                         "<i class='fa fa-trash-o fa-fw'></i>删除</a>";
                    	return str +="</nobr>";
                       
                    }
                }
            ]    
        });

    	

        var spArr= spType.split(';');
        for (var i = 0; i < spArr.length; i++) {
            var checkSpType = spArr[i];
            //line;delivery;pickup;personal;carrier;air;broker;head_car;oversea_agent;booking_agent;truck;manufacturer;traders
            if(checkSpType == 'line'){
                $('#sp_type_line').attr('checked', 'checked');
            }else if(checkSpType == 'delivery'){
                $('#sp_type_delivery').attr('checked', 'checked');
            }else if(checkSpType == 'pickup'){
                $('#sp_type_pickup').attr('checked', 'checked');
            }else if(checkSpType == 'personal'){
                $('#sp_type_personal').attr('checked', 'checked');
            }else if(checkSpType == 'carrier'){
                $('#sp_type_carrier').attr('checked', 'checked');
            }else if(checkSpType == 'air'){
                $('#sp_type_air').attr('checked', 'checked');
            }else if(checkSpType == 'broker'){
                $('#sp_type_broker').attr('checked', 'checked');
            }else if(checkSpType == 'head_car'){
                $('#sp_type_head_car').attr('checked', 'checked');
            }else if(checkSpType == 'oversea_agent'){
                $('#sp_type_oversea_agent').attr('checked', 'checked');
            }else if(checkSpType == 'booking_agent'){
                $('#sp_type_booking_agent').attr('checked', 'checked');
            }else if(checkSpType == 'truck'){
                $('#sp_type_truck').attr('checked', 'checked');
            }else if(checkSpType == 'cargo_agent'){
                $('#sp_type_cargo_agent').attr('checked', 'checked');
            }else if(checkSpType == 'manufacturer'){
                $('#sp_type_manufacturer').attr('checked', 'checked');
            }else if(checkSpType == 'traders'){
                $('#sp_type_traders').attr('checked', 'checked');
            }else if(checkSpType == 'port_supervision'){
                $('#sp_type_port_supervision').attr('checked', 'checked');
            }else if(checkSpType == 'wharf'){
                $('#sp_type_wharf').attr('checked', 'checked');
            }else if(checkSpType == 'warehouse'){
                $('#sp_type_warehouse').attr('checked', 'checked');
            }
            
        };

    	if(payment == "monthlyStatement"){
    		$("#payment").find("option[value='monthlyStatement']").attr("selected",true);
  		}else if(payment == "freightCollect"){
  			$("#payment").find("option[value='freightCollect']").attr("selected",true);
  		}else{
  			$("#payment").find("option[value='cashPayment']").attr("selected",true);
  		}

        $('#menu_profile').addClass('active').find('ul').addClass('in');
        $('#customerForm').validate({
            rules: {
              company_name: {//form 中company_name为必填, 注意input 中定义的id, name都要为company_name
                required: true,
                maxlength:100
              },
              abbr:{//form 中 abbr为必填
                  required: true,
                  maxlength:60
              },
              quick_search_code :{
        	      required: true,
        	      maxlength:45
              },
              contact_person:{//form 中 name为必填
                required: true
              },
              /*location:{
                required: true
              },*/
              company_name_eng :{
              	maxlength:100
              },
              address: {
	        	  maxlength:255
	          },
	              contact_person_eng: {
            	  maxlength:100
              },
              address_eng: {
            	  maxlength:255
              },
              bill_of_lading_info: {
            	  maxlength:1000
              },
              contact_person: {
            	  maxlength:100
              },
              contact_person_eng: {
            	  maxlength:100
              },
              phone: {
            	  isMobile:true
              },
              email: {
            	  email:true
              },
              website: {
            	  isWebsite:true
              },
              fax: {
            	  maxlength:50
              },
              identification_no: {
            	  maxlength:100
              },
              special_item:{
            	  maxlength:500
              },
              register_capital: {
            	  maxlength:255
              },
              main_business: {
            	  maxlength:255
              },
              receiver: {
            	  maxlength:50
              },
              postal_code: {
            	  maxlength:60
              },
              status: {
            	  maxlength:255
              }
            },
            highlight: function(element) {
                $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
            },
            success: function(element) {
                element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
            }
        });
        $("#this_year_salesamount,#last_year_salesamount,#beforelast_year_salesamount,#response_problem_time,#solve_problem_time,#pay_account_time,"
        		+"#business_scope,#introduction,#remark").on("blur",function(){
			 var data = $.trim($(this).val());
	    	 var name = $(this).attr("name");
	    	 var len = $.trim(data).length;
        	 if(name=="this_year_salesamount"||name=="last_year_salesamount"||name=="beforelast_year_salesamount"||name=="introduction"||name=="remark"){
        		 if(len>255){
        			 $(this).parent().append("<span style='color:red;display:block;' class='error_span'>请输入最多是255长度的字符串</span>");
        			 return;
        		 }
        	 }
        	 if(name=="response_problem_time"||name=="solve_problem_time"||name=="pay_account_time"){
        		 if(len>225){
        			 $(this).parent().append("<span style='color:red;display:block;' class='error_span'>请输入最多是225长度的字符串</span>");
        			 return;
        		 }
        	 }
        	 if(name=="business_scope"){
        		 if(len>500){
        			 $(this).parent().append("<span style='color:red;display:block;' class='error_span'>请输入最多是500长度的字符串</span>");
        			 return;
        		 }
        	 }
        });
        $("#this_year_salesamount,#last_year_salesamount,#beforelast_year_salesamount,#response_problem_time,#solve_problem_time,#pay_account_time,"
        		+"#business_scope,#introduction,#remark").on("focus",function(){
        	$(this).parent().find("span").remove();
     	});
        jQuery.validator.addMethod("isMobile", function(value, element) {
            var length = value.length;
            var mobile = /^((1[3456789]\d{9})|(0\d{2,3}-\d{7,8}))$/;
            return this.optional(element) || (mobile.test(value));
        }, "请输入格式正确的电话或手机号码");
        jQuery.validator.addMethod("isWebsite", function(value, element) {
            var length = value.length;
            var re = /^([a-zA-Z\d][a-zA-Z\d-_]+\.)+[a-zA-Z\d-_][^ ]*$/;
            return this.optional(element) || (re.test(value));
        }, "网站格式不正确");
         
      // 回显计费方式
      var chargeTypeOption=$("#chargeType>option");
      var chargeTypeVal=$("#chargeTypeSelect").val();
      for(var i=0;i<chargeTypeOption.length;i++){
         var svalue=chargeTypeOption[i].value;
         if(chargeTypeVal==svalue){
      	   $("#chargeType option[value='"+svalue+"']").attr("selected","selected");
         }
      }
      //自动提交改为手动提交
      $("#save").click(function(){
    	  //提交前，校验必填
      	  var formRequired = 0;
          $('#customerForm').each(function(){
          	if(!$(this).valid()){
          		formRequired++;
              }
          })
          var errorlength = $(".error_span").length;
          if(errorlength>0||formRequired>0){
          	$.scojs_message('单据存在填写格式错误字段未处理', $.scojs_message.TYPE_ERROR);
              return;
          }
          
    	 $("#save").attr("disabled",false);
    	 var order={};
    	 order.acount_json =itemOrder.buildCargoDetail();
    	 order.contacts_json =itemOrder.buildContactsDetail();
    	 order.cars_json =itemOrder.buildCarsDetail();
    	 order.dock_Item=itemOrder.buildDockItem();
    	 $("#acount_json").val(JSON.stringify(order));
    	 $.post("/serviceProvider/save", $("#customerForm").serialize(),function(data){
    		if(data=='abbrError'){
    			$.scojs_message('供应商简称已存在', $.scojs_message.TYPE_ERROR);
    			$("#save").attr("disabled",false);
    		}else if (data=='companyError'){
    			$.scojs_message('公司名称已存在', $.scojs_message.TYPE_ERROR);
    			$("#save").attr("disabled",false);
    		}else if(data.ID != null && data.ID != ""){
     			eeda.contactUrl("edit?id",data.ID);
     			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
     			$("#status").val(data.STATUS);
     			var status= data.STATUS;
     			$("#status_").val(data.STATUS);
     			$("#code").val(data.CODE);
     			$("#partyId").val(data.ID);
     			$("#sp_id").val(data.ID);
     			$("#addChargeType").attr("disabled",false);
     			statusBtn(status);
     		}else{
     			$.scojs_message('数据有误', $.scojs_message.TYPE_ERROR);
     			$("#save").attr("disabled",false);
     		}
    		//异步刷新明细表
    		if(data!='abbrError'){
    			itemOrder.refleshTable(data.ID);
    			itemOrder.refleshContactsTable(data.ID);
    			itemOrder.refleshCarsTable(data.ID);
    		}
    		
         });
    	  
      });
      
      var statusBtn=function(status){
    	  if(status=="新建"){
    		  $("#submit").prop('disabled',false);
	    	  $("#save").prop('disabled',false);
	    	  $("#approval").prop('disabled',true);
	    	  $("#disapproval").prop('disabled',true);
	    	  $("#verification").prop('disabled',true);
	    	  $("#disVerification").prop('disabled',true);
    	  }else if(status=="待审核"){
			  $("#submit").prop('disabled',true);
	    	  $("#save").prop('disabled',true);
	    	  $("#approval").prop('disabled',false);
	    	  $("#disapproval").prop('disabled',false);
	    	  $("#verification").prop('disabled',true);
	    	  $("#disVerification").prop('disabled',true);
		  }else if(status=="审核通过"){
			  $("#save").prop('disabled',false);
			  $("#submit").prop('disabled',true);
	    	  $("#approval").prop('disabled',true);
	    	  $("#disapproval").prop('disabled',false);
	    	  $("#verification").prop('disabled',false);
	    	  $("#disVerification").prop('disabled',false);
		  }else if(status=="审核不通过"){
			  $("#save").prop('disabled',false);
			  $("#submit").prop('disabled',false);
			  $("#approval").prop('disabled',false);
	    	  $("#disapproval").prop('disabled',true);
	    	  $("#verification").prop('disabled',true);
	    	  $("#disVerification").prop('disabled',true);
		  }else if(status=="审批通过"){
			  $("#save").prop('disabled',false);
			  $("#submit").prop('disabled',true);
			  $("#approval").prop('disabled',true);
	    	  $("#disapproval").prop('disabled',true);
	    	  $("#verification").prop('disabled',true);
	    	  $("#disVerification").prop('disabled',false);
		  }else if(status=="审批不通过"){
			  $("#save").prop('disabled',false);
			  $("#submit").prop('disabled',false);
			  $("#approval").prop('disabled',true);
	    	  $("#disapproval").prop('disabled',true);
	    	  $("#verification").prop('disabled',false);
	    	  $("#disVerification").prop('disabled',true);
		  }
      }
    
      
      
      $("#submit,#approval,#disapproval,#verification,#disVerification").click(function(){
    	  var btn = $(this).attr('id');
    	  var value = $(this).text();
    	  $.post("/serviceProvider/approvalOrder", {id:$("#partyId").val(),action:btn},function(data){
    		  if(data){
    			  $.scojs_message('操作成功', $.scojs_message.TYPE_OK);
    			  var status = data.STATUS;
    			  $("#status").val(status);
    			  $("#status_").val(data.STATUS);
    			  statusBtn(status);
    			  
    		  }
    	  }).fail(function() {
	          $.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
	          $("#approval").prop('disabled',false);
	    	  $("#disapproval").prop('disabled',false);
    	  });
    	  
      });
      
      
      
      //审核按钮状态
      var statuOrder=$('#status').val();
      if(statuOrder!=''){
    	  statusBtn(statuOrder);
      }
      
      
      
      
	  //获取客户信息
	  $('#customer_name').on('keyup click', function(){
	        var inputStr = $('#customer_name').val();
	        var companyList =$("#customerList");
	        $.get("/customer/searchPartCustomer", {input:inputStr}, function(data){
	            companyList.empty();
	            for(var i = 0; i < data.length; i++)
	                companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
	        },'json');
	       
	        companyList.css({ 
		    	left:$(this).position().left+"px", 
		    	top:$(this).position().top+32+"px" 
		    });
	        companyList.show();
	    });
	    $('#customerList').on('click', '.fromLocationItem', function(e){        
	        $('#customer_name').val($(this).text());
	        $("#customerList").hide();
	        var companyId = $(this).attr('partyId');
	        $('#customer_id').val(companyId);
	    	
	    });
	    // 没选中客户，焦点离开，隐藏列表
	    $('#customer_name').on('blur', function(){
	        $('#customerList').hide();
	    });

	    //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
	    $('#customer_name').on('blur', function(){
	        $('#customerList').hide();
	    });

	    $('#customerList').on('mousedown', function(){
	        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
	    });
	    
	    
	    $('#itemFrom').validate({
            rules: {
            	customer_name: {//form 中company_name为必填, 注意input 中定义的id, name都要为company_name
                required: true
              }
            },
            highlight: function(element) {
                $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
            },
            success: function(element) {
                element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
            }
        });

	    //点击新增客户计费内的按钮发生的事情
	    $("#chargeTypeItemBtn").click(function(){
	    	var sp_id = $("#sp_id").val();
	    	if(sp_id == null || sp_id ==''){
	    		$.scojs_message('供应商数据没有保存或者数据不完善，请先保存数据或者完善数据', $.scojs_message.TYPE_ERROR);
	    		return false;
	    	}
	    	 if(!$("#itemFrom").valid()){
	    		  return false;
	    	 }
	    	 
	    	$.post("/serviceProvider/saveChargeType",$("#itemFrom").serialize(),function(data){
	    		var id = $("#sp_id").val();
	    		$('#chargeTypeItemModel').modal('hide');
	    		dataTable.fnSettings().sAjaxSource = "/serviceProvider/chargeTypeList?typeId="+id;
	    		dataTable.fnDraw(); 
	            
	    	},'json');
	    	
	    });
	    $("#addChargeType").click(function(){
	    	$("#chargeTypeItemId").val('');
	    	$("#customer_id").val('');
	    	$("#customer_name").val('');
	    	$("#chargeTypeRemark").val('');
	    	$("#customer_name").attr("disabled",false);
	    	$("#c_type option[value='perUnit']").attr("selected","selected");
	    });
	    

    });
});