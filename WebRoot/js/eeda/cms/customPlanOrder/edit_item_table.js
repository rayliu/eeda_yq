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
 	    	eeda.bindTableField('cargo_table','UNIT_ID','/serviceProvider/searchUnit','');
	    	eeda.bindTableField('cargo_table','POR','/location/searchPort','');
	    	eeda.bindTableField('cargo_table','POL','/location/searchPort','');
	    	eeda.bindTableField('cargo_table','POD','/location/searchPort','');
	    	eeda.bindTableField('cargo_table','CARRIER','/serviceProvider/searchCarrier','');
	    	eeda.bindTableFieldCurrencyId('cargo_table','CURRENCY','/serviceProvider/searchCurrency','');
	    	eeda.bindTableField('cargo_table','DESTINATION_COUNTRY_ITEM','/location/searchCountry','');
	    	eeda.bindTableField('cargo_table','EXEMPTION','/serviceProvider/searchCustomExemptionNature','');
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
	                		return '<button type="button" class="delete btn btn-default btn-xs" style="width:40px" >删除</button> ';
	                }
	            },
	            { "data": "ITEM_NO","width": "60px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return '<input style="width:80px"  type="text" name="ITEM_NO" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "COMMODITY_CODE","width": "80px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="COMMODITY_CODE" value="'+data+'" class="form-control search-control" style="width:100px"/>';
	                }
	            },
	            { "data": "COMMODITY_NAME","width": "80px",
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                	   data='';
	                   return '<input type="text" name="COMMODITY_NAME" value="'+data+'" class="form-control search-control" style="width:100px"/>';
	                }
	            },
	            { "data": "STANDARD","width": "80px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="STANDARD" value="'+data+'" class="form-control search-control" style="width:100px"/>';
	                }
	            },
	            { "data": "DECLARE_ELEMENT","width": "180px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="DECLARE_ELEMENT" value="'+data+'" class="form-control search-control" style="width:200px"/>';
	                }
	            },
	            { "data": "TRANSACTION_AMOUNT","width": "80px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input style="width:100px" type="text" name="TRANSACTION_AMOUNT" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "TRANSACTION_UNIT" ,"width": "80px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var str = '<select name="transaction_unit" class="form-control search-control" style="width:100px" >'
	                    	+'<option value="" '+(data=='' ? 'selected':'')+'></option>'
	                        +'<option value="件" '+(data=='件' ? 'selected':'')+'>件</option>'
	                        +'<option value="支" '+(data=='支' ? 'selected':'')+'>支</option>'
	                        +'<option value="台" '+(data=='台' ? 'selected':'')+'>台</option>'
	                        +'<option value="个" '+(data=='个' ? 'selected':'')+'>个</option>'
	                        +'<option value="只" '+(data=='只' ? 'selected':'')+'>只</option>'
	                        +'<option value="块" '+(data=='块' ? 'selected':'')+'>块</option>'
	                        +'<option value="片" '+(data=='片' ? 'selected':'')+'>片</option>'
	                        +'<option value="千升" '+(data=='千升' ? 'selected':'')+'>千升</option>'
	                        +'<option value="吨" '+(data=='吨' ? 'selected':'')+'>吨</option>'
	                        +'<option value="长吨" '+(data=='长吨' ? 'selected':'')+'>长吨</option>'
	                        +'<option value="短吨" '+(data=='短吨' ? 'selected':'')+'>短吨</option>'
	                        +'<option value="司马担" '+(data=='司马担' ? 'selected':'')+'>司马担</option>'
	                        +'<option value="司马斤" '+(data=='司马斤' ? 'selected':'')+'>司马斤</option>'
	                        +'<option value="斤" '+(data=='斤' ? 'selected':'')+'>斤</option>'
	                        +'<option value="磅" '+(data=='磅' ? 'selected':'')+'>磅</option>'
	                        +'<option value="担" '+(data=='担' ? 'selected':'')+'>担</option>'
	                        +'<option value="英担" '+(data=='英担' ? 'selected':'')+'>英担</option>'
	                        +'<option value="两" '+(data=='两' ? 'selected':'')+'>两</option>'
	                        +'<option value="市担" '+(data=='市担' ? 'selected':'')+'>市担</option>'
	                        +'</select>';
	                	return str;
	                }
	            },
	            { "data": "LEGAL_AMOUNT","width": "80px", 
	                "render": function ( data, type, full, meta ) {
	                	if(!data)
	                        data='';
	                	return '<input style="width:100px" type="text" name="LEGAL_AMOUNT" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            { "data": "LEGAL_UNIT","width": "80px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var str = '<select name="legal_unit" class="form-control search-control" style="width:100px" >'
	                    	+'<option value="" '+(data=='' ? 'selected':'')+'></option>'
	                    	+'<option value="个" '+(data=='个' ? 'selected':'')+'>个</option>'
	                        +'<option value="千克" '+(data=='千克' ? 'selected':'')+'>千克</option>'
	                        +'<option value="克" '+(data=='克' ? 'selected':'')+'>克</option>'
	                        +'<option value="毫克" '+(data=='毫克' ? 'selected':'')+'>毫克</option>'
	                        +'<option value="吨" '+(data=='吨' ? 'selected':'')+'>吨</option>'
	                        +'<option value="原子质量单位" '+(data=='原子质量单位' ? 'selected':'')+'>原子质量单位</option>'
	                        +'<option value="平方米" '+(data=='平方米' ? 'selected':'')+'>平方米</option>'
	                        +'<option value="厘米" '+(data=='厘米' ? 'selected':'')+'>厘米</option>'
	                        +'<option value="公顷" '+(data=='公顷' ? 'selected':'')+'>公顷</option>'
	                        +'<option value="立方米" '+(data=='立方米' ? 'selected':'')+'>立方米</option>'
	                        +'</select>';
	                	return str;
	                }
	            },
	            { "data": "DESTINATION_COUNTRY_ITEM","width": "180px",
	            	"render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var field_html = template('table_dropdown_template',
	                            {
	                                id: 'DESTINATION_COUNTRY_ITEM',
	                                value: data,
	                                display_value: full.DESTINATION_COUNTRY_ITEM_NAME,
	                                style:'width:200px'
	                            }
	                        );
	                    return field_html;
	                }
	            },
	            { "data": "PRICE","width": "80px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="PRICE" value="'+data+'" class="form-control search-control" style="width:100px"/>';
	                }
	            },
	            { "data": "TOTAL_PRICE","width": "100px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                   return '<input type="text" name="TOTAL_PRICE" value="'+data+'" class="form-control search-control " style="width:120px" disabled />';
	                }
	            },
	            { "data": "CURRENCY" ,"width": "80px",
	            	 "render": function ( data, type, full, meta ) {
	 	                	if(!data)
	 	                        data='';
	 	                    var field_html = template('table_dropdown_template',
	 	                        {
	 	                            id: 'CURRENCY',
	 	                            value: data,
	 	                            display_value: full.CURRENCY_NAME,
	 	                            style:'width:100px'
	 	                        }
	 	                    );
	 	                    return field_html;
	                 }
	            },
	            { "data": "EXEMPTION","width": "180px",
	            	"render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var field_html = template('table_dropdown_template',
	                            {
	                                id: 'EXEMPTION',
	                                value: data,
	                                display_value: full.EXEMPTION_NAME,
	                                style:'width:200px'
	                            }
	                        );
	                    return field_html;
	                }
	            },
	            { "data": "IS_GEN_JOB", "visible": false,
	            	"render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return data;
	                }
	            } ,
	            { "data": "CURRENCY_NAME", "visible": false,
	            	"render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return data;
	                }
	            },
	            { "data": "DESTINATION_COUNTRY_ITEM_NAME", "visible": false,
	            	"render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return data;
	                }
	            },
	            { "data": "EXEMPTION_NAME", "visible": false,
	            	"render": function ( data, type, full, meta ) {
	            		if(!data)
	            			data='';
	            		return data;
	            	}
	            }
	        ]
	    });
	    
	    
	    //输入 申报数量*单价的时候，计算金额
	    $('#cargo_table').on('keyup','[name=TRANSACTION_AMOUNT],[name=PRICE],[name=TOTAL_PRICE]',function(){
	    	var row = $(this).parent().parent();
	    	var transaction_amount = $(row.find('[name=TRANSACTION_AMOUNT]')).val();
	    	var price = $(row.find('[name=PRICE]')).val();
	    	if(transaction_amount==''||price==''){
	    		$(row.find('[name=TOTAL_PRICE]')).val('');
	    	}
	    	if(transaction_amount!=''&&price!=''&&!isNaN(transaction_amount)&&!isNaN(price)){
	    		var total_price = parseFloat(transaction_amount)*parseFloat(price);
	    		$(row.find('[name=TOTAL_PRICE]')).val(total_price);
	    	}
	    });
	    
	    
	    var self ;
	    $('#cargo_table').on('click','input[name=DECLARE_ELEMENT]',function(){
	    	self = $(this);
	    	$('#showNote').val(self.val())
	    	$('#a_btn').click();
	    	$('#btnConfirm').click(function(){
	    		var showNote = $('#showNote').val();
	    		self.val(showNote);
	    	})
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
