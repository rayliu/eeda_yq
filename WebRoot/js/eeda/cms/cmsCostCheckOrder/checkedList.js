define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','sco','validate_cn'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '应收对账单查询 | '+document.title;


    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-tableChecked',
            paging: true,
            scrollX:false,
            serverSide: true, //不打开会出现排序不对
            ajax: "/cmsCostCheckOrder/checkedList",
            columns:[	                
				{ "data": "ORDER_NO", "width": "100px",
					 "render": function ( data, type, full, meta ) {
						  return "<a href='/cmsCostCheckOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
					  }
				},
				
				{ "data": "STATUS", "width": "100px"},
				{ "data": "PARTY_NAME", "width": "80px"}, 
				{ "data": "CHECK_AMOUNT","width": "80px",
				"render":function(data, type, full, meta){
					 if(data<0){
						 return '<span style="color:red">'+data+'</span>';
					 }else{
						 return data;
					 }
				}
				},
				{ "data": "CREATOR_NAME", "width": "80px"}, 
				{ "data": "CREATE_STAMP", "width": "80px"}, 
				{ "data": "CONFIRM_NAME", "width": "80px"}, 
				{ "data": "CONFIRM_STAMP", "width": "80px"}
            ]
        });
   	           
      $('#resetBtn1').click(function(e){
          $("#orderSearchForm")[0].reset();
      });

      $('#searchBtn1').click(function(){
          searchData(); 
      });

     var searchData=function(){
          var order_no = $("#order_no1").val();
          var sp_name = $('#sp1_input').val().trim();
          var start_date = $("#create_stamp1_begin_time").val();
          var end_date = $("#create_stamp1_end_time").val();
          
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/cmsCostCheckOrder/checkedList?order_no="+order_no
               +"&party_name="+sp_name
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
       }
       
    });
});