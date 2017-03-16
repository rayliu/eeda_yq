define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 
    $(document).ready(function() {
    	document.title = '亚马逊账号 | '+document.title;
    	
      $("#breadcrumb_li").text('亚马逊账号');
    	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: false,
          serverSide: true, //不打开会出现排序不对
          ajax: "/amazonAccount/list",
          columns:[
              {"data": null, "width":"60px",
                "render": function ( data, type, full, meta ) {
                  return '<button type="button" class="btn btn-default btn-xs">'+
                        '<i class="fa fa-trash-o"></i> 停用</button>';
                }
              },
              { "data": "SELLER_NAME", "width":"80px"},
              { "data": "SELLER_ID", "width":"80px"},
              { "data": "AWS_ACCESS_KEY_ID", "width":"120px",
                "render": function ( data, type, full, meta ) {
                  if(data){
                    return '*****';
                  }
                  return '未设置';
                }
              }, 
              { "data": "SECRET_KEY", "width":"120px",
                "render": function ( data, type, full, meta ) {
                  if(data){
                    return '*****';
                  }
                  return '未设置';
                }
              },
              { "data": "CREATOR_NAME", "width":"60px"}, 
              { "data": "CREATE_TIME", "width":"90px"}
          ]
      });
      
      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){

          var url = "/ebayAccount/list";

          dataTable.ajax.url(url).load();
      };
      
      $('#eeda_table').on('click','.edit',function(){
    	  var tr = $(this).parent().parent();
    	  $('#edit_id').val(tr.attr('id'));
    	  $('#edit_radioTitle').val($(this).text());
    	  $('#edit_radioContent').val($(tr.find(".content")).text());
    	  $('#editRadio').click();
      });
    	
});
});