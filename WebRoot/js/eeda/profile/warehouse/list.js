define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 

	document.title = '仓库查询 | '+document.title;
    $('#menu_profile').addClass('active').find('ul').addClass('in');

    var dataTable = eeda.dt({
        id: 'eeda-table',
        ajax: "/warehouse/list",
        columns:[
            {  "data": "WAREHOUSE_NAME", 
                "render": function ( data, type, full, meta ) {
                    if(Warehouser.isUpdate){
                        return "<a  href='/warehouse/edit?id="+full.ID+"' target='_blank'>" + data + "</a>";
                    }else{
                        return data;
                    }
                }
            },
            {"data":"NOTIFY_NAME"},
            {"data":"NOTIFY_MOBILE"},
            {"data":"DNAME"},
            {"data":"WAREHOUSE_ADDRESS"},
            {"data":"WAREHOUSE_DESC"},
            {
                "data": null,
                //"sWidth": "8%",
                //"bVisible":(Warehouser.isUpdate || Warehouser.isDel),
                "render": function ( data, type, full, meta )  {  
                    var str="<nobr>";
                    if(Warehouser.isUpdate){
                        str += "<a class='btn  btn-primary btn-sm' href='/warehouse/edit?id="+full.ID+"' target='_blank'>"+
                            "<i class='fa fa-edit fa-fw'></i>"+
                            "编辑"+
                            "</a> ";
                    }
                    if(Warehouser.isDel){
                        if(full.STATUS != "inactive"){
                            str += "<a class='btn btn-danger  btn-sm' href='/warehouse/delete/"+full.ID+"'>"+
                                    "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                    "停用"+
                                "</a>";
                        }else{
                            str += "<a class='btn btn-success  btn-sm' href='/warehouse/delete/"+full.ID+"'>"+
                                    "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                    "启用"+
                                "</a>";
                        }
                    }
                    str+="</nobr>";
                    return str;
                 }
              }
        ]
    });

    $('#resetBtn').click(function(e){
        $("#orderForm")[0].reset();
    });

    $('#searchBtn').click(function(){
        searchData(); 
    });

    var searchData=function(){
        var name = $("#warehouse_name").val();
        /*  
          查询规则：参数对应DB字段名
          *_no like
          *_id =
          *_status =
          时间字段需成双定义  *_begin_time *_end_time   between
        */
        var url = "/warehouse/list?warehouse_name="+name;

        dataTable.ajax.url(url).load();
    };
});