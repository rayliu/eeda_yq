define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'dtColReorder', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
          serverSide: true, //不打开会出现排序不对
          colReorder: true,
          paging: true,
          ajax: "/supplierRating/list",
          columns:[
              { "data": "ORDER_NO","width":"150px",
            	  "render": function ( data, type, full, meta ) {
                      return "<a href='/supplierRating/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
                  }
              },
              { "data": "STATUS","width":"100px"}, 
              { "data": "SP_NAME","width":"100px" },
              { "data": "ITEM_NAME","width":"100px"},
              { "data": "PLUS_SCORE","width":"100px"},
              { "data": "REDUCE_SCORE","width":"100px"},
              { "data": "SUM_SCORES","width":"100px"},
              { "data": "CREATOR_NAME","width":"100px"},
              { "data": "CREATE_STAMP","width":"200px"},
          ]
      });
      
      $('.complex_search').click(function(event) {
          if($('.search_single').is(':visible')){
            $('.search_single').hide();
          }else{
            $('.search_single').show();
          }
      });
      //清空查询条件
  	$('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });
      $("#singleSearchBtn").click(function(){
    	  var sp_id = $("#single_sp_id").val();
    	  var sp_name = $("#single_sp_id_input").val();
    	  $("#orderForm")[0].reset();
      	  $("#sp_id_input").val($("#single_sp_id_input").val());
     	  var url = "/supplierRating/list?sp_id="+sp_id
     	  		  + "&&sp_name="+sp_name;
     	  dataTable.ajax.url(url).load();
      })
      
      $("#searchBtn").click(function(){
    	  var sp_id = $("#sp_id").val();
    	  var sp_name = $("#sp_id_input").val();
    	  var status = $("#status").val();
    	  var creator = $("#creator").val();
     	 var url = "/supplierRating/list?sp_id="+sp_id
     	 			+"&&sp_name="+sp_name
			     	+"&&status="+status
			     	+"&&creator_name="+creator;
     	 dataTable.ajax.url(url).load();
      })
      
	});
});