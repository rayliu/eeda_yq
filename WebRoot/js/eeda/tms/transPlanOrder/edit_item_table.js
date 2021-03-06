define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'jq_blockui'], function ($, metisMenu, template) { 
	$(document).ready(function() {

	    var deletedTableIds=[];

	    //删除一行
	    $("#cargo_table").on('click', '.delete', function(e){
	        e.preventDefault();
	        var tr = $(this).parent().parent();
	        deletedTableIds.push(tr.attr('id'));
	        
	        cargoTable.row(tr).remove().draw();
	    }); 
	    
	    salesOrder.buildCargoDetail=function(){
	    	var cargo_table_rows = $("#cargo_table tr");
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
	    	    format: 'yyyy-MM-dd hh:mm:ss',  
	    	    language: 'zh-CN'
	    	}).on('changeDate', function(el){
	    	    $(".bootstrap-datetimepicker-widget").hide();   
	    	    $(el).trigger('keyup');
	    	});

	    	eeda.bindTableField('cargo_table','UNIT_ID','/serviceProvider/searchUnit','');
	    	eeda.bindTableField('cargo_table','POR','/location/searchPort','');
	    	eeda.bindTableField('cargo_table','POL','/location/searchPort','');
	    	eeda.bindTableField('cargo_table','POD','/location/searchPort','');
	    	eeda.bindTableField('cargo_table','CARRIER','/serviceProvider/searchCarrier','');
	    };

	    //------------事件处理
	    var cargoTable = eeda.dt({
            id: 'cargo_table',
            autoWidth: false,
            "drawCallback": function( settings ) {
		        bindFieldEvent();
		        $.unblockUI();
		    },
            columns:[
				{ "width": "10px",
				    "render": function ( data, type, full, meta ) {
				    	if(full.IS_GEN_JOB == 'N')
				    		return '<input type="checkbox" class="checkBox">';
				    	else 
				    		return '<input type="checkbox" class="checkBox" disabled>';
				    }
				},
	            {"width": "10px",
	                "render": function ( data, type, full, meta ) {
	                	if(full.IS_GEN_JOB == 'Y'){
	                		return '<button type="button" class="delete btn btn-default btn-xs" disabled>删除</button> ';
	                	}else{
	                		return '<button type="button" class="delete btn btn-default btn-xs">删除</button> ';
	                	}
	                }
	            },
	            { "data": "TRANSPORT_TYPE","width": "80px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var str = '<select name="transport_type" class="form-control search-control">'
                			+'<option></option>'
		                   +'<option value="ocean" '+(data=='ocean' ? 'selected':'')+'>海运</option>'
		                   +'<option value="land" '+(data=='land' ? 'selected':'')+'>陆运</option>'
		                   +'<option value="air" '+(data=='air' ? 'selected':'')+'>空运</option>'
		                   +'</select>';
	                    return str;
	                }
	            },
	            { "data": "LOAD_TYPE","width": "80px",
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                	   data='';
	                   var str= '<select name="load_type" class="form-control search-control">'
	                	   	 	+'<option></option>'
			                   +'<option value="FCL" '+ (data=='FCL'?'selected':'') +'>FCL</option>'
			                   +'<option value="LCL" '+ (data=='LCL'?'selected':'') +'>LCL</option>'
			                   +'<option value="FTL" '+ (data=='FTL'?'selected':'') +'>FTL</option>'
			                   +'<option value="LTL" '+ (data=='LTL'?'selected':'') +'>LTL</option>'
			                   +'</select>';
			           return str;
	                }
	            },
	            { "data": "CONTAINER_TYPE","width": "80px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var str = '<select name="container_type" class="form-control search-control">'
	                    			+'<option></option>'
				                   +'<option value="20GP" '+(data=='20GP' ? 'selected':'')+'>20GP</option>'
				                   +'<option value="40GP" '+(data=='40GP' ? 'selected':'')+'>40GP</option>'
				                   +'<option value="40HQ" '+(data=='40HQ' ? 'selected':'')+'>40HQ</option>'
				                   +'</select>';
	                    return str;
	                }
	            },
	            { "data": "CONTAINER_AMOUNT","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="CONTAINER_AMOUNT" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "CARGO_NAME","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="CARGO_NAME" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "PIECES" ,"width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="PIECES" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "UNIT_ID","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                	if(!data)
	                        data='';
	                    var field_html = template('table_dropdown_template',
	                        {
	                            id: 'UNIT_ID',
	                            value: data,
	                            display_value: full.UNIT_NAME,
	                            style:'width:200px'
	                        }
	                    );
	                    return field_html;
	                }
	            },
	            { "data": "VOLUME","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="VOLUME" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "NET_WEIGHT","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="NET_WEIGHT" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "GROSS_WEIGHT","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="GROSS_WEIGHT" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "VGM","width": "180px",
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="VGM" value="'+data+'" class="form-control search-control" />';
	            	}
	            },
	            { "data": "FACTORY_LOADING_TIME" ,"width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var field_html = template('table_date_field_template',
		                    {
		                        id: 'FACTORY_LOADING_TIME',
		                        value: data.substr(0,19)
		                    }
		                );
	                    return field_html;
	                }
	            },
	            { "data": "TRUCK_TYPE","width": "80px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   var field_html = template('table_truck_type_field_template',
		                    {
		                        id: 'TRUCK_TYPE',
		                        value: data
		                    }
		                );
	                    return field_html;
	                }
	            },
	            { "data": "PICKUP_ADDR","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="PICKUP_ADDR" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "DILVERY_ADDR","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="DILVERY_ADDR" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "POR" ,"width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var field_html = template('table_dropdown_template',
		                    {
		                        id: 'POR',
		                        value: data,
		                        display_value: full.POR_NAME
		                    }
		                );
	                    return field_html;
	                }
	            },
	            { "data": "POL" ,"width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var field_html = template('table_dropdown_template',
		                    {
		                        id: 'POL',
		                        value: data,
		                        display_value: full.POL_NAME
		                    }
		                );
	                    return field_html;
	                }
	            },
	            { "data": "POD","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var field_html = template('table_dropdown_template',
		                    {
		                        id: 'POD',
		                        value: data,
		                        display_value: full.POD_NAME
		                    }
		                );
	                    return field_html;
	                }
	            },
	            { "data": "CARRIER","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var field_html = template('table_dropdown_template',
		                    {
		                        id: 'CARRIER',
		                        value: data,
		                        display_value: full.CARRIER_NAME
		                    }
		                );
	                    return field_html;
	                }
	            },
	            { "data": "VESSEL","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="VESSEL" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "VOYAGE" ,"width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="VOYAGE" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "CLS","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var field_html = template('table_date_field_template',
		                    {
		                        id: 'CLS',
		                        value: data.substr(0,19)
		                    }
		                );
	                    return field_html;
	                }
	            },
	            { "data": "ETD","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var field_html = template('table_date_field_template',
		                    {
		                        id: 'ETD',
		                        value: data.substr(0,19)
		                    }
		                );
	                    return field_html;
	                }
	            },
	            { "data": "ETA","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var field_html = template('table_date_field_template',
		                    {
		                        id: 'ETA',
		                        value: data.substr(0,19)
		                    }
		                );
	                    return field_html;
	                }
	            }, 
	            { "data": "CUSTOMS_TYPE","width": "80px",
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                	   data='';
	                   var str= '<select name="customs_type" class="form-control search-control">'
	                	   	 	+'<option></option>'
			                   +'<option value="代理报关" '+ (data=='代理报关'?'selected':'') +'>代理报关</option>'
			                   +'<option value="自理报关" '+ (data=='自理报关'?'selected':'') +'>自理报关</option>'
			                   +'</select>';
			           return str;
	                }
	            },
	            { "data": "CUSTOMS_DATA","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var field_html = template('table_date_field_template',
		                    {
		                        id: 'CUSTOMS_DATA',
		                        value: data.substr(0,19)
		                    }
		                );
	                    return field_html;
	                }
	            }, 
	            { "data": "POR_NAME", "visible": false,
	            	"render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return data;
	                }
	            }, 
	            { "data": "POL_NAME", "visible": false,
	            	"render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return data;
	                }
	            },
	            { "data": "POD_NAME", "visible": false,
	            	"render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return data;
	                }
	            }, 
	            { "data": "CARRIER_NAME", "visible": false,
	            	"render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return data;
	                }
	            }, 
	            { "data": "IS_GEN_JOB", "visible": false,
	            	"render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return data;
	                }
	            },
	            { "data": "UNIT_NAME", "visible": false,
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return data;
	                }
	            }
	        ]
	    });

	    $('#add_cargo').on('click', function(){
	        var item={};
	        cargoTable.row.add(item).draw(true);
	    });
	    
	    //刷新明细表
	    salesOrder.refleshTable = function(order_id){
	    	var url = "/planOrder/tableList?order_id="+order_id;
	    	cargoTable.ajax.url(url).load();
	    }
	    
	    //checkbox选中则button可点击
		$('#cargo_table').on('click','.checkBox',function(){
			
			var hava_check = 0;
			$('#cargo_table input[type="checkbox"]').each(function(){	
				var checkbox = $(this).prop('checked');
	    		if(checkbox){
	    			hava_check=1;
	    		}	
			})
			if(hava_check>0){
				$('#create_jobOrder').attr('disabled',false);
			}else{
				$('#create_jobOrder').attr('disabled',true);
			}
		});
	});
});
