var queryRole = function(){
	$.post('/privilege/seachNewRole', function(data){
		var roleList =$("#role_filter");
		roleList.empty();
		roleList.append("<option value='' checked>请选择岗位</option>");
		for(var i = 0; i < data.length; i++)
		{
			var name = data[i].NAME;
			if(name == null){
				name = '';
			}
			
			roleList.append("<option value='"+data[i].NAME+"'>"+name+"</option>");
		}
	},'json');
};

$(document).ready(function() {
  $('#menu_sys_profile').addClass('active').find('ul').addClass('in');
  queryRole();

	var numberName="";
	var privilege_table = $('#eeda-table').dataTable({
    	"bFilter" : false,
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        //"sPaginationType": "bootstrap",
    	"bPaginate": false,
    	"bLengthChange": false,
    	"bInfo": false,
    	"bStateSave":true,
    	"iDisplayLength": 25,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/privilege/list",
        "aoColumns": [           
            { "mDataProp": null,"sWidth":"15%",
            	"fnRender":function(obj){
            		console.log(obj.aData.IS_AUTHORIZE);
            		if(obj.aData.IS_AUTHORIZE !=null && obj.aData.IS_AUTHORIZE !=0){
            			
            			return obj.aData.MODULE_NAME+'<br/>'+'<br/><div><input type="checkbox" class="model_checkAll" name="checkmodel" >本模块全选</div>';
            		}else{
            			return obj.aData.MODULE_NAME+'<br/>'+'<br/><div><input type="checkbox" disabled="true" class="model_checkAll" name="checkmodel" >本模块全选</div>';
            		}
					
            }},
			{ "mDataProp": null,
				"fnRender":function(obj){
					var str = "";
					for(var i=0;i<obj.aData.CHILDRENS.length;i++){
						if(obj.aData.IS_AUTHORIZE !=null && obj.aData.IS_AUTHORIZE !=0){
							str +='<div class="col-md-6"><input type="checkbox" class="unChecked" name="permissionCheck" value="'+obj.aData.CHILDRENS[i].CODE+'">　'+obj.aData.CHILDRENS[i].NAME+'</div>';
						}else{
							str +='<div class="col-md-6"><input type="checkbox" class="unChecked"  disabled="true" name="permissionCheck" value="'+obj.aData.CHILDRENS[i].CODE+'">　'+obj.aData.CHILDRENS[i].NAME+'</div>';
						}
						
					}
				
				return str;
			}}
        ] 
    });
	
/*	$('#roleList').on('mousedown', '.fromLocationItem', function(e){
		var message = $(this).text();
		$('#role_filter').val(message.substring(0, message.indexOf(" ")));
		$('#roleList').hide();
	});*/
	
	/*点击下一页没有保存上页的数据*/
	var permission=[];
    $('#saveBtn').click(function(e){
        e.preventDefault();
        permission.splice(0,permission.length);	
        $("input[name='permissionCheck']").each(function(){
        	if($(this).prop('checked') == true){
        		permission.push($(this).val());
        	}
        });
        var rolename = $("#role_filter").val();
        var permissions = permission.toString();
        if(rolename != ""&&permission!=0){
        	$.get('/privilege/save?name='+rolename+'&permissions='+permissions, function(data){
        		$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
        		$("#role_filter").empty();
        		
        		queryRole();
        		$("input[name='permissionCheck']").each(function(){
                	$(this).prop('checked',false);	
                });
        		$("input[name='checkmodel']").each(function(){
                	$(this).prop('checked',false);	
                });
        		permission.splice(0,permission.length);
    		},'json');
        }else{
        	$.scojs_message('保存失败,当前没有选择岗位或者权限', $.scojs_message.TYPE_ERROR);
        }
       
    });
    $("#allCheck").on('click',function(){
    	$("input[name='permissionCheck']").each(function(){
    		if($(this).prop('disabled')!=true){
    			$(this).prop('checked',$("#allCheck").prop('checked'));	
    		}
        	
        });
    	$("input[name='checkmodel']").each(function(){
    		if($(this).prop('disabled')!=true){
    			$(this).prop('checked',$("#allCheck").prop('checked'));
    		}
        });
    });
    $("#eeda-table").on('click','.unChecked',function(){    	
    	var size=$("input[name='permissionCheck']").length;    
    	 if($("input[name='permissionCheck']:checked").length != size ){
    		 $("#allCheck").attr("checked",false);
    	 }else{
    		 $("#allCheck").prop('checked',true);
    		 
    		 
    	 }
    });
    $("#eeda-table").on('click','.model_checkAll',function(){
    	if($(this).prop("checked")==true){
    		$(this).parent().parent().next().find("input[name='permissionCheck']").prop("checked",true);
    	}else{
    		$(this).parent().parent().next().find("input[name='permissionCheck']").prop("checked",false);
    	}
    	var size=$("input[name='permissionCheck']").length;
        
	   	 if($("input[name='permissionCheck']:checked").length != size ){
	   		 $("#allCheck").attr("checked",false);
	   	 }else{
	   		 $("#allCheck").prop('checked',true);
	   		 
	   		 
	   	 }
    });
    $("#eeda-table").on('click','.unChecked',function(){
    	var size = $(this).parent().parent().find("input[name='permissionCheck']").length;
    	var checkedSize = $(this).parent().parent().find("input[name='permissionCheck']:checked").length
    	if(checkedSize != size){
    		$(this).parent().parent().parent().find("input[name='checkmodel']").prop("checked",false);
    	}else{
    		$(this).parent().parent().parent().find("input[name='checkmodel']").prop("checked",true);
    	}
    	
    });
});