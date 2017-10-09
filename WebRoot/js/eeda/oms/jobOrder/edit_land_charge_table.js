define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
$(document).ready(function() {
    

	var deletedTableIds=[];
	// 常用费用
    $('#landcollapseChargeInfo').on('show.bs.collapse',function(){
        var thisType=$(this).attr('id');
        var type='Charge';
        var div=$('#land'+type+'Div').empty();
        $('#landcollapse'+type+'Icon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
        var order_type = $('#type').val();
        var customer_id = $('#customer_id').val();
        if(order_type.trim() == '' || customer_id == ''){
            $.scojs_message('请先选择类型和客户', $.scojs_message.TYPE_ERROR);
            return
        }else{
            $.post('/jobOrder/getLandArapTemplate', {order_type:order_type,customer_id:customer_id,arap_type:type}, function(data){
                if(data){
                    for(var i = 0;i<data.length;i++){
                        var json_obj = JSON.parse(data[i].JSON_VALUE);
                        var li = '';
                        var li_val = '';
                        for(var j = 0;j<json_obj.length;j++){
                            li +='<li '
                                +' sp_name="'+json_obj[j].sp_name+'" '
                                +'charge_eng_id="'+json_obj[j].CHARGE_ENG_ID+'" '
                                +'charge_id="'+json_obj[j].CHARGE_ID+'" '
                                +'currency_id="'+json_obj[j].CURRENCY_ID+'" '
                                +'sp_id="'+json_obj[j].SP_ID+'" '
                                +'unit_id="'+json_obj[j].UNIT_ID+'" '
                                +'amount="'+json_obj[j].amount+'" '
                                +'charge_name="'+json_obj[j].charge_name+'" '
                                +'charge_eng_name="'+json_obj[j].charge_eng_name+'" '
                                +'currency_name="'+json_obj[j].currency_name+'" '
                                +'currency_total_amount="'+json_obj[j].currency_total_amount+'" '
                                +'exchange_currency_id="'+json_obj[j].exchange_currency_id+'" '
                                +'exchange_currency_name="'+json_obj[j].exchange_currency_name+'" '
                                +'exchange_currency_rate="'+json_obj[j].exchange_currency_rate+'" '
                                +'exchange_rate="'+json_obj[j].exchange_rate+'" '
                                +'exchange_total_amount="'+json_obj[j].exchange_total_amount+'" '
                                +'order_type="'+json_obj[j].order_type+'" '
                                +'price="'+json_obj[j].price+'" '
                                +'remark="'+json_obj[j].remark+'" '
                                +'total_amount="'+json_obj[j].total_amount+'" '
                                +'type="'+json_obj[j].type+'" '
                                +'unit_name="'+json_obj[j].unit_name+'" '
                                +'></li>';
                            li_val += '<span></span> '+json_obj[j].sp_name+' , '+json_obj[j].charge_name+' , '+json_obj[j].charge_eng_name+'<br/>';
                        }
                        
                        div.append('<ul class="used'+type+'Info" id="'+data[i].ID+'">'
                                +li
                                +'<div class="radio">'
                                +'  <a class="delete'+type+'Template" style="margin-right: 10px;padding-top: 5px;float: left;">删除</a>'
                                +'  <div class="select'+type+'Template" style="margin-left: 60px;padding-top: 0px;">'
                                +'      <input type="radio" value="1" name="used'+type+'Info">'
                                +       li_val
                                +'  </div>'
                                +'</div><hr/>'
                                +'</ul>');
                        
                    }
                }
            });
        }
    });
     $('#landcollapseChargeInfo').on('hide.bs.collapse', function () {
        var thisType = $(this).attr('id');
        var type = 'Charge';
        if('landcollapseChargeInfo'!=thisType){
            type='Cost';
        }
        $('#landcollapse'+type+'Icon').removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
    });

    $('#landChargeDiv,#landCostDiv').on('click','.deleteChargeTemplate,.deleteCostTemplate',function(){
        $(this).attr('disabled',true);
        var ul=$(this).parent().parent();
        var id=ul.attr('id');
        $.post('/jobOrder/deleteLandArapTemplate',{id:id},function(data){
            if(data){
                $.scojs_message('删除成功',$.scojs_message.TYPE_OK);
                $(this).attr('disabled',false);
                ul.css("display","none");
            }
        },'json').fail(function(){
            $(this).attr('disabled',false);
            $.scojs_message('删除失败',$.scojs_message.TYPE_ERROR);
        })
    });

    //回显
    $('#landChargeDiv,#landCostDiv').on('click','.selectChargeTemplate,.selectChargeTemplate',function(){
        $(this).parent().find('[type=radio]').prop('checked',true);
        var thisType=$(this).attr('class');
        var type='Charge';
        var table='charge_table';
        if('selectChargeTemplate'!=thisType){
            type='Cost';
            table='cost_table';
        }

        var li=$(this).parent().parent().find('li');
        var dataTable=$('#land_'+table).DataTable();

        for (var i = 0; i < li.length; i++) {
            var row=$(li[i]);
            var item={};
            item.ID='';
            item.TYPE=row.attr('type');
            item.SP_ID=row.attr('sp_id');
            item.CHARGE_ID= row.attr('charge_id');
            item.CHARGE_ENG_ID= row.attr('charge_eng_id');
            item.PRICE= row.attr('PRICE');
            item.AMOUNT= row.attr('amount');
            item.UNIT_ID= row.attr('unit_id');
            item.TOTAL_AMOUNT= row.attr('total_amount');
            item.CURRENCY_ID= row.attr('currency_id');
            item.EXCHANGE_RATE= row.attr('exchange_rate');
            item.CURRENCY_TOTAL_AMOUNT= row.attr('currency_total_amount');
            item.EXCHANGE_CURRENCY_ID= row.attr('exchange_currency_id');
            item.EXCHANGE_CURRENCY_RATE= row.attr('exchange_currency_rate');
            item.EXCHANGE_TOTAL_AMOUNT= row.attr('exchange_total_amount');
            item.REMARK= row.attr('remark');
            item.SP_NAME=row.attr('sp_name');
            item.CHARGE_NAME=row.attr('charge_name');
            item.CHARGE_ENG_NAME=row.attr('charge_eng_name');
            item.UNIT_NAME=row.attr('unit_name');
            item.CURRENCY_NAME=row.attr('currency_name');
            item.EXCHANGE_CURRENCY_ID_NAME=row.attr('exchange_currency_name');
            item.AUDIT_FLAG='';
            dataTable.row.add(item).draw();
        };
    });

    itemOrder.buildLandChargeTemplate=function(){
        var cargo_table_rows = $("#land_charge_table tr");
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
            item.order_type = "charge";//应收
            for(var i = 1; i < row.childNodes.length; i++){
                if($(row.childNodes[i]).find('.notsave').size()==0){
                    var el = $(row.childNodes[i]).find('input,select');
                    var name = el.attr('name'); 
                    
                    if(el && name){
                        if(name=='exchange_currency_id'&&el.val()==''){
                            el.val(el.parent().parent().parent().find('[name=CURRENCY_ID]').val());
                        }
                        if(name=='exchange_currency_rate'&&el.val()==''){
                            el.val(1);
                        }
                        if(name=='exchange_total_amount'&&el.val()==''){
                            el.val(el.parent().parent().find('[name=total_amount]').val());
                        }
                        
                        if(name.toLowerCase()!='unit_id'){
                            var value = el.val();//元素的值
                            item[name] = value;
                        }
                        

                        if(name.toLowerCase().indexOf("_id")>=0){
                            var id_value = $(row.childNodes[i]).find('[name='+name+'_input]').val();
                            var abbr = name.toLowerCase().replace('id','name');
                            if(abbr!='unit_name'){
                                item[abbr] = id_value;
                            }
                        }
                    }
                }
            }
            cargo_items_array.push(item);
        }
        return cargo_items_array;
    };

    itemOrder.buildLandAllChargeTemplate=function(){
        var cargo_table_rows = $("#land_charge_table tr");
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
            item.order_type = "charge";//应收
            for(var i = 1; i < row.childNodes.length; i++){
                    var el = $(row.childNodes[i]).find('input,select');
                    var name = el.attr('name'); 
                    
                    if(el && name){
                        if(name=='exchange_currency_id'&&el.val()==''){
                            el.val(el.parent().parent().parent().find('[name=CURRENCY_ID]').val());
                        }
                        if(name=='exchange_currency_rate'&&el.val()==''){
                            el.val(1);
                        }
                        if(name=='exchange_total_amount'&&el.val()==''){
                            el.val(el.parent().parent().find('[name=total_amount]').val());
                        }
                        var value = el.val();//元素的值
                        item[name] = value;

                        if(name.toLowerCase().indexOf("_id")>=0){
                            var id_value = $(row.childNodes[i]).find('[name='+name+'_input]').val();
                            var abbr = name.toLowerCase().replace('id','name');
                            item[abbr] = id_value;
                        }
                    }
            }
            cargo_items_array.push(item);
        }
        return cargo_items_array;
    };

    $('.land_charge').on('click',function(){
        deletedTableIds=[];
    })
    //删除一行
    $("#land_charge_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent().parent();
        deletedTableIds.push(tr.attr('id'));
        chargeTable.row(tr).remove().draw();
    }); 
    //添加一行
    $('#land_add_charge').on('click', function(){
        var item={};
        chargeTable.row.add(item).draw(true);
    });
    
    itemOrder.buildLandChargeDetail=function(){
        var cargo_table_rows = $("#land_charge_table tr");
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
            item.order_type = "charge";//应收
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
            if(typeof(id)=="undefined")continue;
            var item={
                id: id,
                action: 'DELETE'
            };
            cargo_items_array.push(item);
        }
        deletedTableIds = [];
        return cargo_items_array;
    };
    
  //弹出此陆运相关的费用
	var bindFieldEvent=function(){
    	eeda.bindTableField('land_charge_table','SP_ID','/serviceProvider/searchCompany','');
        // eeda.bindTableField('land_charge_table','CHARGE_ID','/finItem/search','');
        eeda.bindTableFieldChargeId('land_charge_table','CHARGE_ID','/finItem/search','');
        eeda.bindTableField('land_charge_table','CHARGE_ENG_ID','/finItem/search_eng','');
        eeda.bindTableField('land_charge_table','UNIT_ID','/serviceProvider/searchChargeUnit','');
        eeda.bindTableFieldCurrencyId('land_charge_table','CURRENCY_ID','/serviceProvider/searchCurrency','');
        eeda.bindTableFieldCurrencyId('land_charge_table','exchange_currency_id','/serviceProvider/searchCurrency','');
    };
    
    //------------事件处理
    var chargeTable = eeda.dt({
        id: 'land_charge_table',
        autoWidth: false,
        drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
            bindFieldEvent();
        },
        columns:[
			{"data": "ID","width": "10px",
			    "render": function ( data, type, full, meta ) {
			    	if(data)
			    		return '<input type="checkbox" class="checkBoxOfLandChargeTable" style="width:30px" checked>';
			    	else 
			    		return '<input type="checkbox" style="width:30px" disabled>';
			    }
			},
            { "width": "100px",
                "render": function ( data, type, full, meta ) {
                	var str="<nobr>";
                	if(full.ID&&full.AUDIT_FLAG == 'Y'){
                		str+= '<button type="button" class="delete btn btn-default btn-xs" style="width:60px" disabled>删除</button>&nbsp';
                		str+= '<button type="button" class="btn btn_green" style="width:60px"  disabled>确认</button> '; 
                		}
                	else if(full.ID){
                		str+= '<button type="button" class="delete btn btn-default btn-xs" style="width:60px" >删除</button>&nbsp';
                		str+= '<button type="button" class="chargeConfirm btn btn_green" style="width:60px" value="'+full.ID+'" >确认</button> ';		
                	}else{
                		str+= '<button type="button" class="delete btn btn-default btn-xs" style="width:60px">删除</button>&nbsp';
                		str+= '<button type="button" class="btn btn_green" style="width:60px"  disabled>确认</button> ';
                	}
                	str +="</nobr>";
                    return str;
                }
            },
            { "data": "TYPE", "width": "80px", 
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		var str = '<select name="type" class="form-control search-control notsave" style="width:100px" disabled>'
	                        +'<option value="陆运" '+(data=='陆运' ? 'selected':'')+'>陆运</option>'
	                        +'<option value="海运" '+(data=='海运' ? 'selected':'')+'>海运</option>'
	                        +'<option value="空运" '+(data=='空运' ? 'selected':'')+'>空运</option>'
	                        +'<option value="报关" '+(data=='报关' ? 'selected':'')+'>报关</option>'
	                        +'<option value="保险" '+(data=='保险' ? 'selected':'')+'>保险</option>'
	                        +'</select>';
	                	return str;
                	}else{
                    var str = '<select name="type" class="form-control search-control notsave" style="width:100px">'
	                    	+'<option value="陆运" '+(data=='陆运' ? 'selected':'')+'>陆运</option>'
	                        +'<option value="海运" '+(data=='海运' ? 'selected':'')+'>海运</option>'
	                        +'<option value="空运" '+(data=='空运' ? 'selected':'')+'>空运</option>'
	                        +'<option value="报关" '+(data=='报关' ? 'selected':'')+'>报关</option>'
	                        +'<option value="保险" '+(data=='保险' ? 'selected':'')+'>保险</option>'
                            +'</select>';
                    return str;
                  }
                }
            },
            { "data": "SP_ID", "width": "180px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		if(!data)
                            data='';
                        var field_html = template('table_dropdown_template',
                            {
                                id: 'SP_ID',
                                value: data,
                                display_value: full.SP_NAME,
                                style:'width:200px',
                                disabled:'disabled'
                            }
                        );
                        return field_html;
                  }else{
                    if(!data)
                        data='';
                    var field_html = template('table_dropdown_template',
                        {
                            id: 'SP_ID',//对应数据库字段
                            value: data,
                            display_value: full.SP_NAME,
                            style:'width:200px'
                        }
                    );
                    return field_html;
                  }
                }
            },
            
            { "data": "CHARGE_ID", "width": "180px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                		if(!data)
                            data='';
                        var field_html = template('table_dropdown_template',
                            {
                                id: 'CHARGE_ID',
                                value: data,
                                display_value: full.CHARGE_NAME,
                                style:'width:200px',
                                disabled:'disabled'
                            }
                        );
                        return field_html;
                     }else{
                    if(!data)
                        data='';
                    var field_html = template('table_dropdown_template',
                        {
                            id: 'CHARGE_ID',//对应数据库字段
                            value: data,
                            display_value: full.CHARGE_NAME,
                            style:'width:200px'
                        }
                    );
                    return field_html;
                }
              }
            },
            { "data": "CHARGE_ENG_ID", "width": "180px",
            	"render": function ( data, type, full, meta ) {
        			if(!data)
        				data='';
        			var field_html = template('table_dropdown_template',
        					{
        				id: 'CHARGE_ENG_ID',
        				value: data,
        				display_value: full.CHARGE_ENG_NAME,
        				style:'width:200px',
        				disabled:'disabled'
        					}
        			);
        			return field_html;
            	}
            },
            { "data": "PRICE", "width": "120px",
                "render": function ( data, type, full, meta ) {
                	if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                    	str = '';
                	if(full.AUDIT_FLAG == 'Y'){
                    	return '<input type="text" name="price" style="width:120px" value="'+str+'" class="form-control notsave" disabled />';
                     }else{
	                    return '<input type="text" name="price" style="width:120px" value="'+str+'" class="form-control notsave" />';
	                 }
                  }
            },
            { "data": "AMOUNT","width": "80px",
                "render": function ( data, type, full, meta ) {
                	if(!data)
                        data='';
                	if(full.AUDIT_FLAG == 'Y'){
                        return '<input type="text" name="amount" min="0" style="width:80px" value="'+data+'" class="form-control notsave" disabled/>';
                     }else{
                    	 
	                    return '<input type="text" name="amount" min="0" style="width:80px" value="'+data+'" class="form-control notsave"/>';
	                }
                }
            },
            { "data": "UNIT_ID", "width": "60px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
                   	 if(!data)
                            data='';
                        var field_html = template('table_dropdown_template',
                            {
                                id: 'UNIT_ID',
                                value: data,
                                display_value: full.UNIT_NAME,
                                style:'width:80px',
                                disabled:'disabled'
                            }
                        );
                        return field_html;
                   }else{
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
                }
            },
            { "data": "TOTAL_AMOUNT", "width": "150px","class":"currency_total_amount",
                "render": function ( data, type, full, meta ) {
                	if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                    	str = '';
	                return '<input type="text" name="total_amount" style="width:150px" value="'+str+'" class="form-control notsave" disabled/>';
                }
            },
            { "data": "CURRENCY_ID", "width": "60px","className":"currency_name",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
	                	if(!data)
	                        data='';
	                    var field_html = template('table_dropdown_template',
	                        {
	                            id: 'CURRENCY_ID',
	                            value: data,
	                            display_value: full.CURRENCY_NAME,
	                            style:'width:80px',
	                            disabled:'disabled'
	                        }
	                    );
	                    return field_html;
                }else{
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
                }
            },
            { "data": "EXCHANGE_RATE", "width": "80px",
                "render": function ( data, type, full, meta ) {
                	if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                    	str = '';
                	if(full.AUDIT_FLAG == 'Y'){
                        return '<input type="text" name="exchange_rate" style="width:100px" value="'+str+'" class="form-control" disabled />';
                    } else{
	                    return '<input type="text" name="exchange_rate" style="width:100px" value="'+str+'" class="form-control" />';
	                }
                }
            },
            { "data": "CURRENCY_TOTAL_AMOUNT", "width": "150px","class":"cny_total_amount",
                "render": function ( data, type, full, meta ) {
                	if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                    	str = '';
	                return '<input type="text" name="currency_total_amount" style="width:150px" value="'+str+'" class="form-control notsave" disabled/>';
                }
            },
            { "data": "EXCHANGE_CURRENCY_ID", "width":"80px","className":"cny_to_other",
            	"render": function ( data, type, full, meta ) {
            		if(full.AUDIT_FLAG == 'Y'){
            			if(!data)
            				data='';
            			var field_html = template('table_dropdown_template',
            					{
		            				id: 'exchange_currency_id',
		            				value: data,
		            				display_value: full.EXCHANGE_CURRENCY_ID_NAME,
		            				style:'width:100px',
		            				disabled:'disabled'
            					}
            			);
            			return field_html;
            		}else{
            			if(!data)
            				data='';
            			var field_html = template('table_dropdown_template',
            					{
		            				id: 'exchange_currency_id',
		            				value: data,
		            				display_value: full.EXCHANGE_CURRENCY_ID_NAME,
		            				style:'width:100px'
            					}
            			);
            			return field_html; 
            		}
            	}
            },
            { "data": "EXCHANGE_CURRENCY_RATE", "width": "80px","className":"exchange_currency_rate", 
            	"render": function ( data, type, full, meta ) {
            		if(data)
            			var str =  parseFloat(data).toFixed(2);
            		else
            			str = '';
            		if(full.AUDIT_FLAG == 'Y'){
            			return '<input type="text" name="exchange_currency_rate" style="width:100px" value="'+str+'" class="form-control" disabled />';
            		}else{
            			return '<input type="text" name="exchange_currency_rate" style="width:100px" value="'+str+'" class="form-control" />';
            		}
            	}
            },
            { "data": "EXCHANGE_TOTAL_AMOUNT", "width": "150px","className":"exchange_total_amount",
            	"render": function ( data, type, full, meta ) {
            		if(data)
            			var str =  parseFloat(data).toFixed(2);
            		else
            			str = '';
            		return '<input type="text" name="exchange_total_amount" style="width:150px" value="'+str+'" class="form-control notsave" disabled />';
            	}
            },
            { "data": "REMARK","width": "180px",
                "render": function ( data, type, full, meta ) {
                	if(full.AUDIT_FLAG == 'Y'){
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="remark" style="width:200px" value="'+data+'" class="form-control" disabled />';
	                }else{
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="remark" style="width:200px" value="'+data+'" class="form-control" />';
	                }
                }
            }, { "data": "SP_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            }, 
            { "data": "CHARGE_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            },
            { "data": "CHARGE_ENG_NAME", "visible": false,
            	"render": function ( data, type, full, meta ) {
            		if(!data)
            			data='';
            		return data;
            	}
            },
            { "data": "UNIT_NAME", "visible": false,
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
            { "data": "EXCHANGE_CURRENCY_ID_NAME", "visible": false,
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
	
	$('#land_charge_table').on("blur","[name=price],[name=amount],[name=exchange_rate],[name=EXCHANGE_CURRENCY_RATE]",function(){
		self = $(this)
		data = self.val()
		data = $.trim(data)
		if(isNaN(data)){   
			self.parent().append("<span style='color:red'>请输入数字！！！</span>")
		}
	})
	
	$('#land_charge_table').on("focus","[name=price],[name=amount],[name=volume],[name=vgm]",function(){
    		self = $(this)
    		self.parent().find("span").remove()
    })
    
    
    $('#land_charge_table_msg').on('click','.save',function(){
    	$(this).attr('disabled', true);
    	var order_id = $('#order_id').val();
    	var order = {};
    	order.order_id = order_id;
    	order.land_item_id = $('#land_item_id').val();
    	order.land_charge_item = itemOrder.buildLandChargeDetail();

    	$.post('/jobOrder/saveLandCharge', {params:JSON.stringify(order)}, function(data){
    		 
    		 var url = "/jobOrder/tableListOfLandCharge?order_id="+order_id+"&land_item_id="+land_item_id;
        	 chargeTable.ajax.url(url).load();
        	 itemOrder.refleshChargeTable(order_id);
             itemOrder.refleshCostTable(order_id);
    		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
    		$('#land_charge_table_msg .save').attr('disabled', false);
    	 },'json').fail(function() {
             $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
             $('#land_charge_table_msg .save').attr('disabled', false);
         });
    })
    
    $('#land_table').on('click','.land_charge',function(){
    		var land_item_id = $(this).parent().parent().attr('id');
            $('#landcollapseChargeInfo').removeClass('in');
    		$('#land_item_id').val(land_item_id);
    		var order_id = $('#order_id').val();
    		var url = "/jobOrder/tableListOfLandCharge?order_id="+order_id+"&land_item_id="+land_item_id;
        	chargeTable.ajax.url(url).load();
        	$('#land_charge_table_msg_btn').click();
        	
        	//费用明细确认按钮动作
            $("#land_charge_table").on('click', '.chargeConfirm', function(){
            	var id = $(this).parent().parent().parent().attr('id');
            	$.post('/jobOrder/feeConfirm',{id:id},function(data){
            		var url = "/jobOrder/tableListOfLandCharge?order_id="+order_id+"&land_item_id="+land_item_id;
                	chargeTable.ajax.url(url).load();
            		$.scojs_message('确认成功', $.scojs_message.TYPE_OK);
            		itemOrder.refleshChargeTable(order_id);
            	},'json').fail(function() {
                    $.scojs_message('确认失败', $.scojs_message.TYPE_ERROR);
               });
               
            });
    })
    //柜货派车信息表
    $('#land_shipment_table').on('click','.land_charge',function(){
		var land_item_id = $(this).parent().parent().attr('id');
        $('#landcollapseChargeInfo').removeClass('in');
		$('#land_item_id').val(land_item_id);
		var order_id = $('#order_id').val();
		var url = "/jobOrder/tableListOfLandCharge?order_id="+order_id+"&land_item_id="+land_item_id;
    	chargeTable.ajax.url(url).load();
    	$('#land_charge_table_msg_btn').click();
    	
    	//费用明细确认按钮动作
        $("#land_charge_table").on('click', '.chargeConfirm', function(){
        	var id = $(this).parent().parent().parent().attr('id');
        	$.post('/jobOrder/feeConfirm',{id:id},function(data){
        		var url = "/jobOrder/tableListOfLandCharge?order_id="+order_id+"&land_item_id="+land_item_id;
            	chargeTable.ajax.url(url).load();
        		$.scojs_message('确认成功', $.scojs_message.TYPE_OK);
        		itemOrder.refleshChargeTable(order_id);
        	},'json').fail(function() {
                $.scojs_message('确认失败', $.scojs_message.TYPE_ERROR);
           });
           
        });
})
   
  //数量和单价自动补零
    $('#land_charge_table').on('blur','[name=price],[name=amount]',function(){
    	var amount = $(this).val();
    	if(amount!=''&&!isNaN(amount)){
    		$(this).val(itemOrder.returnFloat(amount));
    	}
    })
    
    //输入 数量*单价的时候，计算金额
    $('#land_charge_table').on('keyup','[name=price],[name=amount],[name=exchange_rate],[name=exchange_currency_rate]',function(){
    	var row = $(this).parent().parent();
    	var price = $(row.find('[name=price]')).val();
    	var amount = $(row.find('[name=amount]')).val();
    	var exchange_rate = $(row.find('[name=exchange_rate]')).val();
    	var exchange_currency_rate = $(row.find('[name=exchange_currency_rate]')).val();
    	if(price==''||amount==''){
    		$(row.find('[name=total_amount]')).val('');
    		$(row.find('[name=currency_total_amount]')).val('');
    		$(row.find('[name=exchange_total_amount]')).val('');
    	}
    	if(price!=''&&amount!=''&&!isNaN(price)&&!isNaN(amount)){
    		var total_amount = parseFloat(price)*parseFloat(amount);
    		$(row.find('[name=total_amount]')).val(total_amount);
    		if(exchange_rate==''){
    			$(row.find('[name=currency_total_amount]')).val('');
    		}
    		if(exchange_rate!=''&&!isNaN(exchange_rate)){
    			$(row.find('[name=currency_total_amount]')).val((total_amount*parseFloat(exchange_rate)).toFixed(3));
    			if(exchange_currency_rate==''){
        			$(row.find('[name=exchange_total_amount]')).val('');
        		}
    			if(exchange_currency_rate!=''&&!isNaN(exchange_currency_rate)){
    				$(row.find('[name=exchange_total_amount]')).val((total_amount*parseFloat(exchange_currency_rate).toFixed(3)));
        		}
    		}
    	}
    });
    
  //选择是否是同一结算公司
	var cnames = [];
	$('#land_charge_table').on('click','input[type="checkbox"]',function () {
			var cname = $($(this).parent().parent().find('[name="SP_ID"]')).val();
			
			if($(this).prop('checked')==true){	
				if(cnames.length > 0 ){
					if(cnames[0]!=cname){
						$.scojs_message('打印PDF时,请选择同一个结算公司', $.scojs_message.TYPE_ERROR);
						$(this).attr('checked',false);
						return false;
					}else{
						cnames.push(cname);
					}
				}else{
					cnames.push(cname);	
				}
			}else{
				cnames.pop(cname);
		 }
	 });

	
	
	//全选
    $('#land_AllCheckOfChargeTable').click(function(){
	    $(".checkBoxOfLandChargeTable").prop("checked",this.checked);
	    
	    var hava_check = 0;
    	$('#land_charge_table input[type="checkbox"]:checked').each(function(){	
    		hava_check++;
    	})
	    if(this.checked==true&&$('#land_charge_table td').length>1&&hava_check>0){
	    	$('#print_debit_note').attr('disabled',false);
	    }else{
	    	$('#print_debit_note').attr('disabled',true);
	    }
    });
    $("#land_charge_table").on('click','.checkBoxOfLandChargeTable',function(){
		  $("#land_AllCheckOfChargeTable").prop("checked",$(".checkBoxOfLandChargeTable").length == $(".checkBoxOfLandChargeTable:checked").length ? true : false);
    });
    
    //存为模板
    $('#landBtnTemplet').click(function(){
    	var landOrderTemplet={};
    	landOrderTemplet.customer_id = $('#customer_id').val();
    	landOrderTemplet.type = $('#type').val();
    	landOrderTemplet.land_charge_template = itemOrder.buildLandChargeTemplate();
    	landOrderTemplet.land_allCharge_template = itemOrder.buildLandAllChargeTemplate();
    	$.post('/jobOrder/saveLandTemplet',{params:JSON.stringify(landOrderTemplet)},function(data){
    		$.scojs_message('陆运费用信息模板保存成功', $.scojs_message.TYPE_OK);
    	});
    });
	
  });
});