define(['jquery', 'sco'], function ($) {

    $.fn.isBound = function(type, fn) {
        var data = this.data('events')[type];

        if (data === undefined || data.length === 0) {
            return false;
        }

        return (-1 !== $.inArray(fn, data));
    };

    $('input[eeda_type=drop_down]').on('keyup click', function(event) {
        var inputField = $(this);
        var inputField_name = inputField.attr('name');

        var hiddenField_name = inputField_name.substr(0, inputField_name.length-6);
        var hiddenField = $("[name="+hiddenField_name+"]");

        var drop_list =$("#"+inputField_name+"_list");
        var drop_down_span = drop_list.closest('.dropDown');

        drop_list.attr({
            input_name: inputField_name,
            input_hidden_name: hiddenField_name
        });
        //处理中文输入法, 没完成前不触发查询
        var cpLock = false;
        inputField.on('compositionstart', function () {
            cpLock = true;
        });
        inputField.on('compositionend', function () {
            cpLock = false;
        });

        var inputStr = inputField.val();
        if(cpLock)
            return;
        
        if (event.keyCode == 40) {
            drop_list.find('li').first().focus();
            return false;
        }
        
        var target_form_id = inputField.attr('target_form');
        var target_field = inputField.attr('target_field_name');
        var target_field_arr = target_field.split(",");
        $.get('/form/'+target_form_id+'-doQuery?target_field='+target_field+'&like_str='+inputStr, function(dto){
            if(inputStr!=inputField.val()){//查询条件与当前输入值不相等，返回
                return;
            }
            drop_list.empty();

            var data = dto.data;

            for(var i = 0; i < data.length; i++){
                var display_cols = "";
                target_field_arr.forEach(element => {
                    display_cols += " "+data[i][element.toUpperCase()];
                });
                var html= "<li><a tabindex='-1' class='drop_list_item' target_field='"+data[i][target_field.toUpperCase()]
                        +"' id='"+data[i].ID+"' data='"+JSON.stringify(data[i])+"'>"+display_cols+"</a></li>";

                drop_list.append(html);
            }
            
            drop_list.css({ 
                left: 0-inputField.position().left-inputField.width()-10+"px", 
                top:inputField.position().top+17+"px"
                //width: inputField.width()+"px"
            });
            drop_down_span.addClass('open');
            
            drop_list.css('display', 'block');
            
        },'json');
    });


    $('ul.dropDown-menu').on('click', 'a', function(e){
        var item = $(this);
        var id = item.attr('id');
        var value = item.text();
        console.log(item);

        var ul = item.closest('ul');
        var inputField_name = ul.attr('input_name');
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
    });

    //pop 弹框响应, 点击后才初始化 pop_table
    $('input[eeda_type=pop]').on('keyup click', function(event) {
        var inputField = $(this);
        var inputStr = inputField.val();
        var inputFieldName = inputField.attr('name');

        $('#pop_template_target_field_id').val(inputFieldName);

        var target_form_id = inputField.attr('target_form');
        var target_field = inputField.attr('target_field_name');
        var target_field_arr = target_field.split(",");
        //处理title
        var item_list_str = inputField.attr('item_list');
        var item_list = JSON.parse(item_list_str);

        var cols=[
            { "data": "ID", "width": "30px",
                  "render": function ( data, type, full, meta ) {
                      var id='';
                      if(data){
                        id=data;
                      }
                  return "<input type='radio' name='pop_radio' style='margin-right:5px;' data='"+JSON.stringify(full)+"'>"
                  +'<input name="id" type="hidden" value="'+id+'">';
                  }
              }];

        target_field_arr.forEach(element => {
            var title = "";
            $.each(item_list, function(index, item) {
                if(item.FROM_FIELD_NAME==element){
                    title = item.FROM_NAME.split(".")[1];
                }
            });

            var col={"data":element.toUpperCase(),"title":title};
            cols.push(col);
        });
        var datatable = $('#pop_list_table').DataTable();
        datatable.destroy(); //改变列的结构，先销毁前面的实例
        $('#pop_list_table').empty();// 列改变了，需要清空table
        
        //datatable, 动态处理
        datatable = eeda.dt({
            id: 'pop_list_table',
            paging: true,
            lengthChange: false,
            columns: cols
        });
        $.get('/form/'+target_form_id+'-doQuery?target_field='+target_field, function(dto){
            var data = dto.data;

            for(var i = 0; i < data.length; i++){
                var dataObj= data[i];
                var item={};
                item.ID = "";
                target_field_arr.forEach(element => {
                    item[element.toUpperCase()]=dataObj[element.toUpperCase()];
                });
                
                datatable.row.add(item).draw(false);
            }
        });
        $('#pop_template').modal('show');
    });

    $('#pop_modal_ok_btn').click(function(){
        var inputRadio = $('#pop_list_table input[type=radio]:checked');
        if(inputRadio.length==0){
            alert('请选择一条记录');
            return;
        }
        var tr = inputRadio.closest('tr');
        var inputFieldName = $('#pop_template_target_field_id').val();
        var inputField = $('input[name='+ inputFieldName +']');
        //处理回填
        var item_list_str = inputField.attr('item_list');
        var item_list = JSON.parse(item_list_str);
        var data_str = inputRadio.attr('data');
        var data= JSON.parse(data_str);
        $.each(item_list, function(index, item) {
            var from_name = item.FROM_FIELD_NAME;
            var to_name = item.TO_FIELD_NAME;
            $("input[name*='"+to_name+"']").val(data[from_name.toUpperCase()]);
        });
        $('#pop_template').modal('hide');
    });
});
