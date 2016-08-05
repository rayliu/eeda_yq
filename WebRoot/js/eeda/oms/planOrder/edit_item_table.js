define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) { 
	$(document).ready(function() {

	    var deletedTableIds=[];
	    
	    if($("#order_flag").val()=='1'){
	    	$('#create_jobOrder').attr('disabled',true);
	    	$('#create_jobOrder').html("已创建过工作单");
	    }

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
	            	var el = $(row.childNodes[i]).find('input, select');
	            	if(el.length>1){
	            		el = $(el[0]);
	            	}

	            	var name = el.attr('name'); //name='abc'
	            	
	            	if(el && name){
	                	var value = $(el).val();//元素的值
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
	    	    format: 'yyyy-MM-dd',  
	    	    language: 'zh-CN'
	    	}).on('changeDate', function(el){
	    	    $(".bootstrap-datetimepicker-widget").hide();   
	    	    $(el).trigger('keyup');
	    	});

	    	eeda.bindTablePortField();
	    	eeda.bindTableCarrierField();
	    };

	    //------------事件处理
	    var cargoTable = eeda.dt({
            id: 'cargo_table',
            "drawCallback": function( settings ) {
		        bindFieldEvent();
		    },
            columns:[
				{ "data":"ID","width": "10px",
				    "render": function ( data, type, full, meta ) {
				    	if(data)
				    		return '<input type="checkbox" class="checkBox">';
				    	else 
				    		return '<input type="checkbox" class="checkBox" disabled>';
				    }
				},
                         
	            { "width": "30px",
	                "render": function ( data, type, full, meta ) {
	                	return '<button type="button" class="delete btn btn-default btn-xs">删除</button> ';
	                }
	            },
	            { "data": "TRANSPORT_TYPE",
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
	            { "data": "LOAD_TYPE", 
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
	            { "data": "CONTAINER_TYPE", 
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var str = '<select name="container_type" class="form-control search-control">'
	                    			+'<option></option>'
				                   +'<option value="20GP" '+(data=='20GP' ? 'selected':'')+'>20GP</option>'
				                   +'<option value="40GP" '+(data=='40GP' ? 'selected':'')+'>40GP</option>'
				                   +'<option value="45GP" '+(data=='45GP' ? 'selected':'')+'>45GP</option>'
				                   +'</select>';
	                    return str;
	                }
	            },
	            { "data": "CONTAINER_AMOUNT",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="CONTAINER_AMOUNT" value="'+data+'" class="form-control search-control" style="width:80px"/>';
	                }
	            },
	            { "data": "CARGO_NAME",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="CARGO_NAME" value="'+data+'" class="form-control search-control" style="width:80px"/>';
	                }
	            },
	            { "data": "PIECES" ,
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="PIECES" value="'+data+'" class="form-control search-control" style="width:80px"/>';
	                }
	            },
	            { "data": "VOLUME",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="VOLUME" value="'+data+'" class="form-control search-control" style="width:80px"/>';
	                }
	            },
	            { "data": "NET_WEIGHT",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="NET_WEIGHT" value="'+data+'" class="form-control search-control" style="width:80px"/>';
	                }
	            },
	            { "data": "GROSS_WEIGHT",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="GROSS_WEIGHT" value="'+data+'" class="form-control search-control" style="width:80px"/>';
	                }
	            },
	            { "data": "FACTORY_LOADING_TIME" ,
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
	            { "data": "TRUCK_TYPE",
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
	            { "data": "PICKUP_ADDR",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="PICKUP_ADDR" value="'+data+'" class="form-control search-control" style="width:80px"/>';
	                }
	            },
	            { "data": "DILVERY_ADDR",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="DILVERY_ADDR" value="'+data+'" class="form-control search-control" style="width:80px"/>';
	                }
	            },
	            { "data": "POR" ,
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var field_html = template('table_port_field_template',
		                    {
		                        id: 'POR',
		                        value: data,
		                        display_value: full.POR_NAME
		                    }
		                );
	                    return field_html;
	                }
	            },
	            { "data": "POL" ,
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var field_html = template('table_port_field_template',
		                    {
		                        id: 'POL',
		                        value: data,
		                        display_value: full.POL_NAME
		                    }
		                );
	                    return field_html;
	                }
	            },
	            { "data": "POD",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var field_html = template('table_port_field_template',
		                    {
		                        id: 'POD',
		                        value: data,
		                        display_value: full.POD_NAME
		                    }
		                );
	                    return field_html;
	                }
	            },
	            { "data": "CARRIER",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var field_html = template('table_carrier_field_template',
		                    {
		                        id: 'CARRIER',
		                        value: data,
		                        display_value: full.CARRIER_NAME
		                    }
		                );
	                    return field_html;
	                }
	            },
	            { "data": "VESSEL",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="VESSEL" value="'+data+'" class="form-control search-control" style="width:80px"/>';
	                }
	            },
	            { "data": "VOYAGE" ,
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="VOYAGE" value="'+data+'" class="form-control search-control" style="width:80px"/>';
	                }
	            },
	            { "data": "CLS",
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
	            { "data": "ETD",
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
	            { "data": "ETA",
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
	            }, { "data": "POR_NAME", "visible": false,
	            	"render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return data;
	                }
	            }, { "data": "POL_NAME", "visible": false,
	            	"render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return data;
	                }
	            }, { "data": "POD_NAME", "visible": false,
	            	"render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return data;
	                }
	            }, { "data": "CARRIER_NAME", "visible": false,
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
	        bindFieldEvent();
	    });
	    
	    //刷新明细表
	    salesOrder.refleshTable = function(order_id){
	    	var url = "/planOrder/tableList?order_id="+order_id;
	    	cargoTable.ajax.url(url).load();
	    }
	    
	    //checkbox选中则button可点击
		$('#cargo_table').on('click','.checkBox',function(){
			if($("#order_flag").val()==''){
				var hava_check = 0;
				$('#cargo_table input[type="checkbox"]').each(function(){	
					var checkbox = $(this).prop('checked');
		    		if(checkbox){
		    			hava_check=1;
		    		}	
				})
			}
			if(hava_check>0){
				$('#create_jobOrder').attr('disabled',false);
			}else{
				$('#create_jobOrder').attr('disabled',true);
			}
		});
	});
});
