define(['jquery', 'metisMenu', 'sb_admin', 'dataTablesBootstrap', 'sco'], function ($, metisMenu) {
    $(document).ready(function() {

        document.title = '登录用户查询 | '+document.title;
     
        eeda.dt({
            id:'example',
            "ajax": "/loginUser/listUser",
            "columns": [
                { "data": "USER_NAME", "width": "20%",
                    "render":function(data, type, full, meta){
                        return "<a  href='/loginUser/edit/"+full.ID+"' target='_blank' >" + data + "</a>";
                    }},
                { "data": "C_NAME", "width": "10%" },
                { "data": "POSITION_NAME", "width": "10%" },
                { "data": "ROLE_MSG", "width": "40%" },
                { "data": "PASSWORD_HINT","width": "15%"},
                { 
                    "data": null, 
                    "width": "5%",
                    //"bVisible":(User.update || User.del),
                    "render": function(data, type, full, meta) {
                        var str = "<nobr>";
                        str = str + "<a class='btn  btn-primary btn-sm editbutton' href='/loginUser/edit/"+full.ID+"' target='_blank'>"+
                                     "<i class='fa fa-edit'> </i>编辑</a> ";
                        
                        if(full.IS_STOP != true){
                            str = str +"<a class='btn  btn-danger btn-sm ' href='/loginUser/del/"+full.ID+"'>"+
                                         "<i class='fa fa-trash-o fa-edit'></i>停用</a>";
                        }else{
                            str = str + "<a class='btn  btn-success btn-sm dropdown-toggle' href='/loginUser/del/"+full.ID+"'>"+
                                         "<i class='fa fa-trash-o fa-edit'></i>启用 </a>";
                        }
                        
                        return str+"</nobr>"
                }
               }
           ]
        });


    });
});