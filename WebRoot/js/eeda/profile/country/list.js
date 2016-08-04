define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '单位查询 | '+document.title;
    	$('#menu_profile').addClass('active').find('ul').addClass('in');

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            ajax: "/country/list",
            columns:[
	              { "data": "CODE"},
	              { "data": "ENGLISH_NAME"}, 
	              { "data": "CHINESE_NAME"}, 
                  {"data": null,
	            	  "width": "20%",
		            	"render": function ( data, type, full, meta ) {            		
							
							var str="";

                            str = "<a class='btn  btn-primary btn-sm' href='/country/edit?id="+full.ID+"' target='_blank'>"+
                        "<i class='fa fa-edit fa-fw'></i>"+
                        "编辑"+"</a> ";                       
                          
                  		if(data.IS_STOP != true){
		                    str += "<a class='btn btn-danger  btn-sm' href='/country/delete?id="+full.ID+"'>"+
		                         "<i class='fa fa-trash-o fa-fw'></i>"+ 
		                         "停用"+
		                         "</a>";                		
	               	     }else{
	               		    str +="<a class='btn btn-success btn-sm' href='/country/delete?id="+full.ID+"'>"+
			                         "<i class='fa fa-trash-o fa-fw'></i>"+ 
			                         "启用"+
			                     "</a>";
	               	}
                  		    return str +="</nobr>";
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
          var chinese_name = $("#chinese_name").val();
          
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/country/list?chinese_name="+chinese_name;

          dataTable.ajax.url(url).load();
      };
    	
    });
});