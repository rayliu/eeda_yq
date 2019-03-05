define(['jquery', 'metisMenu', 'template', 'sb_admin', 'validate_cn', 'layer'], function ($, metisMenu, template) {
	$(document).ready(function() {
		$("#breadcrumb_li").text('用户信息');
		$.Huitab = function(tabBar,tabCon,class_name,tabEvent,i){
        	var $tab_menu = $(tabBar);
        	// 初始化操作
        	$tab_menu.removeClass(class_name);
        	$(tabBar).eq(i).addClass(class_name);
        	$(tabCon).hide();
        	$(tabCon).eq(i).show();
        	  
        	$tab_menu.bind(tabEvent,function(){
        	  	$tab_menu.removeClass(class_name);
        	      $(this).addClass(class_name);
        	      var index=$tab_menu.index(this);
        	      $(tabCon).hide();
        	      $(tabCon).eq(index).show();
        	});
        };
		$.Huitab("#tab_demo .tabBar span","#tab_demo .tabCon","current","click","0");
		
		$('#leadsForm').validate({
		    rules: {
		    	old_pwd: {
			       minlength: 6,
			       required: true,
			       remote: {
					    url: "/loginUser/checkOldPwd",     //后台处理程序
					    type: "post",               //数据发送方式
					    dataType: "json",           //接受数据格式   
					    data: {   //要传递的数据
					    	user_name: function() {
					            return $("#username").val();
					        },
					        old_pwd: function() {
					            return $("#old_pwd").val();
					        }
					    }
					}
			    },	
			    new_pwd: {
			       minlength: 6,
			       required: true
			    }
		    },
		    messages: {
	            old_pwd: {
	                remote: "输入的旧密码不对"
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
			$.post('/loginUser/savePerson', $("#leadsForm").serialize(), function(data){
				if(data){
					layer.close(layer_index); 
					$('#role_id').val(data.ID)
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