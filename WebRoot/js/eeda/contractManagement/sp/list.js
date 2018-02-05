define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn',  'dtColReorder'], function ($, metisMenu) { 

    $(document).ready(function() {

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            colReorder: true,
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/supplierContract/list",
            columns:[
                {
                	"width": "5%",
				    "render": function ( data, type, full, meta ) {
				        return '<a type="button"  class="copy btn table_btn delete_btn btn-xs" href="/supplierContract/copyJobOrder?id='+full.ID+'"> 复制</a>';
				    }
                },
                { "data": "CONTRACT_NO","width": "10%",
                    "render": function ( data, type, full, meta ) {
                        return "<a href='/supplierContract/edit?id="+full.ID+"' target='_blank'>" + data+ "</a>";
                    }
                },
                { "data": "CUSTOMER_NAME", "width": "10%"}, 
                { "data": "CONTRACT_PERIOD", "width": "10%"}, 
                { "data": "CREATOR_NAME", "width": "10%"}, 
                { "data": "CREATE_DATE", "width": "10%"},
                { "data": "STATUS" ,"width": "5%"},
                { 
                    "data": null, 
                    "width": "5%",
                    "render": function(data, type, full, meta) {
                         var str ="<nobr>";
                         //if(Provider.isUpdate){
                          str += "<a class='btn table_btn btn-success btn-sm' href='/supplierContract/edit?id="+full.ID+"' target='_blank'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "编辑"+"</a> ";
                         //}
                        
                          if(full.IS_STOP != "Y"){
                              str = str +"<a class='btn table_btn  btn-danger btn-sm ' href='/supplierContract/delete/"+full.ID+"'>"+
                                           "<i class='fa fa-trash-o fa-edit'></i>停用</a>";
                          }else{
                              str = str + "<a class='btn table_btn  btn_green btn-xs dropdown-toggle' href='/supplierContract/delete/"+full.ID+"'>"+
                                           "<i class='fa fa-trash-o fa-edit'></i>启用 </a>";
                          }
                         
                         return str +="</nobr>";
                    }
                }
            ]
        });

        //base on config hide cols
        eeda.showCols(dataTable, cols_config);
      //清空查询条件
    	$('#resetBtn').click(function(e){
            $("#orderForm")[0].reset();
        });
    	
        //条件筛选
    	$("#searchBtn").on('click', function () {    	 	
          	var customer_id = $("#customer_id").val();
          	var contract_no = $("#contract_no").val();
            var start_date = $("#create_date_begin_time").val();
            var end_date = $("#create_date_end_time").val();
          	
          	var url = "/supplierContract/list?customer_id="+customer_id
          	+"&contract_no="+contract_no
          	+"&create_date_begin_time="+start_date
          	+"&create_date_end_time="+end_date;
          	dataTable.ajax.url(url).load();
    	});
    });
});