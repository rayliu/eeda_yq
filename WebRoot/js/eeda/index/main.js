define(['jquery', 'metisMenu', 'sb_admin', './index_weekly_charts', './index_profit_charts'], function ($, metisMenu) {

    $(document).ready(function () {
        document.title = '管理看板 | ' + document.title;

        //查询系统单据数量合计
        $.get('/todo/getPlanOrderTodoCount', function(data){
            //设置index中
            $("#planOrderTodoCount").empty().text(data);
        });
        
        $.get('/todo/getSOTodoCount', function(data){
            //设置index中
            $("#SOTodoCount").empty().text(data);
        });
        
        $.get('/todo/getTruckOrderTodoCount', function(data){
            //设置index中
//            var total = 
            	$("#truckOrderTodoCount").empty().text(data);
//            if（$('#truckOrderPDF').click()）{
//            	
//            	total=total-1;
//            	
//            }
            
//            $("#truckOrderTodoCount").empty().text(total);
        });
        
        $.get('/todo/getSITodoCount', function(data){
            //设置index中
            $("#SITodoCount").empty().text(data);
        });
        
        $.get('/todo/getMBLTodoCount', function(data){
            //设置index中
            $("#MBLTodoCount").empty().text(data);
        });
        
        $.get('/todo/getWaitCustomTodoCount', function(data){
            //设置index中
            $("#waitCustomTodoCount").empty().text(data);
        });
        
        $.get('/todo/getWaitBuyInsuranceTodoCount', function(data){
            //设置index中
            $("#waitBuyInsuranceTodoCount").empty().text(data);
        });
        
        $.get('/todo/getWaitOverseaCustomTodoCount', function(data){
            //设置index中
            $("#waitOverseaCustomTodoCount").empty().text(data);
        });
        
        $.get('/todo/getTlxOrderTodoCount', function(data){
            //设置index中
            $("#TlxOrderTodoCount").empty().text(data);
        });
        
        
    });
});