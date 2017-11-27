define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco',  'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {
      var dataTable = eeda.dt({
          id: 'eeda_table',
          colReorder: true,
          paging: false,
          serverSide: false, //不打开会出现排序不对 
          ajax: "/invoiceApply/list",
          columns: [
          			{ "width": "30px"},
    	            { "data": "ORDER_NO", "width": "100px",
    	            	"render": function(data, type, full, meta) {
    	            		return "<a href='/invoiceApply/edit?id="+full.ID+"'>"+data+"</a>"
    	            	}
    	            },
    	            { "data": "PARTY_NAME", "width": "100px"},
    	            { "data": "INVOICE_NO", "width": "100px"},
    	            { "data": "STATUS", "width": "100px",
    	            	"render": function(data, type, full, meta) {
    	            		if(data=='复核不通过'){
    	            			return "<span style='color:red;'>"+data+"</span>"
    	            		}
    	            		return data;
    	            	}
    	            },
    	            { "data": "INVOICE_NO", "width": "100px",
    	            	"render": function ( data, type, full, meta ) {
    	                    var str="<nobr>";
    	                    if(full.STATUS=="开票中"){
    	                    	str+= '<button type="button" class="submitBtn btn table_btn btn_green btn-xs" style="width:60px">提交</button> ';
    	                        str+= '<button type="button" disabled class="checkBtn btn table_btn btn_green btn-xs " style="width:60px" >复核</button>&nbsp';
    	                    }else if(full.STATUS=="已提交"){
    	                    	str+= '<button type="button" disabled class="submitBtn btn table_btn btn_green btn-xs" style="width:60px">提交</button> ';
    	                        str+= '<button type="button" class="checkBtn btn table_btn btn_green btn-xs" style="width:60px" >复核</button>&nbsp';
    	                    }else if(full.STATUS=="已开票"){
    	                    	str+= '<button type="button" disabled class="submitBtn btn table_btn btn_green btn-xs" style="width:60px">提交</button> ';
    	                        str+= '<button type="button" disabled class="checkBtn btn table_btn btn_green btn-xs" style="width:60px" >复核</button>&nbsp';
    	                     }
    	                    str +="</nobr>";
    	                    return str;
    	                }
    	            },
    	            { "data": "CREATOR_NAME", "width": "100px"},
    	            { "data": "CREATE_TIME", "width": "100px"}
    	          ]
    	      });
      
      $('.complex_search').click(function(event) {
          if($('.search_single').is(':visible')){
            $('.search_single').hide();
          }else{
            $('.search_single').show();
          }
      });
    //简单查询
      $('#selected_field').change(function(event) {
	      var selectField = $('#selected_field').val();
	      if(selectField=='sp_id'){
	    	  $("#single_order_export_date_begin_time").val("");
	    	  $("#single_order_export_date_end_time").val("");
	    	  $("#order_export_date_show").hide();
	    	  $("#sp_id_show").show();
	      }
	      if(selectField=="order_export_date"){
	    	  $("#single_sp_id").val("");
	    	  $("#single_sp_id_input").val("");
	    	  $("#sp_id_show").hide();
	    	  $("#order_export_date_show").show();
	      }
     });
	
	$('#singleSearchBtn').click(function(){
		var selectField = $("#selected_field").val();
		var selectValue = "";
		  if(selectField=='order_no'){
			  selectValue = $("#public_text").val();
	      }
	      if(selectField=="invoice_no"){
	    	  selectValue = $("#public_text").val();
	      }
	      
	      var url = "/invoiceApply/list?"+selectField+"="+selectValue;
	      	dataTable.ajax.url(url).load();
	}); 
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })
      
     var searchData=function(){
    	 var order_no = $("#order_no").val();
         var invoice_no=$('#invoice_no').val();
         
         var url = "/invoiceApply/list?order_no="+order_no
          				+"&invoice_no="+invoice_no;
          dataTable.ajax.url(url).load();
      };
      
	//提交按钮动作
	$("#eeda_table").on("click",".submitBtn",function(){
		var order_id = $(this).parent().parent().parent().attr("id");
	    $.post("/invoiceApply/submitMethod",{order_id:order_id},function(data){
	    	if(data){
	    		$.scojs_message('提交成功', $.scojs_message.TYPE_OK);
	    		$(this).attr("disabled",true);
	    		$(this).parent().find(".checkBtn").attr("checked",false);
	    		refreshDetailed();
	    	}else{
	    		$.scojs_message('提交失败', $.scojs_message.TYPE_ERROR);
	    	}
	    });
	});
	
	//复核按钮动作
	$("#eeda_table").on("click",".checkBtn",function(){
		var order_id = $(this).parent().parent().parent().attr("id");
	    $.post("/invoiceApply/checkMethod",{order_id:order_id},function(data){
	    	if(data){
	    		$.scojs_message('开票复核成功', $.scojs_message.TYPE_OK);
	    		$(this).attr("disabled",true);
	    		$(this).parent().find(".submitBtn").attr("checked",true);
	    		refreshDetailed();
	    	}else{
	    		$.scojs_message('复核开票失败', $.scojs_message.TYPE_ERROR);
	    	}
	    });
	});
	var refreshDetailed = function(){
		var url = "/invoiceApply/list";
	  	dataTable.ajax.url(url).load();
	}
	
  });
});