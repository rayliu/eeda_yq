define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','datetimepicker_CN'], function ($, metisMenu, template) { 
	$(document).ready(function() {
	    var deletedTableIds=[];

	    //删除一行
	    $("#air_transport_item_table").on('click', '.delete', function(e){
	        e.preventDefault();
	        var tr = $(this).parent().parent();
	        deletedTableIds.push(tr.attr('id'));
	        
	        cargoTable.row(tr).remove().draw();
	    }); 
	    
	    itemOrder.buildAirTransportItemDetail=function(){
	    	var cargo_table_rows = $("#air_transport_item_table tr");
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
	    	$('table .date').datetimepicker({  
	    	    format: 'yyyy-MM-dd',  
	    	    language: 'zh-CN'
	    	}).on('changeDate', function(el){
	    	    $(".bootstrap-datetimepicker-widget").hide();   
	    	    $(el).trigger('keyup');
	    	});
	    	
	    	
	    	eeda.bindTableField('cargo_table','UNIT_ID','/serviceProvider/searchUnit','');
	    	eeda.bindTableField('cargo_table','POR','/location/searchPort','port');
	    	eeda.bindTableField('air_transport_item_table','45KG_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('air_transport_item_table','100KG_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('air_transport_item_table','300KG_CRC','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('air_transport_item_table','500KG_CRC','/serviceProvider/searchCurrency','');
	    };

	    //------------事件处理
	    var cargoTable = eeda.dt({
            id: 'air_transport_item_table',
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
	            { "data": "ROUTE_FROM", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="route_from"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "TRANSIT_PLACE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="transit_place"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "ROUTE_TO", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="route_to"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "FLIGHT_NO", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="flight_no"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "ETD", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		 var field_html = template('table_date_field_template',
	     	                    {
	     	                        id: 'ETD',
	     	                        value: data.substr(0,19),
	     	                        style:'width:180px'
	     	                    }
	     	                );
	                     return field_html;
	            	}
            	},
            	{ "data": "ETA", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		var field_html = template('table_date_field_template',
	     	                    {
	     	                        id: 'ETA',
	     	                        value: data.substr(0,19),
	     	                        style:'width:180px'
	     	                    }
	     	                );
	                     return field_html;
	            	}
            	},
            	{ "data": "FREQUENCY", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="frequency"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "45KG", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="45KG"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "45KG_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: '45KG_CRC',
                               value: data,
                               display_value: full.A45KG_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "100KG", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="100KG"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "100KG_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: '100KG_CRC',
                               value: data,
                               display_value: full.A100KG_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "300KG", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="300KG"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "300KG_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: '300KG_CRC',
                               value: data,
                               display_value: full.A300KG_CRC_NAME,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
                },
            	{ "data": "500KG", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="500KG"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "500KG_CRC", "width":"60px","className":"currency_name",
                    "render": function ( data, type, full, meta ) {
                	   if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: '500KG_CRC',
                               value: data,
                               display_value: full.A500KG_CRC_NAME,
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
            	{ "data": "REMARK", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="remark"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "CREATOR_NAME", "visible": false,
	            	"render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return data;
	                }
	            },
	            { "data": "A45KG_CRC_NAME", "visible": false}, 
            	{ "data": "A100KG_CRC_NAME", "visible": false}, 
            	{ "data": "A300KG_CRC_NAME", "visible": false}, 
            	{ "data": "A500KG_CRC_NAME", "visible": false}
	        ]
	    });

	    $('#add_air_transport_item').on('click', function(){
	        var item={};
	        cargoTable.row.add(item).draw(true);
	    });
	    
	    //刷新明细表
	    itemOrder.refleshAirTransportItemTable = function(order_id){
	    	var url = "/supplierContract/tableList?order_id="+order_id+"&type=airTransportItem";
	    	cargoTable.ajax.url(url).load();
	    }
	    
	   
	});
});
