define(['jquery', 'sco'], function ($) {
        //按钮事件响应
        $('button').click(function(event) {
            event.preventDefault();
            var btn = $(this);
            var btn_id = btn.attr('id');
            console.log('['+btn_id+'] btn click:');

            var module_id = $('#module_id').val();
            var order_id = $('#order_id').val();
            var btn_id = btn_id.split('-')[1].split('_')[1];

            $.post('/form/'+module_id+'-click-'+btn_id, function(events){
                console.log(events);
                if(events){
                    for (var i = 0; i < events.length; i++) {
                        var event = events[i];
                        if(event.TYPE == "open"){
                            var url = '/form/'+event.OPEN.MODULE_ID+'-add';
                            if(event.OPEN.OPEN_TYPE = 'newTab'){
                                window.open(url);
                            }else if(event.OPEN.OPEN_TYPE = 'self'){
                                window.location.href=url;
                            }else{
                                window.open(url);
                            }
                        }else if(event.TYPE == "save"){
                            var $form = $("#module_form");
                            var data = getFormData($form);
                            console.log('save action....');
                            console.log(data);
                            if(order_id==-1){
                                doAdd(data);
                            }else{
                                doUpdate(data);
                            }
                        }
                    }
                }
            });

            return false;
        });

        function getFormData($form){
            var unindexed_array = $form.serializeArray();
            var indexed_array = {};

            $.map(unindexed_array, function(n, i){
                indexed_array[n['name']] = n['value'];
            });

            return indexed_array;
        }


        function doAdd(data){
            $.post('/form/'+data.module_id+'-doAdd', {data: JSON.stringify(data)}, function(dto){
                if(dto){
                    var url = '/form/'+data.module_id+'-edit-'+dto.ID;
                    window.location.href=url;
                }
            });
        }

        function doUpdate(data){
            $.post('/form/'+data.module_id+'-doUpdate', {data: JSON.stringify(data)}, function(dto){
                if(dto){
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                }else{
                    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                }
            });
        }
});
