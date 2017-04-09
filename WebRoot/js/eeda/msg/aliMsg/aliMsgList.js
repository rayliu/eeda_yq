define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 
    $(document).ready(function() {
    	document.title = '速卖通站内信 | '+document.title;
    	
      $("#breadcrumb_li").text('速卖通站内信');
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/aliMsg/list",
            columns:[
                { "data": "ID", visible: false}, 
                { "data": "DEAL_STAT", "width":"20px"}, 
	              { "data": "RANK", "width":"20px"}, 
                { "data": "OTHER_NAME", "width":"60px"},
                { "data": "LAST_MESSAGE_CONTENT", "width":"260px",
                  "render": function ( data, type, full, meta ) {
                      return "<a href='#' class='edit' style='cursor: pointer;'>"+full.LAST_MESSAGE_CONTENT+"</a>";
                  }
                },
                { "data": "CHANNEL_ID","class":'item_id', "width":"60px"},
                { "data": "MESSAGE_TIME", "width":"60px"},
                
            ]
        });
      
      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){

          var url = "/aliMsg/list";

          dataTable.ajax.url(url).load();
      };
      
      $('#importBtn').click(function(){
        $.blockUI();
           $.post('/aliMsg/importMsg', {nothing: 'nothing'}, function(data, textStatus, xhr) {
                $.unblockUI();
                searchData(); 
           });
      });

      $('#eeda_table').on('click','.edit',function(){
        var tr = $(this).parent().parent();
        $('#edit_id').val(tr.attr('id'));

          $.post('/aliMsg/getMemberMsg', {id: tr.attr('id'),item_id:tr.find(".item_id").text()}, function(data, textStatus, xhr) {
        	  $('#msg_id').val(data[0].MESSAGE_ID);
              $('#msg_body').text(data[0].BODY);
              $('#recipient_id').val(data[0].SENDER_ID);
              if(data.RESPONSE){
                $('#msg_response').text(data.RESPONSE);
              }else{
                $('#msg_response').text("");
              }
              
              $('.chat').text('');
              for(var i = 0; i<data.length;i++){
            	  var response = data[i].RESPONSE;
            	  var body = data[i].BODY;
            	  var send_id = data[i].SENDER_ID;
            	  var recipient_id = data[i].RECIPIENT_ID;
            	  var creation_date = data[i].CREATION_DATE;
            	  if(response==null || response==''){
            		  $('.chat').append(
                       	   '<li class="left clearfix">'
                       	  +'	<span class="chat-img pull-left">'
                       	  +'	<img src="/images/user_chat.png" alt="User Avatar" >'
                       	  +'	</span>'
                       	  +'    <div class="chat-body clearfix">'
                       	  +'        <div class="header">'
                       	  +'            <strong class="primary-font">'+send_id+'</strong>'
                       	  +'            <small class="pull-right text-muted">'
                       	  +'                <i class="fa fa-clock-o fa-fw"></i>'+creation_date
                       	  +'            </small>'
                       	  +'        </div><br/>'
                       	  +'    <p>'+body+'</p>'
                       	  +'    </div>'
                       	  +'</li>'  
                       	  );
            	  }else{//发送人
            		  
            		  $('.chat').append(
            		  '<li class="right clearfix">'
            		  +'<span class="chat-img pull-right">'
            		  +'    <img src="/images/user_headphone.png" alt="User Avatar" >'
            		  +'</span>'
            		  +'<div class="chat-body clearfix">'
            		  +'    <div class="header">'
            		  +'    	<strong class="pull-right primary-font">'+recipient_id+'</strong>'
            		  +'        <small class=" text-muted" >'
            		  +'            <i class="fa fa-clock-o fa-fw"></i>'+creation_date
            		  +'        </small>'
            		  +'    </div><br/>'
            		  +'    <p style="float:right">'+response+'</p>'
            		  +'</div>'
            		  +'</li>')};
            	  
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

        $.post('/aliMsg/replyMsg', {id:id, msg_id:msg_id, response:response, recipient_id:recipient_id}, function(data, textStatus, xhr) {
            //alert(data);
            //$('#editMsgModal').modal('hide');
            
           var response = data.RESPONSE;
      	   var body = data.BODY;
      	   var send_id = data.SENDER_ID;
      	   var recipient_id = data.RECIPIENT_ID;
      	   var creation_date = data.CREATION_DATE;
            $('.chat').append(
          		  '<li class="right clearfix">'
          		  +'<span class="chat-img pull-right">'
          		  +'    <img src="/images/user_headphone.png" alt="User Avatar" >'
          		  +'</span>'
          		  +'<div class="chat-body clearfix">'
          		  +'    <div class="header">'
          		  +'    	<strong class="pull-right primary-font">'+recipient_id+'</strong>'
          		  +'        <small class=" text-muted" >'
          		  +'            <i class="fa fa-clock-o fa-fw"></i>'+creation_date
          		  +'        </small>'
          		  +'    </div><br/>'
          		  +'    <p style="float:right">'+response+'</p>'
          		  +'</div>'
          		  +'</li>');
            
            
        }).fail(function() {
            $.scojs_message('回复失败', $.scojs_message.TYPE_ERROR);
        });

      });

    });
});