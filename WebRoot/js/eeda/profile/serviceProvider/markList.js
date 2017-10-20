define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'dtColReorder', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
          sort:true,
          colReorder: true,
          paging: true,
          serverSide: true, //不打开会出现排序不对
          ajax: "/supplierRating/list",
           "drawCallback": function( settings ) {
                $('.other').popover({
                    html: true,
                    container: 'body',
                    placement: 'right',
                    trigger: 'hover'
                });
        },
          columns: [
              { "data": "SP_NAME","width":"150px",
                  "render": function ( data, type, full, meta ) {
                	  var other = '';
                    var new_count='';
                    if(full.NEW_COUNT>0){
                      new_count='style="background-color:white;color:red;"';
                    }
                	  if(full.OTHER_FLAG=='other'){
                		  other = ' <span class="" '+new_count
                          +'><img src="/images/order_from_outside.png" style="height:15px;" title="Outside Order"></span>';
                	  }
                      return "<a href='/serviceProvider/edit?id="+full.SPID+"'target='_blank'>"+data+other+"</a>";
                  }
              },
              { "data": "SUM_SCORE","width":"100px",
                  "render": function ( data, type, full, meta ) {
	                    if(!data){
	                    	data='80';
	                    	return data;
	                    }
	                    var data = data+80;
	                    return data;
                  }
              }, 
              { "data": "ITEM_SMG","width":"100px",
                  "render": function ( data, type, full, meta ) {
                    if(!data){
                      data = '';
                    }
                    return data;
                  }
              },
              { "data": "SPID","width":"200px",
                  "render": function ( data, type, full, meta ) {
                      return "<a class=' btn table_btn btn_green btn-xs' href='/supplierRating/edit?sp_id="+full.SPID+"'target='_blank'>打分</a>";
                  }
              }
          ]
      });

      
	});
});