define(['jquery', 'metisMenu', 'sb_admin','./order_item_table','dataTablesBootstrap','validate_cn', 'sco'], function ($, metisMenu, sb, selectContr) {
	$(document).ready(function() {
		var billIds=[];
		var dataTable = eeda.dt({
		    id: 'request_item_table',
		    autoWidth: false,
		    //serverSide: true, //不打开会出现排序不对 
		    ajax: "/invoiceApply/requestList",
		    columns: [
					{ "width":"50px",
					    "render": function(data, type, full, meta) {
					    	if(full.GREATE_FLAG=='Y'){
					    		return '<input type="checkbox" class="checkBox" name="order_check_box" disabled>';
					    	}else{
					    		return '<input type="checkbox" name="order_check_box" class="checkBox" >';
					    	}
					    }
					},
					{"data":"ORDER_NO","width":"120px",
						 "render": function(data, type, full, meta) {
							 return "<a href='/chargeCheckOrder/edit?id="+full.ID+"'>"+data+"</a>"
						 }
					},
					{"data":"PARTY_NAME","class":"PARTY_NAME","width":"120px"},
					{"data":"STATUS","width":"120px"},
					{"data":"CNY","width":"120px",
						"render":function(data,type,full,meta){
							if(data==''){
								data=0.00;
							}
							return parseFloat(data).toFixed(2);
						}
					},
					{"data":"USD","width":"120px",
						"render":function(data,type,full,meta){
							if(data==''){
								data=0.00;
							}
							return parseFloat(data).toFixed(2);
						}
					},
					{"data":"JPY","width":"120px",
						"render":function(data,type,full,meta){
							if(data==''){
								data=0.00;
							}
							return parseFloat(data).toFixed(2);
						}
					},
					{"data":"HKD","width":"120px",
						"render":function(data,type,full,meta){
							if(data==''){
								data=0.00;
							}
							return parseFloat(data).toFixed(2);
						}
					}
	        ]      
	    });

		//查询按钮单击事件
		$("#searchBtn").click(function(){
			var sp_id = $("#sp").val();
			var begin_time = $("#biz_period1_begin_time").val();
			var end_time = $("#biz_period1_end_time").val();
			
			var url = "/invoiceApply/requestList?sp_id="+sp_id
       	   	+"&begin_time_begin="+begin_time
            +"&end_time_end="+end_time;
			
			 dataTable.ajax.url(url).load();
		});
		
		//选择是否是同一个付款对象
		$('#request_item_table').on('click',"input[name='order_check_box']",function () {
				var cname = $(this).parent().siblings('.PARTY_NAME')[0].textContent;
				if($(this).prop('checked')==true){	
					if(cnames.length > 0 ){
						if(cnames[0]==cname){
								cnames.push(cname);
								if($(this).val() != ''){
									billIds.push($(this).val());
								}
						}else{
							$.scojs_message('请选择同一个付款对象', $.scojs_message.TYPE_ERROR);
							$(this).attr('checked',false);
							return false;
						}
					}else{
						cnames.push(cname);
						if($(this).val() != ''){
							billIds.push($(this).val());
						}
					}

				}else{
					billIds.splice($.inArray($(this).val(), billIds), 1);
					cnames.pop(cname);
			 }
				
		 });
		
		$('#request_item_table').on('click', 'input[type="checkbox"]',function(){
			var idsArray=[];
			var sp_name='';
			var sp_id='';
			var rowindex = $(this).parent().parent().index();
			if($(this).prop('checked')){
				 sp_id = dataTable.row(rowindex).data().SP_ID.toString();
				 sp_name = dataTable.row(rowindex).data().PARTY_NAME.toString();
				 begin_time = dataTable.row(rowindex).data().BEGIN_TIME.toString();
				 end_time = dataTable.row(rowindex).data().END_TIME.toString();
			}
	      	$('#request_item_table input[type="checkbox"]:checked').each(function(){
	      			var itemId = $(this).parent().parent().attr('id');
	      			if(itemId){
	      				idsArray.push(itemId);
	      			}
	      	});
	      	if(idsArray==''){
	      		 $('#party_id_input').val('');
	      		 $('#party_id').val('');
	      		 $('#createSave').attr('disabled',true);
	      		 billIds=[];
	      		 cnames=[];
	  		     selectContr.refleshSelectTable(idsArray);
	  		     return;
	      	}
	      	$('#party_id_input').val(sp_name);
	      	$('#party_id').val(sp_id);
	      	$('#biz_period_begin_time').val(begin_time.substring(0,10));
	     	 $('#biz_period_end_time').val(end_time.substring(0,10));
	  		$('#ids').val(idsArray);
	  		selectContr.refleshSelectTable(idsArray);
	  		$("#allCheck1").prop("checked",$("#request_item_table tr:has(td) input[type=checkbox]").length == $("#request_item_table tr:has(td) input[type=checkbox]:checked").length ? true : false);
		})
		
		//checkbox选中则button可点击
		$('#request_item_table').on('click',"input[name='order_check_box']",function () {
			if(billIds.length>0){
				$('#createSave').attr('disabled',false);
			}else{
				$('#createSave').attr('disabled',true);
			}
		});
		
		var refleshSelectTable = function(){
		    var url = "/chargeRequest/OrderList";
		    costAccept_table.ajax.url(url).load();
	    }
		
		return {
			refleshRequestItemTable: refleshSelectTable
		};
	});
});