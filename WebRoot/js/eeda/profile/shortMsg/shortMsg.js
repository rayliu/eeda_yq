define(['jquery','hui'], function ($,huiCont) {
    $(document).ready(function() {
        document.title = '短信设置 | '+document.title;
        
        $.Huitab = function(tabBar,tabCon,class_name,tabEvent,i){
        	var $tab_menu = $(tabBar);
        	// 初始化操作
        	$tab_menu.removeClass(class_name);
        	$(tabBar).eq(i).addClass(class_name);
        	$(tabCon).hide();
        	$(tabCon).eq(i).show();
        	  
        	$tab_menu.bind(tabEvent,function(){
        	  	$tab_menu.removeClass(class_name);
        	      $(this).addClass(class_name);
        	      var index=$tab_menu.index(this);
        	      $(tabCon).hide();
        	      $(tabCon).eq(index).show();
        	});
        };
        $.Huitab("#tab_demo .tabBar span","#tab_demo .tabCon","current","click","0");
      
        
        $("#save_btn").click(function(){
        	var shortMsgFrom = $("#shortMsgFrom").serializeArray();
        	
        	$.post("/shortMsg/save",{params:JSON.stringify(shortMsgFrom)},function(data){
        		if(data.result){
        			$(".Huialert-error").hide();
        			$(".Huialert-success").show();
        		}else{
        			$(".Huialert-success").hide();
        			$(".Huialert-error").show();
        		}
        	}).fail(function(){
        		$(".Huialert-success").hide();
    			$(".Huialert-error").show();
            });;
        });
    });
});