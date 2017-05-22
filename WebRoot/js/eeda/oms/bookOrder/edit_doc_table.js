define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) { 
$(document).ready(function() {
	
	//未保存不能上传文件
	if($('#order_id').val()==''){
		$("#fileuploadSpan").hide();
		$("#sendEmail").hide();
	}

    //删除一行
    $("#one_doc_table,#two_doc_table,#three_doc_table,#four_doc_table").on('click', '.delete', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        var id = tr.attr('id');
        this.disabled = true;
        
        var table_id = $($(this).parent().parent().parent().parent()).prop('id');

         $.post('/bookOrder/deleteDoc', {docId:id}, function(data){
        	 if(data.result==true){
        		 if(table_id=='one_doc_table'){
        			oneTable.row(tr).remove().draw();
    	         }else {
    	        	threeTable.row(tr).remove().draw();
    	         }
        		 table.row(tr).remove().draw();
	        	 $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
        	 }else if(data.result==false){
        		 $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
        	 }else{
        		 if(table_id=='one_doc_table'){
     			 	oneTable.row(tr).remove().draw();
	 	         }else {
	 	        	threeTable.row(tr).remove().draw();
	 	         }
        		 $.scojs_message(data.result, $.scojs_message.TYPE_ERROR);
        	 }
         },'json').fail(function() {
             	 $.scojs_message('删除失败!', $.scojs_message.TYPE_ERROR);
         });
    }); 

    //文件发送-----工作单
    $("#one_doc_table").on('click', '.confirmSend', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        var id = tr.attr('id');
        var job_order_id = $('#job_order_id').val();
        this.disabled = true;
         $.post('/bookOrder/confirmSend', {docId:id,job_order_id:job_order_id}, function(data){
        	 if(data){
        		 $.scojs_message('发送成功!', $.scojs_message.TYPE_OK);
        		 itemOrder.refleshOneDocTable(data.ORDER_ID);
        	 }
        	 $.scojs_message('发送成功!', $.scojs_message.TYPE_OK);
         },'json').fail(function() {
             $.scojs_message('后台报错!', $.scojs_message.TYPE_ERROR);
         });
    }); 

    
    //------------事件处理,文档table
    var oneTable = eeda.dt({
        id: 'one_doc_table',
        autoWidth: false,
        columns:[
			{ "data":"ID","width": "10px",
			    "render": function ( data, type, full, meta ) {
			    	return '<button type="button" class="delete btn table_btn delete_btn btn-xs">删除</button>';
			    }
			},
            { "width": "50px",
                "render": function ( data, type, full, meta ) {
                	return '<button type="button" class="confirmSend btn table_btn delete_btn btn-xs">发送PC资料</button>';
                }
            },
            { "data": "DOC_NAME","width": "280px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<a class="doc_name" href="/upload/doc/'+data+'" style="width:300px" target="_blank">'+data+'</a>';
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
    

    //刷新明细表
    itemOrder.refleshOneDocTable = function(order_id){
    	var url = "/bookOrder/docTableList?order_id="+order_id+"&type=one";
    	oneTable.ajax.url(url).load();
    }

    //第二部
  //删除一行

    //------------事件处理,文档table
    var twoTable = eeda.dt({
        id: 'two_doc_table',
        autoWidth: false,
        columns:[
                 { "width": "50px",
                     "render": function ( data, type, full, meta ) {
                     	return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="display:none">删除</button>'
                     	+'<button type="button" class="downloadDoc btn table_btn delete_btn btn-xs"><a  href="/upload/doc/'+full.DOC_NAME+'"  target="_blank">文件下载</a></button>';
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
                    return '<a class="doc_name" href="/upload/doc/'+data+'" style="width:300px" target="_blank">'+data+'</a>';
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
    
    
  //文件下载-----工作单
    $("#two_doc_table").on('click', '.downloadDoc', function(e){
        e.preventDefault();
        var self = this;
        var tr = $(this).parent().parent();
        var id = tr.attr('id');
        this.disabled = true;
         $.post('/bookOrder/downloadDoc', {docId:id}, function(data){
        	 if(data){
        		 $.scojs_message('下载完成!', $.scojs_message.TYPE_OK);
        		 self.disabled = false;
        		 itemOrder.refleshTwoDocTable(data.ORDER_ID);
        	 }
        	 
         },'json').fail(function() {
             $.scojs_message('后台报错!', $.scojs_message.TYPE_ERROR);
         }); 
    }); 
    
    
    //文件确认-----工作单
    $("#two_doc_table").on('click', '.confirmDoc', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        var id = tr.attr('id');
        this.disabled = true;
         $.post('/bookOrder/confirmDoc', {docId:id}, function(data){
        	 if(data){
        		 $.scojs_message('确认成功!', $.scojs_message.TYPE_OK);
        		 itemOrder.refleshTwoDocTable(data.ORDER_ID);
        	 }
        	 
         },'json').fail(function() {
             $.scojs_message('后台报错!', $.scojs_message.TYPE_ERROR);
         });
    }); 
    

    //刷新明细表
    itemOrder.refleshTwoDocTable = function(order_id){
    	var url = "/bookOrder/docTableList?order_id="+order_id+"&type=two";
    	twoTable.ajax.url(url).load();
    }

    
    //第三步
    //删除一行
    //文件发送-----工作  
    $("#three_doc_table").on('click', '.confirmSend', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        var id = tr.attr('id');
        var job_order_id = $('#job_order_id').val();
        this.disabled = true;
         $.post('/bookOrder/confirmSend', {docId:id,job_order_id:job_order_id}, function(data){
        	    if(data){
        			 $.scojs_message('发送成功!', $.scojs_message.TYPE_OK);
        			 itemOrder.refleshThreeDocTable(data.ORDER_ID);
        		 }
         },'json').fail(function() {
             $.scojs_message('后台报错!', $.scojs_message.TYPE_ERROR);
         });
    }); 


    
    //------------事件处理,文档table
    var threeTable = eeda.dt({
        id: 'three_doc_table',
        autoWidth: false,
        columns:[
			{ "data":"ID","width": "10px",
			    "render": function ( data, type, full, meta ) {
			    	return '<button type="button" class="delete btn table_btn delete_btn btn-xs">删除</button>';
			    }
			},
            { "width": "50px",
                "render": function ( data, type, full, meta ) {
                	return '<button type="button" class="confirmSend btn table_btn delete_btn btn-xs">发送PC资料</button>';
                }
            },
            { "data": "DOC_NAME","width": "280px",
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<a class="doc_name" href="/upload/doc/'+data+'" style="width:300px" target="_blank">'+data+'</a>';
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
    
    
  //刷新明细表
    itemOrder.refleshThreeDocTable = function(order_id){
    	var url = "/bookOrder/docTableList?order_id="+order_id+"&type=three";
    	threeTable.ajax.url(url).load();
    }

    

    //第4部
  //删除一行
    //------------事件处理,文档table
    var fourTable = eeda.dt({
        id: 'four_doc_table',
        autoWidth: false,
        columns:[
                 { "width": "50px",
                     "render": function ( data, type, full, meta ) {
                     	return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="display:none">删除</button>'
                     	+'<button type="button" class="downloadDoc btn table_btn delete_btn btn-xs"><a  href="/upload/doc/'+full.DOC_NAME+'"  target="_blank">文件下载</a></button>';
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
                    return '<a class="doc_name" href="/upload/doc/'+data+'" style="width:300px" target="_blank">'+data+'</a>';
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
    
    
  //文件下载-----工作单
    $("#four_doc_table").on('click', '.downloadDoc', function(e){
        e.preventDefault();
        var self = this;
        var tr = $(this).parent().parent();
        var id = tr.attr('id');
        this.disabled = true;
         $.post('/bookOrder/downloadDoc', {docId:id}, function(data){
        	 if(data){
        		 $.scojs_message('下载完成!', $.scojs_message.TYPE_OK);
        		 self.disabled = false;
        		 itemOrder.refleshFourDocTable(data.ORDER_ID);
        	 }
        	 
         },'json').fail(function() {
             $.scojs_message('后台报错!', $.scojs_message.TYPE_ERROR);
         }); 
    }); 
    
    
    //文件确认-----工作单
    $("#four_doc_table").on('click', '.confirmDoc', function(e){
        e.preventDefault();
        var tr = $(this).parent().parent();
        var id = tr.attr('id');
        this.disabled = true;
         $.post('/bookOrder/confirmDoc', {docId:id}, function(data){
        	 if(data){
        		 $.scojs_message('确认成功!', $.scojs_message.TYPE_OK);
        		 itemOrder.refleshFourDocTable(data.ORDER_ID);
        	 }
        	 
         },'json').fail(function() {
             $.scojs_message('后台报错!', $.scojs_message.TYPE_ERROR);
         });
    }); 
    
    
    itemOrder.refleshFourDocTable = function(order_id){
    	var url = "/bookOrder/docTableList?order_id="+order_id+"&type=four";
    	fourTable.ajax.url(url).load();
    }
    
    
    //第zero部
    //删除一行
      //------------事件处理,文档table
      var zeroTable = eeda.dt({
          id: 'zero_doc_table',
          autoWidth: false,
          columns:[
                   { "width": "50px",
                       "render": function ( data, type, full, meta ) {
                       	return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="display:none">删除</button>'
                       	+'<button type="button" class="downloadDoc btn table_btn delete_btn btn-xs"><a  href="/upload/doc/'+full.DOC_NAME+'"  target="_blank">文件下载</a></button>';
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
              }
          ]
      });
      
      
    //文件下载-----工作单
      $("#zero_doc_table").on('click', '.downloadDoc', function(e){
          e.preventDefault();
          $(this).parents('tr').find('.doc_name').click();
          var self = this;
          var tr = $(this).parent().parent();
           var href=tr.find('.doc_name').attr('href');
          window.open(href);
          var id = tr.attr('id');
          this.disabled = true;
           $.post('/bookOrder/downloadDoc', {docId:id}, function(data){
          	 if(data){
          		 $.scojs_message('下载完成!', $.scojs_message.TYPE_OK);
          		 self.disabled = false;
          		 itemOrder.refleshFourDocTable(data.ORDER_ID);
          	 }
          	 
           },'json').fail(function() {
               $.scojs_message('后台报错!', $.scojs_message.TYPE_ERROR);
           }); 
      }); 
      
      
      //文件确认-----工作单
      $("#zero_doc_table").on('click', '.confirmDoc', function(e){
          e.preventDefault();
          var tr = $(this).parent().parent();
          var id = tr.attr('id');
          this.disabled = true;
           $.post('/bookOrder/confirmDoc', {docId:id}, function(data){
          	 if(data){
          		 $.scojs_message('确认成功!', $.scojs_message.TYPE_OK);
          		 itemOrder.refleshZeroDocTable(data.ORDER_ID);
          	 }
          	 
           },'json').fail(function() {
               $.scojs_message('后台报错!', $.scojs_message.TYPE_ERROR);
           });
      }); 
      
      
      itemOrder.refleshZeroDocTable = function(order_id){
      	var url = "/bookOrder/docTableList?order_id="+order_id+"&type=zero";
      	zeroTable.ajax.url(url).load();
      }
});
});