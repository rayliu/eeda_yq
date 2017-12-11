define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap',  'dtColReorder'], function ($, metisMenu) { 
    $(document).ready(function() {
    	$('.search_single input,.search_single select').on('input',function(){
    		  $("#orderForm")[0].reset();
    	  });
    	
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
                      var str = "<a class='btn  btn-primary btn-sm' href='/unit/edit?id="+full.ID+"' target='_blank' style='display:none;'>"+
                        "<i class='fa fa-edit fa-fw'></i>"+
                        "编辑"+"</a> ";
                      return str;
                    }
               	  },
            ]
        });
        
        $('.complex_search').click(function(event) {
            if($('.search_single').is(':visible')){
              $('.search_single').hide();
            }else{
              $('.search_single').show();
            }
        });
        //简单查询
        $('#singleSearchBtn').click(function(){
        	$('#checked_application_table').empty();
        	singleSearchData();
        });
    	var singleSearchData = function(){ 
    		$("#orderForm")[0].reset();
    		$("#code").val($("#single_code").val());
    		$('#searchBtn').click();
    	}
        
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
          
          var url = "/unit/list?name_equals="+name+"&code_equals="+code+"&name_eng_equals="+name_eng;
          dataTable.ajax.url(url).load();
      };
    	
    });
});