define(['jquery', 'metisMenu', 'sb_admin', 'dataTablesBootstrap', 'sco'], function ($, metisMenu) {
    $(document).ready(function() {

        document.title = '登陆用户列表 | '+document.title;
        $("#breadcrumb_li").text('登陆用户列表');

        $.Huitab = function(tabBar,tabCon,class_name,tabEvent,i){
        	var $tab_menu = $(tabBar);
        	// 初始化操作
        	$tab_menu.removeClass(class_name);
        	$(tabBar).eq(i).addClass(class_name);
        	$(tabCon).hide();
        	$(tabCon).eq(i).show();
        	  
        	$tab_menu.bind(tabEvent,function(){
        	  	$tab_menu.removeClass(class_name);
        	      $(this).addClass(class_name);
        	      var index=$tab_menu.index(this);
        	      $(tabCon).hide();
        	      $(tabCon).eq(index).show();
        	});
        };
        $.Huitab("#tab_demo .tabBar span","#tab_demo .tabCon","current","click","0");

        var dataTable = eeda.dt({
            id:'user_table',
            "ajax": "/loginUser/listUser",
            paging:true,
            lengthChange:false,
            "columns": [
                { "data": "USER_NAME", "width": "20%",
                    "render":function(data, type, full, meta){
                    	return data;
                    }
                },
                { "data": "C_NAME", "width": "10%" },
                { "data": "POSITION_NAME", "width": "10%"},
                { "data": "PASSWORD_HINT","width": "15%",visible:false},
                { 
                    "data": null, 
                    "width": "5%",
                    "render": function(data, type, full, meta) {
                        var str = "<nobr>";

                        if(this_role != 'admin' && full.ROLE_CODE == 'admin' ){
                        	str = "无权限";
                        }else{
                        	if(User.update){
                            	str += "<a class='btn  btn-primary btn-sm editbutton' href='/loginUser/edit?id="+full.ID+"'>"+
                                "<i class='fa fa-edit'> </i>编辑</a> ";
                            }
                            
                            if(User.stop){
                            	if(full.IS_STOP != 1){
                                    str = str +"<button type='button' id='"+full.ID+"' class='btn  btn-danger btn-sm delete'>"+
                                                 "<i class='fa fa-trash-o fa-edit'></i>停用</button>";
                                }else{
                                    str = str + "<button  type='button' id='"+full.ID+"' class='btn btn-primary btn-sm delete'>"+
                                                 "<i class='fa fa-trash-o fa-edit'></i>启用 </button>";
                                }
                            }
                        }
                        return str+"</nobr>"
                    }
               }
           ]
        });
        
        $('#user_table').on('click','.delete',function(){
        	var id = $(this).attr('id');
        	$.post('/loginUser/del',{id:id},function(data){
        		if(data){
        			 $.scojs_message('停用成功', $.scojs_message.TYPE_OK);
        			 var url = "/loginUser/listUser";
        			 dataTable.ajax.url(url).load();
        		}
        	}).fail(function(){
        		 $.scojs_message('操作失败', $.scojs_message.TYPE_FALSE);
        	});
        });
        


    });
});