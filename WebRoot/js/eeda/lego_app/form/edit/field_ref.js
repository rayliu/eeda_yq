define(['jquery'], function ($) {

    function bindTap(el){
        el.addEventListener('tap', function(){
            var field_name = $(el).attr('name');
            $('#popover').attr('field_name', field_name);

            var target_form_id = $(el).attr('target_form');
            var target_field = $(el).attr('target_field_name');
            var target_field_arr = target_field.split(",");
            $.get('/form/'+target_form_id+'-doQuery?target_field='+target_field+'&like_str=', function(dto){
                console.log(dto);
                var drop_list=$('#popover ul');
                
                drop_list.empty();

                var data = dto.data;

                for(var i = 0; i < data.length; i++){
                    var display_cols = "";
                    target_field_arr.forEach(element => {
                        display_cols += " "+data[i][element.toUpperCase()];
                    });
                    var html= "<li class='mui-table-view-cell' target_field='"+data[i][target_field.toUpperCase()]
                            +"' id='"+data[i].ID+"' data='"+JSON.stringify(data[i])+"'><a href='#'>"+display_cols+"</a></li>";

                    // var html= "<li><a tabindex='-1' class='drop_list_item' target_field='"+data[i][target_field.toUpperCase()]
                    //         +"' id='"+data[i].ID+"' data='"+JSON.stringify(data[i])+"'>"+display_cols+"</a></li>";

                    drop_list.append(html);
                }
            
                mui('#popover').popover('toggle', el);
            });
            
        });
    }

    $('#popover ul').on('click', 'a', function(e){
        var item = $(this).closest('li');
        var id = item.attr('id');
        var value = item.text();
        console.log(item);
       
        var inputField_name = $('#popover').attr('field_name');
        var inputField = $("[name="+inputField_name+"]");
        // inputField.val(value);
        //处理引用字段
        var item_list_str = inputField.attr('item_list');
        var item_list = JSON.parse(item_list_str);
        var data_str = item.attr('data');
        var data= JSON.parse(data_str);
        $.each(item_list, function(index, item) {
            var from_name = item.FROM_FIELD_NAME;
            var to_name = item.TO_FIELD_NAME;
            $("input[name*='"+to_name+"']").val(data[from_name.toUpperCase()]);
        });
        mui('#popover').popover('toggle');
    });
    
    return{
        bindTap: bindTap
    }
});