define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
$(document).ready(function() {

	var deletedTableIds=[];
    //删除一行
    $("#air_table").on('click', '.delete', function(){
        var tr = $(this).parent().parent();
        tr.css("display","none");
        deletedTableIds.push(tr.attr('id'))
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

        eeda.bindTableField('air_table','AIR_COMPANY','/serviceProvider/searchAirCompany','air');
        eeda.bindTableField('air_table','START_FROM','/location/searchPort','air_port');
        eeda.bindTableField('air_table','DESTINATION','/location/searchPort','air_port');
    };
    //------------事件处理
    var cargoTable = eeda.dt({
	    id: 'air_table',
	    autoWidth: false,
	    drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
	        bindFieldEvent();
	    },
	    columns:[
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
            { "data": "FLIGHT_NO", "width": "130px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="flight_no" value="'+data+'" class="form-control" style="width:150px"/>';
                }
            },
            { "data": "VOYAGE_NO", "width": "140px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="voyage_no" value="'+data+'" class="form-control" style="width:160px"/>';
                }
            },
            { "data": "START_FROM", "width":"130px",
                "render": function ( data, type, full, meta ) {
                if(!data)
                       data='';
                   var field_html = template('table_dropdown_template',
                       {
                           id: 'START_FROM',
                           value: data,
                           display_value: full.START_FROM_NAME,
                           style:'width:150px'
                       }
                   );
                   return field_html; 
                }
            },
            { "data": "ETD", "width": "160px",
            	"render": function ( data, type, full, meta ) {
            		 if(!data)
	                        data='';
	                    var field_html = template('table_date_field_template',
		                    {
		                        id: 'ETD',
		                        value: data.substr(0,19),
		                        style:'width:170px'
		                    }
		                );
	                    return field_html;
            	}
            },            
            { "data": "DESTINATION", "width":"130px",
                "render": function ( data, type, full, meta ) {
                if(!data)
                       data='';
                   var field_html = template('table_dropdown_template',
                       {
                           id: 'DESTINATION',
                           value: data,
                           list:'list',
                           display_value: full.DESTINATION_NAME,
                           style:'width:150px'
                       }
                   );
                   return field_html; 
                }
            },
            { "data": "ETA", "width": "160px",
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
            }, 
            { "data": "AIR_COMPANY_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            }, 
            { "data": "START_FROM_NAME", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            }, 
            { "data": "DESTINATION_NAME", "visible": false,
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
    
    $("[name=hawb_no],[name=mawb_no],[name=shipping_mark],[name=goods_mark],[name=booking_mark]").on("blur",function(){
		self = $(this);
		data = self.val();
		len = $.trim(data).length;
		var name = self.attr("name");
		if(name=="hawb_no"||name=="mawb_no"){
			var re = /^.{50,}$/g;
			if(len!=0&&re.test(data)){
				self.parent().append("<p><span style='color:red' class='error_span'>请输入长度50以内的字符串</span></p>")
			}
		}
		if(name=="shipping_mark"){
			var re = /^.{100,}$/g;
			if(len!=0&&re.test(data)){
				self.parent().append("<p><span style='color:red' class='error_span'>请输入长度100以内的字符串</span></p>")
			}
		}
		if(name=="goods_mark"){
			var re = /^.{2000,}$/g;
			if(len!=0&&re.test(data)){
				self.parent().append("<p><span style='color:red' class='error_span'>请输入长度2000以内的字符串</span></p>")
			}
		}
		if(name=="booking_mark"){
			var re = /^.{255,}$/g;
			if(len!=0&&re.test(data)){
				self.parent().append("<p><span style='color:red' class='error_span'>请输入长度255以内的字符串</span></p>")
			}
		}
	})
	
	//航班号、航次校验
	$('#air_table').on("blur","[name=flight_no],[name=voyage_no]",function(){
		self = $(this);
		data = self.val();
		len = $.trim(data).length;
		var re = /^.{100,}$/g;
		if(re.test(data)&&len!=0){   
			self.parent().append("<span style='color:red' class='error_span'>请输入长度100以内的字符串</span>");
		}
	})
	
	$("[name=hawb_no],[name=mawb_no],[name=shipping_mark],[name=goods_mark],[name=booking_mark]").on("focus",function(){
    		self = $(this)
    		self.parent().find("span").remove()
    })
    
	$('#air_table').on("focus","[name=flight_no],[name=voyage_no]",function(){
		self = $(this)
		self.parent().find("span").remove()
	})
    
    //刷新明细表
    itemOrder.refleshAirItemTable = function(order_id){
    	var url = "/jobOrder/tableList?order_id="+order_id+"&type=air";
    	cargoTable.ajax.url(url).load();
    }
    

});
});