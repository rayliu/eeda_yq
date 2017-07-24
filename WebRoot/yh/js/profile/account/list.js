$(document).ready(function() {
	document.title = '金融账户查询 | '+document.title;
	$('#menu_profile').addClass('active').find('ul').addClass('in');
	$('#example').dataTable( {
		 "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
	        //"sPaginationType": "bootstrap",
	        "iDisplayLength": 10,
	    	"oLanguage": {
	            "sUrl": "/eeda/dataTables.ch.txt"
	        },
	        "sAjaxSource": "/account/listAccount",
			"aoColumns": [
				{ "mDataProp": "BANK_NAME","sWidth": "12%",
					"fnRender": function(obj) {  
						if(Account.isUpdate){
							return "<a href='/account/edit/"+obj.aData.ID+"'target='_blank'>"+
			                            obj.aData.BANK_NAME+
			                        "</a>";
						}else{
							return obj.aData.BANK_NAME;
						}
	                    
	                }
	            },
	            { "mDataProp": "TYPE","sWidth": "10%",
	            	"fnRender": function(obj) {
	            		var displayName='';
	            	    if(obj.aData.TYPE ==='REC')
	            	    	displayName='收款';
	            	    if(obj.aData.TYPE ==='PAY')
	            	    	displayName='付款';
	            	    if(obj.aData.TYPE ==='ALL')
	            	    	displayName='收款付款';
	                    return displayName;
	                }
	        	},
	            { "mDataProp": "ACCOUNT_NO","sWidth": "9%" },
	            { "mDataProp": "BANK_PERSON","sWidth": "9%" },
	            { "mDataProp": "CURRENCY","sWidth": "9%" },
	            { "mDataProp": "AMOUNT","sWidth": "9%" },
	           	{ "mDataProp": "REMARK" },
	            { 
	                "mDataProp": null, 
	                "sWidth": "11%",
	                "bVisible":(Account.isUpdate || Account.isDel),
	                "fnRender": function(obj) { 
	                	var str = "<nobr>";
	                	if(Account.isUpdate){
	                		str += "<a class='btn  btn-primary btn-sm' href='/account/edit/"+obj.aData.ID+"' target='_blank' ><i class='fa fa-edit fa-fw'></i>"+
		                            "编辑"+
			                        "</a> ";
	                	}
	                	if(Account.isDel){
	                		if(obj.aData.IS_STOP != true){
		                		str +="<a class='btn btn-danger  btn-sm' href='/account/del/"+obj.aData.ID+"'>"+
				                            "<i class='fa fa-trash-o fa-fw'></i>"+ 
				                            "停用"+
				                        "</a>";
		                	}else{
		                		str += "<a class='btn btn-success  btn-sm' href='/account/del/"+obj.aData.ID+"'>"+
				                            "<i class='fa fa-trash-o fa-fw'></i>"+ 
				                            "启用"+
				                        "</a>";
		                	}
		                    
	                	}
	                	str += "<nobr>";
	                	return str;
	                }
	            } 
	            ]
	} );
});