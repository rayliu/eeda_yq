define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '贸易商品信息列表 | '+document.title;
    	$('#menu_profile').addClass('active').find('ul').addClass('in');
    	$('#breadcrumb_li').text('贸易商品信息列表');
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            ajax: "/tradeItem/list",
            columns:[
                 { "data": "COMMODITY_NAME","width":"100px"},          
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

     var searchData=function(){
          var code = $("#code").val().trim();
          var name = $("#name").val().trim();
          var name_eng = $("#name_eng").val().trim();
          
          var url = "/finItem/list?name="+name+"&code="+code+"&name_eng="+name_eng;
          dataTable.ajax.url(url).load();
      };
    	
      
      
    });
});