define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) { 
	$(document).ready(function() {

	    var deletedTableIds=[];

	    //删除一行
	    $("#dock_table").on('click', '.delete', function(e){
	        e.preventDefault();
	        var tr = $(this).parent().parent();
	        deletedTableIds.push(tr.attr('id'));
	        
	        cargoTable.row(tr).remove().draw();
	    }); 
	    
	    //构造函数，获得json
	    itemOrder.buildDockItem=function(){
	    	var cargo_table_rows = $("#dock_table tr");
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
	            item.party_type = 'customer';
	            item.office_id = $('#office_id').val();
	            for(var i = 1; i < row.childNodes.length; i++){
	            	var name = $(row.childNodes[i]).find('input,select,textarea,span').attr('name');
	            	var value = $(row.childNodes[i]).find('input,select,textarea,span').val();
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
	    

	    //------------事件处理
	    var cargoTable = eeda.dt({
            id: 'dock_table',
            autoWidth: false,
            "drawCallback": function( settings ) {
		        
		    },
            columns:[
	            {"data":"ID","width": "3%",
	                "render": function ( data, type, full, meta ) {
	                		return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:100%" >删除</button> ';
	                }
	            },
	            { "data": "DOCK_NAME","width": "20%",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return '<input style="width:100%"  type="text" name="dock_name" value="'+data+'" class="form-control search-control" />';
	                }
	            },
	            {"data":"LAND_CONTACTS","width":"10%",
	            	 "render": function ( data, type, full, meta ) {
	            		 if(!data)
	            			 data='';
	            		return '<input style="width:100%" type="text" name="land_contacts" value="'+data+'" class="form-control search-control" />';
	            		 
	            	 }
	            },
	            {"data":"LAND_CONTACT_PHONE","width":"10%",
	            	 "render": function ( data, type, full, meta ) {
	            		 if(!data)
	            			 data='';
	            		return '<input style="width:100%" type="text" name="land_contact_phone" value="'+data+'" class="form-control search-control" />';
	            		 
	            	 }
	            },
	            {"data":"BILL_REMARK","width":"20%",
	            	 "render": function ( data, type, full, meta ) {
	            		 if(!data)
	            			 data='';
	            		return '<textarea style="width:100%" rows="2" type="text" name="bill_remark" value="'+data+'" class="form-control search-control" >'+data+'</textarea>';
	            		 
	            	 }
	            },
	            { "data":"PICTURE_LINK","width":"10%",
	            	"render": function ( data, type, full, meta ) {
	            		if(full.ID){
	            			if(data){
		            			var str = '<a href="/upload/dockinfo/'+data+'" target="_blank">'+data+'</a>&nbsp;&nbsp;'
		            			  +'<a id="'+data+'" class="glyphicon glyphicon-remove delete_icon_of_sign_desc" style="margin-right:15px;" role="menuitem" tabindex="-10"></a>';
		            			return '<span style="width:100%;" name="picture_link" >'+str+'</span>';
		            		}else{
		            			return '<span class="btn table_btn btn_green btn-xs fileinput-button upload" style="width:100%" title="请先保存再上传文件">' 
			                		+'<i class="glyphicon glyphicon-plus"></i>'
			                		+'<span>上传图片</span>'
			                		+'<input   type="file" multiple>'
			                		+'</span>';
		            		}
	            		}else{
	            			return '<span class="btn table_btn btn_green btn-xs fileinput-button upload" style="width:100%" disabled title="请先保存再上传文件">' 
	                		+'<i class="glyphicon glyphicon-plus"></i>'
	                		+'<span disabled>上传图片</span>'
	                		+'<input   type="button" disabled>'
	                		+'</span>';
	            		}
	            		
			            				
	            	}
	            },
	            { "data": "DOCK_NAME_ENG","width": "20%",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
	                        data='';
	                    return '<input type="text" name="dock_name_eng" value="'+data+'" class="form-control search-control" style="width:100%"/>';
	                }
	            },
	            { "data": "DOCK_REGION","width": "10%",
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                	   data='';
	                   return '<input type="text" name="dock_region" value="'+data+'" class="form-control search-control" style="width:100%"/>';
	                }
	            },
	            { "data": "PICTURE_LINK_ID","visible":false,
	                "render": function ( data, type, full, meta ) {
	                   if(!data)
	                	   data='';
	                   return '<input type="text" name="picture_link_id" value="'+data+'" class="form-control search-control" style="width:100%"/>';
	                }
	            }
	        ]
	    });
	    
	    //单据备注信息填写
	    var dock_bill_remark_self;
	    $('#dock_table').on('click','[name=bill_remark]',function(){
	    	dock_bill_remark_self = $(this);
	    	$('#showNote').val(dock_bill_remark_self.val());
	    	$('#a_btn').click();
	    });
	    $('#btnConfirm').click(function(){
    		var showNote = $('#showNote').val();
    		dock_bill_remark_self.val(showNote);
    	})

	    $('#add_cargo').on('click', function(){
	        var item={};
	        cargoTable.row.add(item).draw(true);
	    });
	    //上传图片
	    $("#dock_table").on('click', '.upload', function(){
			var id = $(this).parent().parent().attr('id');
			var order_id = $('#partyId').val();
				$(this).fileupload({
					autoUpload: true, 
				    url: '/customer/saveLandDocFile?id='+id,
				    dataType: 'json',
			        done: function (e, data) {
	                    if(data.result.result){
	    		    		$.scojs_message('上传成功', $.scojs_message.TYPE_OK);
	    		    		itemOrder.refleshTable(order_id);
	                    }else{
	                        $.scojs_message('上传失败:'+data.result.ERRMSG, $.scojs_message.TYPE_ERROR);
	                    }
				     },
			        error: function () {
			            alert('上传的时候出现了错误！');
			        }
				});
		});
	    
	    //单个删除签收文件
	    $("#dock_table").on('click', '.delete_icon_of_sign_desc', function(){
	    	var id =  $(this).parent().parent().parent().attr('id');
	    	var order_id = $('#partyId').val();
		     $.post('/customer/delectLandDocFile', {id:id}, function(data){
		        	 $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
		        	 itemOrder.refleshTable(order_id);
		     },'json').fail(function() {
		         	 $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
		     });
	    })
	    
	    //刷新明细表
	    itemOrder.refleshTable = function(order_id){
	    	var url = "/customer/tableList?order_id="+order_id+"&type=dock";
	    	cargoTable.ajax.url(url).load();
	    }
	    
	    //校验
        $('#dock_table').on('blur','[name=dock_name],[name=land_contacts],[name=land_contact_phone],[name=bill_remark],[name=dock_name_eng],[name=dock_region]',function(){
        	var data = $(this).val();
        	var name = $(this).attr("name");
        	var len = $.trim(data).length;
        	if(name=="dock_name"||name=="dock_name_eng"){
        		var re = /^.{100,}$/;
        		if(re.test(data)&&len>0){
        			$(this).parent().append("<span style='color:red;display:block;' class='error_span'>只能输入长度100内的字符串</span>");
        			return;
        		}
        	}
        	if(name=="land_contacts"){
        		var re = /^.{50,}$/;
        		if(re.test(data)&&len>0){
        			$(this).parent().append("<span style='color:red;display:block;' class='error_span'>只能输入长度50内的字符串</span>");
        			return;
        		}
        	}
        	if(name=="bill_remark"){
        		var re = /^.{1000,}$/;
        		if(re.test(data)&&len>0){
        			$(this).parent().append("<span style='color:red;display:block;' class='error_span'>只能输入长度1000内的字符串</span>");
        			return;
        		}
        	}
        	if(name=="dock_region"){
        		var re = /^.{255,}$/;
        		if(re.test(data)&&len>0){
        			$(this).parent().append("<span style='color:red;display:block;' class='error_span'>只能输入长度255内的字符串</span>");
        			return;
        		}
        	}
        	if(name=="land_contact_phone"){
        		var re = /^[\u4e00-\u9fa5]$/;
        		if(re.test(data)){
            		$(this).parent().append("<span style='color:red;display:block;' class='error_span'>不能输入汉字</span>");
            	}else if(len>100){
            		$(this).parent().append("<span style='color:red;display:block;' class='error_span'>只能输入长度100内的字符串</span>");
            	}
        	}
        });
        $('#dock_table').on('focus','[name=dock_name],[name=land_contacts],[name=land_contact_phone],[name=bill_remark],[name=dock_name_eng],[name=dock_region]',function(){
        	$(this).parent().find("span").remove();
        });
	    
	});
});
