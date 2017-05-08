define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) { 
    $(document).ready(function() {
    	document.title = 'eBay退货 | '+document.title;
    	
      $("#breadcrumb_li").text('eBay退货');
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/ebayReturn/list",
            columns:[
                {"data": "RETURN_ID", "width":"100px", "class":'return_id',
                    "render": function ( data, type, full, meta ) {
                      return "<a href='#' class='edit' style='cursor: pointer;'>"+data+"</a>";
                    }
                },
                {"data": "TRANSACTION_ID", "width":"100px"},
                { "data": "ITEM_ID", "width":"100px"},
	              { "data": "BUYER_LOGIN_NAME", "width":"90px", "class":'buyer_login_name'}, 
                { "data": "SELLER_LOGIN_NAME", "width":"90px", "class":'seller_login_name'},
                { "data": "REASON", "width":"90px", "class":'reason'},
                { "data": "CURRENT_TYPE", "width":"90px", "class":'current_type'},
                { "data": "SELLER_ESTIMATE_TOTAL_REFUND", "width":"60px", "class":'seller_estimate_total_refund',
                  "render": function ( data, type, full, meta ) {
                      if(data){
                          return full.SELLER_ESTIMATE_TOTAL_REFUND_CURRENCY+" "+data;
                      }
                      return "";
                    }
                },
                { "data": "SELLER_ACTUAL_TOTAL_REFUND", "width":"60px",  "class":'seller_actual_total_refund',
                  "render": function ( data, type, full, meta ) {
                      if(data){
                          return full.SELLER_ACTUAL_TOTAL_REFUND_CURRENCY+" "+data;
                      }
                      return "";
                    }
                }, 
                { "data": "CREATION_DATE", "width":"60px", "class":'creation_date'},
                { "data": "STATUS", "width":"60px", "class":'status'},
                { "data": "COMMENTS", "width":"160px", "class":'comments'}
            ]
        });
      
      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){

          var url = "/ebayReturn/list";

          dataTable.ajax.url(url).load();
      };
      
      $('#importBtn').click(function(){
        $.blockUI();
         $.post('/ebayReturn/importReturn', {nothing: 'nothing'}, function(data, textStatus, xhr) {
            searchData();
            $.unblockUI();
         });
      })
    	
      $('#eeda_table').on('click','.edit',function(){
        var tr = $(this).parent().parent();
        $('#edit_id').val(tr.attr('id'));
        var return_id = tr.find(".return_id").text();
        var reason = tr.find(".reason").text();
        var current_type = tr.find(".current_type").text();
        var status = tr.find(".status").text();
        var seller_estimate_total_refund = tr.find(".seller_estimate_total_refund").text();
        var seller_actual_total_refund = tr.find(".seller_actual_total_refund").text();
        var creation_date = tr.find(".creation_date").text();
        var comments = tr.find(".comments").text();
        var buyer_login_name = tr.find(".buyer_login_name").text();
        var seller_login_name= tr.find(".seller_login_name").text();

        $('#buyer_login_name').text(buyer_login_name);
        $('#return_id').text(return_id);
        $('#reason').text(reason);
        $('#current_type').text(current_type);
        $('#status').text(status);
        $('#seller_estimate_total_refund').text(seller_estimate_total_refund);
        $('#seller_actual_total_refund').text(seller_actual_total_refund);
        $('#creation_date').text(creation_date);
        $('#creation_date1').text(creation_date);
        $('#comments').text(comments);

        $.post('/ebayReturn/getMsgHistory', {return_id:return_id}, function(data, textStatus, xhr) {
              
              $('.chat').text('');
              for(var i = 0; i<data.length;i++){
                var type = data[i].AUTHOR;
                var activity = data[i].ACTIVITY;
                var creation_date = data[i].CREATION_DATE;
                var notes = data[i].NOTES;
                var tracking_number = data[i].TRACKING_NUMBER;
                var carrier = data[i].CARRIER;

                if(notes==null)
                  notes='';

                var trackStr='';
                if(tracking_number){
                  trackStr='            <br><br><strong class="primary-font">Tracking No.:</strong> '+tracking_number
                          +' &nbsp;&nbsp;&nbsp;&nbsp;<strong class="primary-font">Carrier:</strong> '+carrier;
                }

                if(type=='BUYER'){

                  $('.chat').append(
                           '<li class="left clearfix">'
                          +'  <span class="chat-img pull-left">'
                          +'  <img src="/images/user_chat.png" alt="User Avatar" >'
                          +'  </span>'
                          +'    <div class="chat-body clearfix">'
                          +'        <div class="header">'
                          +'            <strong class="primary-font">'+buyer_login_name+'</strong> '+activity
                          + trackStr
                          +'            <small class="pull-right text-muted">'
                          +'                <i class="fa fa-clock-o fa-fw"></i>'+creation_date
                          +'            </small>'
                          +'        </div><br/>'
                          +(activity=='BUYER_PROVIDE_TRACKING_INFO'?'':'    <pre>'+notes+'</pre>')
                          +'    </div>'
                          +'</li>'  
                          );
                }else{//发送人
                  
                  var notes_str= '    <pre>'+notes+'</pre>';
                  if(activity=='SELLER_APPROVE_REQUEST' ||
                    activity=='SELLER_PROVIDE_RMA' ||
                    activity=='REMINDER_FOR_SHIPPING'){
                      notes_str='';
                  }


                  $('.chat').append(
                  '<li class="right clearfix">'
                  +'<span class="chat-img pull-right">'
                  +'    <img src="/images/user_headphone.png" alt="User Avatar" >'
                  +'</span>'
                  +'<div class="chat-body clearfix">'
                  +'    <div class="header">'
                  +'      <span class="pull-right"> ' +activity +' '
                  +'        <strong class="primary-font">'+seller_login_name+' </strong></span> '
                  +'        <small class=" text-muted" >'
                  +'            <i class="fa fa-clock-o fa-fw"></i>'+creation_date
                  +'        </small>'
                  +'    </div><br/>'
                  +notes_str
                  +'</div>'
                  +'</li>')
                };

                $('#editMsgModal').modal('show');
              }
          }); 
      });
  });
});