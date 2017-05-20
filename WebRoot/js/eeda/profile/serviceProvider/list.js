define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn',  'dtColReorder'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '供应商基本信息列表| '+document.title;
    	$('#menu_profile').addClass('active').find('ul').addClass('in');
        $("#breadcrumb_li").text('供应商基本信息列表');

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            colReorder: true,
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/serviceProvider/list",
            columns:[
                { "data": "COMPANY_NAME","width": "10%",
                    "render": function ( data, type, full, meta ) {
                        if(Provider.isUpdate){
                             return "<a href='/serviceProvider/edit?id="+full.ID+"' target='_blank'>" + data+ "</a>";
                        }else{
                             return data;
                        }
                    }
                },
                { "data": "STATUS", "width": "10%"}, 
                { "data": "ABBR", "width": "10%"}, 
                { "data": "CODE", "width": "10%"}, 
                { "data": "SP_TYPE", "width": "10%",
                    "render": function(data, type, full, meta) {
                         var str = "";
                         if(data == null)
                            return '';
                        
                         typeArr = data.split(";");
                         
                         $.each(typeArr, function(index, val) {
                        	 //line;delivery;pickup;personal;carrier;air;broker;head_car;oversea_agent
                             if(val == "line"){
                                 str += "干线运输供应商<br>";
                             }else if(val == "delivery"){
                                 str += "配送供应商<br>";
                             }else if(val == "pickup"){
                                 str += "提货供应商<br>";
                             }else if(val == "personal"){
                                 str += "个体供应商<br>";
                             }else if(val == "carrier"){
	                        	 str += "船公司<br>";
	                         }else if(val == "air"){
	                        	 str += "航空公司<br>";
	                         }else if(val == "broker"){
	                        	 str += "报关行<br>";
	                         }else if(val == "head_car"){
	                        	 str += "头程船公司<br>";
	                         }else if(val == "oversea_agent"){
	                        	 str += "海外代理<br>";
	                         }else if(val == "booking_agent"){
                                 str += "订舱代理<br>";
                             }else if(val == "truck"){
                                 str += "运输公司<br>";
                             }else if(val == "cargo_agent"){
                                 str += "货代公司<br>";
                             }else if(val == "manufacturer"){
                                 str += "生产商<br>";
                             }else if(val == "traders"){
                                 str += "贸易商<br>";
                             }
                         });
                         
                         return str;
                    }
                }, 
                { "data": "CONTACT_PERSON", "width": "10%"}, 
                { "data": "PHONE", "width": "10%"}, 
                { "data": "ADDRESS", "width": "20%",
                    "render": function ( data, type, full, meta ) {
                        if(data){
                            return data;
                        }else{
                            return full.ADDRESS_ENG;
                        }
                    }
                },
                { "data": "RECEIPT" ,"width": "5%"},
                { "data": "PAYMENT", "width": "10%",
                    "render": function(data, type, full, meta) {
                         if(data == "monthlyStatement"){
                             return "月结";
                         }else if(data == "freightCollect"){
                             return "到付";
                         }else{
                             return "现付";
                         }
                    }
                },
                { 
                    "data": null, 
                    "width": "5%",
                    "visible":(Provider.isUpdate || Provider.isDel),
                    "render": function(data, type, full, meta) {
                         var str ="<nobr>";
                         if(Provider.isUpdate){
                          str += "<a class='btn table_btn btn-success btn-sm' href='/serviceProvider/edit?id="+full.ID+"' target='_blank'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "编辑"+"</a> ";
                         }
                        if(Provider.isDel){
                            if(full.IS_STOP != true){
                                str += "<a class='btn table_btn btn-danger btn-sm' href='/serviceProvider/delete/"+full.ID+"'>"+
                                     "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                     "停用"+
                                     "</a>";
                             }else{
                                str +="<a class='btn table_btn btn-success btn-sm' href='/serviceProvider/delete/"+full.ID+"'>"+
                                         "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                         "启用"+
                                     "</a>";
                             }
                         }
                         return str +="</nobr>";
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
      //清空查询条件
    	$('#resetBtn').click(function(e){
            $("#orderForm")[0].reset();
        });
    	
        //条件筛选
    	$("#searchBtn").on('click', function () {    	 	
          	var COMPANY_NAME = $("#COMPANY_NAME").val().trim();
          	var CONTACT_PERSON = $("#CONTACT_PERSON").val().trim();
        	var code = $("#code").val().trim();
          	var ABBR = $("#ABBR").val().trim(); 
          	var status = $("#status").val().trim();
          	var ADDRESS = $("#ADDRESS").val().trim();
          	var url = "/serviceProvider/list?company_name_like="+COMPANY_NAME+"&contact_person_like="+CONTACT_PERSON
          	+"&code_like="+code+"&abbr_like="+ABBR+"&address_like="+ADDRESS+"&status="+status;
          	dataTable.ajax.url(url).load();
        });

    });
});