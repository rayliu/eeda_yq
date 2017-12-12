define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'dtColReorder'], function ($, metisMenu, template) {
$(document).ready(function() {

    var order_id = $('#order_id').val();
	var deletedTableIds=[];
	var land_ids_true = [];
	var land_ids_false = [];
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
        var customer=$('#customer_id_input').val().trim();
        if(customer!=''&&customer!=null){
         	$.get('/serviceProvider/searchTruckOut', {input:customer}, function(data){
         		if(data.length>0){
         			item={"CONSIGNOR_NAME":data[0].NAME,"CONSIGNOR":data[0].ID
         				,"CONSIGNOR_PHONE":data[0].PHONE,"TAKE_ADDRESS":data[0].ADDRESS
         				,"CONSIGNOR_CONTACT_MAN":data[0].CONTACT_PERSON
         			};
         		}
            	 cargoTable.row.add(item).draw(true);
         	});
        }else{
        	 $.scojs_message('请先选择客户', $.scojs_message.TYPE_ERROR);
        	 return;
        }
       
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
            item.land_type = "bulk_car";
            item.status = '待发车';//默认待发车
            for(var i = 1; i < row.childNodes.length; i++){
            	var el = $(row.childNodes[i]).find('input,select');
            	var name = el.attr('name'); //name='abc'
            	
            	if(el && name){
                	var value = el.val();//元素的值
                	item[name] = value;
                	if(name=='take_address_id'){
                		if(value==undefined){
                			item[name] = "";
                		}
                		value = $(row.childNodes[i]).parent().find('[name=TAKE_ADDRESS]').attr('loc_type');
                		item['take_address_type'] = value;
                	}else if(name=='delivery_address_id'){
                		if(value==undefined){
                			item[name] = "";
                		}
                		value = $(row.childNodes[i]).parent().find('[name=DELIVERY_ADDRESS]').attr('loc_type');
                		item['delivery_address_type'] = value;
                	}
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
        eeda.bindTableField('land_table','UNIT_ID','/serviceProvider/searchUnit','');
        
        eeda.bindTableAddressField('land_table', 'TAKE_ADDRESS','/serviceProvider/searchTruckOut','CONSIGNOR');
        eeda.bindTableAddressField('land_table', 'DELIVERY_ADDRESS','/serviceProvider/searchTruckOut','CONSIGNEE');
//        eeda.bindTableLocationField('land_table','ROUTE_FROM');
//        eeda.bindTableLocationField('land_table','ROUTE_TO');
    };
    //------------事件处理
	 var cargoTable = eeda.dt({
	        id: 'land_table',
            colReorder: true,
	        autoWidth: true,
	        drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
	        	bindFieldEvent();
	        	huidiao();
	        },
	        columns:[
			{ "data":"ID","width": "20px",
			    "render": function ( data, type, full, meta ) {
			    	if(data)
			    		return '<input type="checkbox" class="checkBox" name="checkBox" style="width:20px">';
			    	else 
			    		return '<input type="checkbox" style="width:20px" disabled>';
			    }
			},
            { "data":"TAKE_ADDRESS_ID","width": "40px","className":"consignor_contact_id",
                "render": function ( data, type, full, meta ) {
                	if(full.SUBMIT_FLAG=="Y"){
                		land_ids_true.push(full.ID);
                	}
                	if(full.APPROVAL_UPDATE=="Y"){
                		land_ids_false.push(full.ID);
                	}
                	if(!data){
                		return '<button type="button" style="width:60px" name="delete" dockId class="delete btn table_btn delete_btn btn-xs" >删除</button>'
             		   +'<input type="text" name="take_address_id" loc_type="'+full.TAKE_ADDRESS_TYPE+'" style="display:none;"/>';
                	}else{
                		return '<button type="button" style="width:60px" name="delete" dockId class="delete btn table_btn delete_btn btn-xs" >删除</button>'
             		   +'<input type="text" name="take_address_id" value="'+data+'" loc_type="'+full.TAKE_ADDRESS_TYPE+'" style="display:none;"/>';
                	}
                	
                }
            },
            { "data":"DELIVERY_ADDRESS_ID","width": "40px","className":"consignee_contact_id",
            	"render": function ( data, type, full, meta ) {
            		if(!data){
            			if(full.ID){
                			return '<button type="button" style="width:60px" name="land_charge" class="land_charge btn table_btn btn_green btn-xs" >费用</button>'
                					+'<input type="hidden" name="delivery_address_id" loc_type="'+full.DELIVERY_ADDRESS_TYPE+'"  />';	
                		}else{
                			return '<button type="button" style="width:60px" class="land_charge btn table_btn btn_green btn-xs"  disabled>费用</button>'
                			+'<input type="hidden" name="delivery_address_id" loc_type="'+full.DELIVERY_ADDRESS_TYPE+'"  />';
                		}
            		}else{
            			if(full.ID){
                			return '<button type="button" style="width:60px" name="land_charge" class="land_charge btn table_btn btn_green btn-xs" >费用</button>'
                					+'<input type="hidden" name="delivery_address_id" loc_type="'+full.DELIVERY_ADDRESS_TYPE+'" value="'+data+'" />';	
                		}else{
                			return '<button type="button" style="width:60px" class="land_charge btn table_btn btn_green btn-xs"  disabled>费用</button>'
                			+'<input type="hidden" name="delivery_address_id" loc_type="'+full.DELIVERY_ADDRESS_TYPE+'" value="'+data+'" />';
                		}
            		}
            		
            	}
            },
            
            { "data": "DOC_NAME","width": "10px",
                "render": function ( data, type, full, meta ) {
                	if(data)
                		return '<button type="button" class="btn btn-default btn-xs delete_sign_desc" style="width:10px">删除签收文件</button>';
                	else 
                		return '';
                }
            },
           
            { "data": "UNLOAD_TYPE", "width": "90px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    var str= '<select name="unload_type" class="form-control search-control"  style="width:110px;">'
            	   	 		   +'<option></option>'
			                   +'<option value="卸货" '+ (data=='卸货'?'selected':'') +'>卸货</option>'
			                   +'<option value="收货" '+ (data=='收货'?'selected':'') +'>收货</option>'
			                   +'<option value="收卸货" '+ (data=='收卸货'?'selected':'') +'>收卸货</option>'
			                   +'</select>';
                    return str;
                }
            },
            { "data": "TRANSPORT_COMPANY", "width": "100px",
                "render": function ( data, type, full, meta ) {
                	if(!data)
                        data='';
                    var field_html = template('table_dropdown_template',
                            {
                                id: 'TRANSPORT_COMPANY',
                                value: data,
                                ref_office_id:full.REF_OFFICE_ID,
                                display_value: full.TRANSPORT_COMPANY_NAME,
                                style:'width:120px'
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
                            style:"width:80px"
	                    }
	                );
                    return field_html;
                }
            },
            { "data": "ETA", "width": "130px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
                        data='';
                    var field_html = template('table_date_field_template',
	                    {
	                        id: 'ETA',
	                        value: data.substr(0,19),
	                        style:'width:150px'
	                    }
	                );
                    return field_html;
            	}
            },
            { "data": "CONSIGNOR", "width": "80px",
            	"render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    var field_html = template('table_truck_out_template',
                        {
                            id: 'CONSIGNOR',
                            value: data,
                            display_value: full.CONSIGNOR_NAME,
                            style:'width:100px'
                        }
                    );
                    return field_html;
                }
            },
            { "data": "TAKE_ADDRESS", "width": "180px", "className":"consignor_addr",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		var field_html = template('table_address_template',
                            {
                                id: 'TAKE_ADDRESS',
                                value: data,
                                style:'width:200px',
                                display_value: full.TAKE_ADDRESS
                            }
                        );
                        return field_html;
            	}
            },
            
            { "data": "CONSIGNOR_CONTACT_MAN","width": "80px", "className":"consignor_contact_man",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="consignor_contact_man" value="'+data+'" class="form-control" style="width:100px"/>';
            	}
            },
            
            { "data": "CONSIGNOR_PHONE","width": "80px", "className":"consignor_phone",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="consignor_phone" value="'+data+'" class="form-control" style="width:100px"/>'
            		+'<input type="hidden" name="take_address_id" value=""/><input type="hidden" name="delivery_address_id" value=""/>';
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
            { "data": "DELIVERY_ADDRESS", "width": "180px", "className":"consignee_addr",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		var field_html = template('table_address_template',
                            {
                                id: 'DELIVERY_ADDRESS',
                                value: data,
                                style:'width:200px',
                                display_value: full.DELIVERY_ADDRESS
                            }
                        );
                        return field_html;
            	}
            },
            
            { "data": "CONSIGNEE_CONTACT_MAN","width": "80px",  "className":"consignee_contact_man",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="consignee_contact_man" value="'+data+'" class="form-control" style="width:100px"/>';
            	}
            },
            
            { "data": "CONSIGNEE_PHONE","width": "100px",  "className":"consignee_phone",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="consignee_phone" value="'+data+'" class="form-control" style="width:120px"/>';
            	}
            },
            { "data": "REACH_WHARF_TIME", "width": "130px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
                        data='';
                    var field_html = template('table_date_field_template',
	                    {
	                        id: 'REACH_WHARF_TIME',
	                        value: data.substr(0,19),
	                        style:'width:150px'
	                    }
	                );
                    return field_html;
            	}
            },
            { "data": "CAR_NO", "width": "80px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="car_no" value="'+data+'" class="form-control" style="width:100px"/>';
            	}
            },
            { "data": "CARGO_DESC", "width": "160px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="cargo_desc" value="'+data+'" class="form-control" style="width:180px"/>';
            	}
            },
            { "data": "LAND_CONTAINER_TYPE","width": "50px", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    var str = '<select name="land_container_type" class="form-control search-control" style="width:70px">'
                    			+'<option></option>'
			                   +'<option value="20\'GP" '+(data=='20\'GP'||data=='20GP' ? 'selected':'')+'>20GP</option>'
			                   +'<option value="40\'GP" '+(data=='40\'GP'||data=='40GP' ? 'selected':'')+'>40GP</option>'
                               +'<option value="40\'HQ" '+(data=='40\'HQ'||data=='40HQ' ? 'selected':'')+'>40HQ</option>'
			                   +'<option value="45\'GP" '+(data=='45\'GP'||data=='25GP' ? 'selected':'')+'>45GP</option>'
			                   +'</select>';
                    return str;
                }
            },
            { "data": "LAND_CONTAINER_NO","width": "80px",  
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="land_container_no" value="'+data+'" class="form-control" style="width:100px"/>';
                }
            },
            { "data": "LAND_SEAL_NO","width": "80px",  
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="land_seal_no" value="'+data+'" class="form-control" style="width:100px"/>';
                }
            },
            { "data": "PIECES", "width": "60px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="pieces" value="'+data+'" class="form-control" style="width:80px"/>';
            	}
            },
            { "data": "UNIT_ID", "width": "60px",
                "render": function ( data, type, full, meta ) {
                	if(!data)
                        data='';
                    var field_html = template('table_dropdown_template',
                        {
                            id: 'UNIT_ID',
                            value: data,
                            display_value: full.UNIT_NAME,
                            style:'width:100px'
                        }
                    );
                    return field_html;
                }
            },
            { "data": "GROSS_WEIGHT", "width": "60px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="gross_weight" value="'+data+'" class="form-control" style="width:80px"/>';
            	}
            },
            { "data": "VOLUME", "width": "70px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="volume" value="'+data+'" class="form-control" style="width:90px"/>';
            	}
            },
            { "data": "REQUIRED_TIME_REMARK", "width": "110px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="required_time_remark" value="'+data+'" class="form-control" style="width:130px"/>';
            	}
            },
            { "data": "TRANS_NO", "width": "80px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="trans_no" value="'+data+'" class="form-control" style="width:100px" />';
                }
            },
            { "data": "TRANS_INFO", "width": "80px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="trans_info" value="'+data+'" class="form-control" style="width:100px" />';
                }
            },
            /*{ "data": "DRIVER", "width": "60px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="driver" value="'+data+'" class="form-control" style="width:80px" />';
                }
            },
            { "data": "DRIVER_TEL", "width": "80px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="driver_tel" value="'+data+'" class="form-control" style="width:100px" />';
                }
            },*/
            { "data":"ID","width": "80px",
            	"render": function ( data, type, full, meta ) {
            		if(data)
	            		return '<span class="btn table_btn btn-success btn-xs fileinput-button " style="100px" >' 
		                		+'<i class="glyphicon glyphicon-plus"></i>'
		                		+'<span>上传签收文件</span>'
		                		+'<input class="upload" type="file" multiple>'
		                		+'</span>'
		            else
		            	return '<span class="btn table_btn btn-default btn-xs fileinput-button" style="100px" title="请先保存再上传文件" disabled>' 
		                		+'<i class="glyphicon glyphicon-plus"></i>'
		                		+'<span>上传签收文件</span>'
		                		+'<input  class=" upload" type="button" disabled>'
		                		+'</span>'			
            	}
            },
            { "data": "DOC_NAME", "width": "100px",
            	"render": function ( data, type, full, meta ) {
            		if(!data){
            			return '<span style="width:120px;"><span/>';
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
	            		return '<span style="width:120px;" >'+str+'</span>';
            		}
            	}
            },
            { "data": "SIGN_STATUS", "width": "60px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="sign_status" value="'+data+'" class="form-control" style="width:80px"/>';
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
            { "data": "SUBMIT_FLAG", "visible": false,
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return data;
            	}
            },
            { "data": "APPROVAL_UPDATE", "visible": false,
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return data;
            	}
            },
            { "data": "TAKE_ADDRESS_TYPE", "visible": false,
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return data;
            	}
            },
            { "data": "DELIVERY_ADDRESS_TYPE", "visible": false,
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return data;
            	}
            },
            { "data": "REF_OFFICE_ID", "visible": false,
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return data;
            	}
            }
        ]
    });
	 
	//司机、车牌、柜号、封条号、货品描述、运输时间及要求、运单号、签收状态校验
    $('#land_table').on("blur","[name=driver],[name=car_no],[name=land_seal_no],[name=land_container_no],"
    				   +"[name=cargo_desc],[name=required_time_remark],[name=trans_no],[name=sign_status]",function(){
		self = $(this);
		data = self.val();
		len = $.trim(data).length;
		var name = self.attr("name");
		if(name=="driver"||name=="car_no"||name=="land_seal_no"||name=="land_container_no"){
			var re = /^.{100,}$/g;
			if(re.test(data)&&len!=0){
				self.parent().append("<span style='color:red' class='error_span'>请输入长度100以内的字符串</span>")
			}
		}
		if(name=="cargo_desc"||name=="required_time_remark"){
			var re = /^.{255,}$/g;
			if(re.test(data)&&len!=0){
				self.parent().append("<span style='color:red' class='error_span'>请输入长度255以内的字符串</span>")
			}
		}
		if(name=="trans_no"){
			var re = /^.{50,}$/g;
			if(re.test(data)&&len!=0){
				self.parent().append("<span style='color:red' class='error_span'>请输入长度50以内的字符串</span>")
			}
		}
		if(name=="sign_status"){
			var re = /^.{20,}$/g;
			if(re.test(data)&&len!=0){
				self.parent().append("<span style='color:red' class='error_span'>请输入长度20以内的字符串</span>")
			}
		}
	});

    //发货人电话，收货人电话，司机电话校验
	$('#land_table').on("blur","[name=driver_tel],[name=consignor_phone],[name=consignee_phone]",function(){
		self = $(this)
		data = self.val()
		data = $.trim(data)
		var mobile = /^((1[34578]\d{9})|(0\d{2,3}-\d{7,8}))$/;
		
		if(!mobile.test(data)&&data!=''){   
			self.parent().append("<span style='color:red' class='error_span'>请输入正确的电话或者手机号码</span>")
		}
	})
	//件数、毛重、体积校验
	$('#land_table').on("blur","[name=pieces],[name=gross_weight],[name=volume]",function(){
		self = $(this)
		data = self.val()
		data = $.trim(data)
		if(isNaN(parseFloat(data))&&data!=''){   
			self.parent().append("<span style='color:red' class='error_span'>请输入合法数字</span>")
		}
	})
		
	$('#land_table').on("focus","[name=driver_tel],[name=consignor_phone],[name=pieces],[name=gross_weight],"
						+"[name=volume],[name=consignee_phone],[name=driver],[name=car_no],[name=land_seal_no]," 
						+"[name=land_container_no],[name=cargo_desc],[name=required_time_remark],[name=trans_no]",function(){
    		self = $(this)
    		self.parent().find("span").remove()
    })
	    
	 

    //刷新明细表
    itemOrder.refleshLandItemTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=land";
    	cargoTable.ajax.url(url).load();
    }
    
  //checkbox选中则button可点击
    $('#land_table').on('click','input[type="checkbox"]',function(){
    	var hava_check = 0;
    	$('#land_table input[type="checkbox"]:checked').each(function(){	
    		hava_check++;
    	})
    	if(hava_check>0){
    		$('#truckOrderPDF').attr('disabled',false);
	    	$('#cabinet_truck').attr('disabled',false);
	    	$('#cabinet_truck').attr('disabled',false);
	    	$('#cabinet_truckMBL').attr('disabled',false);
	    	$("#submitBtn").attr("disabled",false);
        	$("#updateApplyBtn").attr("disabled",false);
    	}else{
    		$('#truckOrderPDF').attr('disabled',true);
	    	$('#cabinet_truck').attr('disabled',true);
	    	$('#cabinet_truckMBL').attr('disabled',true);
	    	$('#land_print_debit_note').attr('disabled',true);
	    	$("#submitBtn").attr("disabled",true);
        	$("#updateApplyBtn").attr("disabled",true);
    	}
    });
    
    //全选
    $('#allCheckOfLand').click(function(){
	    $("#land_table .checkBox").prop("checked",this.checked);
	    var hava_check = 0;
    	$('#land_table input[type="checkbox"]:checked').each(function(){	
    		hava_check++;
    	})
	    if(this.checked==true&&$('#land_table td').length>1&&hava_check>0){
	    	$('#truckOrderPDF').attr('disabled',false);
	    	$('#cabinet_truck').attr('disabled',false);
	    	$('#cabinet_truckMBL').attr('disabled',false);
	    	$('#land_print_debit_note').attr('disabled',false);
	    	$("#submitBtn").attr("disabled",false);
        	$("#updateApplyBtn").attr("disabled",false);
	    }else{
	    	$('#truckOrderPDF').attr('disabled',true);
	    	 $('#cabinet_truck').attr('disabled',true);
	    	 $('#cabinet_truckMBL').attr('disabled',true);
	    	$('#land_print_debit_note').attr('disabled',true);
	    	$("#submitBtn").attr("disabled",true);
        	$("#updateApplyBtn").attr("disabled",true);
	    }
    });
    
    $("#land_table").on('click','.checkBox',function(){
		  $("#allCheckOfLand").prop("checked",$("#land_table .checkBox").length == $("#land_table .checkBox:checked").length ? true : false);
    });
    
    //一起删除签收文件
    $("#land_table").on('click', '.delete_sign_desc', function(){
    	var tr = $(this).parent().parent();
    	var id = tr.attr('id');
    	var order_id = $('#order_id').val();
	     $.post('/jobOrder/deleteSignDesc', {id:id}, function(data){
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
	     $.post('/jobOrder/deleteOneSignDesc', {id:id,name:name}, function(data){
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
			    url: '/jobOrder/uploadSignDesc?id='+id,
			    dataType: 'json',
		        done: function (e, data) {
                    if(data.result.result){
    		    		$.scojs_message('上传成功', $.scojs_message.TYPE_OK);
    		    		itemOrder.refleshLandItemTable(order_id);
                    }else{
                        $.scojs_message('上传失败:'+data.result.ERRMSG, $.scojs_message.TYPE_ERROR);
                    }
			     },
		        error: function () {
		            alert('上传的时候出现了错误！');
		        }
			});
	});
    
    var self;
    $('#land_table').on('click','[name=required_time_remark]',function(){
    	self = $(this);
    	$('#land_showNote').val(self.val());
    	$('#land_showNote_btn').click();
    });
    $('#land_btnConfirm').click(function(){
		var showNote = $('#land_showNote').val();
		self.val(showNote);
	});
    
    var transInfo;
    $('#land_table').on('click','[name=trans_info]',function(){
    	transInfo = $(this);
    	$('#transInfo_showNote').val(transInfo.val());
    	$('#transInfo_btn').click();
    });
    $('#transInfo_btnConfirm').click(function(){
		var showNote = $('#transInfo_showNote').val();
		transInfo.val(showNote);
	});
    
    if(truck_type_hidden!=null && truck_type_hidden!=""){
    	var arrays = truck_type_hidden.split(",");
        var landtable = $('#land_table').DataTable();
        for(var i = 0; i < arrays.length; i++){
        	
        	var array = arrays[i].split("X");
        	var type = array[0];
        	var number = array[1];
        	for(var j = 0; j < number; j++){
        		var item={};
        		item.TRUCK_TYPE = type;
        		landtable.row.add(item).draw();
        	}
        };
    };
    //so号、mbl号的显示和隐藏
    $('#cabinet_truck_detail input[type="radio"]').change(function(){
     	var type = $(this).val();
     	if(type == 'so_no'){
     		$('#SONOLand').show()
     		$('#mblNoLand').hide()
     	}else if(type == 'mbl_no'){
     		$('#SONOLand').hide()
     		$('#mblNoLand').show()
     	}
     });
    
    //提交按钮单击事件
    $("#submitBtn").click(function(){
    	$("#allCheckOfLand").attr("disabled",true);
    	var ref_office_id = $("#TRANSPORT_COMPANY_input").attr("ref_office_id");
    	if(ref_office_id==undefined||ref_office_id==null){
    		$.scojs_message('不能提交给该公司', $.scojs_message.TYPE_ERROR);
    	}else{
    		var order = {};
        	order.order_id = $("#order_id").val();
        	order.customer_id = $("#customer_id").val();
        	order.type = $("#type").val();
        	order.trade_type = $("#trade_type").val();
        	order.order_no = $("#order_no").val();
        	order.landList = land_item();
    		$.post("/jobOrder/landSubmit",{params:JSON.stringify(order)},function(data){
        		if(data.RESULT){
        			$.scojs_message('提交成功', $.scojs_message.TYPE_OK);
        		}else if(data.error){
            		var error = "（"+data.error+"）";
                    $.scojs_message('提交失败'+error, $.scojs_message.TYPE_ERROR);
        			relieve();
        		}
        	},'json').fail(function() {
                $.scojs_message('提交失败', $.scojs_message.TYPE_ERROR);
                relieve();
            });
    	}
    });
    
    //修改申请单击事件
    $("#updateApplyBtn").click(function(){
    	var land_item_ids = [];
    	$('#land_table input[type="checkbox"]:checked').each(function(){
    		var row_id = $(this).parent().parent().attr("id");
    		land_item_ids.push(row_id);
    	});
    	$.post("/jobOrder/updateApply",{land_item_ids:land_item_ids.toString()},function(data){
    		if(data){
    			$.scojs_message('发送请求成功', $.scojs_message.TYPE_OK);
    		}else{
    			$.scojs_message('发送请求失败', $.scojs_message.TYPE_ERROR);
    		}
    	});
    });
    
    //循环获取td还有禁用td
    var land_item = function(){
    	var land_item = [];
    	$('#land_table input[type="checkbox"]:checked').each(function(){
    		var row_id = $(this).parent().parent().attr("id");
    		var item={}
    		item.id = row_id;
    		$("#land_table tr[id='"+row_id+"'] td").each(function(){
    			var name = $(this).children().attr("name");
    			if(name=='delete'){
    				item['take_address_id'] = $(this).find("input").val()==null?"":$(this).find("input").val();
    				item['take_address_type'] = $(this).find("input").attr("loc_type");
    			}else if(name=='land_charge'){
    				item['delivery_address_id'] = $(this).find("input").val()==null?"":$(this).find("input").val();
    				item['delivery_address_type'] = $(this).find("input").attr("loc_type");
    			}else{
    				item[name] = $(this).children().val();
    			}
    			if(name!='checkBox'){
    				$(this).children().attr("disabled",true);
    			}
    		});
        	$("#land_table tr[id='"+row_id+"'] td div input").each(function(){
        		var name = $(this).attr("name");
        		item[name] = $(this).val();
        		$(this).attr("disabled",true);
        	});
    		
			land_item.push(item);
    	});
    	return land_item;
    }
    var huidiao = function(){
    	 //刷新时，控制td
        if(land_ids_true.length>0){
        	for(var i = 0;i<land_ids_true.length;i++){
        		var row_id = land_ids_true[i];
        		$("#land_table tr[id='"+row_id+"'] td").each(function(){
            		var name = $(this).children().attr("name");
            		if(name!='checkBox'&&name!='trans_info'){
            			$(this).children().attr("disabled",true);
            		}
            	});
            	$("#land_table tr[id='"+row_id+"'] td div input").each(function(){
            		var name = $(this).attr("name");
            		$(this).attr("disabled",true);
            	});
        	}
        }
        //刷新时，控制td
        if(land_ids_false.length>0){
        	for(var i = 0;i<land_ids_false.length;i++){
        		var row_id = land_ids_false[i];
        		$("#land_table tr[id='"+row_id+"'] td").each(function(){
            		var name = $(this).children().attr("name");
            		if(name!='checkBox'){
            			$(this).children().attr("disabled",false);
            		}
            	});
            	$("#land_table tr[id='"+row_id+"'] td div input").each(function(){
            		var name = $(this).attr("name");
            		$(this).attr("disabled",false);
            	});
        	}
        }
    }
    
    var relieve = function(){
    	$('#land_table input[type="checkbox"]:checked').each(function(){
    		var row_id = $(this).parent().parent().attr("id");
    		$("#land_table tr[id='"+row_id+"'] td").each(function(){
    			var name = $(this).children().attr("name");
    			if(name!='checkBox'){
    				$(this).children().attr("disabled",false);
    			}
    		});
        	$("#land_table tr[id='"+row_id+"'] td div input").each(function(){
        		var name = $(this).attr("name");
        		$(this).attr("disabled",false);
        	});
    	});
    }
	});
});