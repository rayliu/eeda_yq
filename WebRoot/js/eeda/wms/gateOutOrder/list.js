define(['jquery', 'metisMenu', 'sb_admin','dataTables',  'dataTablesBootstrap', 'validate_cn', 'sco','./item_list', 'jq_blockui'], function ($, metisMenu) {
	$(document).ready(function() {
    	document.title = '出库单 | '+document.title;

    	$("#breadcrumb_li").text('出库单列表 ');

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: false, //不打开会出现排序不对
            //ajax: "/gateOutOrder/numLlist",
            "drawCallback": function( settings ) {
		        $.unblockUI();
		    },
            columns:[
                { "width": "30px",
                    "render": function ( data, type, full, meta ) {
                      return '<input type="checkBox" name="checkBox" checked disabled>';
                    }
                },
                { "data": "ITEM_NO"}, 
                {"data": "ITEM_NAME", 
              	    "render": function ( data, type, full, meta ) {
              		    return data;
              	    }
                },
                { "data": "PART_NO"}, 
				{ "data": "PART_NAME"}, 
				{ "data": "AMOUNT"},
				{ "data": "QUANTITY"},
				{ "data": "SHELVES"}, 
				{ "data": "CREATE_TIME"},
				{ "data": "CREATOR_NAME"}
            ]
        });
        
        var idArray = [];
        $('#searchBtn').click(function(){
        	$("#msgLoad").empty();
        	var total = parseFloat($('#quantity').val());
        	var item_no = $('#item_no').val();
        	var kt_no = $('#kt_no').val();
        	if(item_no.trim() == '' || kt_no.trim() == ''){
        		$.scojs_message('产品编码和KT_NO不能为空', $.scojs_message.TYPE_FALSE);
        		return false;
        	}

        	if(!total){
        		$.scojs_message('数量不规范', $.scojs_message.TYPE_FALSE);
        		return false;
        	}
        	$.blockUI({ 
                message: '<h1><img src="/images/loading.gif" style="height: 50px; margin-top: -3px;"/> LOADING...</h1>' 
            });
        	
        	var itemJson = buildCondition();
        	var url = "/gateOutOrder/list?jsonStr="+JSON.stringify(itemJson);
        	var partJson = {};
        	$.post(url,function(data){
        		var itemTable = $('#eeda-table').DataTable();
        		var itemCount = itemTable.rows().count();
        		for(var c = 0 ;c < itemCount;c++){
        			itemTable.row(0).remove().draw();
        		}
        		idArray = [];
                for(var i=0; i<data.length; i++){
                	var row = data[i];
                	var part_no = row.PART_NO;
                	var quantity = parseFloat(row.QUANTITY);//入库一箱的数量
                	var amount = parseFloat(row.AMOUNT);//使用量
                	if(partJson[part_no]==undefined){
                		partJson[part_no] = total*amount-quantity;
                	}else{
                		if(partJson[part_no]<=0)
                			continue;
                		partJson[part_no] = parseFloat(partJson[part_no])-quantity;
                	}
                	
                	idArray.push(row.ID);
                	var item={};
                	item.ID = row.ID;
                	item.ITEM_NO = row.ITEM_NO;
                	item.ITEM_NAME = row.ITEM_NAME;
                	item.PART_NO = row.PART_NO;
                	item.PART_NAME = row.PART_NAME;
                	item.AMOUNT = row.AMOUNT;
                	item.QUANTITY = row.QUANTITY;
                	item.SHELVES = row.SHELVES;
                	item.CREATE_TIME = row.CREATE_TIME;
                	item.CREATOR_NAME = row.CREATOR_NAME;
                	itemTable.row.add(item).draw();
                }

                var flag = true;
                for(part in partJson){
                	var name = part;
                	var num = partJson[name];
                	if(parseFloat(num)>0){
                		$('#myModal').modal('show');
                		$("#footer").show();
        	        	$("#msgLoad").append('<h4>组件：'+name+'数量还差'+num+'件</h4>');
        	        	flag = false;
                	}
                }
                
                if(flag && data.length>0){
                	$('#createBtn').prop('disabled',false);
                }else{
                	$('#createBtn').prop('disabled',true);
                }
                $.unblockUI();
        	}).fail(function() {
                $.scojs_message('查询失败', $.scojs_message.TYPE_ERROR);
                $('#searchBtn').attr('disabled', false);
                $.unblockUI();
            });
        });
        
        $('#createBtn').click(function(){
        	$.blockUI({ 
                message: '<h1><img src="/images/loading.gif" style="height: 50px; margin-top: -3px;"/> COMMITTING...</h1>' 
            });
        	var self = this;
        	self.disabled = true;
        	var item_no = $('#item_no').val();
        	var totalQuantity = $('#totalQuantity').val();
        	var quantity = $('#quantity').val();
        	var kt_no = $('#kt_no').val();
        	$.post('/gateOutOrder/create',{item_no:item_no,quantity:quantity,totalQuantity:totalQuantity,idArray:idArray.toString(),kt_no:kt_no},function(data){
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
            ajax: "/gateOutOrder/orderList",
            columns:[
                { "width": "30px",
                    "render": function ( data, type, full, meta ) {
                      return '<button type="button" class="btn table_btn delete_btn btn-xs" style="display:none">'+
                        '<i class="fa fa-trash-o"></i> 删除</button>'+
                        '<button type="button" class="btn btn-primary btn-xs print">'+
                        '<em class="icon-print"></em> 打印</button>';
                    }
                },
                { "data": "KT_NO"}, 
                {"data": "ORDER_NO", 
                	"render": function ( data, type, full, meta ) {
              	    	if(!data)
              	    		data = '';
              	    	return "<a class='item_detail' order_id='"+full.ID+"' data-target='#itemDetail' data-toggle='modal'>"+data+"</a>";
              	    }
                },
				{ "data": "ITEM_NO"}, 
				{ "data": "TOTAL_QUANTITY"}, 
				{ "data": "QUANTITY"}, 
				{ "data": "CREATE_TIME"},
				{ "data": "CREATOR_NAME"}
            ]
        });
        
        
        var actualTable = eeda.dt({
            id: 'actual_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/gateOutOrder/actualList",
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
        	
        	$.post('/gateOutOrder/printDetailPDF',{order_id:order_id},function(data){
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
        	$("#orderForm")[0].reset();
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
	      	}
	        return item;
        };
      
        var searchData=function(){
        	$.blockUI({ 
                message: '<h1><img src="/images/loading.gif" style="height: 50px; margin-top: -3px;"/> LOADING...</h1>' 
            });
        	var itemJson = buildCondition();
        	var url = "/gateOutOrder/list?error_flag=N&jsonStr="+JSON.stringify(itemJson);
        	dataTable.ajax.url(url).load();
        };
        
        order.refleshTable = function(){
        	$.blockUI({ 
                message: '<h1><img src="/images/loading.gif" style="height: 50px; margin-top: -3px;"/> LOADING...</h1>' 
            });
        	orderTable.ajax.url("/gateOutOrder/orderList").load();
        }
        
        $('#totalQuantity').on('input',function(){
        	$('#quantity').val(this.value);
        });
        
        $('#kt_no').on('input',function(){
        	var kt_no = $('#kt_no').val();
        	if(kt_no.trim()=="")
        		return;
        	
        	$.post('/gateOutOrder/searchKT',{kt_no:kt_no},function(data){
        		if(data){
        			$('#item_no').val(data.ITEM_NO);
        			$('#item_no_input').val(data.ITEM_NO);
        			$('#totalQuantity').val(data.TOTAL_QUANTITY);
        			$('#haveQuantity').val(data.QUANTITY);
        		}
        	}).fail(function() {
                $.scojs_message('后台报错', $.scojs_message.TYPE_ERROR);
            });
        });
	});
});