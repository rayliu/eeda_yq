define(['jquery', '../btns', '../add/detail_table'],function ($, btnCont, tableCont) {
        document.title = '编辑 | ' + document.title;

        var form_define_json = JSON.parse($("#form_define").text());

        var order_id = $('#order_id').val();
        var module_form_id = form_define_json.ID;
        console.log('edit.....');

        //这里用回调保证 先应用了 dataTable setting,  再取数据回显
        tableCont.callback(function(){
            $.post('/form/'+form_define_json.MODULE_ID+"-doGet-"+order_id,  function(data){
                console.log(data);
                fillFormData(data);
            });
        });
        

        var fillFormData = function(data){
            // 开始遍历 主表字段
            for ( var p in data ){ // 方法 
                if( typeof ( data[p]) == "function"){
                }else if(typeof ( data[p]) == "object" ){ 
                    var value = data[p]; 
                    var field_id = '[name=form_'+module_form_id+'-'+p.toLowerCase()+']';
                } else { // p 为属性名称，obj[p]为对应属性的值 
                    var value = data[p]; 
                    var field_id = '[name=form_'+module_form_id+'-'+p.toLowerCase()+']';
                    //console.log ( field_id +" = "+value ) ;
                    var type = $(field_id).attr('type');
                    if(type == "text"){ //inputBox
                        $(field_id).val(data[p]);// 根据ID 显示所有的属性 
                    }else if(type == "radio"){
                        $(field_id+"[value='"+data[p]+"']").prop("checked",true);
                    }
                }
            } 
            
            // 开始遍历  从表
            for ( var p in data ){ // 方法 
                if( data[p] instanceof Array ){ 
                   var list = data[p]; 
                   $.each(list, function(index, item) {
                        var target_table_id = item.TABLE_ID;
                        var data_list = item.DATA_LIST;
                        var dataTable = $('#'+target_table_id).DataTable();
                        if(data_list){
                          for (var i = 0; i < data_list.length; i++) {
                              var item = data_list[i];
                              dataTable.row.add(item).draw();
                          }
                        }
                   });
                }
            } 
        };

});
