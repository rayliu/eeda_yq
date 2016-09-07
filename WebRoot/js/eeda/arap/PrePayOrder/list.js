define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	document.title = '手工成本单查询 | '+document.title;
    if(type!=""){
		  $('#menu_todo_list').addClass('active').find('ul').addClass('in');
		  $('#menu_cost').removeClass('active').find('ul').removeClass('in');
	  }else{
		$('#menu_todo_list').removeClass('active').find('ul').removeClass('in');
		  $('#menu_cost').addClass('active').find('ul').addClass('in');
	  }
                      
      var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: true,
          serverSide: false, //不打开会出现排序不对 
          ajax: "/costPrePayOrder/list",
          columns: [
            {"data":"ORDER_NO","width": "80px",
            	"render": function(data, type, full, meta) {
        			return "<a href='/costPrePayOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
        		}},
            {"data":"SP_NAME","width": "150px"},
            {"data":"TOTAL_AMOUNT","width": "100px"},
            {"data":"STATUS","width": "100px"},
            {"data":"CREATOR_NAME","width": "100px"},
            {"data":"CREATE_DATE","width": "100px",
                "render":function(data, type, full, meta){
                    if(data){
                        return data.substring(0,10);
                    }
                    else
                        return "";
                }
            },
            {"data":"REF_NO","width": "150px"},
            {"data":"REMARK","width": "150px"}                       
        ]      
    });	 
 
      $('#resetBtn').click(function(e){
          $("#prepayForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })
	    var searchData=function(){
	        var order_no = $("#order_no").val();
	        var start_time = $("#start_time").val();
	        var end_time = $("#end_time").val();
	        var sp = $("#sp_filter").val();
	        var url = "/costPrePayOrder/list?beginTime="+start_time+"&endTime="+end_time+"&spName="+sp+"&orderNo="+order_no;
	        dataTable.ajax.url(url).load();
	    };
});
});