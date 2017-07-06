define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 

    $(document).ready(function() {

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            ajax: "/custom/list",
            columns:[
	              { "data": "CODE"}, 
	              { "data": "NAME"}, 
                  {"data": null, 
                    "render": function ( data, type, full, meta ) {
                      var str = "<a class='btn  btn-primary btn-sm' href='/custom/edit?id="+full.ID+"' target='_blank'>"+
                        "<i class='fa fa-edit fa-fw'></i>"+
                        "编辑"+"</a> ";
                      return str;
                    }
                },
            ]
        });
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
          var code = $("#code").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/custom/list?code="+code;

          dataTable.ajax.url(url).load();
      };
    	
    });
});