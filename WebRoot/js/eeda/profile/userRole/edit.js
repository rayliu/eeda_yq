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
                        if(full.ROLE_CODE==null){
                             return '<input type="checkbox" name="roleCheck" class="unChecked" value="'+full.ID+'">'; 
                         }else{
                             return '<input type="checkbox" checked="true" class="unChecked" name="roleCheck" value="'+full.ID+'">';
                         } 
                    }
                },
                { data:"CODE"},
                { data:"NAME"}
            ]
        });

        // var datatable = $('#eeda-table').dataTable({
        // 	"bFilter" : false,
        // 	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //     //"sPaginationType": "bootstrap",
        // 	"bStateSave":true,
        // 	"iDisplayLength": 25,
        // 	"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        //     "bServerSide": true,
        // 	"oLanguage": {
        //         "sUrl": "/js/lib/datatables/i18n/Chinese.json"
        //     },
        //     "sAjaxSource": "/userRole/roleList?username="+name,
        //     "aoColumns": [
        //         { "mDataProp": null, "sWidth": "7%"	,
        //         	 "fnRender": function(obj) {
        //         		 if(obj.aData.ROLE_CODE==null){
        //         			 return '<input type="checkbox" name="roleCheck" class="unChecked" value="'+obj.aData.ID+'">'; 
        //         		 }else{
        //         			 return '<input type="checkbox" checked="true" class="unChecked" name="roleCheck" value="'+obj.aData.ID+'">';
        //         		 } 
        //               }	
        //         },
        //         { "mDataProp": "CODE"},
        //         { "mDataProp": "NAME"},
              
        //     ]
        // });	
        var role=[];
        $("#eeda-table").on('click','.unChecked',function(){
    		 role.splice(0,role.length);
    		 $("input[name='roleCheck']").each(function(){
    	        	if($(this).prop('checked') == true){
    	        		role.push($(this).val());
    	        	}
    	     });
    		 console.log(role);
    	  });
        $('#saveBtn').click(function(e){
            e.preventDefault();
           
            var username = $("#user_name").val();
            var roles = role.toString();
        	$.post('/userRole/updateRole?name='+username+'&roles='+roles,function(data){
        		$.scojs_message('更新成功', $.scojs_message.TYPE_OK);
        		//$("#saveBtn").attr("disabled",true);
        	},'json');
            
        });
        var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable"  style="display:none">'+
    			    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
    			    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
    			    '</div>';
        $('body').append(alerMsg); 
        
    });	
});