define(['jquery', 'metisMenu', 'sb_admin', 'dataTables', 'validate_cn'], function ($) {

  var queryRole = function(){
  	document.title = '岗位权限查询 | '+document.title;
  	$.get('/privilege/roleList', function(data){
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
  	var privilege_table = $('#eeda-table').DataTable({
      	"processing": true,
        "searching": false,
        //"serverSide": true,
        "scrollX": true,
        //"scrollY": "300px",
        "scrollCollapse": true,
        "autoWidth": false,
        paging: false,
        //"aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "language": {
            "url": "/js/lib/datatables/i18n/Chinese.json"
        },
        "ajax": "/privilege/list",
        "columns": [
            { "data": "MODULE_NAME",
              "width": "25%", 
              "render": function ( data, type, full, meta ) {
                return '<h4>' + full.MODULE_NAME + '</h4>';
              }
            },
            { "data": null,
              "width": "75%", 
              "render": function ( data, type, full, meta ) {
                var str = "";
                  for(var i=0;i<full.CHILDRENS.length;i++){
                    
                  if(full.CHILDRENS[i].PERMISSION_CODE==null){
                    str +='<div class="col-md-6"><input type="checkbox" class="unChecked" style="cursor: default;" disabled="true" name="permissionCheck" value="'+full.CHILDRENS[i].CODE+'">　'+full.CHILDRENS[i].NAME+'</div>';                       
                  }else{
                    if(full.IS_AUTHORIZE != null && full.IS_AUTHORIZE != 0){
                      str +='<div class="col-md-6"><input type="checkbox" class="unChecked" style="cursor: default;" disabled="true" checked="true" name="permissionCheck" value="'+full.CHILDRENS[i].CODE+'">　'+full.CHILDRENS[i].NAME+'</div>';
                    }else{
                      str +='<div class="col-md-6"><input type="checkbox" class="unChecked" style="cursor: default;" disabled="true" name="permissionCheck" value="'+full.CHILDRENS[i].CODE+'">　'+full.CHILDRENS[i].NAME+'</div>';
                    }
                  }
                }
                
                return str;
              }
            }
          ]
        // "sAjaxSource": "",
        //   "aoColumns": [
        //       { "mDataProp": "MODULE_NAME","sWidth":"15%",
        //           "fnRender":function(obj){
        //             return '<h4>' + obj.aData.MODULE_NAME + '</h4>';
        //           }
        //       },
        //       { "mDataProp": null,
        //       	"fnRender":function(obj){
        //       		var str = "";
        //       		for(var i=0;i<obj.aData.CHILDRENS.length;i++){
              			
        //       			if(obj.aData.CHILDRENS[i].PERMISSION_CODE==null){
        //       				str +='<div class="col-md-6"><input type="checkbox" class="unChecked" style="cursor: default;" disabled="true" name="permissionCheck" value="'+obj.aData.CHILDRENS[i].CODE+'">　'+obj.aData.CHILDRENS[i].NAME+'</div>';               			 	 
  	     //           		}else{
  	     //           			if(obj.aData.IS_AUTHORIZE != null && obj.aData.IS_AUTHORIZE != 0){
  	     //           				str +='<div class="col-md-6"><input type="checkbox" class="unChecked" style="cursor: default;" disabled="true" checked="true" name="permissionCheck" value="'+obj.aData.CHILDRENS[i].CODE+'">　'+obj.aData.CHILDRENS[i].NAME+'</div>';
  	     //           			}else{
  	     //           				str +='<div class="col-md-6"><input type="checkbox" class="unChecked" style="cursor: default;" disabled="true" name="permissionCheck" value="'+obj.aData.CHILDRENS[i].CODE+'">　'+obj.aData.CHILDRENS[i].NAME+'</div>';
  	     //           			}
  	               		    
  	               			
  	     //           		}   
        //       		}
              		
        //       		return str;
        //       	}}
        //   ] 
      });
  	
  	$('#role_filter').on('change',function(e){
  		
  		var rolename = $('#role_filter').val();
  		
  		privilege_table.fnSettings().sAjaxSource = "/privilege/list?rolename="+rolename;
  		privilege_table.fnDraw(); 

  	});
      $('#roleForm').validate({
          rules: {
          	rolename: {
                 required: true
          	}
         	 },  	
  		 messages:{
  			 rolename:{
  				 required:"岗位不能为空"
  			 }
         },
         highlight: function(element) {
        	$(element).parent().removeClass('has-success').addClass('has-error');
        	
        },
        success: function(element) {
        	//element.parent().removeClass('has-error').addClass('has-success');
        }
          
   });	

  	
  });


});