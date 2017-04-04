define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 
    $(document).ready(function() {
    	document.title = '速卖通账号 | '+document.title;
    	
      $("#breadcrumb_li").text('速卖通账号');
    	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: false,
          serverSide: true, //不打开会出现排序不对
          ajax: "/aliexpressAccount/list",
          columns:[
              {"data": "CODE", "width":"60px",
                "render": function ( data, type, full, meta ) {
                  return '<button type="button" class="stop btn btn-default btn-xs">'+
                        '<i class="fa fa-trash-o"></i> 停用</button> '+
                        '<button type="button" class="edit btn btn-default btn-xs">'+
                        '<i class="fa fa-refresh"></i> 重新授权</button>';
                }
              },
              { "data": "CODE", "width":"80px"},
              { "data": "ACCOUNT_NAME", "width":"120px"},
              { "data": "CLIENT_ID", "width":"120px"}, 
              { "data": "REFRESH_TOKEN_TIMEOUT", "width":"120px"},
              { "data": "STATUS", "width":"60px"}
          ]
      });
      
      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){

          var url = "/aliexpressAccount/list";

          dataTable.ajax.url(url).load();
      };
      
      $('#eeda_table').on('click','.edit',function(){
    	  var tr = $(this).parent().parent();
    	  $('#edit_id').val(tr.attr('id'));
    	  $('#edit_radioTitle').val($(this).text());
    	  $('#edit_radioContent').val($(tr.find(".content")).text());
    	  $('#addAccountDetail').modal('show');
      });
    	
    });
});