define(['jquery', 'metisMenu', 'sb_admin', 'dataTables', 'validate_cn'], function ($, metisMenu) {

	$(document).ready(function() {
		document.title = '岗位编辑 | '+document.title;

		$('#editForm').validate({
			rules : {
				rolename : {
					required : true
					/* remote:{
	                	url: "/role/checkRoleNameExit", //后台处理程序    
                        type: "post",  //数据发送方式  
                        data:  {                     //要传递的数据   
                        	rolename: function() {   
                                return $("#rolename").val();   
                              }   

                        } 
					}*/
				} 

			},
			/* messages:{
            	 rolename:{
            		 remote:"岗位已存在"
            	 }
             }, */
			highlight : function(element) {
				$(element).closest('.form-group')
						.removeClass('has-success')
						.addClass('has-error');
			},
			success : function(element) {
				element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
			}
		});
	});
});