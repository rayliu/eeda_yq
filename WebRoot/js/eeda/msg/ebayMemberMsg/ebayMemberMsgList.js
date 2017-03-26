define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 
    $(document).ready(function() {
    	document.title = 'eBay站内信 | '+document.title;
    	
      $("#breadcrumb_li").text('eBay站内信');
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/ebayMemberMsg/list",
            columns:[
                { "data": "ID", visible: false}, 
                { "data": "MESSAGE_STATUS", "width":"20px"}, 
	              { "data": "SENDER_ID", "width":"60px"}, 
                { "data": "RECIPIENT_ID", "width":"60px"},
                { "data": "SUBJECT", "width":"260px",
                  "render": function ( data, type, full, meta ) {
                      return "<a href='#' class='edit' style='cursor: pointer;'>"+data+"</a>";
                  }
                },
                { "data": "CREATION_DATE", "width":"60px"},
                { "data": "ITEM_ID", "width":"60px"}
            ]
        });
      
      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){

          var url = "/ebayMemberMsg/list";

          dataTable.ajax.url(url).load();
      };
      
      $('#importBtn').click(function(){
           $.post('/ebayMemberMsg/importMemberMsg', {nothing: 'nothing'}, function(data, textStatus, xhr) {
                
           });
      });

      $('#eeda_table').on('click','.edit',function(){
        var tr = $(this).parent().parent();
        $('#edit_id').val(tr.attr('id'));

          $.post('/ebayMemberMsg/getMemberMsg', {id: tr.attr('id')}, function(data, textStatus, xhr) {
              $('#msg_id').val(data.MESSAGE_ID);
              $('#msg_body').text(data.BODY);
              $('#recipient_id').val(data.SENDER_ID);
              if(data.RESPONSE){
                $('#msg_response').text(data.RESPONSE);
              }else{
                $('#msg_response').text("");
              }
              $('#editMsgModal').modal('show');
           });
          $('#msg_subject').val($(this).text());
       
          
      });

    	$('#modal_reply_btn').click(function(event) {
        var id=$('#edit_id').val(); 
        var msg_id=$('#msg_id').val(); 
        var response=$('#msg_response').val(); 
        var recipient_id=$('#recipient_id').val();

        $.post('/ebayMemberMsg/replyMsg', {id:id, msg_id:msg_id, response:response, recipient_id:recipient_id}, function(data, textStatus, xhr) {
            alert(data);
            $('#editMsgModal').modal('hide');
        });

      });

    });
});