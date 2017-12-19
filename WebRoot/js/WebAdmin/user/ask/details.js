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
});
});