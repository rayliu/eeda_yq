define(['jquery', 'metisMenu', 'sb_admin', 'dataTablesBootstrap', 'sco'], function ($, metisMenu) {
    $(document).ready(function() {
        var name = $("#user_name").val();
    	if(name){
    		document.title = name+' | '+document.title;
    	}
    	$("#breadcrumb_li").text('岗位编辑');

        var eeda_table = eeda.dt( {
            id: 'eeda_table',
            paging: false,
            serverSide: true,
            ajax: "/userRole/roleList?username="+name,
            columns: [
                { data:null, width: "8%",
                    render: function( data, type, full, meta){
                        if(full.IS_ASSIGN=='N'){
                             return '<input type="checkbox" name="roleCheck" class="unChecked" role_id="'+full.ID+'">'; 
                         }else{
                             return '<input type="checkbox" checked class="unChecked" name="roleCheck" role_id="'+full.ID+'">';
                         } 
                    }
                },
                { data:"NAME"}
            ]
        });


        $('#saveBtn').click(function(e){
            e.preventDefault();
           
            var username = $("#user_name").val();

            var role=[];
            $("input[name='roleCheck']").each(function(index, el){
                    if($(el).prop('checked') == true){
                        role.push($(el).attr('role_id'));
                    }
             });
            var roles = role.toString();
        	$.post('/userRole/updateRole?name='+username+'&roles='+roles,function(data){
        		$.scojs_message('更新成功', $.scojs_message.TYPE_OK);
        		//$("#saveBtn").attr("disabled",true);
        	},'json');

        });

    });
});