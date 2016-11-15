define(['jquery'], function ($) {
       
    var updateTodo = function(bUpdateIndex){
    	var bIndex = bUpdateIndex || false;
        //查询系统单据数量合计
        $.get('/todo/getYqTodoList', function(data){
        	if(bIndex){
            	$("#planOrderTodoCount").empty().text(data.PLANORDERTODOCOUNT);
                $("#SOTodoCount").empty().text(data.SOTODOCOUNT);
                $("#truckOrderTodoCount").empty().text(data.TRUCKORDERTODOCOUNT);
                $("#SITodoCount").empty().text(data.SITODOCOUNT);
                $("#MBLTodoCount").empty().text(data.MBLTODOCOUNT);
                $("#waitCustomTodoCountPlan").empty().text(data.WAITCUSTOMTODOCOUNTPLAN);
                $("#waitCustomTodoCount").empty().text(data.WAITCUSTOMTODOCOUNT);
                $("#waitOverseaCustomTodoCount").empty().text(data.WAITOVERSEACUSTOMTODOCOUNT);
                $("#TlxOrderTodoCount").empty().text(data.TLXORDERTODOCOUNT);
                $("#waitBuyInsuranceTodoCount").empty().text(data.WAITBUYINSURANCETODOCOUNT);
        	}
            // $(".planOrderWait").empty().text(data);
            // $(".soWait").empty().text(data);
            // $(".truckOrderWait").empty().text(data);
            // $(".siWait").empty().text(data);
            // $(".mblWait").empty().text(data);
            // $(".customwaitPlan").empty().text(data);
            // $(".customWait").empty().text(data);
            // $(".insuranceWait").empty().text(data);
            // $(".overseaCustomWait").empty().text(data);
            // $(".tlxOrderWait").empty().text(data);
        });

    }

    return {
    	updateTodo: updateTodo
    };
});