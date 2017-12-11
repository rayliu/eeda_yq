define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 
    $(document).ready(function() {
    	$('.search_single input,.search_single select').on('input',function(){
    		  $("#orderForm")[0].reset();
    	  });
    	
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            ajax: "/finItem/list",
            columns:[
                 { "data": "CODE"},          
                 { "data": "NAME"},          
                 { "data": "NAME_ENG"}, 
                 { "data": "BINDING_CURRENCY"},
                 { "data": "REMARK"},
                 {"data": null, 
                    "render": function ( data, type, full, meta ) {
                      var str = "<a class='btn  btn-primary btn-sm' href='/finItem/edit?id="+full.ID+"' target='_blank'>"+
                        "<i class='fa fa-edit fa-fw'></i>"+
                        "编辑"+"</a> ";
                      return str;
                    }
                }
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
          
          var url = "/finItem/list?name_equals="+name+"&code_equals="+code+"&name_eng_equals="+name_eng;
          dataTable.ajax.url(url).load();
      };
    	
      
      
    });
});