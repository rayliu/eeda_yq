define(['jquery', 'metisMenu', 'sb_admin', 'sco', 'dataTablesBootstrap', 'validate_cn',  'dtColReorder', 'jq_blockui'], function ($, metisMenu) { 

    $(document).ready(function() {


    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            colReorder: true,
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/mailBox/list",
            columns:[
                { "data": "MAIL_BOX","width": "10%"},
                { "data": "STATUS","width": "3%"},
                { "data": "FROM", "width": "10%"}, 
                { "data": "SUBJECT", "width": "40%",
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
                                "回复"+"</a> ";
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

        //base on config hide cols
        eeda.showCols(dataTable, cols_config);

      //清空查询条件
    	$('#getMailBtn').click(function(e){
          $.blockUI();
          $.post("/mailBox/receivceMail", function(data){
            if(data== "OK"){
                $.scojs_message('成功', $.scojs_message.TYPE_OK);
                dataTable.ajax.url("/mailBox/list").load();
                $.unblockUI();
            }else{
                $.scojs_message('失败', $.scojs_message.TYPE_ERROR);
                $.unblockUI();
            }
           
          },'text').fail(function() {
              $.scojs_message('失败', $.scojs_message.TYPE_ERROR);
              $.unblockUI();
          });
      });
    	
        //条件筛选
    	$("#searchBtn").on('click', function () {
        	var title = $("#title").val().trim();
        	var url = "/mailBox/list?title_like="+title;
        	dataTable.ajax.url(url).load();
      });

    });
});