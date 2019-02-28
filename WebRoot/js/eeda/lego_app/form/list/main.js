define(['jquery', 'template', 'mui', '../btns'], function ($, template) {

        
        
        document.title = '查询 | ' + document.title;

        var module_id=$('#module_id').val();
        var order_id=$('#order_id').val();
        var field_col_name=$('#field_col_name').val();

        //操作按钮
        var btnCol = {
            data: null,
            render: function ( data, type, full, meta ) {
        		return '<a class="btn btn-xs" style="text-decoration:none" class="ml-5" href="/form/'+module_id+'-edit-'+data.ID+'" title="编辑"><i class="Hui-iconfont">&#xe6df;</i></a>'
                +' <a class="btn btn-xs" style="text-decoration:none" href="javascript:;" class="ml-5 delete" module_id='+module_id+' id='+data.ID+' title="删除"><i class="Hui-iconfont">&#xe6e2;</i></a> ';
            }
        };

        var url = '/app/form/'+$('#form_id').val()+'-doQuery';

        console.log('app doQuery.................');
        var app_form_list = $("#app_form_list");
        $.get(url, function(result){
            console.log(result);
            result.data.forEach(element => {
                var str = template('app_form_list_item', { 
                    "value":element[field_col_name.toUpperCase()],
                    "link":"/app/form/"+module_id+"-view-"+element['ID']
                }); 
                app_form_list.append(str); 
            });
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
