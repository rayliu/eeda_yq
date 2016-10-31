define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 
    $(document).ready(function() {
    	document.title = '公告列表| '+document.title;
    	
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/msgBoard/list",
            columns:[
                { "width": "30px",
                    "render": function ( data, type, full, meta ) {
                      return '<button type="button" class="delete btn btn-default btn-xs" style="width:50px">删除</button>';
                    }
                },
                {"data": "TITLE", "width":"20%",
              	  "render": function ( data, type, full, meta ) {
              		  return "<a href='#' class='edit' >"+data+"</a>";
              	  }
                },
	              { "data": "CONTENT", "width":"40%", "className":"content"}, 
	              { "data": "CREATE_NAME"}, 
	              { "data": "CREATE_STAMP"}, 
	              { "data": "UPDATE_NAME"},
	              { "data": "UPDATE_STAMP"},
            ]
        });
      
      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
          var creator = $.trim($("#creator").val()); 
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();

          var url = "/msgBoard/list?create_name_like="+creator
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date;

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