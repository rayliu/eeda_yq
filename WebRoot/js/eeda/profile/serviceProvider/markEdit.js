define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco',  'dtColReorder','./mark_doc_table'], function ($, metisMenu) { 
	$(document).ready(function() {
		$("#saveBtn").click(function(){
			if($("#sp_id_input").val()==""){
				$.scojs_message('供应商为必填', $.scojs_message.TYPE_ERROR);
				return;
			}
			var order = {};
			order.id = $('#order_id').val();
			order.sp_id = $("#sp_id").val();
			order.audit_suggestion = $("#audit_suggestion").val();
			order.score_type = $("#plus_score").prop("checked")==true?'plus':'reduce';
			order.event_time = $("#event_time").val();
			order.item_name = $("#item_name").val();
			order.score = $("#score").val();
			order.participant = $("#participant").val();
			order.about = $("#about").val();
			order.review = $("#review").val();
			order.improvement = $("#improvement").val();
			
			order.doc_list = itemOrder.buildDocDetail;
			$.post("/supplierRating/save",{params:JSON.stringify(order)},function(data){
				if(data){
					$("#order_no").val(data.ORDER_NO);
					$("#order_id").val(data.ID);
					$("#status").val(data.STATUS);
					$("#creator").val(data.CREATOR_NAME);
					$("#create_stamp").val(data.CREATE_STAMP);
					eeda.contactUrl("edit?id",data.ID);
					$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
					$("#submitBtn").attr("disabled",false);
					$("#fileuploadSpan").show();
				}else{
					$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
				}
			});
		});
		$("#submitBtn").click(function(){
			$.post("/supplierRating/submitMethod",{id:$("#order_id").val()},function(data){
				if(data){
					$("#status").val(data.STATUS);
 					$.scojs_message('提交成功', $.scojs_message.TYPE_OK);
 					$("#saveBtn").attr("disabled",true);
 					$("#submitBtn").attr("disabled",true);
 					$("#fileupload").attr("disabled",true); 
 					$(".delete").attr("disabled",true);
 					$("#checkBtn").attr("disabled",false);
 					$("#notPassCheckBtn").attr("disabled",false);
 					$("#fileuploadSpan").hide();
				}else{
					$.scojs_message('提交失败', $.scojs_message.TYPE_ERROR);
				}
			})
		});
		$("#checkBtn").click(function(){
			var type = "check";
			/*var score_type = $("#plus_score").prop("checked")==true?'plus':'reduce';
			var total_score = $("#total_score").val();
			var score = $("#score").val();
			if(score_type=='plus'){
				total_score = (total_score*1)+(score*1);
			}else if(score_type=='reduce'){
				total_score = (total_score*1)-(score*1);
			}*/
			$.post("/supplierRating/checkMethod",{id:$("#order_id").val(),type:type},function(data){
				if(data){
					$("#status").val(data.STATUS);
					$("#total_score").val(data.SUM_SCORES);
					$("#auditor").val(data.AUDITOR_NAME);
					$("#audit_stamp").val(data.AUDIT_STAMP);
 					$.scojs_message('审核成功', $.scojs_message.TYPE_OK);
 					$("#saveBtn").attr("disabled",true);
 					$("#submitBtn").attr("disabled",true);
 					$("#fileupload").attr("disabled",true);
 					$(".delete").attr("disabled",true);
 					$("#checkBtn").attr("disabled",true);
 					$("#notPassCheckBtn").attr("disabled",true);
				}else{
					$.scojs_message('审核失败', $.scojs_message.TYPE_ERROR);
					$("#saveBtn").attr("disabled",true);
					$("#submitBtn").attr("disabled",true);
					$("#fileupload").attr("disabled",true);
					$(".delete").attr("disabled",true);
					$("#checkBtn").attr("disabled",true);
					$("#notPassCheckBtn").attr("disabled",true);
				}
			})
		});
		$("#notPassCheckBtn").click(function(){
			var type = "notPassCheck";
			var total_score = $("#total_score").val();
			$.post("/supplierRating/checkMethod",{id:$("#order_id").val(),type:type},function(data){
				if(data){
					$("#status").val(data.STATUS);
					$("#auditor").val(data.AUDITOR_NAME);
					$("#audit_stamp").val(data.AUDIT_STAMP);
					$("#total_score").val(total_score);
 					$.scojs_message('审核成功', $.scojs_message.TYPE_OK);
 					$("#saveBtn").attr("disabled",true);
 					$("#submitBtn").attr("disabled",true);
 					$("#fileupload").attr("disabled",true);
 					$(".delete").attr("disabled",true);
 					$("#checkBtn").attr("disabled",true);
 					$("#notPassCheckBtn").attr("disabled",true);
				}else{
					$.scojs_message('审核失败', $.scojs_message.TYPE_ERROR);
				}
			})
		});
		//radio控制
		$(".row_show").hide();
		if($("#plus_score").prop("checked")==true){
			$(".row_show").hide();
		}else{
			$(".row_show").show();
		}
		$("#plus_score").click(function(){
			if($("#reduce_score").prop("checked")==true){
				$("#reduce_score").prop("checked",false);
				$(".row_show").hide();
			}
			
		});
		$("#reduce_score").click(function(){
			if($("#plus_score").prop("checked")==true){
				$("#plus_score").prop("checked",false);
				$(".row_show").show();
			}
		});
		//按钮控制
		var order_id = $("#order_id").val();
		if(order_id==""){
			$("#submitBtn").attr("disabled",true);
		}else{
			var status = $("#status").val();
			if(status=="待审核"){
				$("#saveBtn").attr("disabled",true);
				$("#submitBtn").attr("disabled",true);
				$("#fileupload").attr("disabled",true);
				$(".delete").attr("disabled",true);
				$("#checkBtn").attr("disabled",false);
				$("#notPassCheckBtn").attr("disabled",false);
				$("#fileuploadSpan").hide();
			}else if(status=="已审核"){
				$("#saveBtn").attr("disabled",true);
				$("#submitBtn").attr("disabled",true);
				$("#fileupload").attr("disabled",true);
				$(".delete").attr("disabled",true);
				$("#checkBtn").attr("disabled",true);
				$("#notPassCheckBtn").attr("disabled",true);
				$("#fileuploadSpan").hide();
			}else if(status=="审核不通过"){
				$("#saveBtn").attr("disabled",true);
				$("#submitBtn").attr("disabled",true);
				$("#fileupload").attr("disabled",true);
				$(".delete").attr("disabled",true);
				$("#checkBtn").attr("disabled",true);
				$("#notPassCheckBtn").attr("disabled",true);
				$("#fileuploadSpan").hide();
			}
		}
	})
})