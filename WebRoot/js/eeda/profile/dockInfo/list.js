define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) {
    $(document).ready(function() {
        document.title = '陆运地点信息列表 | '+document.title;
        $("#breadcrumb_li").text('陆运地点信息列表');
        var carTable = eeda.dt({
            id: 'car_table',
            paging:true,
            autoWidth: false,
            ajax: "/dockInfo/list",
            columns:[
                { "data":"DOCK_NAME",
                    "render": function ( data, type, full, meta ) {
                        return "<a href='/dockInfo/edit?id="+full.ID+"'target='_blank'>"+full.DOCK_NAME+"</a>";  
                    }},
                { "data":"DOCK_NAME_ENG"},
                { "data":"QUICK_SEARCH_CODE"},
                { "data":"DOCK_REGION"},
                { 
                    "data": null, 
                    "width": "5%",
                    "visible":(Provider.isUpdate || Provider.isDel),
                    "render": function(data, type, full, meta) {
                         var str ="<nobr>";
                         if(Provider.isUpdate){
                          str += "<a class='btn table_btn btn-success btn-sm' href='/dockInfo/edit?id="+full.ID+"' target='_blank'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "编辑"+"</a> ";
                         }
                        if(Provider.isDel){
                            if(full.IS_STOP != true){
                                str += "<a class='btn table_btn btn-danger btn-sm' href='/dockInfo/delete/"+full.ID+"'>"+
                                     "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                     "停用"+
                                     "</a>";
                             }else{
                                str +="<a class='btn table_btn btn-success btn-sm' href='/dockInfo/delete/"+full.ID+"'>"+
                                         "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                         "启用"+
                                     "</a>";
                             }
                         }
                         return str +="</nobr>";
                    }
                }
            ]
        });
        $('#resetBtn').click(function(){
        	$('#query_dock_name').val('');
        	$("#query_quick_search_code").val('');
        	$("#query_dock_region").val('');
        });
        $('#searchBtn').click(function(){
            searchData(); 
        });

       var searchData=function(){
            var dock_name = $('#query_dock_name').val().trim();
            var quick_search_code = $("#query_quick_search_code").val().trim();
            var dock_region = $("#query_dock_region").val().trim();
            
            var url = "/dockInfo/list?dock_name="+dock_name
            	   +"&quick_search_code="+quick_search_code
                 +"&dock_region="+dock_region;

            carTable.ajax.url(url).load();
          }

    });
});