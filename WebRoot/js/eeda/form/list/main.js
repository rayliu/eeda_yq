define(['jquery', '../btns'], function ($) {
        document.title = '查询 | ' + document.title;

        var module_id=$('#module_id').val();
        var order_id=$('#order_id').val();
        var field_list_json = JSON.parse($('#field_list_json').text());

        var colsSetting = [
            {
                data: null,
                render: function ( data, type, full, meta ) {
                  return '<input type="checkBox" name="checkBox" style="margin-right:5px;"> '
                    +'<a href="/form/'+module_id+'-doDelete-'+data.ID+'" class="btn table_btn  btn-xs" ><i class="fa fa-trash-o"></i></a> '
                    +'<a href="/form/'+module_id+'-edit-'+data.ID+'" class="btn table_btn  btn-xs" ><i class="fa fa-edit"></i></a>';
                }
            }
        ];

        for (var i=0;i<field_list_json.length;i++){
            var field = field_list_json[i];
            var visible = field.LISTED=="Y"?true:false;
            var col={
                data: ('f'+field.ID+'_'+field.FIELD_NAME).toUpperCase(),
                visible: visible
            };
            //console.log(col);
            colsSetting.push(col);
        };

        var dataTable = eeda.dt({
            id: 'list_table',
            paging: true,
            serverSide: true,
            columns: colsSetting
        });

        var url = '/form/'+$('#form_id').val()+'-doQuery';


        console.log('doQuery.................');
        dataTable.ajax.url(url).load();

        
});
