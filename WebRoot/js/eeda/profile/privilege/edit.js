$(document).ready(function() {
  $('#menu_sys_profile').addClass('active').find('ul').addClass('in');
  	var rolename =$("#role_filter").val();
  	if(rolename.indexOf("管理员")>=0){
  		$("#allCheck").prop('disabled',true);
  	}
  	var allLength=0;
  	var checkAllLength=0;
	var privilege_table = $('#eeda-table').dataTable({
    	"bFilter" : false,	
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"bPaginate": false,
    	"bLengthChange": false,
    	"bInfo": false,
    	"bStateSave":true,
    	//"sPaginationType": "bootstrap",
        "iDisplayLength": 25,
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/privilege/list?rolename="+rolename,
        "aoColumns": [
			{ "mDataProp": null,"sWidth":"15%",
				"fnRender":function(obj){
					var parentCheck=0;
					for(var i=0;i<obj.aData.CHILDRENS.length;i++){					
						if(obj.aData.CHILDRENS[i].PERMISSION_CODE!=null){
							parentCheck+=1;
							checkAllLength+=1;
			       		} 
					}
					var is_disabled = rolename.indexOf("管理员");
					
					if(is_disabled<0){
						if(parentCheck==obj.aData.CHILDRENS.length){
							if(obj.aData.IS_AUTHORIZE !=null && obj.aData.IS_AUTHORIZE !=0){
								return obj.aData.MODULE_NAME+'<br/>'+'<br/><div><input type="checkbox" class="model_checkAll" checked="true" name="checkmodel" >本模块全选</div>';
							}else{
								return obj.aData.MODULE_NAME+'<br/>'+'<br/><div><input type="checkbox" class="model_checkAll" name="checkmodel" >本模块全选</div>';
							}
							
						}else{
							if(obj.aData.IS_AUTHORIZE !=null && obj.aData.IS_AUTHORIZE !=0){
								return obj.aData.MODULE_NAME+'<br/>'+'<br/><div><input type="checkbox" class="model_checkAll" name="checkmodel" >本模块全选</div>';
							}else{
								return obj.aData.MODULE_NAME+'<br/>'+'<br/><div><input type="checkbox" class="model_checkAll" disabled="true" name="checkmodel" >本模块全选</div>';
							}
							
						}
					}else{
						if(parentCheck==obj.aData.CHILDRENS.length){
							if(obj.aData.IS_AUTHORIZE !=null && obj.aData.IS_AUTHORIZE !=0){
								return obj.aData.MODULE_NAME+'<br/>'+'<br/><div><input type="checkbox" class="model_checkAll" style="cursor: default;" disabled="true" checked="true" name="checkmodel" >本模块全选</div>';
							}else{
								return obj.aData.MODULE_NAME+'<br/>'+'<br/><div><input type="checkbox" class="model_checkAll" style="cursor: default;" disabled="true"  name="checkmodel" >本模块全选</div>';
							}
						}else{
							return obj.aData.MODULE_NAME+'<br/>'+'<br/><div><input type="checkbox" class="model_checkAll" style="cursor: default;" disabled="true" name="checkmodel" >本模块全选</div>';
							
						}
					}
					
					
            }},
			{ "mDataProp": null,
				"fnRender":function(obj){
					var str = "";
					var is_disabled = rolename.indexOf("管理员");
					
					for(var i=0;i<obj.aData.CHILDRENS.length;i++){
						allLength+=1;
						
						if(is_disabled<0){
							if(obj.aData.CHILDRENS[i].PERMISSION_CODE==null){
								if(obj.aData.IS_AUTHORIZE !=null && obj.aData.IS_AUTHORIZE !=0){
									str +='<div class="col-md-6"><input type="checkbox" class="unChecked"  name="permissionCheck" value="'+obj.aData.CHILDRENS[i].CODE+'">　'+obj.aData.CHILDRENS[i].NAME+'</div>';
								}else{
									str +='<div class="col-md-6"><input type="checkbox" class="unChecked" disabled="true"  name="permissionCheck" value="'+obj.aData.CHILDRENS[i].CODE+'">　'+obj.aData.CHILDRENS[i].NAME+'</div>';
								}
								
				       		}else{
				       			if(obj.aData.IS_AUTHORIZE !=null && obj.aData.IS_AUTHORIZE !=0){
				       				str +='<div class="col-md-6"><input type="checkbox" class="unChecked" style="cursor: default;" checked="true" name="permissionCheck" value="'+obj.aData.CHILDRENS[i].CODE+'">　'+obj.aData.CHILDRENS[i].NAME+'</div>';
								}else{
									str +='<div class="col-md-6"><input type="checkbox" class="unChecked" style="cursor: default;" disabled="true"  name="permissionCheck" value="'+obj.aData.CHILDRENS[i].CODE+'">　'+obj.aData.CHILDRENS[i].NAME+'</div>';
								}
				       		    		
				       		}  
						}else{
							if(obj.aData.CHILDRENS[i].PERMISSION_CODE==null){
								if(obj.aData.IS_AUTHORIZE !=null && obj.aData.IS_AUTHORIZE !=0){
									str +='<div class="col-md-6"><input type="checkbox" class="unChecked" style="cursor: default;" disabled="true" name="permissionCheck" value="'+obj.aData.CHILDRENS[i].CODE+'">　'+obj.aData.CHILDRENS[i].NAME+'</div>';
								}
				       		}else{
				       			
				       			if(obj.aData.IS_AUTHORIZE !=null && obj.aData.IS_AUTHORIZE !=0){
				       				str +='<div class="col-md-6"><input type="checkbox" class="unChecked" style="cursor: default;" disabled="true" checked="true" name="permissionCheck" value="'+obj.aData.CHILDRENS[i].CODE+'">　'+obj.aData.CHILDRENS[i].NAME+'</div>';
								}else{
									str +='<div class="col-md-6"><input type="checkbox" class="unChecked" style="cursor: default;"  disabled="true" name="permissionCheck" value="'+obj.aData.CHILDRENS[i].CODE+'">　'+obj.aData.CHILDRENS[i].NAME+'</div>';
								}
				       		    		
				       		}  
						}
						
						
					}
					
				
				return str;
			}}
        ] 
    });
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
	       
        	$.get('/privilege/update?name='+rolename+'&permissions='+permissions, function(data){
        		//保存成功返回到当前用户的权限页面
        		$.scojs_message('更新成功', $.scojs_message.TYPE_OK);
        		/*$("#saveBtn").attr("disabled",true);*/
        		permission.splice(0,permission.length);
    		},'json');
        	 
	       
	    });
	    var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable"  style="display:none">'+
				    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
				    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
				    '</div>';
	    $('body').append(alerMsg); 
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

