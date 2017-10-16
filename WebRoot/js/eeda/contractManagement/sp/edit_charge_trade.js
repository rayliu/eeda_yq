define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
$(document).ready(function() {

	var deletedTableIds=[];
    //删除一行
    $("#charge_table_trade").on('click', '.delete', function(){
        var tr = $(this).parent().parent();
        tr.css("display","none");
        deletedTableIds.push(tr.attr('id'))
    });

    //注意使用通用的方法 buildTableDetail
    itemOrder.buildTradeItem=function(){
        var items = eeda.buildTableDetail("charge_table_trade", deletedTableIds);
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            item.contract_type = "trade";
        }
        return items;
    };

    var bindFieldEvent=function(){
        eeda.bindTableFieldChargeId('charge_table_trade','FEE_ID','/finItem/search','');
        eeda.bindTableFieldCurrencyId('charge_table_trade','CURRENCY_ID','/serviceProvider/searchCurrency','');
        eeda.bindTableField('charge_table_trade','UOM','/serviceProvider/searchChargeUnit','');
    };

    //------------事件处理
    var cargoTable = eeda.dt({
	    id: 'charge_table_trade',
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

    $('#add_trade_charge_fee').on('click', function(){
        var item={};
        cargoTable.row.add(item).draw(true);
    });
    
    //刷新明细表
    itemOrder.refleshLandItemTable = function(contract_id){
    	var url = "/supplierContract/tableList?contract_id="+contract_id+"&type=trade";
    	cargoTable.ajax.url(url).load();
    }
    
    //加工贸易页面校验
    $('#charge_table_trade').on('blur','[name=price]',function(){
    	var data = $(this).val();
    	var len = $.trim(data).length;
    	var re = /^\d{0,9}(\.\d{1,5})?$/;
    	if(!re.test(data)&&len>0||len>15&&len>0){
    		$(this).parent().append("<span style='color:red;display:block;' class='error_span'>请输入合法数字</span>")
    	}
    });
    $('#charge_table_trade').on('focus','[name=price]',function(){
    	$(this).parent().find("span").remove();
    });
        
});
});