define(['jquery', 'metisMenu', 'sb_admin', 'dataTables', 'validate_cn', './edit_doc_table', './edit_customer_quotation_table','./add_dock_item_table'], function ($, metisMenu) { 
    $(document).ready(function() {
  
        $('#menu_profile').addClass('active').find('ul').addClass('in');
        $("#breadcrumb_li").text('客户基本信息');
        var cname = $("#company_name").val();
        var sname = $("#abbr").val();
        $('#customerForm').validate({
            rules: {
                mbProvince:{
                  digits:true 
                },
                cmbCity:{
                  digits:true 
                },
                company_name: {//form 中company_name为必填, 注意input 中定义的id, name都要为company_name
                	required: true,
                	remote:{
	                    url: "/customer/checkCustomerNameExist", //后台处理程序x
	                    type: "post",  //数据发送方式
	                    data:  {   //要传递的数据
	                        company_name: function() { 
	                            // $("#company_name").val()
	                            if($("#partyId").val()==null||$("#partyId").val()==""){
	                                 return $("#company_name").val();
	                            }else{
	                                if(cname==$("#company_name").val()){
	                                    return true;
	                                }else{
	                                    return $("#company_name").val();
	                                }
	                            }
	                        }
                    	}
                	}
                },
                abbr:{//form 中 abbr为必填
                  required: true,
                  remote:{
                    url: "/customer/checkCustomerAbbrExist", //后台处理程序    
                      type: "post",  //数据发送方式  
                      data:  {                     //要传递的数据   
                          abbr: function() { 
                              if($("#partyId").val()==null||$("#partyId").val()==""){
                                  return $("#abbr").val();  
                            }else{
                                if(sname==$("#abbr").val()){
                                    return true;
                                }else{
                                    return $("#abbr").val();  
                                }
                            }
                               
                            }   
                      }
                    }
                },
              contact_person:{//form 中 name为必填
                required: true
              },
              location:{
                required: true
              },
              insurance_rates:{
                number:true 
              },
              email:{
                email: true
              }
            },
            messages:{
                company_name:{
                    remote:"公司名称已存在"
                },
                abbr:{
                    remote:"简称已存在"
                },
                mbProvince: {
                    digits: "请选择省份"
                },
                cmbCity: {
                    digits: "请选择城市"
                },
                insurance_rates: {
                    remote: "请输入数字"
                }
            },
            highlight: function(element) {
                $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
            },
            success: function(element) {
                element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
            }
        });
        
      
        // 回显入库是否库存管理
        $("input[name='isInventoryControl']").each(function(){
            if($("#inventoryControl").val() == $(this).val()){
                $(this).attr('checked', true);
            }
        });
        // 回显车长
        var chargeTypeOption=$("#chargeType>option");
        var chargeTypeVal=$("#chargeTypeSelect").val();
        for(var i=0;i<chargeTypeOption.length;i++){
           var svalue=chargeTypeOption[i].value;
           if(chargeTypeVal==svalue){
               $("#chargeType option[value='"+svalue+"']").attr("selected","selected");
           }
        }

        $("input[name='charge_type']").click(function(){
             //等于零担的时候
            if($('input[name="charge_type"]:checked').val()==='perCargo'){
                $('#ltl_price_type').show();
                $("#carInfomation").hide();
                $("#car_type_div").hide();
            }else if($('input[name="charge_type"]:checked').val()==='perCar'){
                $("#carInfomation").show();
                //显示车辆信息
                $(this).prop('checked', true);
                $("#car_type_div").show();
                $('#ltl_price_type').hide();
            }else{
                $('#ltl_price_type').hide();
                $("#car_type_div").hide();
                //计费方式为计件的时候
                if($('input[name="charge_type"]:checked').val()==='perUnit'){
                    $("#carInfomation").hide();
                }else{
                    $("#carInfomation").show();
                }
            }
         });

        var chargeType = $("#chargeTypeRadio").val();
        
        $("input[name='chargeType']").each(function(){
            if(chargeType == $(this).val()){
                $(this).prop('checked', true);
            }
        });

        $('#saveRouteBtn').click(function(){
            $.post('/customer/saveCustomerRoute', $('#routeItemForm').serialize()+'&limitationFile='+$('#limitationFile').val()+'&customer_id='+$('#partyId').val(), function(data){
                if(data.ID==-1){
                    $.scojs_message('保存失败，相同路线已存在', $.scojs_message.TYPE_ERROR);
                    return;
                }
                if(data.ID>0){
                    $('#myModal').modal('hide');
                    dataTable.ajax.reload();
                }else{
                    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                }
            });
        });
        
        
        var buildDocItem=function(){
            var cargo_table_rows = $("#doc_table tr");
            var cargo_items_array=[];
            for(var index=0; index<cargo_table_rows.length; index++){
                if(index==0)
                    continue;

                var row = cargo_table_rows[index];
                var empty = $(row).find('.dataTables_empty').text();
                if(empty)
                	continue;
                
                var id = $(row).attr('id');
                if(!id){
                    id='';
                }
                
                var item={}
                item.id = id;
                for(var i = 1; i < row.childNodes.length; i++){
                	var el = $(row.childNodes[i]).find('input, select');
                	var name = el.attr('name');
                	if(el && name){
                    	var value = el.val();
                    	item[name] = value;
                	}
                }
                item.action = id.length > 0?'UPDATE':'CREATE';
                cargo_items_array.push(item);
            }

            return cargo_items_array;
        };
        
        $('#saveBtn').click(function(){
        	//提交前，校验必填
            if(!$("#customerForm").valid()){
                return;
            }
            var order ={}
            order.id = $("#partyId").val();
            order.code = $("#code").val();
            order.quick_search_code = $("#quick_search_code").val();
            order.abbr = $("#abbr").val();
            order.company_name = $("#company_name").val();
            order.company_name_eng = $("#company_name_eng").val();
            order.company_type = $("#company_type").val();
            order.address = $("#address").val();
            order.address_eng = $("#address_eng").val();
            order.contact_person = $("#contact_person").val();
            order.contact_person_eng = $("#contact_person_eng").val();
            order.phone = $("#phone").val();
            order.skype = $("#skype").val();
            order.email = $("#email").val();
            order.fax = $("#fax").val();
            order.receipt = $("#receipt").val();
            order.payment = $("#payment").val();
            order.chargeType = $("#chargeType").val();
            order.insurance_rates = $("#insurance_rates").val();
            order.introduction = $("#introduction").val();
            order.remark = $("#remark").val();
            order.customer_remind = $("#customer_remind").val();
            order.registration = $("#registration").val();
            order.custom_registration = $("#custom_registration").val();
            order.zip_code = $("#zip_code").val();
            order.docItem = buildDocItem();
            		
            order.customer_quotationItem=itemOrder.buildCustomerQuotationDetail();
            order.dock_Item=itemOrder.buildDockItem();
        	$('#saveBtn').attr('disabled', true);
        	$.post('/customer/save', {params:JSON.stringify(order)}, function(data){
        		eeda.contactUrl("edit?id",data.ID);
        		itemOrder.refleshTable(data.ID)
        		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
        		$('#partyId').val(data.ID);
        		$('#saveBtn').attr('disabled', false);
        		$("#fileuploadSpan").show();
        	},'json').fail(function(){
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
              });
        })

        

    });
});