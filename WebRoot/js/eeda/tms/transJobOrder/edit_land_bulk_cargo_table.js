define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
$(document).ready(function() {

	var deletedTableIds=[];
	
    //删除一行
    $("#land_bulk_cargo_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))
        
        abulkCargoTable.row(tr).remove().draw();
    }); 
    //添加一行
    $('#add_land_bulk').on('click', function(){
    	var item={};
        abulkCargoTable.row.add(item).draw(true);
    });
    

    itemOrder.buildLoadBulkItem=function(){
        var cargo_table_rows = $("#land_bulk_cargo_table tr");
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
            item.item_type = 'bulk';
            item.car_no = $('#car_no').val();
            item.truck_type = $('#cartype').val();
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
    	
        eeda.bindTableFieldDockInfo('land_bulk_cargo_table','LOADING_WHARF1');
        eeda.bindTableFieldDockInfo('land_bulk_cargo_table','LOADING_WHARF2');
        
        $('#land_bulk_cargo_table [name=CABINET_DATE_div]').datetimepicker({
            format: 'yyyy-MM-dd',  
            language: 'zh-CN'
          }).on('changeDate', function(ev){
                $(".bootstrap-datetimepicker-widget").hide();
                	$('#land_bulk_cargo_table [name=CABINET_DATE_div]').each(function(){
                		var self_val = $(this).find('input').val();
                		if(self_val){
                			$("#charge_time").val(self_val);
                		}
                		
                	});
            });
    	
    	$('#land_bulk_cargo_table [name=CABINET_DATE_div]').on('keyup','[name=CABINET_DATE]',function(){
    		$('#land_table [name=CLOSING_DATE_div]').each(function(){
        		var self_val = $(this).find('input').val();
        		if(self_val){
        			$("#charge_time").val(self_val);
        		}
        		
        	});
    	}); 
    };


    //------------事件处理
	 var abulkCargoTable = eeda.dt({
	        id: 'land_bulk_cargo_table',
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
                	return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:50px">删除</button>';
                }
            },
            
            { "data": "DOC_NAME","width": "10px",
                "render": function ( data, type, full, meta ) {
                	if(data)
                		return '<button type="button" class="btn btn-default btn-xs delete_sign_desc" style="width:30px">删除签收文件</button>';
                	else 
                		return '';
                }
            },
            { "data": "CABINET_DATE", "width": "158px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
                        data='';
                    var field_html = template('table_date_field_template',
	                    {
	                        id: 'CABINET_DATE',
	                        value: data.substr(0,19),
	                        style:'width:150px'
	                    }
	                );
                    return field_html;
            	}
            },
            { "data": "LAND_RECEIPT_NO", "width": "88px", 
            	"render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="land_receipt_no" value="'+data+'" class="form-control" style="width:100px" />';
                }
            },
           { "data": "LOADING_WHARF1", "width": "120px", "className":"consigner_addr",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                            data='';
                        var field_html = template('table_dock_no_field_template',
                            {
                                id: 'LOADING_WHARF1',
                                value: data,
                                display_value: full.LOADING_WHARF1_NAME,
                                style:'width:140px',
                            }
                        );
                        return field_html;
                    }
            },
            { "data": "LOADING_WHARF2", "width": "120px", "className":"consigner_addr",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                            data='';
                        var field_html = template('table_dock_no_field_template',
                            {
                                id: 'LOADING_WHARF2',
                                value: data,
                                display_value: full.LOADING_WHARF2_NAME,
                                style:'width:140px',
                            }
                        );
                        return field_html;
                    }
            },
            { "data": "LOADING_PLATFORM", "width": "90px", 
            	"render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="loading_platform" value="'+data+'" class="form-control" style="width:100px" />';
                }
            },
            { "data": "TONNAGE", "width": "70px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="tonnage" value="'+data+'" class="form-control tonnage" style="width:80px" />';
                }
            },
            { "data": "VOLUME", "width": "70px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="volume" value="'+data+'" class="form-control" style="width:80px" />';
                }
            },
            { "data": "LOADING_WHARF1_NAME", "visible": false},
            { "data": "LOADING_WHARF2_NAME", "visible": false}
        ]
    });

   
    

    //刷新明细表
    itemOrder.refleshLandBulkItemTable = function(order_id){
    	var url = "/transJobOrder/tableList?order_id="+order_id+"&type=land_bulk";
    	abulkCargoTable.ajax.url(url).load();
    }
    
    //全选
    $('#allCheckOfLand2').click(function(){
    	var ischeck = this.checked;
    	$('#land_bulk_cargo_table .checkBox').each(function(){
    		this.checked = ischeck;
    	})
    })
    
    //一起删除签收文件
    $("#land_bulk_cargo_table").on('click', '.delete_sign_desc', function(){
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
    $("#land_bulk_cargo_table").on('click', '.delete_icon_of_sign_desc', function(){
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
    $("#land_bulk_cargo_table").on('click', '.upload', function(){
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