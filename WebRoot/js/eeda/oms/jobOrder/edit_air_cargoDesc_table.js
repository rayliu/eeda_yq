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
	var cargoDesc_table = eeda.dt({
	    id: 'cargoDesc_table',
	    autoWidth: false,
	    columns:[
            { "width": "30px",
                "render": function ( data, type, full, meta ) {
                	return '<button type="button" class="delete btn btn-default btn-xs" style="width:50px">删除</button>';
                }
            },
            { "data": "LONG",  "width": "80px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="long" value="'+data+'" class="form-control" style="width:100px"/>';
                }
            },
            { "data": "WIDE", "width": "80px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="wide" value="'+data+'" class="form-control" style="width:100px"/>';
                }
            },
            { "data": "HIGH", "width": "80px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="high" value="'+data+'" class="form-control" style="width:100px"/>';
                }
            },
            { "data": "GROSS_WEIGHT", "width": "80px",
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return '<input type="text" name="gross_weight" value="'+data+'" class="form-control" style="width:100px"/>';
            	}
            },
            { "data": "AMOUNT", "width": "80px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="amount" value="'+data+'" class="form-control" style="width:100px"/>';
                }
            },
            { "data": "VOLUME", "width": "100px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="volume" value="'+data+'" class="form-control" style="width:120px" disabled/>';
                }
            }
        ]
    });

   	$('#cargoDesc_table').on("blur","[name=long],[name=high],[name=wide],[name=gross_weight],[name=amount]",function(){
		self = $(this)
		data = self.val()
		len = $.trim(data).length
		re = /^\d*\.?\d*$/g
		if(len>11&&len!=0||!re.test(data)&&len!=0){
			self.parent().append("<p><span style='color:red' class='error_span'>请输入数字！！！</span></p>")
		}else if (len != 0 ){
			
		}
	})
	
	$('#cargoDesc_table').on("focus","[name=long],[name=high],[name=wide],[name=gross_weight],[name=amount]",function(){
    		self = $(this)
    		self.parent().find("p").remove()
    	})
	
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
    $('#cargoDesc_table').on('keyup','[name=long],[name=wide],[name=high],[name=amount]',function(){
    	var row = $(this).parent().parent();
    	var long = $(row.find('[name=long]')).val();
    	var wide = $(row.find('[name=wide]')).val();
    	var high = $(row.find('[name=high]')).val();
    	var amount = $(row.find('[name=amount]')).val();
    	if(long!=''&&wide!=''&&high!=''&&amount!=''&&!isNaN(long)&&!isNaN(wide)&&!isNaN(high)&&!isNaN(amount)){
    		$(row.find('[name=volume]')).val(parseFloat(long)*parseFloat(wide)*parseFloat(high)*parseFloat(amount)/1000000);
    		var volume = 0;
        	$('#cargoDesc_table [name=volume]').each(function(){
        		var val = this.value;
            	if(val!=''&&!isNaN(val)){
            		volume += parseFloat(val);
            	}
            })
            $('#air_volume').val(volume);
    	}
    })
    
    //计算总毛重
    $('#cargoDesc_table').on('keyup','[name=gross_weight]',function(){
    	var gross_weight = 0;
		$('#cargoDesc_table [name=gross_weight]').each(function(){
			var val = this.value;
			if(val!=''&&!isNaN(val)){
				gross_weight += parseFloat(val);
			}
		})
		$('#air_gross_weight').val(gross_weight.toFixed(3));
    })
    
    
});
});