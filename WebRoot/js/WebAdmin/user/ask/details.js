define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
$(document).ready(function() {
    	$(".delete").click(function(){
    		var id= $(this).attr("id");
    		$.post("/WebAdmin/user/ask/deleteResponse",{id:id},function(data){
    			if(data.result){
        			window.location.reload();
        		}
        	});
    	});
    	
    	$('#answerBtn').on('click', function(){
    		 var bh=$(window).height();//获取屏幕高度
 	        var bw=$(window).width();//获取屏幕宽度
 	        $("#layer").css({
 	            height:bh,
 	            width:bw,
 	            display:"block"
 	        });
 	        
 	        $('#box').show();
	    });
    	
    	$("#layer").click(function(){
    		$("#layer").hide();
	    	$(".pop").hide();
	    });
    	
    	$("#saveBtn").click(function(){
    		var self = this;
    		var value = $('#answerValue').val();
    		var order_id = $('#order_id').val();
    		
    		if(value.trim() == ''){
    			$.scojs_message('内容不能为空', $.scojs_message.TYPE_ERROR);
    			return;
    		}
    		self.diabled = true;
    		$.post("/WebAdmin/user/ask/save",{order_id:order_id,value:value},function(data){
    			if(data){
        			window.location.reload();
        		}else{
        			$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR);
        			self.diabled = false;
        		}
        	});
    	});
    	

});
});