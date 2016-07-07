define(['jquery', 'metisMenu', 'sb_admin', 'dataTablesBootstrap', 'validate_cn'], function ($, metisMenu) {

    $(document).ready(function() {
    	document.title = '客户查询 | '+document.title;
    	$('#menu_profile').addClass('active').find('ul').addClass('in');

        var dataTable = $('#eeda-table').DataTable({
            "processing": true,
            "searching": false,
            //"serverSide": true,
            "scrollX": true,
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
    	//datatable, 动态处理
       // var dataTable= $('#eeda-table').dataTable({
    	  //  "bFilter": false, //不需要默认的搜索框 
    	  //  //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
       //      "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
       //      //"sPaginationType": "bootstrap",
       //      "iDisplayLength": 10,
       //      "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
       //  	"oLanguage": {
       //          "sUrl": "/eeda/dataTables.ch.txt"
       //      },
       //      "bProcessing": true,
       //      "bServerSide": true,
       //      "sAjaxSource": "/customer/list",
       //      "aoColumns": [   
       //          {"mDataProp":"COMPANY_NAME",
       //          	"fnRender":function(obj){
       //          		if(Customer.updatePermission){
       //          			return "<a href='/customer/edit/"+obj.aData.PID+"'target='_blank'>" + obj.aData.COMPANY_NAME + "</a>";
       //          		}else{
       //          			return obj.aData.COMPANY_NAME;
       //          		}
                		
       //          	}},
       //          {"mDataProp":"ABBR"},
       //          {"mDataProp":"CONTACT_PERSON"},        	
       //          {"mDataProp":"PHONE"},
       //          {"mDataProp":"ADDRESS","sWidth": "15%"},
       //          {"mDataProp":"RECEIPT"},
       //          {"mDataProp":"PAYMENT",
       //          	"fnRender": function(obj) {
       //          		if(obj.aData.PAYMENT == "monthlyStatement"){
       //          			return "月结";
       //          		}else if(obj.aData.PAYMENT == "freightCollect"){
       //          			return "到付";
       //          		}else{
       //          			return "现付";
       //          		}}},
       //          {"mDataProp":null,
       //          	"fnRender": function(obj) {
    	  //           		if(obj.aData.DNAME == null){
    	  //           			return obj.aData.NAME;
    	  //           		}else{
    	  //           			return obj.aData.DNAME;
    	  //           		}}
       //          },
       //          { 
       //              "mDataProp": null, 
       //              "sWidth": "11%",  
       //              "bVisible":(Customer.updatePermission || Customer.delPermission),
       //              "fnRender": function(obj) {  
       //              	var str="<nobr>";
       //              	if(Customer.updatePermission){
       //          			str +="<a class='btn  btn-primary btn-sm' href='/customer/edit/"+obj.aData.PID+"' target='_blank'>"+
    	  //                           "<i class='fa fa-edit fa-fw'></i>"+
    	  //                           "编辑"+"</a> ";
       //          		}
       //              	if(Customer.delPermission){
    	  //               	if(obj.aData.IS_STOP != true){
    	  //               			str += "<a class='btn btn-danger  btn-sm' href='/customer/delete/"+obj.aData.PID+"'>"+
    			//                          "<i class='fa fa-trash-o fa-fw'></i>"+ 
    			//                          "停用"+
    			//                          "</a>";
    	  //               	}else{
    	  //               		str +="<a class='btn btn-success' href='/customer/delete/"+obj.aData.PID+"'>"+
    		 //                         "<i class='fa fa-trash-o fa-fw'></i>启用</a>";
    	  //               	}
       //              	}
       //              	str +="</nobr>";
       //                 return str;
       //              }
       //          }                         
       //      ]    
       //  });
        
      //条件筛选
    	$("#COMPANY_NAME,#CONTACT_PERSON ,#RECEIPT,#ABBR,#ADDRESS,#LOCATION").on('keyup click', function () {    	 	
          	var COMPANY_NAME = $("#COMPANY_NAME").val();
          	var CONTACT_PERSON = $("#CONTACT_PERSON").val();
        	var RECEIPT = $("#RECEIPT").val();
          	var ABBR = $("#ABBR").val();    	
          	var ADDRESS = $("#ADDRESS").val();
          	var LOCATION = $("#LOCATION").val();
          	dataTable.fnSettings().sAjaxSource = "/customer/list?COMPANY_NAME="+COMPANY_NAME+"&CONTACT_PERSON="+CONTACT_PERSON+"&RECEIPT="+RECEIPT+"&ABBR="+ABBR+"&ADDRESS="+ADDRESS+"&LOCATION="+LOCATION;
          	dataTable.fnDraw();
          });
    });
});