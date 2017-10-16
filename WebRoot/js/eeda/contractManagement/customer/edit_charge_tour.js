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
        return items;
    };

    itemOrder.buildTourLocItem=function(){
        var items = eeda.buildTableDetail("tour_location_table", deletedLoactionTableIds);
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            item.type = "tour_loc";
        }
        return items;
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
    itemOrder.refleshLandItemTable = function(contract_id){
    	var url = "/customerContract/tableList?contract_id="+contract_id+"&type=tour";
    	cargoTable.ajax.url(url).load();
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
        
        //加工贸易页面校验
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