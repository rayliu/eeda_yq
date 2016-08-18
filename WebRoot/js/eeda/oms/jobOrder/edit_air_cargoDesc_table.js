define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {

	var deletedTableIds=[];
    //删除一行
    $("#cargoDesc_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'))
        
        cargoDesc_table.row(tr).remove().draw();
    }); 

    itemOrder.buildCargoDescDetail=function(){
        var cargo_table_rows = $("#cargoDesc_table tr");
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
    
    
    //------------事件处理
    var cargoDesc_table = $('#cargoDesc_table').DataTable({
        "processing": true,
        "searching": false,
        "paging": false,
        "info": false,
        "scrollX":  true,
        "autoWidth": false,
        "language": {
            "url": "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
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
            { "data": "LONG",  "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="number" name="long" value="'+data+'" class="form-control" style="width:200px"/>';
                }
            },
            { "data": "WIDE", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="number" name="wide" value="'+data+'" class="form-control" style="width:200px"/>';
                }
            },
            { "data": "HIGH", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="number" name="high" value="'+data+'" class="form-control" style="width:200px"/>';
                }
            },
            { "data": "GROSS_WEIGHT", "width": "180px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="number" name="gross_weight" value="'+data+'" class="form-control" style="width:200px"/>';
            	}
            },
            { "data": "AMOUNT", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="number" name="amount" value="'+data+'" class="form-control" style="width:200px"/>';
                }
            },
            { "data": "VOLUME", "width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="number" name="volume" value="'+data+'" class="form-control" style="width:200px" disabled/>';
                }
            }
        ]
    });

    $('#add_cargoDesc').on('click', function(){
        var item={};
        cargoDesc_table.row.add(item).draw(true);
    });
    
    //刷新明细表
    itemOrder.refleshCargoDescTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=cargoDesc";
    	cargoDesc_table.ajax.url(url).load();
    }
    
    //计算体积
    $('#cargoDesc_table').on('keyup','[name=long],[name=wide],[name=high]',function(){
    	var row = $(this).parent().parent();
    	var long = $(row.find('[name=long]')).val();
    	var wide = $(row.find('[name=wide]')).val();
    	var high = $(row.find('[name=high]')).val();
    	if(long!=''&&wide!=''&&high!=''){
	    	if(!isNaN(long)&&!isNaN(wide)&&!isNaN(high)){
	    		$(row.find('[name=volume]')).val(parseFloat(long)*parseFloat(wide)*parseFloat(high));
	    	}else{
	    		$(row.find('[name=volume]')).val('');
	    	}
    	}
    })
    
    
});
});