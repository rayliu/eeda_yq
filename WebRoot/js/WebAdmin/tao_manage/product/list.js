define(['jquery', 'metisMenu',  'dataTablesBootstrap'], function ($, metisMenu) { 
	$(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/tao_manage/product/list",
            columns:[
                     {"data":"PRODUCTOR","width": "80px" },
                     {"data": "NAME", "width":"120px"},
                     {"data": "COVER", "width":"150px",
                    	 "render":function(data){
                    		 return "<img src='/upload/"+data+"' width='120' height='90'/>"
                    	 }
                     }, 
                     {"data": "PRICE", "width":"60px"},
                     {"data": "IS_ACTIVE", "width":"60px",
                    	"render":function(data,full){
                    		if(data=="Y"){
                    			data="已上架";
                    		}else{
                    			data="未上架"
                    		}
                    		return data;
                    	}
                     },
                     {"data": "ID", "width":"60px",
                    	 "render":function(data,full){
                    		 return "<a href='/WebAdmin/tao_manage/product/detail?id="+data+"'>操作1</a>";
                    	 }
                     }
                    ]
        });
      
      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
          var creator = $.trim($("#creator").val()); 
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();

          var url = "/msgBoard/list?create_name_like="+creator
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
      };
      
      $('#eeda_table').on('click','.edit',function(){

      });
    	
});
});