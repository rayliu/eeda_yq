define(['jquery', 'metisMenu', 'sb_admin', 'dataTablesBootstrap', 'validate_cn'], function ($, metisMenu) {

	$(document).ready(function() {
		document.title = '岗位查询 | '+document.title;
		$("#breadcrumb_li").text('岗位列表');

	var dataTable = eeda.dt({
			id: 'role_table',
			"ajax": "/role/list",
			paging:true,
            lengthChange:false,
	        "columns": [
	            { "data": "NAME",},
	            { "data": "REMARK"}, 
	            { "data": null,
	            	"width": "20%",
	            	"render": function ( data, type, full, meta ) {
						var role_update_permission = true;//Role.UpdatePermission;
						var role_del_permission = true;//Role.DelPermission;

						var str="";
						if(data.CODE != "admin"){//|| this_rode =='admin' 
							if(role_update_permission){
								str += "<nobr><a class='btn  btn-primary btn-sm' href='/role/edit?id="+full.ID+"'>"
									+ "<i class='fa fa-edit fa-fw'></i> "
									+ "编辑"
									+ "</a> ";
							}
						
							if(role_del_permission && data.CODE != "admin"){
								str += "<a class='btn  btn-sm btn-danger' href='/role/deleteRole/"+full.ID+"'>"
									+ "<i class='fa fa-trash-o fa-fw'></i> "
									+ "删除"
									+ "</a>";
							}
						}else{
							str="无权限";
						}
						return str +="</nobr>";
                  }
	        	}
	        ]
			});
		$("#createBtn").click(function(){
			document.title=document.title.substring(6, 11);
			$("#roleList").hide();
			$("#addRole").show();
		});	
	    /*$("#saveBtn").click(function(){
	    	$("#roleList").show();
			$("#addRole").hide();
	    });	*/
	    $('#addRoleForm').validate({
			rules : {
				rolename : {
					required : true,
					remote:{
	                	url: "/role/checkRoleNameExit", //后台处理程序    
                        type: "post",  //数据发送方式  
                        data:  {                     //要传递的数据   
                        	rolename: function() {   
                                return $("#rolename").val();   
                              }   

                        } 
					}
				}
			},
			messages:{
            	rolename:{
            		remote:"岗位已存在"
            	}
            },
			highlight : function(element) {
				$(element).closest('.form-group')
						.removeClass('has-success')
						.addClass('has-error');
			},
			success : function(element) {
				element.addClass('valid').closest(
						'.form-group').removeClass(
						'has-error').addClass(
						'has-success');
			}
		});
	});
});