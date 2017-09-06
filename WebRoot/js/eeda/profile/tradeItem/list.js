define(['jquery', 'metisMenu', 'sb_admin',  'sco','dataTablesBootstrap'], function ($, metisMenu) { 

    $(document).ready(function() {

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            ajax: "/tradeItem/list",
            columns:[
                 { "data": "COMMODITY_NAME","width":"100px"},  
                 { "data": "COMMODITY_CODE","width":"100px"}, 
                 { "data": "UNIT_NAME"},                   
                 { "data": "VAT_RATE","width":"80px"},
                 { "data": "REBATE_RATE"},          
                 { "data": "REMARK"},
                 {"data": null, 
                    "render": function ( data, type, full, meta ) {
                      var str = "<a class='btn  btn-primary btn-sm' href='/tradeItem/edit?id="+full.ID+"' target='_blank'>"+
                        "<i class='fa fa-edit fa-fw'></i>"+
                        "编辑"+"</a> ";
                      return str;
                    }
                }
            ]
        });
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })
      $('#exportTotaledExcel').click(function(){
    	  $(this).attr('disabled', true);
          var commodity_name = $("#commodity_name").val();
          var commodity_code = $("#commodity_code").val();
          excel_method(commodity_name,commodity_code);
      });
      var excel_method = function(commodity_name,commodity_code){
		  $.post('/tradeItem/downloadExcelList',{commodity_name:commodity_name,commodity_code:commodity_code}, function(data){
	          $('#exportTotaledExcel').prop('disabled', false);
	          $.scojs_message('生成应收Excel对账单成功', $.scojs_message.TYPE_OK);
	          window.open(data);
	      }).fail(function() {
	          $('#exportTotaledExcel').prop('disabled', false);
	          $.scojs_message('生成应收Excel对账单失败', $.scojs_message.TYPE_ERROR);
	      });
      }
      
     var searchData=function(){
          var commodity_name = $("#commodity_name").val().trim();
          var commodity_code = $("#commodity_code").val().trim();
          var url = "/tradeItem/search?commodity_name="+commodity_name+"&commodity_code="+commodity_code;
          dataTable.ajax.url(url).load();
      };
    });
});