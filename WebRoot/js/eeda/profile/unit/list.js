define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap',  'dtColReorder'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '计量单位列表 | '+document.title;
    	$('#menu_profile').addClass('active').find('ul').addClass('in');
    	$("#breadcrumb_li").text('计量单位列表');
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            colReorder: true,
            ajax: "/unit/list",
            columns:[
	              { "data": "CODE"},          
                  { "data": "NAME"},          
                  { "data": "NAME_ENG"},          
                  {"data": null, 
                    "render": function ( data, type, full, meta ) {
                      var str = "<a class='btn  btn-primary btn-sm' href='/unit/edit?id="+full.ID+"' target='_blank'>"+
                        "<i class='fa fa-edit fa-fw'></i>"+
                        "编辑"+"</a> ";
                      return str;
                    }
               	  },
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
    	  var code = $("#code").val().trim();
          var name = $("#name").val().trim();
          var name_eng = $("#name_eng").val().trim();
          
          var url = "/unit/list?name="+name+"&code="+code+"&name_eng="+name_eng;
          dataTable.ajax.url(url).load();
      };
    	
    });
});