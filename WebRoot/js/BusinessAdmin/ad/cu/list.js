define(['jquery', 'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
	$(document).ready(function() {

		var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: false,
          serverSide: true, 
          ajax: "/BusinessAdmin/ad/cu/list",
          columns: [
            { "data": "ORDER_NO" ,"width": "80px"},
            { "data": "TITLE","class":"title", "width": "100px"},
            { "data": "STATUS","width": "100px"},
            { "data": "BEGIN_DATE","width": "100px" },
            { "data": "END_DATE", "width": "100px" },
            { "data": "PRICE", "width": "100px"},
            { "data": null, "width": "100px",
              render: function(data,type,full,meta){
                return "<a class='stdbtn btn_blue editBtn' id='"+full.ID+"' content='"+full.CONTENT+"' href='#title'>编辑</a>";
              }
            }
          ]
		});
		
		 var refleshTable = function(){
	    	  dataTable.ajax.url("/BusinessAdmin/ad/cu/list").load();
	     }
		
		
		$('#eeda_table').on('click','.editBtn',function(){
			var $row = $(this).parent().parent();
			var title = $($row.find('.title')).text();
			var content = $(this).attr('content')=='null'?"暂无":$(this).attr('content');
			var id = $(this).attr('id');
			
			$('#item_id').val(id);
			$('#title').val(title);
			$('#content').val(content);
			$("#edit").show();
			
		});
		
		  $('#saveBtn').click(function(event) {
		    	var self = this;
		    	$(self).attr('disabled',true);
	    	  
		    	var id = $('#item_id').val();
		    	var title = $('#title').val();
				var content = $('#content').val();
		    	
		    	$.post('/BusinessAdmin/ad/cu/update',{id:id,title:title,content:content}, function(data, textStatus, xhr) {
		    		if(data){
		    			refleshTable()
		    			$.scojs_message('更新成功', $.scojs_message.TYPE_OK);
		    			$('#item_id').val('');
		    			$('#title').val('');
		    			$('#content').val('');
		    			$("#edit"). hide();
		    		}else{
		    			$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
		    		}
		    		$(self).attr('disabled',false);
		    	});
		    });
		
	});
});