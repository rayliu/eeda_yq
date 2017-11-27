define(['jquery', 'metisMenu', 'sb_admin','template','dataTablesBootstrap','validate_cn', 'sco', 'pageguide'], function ($, metisMenu, sb, template,createStep1Contr, selectContr) {
	$(document).ready(function() {
		var order_id = $("#order_id").val();
		var itemTable = eeda.dt({
		    id: 'item_table',
		    autoWidth: true,
		    ajax:"/invoiceApply/itemList?order_ids="+order_id,
		    columns: [
					{ "width":"110px",
					    "render": function(data, type, full, meta) {
					    	var status = $("#status").val();
					    	if(status=='已开票'||status=='已提交'){
					    		return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:40px" disabled>删除</button>'
					    		
					    	}else{
					    		return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:40px">删除</button>'
					    	}
					    }
					},
					{"data":"CHECK_ORDER_NO","width":"120px"},
					{ "data": "ORDER_NO","width":"90px",
	                	"render": function ( data, type, full, meta ) {
			           		  return "<a href='/jobOrder/edit?id="+full.JOID+"'>"+data+"</a>";
			           	  }
	                },
					{"data":"SP_NAME","width":"120px"},
					{"data":"FIN_NAME","width":"120px"},
					{"data":"CURRENCY_NAME","width":"120px"},
					{"data":"EXCHANGE_TOTAL_AMOUNT","width":"120px"}
	        ]      
	    });
		
		//刷新对账单明细列表
		var itemTableRefresh = function(order_id){
			var url = "/invoiceApply/itemList?order_ids="+order_id;
			itemTable.ajax.url(url).load(false);
		}

		var eedaChargeTable = eeda.dt({
		    id: 'eeda_charge_table',
		    autoWidth: false,
		    //ajax: "/chargeRequest/OrderList?pageBoolean="+pageBoolean,
		    columns: [
					{"width":"30px",
					    "render": function(data, type, full, meta) {
					    	if(full.GREATE_FLAG=='Y'){
					    		return '<input type="checkbox" class="checkBox" name="order_check_box" disabled>';
					    	}else{
					    		return '<input type="checkbox" name="order_check_box" class="checkBox" >';
					    	}
					    }
					},
					{"data":"CHECK_ORDER_NO","width":"120px"},
					{ "data": "ORDER_NO","width":"90px",
	                	"render": function ( data, type, full, meta ) {
			           		  return "<a href='/jobOrder/edit?id="+full.ID+"'>"+data+"</a>";
			           	  }
	                },
					{"data":"SP_NAME","width":"120px"},
					{"data":"FIN_NAME","width":"120px"},
					{"data":"CURRENCY_NAME","width":"120px"},
					{"data":"PRICE","width":"120px"}
	        ]      
	    });
		
		//对账单明细-添加明细按钮动作
		$('#add_item').click(function(){
            $('#item_table_msg_btn').click();
            $("#que_sp_input").val($("#party_id_input").val());
            $("#allCharge").prop("checked",false);
            
            var party_id = $("#party_id").val();
            var charge_id = $("#charge_id").val();
            
            var url = "/invoiceApply/addItemList?joa.sp_id="+party_id;
            eedaChargeTable.ajax.url(url).load();
		});

		//对账单明细列表，删除按钮动作
		$("#item_table").on("click",".delete",function(){
			var id=$(this).parent().parent().attr('id');
	        var tr = $(this).parent().parent();
				
	        $.post('/invoiceApply/deleteChargeItem', {id:id},function(data){
	        	itemTable.row(tr).remove().draw();
	        });
		});
		
		//添加明细里的-全选按钮
		$("#allCharge").click(function(){
			$("#eeda_charge_table input[name='order_check_box']").prop("checked",$(this).prop("checked"));
		});
		
		//添加明细里的添加按钮
		$("#add_charge_item").click(function(){
			var charge_itemlist=[];
			var order_id = $("#order_id").val();
			$("#eeda_charge_table input[name=order_check_box]:checked").each(function(){
				var id = $(this).parent().parent().attr("id");
				charge_itemlist.push(id);
			});
			$.post("/invoiceApply/addChargeItem",{charge_itemlist:charge_itemlist.toString(),order_id:order_id},function(data){
				$.scojs_message('添加成功', $.scojs_message.TYPE_OK);
				itemTableRefresh(order_id);
			},"json").fail(function() {
	            $.scojs_message('添加失败', $.scojs_message.TYPE_ERROR);
	        });
		});
		
		//添加明细里的-查询按钮
		$("#searchBtn").click(function(){
			var party_id = $("#party_id").val();
            var charge_id = $("#charge_id").val();
            
            var url = "/invoiceApply/addItemList?joa.sp_id="+party_id
            		+"&charge_id="+charge_id;
            eedaChargeTable.ajax.url(url).load();
		});
		
	});
});