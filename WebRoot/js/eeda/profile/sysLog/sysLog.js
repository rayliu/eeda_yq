define(['jquery','hui'], function ($,huiCont) {
    $(document).ready(function() {
        document.title = '系统日志 | '+document.title;
        
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
        
        var loginTable = eeda.dt({
            id: 'login_table',
            ajax:'/sysLog/getLoginLog',
            paging: true,
            serverSide: true,
            pageLength: 10,
            columns: [
                      { data: 'USER_NAME' },
                      { data: 'OFFICE_NAME' },
                      { data: 'IP' },
                      { data: 'CREATE_STAMP' }
                     ]
        });
        
        var operateTable = eeda.dt({
            id: 'operate_table',
            ajax:'/sysLog/getOperateLog',
            paging: true,
            pageLength: 10,
            serverSide: true, //不打开会出现排序不对 
            columns: [
                      { data: 'FORM_NAME' },
                      { data: 'ORDER_ID' },
                      { data: 'ACTION_TYPE',
                    	  "render": function(data, type, full, meta) {
                              if(data=='doUpdate')
                                  return "<p align='left'>修改</p>";
                              else if(data=='doAdd')
                            	  return "<p align='left'>新增</p>";
                              else if(data=='doDelete')
                            	  return "<p align='left'>删除</p>";
                              else
                            	  return '';
                          }  
                      },
                      { data: 'USER_NAME' },
                      { data: 'IP' },
                      { data: 'OFFICE_NAME' },
                      { data: 'CREATE_STAMP' }
                     ]
        });
        $('#login_table tfoot.search th').each( function (i, item) {
            var th = $('#login_table thead th').eq($(this).index());
            var title = th.text();
            var field_name = th.attr('field_name');
            if(title!="" && title!="操作")
                $(this).html('<input type="text" placeholder="过滤..." data-index="'+i+'" table_type="login" field_name="'+field_name+'" style="width: 100%;"/>');
        });
        $('#operate_table tfoot.search th').each( function (i, item) {
            var th = $('#operate_table thead th').eq($(this).index());
            var title = th.text();
            var field_name = th.attr('field_name');
            if(title!="" && title!="操作")
                $(this).html('<input type="text" placeholder="过滤..." data-index="'+i+'" table_type="operate" field_name="'+field_name+'" style="width: 100%;"/>');
        });
        $('#operate_table,#login_table').on('keyup', 'tfoot input', function () {
        	var table_type = $(this).attr("table_type");
        	if(table_type=='login'){
        		globalSearch('/sysLog/getLoginLog','#login_table',loginTable);
        	}else if(table_type=='operate'){
        		globalSearch('/sysLog/getOperateLog','#operate_table',operateTable);
        	}
        });
        
        //系统日志-特殊处理过的，不适用其他table
        var globalSearch = function(url,table_id,table){
            var query="";
            $(table_id+' tfoot input').each(function(index, el) {
            	var field_name = $(el).attr('field_name');
            	var value = "";
            	if(field_name=='sl.action_type'){
            		value = $(el).val();
            		if(value=='修改'||value=='修'){
            			value = 'doUpdate';
            		}else if(value=='新增'||value=='新'){
            			value = 'doAdd';
            		}else if(value=='删除'||value=='删'){
            			value = 'doDelete';
            		}
            	}else{
            		value = $(el).val();
            	}
                query+="&"+field_name+"_like="+value;
            });

            url = url+'?1=1'+query;
            table.ajax.url(url).load();
        }
        
    });
});