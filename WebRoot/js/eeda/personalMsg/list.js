define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'dtColReorder'], function ($, metisMenu) { 
    $(document).ready(function() {
    	document.title = '消息 | '+document.title;
    	
      $("#breadcrumb_li").text('消息');
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, 
            colReorder: true,
            ajax: "/personalMsg/list",
            columns:[
                { "width": "80px",
                    "render": function ( data, type, full, meta ) {
                      return '<button type="button" class="delete btn table_btn btn-default btn-xs">'+
                        ' 标记为已读</button>';
                    }
                },
                { "data": "STATUS", "width": "80px"},
                {"data": "CONTENT", "width":"70%",
              	  "render": function ( data, type, full, meta ) {
              		  return "<a href='#' class='edit' >"+data+"</a>";
              	  }
                },
	              { "data": "CREATE_STAMP", "width":"120px"},
            ]
        });

      //base on config hide cols
      dataTable.columns().eq(0).each( function(index) {
          var column = dataTable.column(index);
          $.each(cols_config, function(index, el) {
              
              if(column.dataSrc() == el.COL_FIELD){
                console.log(column.dataSrc()+":"+el.IS_SHOW);
                if(el.IS_SHOW == 'N'){
                  column.visible(false, false);
                }else{
                  column.visible(true, false);
                }
              }
          });
      });
      //dataTable.columns.adjust().draw();
      
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