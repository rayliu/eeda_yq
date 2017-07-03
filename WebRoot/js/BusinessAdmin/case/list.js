define(['jquery', 'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {

      var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: false,
          serverSide: true, 
          ajax: "/BusinessAdmin/ad/cu/list",
          columns: [
            { "data": "TITLE", "width": "100px"},
            { "data": "DATE_FROM","width": "100px"},
            { "data": null, "width": "100px",
              render: function(data,type,full,meta){
                return "<a class='stdbtn btn_blue' href='#'>编辑</a> <a class='stdbtn btn_blue' href='#'>删除</a>";
              }
            }
          ]
	    });
  });
});