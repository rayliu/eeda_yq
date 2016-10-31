define(['jquery', 'metisMenu', 'sb_admin', './todo', './index_weekly_charts'], 
	function ($, metisMenu, sb, todoController) {

    $(document).ready(function () {
        document.title = '管理看板 | ' + document.title;

        todoController.updateTodo(true);
        
        $('.seeMsgBoardDetail').click(function(){
        	var id = $(this).attr("msgBoardId");
        	$.post('/msgBoard/seeMsgBoardDetail', {id:id}, function(data){
        		$('#radioTitleSpan').text(data.TITLE)
        		$('#radioContentSpan').text(data.CONTENT)
	            $('#seeRadio').click()
        	},'json')
        })
        
    });
});