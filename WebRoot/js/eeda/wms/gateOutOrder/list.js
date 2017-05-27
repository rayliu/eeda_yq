define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','./item_list', 'jq_blockui'], function ($, metisMenu) {
	$(document).ready(function() {
    	document.title = '出库单 | '+document.title;

    	$("#breadcrumb_li").text('出库单列表 ');

        
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/gateOutOrder/list?jsonStr={}",
            drawCallback: function( settings ) {
		        $.unblockUI();
		    },
		    columns:[
		                { "data": null,
		                    "render": function ( data, type, full, meta ) {
		                      return '<input type="checkBox" name="checkBox" checked disabled>';
		                    }
		                },
		                { "data": "PART_NO"}, 
						{ "data": "PART_NAME"}, 
						{ "data": "SHELVES_TOTAL_PIECE"},
						{ "data": "ACT_QUANTITY"}
		            ]
        });
        

        $('#searchBtn').click(function(){
        	var total = parseFloat($('#quantity').val());
        	var item_no = $('#item_no_input').val();
        	if(!item_no.trim()){
        		$.scojs_message('item_no不能为空', $.scojs_message.TYPE_FALSE);
        		return false;
        	}
        	if(!total){
        		$.scojs_message('数量不规范', $.scojs_message.TYPE_FALSE);
        		return false;
        	}
        	searchData();
        	$('#createBtn').attr('disabled',false);
        });
        
        
        $('#createBtn').click(function(){
        	$.blockUI({ 
                message: '<h1><img src="/images/loading.gif" style="height: 50px; margin-top: -3px;"/> COMMITTING...</h1>' 
            });
        	var self = this;
        	self.disabled = true;
        	var itemJson = buildCondition();
        	$.post('/gateOutOrder/create',{jsonStr:JSON.stringify(itemJson)},function(data){
	        	if(data){
	        		$.scojs_message('单据'+data.ORDER_NO+'创建成功', $.scojs_message.TYPE_OK);
	        		order.refleshTable();
	        		 $.unblockUI();
	        	}
	        }).fail(function() {
	            $.scojs_message('创建失败', $.scojs_message.TYPE_ERROR);
	            self.disabled = false;
	            $.unblockUI();
	        });
        });
        
        
        var orderTable = eeda.dt({
            id: 'order_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            //ajax: "/gateOutOrder/orderList",
            columns:[
                { "width": "30px",
                    "render": function ( data, type, full, meta ) {
                      return '<button type="button" class="btn btn-primary btn-xs print">'+
                        '<em class="icon-print"></em> 打印</button>';
                    }
                },
                { "data": "KT_NO"}, 
                {"data": "ORDER_NO",
                	"render": function ( data, type, full, meta ) {
              	    	if(!data)
              	    		data = '';
              	    	return data;
              	    	//return "<a class='item_detail' order_id='"+full.ID+"' data-target='#itemDetail' data-toggle='modal'>"+data+"</a>";
              	    }
                },
				{ "data": "ITEM_NO","class":"item_no"}, 
				/*{ "data": "TOTAL_QUANTITY"}, */
				{ "data": "QUANTITY"}, 
				{ "data": "CREATE_TIME"},
				{ "data": "CREATOR_NAME"}
            ]
        });
        
        
        var actualTable = eeda.dt({
            id: 'actual_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            //ajax: "/gateOutOrder/actualList",
            columns:[
                { "width": "30px",
                    "render": function ( data, type, full, meta ) {
                      return '<button type="button" class="btn table_btn delete_btn btn-xs" style="display:none">'+
                        '<i class="fa fa-trash-o"></i> 删除</button>'+
                        '<button type="button" class="btn btn-primary btn-xs print" disabled>'+
                        '<em class="icon-print"></em> 打印</button>';
                    }
                },
                { "data": "DATE_NO"}, 
                { "data": "KT_NO"}, 
                {"data": "ORDER_NO", 
                	"render": function ( data, type, full, meta ) {
              	    	if(!data)
              	    		data = '';
              	    	return data;
              	    	//return "<a class='item_detail' order_id='"+full.ID+"' data-target='#itemDetail' data-toggle='modal'>"+data+"</a>";
              	    }
                },
				{ "data": "ITEM_NO"}, 
				{ "data": "TOTAL_QUANTITY"}, 
				{ "data": "QUANTITY"}, 
				{ "data": "CREATE_TIME"},
				{ "data": "CREATOR_NAME"}
            ]
        });
        
        
        //打印明细
        $("#order_table").on('click', '.print', function(e){
        	var self = this;
        	self.disabled = true;
        	
        	var order_id = $(self).parent().parent().attr("id");
        	var item_no = $($(self).parent().parent().find(".item_no")).text();
        	$.post('/gateOutOrder/printDetailPDF',{order_id:order_id,item_no:item_no},function(data){
        		if(data){
        			window.open(data);
        		}else{
        			$.scojs_message('生成PDF失败',$.scojs_message.TYPE_ERROR);
        		}
        		self.disabled = false;
        	}).fail(function() {
                $.scojs_message('生成PDF失败,后台报错', $.scojs_message.TYPE_ERROR);
                self.disabled = false;
            });
        });
      
        $('#resetBtn').click(function(e){
        	$('#createBtn').attr('disabled',true);
        	$("#orderForm")[0].reset();
            $('.itemShow').hide();
            $('#productName').text("");
            searchData();
        });

 
        buildCondition=function(){
	      	var item = {};
	      	var orderForm = $('#orderForm input,select');
	      	for(var i = 0; i < orderForm.length; i++){
	      		var name = orderForm[i].id;
	          	var value =orderForm[i].value;
	          	if(name){
	          		if(value)
	          			value = value.trim();
	          		item[name] = value;
	          	}
		      	item.item_no=$('#item_no_input').val();
	      	}
	        return item;
        };
      
        
        
        var orderFlag = 0;
        $('#orderTab').on('click',function(){
        	if(orderFlag==0){
        		orderTable.ajax.url("/gateOutOrder/orderList").load();
        		orderFlag = 1;
        	}
        	
        });
        
        var gateOutFlag = 0;
        $('#gateOutTab').on('click',function(){
        	if(gateOutFlag==0){
        		actualTable.ajax.url("/gateOutOrder/actualList").load();
             	gateOutFlag = 1;
        	}
        });
        
        
//        $('#kt_no').on('input',function(){
//        	var kt_no = $('#kt_no').val();
//        	if(kt_no.trim()=="")
//        		return;
//        	
//        	$.post('/gateOutOrder/searchKT',{kt_no:kt_no},function(data){
//        		if(data.ITEM_NO){
//        			$('#item_no').val(data.ITEM_NO);
//        			$('#item_no_input').val(data.ITEM_NO);
//        			$('#totalQuantity').val(data.TOTAL_QUANTITY);
//        			$('#haveQuantity').val(data.QUANTITY);
//        		}
//        	}).fail(function() {
//                $.scojs_message('后台报错', $.scojs_message.TYPE_ERROR);
//            });
//        });
        
        var searchData=function(){
            $.blockUI({ 
                message: '<h1><img src="/images/loading.gif" style="height: 50px; margin-top: -3px;"/> LOADING...</h1>' 
            });
            var itemJson = buildCondition();
            var url = "/gateOutOrder/list?jsonStr="+JSON.stringify(itemJson);
            dataTable.ajax.url(url).load();
        };

        
        order.refleshTable = function(){
        	$.blockUI({ 
                message: '<h1><img src="/images/loading.gif" style="height: 50px; margin-top: -3px;"/> LOADING...</h1>' 
            });
        	orderTable.ajax.url("/gateOutOrder/orderList").load();
        }

	});
});