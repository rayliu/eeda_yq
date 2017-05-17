define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn',  'dtColReorder'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '客户合同 | '+document.title;
        $("#breadcrumb_li").text('客户合同');

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            colReorder: true,
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/customerContract/list",
            columns:[
                { "data": "CONTRACT_NO","width": "10%",
                    "render": function ( data, type, full, meta ) {
                        return "<a href='/customerContract/edit?id="+full.ID+"' target='_blank'>" + data+ "</a>";
                    }
                },
                { "data": "TYPE", "width": "10%"}, 
                { "data": "CUSTOMER_NAME", "width": "10%"}, 
                { "data": "CONTRACT_PERIOD", "width": "15%"}, 
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
                        
                            
                            str += "<a class='btn table_btn btn-danger btn-sm' href='/supplierContract/delete/"+full.ID+"'>"+
                                 "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                 "删除"+
                                 "</a>";
                         
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
          	var COMPANY_NAME = $("#COMPANY_NAME").val().trim();
          	
          	var url = "/customerContract/list?customer_name_like="+COMPANY_NAME+"&contact_person_like="+CONTACT_PERSON+"&code_like="+code+"&abbr_like="+ABBR+"&address_like="+ADDRESS;
          	dataTable.ajax.url(url).load();
        });

    });
});