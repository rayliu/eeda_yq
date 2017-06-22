define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	
	
	//校验是否已存在
    var employee_id = $('#employee_id').val();
    if(!employee_id){
    	$('#employeeForm').validate({
            rules: {
            	employee_name: {
                	remote:{
	                    url: "/employeeFiling/checkCodeExist",
	                    type: "post",
	                    data:  {
	                        code: function() { 
	                              return $("#employee_name").val();
	                        }
                    	}
                	}
                }
            },
            messages:{
            	employee_name:{
                    remote:"此员工名字已存在"
                }
            },
            highlight: function(element) {
                $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
            },
            success: function(element) {
                element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
            }
        });
    }
	
	
	itemOrder.buildEmployeeDetail=function(){
		var arrays = [];
    	var item = {};
    	item['id'] = $('#employee_id').val();
    	var employeeForm = $('#employeeForm input,#employeeForm select');
    	for(var i = 0; i < employeeForm.length; i++){
    		var name = employeeForm[i].id;
        	var value =employeeForm[i].value;
        	if(name){
        		item[name] = value;
        	}
    	}
    	arrays.push(item);
        return arrays;
    };
} );
});