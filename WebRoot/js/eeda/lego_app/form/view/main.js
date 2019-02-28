define(['jquery', 'template', 'mui'],
    function ($, template) {
        var module_id=$('#module_id').val();
        var order_id = $('#order_id').val();
        console.log('edit.....');

        var url = '/app/form/'+module_id+'-doGet-'+order_id;

        console.log('app doGet order.................');
        var form_fields = $("#form_fields");
        var app_form_list = $("#app_form_list");
        form_fields.empty();
        app_form_list.empty();
        $.get(url, function(result){
            console.log(result);
            result.forEach(element => {
                var type = element.DISPLAY_TYPE;
                if(type=='field'){
                    var str = template('form_input_field', { 
                        "value":element.VALUE,
                        "display_name":element.DISPLAY_NAME
                    }); 
                    form_fields.append(str); 
                }else{
                    var str = template('detail_list_field', { 
                        "value":element.VALUE,
                        "display_name":element.DISPLAY_NAME
                    }); 
                    app_form_list.append(str); 
                }
            });
        });  
    });
