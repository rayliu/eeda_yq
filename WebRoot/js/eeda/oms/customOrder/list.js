define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	  document.title = '报关单查询   | '+document.title;
      $('#menu_custom').addClass('active').find('ul').addClass('in');
  	  //datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
          paging: true,
          serverSide: true, //不打开会出现排序不对
          ajax: "/customOrder/customOrderlist",
          columns: [
              { "data": "ORDER_NO", width: '10%',
                  "render": function ( data, type, full, meta ) {
                      return "<a href='/customOrder/edit?id="+full.ID+"&order_no="+data+" 'target='_blank'>"+data+"</a>";
                  }
              },
              { "data": "CUSTOM_ORDER_NO", width: '10%',}, 
              { "data": "CUSTOM_TYPE", width: '10%',
            	  "render": function ( data, type, full, meta ) {
                      var str = '';
                      if(data == 'china'){
                        str = '国内报关'; 
                      }else if(data == 'HK/MAC'){
                        str = '香港澳门报关';
                      }else if(data == 'abroad'){
                        str = '海外报关';
                      }
                      return str;
                    }
              }, 
              { "data": "CUSTOMER_NAME", width: '10%',},
              { "data": "STATUS",
            	  "render": function ( data, type, full, meta ) {
                      var str = '';
                      if(data == 'declareSuccess'){
                        str = '申报成功'; 
                      }else if(data == 'declareSuccess,onDataBaseRoad'){
                        str = '发往海关数据库';
                      }else if(data == 'declareSuccess,onDataBaseRoad,gateInSuccess'){
                        str = '入库成功';
                      }else if(data == 'declareSuccess,onDataBaseRoad,gateInSuccess,customOrderCheck'){
                        str = '报关单查验';
                      }else if(data == 'declareSuccess,onDataBaseRoad,gateInSuccess,customOrderCheck,release'){
                        str = '放行'; 
                      }else if(data == 'declareSuccess,onDataBaseRoad,gateInSuccess,customOrderCheck,release,conclusion'){
                        str = '审结';
                      }
                      return str;
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
          var order_no = $.trim($("#order_no").val()); 
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/customOrder/customOrderlist?order_no="+order_no
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
      };
  });

});