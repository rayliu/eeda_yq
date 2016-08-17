
$(document).ready(function() {

    $('#menu_finance').addClass('active').find('ul').addClass('in');
    var type = $("#type").val();//注意这里
    var urlSource1;
    var urlSource2;
    var urlSource3;
	if(type=='CUSTOMER'){
		
		urlSource1="/chargeCheckOrder/list";
		urlSource2="/transferOrder/edit/";
		urlSource3="/transferOrder/delete/";
	}else{	
		urlSource1="/paymentCheckOrder/list";
		urlSource2="/transferOrder/edit/";
		urlSource3="/transferOrder/delete/";
	}
	//datatable, 动态处理
    $('#eeda-table').dataTable({
        //"sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": urlSource1,
        "aoColumns": [   
            {"mDataProp":"ID", "bVisible": false},
            {"mDataProp":"BLLING_ORDER_NO"},
            {"mDataProp":"COMPANY_NAME"},
            {"mDataProp":"CUSTOMER_TYPE",
                "fnRender": function(obj) {
                    if(obj.aData.CUSTOMER_TYPE=='CUSTOMER'){
                        return'客户';
                    }else {
                        return '供应商';
                    }
                }
            },
            {"mDataProp":"STATUS",
                "fnRender": function(obj) {
                    if(obj.aData.STATUS=='new'){
                        return '新建';
                    }else if(obj.aData.STATUS=='checking'){
                        return '已发送对帐';
                    }else if(obj.aData.STATUS=='confirmed'){
                        return '已审核';
                    }else if(obj.aData.STATUS=='completed'){
                        return '已结算';
                    }else if(obj.aData.STATUS=='cancel'){
                        return '取消';
                    }
                    return obj.aData.STATUS;
                }
            },
            {"mDataProp":"TRANSFER_ORDER_NO"},
            {"mDataProp":"TRANSFER_ORDER_NO"},
            {"mDataProp":"DELIVERY_ORDER_NO"},            
            {"mDataProp":"CREATOR_NAME"},        	
            {"mDataProp":"CREATE_STAMP"},
            {"mDataProp":"REMARK"},
            { 
                "mDataProp": null, 
            	//"mDataProp": "CARGO_NATURE", 
                "sWidth": "8%",                
                "fnRender": function(obj) {
                	/*if(obj.aData.CARGO_NATURE=='cargo'){
                		obj.aData.CARGO_NATURE = '普通货品';
                	}else if(obj.aData.CARGO_NATURE=='damageCargo'){
                		return '损坏货品';
                	}*/
                    return	"<a class='btn btn-success' href='"+urlSource2+""+obj.aData.ID+"'>"+
                                "<i class='fa fa-edit fa-fw'></i>"+
                                "编辑"+
                            "</a>"+
                            "<a class='btn btn-danger' href='"+urlSource3+""+obj.aData.ID+"'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                "删除"+
                            "</a>";
                }
            }                         
        ]      
    });	
} );