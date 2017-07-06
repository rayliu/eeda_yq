define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {

  	  
      var dataTable = eeda.dt({
          id: 'eeda_table',
          colReorder: true,
          paging: true,
          serverSide: true, //不打开会出现排序不对 
          ajax: "/billProfitAndPayment/list",
          columns: [
                {"data": "ORDER_NO", 
              	  "render": function ( data, type, full, meta ) {
              		  return "<a href='/jobOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
              	  }
                },
      			{ "data": "ABBR", "width": "120px"},
      			{ "data": "ORDER_EXPORT_DATE", "width": "120px"},
	            { "data": "CHARGE_RMB", "width": "120px"},
	            { "data": "COST_RMB", "width": "120px"  },
	            {
					"render": function(data, type, full, meta) {
						var str = parseFloat(full.CHARGE_RMB - full.COST_RMB).toFixed(2);
						if(str<0){
							return '<span style="color:red" >'+str+'</span>';
						}
						return str;
					}
				},
	            { "data": "CHARGE_TOTAL", "width": "120px",
	            	"render": function(data, type, full, meta) {
	            		var str="";
	            		if(full.COST_RMB!=0){
	            			str = parseFloat(((full.CHARGE_RMB - full.COST_RMB)/full.COST_RMB)*100).toFixed(2);
	            		}
						return str
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
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });
      
      $('#checkboxNegative').click(function(){
    	  searchData(); 
    	  
    	  
      });
      

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
    	  var checked = '';
    	  if($('#checkboxNegative').prop('checked')==true){
    		  checked = 'Y';
    		  }
          var customer = $("#customer").val(); 
          var order_export_date_begin_time = $("#order_export_date_begin_time").val();
          var order_export_date_end_time = $("#order_export_date_end_time").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          
          
          var url = "/billProfitAndPayment/list?checked="+checked
          				  +"&customer_id="+customer
				          +"&order_export_date_begin_time="+order_export_date_begin_time
				          +"&order_export_date_end_time="+order_export_date_end_time;
          dataTable.ajax.url(url).load();
          
          
      };
  });
});