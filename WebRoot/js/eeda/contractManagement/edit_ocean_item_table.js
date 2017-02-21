define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'jq_blockui'], function ($, metisMenu, template) { 
	$(document).ready(function() {
	    var deletedTableIds=[];

	    //删除一行
	    $("#ocean_item_table").on('click', '.delete', function(e){
	        e.preventDefault();
	        var tr = $(this).parent().parent();
	        deletedTableIds.push(tr.attr('id'));
	        
	        cargoTable.row(tr).remove().draw();
	    }); 
	    
	    itemOrder.buildOceanCargoItemDetail=function(){
	    	var cargo_table_rows = $("#ocean_item_table tr");
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
	            	var name = $(row.childNodes[i]).find('input,select').attr('name');
	            	var value = $(row.childNodes[i]).find('input,select').val();
	            	if(name){
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
	    	eeda.bindTableField('cargo_table','UNIT_ID','/serviceProvider/searchUnit','');
	    	eeda.bindTableField('cargo_table','POR','/location/searchPort','');
	    	eeda.bindTableField('cargo_table','POL','/location/searchPort','');
	    	eeda.bindTableField('cargo_table','POD','/location/searchPort','');
	    	eeda.bindTableField('cargo_table','CARRIER','/serviceProvider/searchCarrier','');
	    };

	    //------------事件处理
	    var cargoTable = eeda.dt({
            id: 'ocean_item_table',
            autoWidth: false,
            "drawCallback": function( settings ) {
		        bindFieldEvent();
		        $.unblockUI();
		    },
            columns:[
				{ "width": "30px",
	                "render": function ( data, type, full, meta ) {
	                	return '<button type="button" class="delete btn table_btn delete_btn btn-xs">'+
                        '<i class="fa fa-trash-o"></i> 删除</button>';
	                }
				},
	            { "data": "POL", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="POL"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "POD", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="POD"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "SERVICE_OWNER", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="service_owner"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "DOC", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="DOC"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "TLX", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="TLX"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "EIR", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="EIR"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "HSS", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="HSS"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "O/F_20GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="O/F_20GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "O/F_40GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="O/F_40GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "O/F_40HQ", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="O/F_40HQ"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "THC_20GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="THC_20GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "THC_40GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="THC_40GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "THC_40HQ", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="THC_40HQ"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "EBS_20GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="EBS_20GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "EBS_40GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="EBS_40GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "EBS_40HQ", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="EBS_40HQ"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "AMS/AFR_20GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="AMS/AFR_20GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "AMS/AFR_40GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="AMS/AFR_40GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "AMS/AFR_40HQ", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="AMS/AFR_40HQ"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "VAT_20GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="VAT_20GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "VAT_40GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="VAT_40GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "VAT_40HQ", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="VAT_40HQ"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "ISPS_20GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="ISPS_20GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "ISPS_40GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="ISPS_40GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "ISPS_40HQ", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="ISPS_40HQ"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "TRUCK_20GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="TRUCK_20GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "TRUCK_40GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="TRUCK_40GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "TRUCK_40HQ", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="TRUCK_40HQ"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "TOTAL_20GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="TOTAL_20GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "TOTAL_40GP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="TOTAL_40GP"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "TOTAL_40HQ", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="TOTAL_40HQ"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "EFFECTIVE_TIME", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="effective_time"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "REMARK", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="remark"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "CREATE_STAMP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="create_stamp"  value="'+data+'" class="form-control" />';
	            	}
            	},
            	{ "data": "CREATOR", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="creator"  value="'+data+'" class="form-control" />';
	            	}
            	}
	        ]
	    });

	    $('#add_ocean_item_cargo').on('click', function(){
	        var item={};
	        cargoTable.row.add(item).draw(true);
	    });
	    
	    //刷新明细表
	    itemOrder.refleshOceanCargoItemTable = function(order_id){
	    	var url = "/supplierContract/tableList?order_id="+order_id+"&type=oceanCargoItem";
	    	cargoTable.ajax.url(url).load();
	    }
	    
	   
	});
});
