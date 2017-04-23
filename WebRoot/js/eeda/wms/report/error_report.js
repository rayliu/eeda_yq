define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
	$(document).ready(function() {
    	document.title = '错误报表 | '+document.title;

    	$("#breadcrumb_li").text('错误报表');

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: false, //不打开会出现排序不对
            ajax: "/errorReport/list",
            columns:[
				{ "width": "30px",
				    "render": function ( data, type, full, meta ) {
				      return '<input type="checkBox" name="checkBox">';
				    }
				},
                {"data": "ORDER_TYPE"},
				{ "data": "ERROR_MSG",
					"render": function ( data, type, full, meta ) {
					  		return "<span style='color:red;'>"+data+"</span>";
					  	}	
				}, 
				{"data": "ITEM_NO", 
					  "render": function ( data, type, full, meta ) {
						  //return "<a href='/wmsproduct/edit?id="+full.PRODUCT_ID+"'target='_blank'>"+data+"</a>";
						  return data;
					  }
				},
				{"data": "ITEM_NAME"},
				{ "data": "QR_CODE"}, 
				{ "data": "PART_NO"}, 
				{ "data": "PART_NAME"}, 
				{ "data": "SHELVES"},
				{ "data": "QUANTITY"},
				{ "data": "MOVE_FLAG"}, 
				{ "data": "CREATE_TIME"},
				{ "data": "CREATOR_NAME"}
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
        
        $('#gateInBox').click(function(){
        	var self = this;
        	
        	var idArray = [];
        	$('#eeda-table [name=checkBox]:checked').each(function(){
        		var id = $(this).parent().parent().attr('id');
        		idArray.push(id);
        	});
        	
        	if(idArray.length==0){
        		$.scojs_message('请勾选单据', $.scojs_message.TYPE_ERROR);
        		return false;
        	}
        	
        	self.disabled = true;
        	
        	$.post('/errorReport/gateIn',{idArray:idArray.toString()},function(data){
        		if(data){
        			$.scojs_message('手工入库成功', $.scojs_message.TYPE_OK);
        			searchData();
        		}else{
        			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
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