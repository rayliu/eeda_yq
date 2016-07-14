define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {

	var deletedTableIds=[];
   
    //------------事件处理
    var cargoTable = eeda.dt({
        id: 'depart_table',
        columns:[
            { "width": "30px",
                "render": function ( data, type, full, meta ) {
                	//return '<button type="button" class="delete btn btn-default btn-xs">删除</button> ';
                	return null;
                }
            },
            { "data": "TYPE", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="type" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "TRANS_TYPE", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="trans_type" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "LOAD_TYPE", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="load_type" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "CONTAINER_TYPE", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="container_type" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "CONTAINER_AMOUNT", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='1';
                    return '<input type="number" name="container_amount" value="'+data+'" class="form-control easyui-numberbox" data-options="max:0"/>';
                }
            },
            { "data": "CARGO_NAME", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<input type="text" name="cargo_name" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "PIECES", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='1';
                    return '<input type="text" name="pieces" value="'+data+'" class="form-control" />';
                }
            },
            { "data": "NET_WEIGHT", 
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='0';
                    return '<input type="text" name="net_weight" value="'+data+'" class="form-control" />';
                }
            }
        ]
    });


} );
});