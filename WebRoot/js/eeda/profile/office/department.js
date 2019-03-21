define(['jquery', 'validate_cn', 'layer'], function ($) {
	$(document).ready(function() {

	var table = eeda.dt({
		id: 'eeda_table',
		ajax:'/company/getDepartmentUser?group_id='+$("#group_id").val(),
		paging: false,
		serverSide: true,
		pageLength: false,
		lengthMenu:false,
		columns: [
				{ data: 'USER_NAME' },
				{ data: 'C_NAME' },
				{ data: 'GROUP_NAME' },
				{ data: 'ROLE_NAME',
					"render":function(data,type,full,meta){
						return '<a href="/role/permission/'+full.ROLE_ID+'">'+data+'</a>';
					}
				},
				{ data: 'IS_STOP', 
					"render":function(data,type,full,meta){
						var str = data;
						if(data==0){
							return '启用';
						}
						return '已停用';
					}	
				},
				{ data: '', 
					"render":function(data,type,full,meta){
						return '<a class="btn btn-xs editUser" style="text-decoration:none" class="ml-5" title="编辑">编辑</a>'
							+' <a class="btn btn-xs dissUser" style="text-decoration:none" href="javascript:;" class="ml-5 delete" title="停用">停用</a>';
					}	
				}
		]
	});

	//架构树单击事件
	$(".structManage").on("click",".click",function(){
		var self = $(this);
		var id = self.children("span").attr("id");
		$(".click").css("background-color","");
		self.css("background-color","#d9e3ec");
		
		if(id!=$("#group_id").val()){
			//单击不同的部门，将单击的部门名称回填到右边
			$(".con_head_title").text(self.children("span.name").text());
			$("#group_id").val(id);
			
			//单击不同的部门，异步重载table
			table.ajax.url('/company/getDepartmentUser?group_id='+$("#group_id").val()).load();
			
		}else{
			//单击部门，如果部门id与右边的id相同，就隐藏
			if("block"==self.children("ul").css("display")){
				self.parent().children("ul").css("display","none");
			}else if("block"==self.next("ul").css("display")){
				self.parent().children("ul").css("display","none");
			}else{
				self.parent().children("ul").css("display","block");
			}
			
			var id = self.children("span").attr("id");
			if(self.parent().children("ul").html()==""){
				$.post("/webadmin/organize_structure/groupinfo",{id:id},function(data){
					if(data){
						for(var i=0;i<data.length;i++){
							var group_id = data[i].id;
							var value = data[i].group_name;
							appendMethod(group_id,value);
						}
					}else{
						layer.msg('后台出错',{icon:2});
					}
					
				});
			}
		}
	});
		
	//修改部门名称
	$("#updateGroupName").click(function(){
		var group_id = $("#group_id").val();
		
		layer.prompt({title: '请输入部门名称', formType:0,value:$(".con_head_title").text()}, function(value,index){
			layer.close(index);
		  
			$.post("/company/updateGroupName",{group_id:group_id,group_name:value},function(data){
				if(data=='ok'){
					layer.msg('修改成功',{icon:1});
					$("#"+$("#group_id").val()).text(value);
					$(".con_head_title").text(value);
				}else{
					layer.msg('修改失败',{icon:2});
				}
			});
		  
		});
	});
		
	//删除部门
	$("#deleteGroup").click(function(){
		var group_id = $("#group_id").val();
		layer.confirm('确定删除该部门吗？',{title:'提示'}, function(){
			$.post("/company/deleteGroup",{id:group_id},function(data){
				if(data.result){
					layer.msg('删除成功',{icon:1});
					window.location.href = "/company/department";
				}else{
					layer.msg('删除失败',{icon:2});
				}
			}).fail(function(){
	  			layer.msg('后台出错',{icon:2});
	 		});
		});
	});
	
	//新建子部门
	$("#createGroup").click(function(){
		var group_id = $("#group_id").val();
		
		layer.prompt({title: '请输入新建的部门名称', formType:0}, function(value,index){
			layer.close(index);
			
			$.post("/company/createGroup",{group_id:group_id,group_name:value},function(data){
				if(data.RESULT){
					layer.msg('新建成功',{icon:1});
					appendMethod(data.id,value);
				}else{
					layer.msg('新建失败',{icon:2});
				}
			});
		  
		});
	});
		
	//新建子部门时追加hmtl显示,达到实时显示
	var appendMethod = function(group_id,value){
		var html = '<li>'
				 + '	<div class="organization" title="'+value+'">'
			  	 + '		<div class="click">'
				 + '			<i class="Hui-iconfont">&#xe67e;</i><span id="'+group_id+'">'+value+'</span>'
				 + '		</div>'
				 + '		<ul class="" style="margin-left:7%;display:none;"></ul>'
			     + '	</div>'
				 + '</li>'
		$("#"+$("#group_id").val()).parent().parent().children("ul").append(html);
	}
	
	//单击添加成员弹窗
	$("#createUser").click(function(){
		var group_id = $("#group_id").val();
		addUserPage("添加成员","","","","","","",group_id);
	});

	//添加和编辑成员弹窗的html
	var layer_form_index, is_edit_user=false;
	var addUserPage = function(title,c_name,name,password,mobile,user_id,role_id,group_id){
		
		var pwdInput = '	<div class="row cl mt-5">'
			+'		<label class="form-label col-xs-4 col-sm-3"><span style="color:red;font-weight: bold;">*</span>密码</label>'
			+'		<div class="formControls col-xs-8 col-sm-6">'
			+'			<input type="password" name="pwd" placeholder="请输入密码" class="input-text" />'
			+'		</div>'
			+'	</div>';
		if("编辑成员"==title){
			is_edit_user=true;
			pwdInput = '	<div class="row cl mt-5">'
				+'		<label class="form-label col-xs-4 col-sm-3">密码</label>'
				+'		<div class="formControls col-xs-8 col-sm-6">'
				+'			<input type="password" name="pwd" placeholder="留空则不修改密码" class="input-text" />'
				+'		</div>'
				+'	</div>';
			
			
		}
		$.post("/company/roleList",function(data){
			var roleHtml = "";
			var role_list = data.ROLE_LIST;
			for(var index in role_list){
				if(role_list[index].ID==role_id){
					roleHtml += '<option selected="selected" value="'+role_list[index].ID+'">'+role_list[index].NAME+'</option>';
				}else{
					roleHtml += '<option value="'+role_list[index].ID+'">'+role_list[index].NAME+'</option>';
				}
			}

			var roleSelectInput="", roleSelectOption="";
			var department_list = data.DEPARTMENT_LIST;
			for(var index in department_list){
				if(department_list[index].ID==group_id){
					roleSelectOption += '<option selected value="'+department_list[index].ID+'">'+department_list[index].NAME+'</option>';
				}else{
					roleSelectOption += '<option value="'+department_list[index].ID+'">'+department_list[index].NAME+'</option>';
				}
			}
			roleSelectInput = '	<div class="row cl mt-5">'
				+'		<label class="form-label col-xs-4 col-sm-3">部门</label>'
				+'		<div class="formControls col-xs-8 col-sm-6">'
				+'			<span class="select-box">'
				+'                <select name="department_id" style="display:block;" class="select">'
				+roleSelectOption
				+'			    </select>'
				+'            </span>'
				+'		</div>'
				+'	</div>'
			//页面层
			layer_form_index=layer.open({
			  type: 1,
			  area: ['650px', '300px'], //宽高
			  title:title,
			  content: '<form id="order_form" class="" style="width:500px;margin:20px 0 0 18%;">'
						+'  <input type="hidden" name="group_id" value="'+group_id+'">'
						+'  <input type="hidden" name="user_id" value="'+user_id+'">'
				  	  +'	<div class="row cl">'
					  +'		<label class="form-label col-xs-4 col-sm-3"><span style="color:red;font-weight: bold;">*</span>登录邮箱</label>'
					  +'		<div class="formControls col-xs-8 col-sm-6">'
					  +'			<input type="text" name="user_name" placeholder="请输入邮箱地址" autocomplete="off" value="'+name+'" class="input-text" />'
					  +'		</div>'
					  +'	</div>'
					  +'	<div class="row cl mt-5">'
					  +'		<label class="form-label col-xs-4 col-sm-3"><span style="color:red;font-weight: bold;">*</span>姓名</label>'
					  +'		<div class="formControls col-xs-8 col-sm-6">'
					  +'			<input type="text" name="c_name" placeholder="请输入中文姓名" value="'+c_name+'" class="input-text" />'
					  +'		</div>'
					  +'	</div>'
					  +pwdInput
					  +'	<div class="row cl mt-5">'
					  +'		<label class="form-label col-xs-4 col-sm-3">角色</label>'
					  +'		<div class="formControls col-xs-8 col-sm-6">'
					  +'			<span class="select-box">'
					  +'                <select name="role_id" lay-verify="required" style="display:block;" class="select" lay-filter="change" >'
					  +roleHtml
					  +'			    </select>'
					  +'            </span>'
					  +'		</div>'
					  +'	</div>'
					  +roleSelectInput
					  +'	<div class="layui-form-item layui-col-md8" style="margin:15px 0 0 26%;">'
					  +'		<button id="submitUserBtn" type="button" class="btn btn-success">确认</button>'
					  +'        <button id="cancelUserBtn" type="button" class="btn btn-default">取消</button>'
					  +'	</div>'
					  +'</form>'
			});
		});
	}

	var formValidate = function(){
		return $('#order_form').validate({
			rules: {
				user_name: {
				   required: true,
				   email: true
				},
				c_name: {
					required: true
				},
				pwd: {
					required: !is_edit_user
				}
			},
			success: 'valid'
		});
	}

	//动态按钮需事件委托
	$(document).on("click", "#submitUserBtn", function(){ 
		//检测验证是否通过
		// Validator.form() Validates the form, returns true if it is valid, false otherwise.
		if(!formValidate().form()){
			return false;
		}
		var layer_index = layer.load(1, {
			shade: [0.3,'#000'] //0.3透明度的黑色背景
		});
		$.post('/company/saveUser', $('#order_form').serialize(), function(data){
			layer.close(layer_index);//关闭loading
			if(data.RESULT){
				layer.close(layer_form_index);//关闭form弹框
				//reload table
				table.ajax.url('/company/getDepartmentUser?group_id='+$("#group_id").val()).load();
			}else{
				layer.alert('保存失败, '+data.MSG, {icon: 2});
			}
		});
	});

	$(document).on("click","#cancelUserBtn",function(){
		layer.closeAll();
	});
	
		

		$(document).on("click",".editUser",function(){
			var tr = $(this).closest('tr');
			//var index = table.row( tr ).index();
			var data =table.row(tr).data();
			console.log(data);
			addUserPage("编辑成员",data.C_NAME,data.USER_NAME, '','', data.ID, data.ROLE_ID, data.GROUP_ID);
		});
	});
});