define(['jquery', 'metisMenu', 'sb_admin', 'sco', 'dataTablesBootstrap', 'validate_cn',  'dtColReorder'], function ($, metisMenu) { 

    $(document).ready(function() {
    	  document.title = '邮箱账号设置 | '+document.title;
        $("#breadcrumb_li").text('邮箱账号设置');

    	  //datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            colReorder: true,
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/mailBox/configList",
            columns:[
                { "data": "USER", "width": "40%",
                    "render": function ( data, type, full, meta ) {
                        return "<a href='/mailBox/edit?id="+full.ID+"' target='_blank'>" + data+ "</a>";
                    }
                },
                { "data": "CREATE_TIME", "width": "10%"},
                { 
                    "data": null, 
                    "width": "5%",
                    "render": function(data, type, full, meta) {
                         var str ="<nobr>";
                         //if(Provider.isUpdate){
                          str += "<a class='btn table_btn btn-success btn-sm' href='/mailBox/edit?id="+full.ID+"' target='_blank'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "编辑"+"</a> ";
                         //}

                            str += "<a class='btn table_btn btn-danger btn-sm' href='/mailBox/delete/"+full.ID+"'>"+
                                 "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                 "删除"+
                                 "</a>";

                         return str +="</nobr>";
                    }
                }
            ]
        });
        $('#addBtn').click(function(e){
          $('#configModal').modal('show');
        });

      //对话框确定
    	$('#configConfirm').click(function(e){
          $.post("/mailBox/configSave", function(data){
            if(data== "OK"){
                $.scojs_message('成功', $.scojs_message.TYPE_OK);

            }else{
              $.scojs_message('失败', $.scojs_message.TYPE_ERROR);

            }
            itemOrder.refleshItemTable(data.ID);
          },'text').fail(function() {
              $.scojs_message('失败', $.scojs_message.TYPE_ERROR);

          });
      });

        //条件筛选
    	$("#searchBtn").on('click', function () {
          	var title = $("#title").val().trim();

          	var url = "/mailBox/configList?title_like="+title;
          	dataTable.ajax.url(url).load();
        });

    });
});