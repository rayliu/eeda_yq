define(['jquery', 'layer', 'layui','hui_admin', '../btns'], function ($, layer, layui) {
    $(document).ready(function(){    
        document.title = '查询 | ' + document.title;

        var module_id=$('#module_id').val();
        var order_id=$('#order_id').val();
        var field_list_json = JSON.parse($('#field_list_json').text());
        var list_action_json = JSON.parse($('#list_action_json').text());

        var colsSetting = [
            {
                data: null,
                render: function ( data, type, full, meta ) {
                  return '<input type="checkbox" name="checkbox" style="margin-right:5px;">';
                }
            }
        ];

        for (var i=0;i<field_list_json.length;i++){
            var field = field_list_json[i];
            var visible = field.LISTED=="Y"?true:false;
            var col={
                data: ('f'+field.ID+'_'+field.FIELD_NAME).toUpperCase(),
                visible: visible,
                width: field.COL_WIDTH
            };
            //console.log(col);
            colsSetting.push(col);
        };

        //操作按钮
        var btn_visible = true;
        if(module_id==206 || module_id==207){
            var btn_visible =false;
        }
        var btnCol = {
            data: null,
            visible: btn_visible,
            render: function ( data, type, full, meta ) {
        		return '<a class="btn btn-xs" style="text-decoration:none" class="ml-5" href="/form/'+module_id+'-edit-'+data.ID+'" title="编辑"><i class="Hui-iconfont">&#xe6df;</i></a>'
                +' <a class="btn btn-xs delete" style="text-decoration:none" href="javascript:;" class="ml-5 delete" module_id='+module_id+' id='+data.ID+' title="删除"><i class="Hui-iconfont">&#xe6e2;</i></a> ';
            }
        };
        if(field_list_json[0].CUSTOM_SEARCH!="Y"){
        	colsSetting.push(btnCol);
        }

        var dataTable = eeda.dt({
            id: 'list_table',
            paging: true,
            serverSide: true,
            columns: colsSetting,
            drawCallback:function(){
                redisplayCheckbox();
            }
        });

        var url = '/form/'+$('#form_id').val()+'-doQuery';


        console.log('doQuery.................');
        dataTable.ajax.url(url).load();


        $('#list_table tfoot.search th').each( function (i, item) {
            var th = $('#list_table thead th').eq($(this).index());
            var title = th.text();
            var field_name = th.attr('field_name');
            if(title!="" && title!="操作")
                $(this).html('<input type="text" placeholder="过滤..." data-index="'+i+'" field_name="'+field_name+'" style="width: 100%;"/>');
        });

        $('#advanced_search_btn').click(function(){
            var el = $("#search_div");
            if (el.is(':visible')) {//如果 为可见,:visible是可见的意思,相关用法还有:hidden(隐藏),:first(第一个),:last(最后一个)  
                el.slideUp();//隐藏  
            } else {  
                el.slideDown();//显示  
            }  
        });

        $('#search_btn').click(function(){
            advanceSearch();
        });

        var advanceSearch = function(){
            var query="";
            $('#search_form input').each(function(index, el) {
                var val = $(el).val()
                var fiedl_name = $(el).attr('name');
                if(val){
                    if(fiedl_name.indexOf('_begin_time') >0 || fiedl_name.indexOf('_end_time') >0){
                        query+="&"+fiedl_name+"="+val;
                    }else{
                        query+="&"+fiedl_name+"_like="+val;
                    }
                    
                }
            });

            var url = '/form/'+$('#form_id').val()+'-doQuery?1=1'+query;
            dataTable.ajax.url(url).load();
        }

        $('#reset_btn').click(function(){
            $('#search_form')[0].reset();
            advanceSearch();
        });
       
        $('article').on('keyup', 'tfoot input', function () {
            quickSearch();
        });
        
        //删除
        $('#list_table').on( 'click', '.delete', function () {
        	var module_id = $(this).attr("module_id");
        	var id = $(this).attr("id");
        	if(confirm("您确定删除该单据吗？")){
                $.post('/form/'+module_id+'-doDelete-'+id,function(data){
                	if(data.result){
                		var tr = $(this).closest('tr');
                		dataTable.row(tr).remove().draw();
                		alert("删除成功");
                	}else{
                		alert("删除失败");
                	}
                });
        	}
        });

        var quickSearch = function(){
            var query="";
            $('article tfoot input').each(function(index, el) {
                if($(el).val()){
                    query+="&"+$(el).attr('field_name')+"_like="+$(el).val();
                }
            });

            var url = '/form/'+$('#form_id').val()+'-doQuery?1=1'+query;
            dataTable.ajax.url(url).load();
        }

         //checkbox事件响应
         $('body').on('click', 'input[type=checkbox]', function(event) {
            var tr=$(this).closest('tr').index();
            var checked = $(this).prop('checked');
            console.log('input checked='+checked);
            for (var i=0;i<list_action_json.length;i++){
                var field = list_action_json[i];
                if(field.TYPE=="list_event_mark_row" && checked){
                    var event_json = field.EVENT_JSON;
                    var event_json_obj = JSON.parse(event_json);
                    var children = event_json_obj[0].children;
                    $(children).each(function(index, item){
                        var event_action_setting=item.event_action_setting;
                        var checked_var=event_action_setting.checked_var;
                        //把列表的列表结构也要传过去，否则下页不知道如何取字段名
                        var variable_define = sessionStorage.getItem(checked_var+'_define');
                        if(!variable_define){
                            var checked_var_define=$('#field_list_json').val();
                            sessionStorage.setItem(checked_var+'_define', checked_var_define);
                        }

                        var checked_var_option=event_action_setting.checked_var_option;
                        //往sessionStorage写变量
                        var variable = sessionStorage.getItem(checked_var);
                        if(!variable){
                            variable = [];
                        }else{
                            variable=JSON.parse(variable);
                        }
                            
                        var data = dataTable.row(tr).data();
                        console.log(data);
                        variable.push(data);
                        sessionStorage.setItem(checked_var, JSON.stringify(variable));
                    });
                    break;
                }
                else if(field.TYPE=="list_event_unmark_row" && !checked){
                    var field = list_action_json[i];
                    var event_json = field.EVENT_JSON;
                    var event_json_obj = JSON.parse(event_json);
                    var children = event_json_obj[0].children;
                    $(children).each(function(index, item){
                        var event_action_setting=item.event_action_setting;
                        var checked_var=event_action_setting.checked_var;
                        var checked_var_option=event_action_setting.checked_var_option;
                        //从sessionStorage变量中， 通过ID 删除 记录
                        var variable = sessionStorage.getItem(checked_var);
                        if(!variable){
                            variable = [];
                        }else{
                            variable=JSON.parse(variable);
                        }
                            
                        var data = dataTable.row(tr).data();
                        var delete_id = data.ID;
                        console.log('delete_id='+delete_id+', ID='+data.ID);
                        var new_arr=[];
                        $(variable).each(function(index, item){
                            if(delete_id == item.ID) return true;//continue
                            new_arr.push(item);
                        });
                        sessionStorage.setItem(checked_var, JSON.stringify(new_arr));
                    });
                    break;
                }
            };
         });

         var redisplayCheckbox=function(){
            for (var i=0;i<list_action_json.length;i++){
                var field = list_action_json[i];
                if(field.TYPE=="list_event_mark_row"){
                    var event_json = field.EVENT_JSON;
                    var event_json_obj = JSON.parse(event_json);
                    var children = event_json_obj[0].children;
                    $(children).each(function(index, item){
                        var event_action_setting=item.event_action_setting;
                        var checked_var=event_action_setting.checked_var;
                        var variable = sessionStorage.getItem(checked_var);
                        if(variable){
                            var var_arr=JSON.parse(variable);
                            $(var_arr).each(function(index, row){
                                var row_id = row.ID;
                                console.log(row_id);
                                $('#'+row_id+' td:eq(0) input').prop('checked', true);
                            });
                        }
                    });
                }
            }
         }
    });//end of doc ready
});
