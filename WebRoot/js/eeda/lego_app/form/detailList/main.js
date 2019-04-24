define(['jquery', 'template', 'mui', '../btns'], function ($, template) {

    var module_id=$('#module_id').val();
    var form_id=$('#form_id').val();
    var order_id=$('#order_id').val();
    var detail_field_id=$('#detail_field_id').val();
    var field_col_name=$('#field_col_name').val();

    //操作按钮
    var btnCol = {
        data: null,
        render: function ( data, type, full, meta ) {
            return '<a class="btn btn-xs" style="text-decoration:none" class="ml-5" href="/form/'+module_id+'-edit-'+data.ID+'" title="编辑"><i class="Hui-iconfont">&#xe6df;</i></a>'
            +' <a class="btn btn-xs" style="text-decoration:none" href="javascript:;" class="ml-5 delete" module_id='+module_id+' id='+data.ID+' title="删除"><i class="Hui-iconfont">&#xe6e2;</i></a> ';
        }
    };

    var url = '/app/form/detailTable/'+form_id+'-'+order_id+'-'+detail_field_id;

    console.log('app doQuery.................');
    var app_form_list = $("#app_form_list");
    $.get(url, function(result){
        console.log(result);
        var data_list = result.DATA_LIST;
        var define_list = result.TABLE_DEFINE_LIST[0].DISPLAY_FIELD_LIST;
        data_list.forEach(data => {
            var field_html_arr="";

            for(let key in data){
                var field_value=data[key];
                var field_name="";
                for (let index = 0; index < define_list.length; index++) {
                    const field = define_list[index];
                    if(field.FIELD_NAME.toUpperCase()==key){
                        field_name=field.FIELD_DISPLAY_NAME;
                        var field_html='<h5>'+field_name+': '+field_value+'</h5>';
                        field_html_arr+=field_html;
                    }
                }
            }
            
            var li_html='<li class="mui-table-view-cell">'
                            +field_html_arr+
                        '</li>';
            app_form_list.append(li_html);
        });
    });

});
