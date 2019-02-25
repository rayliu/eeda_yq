define(['jquery','hui'], function ($,huiCont) {
    $(document).ready(function() {
        document.title = '邮件设置 | '+document.title;
        
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
        
        $(".radio-box").click(function(){
        	$(".radio-box input").prop("checked",false);
        	$(this).children("input").prop("checked",true);
        });
        
        $("#save_btn").click(function(){
        	var order = [];
        	var smtpFrom = $("#smtpFrom").serializeArray();
        	smtpFrom.push({"name":"type","value":"smtp"});
        	smtpFrom.push({"name":"checked","value":$("#smtp").prop("checked")==true?"Y":"N"});
        	
        	var aliyunFrom = $("#aliyunFrom").serializeArray();
        	aliyunFrom.push({"name":"type","value":"aliyun"});
        	aliyunFrom.push({"name":"checked","value":$("#aliyun").prop("checked")==true?"Y":"N"});
        	
        	var sendCloudFrom = $("#sendCloudFrom").serializeArray();
        	sendCloudFrom.push({"name":"type","value":"send_cloud"});
        	sendCloudFrom.push({"name":"checked","value":$("#send_cloud").prop("checked")==true?"Y":"N"});
        	
        	order.push(smtpFrom);
        	order.push(aliyunFrom);
        	order.push(sendCloudFrom);
        	
        	$.post("/email/save",{params:JSON.stringify(order)},function(data){
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