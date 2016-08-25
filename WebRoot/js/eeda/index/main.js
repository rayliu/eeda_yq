define(['jquery', 'metisMenu', 'sb_admin', './index_weekly_charts', './index_profit_charts'], function ($, metisMenu) {

    $(document).ready(function () {
        document.title = '管理看板 | ' + document.title;

        //查询系统单据数量合计
        $.get('/todo/getPlanOrderTodoCount', function(data){
            //设置index中
            $("#planOrderTodoCount").empty().text(data);
            $(".planOrderWait").empty().text(data);
        });
        
        $.get('/todo/getSOTodoCount', function(data){
            //设置index中
            $("#SOTodoCount").empty().text(data);
            $(".soWait").empty().text(data);
        });
        
        $.get('/todo/getTruckOrderTodoCount', function(data){
            //设置index中
            	$("#truckOrderTodoCount").empty().text(data);
            	$(".truckOrderWait").empty().text(data);

        });
        
        $.get('/todo/getSITodoCount', function(data){
            //设置index中
            $("#SITodoCount").empty().text(data);
            $(".siWait").empty().text(data);
        });
        
        $.get('/todo/getMBLTodoCount', function(data){
            //设置index中
            $("#MBLTodoCount").empty().text(data);
            $(".mblWait").empty().text(data);
        });
        
        $.get('/todo/getWaitCustomTodoCount', function(data){
            //设置index中
            $("#waitCustomTodoCount").empty().text(data);
            $(".customWait").empty().text(data);
        });
        
        $.get('/todo/getWaitBuyInsuranceTodoCount', function(data){
            //设置index中
            $("#waitBuyInsuranceTodoCount").empty().text(data);
            $(".insuranceWait").empty().text(data);
        });
        
        $.get('/todo/getWaitOverseaCustomTodoCount', function(data){
            //设置index中
            $("#waitOverseaCustomTodoCount").empty().text(data);
            $(".overseaCustomWait").empty().text(data);
        });
        
        $.get('/todo/getTlxOrderTodoCount', function(data){
            //设置index中
            $("#TlxOrderTodoCount").empty().text(data);
            $(".tlxOrderWait").empty().text(data);
        });
        
        
    });
});