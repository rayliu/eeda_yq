define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'jq_blockui'], function ($, metisMenu) {
	$(document).ready(function() {
    	document.title = '错误报表 | '+document.title;

    	$("#breadcrumb_li").text('错误报表');

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: false, //不打开会出现排序不对
            ajax: "/errorReport/list",
            "drawCallback": function( settings ) {
                $.unblockUI();
            },
            columns:[
				{ "width": "5px",
				    "render": function ( data, type, full, meta ) {
				      return '<input type="checkBox" name="checkBox">';
				    }
				},
				{ "width": "30px",
                    "render": function ( data, type, full, meta ) {
                      return '<button type="button" class="btn btn-primary delete_btn btn-xs">'+
                        '<i class="fa fa-trash-o"></i> 删除</button>';
                    }
                },
                {"data": "ORDER_TYPE",'class':'order_type', "width": "100px"},
				{ "data": "ERROR_MSG", "width": "220px",
					"render": function ( data, type, full, meta ) {
					  		return "<span style='color:red;'>"+data+"</span>";
					  	}	
				}, 
				{"data": "ITEM_NO", "width": "80px",
					  "render": function ( data, type, full, meta ) {
						  //return "<a href='/wmsproduct/edit?id="+full.PRODUCT_ID+"'target='_blank'>"+data+"</a>";
						  return data;
					  }
				},
				{"data": "ITEM_NAME", "width": "280px"},
				
				{ "data": "PART_NO", "width": "120px"}, 
				{ "data": "PART_NAME", "width": "320px"}, 
				{ "data": "SHELVES", "width": "80px"},
				{ "data": "QUANTITY", "width": "60px"},
				{ "data": "MOVE_FLAG", "width": "60px"}, 
				{ "data": "CREATE_TIME", "width": "120px"},
				{ "data": "CREATOR_NAME", "width": "80px"},
                { "data": "QR_CODE", "width": "480px"}, 
            ]
        });
        
        $('#checkBox').click(function(){
        	var self = this;
        	var btn = $(self).text();
        	
        	if(btn == '全选'){
        		$(self).text('取消选中');
        		$('#eeda-table [name=checkBox]').prop('checked',true);
        	}else{
        		$(self).text('全选');
        		$('#eeda-table [name=checkBox]').prop('checked',false);
        	}
        });
        
        $('#eeda-table').on('click','.delete_btn',function(){
        	var self = this;
        	var id = $(this).parent().parent().attr('id');
        	var order_type = $($(this).parent().parent().find('.order_type')).text();
        	
        	self.disabled = true;
        	$.post('/errorReport/delete',{id:id,order_type:order_type},function(data){
        		if(data){
        			$.scojs_message('删除成功', $.scojs_message.TYPE_OK);
        			searchData();
        			self.disabled = false;
        		}else{
        			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
        		}
        	}).fail(function() {
                $.scojs_message('后台报错', $.scojs_message.TYPE_ERROR);
                self.disabled = false;
            });
        });
        
        
        
        $('#gateInBox').click(function(){
        	var self = this;
        	
        	var idArray = [];
        	$('#eeda-table [name=checkBox]:checked').each(function(){
        		var id = $(this).parent().parent().attr('id');
        		var order_type = $($(this).parent().parent().find('.order_type')).text();
        		if(order_type!='未入库'){
        			$.scojs_message('请勾选要入库的单据', $.scojs_message.TYPE_ERROR);
        			return false;
        		}
        		idArray.push(id);
        	});
        	
        	if(idArray.length==0){
        		$.scojs_message('请勾选要入库的单据', $.scojs_message.TYPE_ERROR);
        		return false;
        	}
        	
        	self.disabled = true;
        	
        	$.post('/errorReport/gateIn',{idArray:idArray.toString()},function(data){
        		if(data){
        			$.scojs_message('手工入库成功', $.scojs_message.TYPE_OK);
        			searchData();
        			self.disabled = false;
        		}else{
        			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
        		}
        	}).fail(function() {
                $.scojs_message('后台报错', $.scojs_message.TYPE_ERROR);
                self.disabled = false;
            });
        });
        
        
//        $('#deleteBox').click(function(){
//        	var self = this;
//        	
//        	var idArray = [];
//        	$('#eeda-table [name=checkBox]:checked').each(function(){
//        		var id = $(this).parent().parent().attr('id');
//        		idArray.push(id);
//        	});
//        	
//        	if(idArray.length==0){
//        		$.scojs_message('请勾选要删除的单据', $.scojs_message.TYPE_ERROR);
//        		return false;
//        	}
//        	
//        	self.disabled = true;
//        	
//        	$.post('/errorReport/delete',{idArray:idArray.toString()},function(data){
//        		if(data){
//        			$.scojs_message('删除成功', $.scojs_message.TYPE_OK);
//        			searchData();
//        			self.disabled = false;
//        		}else{
//        			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
//        		}
//        	}).fail(function() {
//                $.scojs_message('后台报错', $.scojs_message.TYPE_ERROR);
//                self.disabled = false;
//            });
//        });
      
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
	        return item;
        };
      
        var searchData=function(){
        	var itemJson = buildCondition();
        	var url = "/errorReport/list?jsonStr="+JSON.stringify(itemJson);
        	dataTable.ajax.url(url).load();
        };
        
        
	});
});