define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap',  'dtColReorder'], function ($, metisMenu) { 

    $(document).ready(function() {

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            colReorder: true,
            ajax: "/employeeFiling/list",
            columns:[
	              { "data": "C_NAME"},          
                  { "data": "STATION_NAME"},          
                  { "data": "ENTRY_TIME",
                	"render":function(data, type, full, meta ){
                		if(!data){
                			return "";
                		}
                		return data.substr(0,10)
                	}  
                  },
                  { "data": "CREATE_TIME"},
                  {"data": null,"width":"50px", 
                    "render": function ( data, type, full, meta ) {
                      var str = "<a class='btn table_btn btn_green btn-xs' style='width:70px' href='/employeeFiling/edit?id="+full.ID+"' target='_blank'>"+
                        "<i class='fa fa-edit fa-fw'></i>"+
                        "编辑"+"</a> ";
                      return str;
                    }
               	  }
            ]
        });
      //base on config hide cols
        dataTable.columns().eq(0).each( function(index) {
            var column = dataTable.column(index);
            $.each(cols_config, function(index, el) {
                
                if(column.dataSrc() == el.COL_FIELD){
                  
                  if(el.IS_SHOW == 'N'){
                    column.visible(false, false);
                  }else{
                    column.visible(true, false);
                  }
                }
            });
        });
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
    	  var user_name = $("#user_name").val().trim();
          var station_name = $("#station_name").val().trim();
          var entry_time_begin_time = $("#entry_time_begin_time").val();
          var entry_time_end_time = $("#entry_time_end_time").val();
          var create_stamp_begin_time = $("#create_stamp_begin_time").val();
          var create_stamp_end_time = $("#create_stamp_end_time").val();
          
          
          var url = "/employeeFiling/list?c_name="+user_name
								          +"&station_name="+station_name
								          +"&induction_time_begin_time="+induction_time_begin_time
							          	  +"&induction_time_end_time="+induction_time_end_time
							          	  +"&create_stamp_begin_time="+create_stamp_begin_time
							          	  +"&create_stamp_end_time="+create_stamp_end_time;
          
          dataTable.ajax.url(url).load();
      };
    	
    });
});