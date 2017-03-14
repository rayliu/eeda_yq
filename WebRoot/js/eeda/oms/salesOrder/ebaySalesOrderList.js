define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 
    $(document).ready(function() {
    	document.title = 'eBay订单 | '+document.title;
    	
      $("#breadcrumb_li").text('eBay订单');
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/ebaySalesOrder/list",
            columns:[
                { "width": "80px",
                    "render": function ( data, type, full, meta ) {
                      return '<button type="button" class="delete btn table_btn btn-default btn-xs">'+
                        '<i class="fa fa-trash-o"></i> 删除</button>';
                    }
                },
                {"data": "TITLE", "width":"120px",
              	  "render": function ( data, type, full, meta ) {
              		  return "<a href='#' class='edit' >"+data+"</a>";
              	  }
                },
	              { "data": "CONTENT", "className":"content"}, 
	              { "data": "CREATE_NAME", "width":"60px"}, 
	              { "data": "CREATE_STAMP", "width":"90px"}, 
	              { "data": "UPDATE_NAME", "width":"60px"},
	              { "data": "UPDATE_STAMP", "width":"90px"},
                { "data": "CREATE_NAME", "width":"60px"}, 
                { "data": "CREATE_STAMP", "width":"90px"}, 
                { "data": "UPDATE_NAME", "width":"60px"},
                { "data": "UPDATE_STAMP", "width":"90px"},
                { "data": "UPDATE_NAME", "width":"60px"}
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