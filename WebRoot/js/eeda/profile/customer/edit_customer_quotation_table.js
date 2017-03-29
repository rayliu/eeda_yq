define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','file_upload','datetimepicker_CN'], function ($, metisMenu, template) { 
$(document).ready(function() {
	
    var deletedTableIds=[];
	
	
    //删除一行
    $("#customer_quotation_table").on('click', '.delete', function(e){
        e.preventDefault();
         var tr = $(this).parent().parent();
         deletedTableIds.push(tr.attr('id'));

         cargoTable.row(tr).remove().draw();
    }); 

    var bindFieldEvent=function(){
        $('table .date').datetimepicker({  
            format: 'yyyy-MM-dd hh:mm:ss',  
            language: 'zh-CN'
        }).on('changeDate', function(el){
            $(".bootstrap-datetimepicker-widget").hide();   
            $(el).trigger('keyup');
        }); 


        eeda.bindTableFieldCurrencyId('charge_table','CURRENCY_ID','/serviceProvider/searchCurrency','');
        eeda.bindTableFieldDockInfo('customer_quotation_table','TAKE_ADDRESS');
        eeda.bindTableFieldDockInfo('customer_quotation_table','DELIVERY_ADDRESS');
        eeda.bindTableFieldDockInfo('customer_quotation_table','LOADING_WHARF1');
        eeda.bindTableFieldDockInfo('customer_quotation_table','LOADING_WHARF2');
    };
    //------------事件处理,文档table
    var cargoTable = eeda.dt({
        id: 'customer_quotation_table',
        autoWidth: false,
        drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
                bindFieldEvent();
            },
        columns:[
           { "width": "30px",
                    "render": function ( data, type, full, meta ) {
                        return '<button type="button" class="delete btn table_btn delete_btn btn-xs">'+
                        '<i class="fa fa-trash-o"></i> 删除</button>';
                    }
            },
            { "data": "TAKE_ADDRESS", "width": "150px", "className":"consigner_addr",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
                            data='';
                        var field_html = template('table_dock_no_field_template',
                            {
                                id: 'TAKE_ADDRESS',
                                value: data,
                                display_value: full.TAKE_ADDRESS_NAME,
                                style:'width:180px',
                            }
                        );
                        return field_html;
                    }
            },
            { "data": "LOADING_WHARF1", "width": "150px", "className":"consigner_addr",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                            data='';
                        var field_html = template('table_dock_no_field_template',
                            {
                                id: 'LOADING_WHARF1',
                                value: data,
                                display_value: full.LOADING_WHARF1_NAME,
                                style:'width:180px',
                            }
                        );
                        return field_html;
                    }
            },
            { "data": "LOADING_WHARF2", "width": "150px", "className":"consigner_addr",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                            data='';
                        var field_html = template('table_dock_no_field_template',
                            {
                                id: 'LOADING_WHARF2',
                                value: data,
                                display_value: full.LOADING_WHARF2_NAME,
                                style:'width:180px',
                            }
                        );
                        return field_html;
                    }
            },
            { "data": "DELIVERY_ADDRESS", "width": "150px", "className":"consignee_addr",
            	"render": function ( data, type, full, meta ) {
                if(!data)
                            data='';
                        var field_html = template('table_dock_no_field_template',
                            {
                                id: 'DELIVERY_ADDRESS',
                                value: data,
                                display_value: full.DELIVERY_ADDRESS_NAME,
                                style:'width:180px',
                            }
                        );
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
                            value: data
                        }
                    );
                    return field_html;
                }
            },
            { "data": "CONTAINER_VOLUME","width": "80px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="container_volume" value="'+data+'" class="form-control" style="width:80px"/>';
                }
            },
            { "data": "CURRENCY_ID", "width": "60px",
                "render": function ( data, type, full, meta ) {
                    if(!data&&!full.CURRENCY_NAME){
                        data='3';
                        full.CURRENCY_NAME="CNY";
                    }
                    var field_html = template('table_dropdown_template',
                        {
                            id: 'CURRENCY_ID',
                            value: data,
                            display_value: full.CURRENCY_NAME,
                            style:'width:80px'
                        }
                       );
                       return field_html;
                     }
            },
            { "data": "TAX_FREE_FREIGHT","width": "80px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="tax_free_freight" value="'+data+'" class="form-control" style="width:80px"/>';
                }
            },
            { "data": "TAX_RATE","width": "80px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="tax_rate" value="'+data+'" class="form-control" style="width:80px"/>';
                }
            },
            { "data": "TAX","width": "80px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="tax" value="'+data+'" class="form-control" style="width:80px"/>';
                }
            },
            { "data": "PRICE_TAX","width": "80px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="price_tax" value="'+data+'" class="form-control" style="width:80px"/>';
                }
            },
            { "data": "REMARK","width": "80px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="remark" value="'+data+'" class="form-control" style="width:80px"/>';
                }
            },
            { "data": "TAKE_ADDRESS_NAME","visible": false},
            { "data": "DELIVERY_ADDRESS_NAME","visible": false},
            { "data": "LOADING_WHARF1_NAME","visible": false},
            { "data": "LOADING_WHARF2_NAME","visible": false},
            { "data": "CURRENCY_NAME","visible": false}
            ]
    });
    

    itemOrder.buildCustomerQuotationDetail=function(){
            var cargo_table_rows = $("#customer_quotation_table tr");
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
               
                for(var i = 0; i < row.childNodes.length; i++){
                    var name = $(row.childNodes[i]).find('input,select').attr('name');
                    var value = $(row.childNodes[i]).find('input,select').val();
                    if(name){
                        item[name] = value;
                    }
                }
                item.action = id.length > 0?'UPDATE':'CREATE';
                cargo_items_array.push(item);
                if(!id.length>0){
                    var d = new Date();
                    var str = d.getFullYear()+"-"+(d.getMonth()+1)+"-"+d.getDate()+' '+d.getHours()+':'+d.getMinutes()+':'+d.getSeconds();
                    item.creator = $('#user_id').val();
                    item.create_stamp = str;
                }
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
     
     $('#add_customerQuotation').on('click', function(){
            var item={};
            cargoTable.row.add(item).draw(true);
        });
   //刷新明细表
        itemOrder.refleshCustomerQuotationItemTable = function(order_id){
            var url = "/customer/tableList?order_id="+order_id+"&type=customerQuotationItem";
            cargoTable.ajax.url(url).load();
        }

        $('#customer_quotation_table').on('keyup','[name=tax_free_freight],[name=tax_rate],[name=tax]',function(){
            var row=$(this).parent().parent();
            var tax_free_freight=$(row.find('[name=tax_free_freight]')).val();
            var tax_rate=$(row.find('[name=tax_rate]')).val();
            var tax=$(row.find('[name=tax]')).val();
    
            if(tax_free_freight==''){
              $(row.find('[name=price_tax]')).val('');
              $(row.find('[name=tax]')).val('');
            }
            if(tax_rate==''){
              $(row.find('[name=tax]')).val('');
            }
            if(tax_free_freight!=''&&tax_rate!=''&&!isNaN(tax_free_freight)&&!isNaN(tax_rate)){
                var amount = (parseFloat(tax_free_freight)*parseFloat(tax_rate)).toFixed(2);
                $(row.find('[name=tax]')).val(amount);
            }
            if(tax_free_freight!=''&&!isNaN(tax_free_freight)){
                
                $(row.find('[name=price_tax]')).val(tax_free_freight);
                var tax1=$(row.find('[name=tax]')).val();
                if(tax1!=''&&!isNaN(tax1)){
                    $(row.find('[name=price_tax]')).val((parseFloat(tax_free_freight)+parseFloat(tax1)).toFixed(2));
                }
            }
       })

});
});