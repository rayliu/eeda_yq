define(['jquery', 'metisMenu', 'sb_admin', './todo', './index_weekly_charts', './index_profit_charts'], 
	function ($, metisMenu, sb, todoController) {

    $(document).ready(function () {
        document.title = '管理看板 | ' + document.title;

        todoController.updateTodo(true);
        
        $('.seeMsgBoardDetail').click(function(){
        	var id = $(this).attr("msgBoardId");
        	$.post('/msgBoard/seeMsgBoardDetail', {id:id}, function(data){
        		$('#addRadioBtn').hide()
	            $('#radioTitle').val(data.TITLE)
	            $('#radioContent').val(data.CONTENT)
	            $('#addRadio').click()
        	},'json')
        })
        
        
    });
});