define(['jquery', 'metisMenu', 'sb_admin', 'dataTablesBootstrap', 'validate_cn',  'dtColReorder'], function ($, metisMenu) {

    $(document).ready(function() {
    	document.title = '客户查询 | '+document.title;
        $("#breadcrumb_li").text('客户列表');

    	$('#menu_profile').addClass('active').find('ul').addClass('in');

        var dataTable = eeda.dt({
            id: 'eeda-table',
            colReorder: true,
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/customer/list",
            columns: [
                { "data": null,
                    "width": "10%",
                    "render": function ( data, type, full, meta ) {
                            var str="<nobr>";
                        if(Customer.updatePermission){
                         str +="<a class='btn  btn-primary btn-sm' href='/customer/edit?id="+full.PID+"' target='_blank'>"+
                                    "<i class='fa fa-edit fa-fw'></i>"+
                                    "编辑"+"</a> ";
                        }
                        if(Customer.delPermission){
                             if(full.IS_STOP != true){
                                     str += "<a class='btn btn-danger  btn-sm' href='/customer/delete/"+full.PID+"'>"+
                                             "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                             "停用"+
                                             "</a>";
                             }else{
                                 str +="<a class='btn btn-success' href='/customer/delete/"+full.PID+"'>"+
                                         "<i class='fa fa-trash-o fa-fw'></i>启用</a>";
                             }
                        }
                        str +="</nobr>";
                       return str;
                    }
                },
                { "data": "COMPANY_NAME","width": "15%",
                    "render": function ( data, type, full, meta ) {
                        if(Customer.updatePermission){
                             return "<a href='/customer/edit?id="+full.PID+"'target='_blank'>" + data + "</a>";
                         }else{
                             return data;
                         }
                    }
                },
                { "data": "ABBR"}, 
                { "data": "CODE"}, 
                { "data": "COMPANY_TYPE"}, 
                { "data": "CONTACT_PERSON"}, 
                { "data": "PHONE"}, 
                { "data": "ADDRESS",
                    "render": function ( data, type, full, meta ) {
                        if(data){
                            return data;
                        }else{
                            return full.ADDRESS_ENG;
                        }
                    }
                },
                { "data": "PAYMENT","width": "25px",
                    "render": function ( data, type, full, meta ) {
                        if(data == "monthlyStatement"){
                             return "月结";
                         }else if(data == "freightCollect"){
                             return "到付";
                         }else{
                             return "现付";
                         }
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
      //条件筛选
    	$("#searchBtn").on('click', function () {    	 	
          	var COMPANY_NAME = $.trim($("#COMPANY_NAME").val());
          	var ABBR = $.trim($("#ABBR").val());	
          	var ADDRESS = $.trim($("#ADDRESS").val());
          	var CONTACT_PERSON = $.trim($("#CONTACT_PERSON").val());
          	var code = $.trim($("#code").val());
          	
          	var url= "/customer/list?company_name_like="+COMPANY_NAME+"&contact_person_like="+CONTACT_PERSON+"&abbr_like="+ABBR
          		+"&address_like="+ADDRESS+"&code_like="+code;
          	dataTable.ajax.url(url).load();
          });
    	
    	//清空查询条件
    	$('#resetBtn').click(function(e){
            $("#orderForm")[0].reset();
        });
    	
    	
    });
});