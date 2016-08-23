define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
$(document).ready(function() {

	var deletedTableIds=[];
    //删除一行
    $("#air_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))
        
        cargoTable.row(tr).remove().draw();
    }); 
    
    itemOrder.buildAirItem=function(){
        var cargo_table_rows = $("#air_table tr");
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
         $('table .date').datetimepicker({  
             format: 'yyyy-MM-dd',  
             language: 'zh-CN'
         }).on('changeDate', function(el){
             $(".bootstrap-datetimepicker-widget").hide();   
             $(el).trigger('keyup');
         });

        eeda.bindTableField('AIR_COMPANY','/serviceProvider/searchAirCompany','air');
    };
    //------------事件处理
    var cargoTable = $('#air_table').DataTable({
        "processing": true,
        "searching": false,
        "paging": false,
        "info": false,
        "scrollX":  true,
        "autoWidth": false,
        "language": {
            "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
        },
        "drawCallback": function( settings ) {
            bindFieldEvent();
        },
        "createdRow": function ( row, data, index ) {
            $(row).attr('id', data.ID);
        },
        "columns": [
            { "width": "30px",
                "render": function ( data, type, full, meta ) {
                	return '<button type="button" class="delete btn btn-default btn-xs" style="width:50px">删除</button>';
                }
            },
            { "data": "AIR_COMPANY", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    var field_html = template('table_dropdown_template',
                            {
                                id: 'AIR_COMPANY',
                                value: data,
                                display_value: full.AIR_COMPANY_NAME
                            }
                        );
                    return field_html;
                }
            },
            { "data": "FLIGHT_NO", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="flight_no" value="'+data+'" class="form-control" style="width:200px"/>';
                }
            },
            { "data": "VOYAGE_NO", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="voyage_no" value="'+data+'" class="form-control" style="width:200px"/>';
                }
            },
            { "data": "START_FROM", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="start_from" value="'+data+'" class="form-control" style="width:200px"/>';
                }
            },
            { "data": "ETD", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		 if(!data)
	                        data='';
	                    var field_html = template('table_date_field_template',
		                    {
		                        id: 'ETD',
		                        value: data.substr(0,19),
		                        style:'width:180px'
		                    }
		                );
	                    return field_html;
            	}
            },
            { "data": "DESTINATION", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="destination" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "ETA", "width": "180px",
                "render": function ( data, type, full, meta ) {
                	 if(!data)
	                        data='';
	                    var field_html = template('table_date_field_template',
		                    {
		                        id: 'ETA',
		                        value: data.substr(0,19),
		                        style:'width:180px'
		                    }
		                );
	                    return field_html;
                }
            }, { "data": "AIR_COMPANY_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            }
        ]
    });

    $('#add_air').on('click', function(){
        var item={};
        cargoTable.row.add(item).draw(true);
    });
    
    //刷新明细表
    itemOrder.refleshAirItemTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=air";
    	cargoTable.ajax.url(url).load();
    }
    

});
});