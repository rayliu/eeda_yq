define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco',  'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {
    
      eeda.hideSideBar();//打开报表时自动收起左边菜单
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
          colReorder: true,
          paging: true,
          serverSide: false, //不打开会出现排序不对
          columns: [
              { "data": "CREATE_STAMP" },
              { "data": "TYPE"},
              { "data": "SP_NAME1","visible":false},
              { "data": "SP_NAME2","visible":false},
              { "data": "SP_NAME3","visible":false},
              { "data": "SP_NAME4","visible":false},
              { "data": "SP_NAME5","visible":false},
              { "data": "SP_NAME6","visible":false},
              { "data": "SP_NAME7","visible":false},
              { "data": "SP_NAME8","visible":false},
              { "data": "SP_NAME9","visible":false},
              { "data": "SP_NAME10","visible":false},
              { "data": "SP_NAME11","visible":false},
              { "data": "SP_NAME12","visible":false},
              { "data": "SP_NAME13","visible":false},
              { "data": "SP_NAME14","visible":false},
              { "data": "SP_NAME15","visible":false},
              { "data": "SP_NAME16","visible":false},
              { "data": "SP_NAME17","visible":false},
              { "data": "SP_NAME18","visible":false},
              { "data": "SP_NAME19","visible":false},
              { "data": "SP_NAME20","visible":false}
          ]
      });
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });
      
      
      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
    	  //初始化列（隐藏）
    	  resetCol();
    	  
          var date_type = $('[name=type]:checked').val();
          var balance = $('[name=balance]:checked').val();
          var sp_names = $("#sp_names").val();
          var begin_date = '';
          var end_date = '';
          if(date_type=='year'){
        	  begin_date = $("#year_begin_time").val();
              end_date = $("#year_end_time").val();
          }else if(date_type=='season'){
        	  begin_date = $("#season_begin_time").val();
              end_date = $("#season_end_time").val();
          }else{
        	  begin_date = $("#month_begin_time").val();
              end_date = $("#month_end_time").val();
          }
          

          

          //增加出口日期查询
          var url = "/balanceReport/list?"
          	    +"balance="+balance
          	    +"&sp_names="+sp_names
          	    +"&date_type="+date_type
          		+"&begin_date="+begin_date
          		+"&end_date="+end_date;

          dataTable.ajax.url(url).load();
          
          //显示要出询的列
          if(sp_names!=''){
        	  showCol(sp_names);
          }
      };
      
      $('.collapsed').click(function(event) {
          if($('#exportTotaledExcel1').is(':visible')){
            $('#exportTotaledExcel1').hide();
          }else{
            $('#exportTotaledExcel1').show();
          }
      });
      
      //导出excel
      $('#exportTotaledExcel1').click(function(){
    	  $(this).attr('disabled', true);
    	    var date_type = $('[name=type]:checked').val();
            var balance = $('[name=balance]:checked').val();
            var sp_names = $("#sp_names").val();
            var begin_date = '';
            var end_date = '';
            if(date_type=='year'){
          	  begin_date = $("#year_begin_time").val();
                end_date = $("#year_end_time").val();
            }else if(date_type=='season'){
          	  begin_date = $("#season_begin_time").val();
                end_date = $("#season_end_time").val();
            }else{
          	  begin_date = $("#month_begin_time").val();
                end_date = $("#month_end_time").val();
            }
            
          excel_method(sp_names,begin_date,end_date,balance,date_type);
      });
      $('#exportTotaledExcel').click(function(){
    	  $(this).attr('disabled', true);
    	    var date_type = $('[name=type]:checked').val();
            var balance = $('[name=balance]:checked').val();
            var sp_names = $("#sp_names").val();
            var begin_date = '';
            var end_date = '';
            if(date_type=='year'){
          	  begin_date = $("#year_begin_time").val();
                end_date = $("#year_end_time").val();
            }else if(date_type=='season'){
          	  begin_date = $("#season_begin_time").val();
                end_date = $("#season_end_time").val();
            }else{
          	  begin_date = $("#month_begin_time").val();
                end_date = $("#month_end_time").val();
            }
          excel_method(sp_names,begin_date,end_date,balance,date_type);
      });
      var excel_method = function(sp_names,begin_date,end_date,balance,date_type){
		  $.post('/balanceReport/downloadExcelList',{sp_names:sp_names,
			  											begin_date:begin_date,end_date:end_date
			  											,balance:balance,date_type:date_type}, function(data){
	          $('#exportTotaledExcel1').prop('disabled', false);
	          $('#exportTotaledExcel').prop('disabled', false);
	          $.scojs_message('生成Excel成功', $.scojs_message.TYPE_OK);
	          window.open(data);
	      }).fail(function() {
	          $('#exportTotaledExcel').prop('disabled', false);
	          $.scojs_message('生成Excel失败', $.scojs_message.TYPE_ERROR);
	      });
      }
      
      var showCol = function(sp_names){
    	  var table = $('#eeda-table').dataTable();
    	  var names = [];
    	  names = sp_names.split(",");
    	  for(var i = 0;i<names.length;i++){
    		  table.fnSetColumnVis(2+i, true);
    		  $($('#eeda-table').DataTable().column(2+i).header()).text(names[i]);
    	  }
      }
      
      var resetCol = function(){
    	  var table = $('#eeda-table').dataTable();
    	  for(var i = 0;i<20;i++){
    		  table.fnSetColumnVis(2+i, false);
    	  }
      }
      
      $('[name=type]').on('click',function(){
    	  var value  = $(this).val();
    	  if(value=='year'){
    		  $('#year').show();
    		  $('#month').hide();
    		  $('#season').hide();
    	  }else if(value=='season'){
    		  $('#year').hide();
    		  $('#month').hide();
    		  $('#season').show();
    	  }else{
    		  $('#year').hide();
    		  $('#month').show();
    		  $('#season').hide();
    	  }
      })
      
      
      var sp_names = [];
	  $("#sp_id_list").on('mousedown', '.fromLocationItem', function(e){
		  var sp_name = $(this).text();
		  for(num in sp_names){//重复校验
    		  if(sp_names[num]==sp_name){
    			  $("#sp_id_input").val('');//清空文本框
    			  return false;
    		  }
    	  }
		  sp_names.push(sp_name);
		  
		  $('#sp_names').val(sp_names);
		  $('#spName_list').append('<li class="search-control">'+sp_name+'<a name="delete_icon" class="glyphicon glyphicon-remove" style="margin-right:15px;" role="menuitem" tabindex="-10"></a></li>')
  	  
		  $("#sp_id_input").val('');//清空文本框
	  });
      
      $('#spName_list').on('click', 'a', function(e){
    	  $(this).parent().hide();
    	  var sp_name = $(this).parent().text();
    	  for(num in sp_names){
    		  if(sp_names[num]==sp_name){
    			  sp_names.splice(num,1);
    		  }
    	  }
    	  $('#sp_names').val(sp_names);
      })
 

  });
});