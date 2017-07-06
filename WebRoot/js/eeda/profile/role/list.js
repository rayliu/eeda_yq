define(['jquery', 'metisMenu', 'sb_admin', 'dataTablesBootstrap', 'validate_cn'], function ($, metisMenu) {

	$(document).ready(function() {


	var dataTable = eeda.dt({
			id: 'example',
	        "ajax": "/role/list",
	        "columns": [
	            { "data": "NAME",},
	            { "data": "REMARK"}, 
	            { "data": null,
	            	"width": "20%",
	            	"render": function ( data, type, full, meta ) {
						var role_update_permission = Role.UpdatePermission;
						var role_del_permission = Role.DelPermission;

						var str="";
						if(role_update_permission){
							str += "<nobr><a class='btn  btn-primary btn-sm' href='/role/ClickRole?id="+full.ID+"' target='_blank'>"
								+ "<i class='fa fa-edit fa-fw'></i> "
								+ "编辑"
								+ "</a> ";
						}
						if(data.CODE != "admin"){
							if(role_del_permission){
								str += "<a class='btn  btn-sm btn-danger' href='/role/deleteRole/"+full.ID+"'>"
									+ "<i class='fa fa-trash-o fa-fw'></i> "
									+ "删除"
									+ "</a>";
							}
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