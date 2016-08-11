define(['jquery', 'metisMenu', 'sb_admin', './index_weekly_charts', './index_profit_charts'], function ($, metisMenu) {

    $(document).ready(function () {
        document.title = '管理看板 | ' + document.title;

        //查询系统单据数量合计
        $.get('/todo/getPlanOrderTodoCount', function(data){
            //设置index中
            $("#planOrderTodoCount").empty().text(data);
        });
    });
});