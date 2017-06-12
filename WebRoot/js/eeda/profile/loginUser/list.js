define(['jquery', 'metisMenu', 'sb_admin', 'dataTablesBootstrap', 'sco'], function ($, metisMenu) {
    $(document).ready(function() {

        document.title = '登陆用户列表 | '+document.title;
        $("#breadcrumb_li").text('登陆用户列表');

        eeda.dt({
            id:'example',
            "ajax": "/loginUser/listUser",
            "columns": [
                { "data": "USER_NAME", "width": "20%",
                    "render":function(data, type, full, meta){
                    	return data;
                    }
                },
                { "data": "C_NAME", "width": "10%" },
                { "data": "POSITION_NAME", "width": "10%"},
                { "data": "PASSWORD_HINT","width": "15%"},
                { 
                    "data": null, 
                    "width": "5%",
                    "render": function(data, type, full, meta) {
                        var str = "<nobr>";

                        if(this_role != 'admin' && full.ROLE_CODE == 'admin' ){
                        	str = "无权限";
                        }else{
                        	if(User.update){
                            	str += "<a class='btn  btn-primary btn-sm editbutton' href='/loginUser/edit?id="+full.ID+"' target='_blank'>"+
                                "<i class='fa fa-edit'> </i>编辑</a> ";
                            }
                            
                            if(User.stop){
                            	if(full.IS_STOP != true){
                                    str = str +"<a class='btn  btn-danger btn-sm ' href='/loginUser/del/"+full.ID+"'>"+
                                                 "<i class='fa fa-trash-o fa-edit'></i>停用</a>";
                                }else{
                                    str = str + "<a class='btn btn_green btn-xs dropdown-toggle' href='/loginUser/del/"+full.ID+"'>"+
                                                 "<i class='fa fa-trash-o fa-edit'></i>启用 </a>";
                                }
                            }
                        }
                        return str+"</nobr>"
                    }
               }
           ]
        });


    });
});