define(['jquery', 'metisMenu', 'template','sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','pageguide','datetimepicker_CN','dtColReorder'], function ($, metisMenu,template) {
  $(document).ready(function() {
	  tl.pg.init({
          pg_caption: '本页教程'
      });
      var deletedTableIds=[];
      	var bindFieldEvent=function(){
       	    $('table .date').datetimepicker({  
       	        format: 'yyyy-MM-dd hh:mm:ss',  
       	        language: 'zh-CN'
       	    }).on('changeDate', function(el){
       	        $(".bootstrap-datetimepicker-widget").hide();   
       	        $(el).trigger('keyup');
       	    });
            	
       	    $('#closing_date_div').datetimepicker({  
       	    	format:'yyyy-MM-dd hh:mm:ss',  
       	    			language: 'zh-CN'
       	    }).on('changeDate', function(ev){
       	    	$(".bootstrap-datetimepicker-widget").hide();   
       	    	$('#closing_date').trigger('keyup');
       	    });
       	    
       	    $('#closing_date_div').on('changeDate', function(ev){
       	    	$(".bootstrap-datetimepicker-widget").hide();
       	    		$('#closing_date_div').each(function(){
       	    			$("#charge_time").val($('#closing_date').val());
             	});
       	    });
       	    $('#closing_date').on('keyup',function(){
	    		$("#charge_time").val($("#closing_date").val());
    	    });
       	 	$('#closing_date').on('blur',function(){
       	 		$("#charge_time").val($("#closing_date").val());
       	 	}); 
       	    
       	    $('#eeda-table [name=CLOSING_DATE_div]').on('changeDate', function(ev){
                 $(".bootstrap-datetimepicker-widget").hide();
                 	$('#eeda-table [name=CLOSING_DATE_div]').each(function(){
                 		$(this).parent().parent().find("[name=CHARGE_TIME]").val($(this).parent().find("[name=CLOSING_DATE]").val());
                 	});
             });
       	    $('#eeda-table').on('keyup','[name=CLOSING_DATE]',function(){
   	    		$(this).parent().parent().parent().find("[name=CHARGE_TIME]").val($(this).val());
       	    });
       	    $('#eeda-table').on('blur','[name=CLOSING_DATE]',function(){
	    		$(this).parent().parent().parent().find("[name=CHARGE_TIME]").val($(this).val());
    	    }); 
            eeda.bindTableField('eeda-table','CUSTOMER_ID','/serviceProvider/searchCompany','');
            eeda.bindTableField('eeda-table','CHARGE_ID','/finItem/search','');
            eeda.bindTableFieldCarInfo('eeda-table', 'WHOLE_COURSE_CAR_NO');
            eeda.bindTableFieldCarInfo('eeda-table', 'TIJIGUI_CAR_NO');
            eeda.bindTableFieldCarInfo('eeda-table', 'YIGUI_CAR_NO');
            eeda.bindTableFieldCarInfo('eeda-table', 'SHOUZHONGGUI_CAR_NO');
            eeda.bindTableFieldDockInfo('eeda-table','TAKE_WHARF');
            eeda.bindTableFieldDockInfo('eeda-table','BACK_WHARF');
            eeda.bindTableFieldDockInfo('eeda-table','LOADING_WHARF1');
            eeda.bindTableFieldDockInfo('eeda-table','LOADING_WHARF2');
            eeda.bindTableFieldDockInfo('eeda-table','CROSS_BORDER_TRAVEL');
            eeda.bindTableFieldCurrencyId('eeda-table','CURRENCY_ID','/serviceProvider/searchCurrency','');
            eeda.bindTableFieldCurrencyId('eeda-table','HEAD_CARRIER','/customer/searchParty','');

             // eeda.bindTableLocationField('eeda-table','route_to');
     };
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda-table',
//          paging: true,
          // serverSide: true, //不打开会出现排序不对
          autoWidth: false,
          scrollY: 530,
          colReorder: true,
          scrollCollapse: true,
          drawCallback:function(settings){
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
                { "data":"ID","width": "10px",
	                  "render": function ( data, type, full, meta ) {
	                    return '<button type="button" class="delete btn table_btn  btn-xs" style="width:30px"  >删除</button>';
	                  }
                },
                { "data": "CUSTOMER_ID", "width": "60px",
	                  "render": function ( data, type, full, meta ) {
	                     if(!data)
	                            data='';
	                        var field_html = template('table_dropdown_template',
	                            {
	                                id: 'CUSTOMER_ID',
	                                value: data,
	                                display_value: full.CUSTOMER_ID_INPUT,
	                                required:'required',
	                                style:'width:80px',
	                            }
	                        );
	                        return field_html;
	                    }
                },
	            { "data": "CABINET_DATE", "width": "80px",
	                  "render": function ( data, type, full, meta ) {
	                    if(!data)
	                    data='';
	                   var field_html = template('table_date_field_template',
	                           {
	                               id: 'CABINET_DATE',
	                               value: data,
	                               display_value: full.CABINET_DATE,
	                               style:'width:80px'
	                           }
	                       );
	                  return field_html;
	                }
	            },
	            { "data": "CLOSING_DATE", "width": "80px",
	                  "render": function ( data, type, full, meta ) {
	                    if(!data)
	                    data='';
	                   var field_html = template('table_date_field_template',
	                           {
	                               id: 'CLOSING_DATE',
	                               value: data,
	                               display_value: full.CLOSING_DATE,
	                               style:'width:80px'
	                           }
	                       );
	                  return field_html;
	                }
	            },
	            { "data": "CHARGE_TIME", "width": "60px",
	                  "render": function ( data, type, full, meta ) {
	                    if(!data)
	                    data='';
	                   var field_html = template('table_date_field_template',
	                           {
	                               id: 'CHARGE_TIME',
	                               value: data,
	                               display_value: full.CHARGE_TIME,
	                               style:'width:80px'
	                           }
	                       );
	                  return field_html;
	                }
	            },
                { "data": "TYPE","width": "50px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var str= '<select name="type" class="form-control search-control"  style="width:70px">'
	                        +'<option value="出口柜货" '+(data=='出口柜货' ? 'selected':'')+'>出口柜货</option>'
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
                { "data": "SO_NO","width": "80px",
		              "render": function ( data, type, full, meta ) {
	                      if(!data)
	                        data = '';
	                          return '<input type="text" name="so_no" style="width:100px" value="'+data+'" class="form-control"/>';
	                     }
                }, 
                { "data": "CONTAINER_NO","width": "80px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data = '';
	                          return '<input type="text" name="container_no" style="width:100px" value="'+data+'" class="form-control"/>';
	                     }
	            },
	            { "data": "CABINET_TYPE","width": "40px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var str= '<select  name="cabinet_type" style="width:60px" class="form-control search-control valid">'
	                    		 +'<option value="40HQ" '+(data=='40HQ' ? 'selected':'')+'>40HQ</option>'
	                             +'<option value="20GP" '+(data=='20GP' ? 'selected':'')+'>20GP</option>'
	                             +'<option value="40GP" '+(data=='40GP' ? 'selected':'')+'>40GP</option>'	                             
	                             +'<option value="45GP" '+(data=='45GP' ? 'selected':'')+'>45GP</option>'
	                             +'</select>'; 
	                    return str;
	                }
	            },
	            { "data": "LADING_NO","width": "60px",
	                  "render": function ( data, type, full, meta ) {
	                      if(!data)
	                        data = '';
	                          return '<input type="text" name="lading_no" style="width:80px" value="'+data+'" class="form-control"/>';
	                     }
	            }, 
	            { "data": "SEAL_NO","width": "60px",
	                  "render": function ( data, type, full, meta ) {
	                      if(!data)
	                        data = '';
	                          return '<input type="text" name="seal_no" style="width:80px" value="'+data+'" class="form-control"/>';
	                     }
	            }, 
	            { "data": "HEAD_CARRIER","width": "70px",
	          	  "render": function ( data, type, full, meta ) {
	                    if(!data)
	                           data='';
	                       var field_html = template('table_dropdown_template',
	                           {
	                               id: 'HEAD_CARRIER',
	                               value: data,
	                               display_value: full.HEAD_CARRIER_INPUT,
	                               style:'width:80px',
	                           }
	                       );
	                       return field_html;
	                   }
	              },
	              { "data": "TAKE_WHARF","width": "80px",
		                "render": function ( data, type, full, meta ) {
		                     if(!data)
		                            data='';
		                        var field_html = template('table_dock_no_field_template',
		                            {
		                                id: 'TAKE_WHARF',
		                                value: data,
		                                display_value: full.TAKE_WHARF_INPUT,
		                                style:'width:100px',
		                            }
		                        );
		                        return field_html;
		                    }
		           },
		           { "data": "BACK_WHARF","width": "80px",
		                "render": function ( data, type, full, meta ) {
		                      if(!data)
		                            data='';
		                        var field_html = template('table_dock_no_field_template',
		                            {
		                                id: 'BACK_WHARF',
		                                value: data,
		                                display_value: full.BACK_WHARF_INPUT,
		                                style:'width:100px',
		                            }
		                        );
		                        return field_html;
		                    }
		           },
		           { "data": "LOADING_WHARF1","width": "90px",
		                "render": function ( data, type, full, meta ) {
		                     if(!data)
		                            data='';
		                        var field_html = template('table_dock_no_field_template',
		                            {
		                                id: 'LOADING_WHARF1',
		                                value: data,
		                                display_value: full.LOADING_WHARF1_INPUT,
		                                style:'width:110px',
		                            }
		                        );
		                        return field_html;
		                    }
		           },
		           { "data": "LOADING_WHARF2","width": "90px",
		                "render": function ( data, type, full, meta ) {
		                     if(!data)
		                            data='';
		                        var field_html = template('table_dock_no_field_template',
		                            {
		                                id: 'LOADING_WHARF2',
		                                value: data,
		                                display_value: full.LOADING_WHARF2_INPUT,
		                                style:'width:110px',
		                            }
		                        );
		                        return field_html;
		                    }
		          },
		          { "data": "LOADING_PLATFORM","width": "60px",
		              "render": function ( data, type, full, meta ) {
	                      if(!data)
	                        data = '';
	                          return '<input type="text" name="loading_platform" style="width:80px" value="'+data+'" class="form-control"/>';
	                     }
                  },
		          { "data": "WHOLE_COURSE_CAR_NO","width": "60px",
		                "render": function ( data, type, full, meta ) {
		                   if(!data)
		                        data='';
		                    var field_html = template('table_car_no_field_template',
		                        {
		                            id: 'WHOLE_COURSE_CAR_NO',  //component_id 便于用 #id取组件
		                            value: data,
		                            display_value: full.WHOLE_COURSE_CAR_NO_INPUT,
		                            style:'width:80px'
		                        }
		                    );
		                     return field_html;
		                 }
		          },
	              { "data": "TIJIGUI_CAR_NO","width": "80px",
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                        data='';
	                    var field_html = template('table_car_no_field_template',
	                        {
	                            id: 'TIJIGUI_CAR_NO',  //component_id 便于用 #id取组件
	                            value: data,
	                            display_value: full.TIJIGUI_CAR_NO_INPUT,
	                            style:'width:80px'
	                        }
	                    );
	                     return field_html;
	                 }
	              }, 
	              { "data": "SHOUZHONGGUI_CAR_NO","width": "80px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    var field_html = template('table_car_no_field_template',
	                        {
	                            id: 'SHOUZHONGGUI_CAR_NO',  //component_id 便于用 #id取组件
	                            value: data,
	                            display_value: full.SHOUZHONGGUI_CAR_NO_INPUT,
	                            style:'width:80px'
	                        }
	                    );
	                     return field_html;
	                 }
	              },
	              { "data": "YIGUI_CAR_NO","width": "70px",
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                        data='';
	                    var field_html = template('table_car_no_field_template',
	                        {
	                            id: 'YIGUI_CAR_NO',  //component_id 便于用 #id取组件
	                            value: data,
	                            display_value: full.YIGUI_CAR_NO_INPUT,
	                            style:'width:80px'
	                        }
	                    );
	                     return field_html;
	                 }
	              }, 
	              { "data": "FREIGHT","width": "60px",
		              "render": function ( data, type, full, meta ) {
	                      if(!data)
	                        data = '';
	                          return '<input type="text" name="freight" placeholder="为空,从合同价带出" style="width:80px" value="'+data+'" class="form-control"/>';
	                     }
                  },
                  { "data": "CROSS_BORDER_TRAVEL","width": "60px",
		                "render": function ( data, type, full, meta ) {
		                     if(!data)
		                            data='';
		                        var field_html = template('table_dock_no_field_template',
		                            {
		                                id: 'CROSS_BORDER_TRAVEL',
		                                value: data,
		                                display_value: full.CROSS_BORDER_TRAVEL,
		                                style:'width:80px',
		                            }
		                        );
		                        return field_html;
		                    }
		          },
		          { "data": "CUSTOMER_SALESMAN","width": "70px",
		              "render": function ( data, type, full, meta ) {
	                      if(!data)
	                        data = '';
	                          return '<input type="text" name="customer_salesman" style="width:90px" value="'+data+'" class="form-control"/>';
	                     }
                  },
                  { "data": "CONTRACT_NO","width": "60px",
		              "render": function ( data, type, full, meta ) {
	                      if(!data)
	                        data = '';
	                          return '<input type="text" name="contract_no" style="width:80px" value="'+data+'" class="form-control"/>';
	                     }
                  },
                  { "data": "TOCA_NO","width": "60px",
		              "render": function ( data, type, full, meta ) {
	                      if(!data)
	                        data = '';
	                          return '<input type="text" name="toca_no" style="width:80px" value="'+data+'" class="form-control"/>';
	                     }
                  },
                  { "data": "HIGH_SPEED_FEE","width": "40px",
		              "render": function ( data, type, full, meta ) {
	                      if(!data)
	                        data = '';
	                          return '<input type="text" name="high_speed_fee" style="width:60px" value="'+data+'" class="form-control"/>';
	                     }
                  },
                  { "data": "CALL_FEE","width": "40px",
		              "render": function ( data, type, full, meta ) {
	                      if(!data)
	                        data = '';
	                          return '<input type="text" name="call_fee" style="width:60px" value="'+data+'" class="form-control"/>';
	                     }
                  },
                  { "data": "NIGHT_FEE","width": "40px",
		              "render": function ( data, type, full, meta ) {
	                      if(!data)
	                        data = '';
	                          return '<input type="text" name="night_fee" style="width:60px" value="'+data+'" class="form-control"/>';
	                     }
                  },
                  { "data": "WEIGHING_FEE","width": "40px",
		              "render": function ( data, type, full, meta ) {
	                      if(!data)
	                        data = '';
	                          return '<input type="text" name="weighing_fee" style="width:60px" value="'+data+'" class="form-control"/>';
	                     }
                  },
                  { "data": "JI_JINJI_OUT_FEE","width": "70px",
		              "render": function ( data, type, full, meta ) {
	                      if(!data)
	                        data = '';
	                          return '<input type="text" name="ji_jinji_out_fee" style="width:90px" value="'+data+'" class="form-control"/>';
	                     }
                  },
                  { "data": "ADVANCE_FEE","width": "40px",
		              "render": function ( data, type, full, meta ) {
	                      if(!data)
	                        data = '';
	                          return '<input type="text" name="advance_fee" style="width:60px" value="'+data+'" class="form-control"/>';
	                     }
                  },
	              { "data": "REMARK","width": "100px",
	                "render": function ( data, type, full, meta ) {
	                  if(!data)
	                        data = '';
	                          return '<input type="text" name="remark" style="width:100px" value="'+data+'" class="form-control"/>';
	                     }
	              }
          ]
      });

     
       $('#add_land').on('click', function(){
    	  var customer_id=$('#CUSTOMER_ID').val();
    	  var customer_salesman=$('#customer_salesman').val();
    	  var contract_no=$('#contract_no').val();
          var cabinet_date=$('#cabinet_date').val();
          var closing_date=$('#closing_date').val();
          var charge_time=$('#charge_time').val();
          var type=$('#type').val();
          var so_no=$('#so_no').val();
          var container_no=$('#container_no').val();
          var cabinet_type=$('#cabinet_type').val();
          var lading_no=$('#lading_no').val();
          var seal_no=$('#seal_no').val();
          var HEAD_CARRIER=$('#HEAD_CARRIER').val();
          var take_wharf=$('#TAKE_WHARF').val();
          var back_wharf=$('#BACK_WHARF').val();
          var loading_wharf1=$('#LOADING_WHARF1').val();
          var loading_wharf2=$('#LOADING_WHARF2').val();
          var loading_platform=$('#loading_platform').val();
          var whole_course_car_no=$('#WHOLE_COURSE_CAR_NO').val();
          var tijigui_car_no=$('#TIJIGUI_CAR_NO').val();
          var shouzhonggui_car_no=$('#SHOUZHONGGUI_CAR_NO').val();
          var yigui_car_no=$('#YIGUI_CAR_NO').val();
          var remark=$('#remark').val();
          var customer_id_input=$('#CUSTOMER_ID_input').val();
          var HEAD_CARRIER_input=$('#HEAD_CARRIER_input').val();
          var whole_course_car_no_input=$('#WHOLE_COURSE_CAR_NO_input').val();
          var tijigui_car_no_input=$('#TIJIGUI_CAR_NO_input').val();
          var yigui_car_no_input=$('#YIGUI_CAR_NO_input').val();
          var shouzhonggui_car_no_input=$('#SHOUZHONGGUI_CAR_NO_input').val();
          var take_wharf_input=$('#TAKE_WHARF_input').val();
          var back_wharf_input=$('#BACK_WHARF_input').val();
          var loading_wharf1_input=$('#LOADING_WHARF1_input').val();
          var loading_wharf2_input=$('#LOADING_WHARF2_input').val();
          // var =$('#').val();CUSTOMER_ID_input
          var item={"CUSTOMER_ID":customer_id,"CABINET_DATE":cabinet_date,"CLOSING_DATE":closing_date,"CHARGE_TIME":charge_time,"TYPE":type,"SO_NO":so_no,
                    "CONTAINER_NO":container_no,"CABINET_TYPE":cabinet_type,"LADING_NO":lading_no,"SEAL_NO":seal_no,"HEAD_CARRIER":HEAD_CARRIER,
                    "TAKE_WHARF":take_wharf,"BACK_WHARF":back_wharf,"LOADING_WHARF1":loading_wharf1,"LOADING_WHARF2":loading_wharf2,"LOADING_PLATFORM":loading_platform,
                    "WHOLE_COURSE_CAR_NO":whole_course_car_no,"TIJIGUI_CAR_NO":tijigui_car_no,"SHOUZHONGGUI_CAR_NO":shouzhonggui_car_no,"YIGUI_CAR_NO":yigui_car_no,
                    "FREIGHT":"","CUSTOMER_SALESMAN":customer_salesman,"CONTRACT_NO":contract_no,"TOCA_NO":"","HIGH_SPEED_FEE":"","HIGH_SPEED":"","NIGHT_FEE":"","WEIGHING_FEE":"","JI_JINJI_OUT_FEE":"","ADVANCE_FEE":"","REMARK":remark,
                    "CUSTOMER_ID_INPUT":customer_id_input,"HEAD_CARRIER_INPUT":HEAD_CARRIER_input,"WHOLE_COURSE_CAR_NO_INPUT":whole_course_car_no_input,"TIJIGUI_CAR_NO_INPUT":tijigui_car_no_input,
                    "SHOUZHONGGUI_CAR_NO_INPUT":shouzhonggui_car_no_input,"YIGUI_CAR_NO_INPUT":yigui_car_no_input,"TAKE_WHARF_INPUT":take_wharf_input,"BACK_WHARF_INPUT":back_wharf_input,
                    "LOADING_WHARF1_INPUT":loading_wharf1_input,"LOADING_WHARF2_INPUT":loading_wharf2_input,};
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
        	if(index>0){
        		if($(item).find('[type=checkbox]').prop('checked')){
                    if(!$(item).find('[name=CUSTOMER_ID_input]').val()){
                     $.scojs_message('第'+index+'行未选择客户', $.scojs_message.TYPE_ERROR);
                         error++;
                        }
                  }
        	}
          })
         if(error>0){
          return;
         }
          order.itemList=itemOrder.buildItemList();
          $.post("/transOrderShortCut/create",{params:JSON.stringify(order)},function(data){
              if(data.INDEXS){
            	  $('#eeda-table [type="checkbox"]:checked').parent().parent().remove();        
                  $.scojs_message('创建成功', $.scojs_message.TYPE_OK);
              }               
             }).fail(function() {
             $.scojs_message('创建失败', $.scojs_message.TYPE_ERROR);
         });
      });
        
     //将上框内容应用到表中
     $('#add_on').on('click',function(){
      //当客户为空的列，添加
        var create_stamp=$('#CREATE_STAMP').val();
           // dataTable.row.add(item).draw(true);
          var cargo_table_rows = $("#eeda-table tr");
          for(var index=1; index<cargo_table_rows.length; index++){
              var row = cargo_table_rows[index];
              var empty = $(row).find('.dataTables_empty').text();
              if(empty)
                continue;

                //客户为空时应用
              var customer=$(row).find('[name=CUSTOMER_ID_input]').val();
              for(var i = 1; i < row.childNodes.length; i++){
                var name = $(row.childNodes[i]).find('input,select').attr('name');
                if((!customer||customer==undefined)&&name&&name!=undefined){
                      var value = $('#'+name).val();
                    if(value!=undefined&&value){
                       $(row).find('[name='+name+']').val(value);
                       if(name=='CUSTOMER_ID'||name=='TIJIGUI_CAR_NO'||name=='YIGUI_CAR_NO'||
                        name=='SHOUZHONGGUI_CAR_NO'||name=='TAKE_WHARF'||name=='BACK_WHARF' ||name=='LOADING_WHARF1' ||name=='LOADING_WHARF2'){
                            $(row).find('[name='+name+'_input]').val($('#'+(name+'_input')).val());
                       }
                       if(name=='type'||name=='cabinet_type'){
                          $(row).find('option[value='+value+']').attr('selected',true);
                       }
                    }
                  }
              }
          }
       });

      $('#eeda-table').on('blur','[name=CUSTOMER_ID_input],[name=TAKE_WHARF_input],[name=BACK_WHARF_input],[name=LOADING_WHARF1_input],[name=LOADING_WHARF2_input],[name=CHARGE_ID_input]',function(){
        var row=$(this).parent().parent().parent();
        var order={};
        order.CUSTOMER_ID=$(row.find('[name=CUSTOMER_ID]')).val();
        order.TAKE_WHARF=$(row.find('[name=TAKE_WHARF]')).val();
        order.BACK_WHARF=$(row.find('[name=BACK_WHARF]')).val();
        order.LOADING_WHARF1=$(row.find('[name=LOADING_WHARF1]')).val();
        order.LOADING_WHARF2=$(row.find('[name=LOADING_WHARF2]')).val();
        order.CHARGE_ID=$(row.find('[name=CHARGE_ID]')).val();
        order.CABINET_TYPE=$(row.find('[name=cabinet_type]')).val();
        if(order.CUSTOMER_ID&&order.CABINET_TYPE){
          $.post("/transOrderShortCut/checkCustomerQuotation",{params:JSON.stringify(order)},function(data){
                if(data.length==1){
                  $(row.find('[name=total_amount]')).val(data[0].TAX_FREE_FREIGHT);
                   $(row.find('[name=currency_total_amount]')).val(data[0].TAX_FREE_FREIGHT);
                   $(row.find('[name=CURRENCY_ID]')).val(data[0].CURRENCY_ID);
                    $(row.find('[name=CURRENCY_ID_input]')).val(data[0].CURRENCY_NAME);
                }               
               }).fail(function() {
               
           });
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
              item.index = index.toString();
              for(var i = 1; i < row.childNodes.length; i++){
                var name = $(row.childNodes[i]).find('input,select').attr('name');
                var value = $(row.childNodes[i]).find('input,select').val();
                if(name){
                  item[name] = value;
                }
                if(name=='WHOLE_COURSE_CAR_NO'){
                    item[name]=$(row.childNodes[i]).find('input,select').val()
                  }
                if(name=='TIJIGUI_CAR_NO'){
                  item[name]=$(row.childNodes[i]).find('input,select').val()
                }
                if(name=='YIGUI_CAR_NO'){
                  item[name]=$(row.childNodes[i]).find('input,select').val()
                }
                if(name=='SHOUZHONGGUI_CAR_NO'){
                  item[name]=$(row.childNodes[i]).find('input,select').val()
                }
              }
              item.action = id.length > 0?'UPDATE':'CREATE';
              if($(row).find('[type=checkbox]').prop('checked')){
                    cargo_items_array.push(item);
//                    dataTable.row(row).remove().draw();
                 }
          }
          return cargo_items_array;
       }

  });
});