define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','app/wms/importOrder/upload', 'jq_blockui'], function ($, metisMenu) {
	$(document).ready(function() {
    	document.title = '手动加A | '+document.title;

    	$("#breadcrumb_li").text('手动加A');
    	
    	$("#changeBtn").on('click',function(){
    		var part_no = $("#part_no").val();
    		if(part_no.trim() == ''){
    			$.scojs_message('请填写您要更改的组件编码', $.scojs_message.TYPE_ERROR);
    			return false;
    		}
    		
    		$.blockUI({ 
                message: '<h1><img src="/images/loading.gif" style="height: 50px; margin-top: -3px;"/> COMMITTING...</h1>' 
            });
    		
    		$('#changeBtn').attr('disabled', true);
    		$.post('/changePartNo/update',{part_no:part_no.trim()},function(data){
    			if(data){
    				$.scojs_message('更新成功', $.scojs_message.TYPE_OK);
    			}else{
    				$.scojs_message('更新失败', $.scojs_message.TYPE_ERROR);
    			}
    			$('#changeBtn').attr('disabled', false);
    			$.unblockUI();
    		}).fail(function() {
                $.scojs_message('后台报错', $.scojs_message.TYPE_ERROR);
                $('#changeBtn').attr('disabled', false);
                $.unblockUI();
            });
    			
    	});

    	$.unblockUI();
        
	});
});