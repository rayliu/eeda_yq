define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
$(document).ready(function() {
	//datatable, 动态处理
    var dataTable = eeda.dt({
        id: 'eeda_table',
        paging: true,
        serverSide: true, //不打开会出现排序不对
        ajax: "/WebAdmin/biz/inviterDetail/list",
        columns: [
                  	{ "data": "INVITER_NAME", "width":"60px"},
                  	{ "data": "PARENT_NAME", "width":"60px"},
                  	{ "data": "PARENT_CODE", "width":"60px"},
                  	{ "data": "INVITATION_CODE", "width":"60px"},
                    { "data": "USER_NAME", "width":"60px"},
                    { "data": "PHONE", "width":"80px" },
                    { "data": "WEDDING_DATE", "width":"80px"},
                    { "data": "CREATE_TIME", "width":"80px"},
                    { "data": "REMARK1", "width":"60px",
                    	 "render":function(data,type,full,meta){
                    		 if(!data){
                    			 data = '';
                    		 }
                    		 return "<textarea style='width:100%' class='remark1' data-id='"+full.ID+"'>"+data+"</textarea>";
                    	 }
                    },
                    { "data": "REMARK2", "width":"60px",
                    	 "render":function(data,type,full,meta){
                    		 if(!data){
                    			 data = '';
                    		 }
                    		 return "<textarea style='width:100%' class='remark2' data-id='"+full.ID+"'>"+data+"</textarea>";
                    	 }
                    },
                    { "data": "REMARK3", "width":"60px",
                    	 "render":function(data,type,full,meta){
                    		 if(!data){
                    			 data = '';
                    		 }
                    		 return "<textarea style='width:100%' class='remark3' data-id='"+full.ID+"'>"+data+"</textarea>";
                    	 }
                    }
                 ]
    });
    
    
    
    $('#eeda_table').on('blur', '.remark1', function(){
    	var self = this;
    	var id = $(this).data('id');
    	var remark = $(this).val();

    	self.disabled = true;
    	$.post("/WebAdmin/biz/inviterDetail/update_remark",{id: id, remark1: remark, index:1},function(data){
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
    
    $('#eeda_table').on('blur','.remark2',function(){
    	var self = this;
    	var id = $(this).data('id');
    	var remark = $(this).val();

    	self.disabled = true;
    	$.post("/WebAdmin/biz/inviterDetail/update_remark",{id: id, remark2: remark, index:2},function(data){
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
    
    $('#eeda_table').on('blur','.remark3',function(){
    	var self = this;
    	var id = $(this).data('id');
    	var remark = $(this).val();

    	self.disabled = true;
    	$.post("/WebAdmin/biz/inviterDetail/update_remark",{id: id, remark3: remark, index:3},function(data){
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
    
    
    $('#searchBtn').on('click',function(){
    	var begin_date = $('#begin_date').val();
    	var end_date = $('#end_date').val();
    	var location = $('#location').val();
    	dataTable.ajax.url("/WebAdmin/biz/inviterDetail/list?begin_date="+begin_date+"&end_date="+end_date+"&location="+location).load();
    });


});
});