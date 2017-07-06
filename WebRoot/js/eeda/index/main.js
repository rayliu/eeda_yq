define(['jquery', 'metisMenu', 'sb_admin', './todo'], 
	function ($, metisMenu, sb, todoController) {

    $(document).ready(function () {

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
