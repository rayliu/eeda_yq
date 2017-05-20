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
                        return "<a  href='/loginUser/edit?id="+full.ID+"' target='_blank' >" + data + "</a>";
                    }
                },
                { "data": "C_NAME", "width": "10%" },
                { "data": "POSITION_NAME", "width": "10%",
                    "render":function(data, type, full, meta){
                        if(data){
                            return "<a  href='/role/ClickRole?id="+full.ROLE_ID+"' target='_blank' >" + data + "</a>";
                        }else{
                            return "";
                        }
                    }
                },
                //{ "data": "ROLE_MSG", "width": "40%", "visible": false},
                { "data": "PASSWORD_HINT","width": "15%"},
                { 
                    "data": null, 
                    "width": "5%",
                    //"bVisible":(User.update || User.del),
                    "render": function(data, type, full, meta) {
                        var str = "<nobr>";
                        str = str + "<a class='btn  btn-primary btn-sm editbutton' href='/loginUser/edit?id="+full.ID+"' target='_blank'>"+
                                     "<i class='fa fa-edit'> </i>编辑</a> ";
                        
                        if(full.IS_STOP != true){
                            str = str +"<a class='btn  btn-danger btn-sm ' href='/loginUser/del/"+full.ID+"'>"+
                                         "<i class='fa fa-trash-o fa-edit'></i>停用</a>";
                        }else{
                            str = str + "<a class='btn btn_green btn-xs dropdown-toggle' href='/loginUser/del/"+full.ID+"'>"+
                                         "<i class='fa fa-trash-o fa-edit'></i>启用 </a>";
                        }
                        
                        return str+"</nobr>"
                }
               }
           ]
        });


    });
});