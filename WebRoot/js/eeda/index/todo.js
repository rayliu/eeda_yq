define(['jquery'], function ($) {
       
    var updateTodo = function(bUpdateIndex){
    	var bIndex = bUpdateIndex || false;
        //查询系统单据数量合计
        $.get('/todo/getPlanOrderTodoCount', function(data){
        	if(bIndex){
            	$("#planOrderTodoCount").empty().text(data);
        	}
            $(".planOrderWait").empty().text(data);
        });
        
        $.get('/todo/getSOTodoCount', function(data){
            if(bIndex){
            	$("#SOTodoCount").empty().text(data);
            }
            $(".soWait").empty().text(data);
        });
        
        $.get('/todo/getTruckOrderTodoCount', function(data){
        	if(bIndex){
            	$("#truckOrderTodoCount").empty().text(data);
            }
        	$(".truckOrderWait").empty().text(data);

        });
        
        $.get('/todo/getSITodoCount', function(data){
            if(bIndex){
	            $("#SITodoCount").empty().text(data);
	        }
            $(".siWait").empty().text(data);
        });
        
        $.get('/todo/getMBLTodoCount', function(data){
            if(bIndex){
            	$("#MBLTodoCount").empty().text(data);
            }
            $(".mblWait").empty().text(data);
        });
        
        $.get('/todo/getWaitCustomTodoCountPlan', function(data){
            if(bIndex){
            	$("#waitCustomTodoCountPlan").empty().text(data);
            }
            $(".customwaitPlan").empty().text(data);
        });
        
        $.get('/todo/getWaitCustomTodoCount', function(data){
            if(bIndex){
            	$("#waitCustomTodoCount").empty().text(data);
            }
            $(".customWait").empty().text(data);
        });
        
        $.get('/todo/getWaitBuyInsuranceTodoCount', function(data){
            if(bIndex){
            	$("#waitBuyInsuranceTodoCount").empty().text(data);
            }
            $(".insuranceWait").empty().text(data);
        });
        
        $.get('/todo/getWaitOverseaCustomTodoCount', function(data){
            if(bIndex){
            	$("#waitOverseaCustomTodoCount").empty().text(data);
            }
            $(".overseaCustomWait").empty().text(data);
        });
        
        $.get('/todo/getTlxOrderTodoCount', function(data){
            if(bIndex){
            	$("#TlxOrderTodoCount").empty().text(data);
            }
            $(".tlxOrderWait").empty().text(data);
        });
    }
        
    return {
    	updateTodo: updateTodo
    };
});