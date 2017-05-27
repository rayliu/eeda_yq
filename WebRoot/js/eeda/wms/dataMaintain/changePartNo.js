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
    			if(data.RESULT){
    				$.scojs_message('更新成功', $.scojs_message.TYPE_OK);
    			}else{
    				alert(data.CAUSE +',请检查系统BOM中是否存在加A后的part_no', $.scojs_message.TYPE_ERROR);
    			}
    			$('#changeBtn').attr('disabled', false);
    			$.unblockUI();
    		}).fail(function() {
                $.scojs_message('后台报错', $.scojs_message.TYPE_ERROR);
                $('#changeBtn').attr('disabled', false);
                $.unblockUI();
            });
    			
    	});
    	

        $('#resetBtn').click(function(e){
        	$("#orderForm")[0].reset();
        });
        

    	$.unblockUI();
        
	});
});