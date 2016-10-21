define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) { 
	$(document).ready(function() {

	    var deletedTableIds=[];

	    //删除一行
	    $("#cargo_table").on('click', '.delete', function(e){
	        e.preventDefault();
	        var tr = $(this).parent().parent();
	        deletedTableIds.push(tr.attr('id'));
	        
	        cargoTable.row(tr).remove().draw();
	    }); 
	    
	    //构造函数，获得json
	    salesOrder.buildCargoDetail=function(){
	    	var cargo_table_rows = $("#cargo_table tr");
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
	    	$('table .date').datetimepicker({  
	    	    format: 'yyyy-MM-dd hh:mm:ss',  
	    	    language: 'zh-CN'
	    	}).on('changeDate', function(el){
	    	    $(".bootstrap-datetimepicker-widget").hide();   
	    	    $(el).trigger('keyup');
	    	});

	    	eeda.bindTableField('cargo_table','UNIT_ID','/serviceProvider/searchUnit','');
	    	eeda.bindTableField('cargo_table','POR','/location/searchPort','');
	    	eeda.bindTableField('cargo_table','POL','/location/searchPort','');
	    	eeda.bindTableField('cargo_table','POD','/location/searchPort','');
	    	eeda.bindTableField('cargo_table','CARRIER','/serviceProvider/searchCarrier','');
	    };

	    //------------事件处理
	    var cargoTable = eeda.dt({
            id: 'cargo_table',
            autoWidth: false,
            "drawCallback": function( settings ) {
		        bindFieldEvent();
		    },
            columns:[
	            {"width": "10px",
	                "render": function ( data, type, full, meta ) {
	                		return '<button type="button" class="delete btn btn-default btn-xs">删除</button> ';
	                }
	            },
	            { "data": "COMMODITY_CODE","width": "80px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="COMMODITY_CODE" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "COMMODITY_NAME","width": "80px",
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                	   data='';
	                   return '<input type="text" name="COMMODITY_NAME" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "STANDARD","width": "80px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="STANDARD" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "DECLARE_ELEMENT","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="DECLARE_ELEMENT" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "TRANSACTION_AMOUNT","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="TRANSACTION_AMOUNT" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "TRANSACTION_UNIT" ,"width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var str = '<select name="transaction_unit" class="form-control search-control" style="width:100px" >'
	                    	+'<option value="" '+(data=='' ? 'selected':'')+'></option>'
	                        +'<option value="件" '+(data=='件' ? 'selected':'')+'>件</option>'
	                        +'<option value="支" '+(data=='支' ? 'selected':'')+'>支</option>'
	                        +'<option value="台" '+(data=='台' ? 'selected':'')+'>台</option>'
	                        +'</select>';
	                	return str;
	                }
	            },
	            { "data": "LEGAL_AMOUNT", 
	                "render": function ( data, type, full, meta ) {
	                	if(!data)
	                        data='';
	                	return '<input type="text" name="LEGAL_AMOUNT" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "LEGAL_UNIT","width": "80px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var str = '<select name="legal_unit" class="form-control search-control" style="width:100px" >'
	                    	+'<option value="" '+(data=='' ? 'selected':'')+'></option>'
	                        +'<option value="件" '+(data=='件' ? 'selected':'')+'>件</option>'
	                        +'<option value="支" '+(data=='支' ? 'selected':'')+'>支</option>'
	                        +'<option value="台" '+(data=='台' ? 'selected':'')+'>台</option>'
	                        +'</select>';
	                	return str;
	                }
	            },
	            { "data": "PRICE","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="PRICE" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "TOTAL_PRICE","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="TOTAL_PRICE" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "CURRENCY" ,"width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="CURRENCY" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "EXEMPTION","width": "80px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="EXEMPTION" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "IS_GEN_JOB", "visible": false,
	            	"render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return data;
	                }
	            }
	        ]
	    });

	    $('#add_cargo').on('click', function(){
	        var item={};
	        cargoTable.row.add(item).draw(true);
	    });
	    
	    //刷新明细表
	    salesOrder.refleshTable = function(order_id){
	    	var url = "/customPlanOrder/tableList?order_id="+order_id+"&type=cargo";
	    	cargoTable.ajax.url(url).load();
	    }
	    
	    //checkbox选中则button可点击
		$('#cargo_table').on('click','.checkBox',function(){
			
			var hava_check = 0;
			$('#cargo_table input[type="checkbox"]').each(function(){	
				var checkbox = $(this).prop('checked');
	    		if(checkbox){
	    			hava_check=1;
	    		}	
			})
			if(hava_check>0){
				$('#create_jobOrder').attr('disabled',false);
			}else{
				$('#create_jobOrder').attr('disabled',true);
			}
		});
	});
});
