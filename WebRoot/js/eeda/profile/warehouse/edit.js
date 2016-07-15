define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 
	if(warehouse_name){
		document.title = warehouse_name+' | '+document.title;
	}
    $('#menu_profile').addClass('active').find('ul').addClass('in');
    
    $('#warehouseForm').validate({
        rules: {
        	warehouse_name: {
            required: true
          },
          	warehouse_address:{
            required: true
          },
          	email:{
          	email: true
          },
          	warehouse_area:{
          	required: true,
          	number:true
          },
          officeSelect:{
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

	
	// 回显仓库类型
	$("input[name='warehouseType']").each(function(){
		if($("#warehouseTypeHide").val() == $(this).val()){
			$(this).attr('checked', true);
			if($(this).attr('id') == 'warehouseType1'){
				$("#officeDiv").show();
			}else{
				$("#officeDiv").hide();
			}
		}
	});
	
	// 回显状态
	$("input[name='warehouseStatus']").each(function(){
		if($("#warehouseStatusHide").val() == $(this).val()){
			$(this).attr('checked', true);
		}
	});
	//选中网点回显目的地
	$("#officeSelect").change(function(){
		var officeId = $(this).val();
		$.get('/warehouse/findDocaltion', {"officeId":officeId}, function(data){
			console.log(data);
			if(data != null && data != ""){
				searchAllLocationFrom(data);
			}
		},'json');
	});
	$('#saveBtn').click(function(e){
        //阻止a 的默认响应行为，不需要跳转
        e.preventDefault();
        //提交前，校验数据
        if(!$("#warehouseForm").valid()){
            return;
        }
        
        $(this).attr('disabled', true);

       
        var order = {
            id: $('#id').val(),
            warehouse_name: $('#warehouse_name').val(),
            notify_name:  $('#notify_name').val(),
            notify_mobile:  $('#notify_mobile').val(),
            location:  $('#location').val(),
            warehouse_address:  $('#warehouse_address').val(),
            sp_id:  $('#sp_id').val(),
            warehouse_desc:  $('#warehouse_desc').val()
        };
        //异步向后台提交数据
        $.post('/warehouse/save', {params:JSON.stringify(order)}, function(data){
            var order = data;
            if(order.ID>0){
                $("#name").val(order.NAME);
                
                eeda.contactUrl("edit?id",order.ID);
                $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                $('#saveBtn').attr('disabled', false);
            }else{
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
            }
        },'json').fail(function() {
            $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
            $('#saveBtn').attr('disabled', false);
          });
    });  
    
});