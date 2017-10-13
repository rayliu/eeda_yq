define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	itemOrder.buildInsuranceDetail=function(){
		var arrays = [];
    	var item = {};
    	item['id'] = $('#insurance_id').val();
    	var shipmentForm = $('#insuranceForm input,#insuranceForm select');
    	for(var i = 0; i < shipmentForm.length; i++){
    		var name = shipmentForm[i].id;
        	var value =shipmentForm[i].value;
        	if(name){
        		item[name] = value;
        	}
    	}
    	arrays.push(item);
        return arrays;
    };
    
  //司机、车牌、柜号、封条号、货品描述、运输时间及要求、运单号、签收状态校验
    $("#insurance_company,#policy_holder,#insurant,#insure_no,#insurance_no").on("blur",function(){
		self = $(this);
		data = self.val();
		len = $.trim(data).length;
		var re = /^.{100,}$/g;
		if(re.test(data)&&len!=0){
			self.parent().append("<span style='color:red;width:322px;margin-left:160px;' class='error_span'>请输入长度100以内的字符串</span>")
		}
	});
    $("#insurance_amount").on("blur",function(){
		self = $(this);
		data = $.trim(self.val());
		var re = /^\d{0,9}(\.\d{1,5})?$/g;
		if(!re.test(data)){
			self.parent().append("<span style='color:red;width:322px;margin-left:160px;' class='error_span'>请输入长度15以内的合法数字</span>")
		}
    });
    $("#insurance_company,#policy_holder,#insurant,#insure_no,#insurance_no,#insurance_amount").on("focus",function(){
		self = $(this)
		self.parent().find("span").remove()
	})
} );
});