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
    	
    	eeda.bindTableField('land_table','TRANSPORT_COMPANY','/serviceProvider/searchTruckCompany','truck');
        eeda.bindTableFieldTruckOut('land_table', 'CONSIGNOR');
        eeda.bindTableFieldTruckIn('land_table', 'CONSIGNEE');
    };
    //------------事件处理
	 var cargoTable = eeda.dt({
	        id: 'land_table',
	        autoWidth: false,
	        drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
	        	bindFieldEvent();
	        },
	        columns:[
			{ "data":"ID","width": "30px",
			    "render": function ( data, type, full, meta ) {
			    	if(data)
			    		return '<input type="checkbox" class="checkBox" style="width:30px">';
			    	else 
			    		return '<input type="checkbox" class="checkBox" style="width:30px" disabled>';
			    }
			},
            { "width": "50px",
                "render": function ( data, type, full, meta ) {
                	return '<button type="button" class="delete btn btn-default btn-xs" style="width:50px">删除</button>';
                }
            },
            { "data":"ID","width": "100px",
            	"render": function ( data, type, full, meta ) {
            		if(data)
	            		return '<span class="btn btn-success btn-xs fileinput-button" style="width:100px">' 
		                		+'<i class="glyphicon glyphicon-plus"></i>'
		                		+'<span>上传签收文件</span>'
		                		+'<input class="upload" type="file" multiple>'
		                		+'</span>'
		            else
		            	return '<span class="btn btn-default btn-xs fileinput-button" style="width:100px" title="请先保存再上传文件">' 
		                		+'<i class="glyphicon glyphicon-plus"></i>'
		                		+'<span>上传签收文件</span>'
		                		+'<input  class="upload" type="button" disabled>'
		                		+'</span>'			
            	}
            },
            { "data": "DOC_NAME","width": "100px",
                "render": function ( data, type, full, meta ) {
                	if(data)
                		return '<button type="button" class="btn btn-default btn-xs delete_sign_desc" style="width:100px">删除签收文件</button>';
                	else 
                		return '';
                }
            },
            { "data": "UNLOAD_TYPE", "width": "100px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    var str= '<select name="unload_type" class="form-control search-control"  style="width:100px">'
            	   	 		   +'<option></option>'
			                   +'<option value="提吉柜" '+ (data=='提吉柜'?'selected':'') +'>提吉柜</option>'
			                   +'<option value="移柜" '+ (data=='移柜'?'selected':'') +'>移柜</option>'
			                   +'<option value="收重柜" '+ (data=='收重柜'?'selected':'') +'>收重柜</option>'
			                   +'</select>';
                    return str;
                }
            },
            { "data": "CABINET_DATE", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
                        data='';
                    var field_html = template('table_date_field_template',
	                    {
	                        id: 'CABINET_DATE',
	                        value: data.substr(0,19),
	                        style:'width:180px'
	                    }
	                );
                    return field_html;
            	}
            },
            { "data": "TAKE_ADDRESS", "width": "180px", "className":"consigner_addr",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="take_address" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "CAR_NO", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="car_no" value="'+data+'" class="form-control" style="width:200px"/>';
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
            { "data": "DRIVER", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="driver" value="'+data+'" class="form-control" style="width:200px" />';
                }
            },
            { "data": "DRIVER_TEL", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="driver_tel" value="'+data+'" class="form-control" style="width:200px" />';
                }
            },
            { "data": "CONSIGNOR", "width": "180px",
            	"render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    var field_html = template('table_truck_out_template',
                        {
                            id: 'CONSIGNOR',
                            value: data,
                            display_value: full.CONSIGNOR_NAME,
                            style:'width:200px'
                        }
                    );
                    return field_html;
                }
            },
            { "data": "CONSIGNOR_PHONE","width": "180px", "className":"consigner_phone",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="consignor_phone" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            
            { "data": "CONSIGNEE", "width": "180px",
            	"render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    var field_html = template('table_truck_in_template',
                        {
                            id: 'CONSIGNEE',
                            value: data,
                            display_value: full.CONSIGNEE_NAME,
                            style:'width:200px'
                        }
                    );
                    return field_html;
                }
            },
            { "data": "CONSIGNEE_PHONE","width": "180px",  "className":"consignee_phone",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="consignee_phone" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "DELIVERY_ADDRESS", "width": "180px", "className":"consignee_addr",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="delivery_address" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "CARGO_DESC", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="cargo_desc" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "PIECES", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="pieces" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "VOLUME", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="volume" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "REQUIRED_TIME_REMARK", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="required_time_remark" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "DOC_NAME", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data){
            			return '';
            		}
            		else{
            			var arr = data.split(",");
            			var idStr = full.JOB_ORDER_LAND_DOC_ID;
            			var idArr = idStr.split(",");
            			var str = "";
	            		for(var i=0;i<arr.length;i++){
		            		str += '<a href="/upload/doc/'+arr[i]+'" target="_blank">'+arr[i]+'</a>&nbsp;&nbsp;'
		            			  +'<a id="'+idArr[i]+'" class="glyphicon glyphicon-remove delete_icon_of_sign_desc" style="margin-right:15px;" role="menuitem" tabindex="-10"></a>'
	            		}
	            		return str;
            		}
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
            },
            { "data": "CONSIGNOR_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            },
            { "data": "CONSIGNEE_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            },
            { "data": "JOB_ORDER_LAND_DOC_ID", "visible": false,
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
    	var url = "/transJobOrder/tableList?order_id="+order_id+"&type=land";
    	cargoTable.ajax.url(url).load();
    }
    
    //全选
    $('#allCheckOfLand').click(function(){
    	var ischeck = this.checked;
    	$('.checkBox').each(function(){
    		this.checked = ischeck;
    	})
    })
    
    //一起删除签收文件
    $("#land_table").on('click', '.delete_sign_desc', function(){
    	var tr = $(this).parent().parent();
    	var id = tr.attr('id');
    	var order_id = $('#order_id').val();
	     $.post('/transJobOrder/deleteSignDesc', {id:id}, function(data){
	        	 $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
	        	 itemOrder.refleshLandItemTable(order_id);
	     },'json').fail(function() {
	         	 $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
	     });
    });
    //单个删除签收文件
    $("#land_table").on('click', '.delete_icon_of_sign_desc', function(){
    	var name = $(this).prev().text();
    	var id = $(this).attr('id');
    	var order_id = $('#order_id').val();
	     $.post('/transJobOrder/deleteOneSignDesc', {id:id,name:name}, function(data){
	        	 $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
	        	 itemOrder.refleshLandItemTable(order_id);
	     },'json').fail(function() {
	         	 $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
	     });
    })
	//上传签收文件
    $("#land_table").on('click', '.upload', function(){
		var id = $(this).parent().parent().parent().attr('id');
		var order_id = $('#order_id').val();
			$(this).fileupload({
				autoUpload: true, 
			    url: '/transJobOrder/uploadSignDesc?id='+id,
			    dataType: 'json',
		        done: function (e, data) {
		    		$.scojs_message('上传成功', $.scojs_message.TYPE_OK);
		    		itemOrder.refleshLandItemTable(order_id);
			     },
		        error: function () {
		            alert('上传的时候出现了错误！');
		        }
			});
	});

});
});