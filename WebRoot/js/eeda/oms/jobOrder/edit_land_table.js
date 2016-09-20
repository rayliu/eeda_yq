define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
$(document).ready(function() {

	var deletedTableIds=[];
    //删除一行
    $("#land_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))
        
        cargoTable.row(tr).remove().draw();
    }); 
    //添加一行
    $('#add_land').on('click', function(){
        var item={};
        cargoTable.row.add(item).draw(true);
    });
    

    itemOrder.buildLoadItem=function(){
        var cargo_table_rows = $("#land_table tr");
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
            item.status = '待发车';//默认待发车
            for(var i = 1; i < row.childNodes.length; i++){
            	var el = $(row.childNodes[i]).find('input,select');
            	var name = el.attr('name'); //name='abc'
            	
            	if(el && name){
                	var value = el.val();//元素的值
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
    	
    	eeda.bindTableField('TRANSPORT_COMPANY','/serviceProvider/searchTruckCompany','truck');
    };
    //------------事件处理
	 var cargoTable = eeda.dt({
	        id: 'land_table',
	        autoWidth: false,
	        drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
	        	bindFieldEvent();
	        },
	        columns:[
			{ "data":"ID","width": "10px",
			    "render": function ( data, type, full, meta ) {
			    	if(data)
			    		return '<input type="checkbox" class="checkBox" style="width:30px">';
			    	else 
			    		return '<input type="checkbox" class="checkBox" style="width:30px" disabled>';
			    }
			},
            { "width": "30px",
                "render": function ( data, type, full, meta ) {
                	return '<button type="button" class="delete btn btn-default btn-xs" style="width:50px">删除</button>';
                }
            },
            { "data": "UNLOAD_TYPE", "width": "80px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    var str= '<select name="unload_type" class="form-control search-control"  style="width:100px">'
            	   	 		   +'<option></option>'
			                   +'<option value="卸货" '+ (data=='卸货'?'selected':'') +'>卸货</option>'
			                   +'<option value="收货" '+ (data=='收货'?'selected':'') +'>收货</option>'
			                   +'<option value="收卸货" '+ (data=='收卸货'?'selected':'') +'>收卸货</option>'
			                   +'</select>';
                    return str;
                }
            },
            { "data": "TRANSPORT_COMPANY", "width": "180px",
                "render": function ( data, type, full, meta ) {
                	if(!data)
                        data='';
                    var field_html = template('table_dropdown_template',
                            {
                                id: 'TRANSPORT_COMPANY',
                                value: data,
                                display_value: full.TRANSPORT_COMPANY_NAME
                            }
                        );
                    return field_html;
                }
            },
            { "data": "TRANS_NO", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="trans_no" value="'+data+'" class="form-control" style="width:200px" />';
                }
            },
            { "data": "DRIVER", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="driver" value="'+data+'" class="form-control" style="width:200px" />';
                }
            },
            { "data": "DRIVER_TEL", "width": "80px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="driver_tel" value="'+data+'" class="form-control" style="width:200px" />';
                }
            },
            { "data": "TRUCK_TYPE", "width": "80px",
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
            { "data": "CAR_NO", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="car_no" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "CONSIGNOR", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="consignor" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "CONSIGNOR_PHONE","width": "180px", 
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="consignor_phone" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "TAKE_ADDRESS", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="take_address" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "CONSIGNEE", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="consignee" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "CONSIGNEE_PHONE","width": "180px", 
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="consignee_phone" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "DELIVERY_ADDRESS", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="delivery_address" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "ETA", "width": "180px",
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
            { "data": "CARGO_INFO", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="cargo_info" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "REQUIRED_TIME_REMARK", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="required_time_remark" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "SIGN_DESC", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="sign_desc" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "SIGN_STATUS", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="sign_status" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            }, 
            { "data": "TRANSPORT_COMPANY_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            }
        ]
    });

    //刷新明细表
    itemOrder.refleshLandItemTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=land";
    	cargoTable.ajax.url(url).load();
    }
    

});
});