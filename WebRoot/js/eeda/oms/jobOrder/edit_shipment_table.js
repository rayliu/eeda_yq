define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
    $(document).ready(function() {

    	var deletedTableIds=[];
        //删除一行
        $("#ocean_cargo_table").on('click', '.delete', function(e){
            e.preventDefault();
            var tr = $(this).parent().parent();
            deletedTableIds.push(tr.attr('id'))
            
            cargoTable.row(tr).remove().draw();
        }); 

        itemOrder.buildOceanItem = function(){
            var cargo_table_rows = $("#ocean_cargo_table tr");
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
        	eeda.bindTableField('ocean_cargo_table','UNIT_ID','/serviceProvider/searchUnit','');
        };
        //------------事件处理
    	var cargoTable = eeda.dt({
    	    id: 'ocean_cargo_table',
    	    autoWidth: false,
    	    drawCallback: function( settings ) {
    	        bindFieldEvent();
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
                    	return '<button type="button" class="delete btn table_btn delete_btn btn-xs">删除</button>';
                    }
                },
               
                { "data": "LOAD_TYPE","width": "80px", 
                    "render": function ( data, type, full, meta ) {
                       if(!data)
                    	   data='';
                       var str= '<select name="load_type" class="form-control search-control" style="width:100px">'
                    	   	 	+'<option></option>'
    		                   +'<option value="FCL" '+ (data=='FCL'?'selected':'') +'>FCL</option>'
    		                   +'<option value="LCL" '+ (data=='LCL'?'selected':'') +'>LCL</option>'
    		                   +'<option value="FTL" '+ (data=='FTL'?'selected':'') +'>FTL</option>'
    		                   +'<option value="LTL" '+ (data=='LTL'?'selected':'') +'>LTL</option>'
    		                   +'</select>';
    		           return str;
                    }
                },
                { "data": "CONTAINER_TYPE","width": "80px", 
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        var str = '<select name="container_type" class="form-control search-control" style="width:100px">'
                        			+'<option></option>'
    			                   +'<option value="20\'GP" '+(data=='20\'GP' ? 'selected':'')+'>20GP</option>'
    			                   +'<option value="40\'GP" '+(data=='40\'GP' ? 'selected':'')+'>40GP</option>'
                                   +'<option value="40\'HQ" '+(data=='40\'HQ' ? 'selected':'')+'>40HQ</option>'
    			                   +'<option value="45\'GP" '+(data=='45\'GP' ? 'selected':'')+'>45GP</option>'
    			                   +'</select>';
                        return str;
                    }
                },
                { "data": "CONTAINER_NO","width": "180px",  
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" name="container_no" value="'+data+'" class="form-control" style="width:200px"/>';
                    }
                },
                { "data": "SEAL_NO","width": "180px",  
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" name="seal_no" value="'+data+'" class="form-control" style="width:200px"/>';
                    }
                },
                { "data": "PIECES", "width": "80px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" name="pieces" value="'+data+'" class="form-control" style="width:100px"/>';
                    }
                },
                { "data": "UNIT_ID", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                    	if(!data)
                            data='';
                        var field_html = template('table_dropdown_template',
                            {
                                id: 'UNIT_ID',
                                value: data,
                                display_value: full.UNIT_NAME,
                                style:'width:200px'
                            }
                        );
                        return field_html;
                    }
                },
                { "data": "PALLET_DESC", "width": "180px",
                	"render": function ( data, type, full, meta ) {
                		if(!data)
                			data='';
                		return '<input type="text" name="pallet_desc" value="'+data+'" class="form-control" style="width:200px"/>';
                	}
                },
                { "data": "GROSS_WEIGHT","width": "80px",  
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" name="gross_weight" value="'+data+'" class="form-control" style="width:100px"/>';
                    }
                },
                { "data": "VOLUME", "width": "80px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" name="volume" value="'+data+'" class="form-control" style="width:100px"/>';
                    }
                },
                { "data": "VGM", "width": "80px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" name="vgm" value="'+data+'" class="form-control" style="width:100px"/>';
                    }
                },
                { "data": "UNIT_NAME", "visible": false,
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                }
            ]
        });

//    	if(!boolOcean){
//    		$('#ocean_cargo_table input, #ocean_cargo_table select,#ocean_cargo_table button').prop('disabled', true);
//    		$('#ocean_cargo_table button').css('display', 'none');
//    	}
    	
        $('#add_ocean_cargo').on('click', function(){
            var item={};
            cargoTable.row.add(item).draw(true);
        });

        //刷新明细表
        itemOrder.refleshOceanTable = function(order_id){
        	var url = "/jobOrder/tableList?order_id="+order_id+"&type=shipment";
        	cargoTable.ajax.url(url).load();
        }

        //checkbox选中则button可点击
        $('#ocean_cargo_table').on('blur','[name=container_type], [name=vgm]',function(){
    		var row =$(this).parent().parent();
            var container_type=row.find('[name=container_type]').val();
            var vgmInput=row.find('[name=vgm]')
            var vgm=vgmInput.val();
            if(container_type=='20GP' && vgm>17500){
                alert('20GP的VGM不能大于17500KGS, 请重新修改数据.');
                vgmInput.val('');
            }
            if((container_type=='40GP'||container_type=='40HQ') && vgm>22000){
                alert('40GP/HQ的VGM不能大于22000KGS, 请重新修改数据.');
                vgmInput.val('');
            }
    	});
        
        $('#ocean_cargo_table').on('keyup','[name=vgm]',function(){
        	var vgm = 0;
        	$('#ocean_cargo_table [name=vgm]').each(function(){
        		var val = this.value;
            	if(val!=''&&!isNaN(val)){
            		vgm += parseFloat(val);
            	}
            })
            $('#vgm').val(vgm);
        })
        

    });
});