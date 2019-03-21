define(['jquery', 'dataTables', 'layer'], function ($) {

	$(document).ready(function() {
		document.title = '角色设置';
		var dataTable = eeda.dt({
			id: 'eeda_table',
			ajax: "/role/list",
			paging:true,
            lengthChange:false,
	        "columns": [
	            { "data": "NAME",},
				{ "data": "REMARK"}, 
				{ "data": null, 
					"render": function ( data, type, full, meta ) {
						if(full.PERMISSION_COUNT>0){
							return '<span style="color:green;">已分配</span>';
						}
						return "未分配";
					}
				}, 
	            { "data": null,
	            	"width": "20%",
	            	"render": function ( data, type, full, meta ) {
						var str="";
						str += "<nobr><a class='btn  btn-primary btn-sm' href='/role/edit?id="+full.ID+"'>"
							+ "<i class='fa fa-edit fa-fw'></i> "
							+ "编辑"
							+ "</a> ";
						str += "<nobr><a class='btn  btn-primary btn-sm' href='/role/permission/"+full.ID+"'>"
							+ "<i class='fa fa-edit fa-fw'></i> "
							+ "编辑权限"
							+ "</a> ";
						str += "<a class='btn btn-sm btn-danger delete_role' href='#' role_id='"+full.ID+"'>"
							+ "<i class='fa fa-trash-o fa-fw'></i> "
							+ "删除"
							+ "</a>";
						return str +="</nobr>";
                  }
	        	}
	        ]
			});
		
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