define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'order_table',
          paging: true,
          serverSide: false, //不打开会出现排序不对 
          ajax: "/costCheckOrder/orderList",
          columns: [
            { "data": "ORDER_NO", "width": "100px", 
              "render": function ( data, type, full, meta ) {
                return "<a href='/costCheckOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
              }
            },
            { "data": "CREATE_STAMP", "width": "100px"},
            { "data": "BILL_FLAG", "width": "60px",
            	"render": function ( data, type, full, meta ) {
    			    	if(data != 'Y')
    			    		return '未创建对账单';
    			    	else 
    			    		return '已创建对账单';
    			    }
            },
            { "data": null, "width": "60px"},
            { "data": "TYPE", "width": "60px"},
            { "data": "SP_NAME", "width": "100px"},
            { "data": "TOTAL_COSTRMB", "width": "60px"
            },
            { "data": null, "width": "60px"},
            { "data": null, "width": "60px"},
            { "data": null, "width": "60px"},
            
            { "data": "FND", "width": "60px",
            	"render": function ( data, type, full, meta ) {
                if(data)
    			    		return data;
    			    	else 
    			    		return full.DESTINATION;
    			    }
            },
            { "data": "VOLUME", "width": "60px"},
            { "data": "CONTAINER_AMOUNT","width": "60px",
            	"render": function ( data, type, full, meta ) {
	            	if(data){
	            		var dataArr = data.split(",");
	            		var a = 0;
	            		var b = 0;
	            		var c = 0;
	            		var dataStr = "";
	            		for(var i=0;i<dataArr.length;i++){
	            			if(dataArr[i]=="20GP"){
	            				a++;
	            			}
	            			if(dataArr[i]=="40GP"){
	            				b++;
	            			}
	            			if(dataArr[i]=="45GP"){
	            				c++;
	            			}
	            		}
	            		if(a>0){
	            			dataStr+="20GPx"+a+";"
	            		}
	            		if(b>0){
	            			dataStr+="40GPx"+b+";"
	            		}
	            		if(c>0){
	            			dataStr+="45GPx"+c+";"
	            		}
	            		return dataStr;
	            	}else{
	            		return '';
	            	}
            	}
            },
            { "data": "NET_WEIGHT", "width": "60px"},
            { "data": null, "width": "60px"},
            
            { "data": "MBL_NO", "width": "60px"},
            { "data": "CONTAINER_NO", "width": "100px"},
          ]
      });

      
      $('#resetBtn').click(function(e){
          $("#orderSearchForm")[0].reset();
      });

      $('#searchOrderBtn').click(function(){
          searchOrderData(); 
      })

     var searchOrderData=function(){
          var order_no = $.trim($("#order_no").val()); 
          var customer = $("#customer").val(); 
          var sp = $("#sp").val(); 
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/costCheckOrder/orderList?order_no="+order_no
			            +"&customer_id="+customer
			            +"&sp_id="+sp
		              +"&create_stamp_begin_time="+start_date
		              +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
      };


  });   
});