define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'jq_blockui', 'dtColReorder'], function ($, metisMenu, template) { 
	$(document).ready(function() {

	    var deletedTableIds=[];

	    //删除一行
	    $("#cargo_table").on('click', '.delete', function(e){
	        e.preventDefault();
	        var tr = $(this).parent().parent();
	        deletedTableIds.push(tr.attr('id'));
	        
	        cargoTable.row(tr).remove().draw();
	    }); 
	    
	    lclOrder.buildCargoDetail=function(){
	    	var cargo_table_rows = $("#cargo_table tr");
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
	    
	    var bindFieldEvent=function(){
	    	$('table .date').datetimepicker({  
	    	    format: 'yyyy-MM-dd hh:mm:ss',  
	    	    language: 'zh-CN'
	    	}).on('changeDate', function(el){
	    	    $(".bootstrap-datetimepicker-widget").hide();   
	    	    $(el).trigger('keyup');
	    	});

	    	eeda.bindTableField('cargo_table','UNIT_ID','/serviceProvider/searchUnit','');
	    	eeda.bindTableField('cargo_table','POR','/location/searchPort','port');
	    	eeda.bindTableField('cargo_table','POL','/location/searchPort','port');
	    	eeda.bindTableField('cargo_table','POD','/location/searchPort','port');
	    	eeda.bindTableField('cargo_table','CARRIER','/serviceProvider/searchCarrier','');
	    };
	    


	    //------------事件处理
	    var cargoTable = eeda.dt({
            id: 'cargo_table',
            autoWidth: false,
            "drawCallback": function( settings ) {
		        bindFieldEvent();
		        $.unblockUI();
		    },
            columns:[
	            {"width": "10px",
	                "render": function ( data, type, full, meta ) {
	                		return '<button type="button" class="delete btn table_btn btn-default btn-xs" >删除</button> ';
	                }
	                
	            },
	            { "data": "ORDER_NO","width": "80px"}, 
	            { "data": "CUSTOMER_NAME","width": "120px"},
	            { "data": "ORDER_TYPE","width": "60px"},
	            { "data": "ORDER_EXPORT_DATE" ,"width": "60px"},
	            { "data": "POL" ,"width": "80px"},
	            { "data": "POD","width": "80px"},
	            { "data": "PIECES" ,"width": "30px"},
	            { "data": "GROSS_WEIGHT","width": "50px"},
	            { "data": "VOLUME","width": "60px"},
	            { "data": "CARGO_NAME","width": "100px"},
	            { "data": "CARRIER","width": "60px"},
	            { "data": "VESSEL","width": "80px"},
	            { "data": "VOYAGE" ,"width": "60px"},
	            { "data": "ETA","width": "60px"},
	            { "data": "ETD","width": "60px"},
	            { "data": "SO_NUMBER","width": "100px"},
	            { "data": "NET_WEIGHT","width": "50px"},
	            { "data": "VGM","width": "60px"},
	            { "data": "POR" ,"width": "60px"}
	        ]
	    });
	    

	    
	
//	    
//	    $('#allShipmentBtn').on('click',function(){
//	    	var self = this;
//	    	
//	    	var itemIdArray = [];
//	    	$('#cargo_table .checkBox:checked').each(function(){
//	    		  var id  = $(this).parent().parent().attr('id');
//	    		  itemIdArray.push(id);
//	    	});
//	    	
//	    	if(itemIdArray.length == 0){
//	    		$.scojs_message('请勾选要出货的明细', $.scojs_message.TYPE_ERROR);
//	    		return;
//	    	}
//
//	    	$.blockUI({ 
//                message: '<h4><img src="/images/loading.gif" style="height: 20px; margin-top: -3px;"/> 正在提交...</h4>' 
//            });
//	    	$.post('/planOrder/confirmShipment',{item_id:itemIdArray.toString()},function(data){
//    		    if(data.result){
//    			    $.scojs_message('确认成功', $.scojs_message.TYPE_OK);
//    			    //异步刷新明细表
//                    salesOrder.refleshTable($('#order_id').val());
//    		    }else{
//    			    $.scojs_message('确认失败', $.scojs_message.TYPE_ERROR);
//    			    self.disabled = false;
//    		    }
//    		    $.unblockUI();
//    	    }).fail(function() {
//    		    $.unblockUI();
//    		    self.disabled = false;
//                $.scojs_message('后台出错', $.scojs_message.TYPE_ERROR);
//            });
//	    	
//	    });
//	    
	    

//	    $('#add_cargo').on('click', function(){
//	        var item={};
//	        cargoTable.row.add(item).draw(true);
//	    });
//	    
//	    //获取箱量的值
//	    var self_containerType;
//	    
//	    $('#cargo_table').on('click','[name=CONTAINER_TYPE]',function(){
//	    	self_containerType = $(this);
//	    	$('#containerTypeShow_btn').click();
//	    });
//	    
//	    $('#containerType_btnConfirm').click(function(){
//	    	var transport_type = [];
//	    	$('#containerTypeTab li input[type="checkbox"]:checked').each(function(){
//	    		var containerVal=$(this).val()+'X'+$(this).parent().find('.container_amount').val()
//	        	transport_type.push(containerVal); 
//	        });
//	        var transport_type_str = transport_type.toString();
//	        self_containerType.val('');
//	        self_containerType.val(transport_type_str);
//		})
//		
//		//获取车型车量
//	    var self_truckType;
//	    
//	    $('#cargo_table').on('click','[name=TRUCK_TYPE]',function(){
//	    	self_truckType = $(this);
//	    	$('#truckTypeShow_btn').click();
//	    });
//	    
//	    $('#truckType_btnConfirm').click(function(){
//	    	var transport_type = [];
//	    	$('#truckTypeTab li input[type="checkbox"]:checked').each(function(){
//	    		var containerVal=$(this).val()+'X'+$(this).parent().find('.container_amount').val()
//	        	transport_type.push(containerVal); 
//	        });
//	        var transport_type_str = transport_type.toString();
//	        self_truckType.val('');
//	        self_truckType.val(transport_type_str);
//		})
//	    
//	    
//	    
	    
	    //刷新明细表
	    lclOrder.refleshTable = function(order_id){
	    	var url = "/planOrder/tableList?order_id="+order_id;
	    	cargoTable.ajax.url(url).load();
	    }
	    
//	    //checkbox选中则button可点击
//		$('#cargo_table').on('click','.checkBox',function(){
//			
//			var hava_check = 0;
//			$('#cargo_table input[type="checkbox"]').each(function(){	
//				var checkbox = $(this).prop('checked');
//	    		if(checkbox){
//	    			hava_check=1;
//	    		}	
//			})
//			if(hava_check>0){
//				$('#create_jobOrder').attr('disabled',false);
//			}else{
//				$('#create_jobOrder').attr('disabled',true);
//			}
//		});
	});
});
