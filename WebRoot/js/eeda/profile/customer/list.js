define(['jquery', 'metisMenu', 'sb_admin', 'dataTablesBootstrap', 'validate_cn'], function ($, metisMenu) {

    $(document).ready(function() {
    	document.title = '客户查询 | '+document.title;
    	$('#menu_profile').addClass('active').find('ul').addClass('in');

        var dataTable = $('#eeda-table').DataTable({
            "processing": true,
            "searching": false,
            //"serverSide": true,
            "scrollX": true,
            "sort":true,
            //"scrollY": "300px",
            "scrollCollapse": true,
            "autoWidth": false,
            "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
            "language": {
                "url": "/js/lib/datatables/i18n/Chinese.json"
            },
            "ajax": "/customer/list",
            "columns": [
                { "data": "COMPANY_NAME","width": "15%",
                    "render": function ( data, type, full, meta ) {
                        if(Customer.updatePermission){
                             return "<a href='/customer/edit/"+full.PID+"'target='_blank'>" + data + "</a>";
                         }else{
                             return data;
                         }
                    }
                },
                { "data": "ABBR"}, 
                { "data": "CONTACT_PERSON"}, 
                { "data": "PHONE"}, 
                { "data": "ADDRESS","width": "15%"}, 
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
                },
                { "data": null,"width": "25px",
                    "render": function ( data, type, full, meta ) {
                        if(full.DNAME == null){
                            return full.NAME;
                        }else{
                            return full.DNAME;
                        }
                    }
                },
                { "data": null,
                    "width": "10%",
                    "render": function ( data, type, full, meta ) {
                            var str="<nobr>";
                        if(Customer.updatePermission){
                         str +="<a class='btn  btn-primary btn-sm' href='/customer/edit/"+full.PID+"' target='_blank'>"+
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
                }
            ]
        });
    	
        
      //条件筛选
    	$("#searchBtn").on('click', function () {    	 	
          	var COMPANY_NAME = $("#COMPANY_NAME").val();
          	var CONTACT_PERSON = $("#CONTACT_PERSON").val();
        	  // var RECEIPT = $("#RECEIPT").val();
          	var ABBR = $("#ABBR").val();    	
          	var ADDRESS = $("#ADDRESS").val();
          	var LOCATION = $("#LOCATION").val();
          	var url= "/customer/list?COMPANY_NAME="+COMPANY_NAME+"&CONTACT_PERSON="+CONTACT_PERSON+"&ABBR="+ABBR+"&ADDRESS="+ADDRESS+"&LOCATION="+LOCATION;
          	dataTable.ajax.url(url).load();
          });
    });
});