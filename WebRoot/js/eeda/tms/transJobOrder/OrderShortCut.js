define(['jquery', 'metisMenu', 'template','sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','datetimepicker_CN'], function ($, metisMenu,template) {
  $(document).ready(function() {
  	document.title = '工作单查询   | '+document.title;
  	  
      var deletedTableIds=[];
        var bindFieldEvent=function(){
       	    $('table .date').datetimepicker({  
       	        format: 'yyyy-MM-dd hh:mm:ss',  
       	        language: 'zh-CN'
       	    }).on('changeDate', function(el){
       	        $(".bootstrap-datetimepicker-widget").hide();   
       	        $(el).trigger('keyup');
       	    });
            	
            eeda.bindTableField('eeda-table','CUSTOMER_ID','/serviceProvider/searchCompany','');
            eeda.bindTableField('eeda-table','CHARGE_ID','/finItem/search','');
            eeda.bindTableFieldCarInfo('eeda-table', 'TIJIGUI_CAR_NO');
            eeda.bindTableFieldCarInfo('eeda-table', 'YIGUI_CAR_NO');
            eeda.bindTableFieldCarInfo('eeda-table', 'SHOUZHONGGUI_CAR_NO');
            eeda.bindTableFieldDockInfo('eeda-table','TAKE_WHARF');
            eeda.bindTableFieldDockInfo('eeda-table','BACK_WHARF');
            eeda.bindTableFieldCurrencyId('eeda-table','CURRENCY_ID','/serviceProvider/searchCurrency','');
     };
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
          paging: true,
          // serverSide: true, //不打开会出现排序不对
          // ajax: "/transJobOrder/list?",
          "drawCallback":function(settings){
            bindFieldEvent();
          },
          columns: [
				    { "data":"ID","width": "10px",
				        "render": function ( data, type, full, meta ) {
				        	if(data)
				        		return '<input type="checkbox" class="checkBox" style="width:30px">';
				        	else 
				        		return '<input type="checkbox" class="checkBox"  style="width:30px" >';
				        }
				    },
              { "data":"ID","width": "30px",
                  "render": function ( data, type, full, meta ) {
                    return '<button type="button" class="delete btn btn-default btn-xs" style="width:50px"  >删除</button>';
                  }
              },
              { "data": "CREATE_STAMP", 
                  "render": function ( data, type, full, meta ) {
                    if(!data)
                    data='';
                   var field_html = template('table_date_field_template',
                           {
                               id: 'create_stamp',
                               value: data,
                               display_value: full.effective_time,
                               style:'width:130px'
                           }
                       );
                  return field_html;
                }
              },
              { "data": "SO_NO",
                  "render": function ( data, type, full, meta ) {
                      if(!data)
                        data = '';
                          return '<input type="text" name="so_no" style="width:100px" value="'+data+'" class="form-control"/>';
                     }
               }, 
              { "data": "CUSTOMER_ID", 
                  "render": function ( data, type, full, meta ) {
                     if(!data)
                            data='';
                        var field_html = template('table_dropdown_template',
                            {
                                id: 'customer_id',
                                value: data,
                                display_value: full.CUSTOMER_ID_INPUT,
                                required:'required',
                                style:'width:200px',
                            }
                        );
                        return field_html;
                    }
               }, 
              { "data": "TYPE",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    var str= '<select name="type" class="form-control search-control"  style="width:100px">'
                        +'<option value="进口柜货" '+(data=='进口柜货' ? 'selected':'')+'>进口柜货</option>'
                        +'<option value="出口散货" '+(data=='出口散货' ? 'selected':'')+'>出口散货</option>'
                        +'<option value="进口散货" '+(data=='进口散货' ? 'selected':'')+'>进口散货</option>'
                        +'<option value="出口空运" '+(data=='出口空运' ? 'selected':'')+'>出口空运</option>'
                        +'<option value="进口空运" '+(data=='进口空运' ? 'selected':'')+'>进口空运</option>'
                        +'<option value="内贸海运" '+(data=='内贸海运' ? 'selected':'')+'>内贸海运</option>'
                        +'<option value="香港头程" '+(data=='香港头程' ? 'selected':'')+'>香港头程</option>'
                        +'<option value="香港游" '+(data=='香港游' ? 'selected':'')+'>香港游</option>'
                        +'<option value="陆运" '+(data=='陆运' ? 'selected':'')+'>陆运</option>'
                        +'<option value="报关" '+(data=='报关' ? 'selected':'')+'>报关</option>'
                        +'<option value="快递" '+(data=='快递' ? 'selected':'')+'>快递</option>'
                        +'<option value="加贸" '+(data=='加贸' ? 'selected':'')+'>加贸</option>'
                        +'<option value="贸易" '+(data=='贸易' ? 'selected':'')+'>贸易</option>'
                        +'<option value="园区游" '+(data=='园区游' ? 'selected':'')+'>园区游</option>'
                         +'</select>';
                    return str;
                }
             }, 
              { "data": "CONTAINER_NO",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data = '';
                          return '<input type="text" name="container_no" style="width:100px" value="'+data+'" class="form-control"/>';
                     }
              }, 
              { "data": "CABINET_TYPE",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    var str= '<select id="cabinet_type" name="CABINET_TYPE" class="form-control search-control valid">'
                             +'  <option value=""></option> '
                             +'<option value="20GP" '+(data=='20GP' ? 'selected':'')+'>20GP</option>'
                             +'<option value="40GP" '+(data=='40GP' ? 'selected':'')+'>40GP</option>'
                             +'<option value="40HQ" '+(data=='40HQ' ? 'selected':'')+'>40HQ</option>'
                             +'<option value="45GP" '+(data=='45GP' ? 'selected':'')+'>45GP</option>'
                             +'</select>'; 
                    return str;
                }
            },
              { "data": "TIJIGUI_CAR_NO",
                "render": function ( data, type, full, meta ) {
                   if(!data)
                        data='';
                    var field_html = template('table_car_no_field_template',
                        {
                            id: 'tijigui_car_no',  //component_id 便于用 #id取组件
                            value: data,
                            display_value: full.TIJIGUI_CAR_NO_INPUT,
                            style:'width:200px'
                        }
                    );
                     return field_html;
                 }
            }, 
              { "data": "YIGUI_CAR_NO",
                "render": function ( data, type, full, meta ) {
                   if(!data)
                        data='';
                    var field_html = template('table_car_no_field_template',
                        {
                            id: 'yigui_car_no',  //component_id 便于用 #id取组件
                            value: data,
                            display_value: full.YIGUI_CAR_NO_INPUT,
                            style:'width:200px'
                        }
                    );
                     return field_html;
                 }
            }, 
              { "data": "SHOUZHONGGUI_CAR_NO",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    var field_html = template('table_car_no_field_template',
                        {
                            id: 'shouzhonggui_car_no',  //component_id 便于用 #id取组件
                            value: data,
                            display_value: full.SHOUZHONGGUI_CAR_NO_INPUT,
                            style:'width:200px'
                        }
                    );
                     return field_html;
                 }
            },
              { "data": "TAKE_WHARF",
                "render": function ( data, type, full, meta ) {
                     if(!data)
                            data='';
                        var field_html = template('table_dock_no_field_template',
                            {
                                id: 'take_wharf',
                                value: data,
                                display_value: full.TAKE_WHARF_INPUT,
                                style:'width:200px',
                            }
                        );
                        return field_html;
                    }
              }, 
              { "data": "BACK_WHARF",
                "render": function ( data, type, full, meta ) {
                      if(!data)
                            data='';
                        var field_html = template('table_dock_no_field_template',
                            {
                                id: 'back_wharf',
                                value: data,
                                display_value: full.BACK_WHARF_INPUT,
                                style:'width:200px',
                            }
                        );
                        return field_html;
                    }
              },
             { "data": "CHARGE_ID", 
                  "render": function ( data, type, full, meta ) {
                	  if(!data)
                          data='';
                      var field_html = template('table_dropdown_template',
                          {
                              id: 'CHARGE_ID',
                              value: data,
                              display_value: full.CHARGE_NAME_INPUT,
                              style:'width:200px'
                          }
                      );
                      return field_html;
                  }
             },
            { "data": "TOTAL_AMOUNT", "width": "80px","className":"currency_total_amount",
              "render": function ( data, type, full, meta ) {
                if(data)
                      var str =  parseFloat(data).toFixed(2);
                  else
                    str = '';
                    return '<input type="text" name="total_amount" style="width:100px" value="'+str+'" class="form-control"  />';
               }
            },
            { "data": "CURRENCY_ID",
                     "render": function ( data, type, full, meta ) {
                     if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'CURRENCY_ID',
                               value: data,
                               display_value: full.CURRENCY_ID_INPUT,
                               style:'width:80px'
                           }
                       );
                       return field_html; 
                    }
            },
            { "data": "EXCHANGE_RATE", "width": "80px", "className":"currency_rate",
                "render": function ( data, type, full, meta ) {
                  if(data)
                        var str =  parseFloat(data).toFixed(2);
                    else
                      str = '1.000000';
                      return '<input type="text" name="exchange_rate" style="width:100px" value="'+str+'" class="form-control" />';
                }
            },
            { "data": "CURRENCY_TOTAL_AMOUNT", "width": "100px","className":"cny_total_amount",
                "render": function ( data, type, full, meta ) {
                  if(data) var str =  parseFloat(data).toFixed(2);
                    else
                      str = '';
                      return '<input type="text" name="currency_total_amount" style="width:120px" value="'+str+'" class="form-control" disabled/>';
                 }
            },
            { "data": "REMARK","width": "180px",
                "render": function ( data, type, full, meta ) {
                  if(!data)
                        data = '';
                          return '<input type="text" name="remark" style="width:100px" value="'+data+'" class="form-control"/>';
                     }
            }
          ]
      });

     
       $('#add_land').on('click', function(){
          var create_stamp=$('#sent_out_time').val();
          var so_no=$('#so_no').val();
          var customer_id=$('#customer_id').val();
          var type=$('#type').val();
          var container_no=$('#container_no').val();
          var cabinet_type=$('#cabinet_type').val();
          var tijigui_car_no=$('#tiji_car_no').val();
          var yigui_car_no=$('#yi_car_no').val();
          var shouzhonggui_car_no=$('#shouzhong_car_no').val();
          var take_wharf=$('#take_wharf').val();
          var back_wharf=$('#back_wharf').val();
          var remark=$('#remark').val();
          var customer_id_input=$('#customer_id_input').val();
          var tijigui_car_no_input=$('#tiji_car_no_input').val();
          var yigui_car_no_input=$('#yi_car_no_input').val();
          var shouzhonggui_car_no_input=$('#shouzhong_car_no_input').val();
          var take_wharf_input=$('#take_wharf_input').val();
          var back_wharf_input=$('#back_wharf_input').val();
          // var =$('#').val();CUSTOMER_ID_input
          var item={"CREATE_STAMP":create_stamp,"SO_NO":so_no,"CUSTOMER_ID":customer_id,"TYPE":type,
                    "CONTAINER_NO":container_no,"CABINET_TYPE":cabinet_type,"TIJIGUI_CAR_NO":tijigui_car_no,"YIGUI_CAR_NO":yigui_car_no,
                    "SHOUZHONGGUI_CAR_NO":shouzhonggui_car_no,"TAKE_WHARF":take_wharf,"BACK_WHARF":back_wharf,"REMARK":remark,
                    "CUSTOMER_ID_INPUT":customer_id_input,"TIJIGUI_CAR_NO_INPUT":tijigui_car_no_input,"YIGUI_CAR_NO_INPUT":yigui_car_no_input,
                    "SHOUZHONGGUI_CAR_NO_INPUT":shouzhonggui_car_no_input,"TAKE_WHARF_INPUT":take_wharf_input,"BACK_WHARF_INPUT":back_wharf_input,};
          dataTable.row.add(item).draw(true);
          $('#eeda-table [type="checkbox"]');
      });
       $('#eeda-table').on('click','.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();        
        dataTable.row(tr).remove().draw();
      });
       //清空
        $('#resetBtn').on('click', function(e){
          $('#orderForm')[0].reset();
      });

        //全选
       $('#allCheckOfLand').on('click', function(){
          if($(this).prop('checked')){
            $('#eeda-table [type="checkbox"]').prop('checked',true);
            $('#create').prop('disabled',false);
          }else{
            $('#eeda-table [type="checkbox"]').prop('checked',false);
            $('#create').prop('disabled',true);
          }
      });


        $('#eeda-table').on('click','.checkBox',function(){
          if($('#eeda-table [type="checkbox"]:checked').size()>0){
              $('#create').prop('disabled',false);
            }else{
              $('#create').prop('disabled',true);
            }

            $('#allCheckOfLand').prop('checked',$('#eeda-table .checkBox:checked').length==$('#eeda-table .checkBox').length)
      });

       //创建托运工作单
       $('#create').on('click', function(){
         var order={};
         var error=0;
          $("#eeda-table tr").each(function(index,item){
            if($(item).find('[type=checkbox]').prop('checked')){
              if(!$(item).find('[name=CUSTOMER_ID_input]').val()){

               $.scojs_message('第'+index+'行未选择客户', $.scojs_message.TYPE_ERROR);
                   error++;
                  }
            }
          })
         if(error>0){
          return;
         }
          order.itemList=itemOrder.buildItemList();
          $.post("/transOrderShortCut/create",{params:JSON.stringify(order)},function(data){
              if(data.IDS){
                  $.scojs_message('全部创建成功', $.scojs_message.TYPE_OK);
              }               
             }).fail(function() {
             $.scojs_message('创建失败', $.scojs_message.TYPE_ERROR);
         });
      });
        
     //将上框内容应用到表中
     $('#add_on').on('click',function(){
      //当客户为空的列，添加
        var create_stamp=$('#sent_out_time').val();customer_id
           // dataTable.row.add(item).draw(true);
          var cargo_table_rows = $("#eeda-table tr");
          for(var index=1; index<cargo_table_rows.length; index++){
              var row = cargo_table_rows[index];
              var empty = $(row).find('.dataTables_empty').text();
              if(empty)
                continue;
              for(var i = 1; i < row.childNodes.length; i++){
                var name = $(row.childNodes[i]).find('input,select').attr('name');
               name = name.toLowerCase()
                var value = $('#'+name).val();
                if(name&&value!=undefined&&value){
                   $(row).find('[name='+name.toLowerCase()+']').val(value);
                }
              }
          }
       });

      
       itemOrder.buildItemList=function(){
          var cargo_table_rows = $("#eeda-table tr");
          var cargo_items_array=[];
          for(var index=1; index<cargo_table_rows.length; index++){
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
                if(name=='TIJIGUI_CAR_NO'){
                  item[name]=$(row.childNodes[i]).find('input,select').attr('car_id')
                }
                if(name=='YIGUI_CAR_NO'){
                  item[name]=$(row.childNodes[i]).find('input,select').attr('car_id')
                }
                if(name=='SHOUZHONGGUI_CAR_NO'){
                  item[name]=$(row.childNodes[i]).find('input,select').attr('car_id')
                }
              }
              item.action = id.length > 0?'UPDATE':'CREATE';
              if($(row).find('[type=checkbox]').prop('checked')){
                    cargo_items_array.push(item);
                    dataTable.row(row).remove().draw();
                 }
          }

          return cargo_items_array;
       }

  });
});