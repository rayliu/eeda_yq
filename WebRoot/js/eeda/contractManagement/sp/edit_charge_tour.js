define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
$(document).ready(function() {

	var deletedTableIds=[];
    //删除一行
    $("#charge_tour_table").on('click', '.delete', function(){
        var tr = $(this).parent().parent();
        tr.css("display","none");
        deletedTableIds.push(tr.attr('id'))
    });

    //删除location 一行
    var deletedLoactionTableIds=[];

    $("#tour_location_table").on('click', '.delete', function(){
        var tr = $(this).parent().parent();
        tr.css("display","none");
        deletedLoactionTableIds.push(tr.attr('id'))
    }); 
    
    //注意使用通用的方法 buildTableDetail
    itemOrder.buildTourItem=function(){
        var items = eeda.buildTableDetail("charge_tour_table", deletedTableIds);
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            item.contract_type = "tour";
        }
        deletedTableIds = [];
        return items;
    };
    
    
    itemOrder.buildTourLocItem=function(){
    	var table_id = 'tour_location_table';
    	var deletedTableIds = deletedLoactionTableIds;

    	var item_table_rows = $("#"+table_id+" tr:visible");
        var items_array=[];
        for(var index=0; index<item_table_rows.length; index++){
            if(index==0)
                continue;

            var row = item_table_rows[index];
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
            	if(name == 'checkRoute'){
            		var check = $($(row.childNodes[i]).find('input')).prop('checked');
                 	if(check){
                 		item.is_select = 'Y';
                 	}else{
                 		item.is_select = 'N';
                 	}
            	}else{
                    var value = $(row.childNodes[i]).find('input,select').val();
                    if(name){
                        item[name] = value;
                    }
            	}
            }
            item.action = id.length > 0?'UPDATE':'CREATE';
            item.type = "tour_loc";
    		if(item_table_rows.length == 2){
            	item.is_select = 'Y';
            }
    		
            items_array.push(item);
        }

        //add deleted items
        if(deletedTableIds!=''){
        	
        	for(var index=0; index<deletedTableIds.length; index++){
        		var id = deletedTableIds[index];
        		var item={
        				id: id,
        				action: 'DELETE'
        		};
        		items_array.push(item);
        	}
        	deletedLoactionTableIds = [];
        }
        return items_array;
    };


    var bindFieldEvent=function(){
        eeda.bindTableFieldChargeId('charge_tour_table','FEE_ID','/finItem/search','');
        eeda.bindTableFieldCurrencyId('charge_tour_table','CURRENCY_ID','/serviceProvider/searchCurrency','');
        eeda.bindTableField('charge_tour_table','UOM','/serviceProvider/searchChargeUnit','');
    };
    //------------事件处理
    var cargoTable = eeda.dt({
	    id: 'charge_tour_table',
	    autoWidth: false,
	    drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
	        bindFieldEvent();
	    },
	    columns:[
            {  "width": "30px",
                "render": function ( data, type, full, meta ) {
                   
                    return '<button type="button" class="delete btn table_btn delete_btn btn-xs" ><i class="fa fa-trash-o"></i> 删除</button></button>';
                }
            },
            { "data": "FEE_ID", "width": "80px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    var field_html = template('table_dropdown_template',
                    {
                    	id:'FEE_ID',
                    	value:data,
                    	display_value:full.FEE_NAME,
                    	style:'width:100px'
                    	
                    });
                    return field_html; 
                }
            },
            { "data": "PRICE", "width": "80px",
                "render": function ( data, type, full, meta ) {
                	 if(!data)
	                        data='';
	                    data = (parseFloat(data)).toFixed(2)
	                    if(isNaN(data)){
	                    	data=""
	                    }
	                    return '<input type="text" style="width:100px" name="price" value = "'+data+'" class="form-control notsave" >';
                }
            },
            { "data": "CURRENCY_ID", "width": "50px",
            	"render": function ( data, type, full, meta ) {
            		 if(!data)
	                        data='';
	                    var field_html = template('table_dropdown_template',
		                    {
		                        id: 'CURRENCY_ID',
		                        value: data,
		                        display_value:full.CURRENCY_NAME,
		                        style:'width:70px'
		                    }
		                );
	                    return field_html;
            	}
            },
            { "data": "UOM", "width": "80px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		var field_html = template('table_dropdown_template',
        				{
		            			id: 'UOM',
		                        value: data,
		                        display_value:full.UOM_NAME,
		                        style:'width:100px'
        				});
            		return field_html;
            	}
            },
            { "data": "TRUCK_TYPE", "width": "70px",
                "render": function ( data, type, full, meta ) {
                	if(!data)
                        data='';
                   var field_html = template('table_truck_type_field_template',
	                    {
	                        id: 'TRUCK_TYPE',
	                        value: data,
                            style:"width:90px"
	                    }
	                );
                    return field_html;
                }
            },
            { "data": "FEE_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            }, 
            { "data": "CURRENCY_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            },
            { "data": "UOM_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            }
        ]
    });

    $('#add_tour_charge_fee').on('click', function(){
        var item={};
        cargoTable.row.add(item).draw(true);
    });
    
    //刷新明细表
    itemOrder.refleshTourItemTable = function(contract_id){
    	var url = "/supplierContract/tableList?contract_id="+contract_id+"&type=tour";
    	cargoTable.ajax.url(url).load();
    }
    
    //刷新明细表
    itemOrder.refleshTourLocTable = function(contract_id){
    	var url = "/supplierContract/tableList?contract_id="+contract_id+"&type=tour_loc";
    	locationTable.ajax.url(url).load();
    }

        var bindLocationFieldEvent=function(){
            eeda.bindTableFieldDockInfo('tour_location_table','POL_ID');
            eeda.bindTableFieldDockInfo('tour_location_table','POD_ID');
        };

        //------------事件处理
        var locationTable = eeda.dt({
            id: 'tour_location_table',
            autoWidth: false,
            paging: false,
            info: false,
            drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
                bindLocationFieldEvent();
            },
            columns:[
				{"data": "IS_SELECT",  
					"width": "30px",
				    "render": function ( data, type, full, meta ) {
				    	var select = "";
				    	if(data == 'Y'){
				    		select = "checked";
				    	}
				        return '<input type="radio" '+select+' name="checkRoute" style="margin-right:20px;" />';
				    }
				},
                {  "width": "50px",
                    "render": function ( data, type, full, meta ) {
                        return '<button type="button" class="delete btn table_btn delete_btn btn-xs" ><i class="fa fa-trash-o"></i> 删除</button></button>';
                    }
                },
                { "data": "POL_ID", "width":"130px",
                    "render": function ( data, type, full, meta ) {
                    if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'POL_ID',
                               value: data,
                               display_value: full.POL_NAME,
                               style:'width:150px'
                           }
                       );
                       return field_html; 
                    }
                },
                { "data": "POD_ID", "width": "130px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        var field_html = template('table_dropdown_template',
                        {
                         id:'POD_ID',
                         value:data,
                         display_value:full.POD_NAME,
                         style:'width:150px'
                        });
                        return field_html; 
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
                }
            ]
        });

        $('#add_tour_location').on('click', function(){
            var item={};
            locationTable.row.add(item).draw(true);
        });
        
        
        //根据radioButton显示对应路线费用
        $('#tour_location_table').on('click','[name=checkRoute]', function(){
        	var self = this;
        	var order_id = $('#contract_id').val();
        	if(order_id == '')
        		return;
            var item_id = $(this).parent().parent().attr('id');
            if(item_id==undefined){
            	item_id = "";
            }
            var url = "/supplierContract/clickItem?contract_id="+order_id+"&type=tour&supplier_loc_id="+item_id;
        	cargoTable.ajax.url(url).load();
        });
        
        //园区游页面校验
        $('#charge_tour_table').on('blur','[name=price]',function(){
        	var data = $(this).val();
        	var len = $.trim(data).length;
        	var re = /^\d{0,9}(\.\d{1,5})?$/;
        	if(!re.test(data)&&len>0||len>15&&len>0){
        		$(this).parent().append("<span style='color:red;display:block;' class='error_span'>请输入合法数字</span>")
        	}
        });
        $('#charge_tour_table').on('focus','[name=price]',function(){
        	$(this).parent().find("span").remove();
        });
});
});