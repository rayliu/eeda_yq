define(['jquery','hui'], function ($,huiCont) {
    $(document).ready(function() {
        document.title = '系统API | '+document.title;
        
        // $("body").on("click",".delete",function(){
		// 	var  self = $(this);
		// 	var id = $(this).attr("id")
        // 	layer.confirm('确认要删除吗？',function(){
	    //     	$.post("/webadmin/java/delete",{id:id},function(data){
	    //     		if(data.result){
	    //     			layer.msg('删除成功',{icon:1});
	    //     		}else{
	    //     			layer.msg('删除失败',{icon:2});
	    //     		}
	    //     	});
	    //     });
			
		// });
		
		// //标题模糊查询
		// $("#searchBtn").click(function(){
		// 	var chooseSelect = $("#chooseSelect").val();
		// 	var title="";
		// 	if("title"==chooseSelect){
		// 		title =$("#title").val()
		// 	}
		// 	table.reload('jfa_table',{url:'/webadmin/java/getList',page:{curr: 1},where:{input:title}});
		// });
		
		// form.on('select(chooseSelect)',function(data){
		// 	if("title"==data.value){
		// 		$("#title").show();
		// 	}
		// });
        var eedaTable = eeda.dt({
            id: 'eeda_table',
            ajax:'/webadmin/java/getList',
            paging: true,
            pageLength: 10,
            serverSide: true, //不打开会出现排序不对 
            columns: [
                      { data: 'NAME' },
                      { data: 'URL' },
                      { data: 'TYPE'},
                      { data: 'SCHEDULE' },
                      { data: 'CODE_LENGTH' },
                      { data: 'CREATOR_NAME' },
                      { data: 'CREATE_TIME' },
                      { data: null, 
                        "render": function ( data, type, full, meta ) {
                          var str = '<a class="btn  btn-primary btn-sm" href="/webadmin/java/edit?id='+full.ID+'">编辑</a>'+
                                ' <button id="'+full.ID+'" class="btn btn-sm btn-danger">删除</button>';
                          return str;
                        }
                      }
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