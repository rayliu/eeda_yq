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
        var item={"TRUCK_TYPE":"40HQ"};
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
            item.item_type = 'shipment';
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
    	
        eeda.bindTableFieldCarInfo('land_table', 'CAR_NO');
        eeda.bindTableFieldDockInfo('land_table','LOADING_WHARF1');
        eeda.bindTableFieldDockInfo('land_table','LOADING_WHARF2');
        
        
        $('#land_table [name=CLOSING_DATE_div]').datetimepicker({
            format: 'yyyy-MM-dd',  
            language: 'zh-CN'
          }).on('changeDate', function(ev){
                $(".bootstrap-datetimepicker-widget").hide();
                	$('#land_table [name=CLOSING_DATE_div]').each(function(){
                		var self_val = $(this).find('input').val();
                		if(self_val){
                			$("#charge_time").val(self_val);
                		}
                		
                	});
            });
    	
    	$('#land_table [name=CLOSING_DATE_div]').on('keyup','[name=CLOSING_DATE]',function(){
    		$('#land_table [name=CLOSING_DATE_div]').each(function(){
        		var self_val = $(this).find('input').val();
        		if(self_val){
        			$("#charge_time").val(self_val);
        		}
        		
        	});
    	});        
    };


    //------------事件处理
	 var cargoTable = eeda.dt({
	        id: 'land_table',
	        autoWidth: false,
	        drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
	        	bindFieldEvent();
	        },
            initComplete: function(settings, json){
                if($('#order_id').val()==''){//创建时默认出来两行
                    addDefaultRows();
                }

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
            { "data": "UNLOAD_TYPE", "width": "50px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    var str= '<select name="unload_type" class="form-control search-control ttype"  style="width:70px">'
            	   	 		   +'<option></option>'
			                   +'<option value="提吉柜" '+ (data=='提吉柜'?'selected':'') +'>提吉柜</option>'
			                   +'<option value="移柜" '+ (data=='移柜'?'selected':'') +'>移柜</option>'
			                   +'<option value="收重柜" '+ (data=='收重柜'?'selected':'') +'>收重柜</option>'
			                   +'<option value="全程" '+ (data=='全程'?'selected':'') +'>全程</option>'
			                   +'</select>';
                    return str;
                }
            },
            { "data": "CABINET_DATE", "width": "100px",
            	"render": function ( data, type, full, meta ) {
            		var info = "";
            		if(!data)
                        data='';
            		if(full.UNLOAD_TYPE=="收重柜" || full.UNLOAD_TYPE=="移柜"){
            			info ="disabled"
            		}
                    var field_html = template('table_date_field_template',
	                    {
	                        id: 'CABINET_DATE',
	                        value: data.substr(0,19),
	                        style:'width:120px',
	                        disabled:info
	                    }
	                );
                    return field_html;
            	}
            },
            { "data": "CLOSING_DATE", "width": "100px",
            	"render": function ( data, type, full, meta ) {
            		var info = "";
            		
            		if(!data)
                        data='';
            		if(full.UNLOAD_TYPE=="提吉柜" || full.UNLOAD_TYPE=="移柜"){
            			info ="disabled"
            		}
                    var field_html = template('table_date_field_template',
	                    {
	                        id: 'CLOSING_DATE',
	                        value: data.substr(0,10),
	                        style:'width:120px',
	                        disabled:info
	                    }
	                );
                    return field_html;
            	}
            },
            { "data": "LOADING_WHARF1", "width": "100px", "className":"consigner_addr",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                            data='';
                        var field_html = template('table_dock_no_field_template',
                            {
                                id: 'LOADING_WHARF1',
                                value: data,
                                display_value: full.LOADING_WHARF1_NAME,
                                style:'width:120px',
                            }
                        );
                        return field_html;
                    }
            },
            { "data": "LOADING_WHARF2", "width": "100px", "className":"consigner_addr",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                            data='';
                        var field_html = template('table_dock_no_field_template',
                            {
                                id: 'LOADING_WHARF2',
                                value: data,
                                display_value: full.LOADING_WHARF2_NAME,
                                style:'width:120px',
                            }
                        );
                        return field_html;
                    }
            },
            { "data": "LOADING_PLATFORM", "width": "80px", 
            	"render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="loading_platform" value="'+data+'" class="form-control" style="width:100px" />';
                }
            },
            { "data": "CAR_NO", "width": "80px",
            	"render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    var field_html = template('table_car_no_field_template',
                        {
                            id: 'CAR_NO',  //component_id 便于用 #id取组件
                            value: data,
                            display_value: full.CAR_NO_NAME,
                            style:'width:100px'
                        }
                    );
                    return field_html;
                }
            },
            { "data": "TRUCK_TYPE", "width": "60px",
                "render": function ( data, type, full, meta ) {
                	if(!data)
                        data='';
                   var field_html = template('table_truck_type_field_template',
	                    {
	                        id: 'TRUCK_TYPE',
	                        value: data,
	                        style:'width:60px'
	                    }
	                );
                    return field_html;
                }
            },
            { "data": "TOCA_WEIGHT", "width": "60px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    data = (parseFloat(data)).toFixed(2)
                    if(isNaN(data)){
                    	data = "";
                    }
                    return '<input type="text" name="toca_weight" value="'+data+'" class="form-control toca_weight" style="width:80px" />';
                }
            },
            { "width": "30px",
                "render": function ( data, type, full, meta ) {
                    return '<span aligen="center">kg</span>';
                }
            },
            { "data": "HEAD_WEIGHT", "width": "60px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    data = (parseFloat(data)).toFixed(2)
                    if(isNaN(data)){
                    	data = "";
                    }
                    return '<input type="text" name="head_weight" value="'+data+'" class="form-control head_weight" style="width:80px" />';
                }
            },
            { "width": "30px",
                "render": function ( data, type, full, meta ) {
                    return '<span aligen="center">kg</span>';
                }
            },
            { "data": "PIECES", "width": "60px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="pieces" value="'+data+'" class="form-control" style="width:80px"/>';
            	}
            },
            { "data": "VOLUME", "width": "60px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="volume" value="'+data+'" class="form-control" style="width:80px"/>';
            	}
            },
            { "data": "DRIVER", "width": "60px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="driver" value="'+data+'" class="form-control driver" style="width:80px" />';
                }
            },
            { "data": "DRIVER_TEL", "width": "60px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="driver_tel" value="'+data+'" class="form-control phone" style="width:80px" />';
                }
            },
            { "data": "CONSIGNOR", "width": "60px",
            	"render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    var field_html = template('table_truck_out_template',
                        {
                            id: 'CONSIGNOR',
                            value: data,
                            display_value: full.CONSIGNOR_NAME,
                            style:'width:80px'
                        }
                    );
                    return field_html;
                }
            },
            { "data": "CONSIGNOR_PHONE","width": "60px", "className":"consigner_phone",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="consignor_phone" value="'+data+'" class="form-control" style="width:80px"/>';
            	}
            },
            
            { "data": "CONSIGNEE", "width": "60px",
            	"render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    var field_html = template('table_truck_in_template',
                        {
                            id: 'CONSIGNEE',
                            value: data,
                            display_value: full.CONSIGNEE_NAME,
                            style:'width:80px'
                        }
                    );
                    return field_html;
                }
            },
            { "data": "CONSIGNEE_PHONE","width": "60px",  "className":"consignee_phone",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="consignee_phone" value="'+data+'" class="form-control" style="width:80px"/>';
            	}
            },
            
            { "data": "CARGO_DESC", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="cargo_desc" value="'+data+'" class="form-control" style="width:200px"/>';
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
            { "data":"ID","width": "50px",
            	"render": function ( data, type, full, meta ) {
            		if(data)
	            		return '<span class="btn table_btn btn-default btn-xs fileinput-button" style="width:50px">' 
		                		+'<i class="glyphicon glyphicon-plus"></i>'
		                		+'<span>上传签收文件</span>'
		                		+'<input class="upload" type="file" multiple>'
		                		+'</span>'
		            else
		            	return '<span class="btn table_btn btn-default btn-xs fileinput-button" style="width:50px" title="请先保存再上传文件">' 
		                		+'<i class="glyphicon glyphicon-plus"></i>'
		                		+'<span>上传签收文件</span>'
		                		+'<input  class="upload" type="button" disabled>'
		                		+'</span>'			
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
            },
            { "data": "CAR_NO_NAME", "visible": false},
            { "data": "TAKE_ADDRESS_NAME", "visible": false},
            { "data": "DELIVERY_ADDRESS_NAME", "visible": false},
            { "data": "LOADING_WHARF1_NAME", "visible": false},
            { "data": "LOADING_WHARF2_NAME", "visible": false}
        ]
    });

    //默认加两行, 提空柜, 还重柜
    var addDefaultRows=function(){
        cargoTable.rows.add([
            {"UNLOAD_TYPE":"提吉柜","TRUCK_TYPE":"40HQ"},
            {"UNLOAD_TYPE":"收重柜","TRUCK_TYPE":"40HQ"}]).draw();
    };
    
    //选择客户回填装货地点1，装货地点2
	$('#customer_id_list').on('mousedown',' .fromLocationItem',function(){
		var companyId = $(this).attr('partyId');
			$.post('/transJobOrder/getCustomerQuotationAddress',{companyId:companyId},function(data){
				if(data){
					var loading_wharf1_name =$($("#land_table tr:eq(1) td").find('input[name=LOADING_WHARF1_input]'));
					var loading_wharf2_name =$($("#land_table tr:eq(1) td").find("input[name=LOADING_WHARF2_input]"));
						$('#customer_id_input').val(data.CUSTOMER_ABBR);
						$('#customer_id').val(data.PARTY_ID);
						loading_wharf1_name.val(data.LOADING_WHARF1_NAME);
						$($("#land_table tr:eq(1) td").find('input[name=LOADING_WHARF1]')).val(data.LOADING_WHARF1);
	
						loading_wharf2_name.val(data.LOADING_WHARF2_NAME);
						$($("#land_table tr:eq(1) td").find("input[name=LOADING_WHARF2]")).val(data.LOADING_WHARF2);
					
				}
			},'json').fail(function(){
				
			});
	});
    
    
    //把提柜码头和还柜码头带到table中
   $('#take_wharf_input,#back_wharf_input').click(function(){
	   if($('#take_wharf_input').val()!=''){
		   $($("#land_table tr:eq(1) td").find('input[name=TAKE_ADDRESS_input]')).val($('#take_wharf_input').val());
           $($("#land_table tr:eq(1) td").find('input[name=TAKE_ADDRESS]')).val($('#take_wharf').val());
	   }
	   if($('#back_wharf_input').val()!=''){
		   $($("#land_table tr:eq(2) td").find('input[name=DELIVERY_ADDRESS_input]')).val($('#back_wharf_input').val());
           $($("#land_table tr:eq(2) td").find('input[name=DELIVERY_ADDRESS]')).val($('#back_wharf').val());
	   }
   
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
    //选项测试
    $("#land_table").on("change",".ttype",function(){
    	self  = $(this)
    	var selected = self.val();    	
    	if(selected =="提吉柜"){
    		self.parent().siblings("td").find("[name=CLOSING_DATE]").prop("disabled",true);
    		$((self.parent().siblings("td").find("[name=CLOSING_DATE_div]"))).find('.fa').hide();
    		self.parent().siblings("td").find("[name=CABINET_DATE]").prop("disabled",false);
    		$((self.parent().siblings("td").find("[name=CABINET_DATE_div]"))).find('.fa').show();
    	}else if(selected == "收重柜"){
    		self.parent().siblings("td").find("[name=CABINET_DATE]").prop("disabled",true);
    		$((self.parent().siblings("td").find("[name=CABINET_DATE_div]"))).find('.fa').hide();
    		self.parent().siblings("td").find("[name=CLOSING_DATE]").prop("disabled",false);
    		$((self.parent().siblings("td").find("[name=CLOSING_DATE_div]"))).find('.fa').show();
    		
    	}else if(selected == "移柜"){
    		self.parent().siblings("td").find("[name=CABINET_DATE]").prop("disabled",true);
    		self.parent().siblings("td").find("[name=CLOSING_DATE]").prop("disabled",true);
    		$((self.parent().siblings("td").find("[name=CABINET_DATE_div]"))).find('.fa').hide();
    		$((self.parent().siblings("td").find("[name=CLOSING_DATE_div]"))).find('.fa').hide();
    	}else{
    		self.parent().siblings("td").find("[name=CABINET_DATE]").prop("disabled",false);
    		$((self.parent().siblings("td").find("[name=CABINET_DATE_div]"))).find('.fa').show();
    		self.parent().siblings("td").find("[name=CLOSING_DATE]").prop("disabled",false);
    		$((self.parent().siblings("td").find("[name=CLOSING_DATE_div]"))).find('.fa').show();
    	}
    
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
    
    $('#land_table').on("blur","[name=loading_platform],[name=toca_weight],[name=head_weight],[name=pieces],[name=volume],"
			   +"[name=driver],[name=driver_tel],[name=consignor_phone],[name=consignee_phone],[name=cargo_desc],[name=required_time_remark]",function(){
		var data = $(this).val();
		var len = $.trim(data).length;
		var name = $(this).attr("name");
		if(name=="loading_platform"||name=="car_no"||name=="land_seal_no"||name=="land_container_no"){
			if(len>200){
				$(this).parent().append("<span style='color:red' class='error_span'>请输入长度200以内的字符串</span>")
				return;
			}
		}
		if(name=="toca_weight"||name=="head_weight"){
			if(len>45){
				$(this).parent().append("<span style='color:red' class='error_span'>请输入长度45以内的字符串</span>")
				return;
			}
		}
		if(name=="pieces"){
			if(len>30){
				$(this).parent().append("<span style='color:red' class='error_span'>请输入长度50以内的字符串</span>")
				return;
			}
		}
		if(name=="cargo_desc"||name=="required_time_remark"){
			if(len>255){
				$(this).parent().append("<span style='color:red' class='error_span'>请输入长度255以内的字符串</span>")
				return;
			}
		}
		if(name=="driver"){
			if(len>100){
				$(this).parent().append("<span style='color:red' class='error_span'>请输入长度100以内的字符串</span>")
				return;
			}
		}
		if(name=="driver_tel"||name=="consignor_phone"||name=="consignee_phone"||name=="volume"||name=="sign_status"){
			if(len>20){
				$(this).parent().append("<span style='color:red' class='error_span'>请输入长度20以内的字符串</span>")
				return;
			}
		}
	});
    $('#land_table').on("focus","[name=loading_platform],[name=toca_weight],[name=head_weight],[name=pieces],"
			   +"[name=driver],[name=driver_tel],[name=consignor_phone],[name=consignee_phone],[name=cargo_desc],[name=required_time_remark]",function(){
		$(this).parent().find("span").remove();
	});
});
});