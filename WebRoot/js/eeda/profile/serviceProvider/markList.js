define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'dtColReorder', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {

    
  		$('#menu_todo_list').removeClass('active').find('ul').removeClass('in');
		$('#menu_order').addClass('active').find('ul').addClass('in');
		

  	  
  	$('.complex_search').click(function(event) {
        if($('.search_single').is(':visible')){
          $('.search_single').hide();
        }else{
          $('.search_single').show();
        }
    });
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
              { "data": "SP_NAME","width":"120px",
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
              { "data": "SUM_SCORE","width":"40px",
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
              { "data": "SPID", "width":"40px",
                  "render": function ( data, type, full, meta ) {
                      return "<a class=' btn table_btn btn_green btn-xs' href='/supplierRating/edit?sp_id="+full.SPID+"'target='_blank'>打分</a>";
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

      


      
      
      $('#resetBtn').click(function(){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(type){
          var sp_name = $.trim($("#sp_name_input").val()); 
          //增加出口日期查询
          var url = "/supplierRating/list?sp_name_like="+sp_name;
;

          dataTable.ajax.url(url).load();
      };
      
  });
});