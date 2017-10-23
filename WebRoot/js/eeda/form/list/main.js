define(['jquery', '../btns'], function ($) {
        document.title = '查询 | ' + document.title;

        var module_id=$('#module_id').val();
        var order_id=$('#order_id').val();
        var field_list_json = JSON.parse($('#field_list_json').text());

        var colsSetting = [
            {
                data: null,
                render: function ( data, type, full, meta ) {
                  return '<input type="checkBox" name="checkBox" style="margin-right:5px;">';
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

        //操作按钮
        var btnCol = {
            data: null,
            render: function ( data, type, full, meta ) {

              return '<a style="text-decoration:none" class="ml-5" href="/form/'+module_id+'-edit-'+data.ID+'" title="编辑"><i class="Hui-iconfont">&#xe6df;</i></a>'
                    +'<a style="text-decoration:none" class="ml-5" href="/form/'+module_id+'-doDelete-'+data.ID+'" title="删除"><i class="Hui-iconfont">&#xe6e2;</i></a> ';
            }
        };
        colsSetting.push(btnCol);

        var dataTable = eeda.dt({
            id: 'list_table',
            paging: true,
            serverSide: true,
            columns: colsSetting
        });

        var url = '/form/'+$('#form_id').val()+'-doQuery';


        console.log('doQuery.................');
        dataTable.ajax.url(url).load();

        $('#list_table tfoot.search th').each( function (i, item) {
            var th = $('#list_table thead th').eq($(this).index());
            var title = th.text();
            var field_name = th.attr('field_name');
            if(title!="")
                $(this).html('<input type="text" placeholder="过滤..." data-index="'+i+'" field_name="'+field_name+'" style="width: 100%;"/>');
        });

        $('#list_table').on( 'keyup', 'tfoot input', function () {
            globalSearch();
        });

        var globalSearch = function(){
            var query="";
            $('#list_table tfoot input').each(function(index, el) {
                query+="&"+$(el).attr('field_name')+"_like="+$(el).val();
            });

            var url = '/form/'+$('#form_id').val()+'-doQuery?1=1'+query;
            dataTable.ajax.url(url).load();
        }
});
