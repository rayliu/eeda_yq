define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 

  $(document).ready(function() {
	  $("#breadcrumb_li").text('供应商管理');
	  
	  
        $('#customerForm').validate({
            rules: {
            	dock_name: {//form 中company_name为必填, 注意input 中定义的id, name都要为company_name
                required: true
              },
              quick_search_code:{//form 中 abbr为必填
                  required: true
                }
            },
            highlight: function(element) {
                $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
            },
            success: function(element) {
                element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
            }
        });
        
       
      //自动提交改为手动提交
      $("#saveBtn").click(function(){
    	 if(!$("#customerForm").valid()){
    		  return;
    	 }
    	 var order={};
    	 order.id=$('#dock_id').val();
    	 order.dock_name=$('#dock_name').val();
    	 order.dock_name_eng=$('#dock_name_eng').val();
    	 order.quick_search_code=$('#quick_search_code').val();
    	 order.dock_region=$('#dock_region').val();
    	 $.post("/dockInfo/save", {params:JSON.stringify(order)},function(data){
    		if(data.NAME_NUM||data.CODE_NUM){
    			if(data.NAME_NUM)
    				$.scojs_message('保存失败，供应商简称已存在', $.scojs_message.TYPE_ERROR);
    			if(data.CODE_NUM)
    				$.scojs_message('保存失败，供应商助记码已存在', $.scojs_message.TYPE_ERROR);
    		}else if(data.ID){
     			eeda.contactUrl("edit?id",data.ID);
     			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
     			$("#dock_id").val(data.ID);
     		}
         },'json').fail(function() {
             $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
         });
    	  
      });
    });
});