define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '供应商查询 | '+document.title;
    	$('#menu_profile').addClass('active').find('ul').addClass('in');

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            ajax: "/serviceProvider/list",
            columns:[
                { "data": "COMPANY_NAME","width": "15%",
                    "render": function ( data, type, full, meta ) {
                        if(Provider.isUpdate){
                             return "<a href='/serviceProvider/edit?id="+full.ID+"' target='_blank'>" + data+ "</a>";
                        }else{
                             return data;
                        }
                    }
                },
                { "data": "ABBR", "width": "10%",}, 
                { "data": "CODE", "width": "10%",}, 
                { "data": "SP_TYPE", "width": "15%",
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
                             }
                         });
                         
                         return str;
                    }
                }, 
                { "data": "CONTACT_PERSON"}, 
                { "data": "PHONE"}, 
                { "data": "ADDRESS", "width": "15%"},
                { "data": "RECEIPT"},
                { "data": "PAYMENT",
                    "render": function(data, type, full, meta) {
                         if(data == "monthlyStatement"){
                             return "月结";
                         }else if(data == "freightCollect"){
                             return "到付";
                         }else{
                             return "现付";
                         }}},
                { "data":null,
                    "render": function(data, type, full, meta) {
                         if(full.DNAME == null){
                             return full.NAME;
                         }else{
                             return full.DNAME;
                         }
                     }
                },
                { 
                    "data": null, 
                    //"width": "8%",  
                    "visible":(Provider.isUpdate || Provider.isDel),
                    "render": function(data, type, full, meta) {
                         var str ="<nobr>";
                         if(Provider.isUpdate){
                          str += "<a class='btn  btn-primary btn-sm' href='/serviceProvider/edit?id="+full.ID+"' target='_blank'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "编辑"+"</a> ";
                         }
                        if(Provider.isDel){
                            if(full.IS_STOP != true){
                                str += "<a class='btn btn-danger btn-sm' href='/serviceProvider/delete/"+full.ID+"'>"+
                                     "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                     "停用"+
                                     "</a>";
                             }else{
                                str +="<a class='btn btn-success btn-sm' href='/serviceProvider/delete/"+full.ID+"'>"+
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
          	var ADDRESS = $("#ADDRESS").val().trim();
          	var LOCATION = $("#LOCATION").val().trim();
          	var url = "/serviceProvider/list?COMPANY_NAME="+COMPANY_NAME+"&CONTACT_PERSON="+CONTACT_PERSON+"&code="+code+"&ABBR="+ABBR+"&ADDRESS="+ADDRESS+"&LOCATION="+LOCATION;
          	dataTable.ajax.url(url).load();
        });

    });
});