define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	$("#updateBtn").click(function(){
    		var content=$("#notice_content").val();
    		$.post("/WebAdmin/biz/notice/update",{content:content},function(data){
    			if(data){
    				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
    			}else{
    				$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
    			}
    		})
    	})
    	
    	
    });
});