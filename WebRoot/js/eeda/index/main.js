define(['jquery', 'metisMenu', 'sb_admin', './index_weekly_charts', './index_profit_charts'], function ($, metisMenu) {

    $(document).ready(function () {
        document.title = '管理看板 | ' + document.title;

        var findAllCount = function(pointInTime){
            //查询系统单据数量合计
            // $.get('/statusReport/searchOrderCount', {pointInTime:pointInTime}, function(data){
            //     $("#transferOrderTotal").empty().text(data.transferOrderTotal);
            //     $("#pickupTotal").empty().text(data.pickupTotal);
            //     $("#departTotal").empty().text(data.departTotal);
            //     $("#deliveryTotal").empty().text(data.deliveryTotal);
            //     $("#returnTotal").empty().text(data.returnTotal);
            //     $("#insuranceTotal").empty().text(data.insuranceTotal);
            // });
        };
    });
});