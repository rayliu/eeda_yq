define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','pageguide'], function ($, metisMenu) {
	$(document).ready(function() {
		tl.pg.init({
	        pg_caption: '本页教程'
	    });
		$('#AllCheck').attr('disabled',true);
		var dataTable = eeda.dt({
			id: 'eeda_table',
			autoWidth: false,
			//scrollY: 530,
			//scrollCollapse: true,
			//paging: true,
			initComplete: function (settings) {
				eeda.dt_float_header('eeda_table');
			},
			serverSide: false, //不打开会出现排序不对 
			ajax: '/outputScale/list?export_flag='+$("#export_flag").val(),
			columns:[
			         	{	"width": "10px",
			         		"render": function ( data, type, full, meta ) {
			         			if(full.EXPORT_FLAG != 'Y')
			         				return '<input type="checkbox" class="checkBox" name="check_box" disabled>';
		         				else 
		         					return '<input type="checkbox" name="check_box" disabled>';
			         		}
			         	},
		         		{	"data": "ORDER_NO", "width": "80px",
			         		"render": function ( data, type, full, meta ) {
			         			return "<a href='/transJobOrder/edit?id="+full.TJOID+"'target='_blank'>"+data+"</a>";
			         		}
		         		},
		         		{ "data": "LADING_NO", "width": "60px"},			
		         		{ "data": "C_DATE", "width": "80px"},
		         		{ "data": "CHARGE_TIME", "width": "120px"},
		         		{ "data": "CUSTOMER_NAME", "width": "80px"},
		         		{ "data": "TYPE", "width": "60px"},
		         		{ "data": "EXPORT_FLAG", "width": "60px",
		         			"render": function ( data, type, full, meta ) {
		         				if(data != 'Y')
		         					return '未导出';
		         				else 
		         					return '已导出';
		         			}
		         		},
		         		{ "data": "COMBINE_WHARF", "width": "150px"},
		         		{ "data": "CONTAINER_NO", "width": "80px"},
		         		{ "data": "CABINET_TYPE", "width": "40px"},
		         		{ "data": "COMBINE_UNLOAD_TYPE", "width": "80px"},
		         		{ "data": "COMBINE_CAR_NO", "width": "70px"},
		         		{ "data": "OUTPUTSCALE","width": "40px","class":"outputScale",
		         			"render":function(data,type,full,meta){
		         				var cabinet_type= full.COMBINE_UNLOAD_TYPE;
		         				if(cabinet_type=="全程"){
		         					if(data){
		         						return '<input type="text" class="output_scale" style="width:60px" value = "'+eeda.numFormat(parseFloat(data).toFixed(2),3)+'">'+eeda.numFormat(parseFloat(data).toFixed(2),3);
		         					}
		         					return '<input type="text" class="output_scale" style="width:60px" value = "'+eeda.numFormat(parseFloat(full.FREIGHT).toFixed(2),3)+'">'+eeda.numFormat(parseFloat(full.FREIGHT).toFixed(2),3);
		         				}else{
		         					if(data){
		         						return '<input type="text" class="output_scale" style="width:60px" value = "'+eeda.numFormat(parseFloat(data).toFixed(2),3)+'">'+eeda.numFormat(parseFloat(data).toFixed(2),3);
		         					}
		         					return '<input type="text" class="output_scale" value = "'+eeda.numFormat(parseFloat((full.FREIGHT/2)).toFixed(2),3)+'" style="width:60px">'+eeda.numFormat(parseFloat((full.FREIGHT/2)).toFixed(2),3);
		         				}
		         			}
		         		},
		         		{ "data": "FREIGHT","width":"60px",
		         			"render": function ( data, type, full, meta ) {
		         				if(data)
		         					return eeda.numFormat(parseFloat(data).toFixed(2),3)
		         				else
		         					return '';
		         			}
		         		},
		         		{ "data": "NIGHT_FEE","width":"60px",
		         			"render": function ( data, type, full, meta ) {
		         				if(data)
		         					return eeda.numFormat(parseFloat(data).toFixed(2),3)
		         				else
		         					return '';
		         			}
		         		},
		         		{ "data": "REMARK", "width": "200px"},
		         		{ "data": "CREATE_STAMP","width": "80px"}
		         	]
		});

		$('#resetBtn').click(function(e){
			$("#orderForm")[0].reset();
		});
  
		$('#searchBtn').click(function(){
			searchData();
			var driver = $("#driver").val().trim();
			var car_no = $("#car_id_input").val().trim();
			var export_flag=$("#export_flag").val();
			if((driver == null || driver == "") && (car_no == null || car_no == "") || (export_flag == "Y")){
				$('#AllCheck').attr('disabled',true);
				$('#eeda_table input[name="check_box"]').each(function(){
					$(this).attr('disabled',true);
				});
				return;
			}else{
				allCheck();
				click_checkbox();
			}
		})

		var searchData=function(){
			var order_no = $.trim($("#order_no").val()); 
			var driver = $("#driver").val().trim();
			var export_flag=$("#export_flag").val();
			var c_date_begin_time = $("#c_date_begin_time").val();
			var c_date_end_time = $("#c_date_end_time").val();
			var customer = $("#customer").val();
			//var customer_name = $("#customer_input").val().trim(); 
			//var sp = $("#sp").val(); 
			var car_id = $("#car_id").val();
			var car_no = $("#car_id_input").val().trim();
			var start_date = $("#charge_time_begin_time").val();
			var end_date = $("#charge_time_end_time").val();
			var audit_flag = $("#audit_flag").val();
			/*  
     			查询规则：参数对应DB字段名
			 	*_no like
			 	*_id =
			 	*_status =
      			时间字段需成双定义  *_begin_time *_end_time   between
		 	*/
			var url = "/outputScale/list?order_no="+order_no
			+"&c_date_begin_time="+c_date_begin_time
			+"&c_date_end_time="+c_date_end_time
			+"&customer_id="+customer
			//+"&customer_name="+customer_name
			//+"&sp_id="+sp
			+"&car_id="+car_id
			+"&car_no="+car_no
			+"&driver_equals="+driver
			+"&export_flag="+export_flag
			+"&charge_time_begin_time="+start_date
			+"&charge_time_end_time="+end_date;

			dataTable.ajax.url(url).load(enable_checkbox);
		};
  
		//全选 
		var allCheck = function(){
			$('#AllCheck').attr('disabled',false);
			$('#AllCheck').click(function(){
				$('input[name="check_box"]').prop("checked",this.checked);
				if($('#AllCheck').prop('checked')){
					$('#export_outputTable').attr('disabled',false);
					$('#saveBtn').attr('disabled',false);
				}else{
					$('#export_outputTable').attr('disabled',true);
					$('#saveBtn').attr('disabled',true);
				}
			});
		}
		$("#eeda_table").on('click','.checkBox',function(){
			$("#AllCheck").prop("checked",$("input[name='check_box']").length == $("input[name='check_box']:checked").length ? true : false);
		});

		//checkbox选中则button可点击
		var click_checkbox = function(){
			$('#eeda_table').on('click','input[name="check_box"]',function(){
				var hava_check = 0;
				$('#eeda_table input[name="check_box"]').each(function(){	
					var checkbox = $(this).prop('checked');
					if(checkbox){
						hava_check = 1;
					}	
				})
				if(hava_check>0){
					$('#export_outputTable').attr('disabled',false);
					$('#saveBtn').attr('disabled',false);
				}else{
					$('#export_outputTable').attr('disabled',true);
					$('#saveBtn').attr('disabled',true);
				}
			});
		}
 
		//循环遍历table启用checkbox
		var enable_checkbox = function(){
			var driver = $("#driver").val().trim();
			var car_no = $("#car_id_input").val().trim();
			var export_flag = $("#export_flag").val();
			if((driver == null || driver == "") && (car_no == null || car_no == "") || (export_flag == "Y")){
				$('#export_outputTable').attr('disabled',true);
				$('#saveBtn').attr('disabled',true);
				$('#AllCheck').attr('disabled',true);
				$('#AllCheck').attr('checked',false)
				return;
			}else{	   
				$('#export_outputTable').attr('disabled',false);
				$('#saveBtn').attr('disabled',false);
				$('#AllCheck').prop('checked',true);
				$('#eeda_table input[name="check_box"]').each(function(){
					$(this).attr('disabled',false);
					$(this).attr('checked',true);
				});
			}
		}
 
		$("#saveBtn").click(function(){
			$('#saveBtn').attr('disabled',true);
			var car_no = $('#car_id_input').val().trim();
			var car_id = $('#car_id').val();
			var driver = $('#driver').val().trim();
			var order = {};
			var itemjson = [];
			var itemIds=[];
			$('#eeda_table input[name="check_box"]').each(function(){
				var checkbox = $(this).prop('checked');
				if(checkbox){
					var itemTr = $(this).parent().parent();
					var itemId = itemTr.attr('id');
					var outputScale = $(this).parent().parent().find('.outputScale input').val();
					if(itemId!=undefined){
						var item={};
						itemIds.push(itemId);
						item.id=itemId;
						item.outputScale = outputScale;
						item.car_id = car_id;
						itemjson.push(item);
					}
		   
				}
			});
			order.param = itemjson;
			$.post('/outputScale/downloadList?itemIds='+itemIds,{params:JSON.stringify(order),car_no:car_no,driver:driver},function(data){
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				refresh();
				$('#AllCheck').attr('disabled',true);
				$('#export_outputTable').attr('disabled',true);
			});
		});
		
		//导出产值表
		$('#export_outputTable').click(function(){
			$('#export_outputTable').attr('disabled',true);
			var car_no = $('#car_id_input').val().trim();
			var car_id = $('#car_id').val();
			var driver = $('#driver').val().trim();
			var sign = "导出";
			var order = {};
			var itemjson = [];
			var itemIds=[];
			$('#eeda_table input[name="check_box"]').each(function(){
				var checkbox = $(this).prop('checked');
				if(checkbox){
					var itemTr = $(this).parent().parent();
					var itemId = itemTr.attr('id');
					var outputScale = $(this).parent().parent().find('.outputScale input').val();
					outputScale = outputScale.replace(/,/g,'');
					if(itemId!=undefined){
						var item={};
						itemIds.push(itemId);
						item.id=itemId;
						item.outputScale = outputScale;
						item.car_id = car_id;
						itemjson.push(item);
					}
		   
				}
			});
			order.param = itemjson;
			if(car_no||driver){
				if(car_no||driver||itemIds){
					var order_id = $('#order_id').val();
					var company_name = $('#company_name').val();
					$.post('/outputScale/downloadList?itemIds='+itemIds,{params:JSON.stringify(order),car_no:car_no,driver:driver,sign:sign},function(data){
						if(data){
							window.open(data);
							$.scojs_message('生成产值表PDF成功', $.scojs_message.TYPE_OK);
							refresh();
							$('#AllCheck').attr('disabled',true);
							$('#saveBtn').attr('disabled',true);
						}else{
							$.scojs_message('生成产值表PDF失败',$.scojs_message.TYPE_ERROR);
						}
					});
				}else{
					$.scojs_message('请填上车牌或者司机', $.scojs_message.TYPE_ERROR);
				}
			}else{
				$.scojs_message('结算车牌或者司机不能为空', $.scojs_message.TYPE_ERROR);
				$('#export_outputTable').attr('disabled',false);
	    		return;
			}
		});

		//刷新明细表
		var refresh = function(order_id){
			var url = "/outputScale/list?export_flag="+$('#export_flag').val();
			dataTable.ajax.url(url).load(function(){
			});
		}
	});
});