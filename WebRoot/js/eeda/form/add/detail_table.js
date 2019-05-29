define(['jquery', 'sco', '../btns'], function ($, sco, btnCont) {
    console.log('detail table  .....');
    var tables = $('table[type=dynamic]');
    var field_id_list = [];
    $.each(tables, function(index, item) {
         var id=$(item).attr('id');
         var field_id = id.split('_')[2];
         field_id_list.push(field_id);
    });

    var module_id = $('#module_id').val();
    $.post('/form/'+module_id+'-tableConfig', {field_id_list: JSON.stringify(field_id_list)}, function(data, textStatus, xhr) {
        console.log(data);
        if(data.length ==0){//无从表
            if(getDataFunc){
                getDataFunc();
            }
            return ;
        }
        $.each(data, function(index, item) {
            var display_list = item.DISPLAY_FIELD_LIST;

            var idCol = {
                data: "ID",
                render: function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return '<button class="btn btn-xs" name="table_delete_row_btn" ><i class="Hui-iconfont">&#xe6e2;</i></button> '
                        +'<input type="hidden" name="id" value="'+data+'" class="form-control"/>';
                }
            };

            var cols=[idCol];
            
            var field_id="";
            $.each(display_list, function(index, field) {
                 var col={
                    data: field.FIELD_NAME.toUpperCase(),
                    render: function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        var eeda_type="";
                        var placeholder="";
                        var str = "";
                        
                        if(field.FIELD_TYPE == '下拉列表'){
                        	str = "<select name='"+field.FIELD_NAME+"' style='width: 100%;' class='form-control input-text'>"
                        	var list = field.DROPDOWN_LIST;
                        	var dropdownStr = "";
                        	for(var i = 0;i<list.length;i++){
                        		if(data==list[i].NAME){
                        			dropdownStr += "<option value='"+list[i].VALUE+"' class='input-text' selected='selected'>"+list[i].NAME+"</option>";
                        		}else{
                        			dropdownStr += "<option class='input-text' style='width: 100%;' value='"+list[i].VALUE+"'>"+list[i].NAME+"</option>";
                        		}
                        	}
                        	str+=dropdownStr+"</select>";
                                
                        }else{
                        	if(field.FIELD_TYPE == '字段引用'){
                                eeda_type="drop_down";
                                placeholder="请选择";
                            }
                            str= '<input type="text" name="'+field.FIELD_NAME+'" value="'+data
                                +'" eeda_type="'+eeda_type
                                +'" placeholder="'+placeholder+'" ';

                            if(field.FIELD_TYPE == '日期时间'){
                            	str+= "onfocus=\"WdatePicker({dateFmt:\'yyyy-MM-dd HH:mm:ss\'})\" class=\"input-text Wdate\""
                            }else{
                            	str+= "class=\"input-text\""
                            }
                            if(field.FIELD_TYPE == '字段引用'){
                                str+= " target_form='"+field.REF.TARGET_FORM_ID
                                     +"' target_field_name='"+field.REF.TARGET_FIELD_NAME+"'"
                                     +" item_list='"+JSON.stringify(field.REF.REF_ITEM_LIST)+"'";
                            }
                            str+'  style="width: 100%;"/>';
                        }
                        
                        return str+"<span class='dropDown'>"
                            +"     <ul name='"+field.FIELD_NAME+"_list' class='pull-right dropDown-menu menu default dropdown-scroll' tabindex='-1' style='top: 35%; left: 2%;display:none;'/>"
                            +"</span>";    
                    }
                 }
                 cols.push(col);
                 field_id=field.FIELD_ID;
            });
            console.log(cols);
            var dataTable = eeda.dt({
                id: 'detail_table_'+field_id,
                columns: cols,
                initComplete: function(settings, json) {
                    console.log(json);
                    //等所有 table 初始完成后, 才执行 查数据的动作
                    if( index==(data.length-1) && getDataFunc){
                        getDataFunc();
                    }
                }
            });
        });
    });

    var getDataFunc = null;
    function callback(f){
        getDataFunc =f;
    }

    return {
        callback: callback
    };
});
