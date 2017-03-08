define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 

  $(document).ready(function() {

    var deletedTableIds=[];
  		

      itemOrder.buildItemList=function(){
        var cargo_table_rows = $("#mark_table tr");
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
              if(!id.length>0){
                var d = new Date();
                var str = d.getFullYear()+"-"+(d.getMonth()+1)+"-"+d.getDate()+' '+d.getHours()+':'+d.getMinutes()+':'+d.getSeconds();
                item.mark_date = str;
                item.creator=$('#user_id').val();
              }
              
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

  		var dataTable = eeda.dt({
            id: 'mark_table',
            // ajax:{
            //     url: "/serviceProvider/markCustormerList?sp_id="+$('#markCustomer').val(),
            //     type: 'POST'
            // }, 
            columns:[
            { "width": "10px", "orderable": false,
            "render": function ( data, type, full, meta ) {
                 var str = '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:50px" value='+full.ID+'>删除</button>';
                return str;
            	}
            },
              { "data": "ITEM", "width": "100px",
                 "render": function ( data, type, full, meta ) {
                  if(full.AUDIT_FLAG == 'Y'){
                    var str = '<select name="type" name="item" class="form-control search-control notsave" style="width:150px" disabled>' 
                      +'<option value="价格" '+(data=='价格' ? 'selected':'')+'> 价格 </option>'
                          +'<option value="附加费用" '+(data=='附加费用' ? 'selected':'')+'> 附加费用 </option>'
                          +'<option value="付款方式" '+(data=='付款方式' ? 'selected':'')+'> 付款方式 </option>'
                          +'<option value="紧急情况对应效率" '+(data=='紧急情况对应效率' ? 'selected':'')+'> 紧急情况对应效率 </option>'
                          +'<option value="员工服务态度" '+(data=='员工服务态度' ? 'selected':'')+'> 员工服务态度 </option>'
                          +'<option value="货物交付及时" '+(data=='货物交付及时' ? 'selected':'')+'> 货物交付及时 </option>'
                          +'<option value="货物损坏率" '+(data=='货物损坏率' ? 'selected':'')+'> 货物损坏率 </option>'
                          +'<option value="通关专业程度" '+(data=='通关专业程度' ? 'selected':'')+'> 通关专业程度 </option>'
                          +'<option value="通关时效性" '+(data=='通关时效性' ? 'selected':'')+'> 通关时效性 </option>'
                          +'<option value="其它" '+(data=='其它' ? 'selected':'')+'> 其它 </option>'
                          +'</select>';
                    return str;
                  }else{
                      var trans_type=$('#trans_type').val();
                      var  str = '<select name="type" name="item" class="form-control search-control notsave" style="width:150px">' 
                      +'<option value="价格" '+(data=='价格' ? 'selected':'')+'> 价格 </option>'
                          +'<option value="附加费用" '+(data=='附加费用' ? 'selected':'')+'> 附加费用 </option>'
                          +'<option value="付款方式" '+(data=='付款方式' ? 'selected':'')+'> 付款方式 </option>'
                          +'<option value="紧急情况对应效率" '+(data=='紧急情况对应效率' ? 'selected':'')+'> 紧急情况对应效率 </option>'
                          +'<option value="员工服务态度" '+(data=='员工服务态度' ? 'selected':'')+'> 员工服务态度 </option>'
                          +'<option value="货物交付及时" '+(data=='货物交付及时' ? 'selected':'')+'> 货物交付及时 </option>'
                          +'<option value="货物损坏率" '+(data=='货物损坏率' ? 'selected':'')+'> 货物损坏率 </option>'
                          +'<option value="通关专业程度" '+(data=='通关专业程度' ? 'selected':'')+'> 通关专业程度 </option>'
                          +'<option value="通关时效性" '+(data=='通关时效性' ? 'selected':'')+'> 通关时效性 </option>'
                          +'<option value="其它" '+(data=='其它' ? 'selected':'')+'> 其它 </option>'
                          +'</select>';
                    return str;
                  }
                }
              },
              { "data": "SCORE", "width": "100px",
                "render": function ( data, type, full, meta ) {
                  if(!data)
                    data='';
                  return '<input type="text" name="score"   value="'+data+'" class="form-control" />';
                }
              },
              { "data": "REMARK", "width": "100px",
                "render": function ( data, type, full, meta ) {
                  if(!data)
                    data='';
                  return '<input type="text" name="remark"   value="'+data+'" class="form-control" />';
                }
              },
              { "data": "CREATOR", "width": "100px",
                "render": function ( data, type, full, meta ) {
                  if(!data)
                    data='';
                  return '<input type="text" disabled name="creator"   value="'+data+'" class="form-control" />';
                }
              },
              { "data": "MARK_DATE", "width": "100px",
                "render": function ( data, type, full, meta ) {
                  if(!data)
                    data='';
                  return '<input type="text" disabled name="mark_date"   value="'+data+'" class="form-control" />';
                }
              }
            ]
        });

  	$('#markCustomer_input').on('blur',function(){
  		if($('#markCustomer').val()!=null&&$('#markCustomer').val()!=''){
  			freshItemTable();
  		}
  	})

	 $('#marking').click(function(){
            $('#mark_table_msg_btn').click();             
        })

  	$('#add_mark').on('click',function(){
  			var item={};
        dataTable.row.add(item).draw(true);
  	});		


  	 $("#mark_table").on('click', '.delete', function(e){
  		  e.preventDefault();
  		  var tr= $(this).parent().parent();
        deletedTableIds.push(tr.attr('id'));

        dataTable.row(tr).remove().draw();
  		// $.post('/serviceProvider/markCustormerDelete',{id:id},function(data){
  				// freshItemTable();
  		})
  	 //保存
  	 $('#save_mark').click(function(){
      var order={};
  	 	 order.item_list=itemOrder.buildItemList();
  	 	 order.sp_id=$('#markCustomer').val()
  	 	 //异步向后台提交数据
        $.post('/serviceProvider/markCustormerSave', {params:JSON.stringify(order)}, function(data){
        	freshItemTable();
        	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
        },'JSON').fail(function(){
        	 $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
        	});
  	 });

  	 var freshItemTable=function(){
  	 	 var sp_id=$('#markCustomer').val()
  	 	 var url = "/serviceProvider/markCustormerList?sp_id="+sp_id;
          dataTable.ajax.url(url).load(function(){
          	calculate();
          });
  	 }
  	 //计算总分
  	 var calculate=function(){
  	 	var totalscore=80;
  	 	dataTable.data().each(function(item,index){
  	 		totalscore+=parseInt(item.SCORE);
  	 	})
  	 	$('#total_mark').val(totalscore);
  	 	$('#total_mark').html(totalscore);
  	 }
  })
})