define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
$(document).ready(function() {

	var deletedTableIds=[];
    //删除一行
    $("#charge_land_table").on('click', '.delete', function(){
        var tr = $(this).parent().parent();
        tr.css("display","none");
        deletedTableIds.push(tr.attr('id'))
    }); 
    
    itemOrder.buildLandItem=function(){
        var cargo_table_rows = $("#charge_land_table tr");
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
            item.contract_type = "land";
            for(var i = 1; i < row.childNodes.length; i++){
            	var el = $(row.childNodes[i]).find('input, select');
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
    	
        eeda.bindTableFieldDockInfo('charge_land_table','POL_ID');
        eeda.bindTableFieldDockInfo('charge_land_table','POD_ID');
        eeda.bindTableFieldChargeId('charge_land_table','FEE_ID','/finItem/search','');
        eeda.bindTableFieldCurrencyId('charge_land_table','CURRENCY_ID','/serviceProvider/searchCurrency','');
        eeda.bindTableField('charge_land_table','UOM','/serviceProvider/searchUnit','');
        
    };
    //------------事件处理
    var cargoTable = eeda.dt({
	    id: 'charge_land_table',
	    autoWidth: false,
	    drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
	        bindFieldEvent();
	    },
	    columns:[
            { "data": "ID","width": "10px",
                "render": function ( data, type, full, meta ) {
                	return '<input type="checkbox" class="checkBox">';
                }
            },
            {  "width": "30px",
                "render": function ( data, type, full, meta ) {
                   
                    return '<button type="button" class="delete btn table_btn delete_btn btn-xs" ><i class="fa fa-trash-o"></i> 删除</button></button>';
                }
            },
            { "data": "POL_ID", "width":"130px",
                "render": function ( data, type, full, meta ) {
            	   if(!data)
                       data='';
                   var field_html = template('table_dock_no_field_template',
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
                    var field_html = template('table_dock_no_field_template',
                    {
                    	id:'POD_ID',
                    	value:data,
                    	display_value:full.POD_NAME,
                    	style:'width:150px'
                    });
                    return field_html; 
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
            { "data": "PRICE", "width": "80px",
                "render": function ( data, type, full, meta ) {
                	 if(!data)
	                        data='';
	                    
	                    return '<input type="text" style="width:100px" name="price" value = "'+data+'" class="form-control notsave" >';
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

    $('#add_land_charge_fee').on('click', function(){
        var item={};
        cargoTable.row.add(item).draw(true);
    });
    
    //刷新明细表
    itemOrder.refleshLandItemTable = function(contract_id){
    	var url = "/customerContract/tableList?contract_id="+contract_id+"&type=land";
    	cargoTable.ajax.url(url).load();
    }


});
});