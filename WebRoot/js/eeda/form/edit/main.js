define(['jquery'],function ($) {
        document.title = 'edit | ' + document.title;

        var form_define_json = JSON.parse($("#form_define").text());

        var order_id = $('#order_id').val();
        var module_form_id = form_define_json.ID;
        //获取form JSON  {params:JSON.stringify(dto)},
        $.post('/form/'+form_define_json.MODULE_ID+"-doGet-"+order_id,  function(data){
            console.log(data);
            fillFormData(data);
        });

        var fillFormData = function(data){
            // 开始遍历 
            for ( var p in data ){ // 方法 
                if( typeof ( data[p]) == " function " ){ 
                    //obj [ p ]() ; 
                } else { // p 为属性名称，obj[p]为对应属性的值 
                    var value = data[p]; 
                    var field_id = '#form_'+module_form_id+'-'+p.toLowerCase();
                    //console.log ( field_id +" = "+value ) ;
                    $(field_id).val(data [p]);// 根据ID 显示所有的属性 
                } 
            } 
            
        };

        $('#test').click(function(event) {
            $.post('/form/'+form_define_json.MODULE_ID+"-doGet-"+order_id,  function(data){
                console.log(data);
                fillFormData(data);
            });
        });
});
