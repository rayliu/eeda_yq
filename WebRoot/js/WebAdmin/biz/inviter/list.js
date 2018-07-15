define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
$(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/biz/inviter/list",
            columns: [
	                     { "data": "INVITE_CODE", "width":"60px"},
	                     { "data": "INVITER_NAME", "width":"60px"},
	                     { "data": "PHONE", "width":"60px"},
	                     { "data": "ALIPAY_NO", "width":"120px"},
	                     { "data": "COMPANY", "width":"120px"},
	                     { "data": "INVITE_AMOUNT", "width":"120px"},
	                     { "data": "REMARK", "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 if(!data){
	                    			 data = '';
	                    		 }
	                    		 return "<textarea style='width:100%' class='remark' data-id='"+full.ID+"'>"+data+"</textarea>";
	                    	 }
	                     }
                     ]
        });
        
      
        $('#searchBtn').on('click',function(){
        	var begin_date = $('#begin_date').val();
        	var end_date = $('#end_date').val();
        	var location = $('#location').val();
        	dataTable.ajax.url("/WebAdmin/biz/inviter/list?begin_date="+begin_date+"&end_date="+end_date+"&location="+location).load();
        });
        
        
        $('#eeda_table').on('blur', '.remark', function(){
        	var self = this;
        	var id = $(this).data('id');
        	var remark = $(this).val();

        	self.disabled = true;
        	$.post("/WebAdmin/biz/inviter/update_remark",{id: id, remark: remark},function(data){
        		if(data.RESULT) {
        			$.scojs_message('更新成功', $.scojs_message.TYPE_OK);
        		} else {
        			$.scojs_message('更新失败', $.scojs_message.TYPE_ERROR);
        		}
        		self.disabled = false;
        	}).fail(function(){
        		$.scojs_message('后台出错', $.scojs_message.TYPE_ERROR);
        	});
        });
        
	
});
});