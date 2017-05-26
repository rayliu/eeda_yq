define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'jq_blockui'], function ($, metisMenu) {
	$(document).ready(function() {
    	document.title = '错误报表 | '+document.title;

    	$("#breadcrumb_li").text('错误报表');

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/errorReport/list",
            "drawCallback": function( settings ) {
                $.unblockUI();
            },
            columns:[
				{ "width": "5px",
				    "render": function ( data, type, full, meta ) {
				      return '<input type="checkBox" name="checkBtn">';
				    }
				},
                {"data": "ORDER_TYPE",'class':'order_type', "width": "70px"},
				{ "data": "ERROR_MSG", "width": "220px",'class':"error_msg",
					"render": function ( data, type, full, meta ) {
                            if(data){
					  		   return "<span style='color:red;'>"+data+"</span>";
                            }else{
                                return '';
                            }
					  	}	
				}, 
				{ "data": "IMPORT_MSG", "width": "220px",
					"render": function ( data, type, full, meta ) {
				  		if(data){
                               return "<span style='color:red;'>"+data+"</span>";
                            }else{
                                return '';
                            }
				  	}
				}, 
				{"data": "ITEM_NO", "width": "80px",
					  "render": function ( data, type, full, meta ) {
						  return data;
					  }
				},
				{"data": "ITEM_NAME", "width": "280px"},
				{ "data": "PART_NO", "width": "120px"}, 
				{ "data": "PART_NAME", "width": "320px"}, 
				{ "data": "SHELVES", "width": "80px"},
				{ "data": "QUANTITY", "width": "50px"},
				{ "data": "MOVE_FLAG", "width": "80px"}, 
				{ "data": "CREATE_TIME", "width": "150px"},
				{ "data": "CREATOR_NAME", "width": "80px"},
                { "data": "QR_CODE", "width": "480px"}, 
            ]
        });
        
        $('#checkBtn').click(function(){
        	var self = this;
        	var btn = $(self).text();
        	
        	if(btn == '全选'){
        		$(self).text('取消选中');
        		$('#eeda-table [name=checkBtn]').prop('checked',true);
        	}else{
        		$(self).text('全选');
        		$('#eeda-table [name=checkBtn]').prop('checked',false);
        	}
        });
        

        
        
        
        $('#gateInBtn').click(function(){
        	var self = this;
        	
        	var idArray = [];
        	$('#eeda-table [name=checkBtn]:checked').each(function(){
        		var id = $(this).parent().parent().attr('id');
        		var order_type = $($(this).parent().parent().find('.order_type')).text();
        		var error_msg = $($(this).parent().parent().find('.error_msg')).text();
        		if(error_msg!='未入库'){
        			$.scojs_message('请勾选要入库的单据', $.scojs_message.TYPE_ERROR);
        			return false;
        		}
        		idArray.push(id);
        	});
        	
        	if(idArray.length==0){
        		$.scojs_message('请勾选要入库的单据', $.scojs_message.TYPE_ERROR);
        		return false;
        	}
        	
        	
        	if(!confirm('是否确认入库')){
        		return false;
        	}
        	self.disabled = true;
        	$.post('/errorReport/gateIn',{idArray:idArray.toString()},function(data){
        		if(data){
        			$('#checkBtn').text('全选');
        			$.scojs_message('手工入库成功', $.scojs_message.TYPE_OK);
        			searchData();
        			self.disabled = false;
        		}else{
        			$.scojs_message('操作失败,三秒后自动刷新页面', $.scojs_message.TYPE_ERROR);
        			window.setTimeout(function(){
        				$('#checkBtn').text('全选');
                    	 location.reload();
                    },3000); 
        		}
        	}).fail(function() {
                $.scojs_message('后台报错', $.scojs_message.TYPE_ERROR);
                self.disabled = false;
            });
        });
        
        
        $('#deleteBtn').click(function(){
        	var self = this;
        	
        	var jsonArray = {};
        	var idArray = [];
        	$('#eeda-table [name=checkBtn]:checked').each(function(){
        		var id = $(this).parent().parent().attr('id');
        		var order_type = $($(this).parent().parent().find('.order_type')).text();
        		var group = {}
        		group.order_type = order_type;
        		group.id = id;
        		
        		idArray.push(group);
        	});
        	jsonArray.array = idArray;
        	
        	if(idArray.length==0){
        		$.scojs_message('请勾选要删除的单据', $.scojs_message.TYPE_ERROR);
        		return false;
        	}
        	
        	if(!confirm('是否确认删除')){
        		return false;
        	}
        	self.disabled = true;
        	$.post('/errorReport/delete',{jsonArray:JSON.stringify(jsonArray)},function(data){
        		if(data){
        			$('#checkBtn').text('全选');
        			$.scojs_message('删除成功', $.scojs_message.TYPE_OK);
        			searchData();
        			self.disabled = false;
        		}else{
        			$.scojs_message('操作失败,三秒后自动刷新页面', $.scojs_message.TYPE_ERROR);
        			window.setTimeout(function(){
        				$('#checkBtn').text('全选');
                    	 location.reload();
                    },3000); 
        		}
        	}).fail(function() {
                $.scojs_message('后台报错', $.scojs_message.TYPE_ERROR);
                self.disabled = false;
            });
        });
      
        $('#resetBtn').click(function(e){
        	$("#orderForm")[0].reset();
        });

        $('#searchBtn').click(function(){
            $.blockUI({ 
                message: '<h1><img src="/images/loading.gif" style="height: 50px; margin-top: -3px;"/> LOADING...</h1>' 
            });
        	searchData(); 
        })
 
        buildCondition=function(){
	      	var item = {};
	      	var orderForm = $('#orderForm input, #orderForm select');
	      	for(var i = 0; i < orderForm.length; i++){
	      		var name = orderForm[i].id;
	          	var value =orderForm[i].value;
	          	if(name){
	          		if(value)
	          			value = value.trim();
	          		item[name] = value;
	          	}
	      	}
	      	item.order_type = $('[name=optionsRadios]:checked').val();
	        return item;
        };
        
        $('[name=optionsRadios]').on('change',function(){
        	var value = this.value;
        	if(value=='gateOut'){
        		$('#gateInBtn').show();
        		$('#deleteBtn').hide();
        	}else{
        		$('#gateInBtn').hide();
        		$('#deleteBtn').show();
        	}
        	
        	searchData();
        })
      
        var searchData=function(){
        	var itemJson = buildCondition();
        	var url = "/errorReport/list?jsonStr="+JSON.stringify(itemJson);
        	dataTable.ajax.url(url).load();
        };
	});
});