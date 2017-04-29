define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'jq_blockui'], function ($, metisMenu, template) { 
	$(document).ready(function() {
	    var deletedTableIds=[];

	    //删除一行
	    $("#bulkCargo_item_table").on('click', '.delete', function(e){
	        e.preventDefault();
	        var tr = $(this).parent().parent();
	        deletedTableIds.push(tr.attr('id'));
	        
	        cargoTable.row(tr).remove().draw();
	    }); 
	    
	    itemOrder.buildBulkCargoItemDetail=function(){
	    	var cargo_table_rows = $("#bulkCargo_item_table tr");
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
	           
	            for(var i = 0; i < row.childNodes.length; i++){
	            	var name = $(row.childNodes[i]).find('input,select').attr('name');
	            	var value = $(row.childNodes[i]).find('input,select').val();
	            	if(name){
	            		item[name] = value;
	            	}
	            }
	            item.action = id.length > 0?'UPDATE':'CREATE';
	            cargo_items_array.push(item);
	            if(!id.length>0){
	            	var d = new Date();
	            	var str = d.getFullYear()+"-"+(d.getMonth()+1)+"-"+d.getDate()+' '+d.getHours()+':'+d.getMinutes()+':'+d.getSeconds();
	            	item.creator = $('#user_id').val();
	            	item.create_stamp = str;
	            }
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
	    	eeda.bindTableField('bulkCargo_item_table','UNIT_ID','/serviceProvider/searchUnit','');
	    	eeda.bindTableField('bulkCargo_item_table','POR','/location/searchPort','');
	    	eeda.bindTableField('bulkCargo_item_table','POL','/location/searchPort','');
	    	eeda.bindTableField('bulkCargo_item_table','POD','/location/searchPort','');
	    	eeda.bindTableField('bulkCargo_item_table','CARRIER','/serviceProvider/searchCarrier','');	    	
	    	eeda.bindTableField('bulkCargo_item_table','PORT_CHARGE_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('bulkCargo_item_table','DOC_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('bulkCargo_item_table','VGM_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('bulkCargo_item_table','D_O_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('bulkCargo_item_table','O_F_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('bulkCargo_item_table','HANDLING_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('bulkCargo_item_table','CFS_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('bulkCargo_item_table','THC_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('bulkCargo_item_table','BAF_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('bulkCargo_item_table','CIC_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('bulkCargo_item_table','LOADING_FEE_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('bulkCargo_item_table','TOTAL_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','POL','/location/searchPort','');
	    	eeda.bindTableField('ocean_item_table','POD','/location/searchPort','');

	    };

	    //------------事件处理
	    var cargoTable = eeda.dt({
            id: 'bulkCargo_item_table',
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
				{ "data": "POL", "width":"80px",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'POL',
                               value: data,
                               display_value: full.POL_NAME,
                               style:'width:120px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "POD", "width":"80px",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'POD',
                               value: data,
                               display_value: full.POD_NAME,
                               style:'width:120px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "SERVICE_OWNER", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="service_owner"  value="'+data+'" class="form-control" />';
	            	}
            	},
              { "data": "SHIPMENT", 
                "render": function ( data, type, full, meta ) {
                  if(!data)
                    data='';
                  return '<input type="text" name="shipment"  value="'+data+'" class="form-control" />';
                }
              },
            	{ "data": "PORT_CHARGE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="port_charge"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "PORT_CHARGE_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'PORT_CHARGE_CRC',
                               value: data,
                               display_value: full.PORT_CHARGE_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "DOC", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="DOC"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "DOC_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'DOC_CRC',
                               value: data,
                               display_value: full.DOC_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "VGM", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="VGM"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "VGM_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'VGM_CRC',
                               value: data,
                               display_value: full.VGM_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "D_O", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="D_O"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "D_O_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'D_O_CRC',
                               value: data,
                               display_value: full.D_O_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "O_F", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="O_F"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "O_F_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'O_F_CRC',
                               value: data,
                               display_value: full.O_F_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "HANDLING", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="HANDLING"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "HANDLING_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'HANDLING_CRC',
                               value: data,
                               display_value: full.HANDLING_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "CFS", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="CFS"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "CFS_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'CFS_CRC',
                               value: data,
                               display_value: full.CFS_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "THC", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="THC"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "THC_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'THC_CRC',
                               value: data,
                               display_value: full.THC_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "BAF", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="BAF"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "BAF_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'BAF_CRC',
                               value: data,
                               display_value: full.BAF_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "CIC", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="CIC"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "CIC_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'CIC_CRC',
                               value: data,
                               display_value: full.CIC_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "LOADING_FEE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="LOADING_FEE"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "LOADING_FEE_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'LOADING_FEE_CRC',
                               value: data,
                               display_value: full.LOADING_FEE_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "TOTAL", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="total"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "TOTAL_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'TOTAL_CRC',
                               value: data,
                               display_value: full.TOTAL_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	
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
            	{ "data": "REMARK", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="remark"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "CREATE_STAMP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" disabled  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "CREATOR", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		else
	            			data=full.CREATOR_NAME;
	            		return '<input type="text" disabled value="'+data+'" class="form-control" />';
	            	}
            	} ,
            	{ "data": "CREATOR_NAME", "visible": false,
	            	"render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return data;
	                }
	            },
	            { "data": "PORT_CHARGE_CRC_NAME", "visible": false},
	            { "data": "VGM_CRC_NAME", "visible": false},
	            { "data": "DOC_CRC_NAME", "visible": false},
	            { "data": "D_O_CRC_NAME", "visible": false},
	            { "data": "O_F_CRC_NAME", "visible": false},
	            { "data": "HANDLING_CRC_NAME", "visible": false},
	            { "data": "CFS_CRC_NAME", "visible": false},
	            { "data": "THC_CRC_NAME", "visible": false},
	            { "data": "BAF_CRC_NAME", "visible": false},
	            { "data": "CIC_CRC_NAME", "visible": false},
	            { "data": "LOADING_FEE_CRC_NAME", "visible": false},
	            { "data": "TOTAL_CRC_NAME", "visible": false}, 
            	{ "data": "POL_NAME", "visible": false}, 
            	{ "data": "POD_NAME", "visible": false}
	        ]
	    });
	    
	    $('#add_bulkCargo_item').on('click', function(){
	        var item={};
	        cargoTable.row.add(item).draw(true);
	    });
	    
	    //刷新明细表
	    itemOrder.refleshBulkCargoItemTable = function(order_id){
	    	var url = "/supplierContract/tableList?order_id="+order_id+"&type=bulkCargoItem";
	    	cargoTable.ajax.url(url).load();
	    }
	    
	   
	});
});
