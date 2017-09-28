define(['jquery'], function ($) {
       
    var updateTodo = function(bUpdateIndex){
    	var bIndex = bUpdateIndex || false;
        //查询系统单据数量合计
        $.get('/todo/getYqTodoList', function(data){
        	if(bIndex){
            	//$("#wait1").empty().text(data.WAITABOUTSHIPMENTTODOCOUNT);
                $("#wait1").empty().text(data.WAITABOUTSHIPMENTTODOCOUNT);
                $("#wait2").empty().text(data.TRUCKORDERTODOCOUNT);
                $("#wait3").empty().text(data.WAITSHIPMENTHEADTODOCOUNT);
                $("#wait4").empty().text(data.VGMTODOCOUNT);
                $("#wait5").empty().text(data.HBLTODOCOUNT);
                $("#wait6").empty().text(data.MBLTODOCOUNT);
                $("#wait7").empty().text(data.WAITBUYINSURANCETODOCOUNT);//保险
                $("#wait8").empty().text(data.WAITOVERSEACUSTOMTODOCOUNT);
                $("#wait9").empty().text(data.TLXORDERTODOCOUNT);
                $("#wait10").empty().text(data.WAITCUSTOMTODOCOUNT);
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