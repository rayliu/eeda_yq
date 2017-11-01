define(['jquery', 'metisMenu', 'sb_admin', 'dataTablesBootstrap', 'validate_cn', 'sco'], function($, metisMenu) {
	$(document).ready(function(){
		itemOrder.buildMailSetDetail=function(){
			var arrays = [];
			var item = {};
			item['id']=$('#mail_id').val();
			item['mail_ssl']=$('#mail_ssl').val($('#mail_ssl').prop('checked')==true?'SSL':'');
			var mailForm = $('#mailForm input,#mailForm select,#mailForm textarea');
			for(var i=0;i<mailForm.length;i++){
				var name = mailForm[i].id;
				var value = mailForm[i].value;
				if(name){
					item[name] =value;
				}
			}
			arrays.push(item);
			return arrays;
		};
		var mail_ssl_value=$('#hidden_mail_ssl').val();
		if(mail_ssl_value=="SSL"){
			$('#mail_ssl').attr('checked',true);
		}
		
		
		
	});
});