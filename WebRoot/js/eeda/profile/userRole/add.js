define(['jquery', 'metisMenu', 'sb_admin', 'dataTablesBootstrap', 'sco'], function ($, metisMenu) {
    var queryUser = function(){ 
    	$.post('/userRole/userList', function(data){
    		var userList =$("#user_filter");
    		userList.empty();
    		userList.append("<option value='' checked>请选择用户</option>");
    		for(var i = 0; i < data.length; i++)
    		{
    			var user_name = data[i].USER_NAME;
    			if(user_name == null){
    				user_name = '';
    			}
    			
    			userList.append("<option value='"+user_name+"'>"+user_name+"</option>");
    		}
    	},'json');
    };
    $(document).ready(function() {
    	
    	$("#breadcrumb_li").text('分配用户岗位');
    	
        var roletable = eeda.dt({
            id: 'eeda-table',
            "ajax": "/role/listPart",
            "columns": [
                { "data": null, "width": "7%"	,
                	"render": function(data, type, full, meta) {
                        return '<input type="checkbox" name="roleCheck" class="unChecked" value="'+full.ID+'">';
                    }
                },
                //{ "data": "CODE"},
                { "data": "NAME"},
                { "data": "REMARK"}
            ] 
        });

        queryUser();
        
    	var role=[];
    	 $("#eeda-table").on('click','.unChecked',function(){
    		 role.splice(0,role.length);
    		 $("input[name='roleCheck']").each(function(){
    	        	if($(this).prop('checked') == true){
    	        		role.push($(this).val());
    	        	}
    	     });
    		 
    	  });
        $('#saveBtn').click(function(e){
            e.preventDefault();
            var username = $("#user_filter").val();
            var roles = role.toString();
            
            if(username != ""&&role.length!=0){
            	$.post('/userRole/saveUserRole?name='+username+'&roles='+roles, function(data){
            		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
            		$("#user_filter").empty();
            		queryUser();
            		/*$("#user_filter").empty();;*/
            		$("input[name='roleCheck']").each(function(){
                    	$(this).prop('checked',false);	
                    });
            		
            		role.splice(0,role.length);	
        		},'json');
            }else{
            	$.scojs_message('保存失败，当前没有选择用户或者岗位', $.scojs_message.TYPE_ERROR);
            }
           
        });
        
        var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable"  style="display:none">'+
    			    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
    			    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
    			    '</div>';
        $('body').append(alerMsg);
       

    });
});