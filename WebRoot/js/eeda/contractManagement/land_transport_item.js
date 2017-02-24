define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 

  $(document).ready(function() {
	  
	  var deletedTableIds=[];
	  itemOrder.buildLandTransportItemDetail=function(){
	    	var cargo_table_rows = $("#land_transport_item_table tr");
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
          id: 'land_transport_item_table',
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
	            { "data": "ROUTE_FROM", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="route_from"  value="'+data+'" class="form-control" />';
	            	}
	          	},
	          	{ "data": "ROUTE_TO", 
		            	"render": function ( data, type, full, meta ) {
		            		if(!data)
		            			data='';
		            		return '<input type="text" name="route_to"  value="'+data+'" class="form-control" />';
		            	}
	          	},
	          	{ "data": "DANGEROUS_GOODS_TYPE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="dangerous_goods_type"  value="'+data+'" class="form-control" />';
	            	}
	          	},
	          	{ "data": "CAR_TYPE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="car_type"  value="'+data+'" class="form-control" />';
	            	}
	          	},
	          	{ "data": "UNIT", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="unit"  value="'+data+'" class="form-control" />';
	            	}
	          	},
	          	{ "data": "PRICE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="price"  value="'+data+'" class="form-control" />';
	            	}
	          	},
	          	{ "data": "NOTAX_PRICE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="notax_price"  value="'+data+'" class="form-control" />';
	            	}
	          	},
	          	{ "data": "TAX_RATE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="tax_rate"  value="'+data+'" class="form-control" />';
	            	}
	          	},
	          	{ "data": "TAX_PRICE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="tax_price"  value="'+data+'" class="form-control" />';
	            	}
	          	},
	          	{ "data": "TRANSIT_TIME", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="transit_time"  value="'+data+'" class="form-control" />';
	            	}
	          	},
	          	{ "data": "DOCUMENT_FEE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="document_fee"  value="'+data+'" class="form-control" />';
	            	}
	          	},
	          	{ "data": "TALLY_FEE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="tally_fee"  value="'+data+'" class="form-control" />';
	            	}
	          	},
	          	{ "data": "CEA_CUSTOM_FEE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="CEA_custom_fee"  value="'+data+'" class="form-control" />';
	            	}
	          	},
	          	{ "data": "DIA_CUSTOM_FEE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="DIA_custom_fee"  value="'+data+'" class="form-control" />';
	            	}
	          	},
	          	{ "data": "TARIFF", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="tariff"  value="'+data+'" class="form-control" />';
	            	}
	          	},
	          	{ "data": "EXCHANGE_FEE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="exchange_fee"  value="'+data+'" class="form-control" />';
	            	}
	          	},
	          	{ "data": "STORAGE_FEE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="storage_fee"  value="'+data+'" class="form-control" />';
	            	}
	          	},
	          	{ "data": "DELIVERY_FEE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="delivery_fee"  value="'+data+'" class="form-control" />';
	            	}
	          	},
	          	{ "data": "CUSTOMS_INSPECTION_FEE", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="customs_inspection_fee"  value="'+data+'" class="form-control" />';
	            	}
	          	},
	          	{ "data": "EFFECTIVE_TIME", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="effective_time"  value="'+data+'" class="form-control" />';
	            	}
	          	},
	          	{ "data": "CREATE_STAMP", 
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return '<input type="text" name="create_stamp"  value="'+data+'" class="form-control" />';
	            	}
	          	},
	          	{ "data": "REMARK", 
		            	"render": function ( data, type, full, meta ) {
		            		if(!data)
		            			data='';
		            		return '<input type="text" name="remark"  value="'+data+'" class="form-control" />';
		            	}
	          	}
	        ]
	    });
  		 
  		 
  		 $('#addland_transport_item').on('click', function(){
 	        var item={};
 	       cargoTable.row.add(item).draw(true);
 	    });
 	    
  		 
  		 //删除一行
 	    $("#land_transport_item_table").on('click', '.delete', function(e){
 	        e.preventDefault();
 	        var tr = $(this).parent().parent();
 	        deletedTableIds.push(tr.attr('id'));
 	        
 	        cargoTable.row(tr).remove().draw();
 	    }); 
  		 
 	    //刷新明细表
 	   itemOrder.refleshLandTransportItemTable = function(order_id){
 	    	var url = "/supplierContract/tableList?order_id="+order_id+"&type=landTransportItem";
 	    	cargoTable.ajax.url(url).load();
 	    }
 	    
  		
  });
});