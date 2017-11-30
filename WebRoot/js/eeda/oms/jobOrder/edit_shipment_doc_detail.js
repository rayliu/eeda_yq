define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
    $(document).ready(function() {
        
        
        //删除一行
        $("#one_doc_table,#two_doc_table,#three_doc_table,#four_doc_table,#zero_doc_table").on('click', '.delete', function(e){
            e.preventDefault();
            var tr = $(this).parent().parent();
            var id = tr.attr('id');
            this.disabled = true;
            var table_id = $($(this).parent().parent().parent().parent()).prop('id');
             $.post('/jobOrder/deleteDoc', {docId:id}, function(data){
            	 if(data.result==true){
            		 if(table_id=='two_doc_table'){
         			 	twoTable.row(tr).remove().draw();
	     	         }else if(table_id=='four_doc_table'){
	     	        	fourTable.row(tr).remove().draw();
	     	         }else{
	     	        	zeroTable.row(tr).remove().draw();
	     	         }
    	        	 $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
            	 }else if(data.result==false){
            		 $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
            	 }else{
            		 if(table_id=='two_doc_table'){
          			 	twoTable.row(tr).remove().draw();
 	     	         }else if(table_id=='four_doc_table'){
 	     	        	fourTable.row(tr).remove().draw();
 	     	         }else{
	     	        	zeroTable.row(tr).remove().draw();
	     	         }
            		 $.scojs_message(data.result, $.scojs_message.TYPE_ERROR);
            	 }
             },'json').fail(function() {
                 	 $.scojs_message('删除失败!', $.scojs_message.TYPE_ERROR);
             });
        }); 

        //文件发送-----工作单
        $("#doc_table").on('click', '.confirmSend', function(e){
            e.preventDefault();
            var tr = $(this).parent().parent();
            var id = tr.attr('id');
            this.disabled = true;
             $.post('/bookOrder/confirmSend', {docId:id}, function(data){
            	 $.scojs_message('完成!', $.scojs_message.TYPE_ERROR);
             },'json').fail(function() {
                 $.scojs_message('后台报错!', $.scojs_message.TYPE_ERROR);
             });
        }); 
        
        //接收文件-----工作单
        $("#one_doc_table").on('click', '.downloadDoc', function(e){
            e.preventDefault();
            var self = this;
            var tr = $(this).parent().parent();
            var id = tr.attr('id');
            this.disabled = true; 
             $.post('/jobOrder/downloadDoc', {docId:id}, function(data){
            	 if(data){
            		 $.scojs_message('接收成功!', $.scojs_message.TYPE_OK);
            		 self.disabled = false;
            		 itemOrder.refleshOneDocTable(data.ORDER_ID);
            	 }            	 
             },'json').fail(function() {
                 $.scojs_message('后台报错!', $.scojs_message.TYPE_ERROR);
             }); 
        }); 
        
        
        //文件确认-----工作单
        $("#one_doc_table").on('click', '.confirmDoc', function(e){
            e.preventDefault();
            var tr = $(this).parent().parent();
            var id = tr.attr('id');
            this.disabled = true;
             $.post('/jobOrder/confirmDoc', {docId:id}, function(data){
            	 if(data){
            		 $.scojs_message('确认成功!', $.scojs_message.TYPE_OK);
            		 itemOrder.refleshOneDocTable(data.ORDER_ID);
            	 }
            	 
             },'json').fail(function() {
                 $.scojs_message('后台报错!', $.scojs_message.TYPE_ERROR);
             });
        }); 
        
        
        
        //------------事件处理,文档table
        var oneTable = eeda.dt({
            id: 'one_doc_table',
            autoWidth: false,
            columns:[
                { "width": "50px",
                    "render": function ( data, type, full, meta ) {
                    	return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="display:none">删除</button>'
                    	+'<button type="button" class="downloadDoc btn table_btn delete_btn btn-xs"><a  href="/upload/doc/'+full.DOC_NAME+'"  target="_blank">接收文件</a></button>';
                    }
                },
                { "width": "50px",
                    "render": function ( data, type, full, meta ) {
                    	return '<button type="button" class="confirmDoc btn table_btn delete_btn btn-xs">确认</button>';
                    }
                },
                { "data": "DOC_NAME","width": "280px",
                    "render": function ( data, type, full, meta ) {
                      if(!data)
                          data='';
                      var str='';
                      if(full.SEND_STATUS=='已发送'){
                       str='<span class="badge" style="background-color:white;color:red;margin-left:5px;">新</span>';                        
                      }

                    if(full.REMARK=='自动生成'){
                        return '<a class="doc_name" href="/download/'+data+'" style="width:300px" target="_blank">'+data+str+'</a>';
                    }else{
                        return '<a class="doc_name" href="/upload/doc/'+data+'" style="width:300px" target="_blank">'+data+str+'</a>';
                    }
                  }
                },
                { "data": "C_NAME","width": "180px",
                    "render": function ( data, type, full, meta ) {
                    	if(!data)
                            data='';
                    	return data;
                    }
                },
                { "data": "UPLOAD_TIME", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                },
                { "data": "REMARK","width": "280px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" name="remark" value="'+data+'" class="form-control" style="width:300px"/>';
                    }
                },
                { "data": "SENDER", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                },
                { "data": "SEND_TIME", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                },
                { "data": "SEND_STATUS", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                },
                {"data": "NEW_COUNT","visible":false},
                {"data": null,"visible":'false',
                    "render": function ( data, type, full, meta ) {
                        var str='';
                      if(full.NEW_COUNT>0){
                        str='<span class="badge" style="background-color:white;color:red;margin-left:5px;">新</span>';
                        $($('.simple_rectangle_title_div .font_style')[1]).html('第二步，接受P/C资料上传'+str);
                      }else{
                        $($('.simple_rectangle_title_div .font_style')[1]).html('第二步，接受P/C资料上传'+str);
                      
                      }
                      return "";
                    }
               }
            ]
        });
        
      //刷新明细表
        itemOrder.refleshOneDocTable = function(order_id){
        	var url = "/jobOrder/docTableList?order_id="+order_id+"&type=one";
        	oneTable.ajax.url(url).load();
        }
        
        
        
        //------------------tow
      //------------事件处理,文档table
        var twoTable = eeda.dt({
            id: 'two_doc_table',
            autoWidth: false,
            columns:[
                { "width": "50px",
                    "render": function ( data, type, full, meta ) {
                    	return '<button type="button" class="delete btn table_btn delete_btn btn-xs">删除</button>';
                    }
                },
                { "width": "50px",
                    "render": function ( data, type, full, meta ) {
                    	return '<button type="button" class="confirmSend btn table_btn delete_btn btn-xs">发送</button>';
                    }
                },
                { "data": "DOC_NAME","width": "280px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        if(full.REMARK=='自动生成'){
                        	return '<a class="doc_name" href="/download/'+data+'" style="width:300px" target="_blank">'+data+'</a>';
                        }else{
                        	return '<a class="doc_name" href="/upload/doc/'+data+'" style="width:300px" target="_blank">'+data+'</a>';
                        }
                        
                    }
                },
                { "data": "C_NAME","width": "180px",
                    "render": function ( data, type, full, meta ) {
                    	if(!data)
                            data='';
                    	return data;
                    }
                },
                { "data": "UPLOAD_TIME", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                },
                { "data": "REMARK","width": "280px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" name="remark" value="'+data+'" class="form-control" style="width:300px"/>';
                    }
                },
                { "data": "SENDER", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                },
                { "data": "SEND_TIME", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                },
                { "data": "SEND_STATUS", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                }
            ]
        });
        
        //文件发送-----bookong
        $("#two_doc_table").on('click', '.confirmSend', function(e){
            e.preventDefault();
            var tr = $(this).parent().parent();
            var id = tr.attr('id');
            var plan_order_id = $('#plan_order_id').val();
            var booking_order_id = $('#booking_order_id').val();
            var plan_order_item_id = $('#plan_order_item_id').val();
            this.disabled = true;
             $.post('/jobOrder/confirmSend', {docId:id,plan_order_id:plan_order_id,plan_order_item_id:plan_order_item_id,
            	 								booking_order_id:booking_order_id}, function(data){
            	 if(data=="err"){
               		$.scojs_message('发送失败，不存在关联的booking!', $.scojs_message.TYPE_ERROR);
               	 }else if(data){
            		 $.scojs_message('发送成功!', $.scojs_message.TYPE_OK);
            		 itemOrder.refleshTwoDocTable(data.ORDER_ID);
            	 }
             },'json').fail(function() {
                 $.scojs_message('后台报错!', $.scojs_message.TYPE_ERROR);
             });
        }); 
        
        //刷新明细表
        itemOrder.refleshTwoDocTable = function(order_id){
        	var url = "/jobOrder/docTableList?order_id="+order_id+"&type=two";
        	twoTable.ajax.url(url).load();
        }
        
        
        
        //---------------three
        //接收文件-----工作单
        $("#three_doc_table").on('click', '.downloadDoc', function(e){
            e.preventDefault();
            var self = this;
            var tr = $(this).parent().parent();
            var id = tr.attr('id');
            this.disabled = true;
             $.post('/jobOrder/downloadDoc', {docId:id}, function(data){
            	 if(data){
            		 $.scojs_message('接收成功!', $.scojs_message.TYPE_OK);
            		 self.disabled = false;
            		 itemOrder.refleshThreeDocTable(data.ORDER_ID);
            	 }
            	 
             },'json').fail(function() {
                 $.scojs_message('后台报错!', $.scojs_message.TYPE_ERROR);
             }); 
        }); 
        
        
        //文件确认-----工作单
        $("#three_doc_table").on('click', '.confirmDoc', function(e){
            e.preventDefault();
            var tr = $(this).parent().parent();
            var id = tr.attr('id');
            this.disabled = true;
             $.post('/jobOrder/confirmDoc', {docId:id}, function(data){
            	 if(data){
            		 $.scojs_message('确认成功!', $.scojs_message.TYPE_OK);
            		 itemOrder.refleshThreeDocTable(data.ORDER_ID);
            	 }
            	 
             },'json').fail(function() {
                 $.scojs_message('后台报错!', $.scojs_message.TYPE_ERROR);
             });
        }); 
        
        
        //刷新明细表
        itemOrder.refleshThreeDocTable = function(order_id){
        	var url = "/jobOrder/docTableList?order_id="+order_id+"&type=three";
        	threeTable.ajax.url(url).load();
        }
        
        
        
        //------------事件处理,文档table
        var threeTable = eeda.dt({
            id: 'three_doc_table',
            autoWidth: false,
            columns:[
                { "width": "50px",
                    "render": function ( data, type, full, meta ) {
                    	return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="display:none">删除</button>'
                    	+'<button type="button" class="downloadDoc btn table_btn delete_btn btn-xs"><a  href="/upload/doc/'+full.DOC_NAME+'"  target="_blank">接收文件</a></button>';
                    }
                },
                { "width": "50px",
                    "render": function ( data, type, full, meta ) {
                    	return '<button type="button" class="confirmDoc btn table_btn delete_btn btn-xs">确认</button>';
                    }
                },
                { "data": "DOC_NAME","width": "280px",
                    "render": function ( data, type, full, meta ) {
                      if(!data)
                          data='';
                      var str='';
                      if(full.SEND_STATUS=='已发送'){
                       str='<span class="badge" style="background-color:white;color:red;margin-left:5px;">新</span>';                        
                      }

                    if(full.REMARK=='自动生成'){
                        return '<a class="doc_name" href="/download/'+data+'" style="width:300px" target="_blank">'+data+str+'</a>';
                    }else{
                        return '<a class="doc_name" href="/upload/doc/'+data+'" style="width:300px" target="_blank">'+data+str+'</a>';
                    }
                  }
                },
                { "data": "C_NAME","width": "180px",
                    "render": function ( data, type, full, meta ) {
                    	if(!data)
                            data='';
                    	return data;
                    }
                },
                { "data": "UPLOAD_TIME", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                },
                { "data": "REMARK","width": "280px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" name="remark" value="'+data+'" class="form-control" style="width:300px"/>';
                    }
                },
                { "data": "SENDER", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                },
                { "data": "SEND_TIME", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                },
                { "data": "SEND_STATUS", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                },
              {"data": "NEW_COUNT","visible":false},
               {"data": null,"visible":'false',
                    "render": function ( data, type, full, meta ) {
                        var str='';
                      if(full.NEW_COUNT>0){
                        str='<span class="badge" style="background-color:white;color:red;margin-left:5px;">新</span>';
                        $($('.simple_rectangle_title_div .font_style')[3]).html('第四步，接收保函单'+str);
                      }else{
                        $($('.simple_rectangle_title_div .font_style')[3]).html('第四步，接收保函单'+str);
                      
                      }
                      return "";
                    }
              }
            ]
        });
        
        
        //-----------------------------
    
        
        //------------------tow
      //------------事件处理,文档table
        var fourTable = eeda.dt({
            id: 'four_doc_table',
            autoWidth: false,
            columns:[
                { "width": "50px",
                    "render": function ( data, type, full, meta ) {
                    	return '<button type="button" class="delete btn table_btn delete_btn btn-xs">删除</button>';
                    }
                },
                { "width": "50px",
                    "render": function ( data, type, full, meta ) {
                    	return '<button type="button" class="confirmSend btn table_btn delete_btn btn-xs">发送</button>';
                    }
                },
                { "data": "DOC_NAME","width": "280px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        if(full.REMARK=='自动生成'){
                        	return '<a class="doc_name" href="/download/'+data+'" style="width:300px" target="_blank">'+data+'</a>';
                        }else{
                        	return '<a class="doc_name" href="/upload/doc/'+data+'" style="width:300px" target="_blank">'+data+'</a>';
                        }
                    }
                },
                { "data": "C_NAME","width": "180px",
                    "render": function ( data, type, full, meta ) {
                    	if(!data)
                            data='';
                    	return data;
                    }
                },
                { "data": "UPLOAD_TIME", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                },
                { "data": "REMARK","width": "280px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" name="remark" value="'+data+'" class="form-control" style="width:300px"/>';
                    }
                },
                { "data": "SENDER", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                },
                { "data": "SEND_TIME", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                },
                { "data": "SEND_STATUS", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                }
            ]
        });
        
        //文件发送-----bookong
        $("#four_doc_table").on('click', '.confirmSend', function(e){
            e.preventDefault();
            var tr = $(this).parent().parent();
            var id = tr.attr('id');
            var plan_order_id = $('#plan_order_id').val();
            var plan_order_item_id = $('#plan_order_item_id').val();
            var booking_order_id = $('#booking_order_id').val();
            this.disabled = true;
             $.post('/jobOrder/confirmSend', {docId:id,plan_order_id:plan_order_id,plan_order_item_id:plan_order_item_id
            	 							,booking_order_id:booking_order_id}, function(data){
            	 if(data=="err"){
               		$.scojs_message('发送失败，不存在关联的booking!', $.scojs_message.TYPE_ERROR);
               	 }else if(data){
            		 $.scojs_message('发送成功!', $.scojs_message.TYPE_OK);
            		 itemOrder.refleshFourDocTable(data.ORDER_ID);
            	 }
             },'json').fail(function() {
                 $.scojs_message('后台报错!', $.scojs_message.TYPE_ERROR);
             });
        }); 
        
        //刷新明细表
        itemOrder.refleshFourDocTable = function(order_id){
        	var url = "/jobOrder/docTableList?order_id="+order_id+"&type=four";
        	fourTable.ajax.url(url).load();
        }
        
        
        //------------------zero
        //------------事件处理,文档table
          var zeroTable = eeda.dt({
              id: 'zero_doc_table',
              autoWidth: false,
              columns:[
                  { "width": "50px",
                      "render": function ( data, type, full, meta ) {
                      	return '<button type="button" class="delete btn table_btn delete_btn btn-xs">删除</button>';
                      }
                  },
                  { "width": "50px",
                      "render": function ( data, type, full, meta ) {
                      	return '<button type="button" class="confirmSend btn table_btn delete_btn btn-xs">发送</button>';
                      }
                  },
                  { "data": "DOC_NAME","width": "280px",
                      "render": function ( data, type, full, meta ) {
                          if(!data)
                              data='';
                          if(full.REMARK=='自动生成'){
                          	return '<a class="doc_name" href="/download/'+data+'" style="width:300px" target="_blank">'+data+'</a>';
                          }else{
                          	return '<a class="doc_name" href="/upload/doc/'+data+'" style="width:300px" target="_blank">'+data+'</a>';
                          }
                      }
                  },
                  { "data": "C_NAME","width": "180px",
                      "render": function ( data, type, full, meta ) {
                      	if(!data)
                              data='';
                      	return data;
                      }
                  },
                  { "data": "UPLOAD_TIME", "width": "180px",
                      "render": function ( data, type, full, meta ) {
                          if(!data)
                              data='';
                          return data;
                      }
                  },
                  { "data": "REMARK","width": "280px",
                      "render": function ( data, type, full, meta ) {
                          if(!data)
                              data='';
                          return '<input type="text" name="remark" value="'+data+'" class="form-control" style="width:300px"/>';
                      }
                  },
                  { "data": "SENDER", "width": "180px",
                      "render": function ( data, type, full, meta ) {
                          if(!data)
                              data='';
                          return data;
                      }
                  },
                  { "data": "SEND_TIME", "width": "180px",
                      "render": function ( data, type, full, meta ) {
                          if(!data)
                              data='';
                          return data;
                      }
                  },
                  { "data": "SEND_STATUS", "width": "180px",
                      "render": function ( data, type, full, meta ) {
                          if(!data)
                              data='';
                          return data;
                      }
                  }
              ]
          });
          
          //文件发送-----bookong
          $("#zero_doc_table").on('click', '.confirmSend', function(e){
              e.preventDefault();
              var tr = $(this).parent().parent();
              var id = tr.attr('id');
              var plan_order_id = $('#plan_order_id').val();
              var plan_order_item_id = $('#plan_order_item_id').val();
              var booking_order_id = $('#booking_order_id').val();
              this.disabled = true;
               $.post('/jobOrder/confirmSend', {docId:id,plan_order_id:plan_order_id,plan_order_item_id:plan_order_item_id
            	   								,booking_order_id:booking_order_id	}, function(data){
              	 if(data=="err"){
              		$.scojs_message('发送失败，不存在关联的booking!', $.scojs_message.TYPE_ERROR);
              	 }else if(data){
              		 $.scojs_message('发送成功!', $.scojs_message.TYPE_OK);
              		 itemOrder.refleshTwoDocTable(data.ORDER_ID);
              	 }
               }).fail(function() {
                   $.scojs_message('后台报错!', $.scojs_message.TYPE_ERROR);
               });
          }); 
          
          //刷新明细表
          itemOrder.refleshZeroDocTable = function(order_id){
	          	var url = "/jobOrder/docTableList?order_id="+order_id+"&type=zero";
	          	zeroTable.ajax.url(url).load();
          }
          

        if($('.badge').length>0){
                $('a[href="#collapseDocInfo"]').append('<span class="badge title" style="background-color:white;color:red;margin-left:5px;">新</span>');
            }
        $('a[href="#collapseDocInfo"]').on('click',function(){
            $('.title').remove();
        })
    });
});