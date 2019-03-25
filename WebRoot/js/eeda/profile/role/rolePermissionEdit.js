define(['jquery', 'dataTables', 'layer'], function ($) {

	$(document).ready(function() {
		document.title = '角色权限设置';
		var role_id = $('#role_id').val();
		var dataTable = eeda.dt({
			id: 'eeda_table',
			ajax: "/role/getMenuList?role_id="+role_id,
			paging:false,
            lengthChange:false,
	        "columns": [
				{ "data": "MODULE_NAME", width: 180,
					"render": function ( data, type, full, meta ) {
						if(full.LEVEL==1){
							return data;
						}else{
							return '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'+data;
						}
						
					}
				},
				{ "data": "IS_MENU_OPEN", width: 80,
					"render": function ( data, type, full, meta ) {
						if(full.LEVEL==1){
							return '';
						}
						var checked = "";
						if(data=='Y')
							checked = "checked";
						return '<div class="check-box">'
							+'<input type="checkbox" name="iCheck" class="is_menu_open" menu_id="'+full.ID+'" '+checked+'>'
							+'<label for="checkbox-1">开启</label>'
							+'</div>';
					}
				}, 
				{ "data": null, 
					"render": function ( data, type, full, meta ) {
						if(full.LEVEL==1){
							return '';
						}
						var is_search_open_checked = "";
						var permission_list = full.PERMISSION_LIST;
						
						return '<div class="check-box">'
							+'<input type="checkbox" name="iCheck" class="is_search_open" operation_name="查询" module_id="'+full.ID+'" '
							+    getCheckedFlag(permission_list, '查询')+'>'
							+'<label for="checkbox-1">查询</label>'
							+'</div>'
							+'<div class="check-box">'
							+'<input type="checkbox"  name="iCheck" class="is_add_open" operation_name="新增" module_id="'+full.ID+'" '
							+    getCheckedFlag(permission_list, '新增')+'>'
							+'<label for="checkbox-1">新增</label>'
							+'</div>'
							+'<div class="check-box">'
							+'<input type="checkbox" name="iCheck" class="is_update_open" operation_name="修改" module_id="'+full.ID+'" '
							+    getCheckedFlag(permission_list, '修改')+'>'
							+'<label for="checkbox-1">修改</label>'
							+'</div>'
							+'<div class="check-box">'
							+'<input type="checkbox" name="iCheck" class="is_delete_open" operation_name="删除" module_id="'+full.ID+'" '
							+    getCheckedFlag(permission_list, '删除')+'>'
							+'<label for="checkbox-1">删除</label>'
							+'</div>';
					}
				}
			],
			initComplete:function ( row, data, index ) {
				$('body input').iCheck({
					checkboxClass: 'icheckbox-blue',
					radioClass: 'iradio-blue',
					increaseArea: '20%'
				});
				//menu
				$('body input.is_menu_open').on('ifChecked',function(e){
					var menu_id=$(this).attr("menu_id");
					$.post('/role/setPermissionMenu',{role_id: role_id, menu_id:menu_id, checked:true}, function(data){});
				});
				$('body input.is_menu_open').on('ifUnchecked',function(e){
					var menu_id=$(this).attr("menu_id");
					$.post('/role/setPermissionMenu',{role_id: role_id, menu_id:menu_id, checked:false}, function(data){});
				});
				//CRUD
				$('body input.is_search_open, body input.is_add_open, body input.is_delete_open, body input.is_update_open').on('ifChecked',function(e){
					var module_id=$(this).attr("module_id");
					var operation_name=$(this).attr("operation_name");
					$.post('/role/setModulePermission',{role_id: role_id, module_id:module_id, operation_name:operation_name, checked:true}, function(data){});
				});
				$('body input.is_search_open,body input.is_add_open, body input.is_delete_open, body input.is_update_open').on('ifUnchecked',function(e){
					var module_id=$(this).attr("module_id");
					var operation_name=$(this).attr("operation_name");
					$.post('/role/setModulePermission',{role_id: role_id, module_id:module_id, operation_name:operation_name, checked:false}, function(data){});
				});
			}
		});
		
		var getCheckedFlag=function(permission_list, type){
			var checked = "";
			permission_list.forEach(p => {
				if(p.PERMISSION_NAME==type){
					checked = "checked";
					return;
				}			
			});
			return checked;
		}
			
			$(document).on("click",".delete_role", function(){
				var role_id = $(this).attr('role_id');
				//询问框
				layer.confirm('您确定删除该角色？', {
					btn: ['确定','取消'] //按钮
				}, function(){
					window.location.href='/role/deleteRole/'+role_id;
				}, function(){
					
				});
			});
			
	});
});