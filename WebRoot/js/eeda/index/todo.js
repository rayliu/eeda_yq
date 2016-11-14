define(['jquery'], function ($) {
       
    var updateTodo = function(bUpdateIndex){
    	var bIndex = bUpdateIndex || false;
        //查询系统单据数量合计
        $.get('/todo/getYqTodoList', function(data){
        	if(bIndex){
            	$("#planOrderTodoCount").empty().text(data.planOrderTodoCount);
                $("#SOTodoCount").empty().text(data.SOTodoCount);
                $("#truckOrderTodoCount").empty().text(data.truckOrderTodoCount);
                $("#SITodoCount").empty().text(data.SITodoCount);
                $("#MBLTodoCount").empty().text(data.MBLTodoCount);
                $("#waitCustomTodoCountPlan").empty().text(data.waitCustomTodoCountPlan);
                $("#waitCustomTodoCount").empty().text(data.waitCustomTodoCount);
                $("#waitOverseaCustomTodoCount").empty().text(data.waitOverseaCustomTodoCount);
                $("#TlxOrderTodoCount").empty().text(data.TlxOrderTodoCount);
                $("#waitBuyInsuranceTodoCount").empty().text(data.waitBuyInsuranceTodoCount);
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