define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	itemOrder.buildExpressDetail=function(){
		var arrays = [];
    	var item = {};
    	item['id'] = $('#express_id').val();
    	var expressForm = $('#expressForm input,#expressForm select,#expressForm textarea');
    	for(var i = 0; i < expressForm.length; i++){
    		var name = expressForm[i].id;
        	var value =expressForm[i].value;
        	if(name){
        		item[name] = value;
        	}
    	}
    	arrays.push(item);
        return arrays;
    };
    
    //校验
    $("#send_name,#send_address,#receive_name,#receive_address,#transfer_no,#carrier_company,#express_remark").on("blur",function(){
		self = $(this);
		data = self.val();
		len = $.trim(data).length;
		var re = /^.{255,}$/g;
		if(re.test(data)&&len!=0){
			self.parent().append("<span style='color:red;width:322px;margin-left:160px;' class='error_span'>请输入长度255以内的字符串</span>")
		}
	});
    $("#send_phone,#receive_phone").on("blur",function(){
		self = $(this);
		data = $.trim(self.val());
		len = $.trim(self.val()).length;
		var mobile = /^((1[34578]\d{9})|(0\d{2,3}-\d{7,8}))$/;
		if(!mobile.test(data)&&len!=0){   
			self.parent().append("<span style='color:red;width:322px;margin-left:160px;' class='error_span'>请输入正确的电话或者手机号码</span>")
		}
    });
    $("#send_name,#send_address,#receive_name,#receive_address,#transfer_no,#carrier_company,#express_remark,#send_phone,#receive_phone").on("focus",function(){
		self = $(this)
		self.parent().find("span").remove()
	})
} );
});