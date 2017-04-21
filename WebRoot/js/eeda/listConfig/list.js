define(['jquery'], function ($) {
    $(document).ready(function() {
        var config_dataTable = eeda.dt({
            id: 'eeda_cols_config_table',
            serverSide: false,
            info: false,
            columns:[
                { "width": "80px",
                    "render": function ( data, type, full, meta ) {
                      return '<button type="button" class="delete btn table_btn btn-default btn-xs">'+
                        '<i class="fa fa-trash-o"></i> 删除</button>';
                    }
                },
                { "data": "TITLE", "width":"120px"},
                { "data": "WIDTH", "width":"120px"}
            ]
        });


        $('#list_config').click(function(event) {
            var url = "/listConfig/list?module="+creator;

            config_dataTable.ajax.url(url).load();

            $('#listConfigModal').modal('show');
        });


    });//$(document).ready
});