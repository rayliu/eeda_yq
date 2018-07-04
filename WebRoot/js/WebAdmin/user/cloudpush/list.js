define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/user/cloudpush/list",
            columns: [
	                     { "data": "ACTION", "width":"60px"},
	                     { "data": "TARGET", "width":"60px"},
	                     { "data": "TITLE", "width":"60px"},
	                     { "data": "BODY", "width":"60px"},
	                     { "data": "STATUS", "width":"60px"},
	                     { "data": "CREATE_TIME", "width":"60px"}
                     ]
        });

//        $("#eeda_table").on("click",".delete",function(){
//        	var id = $(this).attr("id");
//        	$.post("/WebAdmin/user/ask/deleteQuestion",{id:id},function(data){
//        		if(data.result){
//        			window.location.reload();
//        		}
//            });
//        });
        
 	var refleshTable = function(){
   	  	dataTable.ajax.url("/WebAdmin/user/cloudpush/list").load();
    }
 	
 	
 	$("#addBtn").on("click",function(){
    	var bh=$(window).height();//获取屏幕高度
        var bw=$(window).width();//获取屏幕宽度
        $("#layer").css({
            height:bh,
            width:bw,
            display:"block"
        });
        $("#pop_loc").show();
    });
    
    $("#layer").click(function(){
    	hidelayer();
    })
    
    function hidelayer(){
    	$("#layer").hide();
    	$(".Popup").hide();
    }
    
    $("#confirmBtn").click(function(){
    	var self = this;
    	var action = $("#action").val();
    	var target = $("#target").val();
    	var target_value = $("#target_value").val();
    	var title = $("#title").val();
    	var body = $("#body").val();
    	
    	if ( $.trim(title) == '' 
    		|| $.trim(body) == ''
    	) {
    		$.scojs_message('内容不能为空', $.scojs_message.TYPE_ERROR);
    		return ;
    	}
    	
    	self.disabled = true;
    	$.post("/WebAdmin/user/cloudpush/sendMsg",{action: action, target: target, target_value: target_value, title: title, body: body},function(data){
    		if(data.RESULT == 'ok') {
    			$.scojs_message('推送成功', $.scojs_message.TYPE_OK);
    			$("#layer").click();
    			refleshTable();
    		} else {
    			$.scojs_message('推送失败', $.scojs_message.TYPE_ERROR);
    		}
    		self.disabled = false;
    	});
    });
});
});