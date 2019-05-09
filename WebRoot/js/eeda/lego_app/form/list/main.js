define(['jquery', 'template', 'mui', 'mui_loading', '../btns'], function ($, template, mui) {

    // mui.plusReady(function() {
        mui.showLoading("正在加载.."); 
        document.title = '查询 | ' + document.title;

        var module_id=$('#module_id').val();
        var order_id=$('#order_id').val();
        var field_col_name=$('#field_col_name').val();

       
        var url = '/app/form/'+$('#form_id').val()+'-doQuery';

        console.log('app doQuery.................');
        var app_form_list = $("#app_form_list");
        $.get(url, function(result){
            console.log(result);
            result.data.forEach(element => {
                var str = template('app_form_list_item', { 
                    "value":element[field_col_name.toUpperCase()],
                    "link":"/app/form/"+module_id+"-edit-"+element['ID']
                }); 
                app_form_list.append(str); 
            });
            mui.hideLoading();
        });

        var globalSearch = function(){
            mui.showLoading("正在加载..");
            var query="";
            var value=$('#searchInput').val();

            var search_url = url+'?s='+value;
            $.get(search_url, function(result){
                console.log(result);
                app_form_list.empty();
                result.data.forEach(element => {
                    var str = template('app_form_list_item', { 
                        "value":element[field_col_name.toUpperCase()],
                        "link":"/app/form/"+module_id+"-edit-"+element['ID']
                    }); 
                    app_form_list.append(str); 
                });
                mui.hideLoading();
            });
        }
        // 搜索事件,获取搜索关键词
        $('#searchInput').keyup(function(e){
            if(e.keyCode == 13) {//用户点击回车的事件号为13
                globalSearch();
            }
        });
        
        
    // });
});
