define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'jq_blockui'], function ($, metisMenu, template) { 
	$(document).ready(function() {
	    var deletedTableIds=[];

	    //删除一行
	    $("#custom_table").on('click', '.delete', function(e){
	        e.preventDefault();
	        var tr = $(this).parent().parent();
	        deletedTableIds.push(tr.attr('id'));
	        
	        cargoTable.row(tr).remove().draw();
	    }); 
	    
	    itemOrder.buildCustomDetail=function(){
	    	var cargo_table_rows = $("#custom_table tr");
	        var cargo_items_array=[];
	        for(var index=0; index<cargo_table_rows.length; index++){
	            if(index==0)
	                continue;

	            var row = cargo_table_rows[index];
	            var empty = $(row).find('.dataTables_empty').text();
	            if(empty)
	            	continue;
	            
	            var id = $(row).attr('id');
	            if(!id){
	                id='';
	            }
	            
	            var item={}
	            
	            item.id = id;
	           
	            for(var i = 1; i < row.childNodes.length; i++){
	            	var name = $(row.childNodes[i]).find('input,select').attr('name');
	            	var value = $(row.childNodes[i]).find('input,select').val();
	            	if(name){
	            		item[name] = value;
	            	}
	            }
	            item.action = id.length > 0?'UPDATE':'CREATE';
	            if(!id.length>0){
	            	var d = new Date();
	            	var str = d.getFullYear()+"-"+(d.getMonth()+1)+"-"+d.getDate()+' '+d.getHours()+':'+d.getMinutes()+':'+d.getSeconds();
	            	item.create_stamp = str;
	            }
	            cargo_items_array.push(item);
	        }

	        //add deleted items
	        for(var index=0; index<deletedTableIds.length; index++){
	            var id = deletedTableIds[index];
	            var item={
	                id: id,
	                action: 'DELETE'
	            };
	            cargo_items_array.push(item);
	        }
	        deletedTableIds = [];
	        return cargo_items_array;
	    };
	    
	    var bindFieldEvent=function(){
	    	eeda.bindTableField('custom_table','DOCK_FEE_REIMBURSEMENT_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('custom_table','INSPECTION_QUARANTINE_REIMBURSEMENT_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('custom_table','CUSTOMS_ENTRY_FEE_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('custom_table','INSPECTION_ENTRY_FEE_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('custom_table','CUSTOMS_AGENT_FEE_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('custom_table','TOTAL_FEE_CRC','/serviceProvider/searchCurrency','');
	    };


	    //------------事件处理
	    var cargoTable = eeda.dt({
            id: 'custom_table',
            autoWidth: false,
            "drawCallback": function( settings ) {
		        bindFieldEvent();
		        $.unblockUI();
		    },
            columns:[
				{ "width": "30px",
				    "render": function ( data, type, full, meta ) {
				    	return '<button type="button" class="delete btn table_btn delete_btn btn-xs">'+
				        '<i class="fa fa-trash-o"></i> 删除</button>';
				    }
				},
	            { "data": "REGION", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="region"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "SERVICE_PORT", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="service_port"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "SERVICE_CONTENT", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="service_content"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "DOCK_FEE_REIMBURSEMENT", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="df_reimbursement"  value="'+data+'" class="form-control" />';
	            	}
            	},
	            { "data": "DOCK_FEE_REIMBURSEMENT_CRC", "width":"80px",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'DOCK_FEE_REIMBURSEMENT_CRC',
                               value: data,
                               display_value: full.DOCK_FEE_REIMBURSEMENT_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "INSPECTION_QUARANTINE_REIMBURSEMENT_CRC", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="iq_reimbursement"  value="'+data+'" class="form-control" />';
	            	}
            	},
	            { "data": "INSPECTION_QUARANTINE_REIMBURSEMENT_CRC", "width":"80px",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'INSPECTION_QUARANTINE_REIMBURSEMENT_CRC',
                               value: data,
                               display_value: full.INSPECTION_QUARANTINE_REIMBURSEMENT_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "CUSTOMS_ENTRY_FEE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="customs_entry_fee"  value="'+data+'" class="form-control" />';
	            	}
            	},
	           { "data": "CUSTOMS_ENTRY_FEE_CRC", "width":"80px",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'CUSTOMS_ENTRY_FEE_CRC',
                               value: data,
                               display_value: full.CUSTOMS_ENTRY_FEE_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "INSPECTION_ENTRY_FEE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="inspection_entry_fee"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "INSPECTION_ENTRY_FEE_CRC", "width":"80px",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'INSPECTION_ENTRY_FEE_CRC',
                               value: data,
                               display_value: full.INSPECTION_ENTRY_FEE_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "CUSTOMS_AGENT_FEE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="customs_agent_fee"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "CUSTOMS_AGENT_FEE_CRC", "width":"80px",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'CUSTOMS_AGENT_FEE_CRC',
                               value: data,
                               display_value: full.CUSTOMS_AGENT_FEE_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "TOTAL_FEE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="total_fee"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "TOTAL_FEE_CRC", 
	            	"render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'TOTAL_FEE_CRC',
                               value: data,
                               display_value: full.TOTAL_FEE_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
            	} ,
            	{ "data": "EFFECTIVE_TIME", 
	            	"render":function(data,type,full,meta){
            			if(!data)
            				data='';
            			 var field_html = template('table_date_field_template',
                           {
                               id: 'EFFECTIVE_TIME',
                               value: data,
                               display_value: full.EFFECTIVE_TIME,
                               style:'width:110px'
                           }
                       );
            			return field_html;
            		}
            	},
            	{ "data": "CREATOR_NAME", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" disabled value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "CREATE_STAMP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" disabled value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "REMARK", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="remark"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "DOCK_FEE_REIMBURSEMENT_CRC_NAME", "visible": false}, 
            	{ "data": "INSPECTION_QUARANTINE_REIMBURSEMENT_CRC_NAME", "visible": false}, 
            	{ "data": "CUSTOMS_ENTRY_FEE_CRC_NAME", "visible": false},
            	{ "data": "INSPECTION_ENTRY_FEE_CRC_NAME", "visible": false}, 
            	{ "data": "CUSTOMS_AGENT_FEE_CRC_NAME", "visible": false}, 
            	{ "data": "TOTAL_FEE_CRC_NAME", "visible": false}
	        ]
	    });

	    $('#add_custom_item').on('click', function(){
	        var item={};
	        cargoTable.row.add(item).draw(true);
	    });
	    
	    //刷新明细表
	    itemOrder.refleshCustomTable = function(order_id){
	    	var url = "/supplierContract/tableList?order_id="+order_id+"&type=custom";
	    	cargoTable.ajax.url(url).load();
	    }
	    
	   
	});
});
