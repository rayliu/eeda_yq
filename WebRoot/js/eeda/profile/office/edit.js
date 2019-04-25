define(['jquery', 'metisMenu', 'template', 'sb_admin', 'validate_cn', 'layer'], function ($, metisMenu, template) {
	$(document).ready(function() {
		
		$('#leadsForm').validate({
		    rules: {
		    	office_name: {
			       required: true
			    }
		    },
			highlight: function(element) {
				$(element).closest('.control-group').removeClass('success').addClass('error');
			},
			success: function(element) {
				element
				.text('OK!').addClass('valid')
				.closest('.control-group').removeClass('error').addClass('success');
			}
		});

		$('#saveBtn').click(function(event) {
			//检测验证是否通过
			if(!$("#leadsForm").valid()){
				return false;
			}
			var layer_index = layer.load(1, {
				shade: [0.3,'#000'] //0.3透明度的黑色背景
			});
			$.post('/company/save', $("#leadsForm").serialize(), function(data){
				if(data=='ok'){
					layer.close(layer_index); 
					layer.alert('保存成功', {icon: 1});
					$('#saveBtn').attr('disabled', false);
				}
			}).fail(function() {
				layer.alert('保存失败', {icon: 2});
				$('#saveBtn').attr('disabled', false);
				layer.close(layer_index); 
			});
		});

	});
});