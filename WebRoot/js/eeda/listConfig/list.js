define(['jquery', 'dataTablesBootstrap'], function ($) {
    $(document).ready(function() {
        
        var config_dataTable = eeda.dt({
            id: 'eeda_cols_config_table',
            serverSide: true,
            info: false,
            columns:[
                { "data": "IS_SHOW", "width": "30px",
                    "render": function ( data, type, full, meta ) {
                        if(data=="Y"){
                            return '<input type= "checkBox" name="checkBox_list_config" checked>';
                        }else{
                            return '<input type="checkBox" name= "checkBox_list_config">';
                        }
                    }
                },
                { "data": "COL_DISPLAY_NAME", "width":"120px"},
                { "data": "COL_FIELD", "width":"120px"}
            ]
        });


        $('#list_config').click(function(event) {
            var module_path = window.location.pathname;


            var url = "/listConfig/list?module_path="+module_path;

            config_dataTable.ajax.url(url).load();

            $('#listConfigModal').modal('show');
        });

        $('#listConfigConfirmBtn').click(function(event) {
            var arr = config_dataTable.data();
            var newArr = []
            var checkBoxArr =$('input[name=checkBox_list_config]');
            $.each(arr, function(index, el) {
                if($(checkBoxArr[index]).prop('checked')){
                    el.IS_SHOW = "Y";
                }else{
                    el.IS_SHOW = "N";
                }
                newArr.push(el);
            });
            var obj={
                arr:newArr
            }
            var str_JSON = JSON.stringify(obj);
            $.post('/listConfig/save', {data: str_JSON}, function(data, textStatus, xhr) {
                if(data=='OK'){
                    $('#listConfigModal').modal('hide');
                    window.location.reload();
                }else{
                    alert('出错，请刷新页面。');
                }
            });
        });
    });//$(document).ready
});