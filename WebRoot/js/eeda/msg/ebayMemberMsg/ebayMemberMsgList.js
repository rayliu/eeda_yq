define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
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
	              { "data": "SENDER_ID", "class":'sender_id', "width":"60px"}, 
                { "data": "RECIPIENT_ID", "width":"60px"},
                { "data": "SUBJECT", 
                  "render": function ( data, type, full, meta ) {
                      return "<a href='#' class='edit' style='cursor: pointer;'>"+data+"</a>";
                  }
                },
                { "data": "CREATION_DATE", "width":"60px"},
                { "data": "ITEM_ID","class":'item_id', "width":"60px"}
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
        $.blockUI();
           $.post('/ebayMemberMsg/importMemberMsg', {nothing: 'nothing'}, function(data, textStatus, xhr) {
              if(data.STATUS == 'OK'){
                $.scojs_message('同步成功', $.scojs_message.TYPE_OK);
                searchData(); 
                $.unblockUI();
              }else{
                $.scojs_message('同步失败:'+data.MSG, $.scojs_message.TYPE_ERROR);
                $.unblockUI();
              }
              
           });
      });

      $('#eeda_table').on('click','.edit',function(){
        var tr = $(this).parent().parent();
        $('#edit_id').val(tr.attr('id'));
        var sender_id = tr.find(".sender_id").text();

          $.post('/ebayMemberMsg/getMemberMsg', {id: tr.attr('id'),item_id:tr.find(".item_id").text(), sender_id:sender_id}, function(data, textStatus, xhr) {
              $('#msg_id').val(data[0].MESSAGE_ID);
        	    $('#sender_id').val(data[0].SENDER_ID);
              $('#recipient_id').val(data[0].RECIPIENT_ID);
              $('#subject').val(data[0].SUBJECT);
              $('#item_id').val(data[0].ITEM_ID);
              $('#msg_type').text(data[0].MESSAGE_TYPE);
              
              $('.chat').text('');
              for(var i = 0; i<data.length;i++){
              	  var body = data[i].BODY;
              	  var sender_id = data[i].SENDER_ID;
              	  var recipient_id = data[i].RECIPIENT_ID;
              	  var creation_date = data[i].CREATION_DATE;
              	  var replay_flag = data[i].REPLAY_FLAG;
                  var response = data[i].RESPONSE;
                  var response_date = data[i].LAST_MODIFIED_DATE;

                  if(replay_flag=='N'){
                      $('.chat').append(
                               '<li class="left clearfix">'
                              +'  <span class="chat-img pull-left">'
                              +'  <img src="/images/user_chat.png" alt="User Avatar" >'
                              +'  </span>'
                              +'    <div class="chat-body clearfix">'
                              +'        <div class="header">'
                              +'            <strong class="primary-font">'+sender_id+'</strong>'
                              +'            <small class="pull-right text-muted">'
                              +'                <i class="fa fa-clock-o fa-fw"></i>'+creation_date
                              +'            </small>'
                              +'        </div><br/>'
                              +'    <pre>'+body+'</pre>'
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
                      +'      <strong class="pull-right primary-font">'+sender_id+'</strong>'
                      +'        <small class=" text-muted" >'
                      +'            <i class="fa fa-clock-o fa-fw"></i>'+creation_date
                      +'        </small>'
                      +'    </div><br/>'
                      +'    <pre style="float:right">'+body+'</pre>'
                      +'</div>'
                      +'</li>');
                    };

            	  
              }
              $('#editMsgModal').modal('show');
           });
          $('#msg_subject').val($(this).text()); 
      });

      $('#modal_reply_btn').click(function(event) {
        var msg_response = $('#msg_response').val(); 
        var recipient_id=$('#recipient_id').val();
        var sender_id=$('#sender_id').val();
        var subject=$('#subject').val();
        var item_id=$('#item_id').val();
        var msg_id=$('#msg_id').val(); 

        $.blockUI();
        $.post('/ebayMemberMsg/replyMsg', {msg_id:msg_id, item_id:item_id, msg_response:msg_response,sender_id:sender_id,subject:subject, recipient_id:recipient_id}, function(data, textStatus, xhr) {
      	   var body = data.BODY;
      	   var sender_id = data.SENDER_ID;
      	   var recipient_id = data.RECIPIENT_ID;
      	   var creation_date = data.CREATION_DATE;
            $('.chat').append(
          		  '<li class="right clearfix">'
          		  +'<span class="chat-img pull-right">'
          		  +'    <img src="/images/user_headphone.png" alt="User Avatar" >'
          		  +'</span>'
          		  +'<div class="chat-body clearfix">'
          		  +'    <div class="header">'
          		  +'    	<strong class="pull-right primary-font">'+sender_id+'</strong>'
          		  +'        <small class=" text-muted" >'
          		  +'            <i class="fa fa-clock-o fa-fw"></i>'+creation_date
          		  +'        </small>'
          		  +'    </div><br/>'
          		  +'    <pre style="float:right">'+body+'</pre>'
          		  +'</div>'
          		  +'</li>');
            
            $('#msg_response').val("");
            $.scojs_message('回复成功', $.scojs_message.TYPE_OK);
            $.unblockUI();
        }).fail(function() {
            $.scojs_message('回复失败', $.scojs_message.TYPE_ERROR);
            $.unblockUI();
        });

      });

    });
});