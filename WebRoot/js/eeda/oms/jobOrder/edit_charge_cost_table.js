define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
$(document).ready(function() {

	var deletedTableIds=[];
	
	//删除一行
    $("#cost_table").on('click', '.delete', function(e){
    	e.preventDefault();
    	var tr = $(this).parent().parent();
    	deletedTableIds.push(tr.attr('id'))
    	
    	costTable.row(tr).remove().draw();
    }); 
    //添加一行
    $('#add_charge_cost').on('click', function(){
    	var item={};
    	costTable.row.add(item).draw(true);
    });

    itemOrder.buildChargeCostDetail=function(){
        var cargo_table_rows = $("#cost_table tr");
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
            item.order_type = "cost";//应付
            for(var i = 1; i < row.childNodes.length; i++){
            	var el = $(row.childNodes[i]).find('input,select');
            	var name = el.attr('name'); 
            	
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
    var bindFieldEvent=function(){	
        eeda.bindTableField('SP_ID','/serviceProvider/searchCompany','');
        eeda.bindTableField('CHARGE_ID','/finItem/search','');
        eeda.bindTableField('UNIT_ID','/serviceProvider/searchUnit','');
        eeda.bindTableField('CURRENCY_ID','/serviceProvider/searchCurrency','');
    };
    //初始化利润总额
    var profitTotalCost = 0;
    var costTable = eeda.dt({
        id: 'cost_table',
        autoWidth: false,
        drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
            bindFieldEvent();
        },
        columns:[
            { "width": "30px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		return '<button type="button" class="delete btn btn-default btn-xs" style="width:50px" disabled>删除</button> ';
                	}else{
                		return '<button type="button" class="delete btn btn-default btn-xs" style="width:50px">删除</button> ';
                	}
                }
            },
            { "data": "TYPE","width": "80px",
                "render": function ( data, type, full, meta ) {
                	var str = '<select name="type" class="form-control search-control" style="width:100px">'
                        +'<option value="海运" '+(data=='海运' ? 'selected':'')+'>海运</option>'
                        +'<option value="空运" '+(data=='空运' ? 'selected':'')+'>空运</option>'
                        +'<option value="陆运" '+(data=='陆运' ? 'selected':'')+'>陆运</option>'
                        +'<option value="报关" '+(data=='报关' ? 'selected':'')+'>报关</option>'
                        +'<option value="保险" '+(data=='保险' ? 'selected':'')+'>保险</option>'
                        +'</select>';
                	return str;
                }
            },
            { "data": "SP_ID","width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    var field_html = template('table_dropdown_template',
                        {
                            id: 'SP_ID',
                            value: data,
                            display_value: full.SP_NAME,
                            style:'width:200px'
                        }
                    );
                    return field_html;
                }
            },
            { "data": "CHARGE_ID","width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    var field_html = template('table_dropdown_template',
                        {
                            id: 'CHARGE_ID',//对应数据库字段
                            value: data,
                            display_value: full.COST_NAME,
                            style:'width:200px'
                        }
                    );
                    return field_html;
                }
            },
            { "data": "PRICE", "width": "60px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="price" style="width:80px" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "AMOUNT","width": "60px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="number" name="amount" style="width:80px" value="'+data+'" class="form-control "/>';
                }
            },
            { "data": "UNIT_ID","width": "60px",
                "render": function ( data, type, full, meta ) {
                	 if(!data)
                         data='';
                     var field_html = template('table_dropdown_template',
                         {
                             id: 'UNIT_ID',
                             value: data,
                             display_value: full.UNIT_NAME,
                             style:'width:80px'
                         }
                     );
                     return field_html;
                }
            },
            { "data": "CURRENCY_ID", "width": "60px",
                "render": function ( data, type, full, meta ) {
                	if(!data)
                        data='';
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
            { "data": "TOTAL_AMOUNT", "width": "60px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
//                    totalCost += parseFloat(data);
                   
                    return '<input type="text" name="total_amount" style="width:80px" value="'+data+'" class="form-control" disabled/>';
                }
            },
            { "data": "EXCHANGE_RATE", "width": "60px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="exchange_rate" style="width:80px" value="'+data+'" class="form-control"/>';
                }
            },
            { "data": "CURRENCY_TOTAL_AMOUNT", "width": "60px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    profitTotalCost += parseFloat(data);
                    return '<input type="text" name="currency_total_amount" style="width:80px" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "REMARK","width": "180px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="remark" style="width:200px" value="'+data+'" class="form-control" />';
                }
            }, { "data": "SP_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            }, { "data": "COST_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            },{ "data": "UNIT_NAME", "visible": false,
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
            { "data": "AUDIT_FLAG", "visible": false,
            	"render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            }
        ]
    });

    //刷新明细表
    itemOrder.refleshCostTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=cost";
    	costTable.ajax.url(url).load();
    }
   
    //输入 数量*单价的时候，计算金额
    $('#cost_table').on('keyup','[name=price],[name=amount],[name=exchange_rate]',function(){
    	var row = $(this).parent().parent();
    	var price = $(row.find('[name=price]')).val()
    	var amount = $(row.find('[name=amount]')).val()
    	var exchange_rate = $(row.find('[name=exchange_rate]')).val()
    	if(price!=''&&amount!=''&&!isNaN(price)&&!isNaN(amount)){
    		var total_amount = parseFloat(price)*parseFloat(amount);
    		$(row.find('[name=total_amount]')).val(total_amount);
    		if(exchange_rate!=''&&!isNaN(exchange_rate)){
    			$(row.find('[name=currency_total_amount]')).val(total_amount*parseFloat(exchange_rate));
    		  }
    				
    	}	
    })
//    		    //获取应收应付字段
    		       var totalCostRMB = 0; 
    		       var totalCostUSD = 0;

    		       var tableCur =$('#cost_table').find('[name=CURRENCY_ID_input]');
    		       var tableAmount =$('#cost_table').find('[name=total_amount]');
    		       for(var i = 0;i<tableCur.length;i++){
    		           if(tableCur[i].value=='RMB'){               
    		               totalCostRMB += parseFloat(tableAmount[i].value);   //parseFloat(data)
    		           }else if(tableCur[i].value=='USD'){
    		               totalCostUSD += parseFloat(tableAmount[i].value);
    		           }
    		       }
    		       

    		       $('.costRMB').text(totalCostRMB);
    		    
    		       if(totalCostRMB !=""&&!isNaN(totalCostUSD)){
    		       $('.costRMB').text(totalCostRMB+'RMB');
    		       }else{
    		           $('.costRMB').text(0+"RMB");
    		       }
    		       
    		      if(totalCostUSD !=''&&!isNaN(totalCostUSD)){
    		          $('.costUSD').text(totalCostUSD+'USD');          
    		      }else{
    		          $('.costUSD').text(0+"USD");
    		      }
//    		    var company = $(table.find('[name=SP_ID_input]')).val()
//    		    $('.company').text(company);
    		//    
      
		        var totalCharge = $('[name=chargeRMB]').text().toString();
		        var totalChargeU = $('[name=chargeUSD]').text().toString();        
		        var totalChargeRMB=parseFloat(totalCharge.replace('RMB',''));        
		        var totalChargeUSD=parseFloat(totalChargeU.replace('USD',''));        
		        if(isNaN(totalChargeRMB)|| totalChargeRMB =='' ){
		            totalChargeRMB=0;
		           }        
		        if(isNaN(totalChargeUSD)|| totalChargeUSD =='' ){
		            totalChargeUSD=0;
		           }
		        
		        var costRMB = $('[name=costRMB]').text();    		        
		        
	            var profitUSD = parseFloat(totalChargeUSD)-parseFloat(totalCostUSD);
	                if(profitUSD>=0){
	                $('[name=profitUSD]').text(profitUSD+"USD");
	               }else if(profitUSD<0){
	                $('[name=profitUSD]').html("<span style='color:red'>"+profitUSD+"USD</span>");
	               } else{
	                 $('[name=profitUSD]').text('');
	               }                
		        
		        

	            var profitRMB = parseFloat(totalChargeRMB)-parseFloat(totalCostRMB);
	                if(profitRMB>=0){
	                $('[name=profitRMB]').text(profitRMB);
	                $('[name=profitRMB]').text(profitRMB+"RMB");
	               }else if(profitRMB<0){
	                $('[name=profitRMB]').html("<span style='color:red'>"+profitRMB+"</span>");
	                $('[name=profitRMB]').html("<span style='color:red'>"+profitRMB+"RMB</span>");
	               } else{
	                 $('[name=profitRMB]').text('');
	               }
    		          
    		        
    		        
    		              			

    			
    			
    			
    			
    			
    
    //应付结算,目前只用人民币结算
//    	var costRMB =$('#cost_table').find('[name=currency_total_amount]');
//    	var chargeRMB =$('#charge_table').find('[name=currency_total_amount]');
//    	var totalCostRMB=0;
//    	var totalChargeRMB=0;
//    	var profitRMB=0;
//    	for(var i = 0;i<costRMB.length;i++){
//    		var j = costRMB[i].value;
//    		if(j!=''&&!isNaN(j)){
//    			totalCostRMB+=parseFloat(j);
//    		}
//    	}
//		for(var i = 0;i<chargeRMB.length;i++){
//			var j = chargeRMB[i].value;
//			if(j!=''&&!isNaN(j)){
//				totalChargeRMB+=parseFloat(j);
//			}
//		}
//		profitRMB = totalChargeRMB-totalCostRMB;
//		$('.costRMB').text(totalCostRMB+"RMB");
//		$('.chargeRMB').text(totalChargeRMB+"RMB");
//		$('.profitRMB').text(profitRMB+"RMB"); 
          
    //取应收人民币总额字段
			   
		    var profitTotalCharge = $('[name=profitTotalCharge]').text();
		    var profitTotalCost = $('[name=profitTotalCost]').text();
		    
		    var profitTotalRMB = parseFloat(profitTotalCharge)-parseFloat(profitTotalCost);
		    if(profitTotalRMB>=0){
		
		    $('[name=profitTotalRMB]').text(profitTotalRMB+"RMB");
		   }else if(profitTotalRMB<0){
		
		    $('[name=profitTotalRMB]').html("<span style='color:red'>"+profitTotalRMB+"RMB</span>");
		   } else{
		     $('[name=profitTotalRMB]').text('');
		       }
    	
    
           $('[name=profitTotalCost]').text(profitTotalCost).hide();
});
});