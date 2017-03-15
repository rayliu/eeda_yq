define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'jq_blockui'], function ($, metisMenu, template) { 
	$(document).ready(function() {
	    var deletedTableIds=[];

	    //删除一行
	    $("#ocean_item_table").on('click', '.delete', function(e){
	        e.preventDefault();
	        var tr = $(this).parent().parent();
	        deletedTableIds.push(tr.attr('id'));
	        
	        cargoTable.row(tr).remove().draw();
	    }); 
	    
	    itemOrder.buildOceanCargoItemDetail=function(){
	    	var cargo_table_rows = $("#ocean_item_table tr");
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
	            	item.creator = $('#user_id').val();
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
	    	eeda.bindTableField('ocean_item_table','DOC_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','TLX_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','EIR_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','HSS_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','O_F_20GP_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','O_F_40GP_CRC','/serviceProvider/searchCurrency','');
        eeda.bindTableField('ocean_item_table','O_F_40HQ_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','THC_20GP_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','THC_40GP_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','THC_40HQ_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','EBS_20GP_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','EBS_40GP_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','EBS_40HQ_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','AMS_AFR_20GP_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','AMS_AFR_40GP_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','AMS_AFR_40HQ_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','VAT_20GP_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','VAT_40GP_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','VAT_40HQ_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','ISPS_20GP_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','ISPS_40GP_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','ISPS_40HQ_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','TRUCK_20GP_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','TRUCK_40GP_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','TRUCK_40HQ_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','TOTAL_20GP_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','TOTAL_40GP_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('ocean_item_table','TOTAL_40HQ_CRC','/serviceProvider/searchCurrency','');
	    };

	    //------------事件处理
	    var cargoTable = eeda.dt({
            id: 'ocean_item_table',
            autoWidth: false,
            "drawCallback": function( settings ) {
		        bindFieldEvent();
		    },
            columns:[
				{ "width": "30px",
	                "render": function ( data, type, full, meta ) {
	                	return '<button type="button" class="delete btn table_btn delete_btn btn-xs">'+
                        '<i class="fa fa-trash-o"></i> 删除</button>';
	                }
				},
	            { "data": "POL", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="POL"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "POD", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="POD"  value="'+data+'" class="form-control" />';
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
            	{ "data": "DOC", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="DOC"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "DOC_CRC", "width":"80px",
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
            	{ "data": "TLX", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="TLX"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "TLX_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'TLX_CRC',
                               value: data,
                               display_value: full.TLX_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "EIR", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="EIR"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "EIR_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'EIR_CRC',
                               value: data,
                               display_value: full.EIR_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "HSS", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="HSS"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "HSS_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'HSS_CRC',
                               value: data,
                               display_value: full.HSS_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "O_F_20GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="O_F_20GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "O_F_20GP_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'O_F_20GP_CRC',
                               value: data,
                               display_value: full.O_F_20GP_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "O_F_40GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="O_F_40GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "O_F_40GP_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'O_F_40GP_CRC',
                               value: data,
                               display_value: full.O_F_40GP_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "O_F_40HQ", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="O_F_40HQ"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "O_F_40HQ_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'O_F_40HQ_CRC',
                               value: data,
                               display_value: full.O_F_40HQ_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "THC_20GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="THC_20GP"  value="'+data+'" class="form_control" />';
	            	}
            	},
            	{ "data": "THC_20GP_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'THC_20GP_CRC',
                               value: data,
                               display_value: full.THC_20GP_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "THC_40GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="THC_40GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "THC_40GP_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'THC_40GP_CRC',
                               value: data,
                               display_value: full.THC_40GP_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "THC_40HQ", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="THC_40HQ"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "THC_40HQ_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'THC_40HQ_CRC',
                               value: data,
                               display_value: full.THC_40HQ_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "EBS_20GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="EBS_20GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "EBS_20GP_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'EBS_20GP_CRC',
                               value: data,
                               display_value: full.EBS_20GP_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "EBS_40GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="EBS_40GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "EBS_40GP_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'EBS_40GP_CRC',
                               value: data,
                               display_value: full.EBS_40GP_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "EBS_40HQ", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="EBS_40HQ"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "EBS_40HQ_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'EBS_40HQ_CRC',
                               value: data,
                               display_value: full.EBS_40HQ_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "AMS_AFR_20GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="AMS_AFR_20GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "AMS_AFR_20GP_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'AMS_AFR_20GP_CRC',
                               value: data,
                               display_value: full.AMS_AFR_20GP_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "AMS_AFR_40GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="AMS_AFR_40GP"  value="'+data+'" class="form_control" />';
	            	}
            	},
            	{ "data": "AMS_AFR_40GP_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'AMS_AFR_40GP_CRC',
                               value: data,
                               display_value: full.AMS_AFR_40GP_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "AMS_AFR_40HQ", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="AMS_AFR_40HQ"  value="'+data+'" class="form_control" />';
	            	}
            	},
            	{ "data": "AMS_AFR_40HQ_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'AMS_AFR_40HQ_CRC',
                               value: data,
                               display_value: full.AMS_AFR_40HQ_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "VAT_20GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="VAT_20GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "VAT_20GP_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'VAT_20GP_CRC',
                               value: data,
                               display_value: full.VAT_20GP_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "VAT_40GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="VAT_40GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "VAT_40GP_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'VAT_40GP_CRC',
                               value: data,
                               display_value: full.VAT_40GP_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "VAT_40HQ", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="VAT_40HQ"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "VAT_40HQ_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'VAT_40HQ_CRC',
                               value: data,
                               display_value: full.VAT_40HQ_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "ISPS_20GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="ISPS_20GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "ISPS_20GP_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'ISPS_20GP_CRC',
                               value: data,
                               display_value: full.ISPS_20GP_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "ISPS_40GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="ISPS_40GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "ISPS_40GP_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'ISPS_40GP_CRC',
                               value: data,
                               display_value: full.ISPS_40GP_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "ISPS_40HQ", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="ISPS_40HQ"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "ISPS_40HQ_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'ISPS_40HQ_CRC',
                               value: data,
                               display_value: full.ISPS_40HQ_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "TRUCK_20GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="TRUCK_20GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "TRUCK_20GP_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'TRUCK_20GP_CRC',
                               value: data,
                               display_value: full.TRUCK_20GP_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "TRUCK_40GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="TRUCK_40GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "TRUCK_40GP_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'TRUCK_40GP_CRC',
                               value: data,
                               display_value: full.TRUCK_40GP_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "TRUCK_40HQ", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="TRUCK_40HQ"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "TRUCK_40HQ_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'TRUCK_40HQ_CRC',
                               value: data,
                               display_value: full.TRUCK_40HQ_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "TOTAL_20GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="TOTAL_20GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "TOTAL_20GP_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'TOTAL_20GP_CRC',
                               value: data,
                               display_value: full.TOTAL_20GP_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "TOTAL_40GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="TOTAL_40GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "TOTAL_40GP_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'TOTAL_40GP_CRC',
                               value: data,
                               display_value: full.TOTAL_40GP_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "TOTAL_40HQ", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="TOTAL_40HQ"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "TOTAL_40HQ_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'TOTAL_40HQ_CRC',
                               value: data,
                               display_value: full.TOTAL_40HQ_CRC_NAME,
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
            	{ "data": "DOC_CRC_NAME", "visible": false} , 
            	{ "data": "TLX_CRC_NAME", "visible": false}, 
            	{ "data": "EIR_CRC_NAME", "visible": false}, 
            	{ "data": "HSS_CRC_NAME", "visible": false}, 
            	{ "data": "O_F_20GP_CRC_NAME", "visible": false}, 
            	{ "data": "O_F_40GP_CRC_NAME", "visible": false}, 
            	{ "data": "O_F_40HQ_CRC_NAME", "visible": false}, 
            	{ "data": "THC_20GP_CRC_NAME", "visible": false}, 
            	{ "data": "THC_40GP_CRC_NAME", "visible": false}, 
            	{ "data": "THC_40HQ_CRC_NAME", "visible": false},
            	{ "data": "EBS_20GP_CRC_NAME", "visible": false}, 
            	{ "data": "EBS_40GP_CRC_NAME", "visible": false}, 
            	{ "data": "EBS_40HQ_CRC_NAME", "visible": false}, 
            	{ "data": "AMS_AFR_20GP_CRC_NAME", "visible": false}, 
            	{ "data": "AMS_AFR_40GP_CRC_NAME", "visible": false}, 
            	{ "data": "AMS_AFR_40HQ_CRC_NAME", "visible": false},
            	{ "data": "VAT_20GP_CRC_NAME", "visible": false}, 
            	{ "data": "VAT_40GP_CRC_NAME", "visible": false}, 
            	{ "data": "VAT_40HQ_CRC_NAME", "visible": false}, 
            	{ "data": "ISPS_20GP_CRC_NAME", "visible": false}, 
            	{ "data": "ISPS_40GP_CRC_NAME", "visible": false}, 
            	{ "data": "ISPS_40HQ_CRC_NAME", "visible": false},
            	{ "data": "TRUCK_20GP_CRC_NAME", "visible": false}, 
            	{ "data": "TRUCK_40GP_CRC_NAME", "visible": false}, 
            	{ "data": "TRUCK_40HQ_CRC_NAME", "visible": false}, 
            	{ "data": "TOTAL_20GP_CRC_NAME", "visible": false}, 
            	{ "data": "TOTAL_40GP_CRC_NAME", "visible": false}, 
            	{ "data": "TOTAL_40HQ_CRC_NAME", "visible": false}
	        ]
	    });

	    $('#add_ocean_item_cargo').on('click', function(){
	        var item={};
	        cargoTable.row.add(item).draw(true);
	    });
	    
	    //刷新明细表
	    itemOrder.refleshOceanCargoItemTable = function(order_id){
	    	var url = "/supplierContract/tableList?order_id="+order_id+"&type=oceanCargoItem";
	    	cargoTable.ajax.url(url).load();
	    }
	    
	   
	});
});
