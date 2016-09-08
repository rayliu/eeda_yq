define(['jquery', 'metisMenu', 'sb_admin', './todo', './index_weekly_charts', './index_profit_charts'], 
	function ($, metisMenu, sb, todoController) {

    $(document).ready(function () {
        document.title = '管理看板 | ' + document.title;

        todoController.updateTodo(true);
    });
});