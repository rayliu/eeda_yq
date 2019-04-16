define(['jquery'], function ($) {
    var dataTable;
    $('#table_set_add_row_btn').click(function(){
        $('#formular_table_add_row_modal').modal('show');
        $('#formular_table_add_row_modal .modal-backdrop').css({"z-index":"0"});
        var form_id=$('#form_id').val()||92;
        var field_name=$('#config_sub_table_list').val();
        
        if(dataTable){
            dataTable.destroy();
            $('#set_table_fields_value_table').empty();
        }
        
        //获取目标表的cols字段
        $.post('/module/getFieldsByFormName', {form_id:form_id, field_name:field_name}, function(list){
            
            //动态设置列
            var columns = [];
            list.forEach(element => {
                var col = {
                    data: element.FIELD_NAME,
                    title: element.FIELD_DISPLAY_NAME,
                    render: function ( data, type, full, meta ) {
                        return '<input type="text" name="'+element.FIELD_NAME+'" style="width:85%;">'
                        +'<button type="button" class="formular_btn btn-default btn-xs"> fx </button>';
				    }
                };
                columns.push(col);
            });

            dataTable = eeda.dt({
                id: 'set_table_fields_value_table',
                paging: false,
                lengthChange: false,
                info: false,
                searching: false,
                columns: columns
            });
            
        });
    });

    $('#formular_table_add_row_modal_add_btn').click(function(){
        var item={};
	    dataTable.row.add(item).draw(true);
    });
    
    //获取当前表单的表格字段
    function loadTargetTableName(){
        var form_id=$('#form_id').val()||92;
        var form_name=$('#form_name').val()||"入库单";
        $.post('/module/getFormSubTableList', {form_id:form_id}, function(list){
            console.log(list);
            $('#config_sub_table_list').empty();
            list.forEach(element => {
                var value = form_name+'.'+element.FIELD_DISPLAY_NAME;
                //field_id为主表对应的field
                var html='<option value="'+value+'" field_id="'+element.ID+'">'+value+'</option>';
                 $('#config_sub_table_list').append(html);
            });
        });
    }
    
    $('#formular_table_add_row_modal_ok_btn').click(function(){
        var data = dataTable.rows().data();//all data
        var row_length = data.toArray().length;
        $('#config_table_row_num').text(row_length);
        //set acttionTree node
        changeActionTreeNode();

        $('#formular_table_add_row_modal').modal('hide');
    });

    var actionTreeObj, actionTreeObjNode;
    var setActionTreeObjNode =function(node, tree){
        actionTreeObjNode=node;
        actionTreeObj=tree;
        redisplay(node);
    }

    var redisplay= function(node){
        if(!node.event_action_setting) return ;
        $('#config_sub_table_list').val(node.event_action_setting.target_table_name);
        $('#config_table_row_num').text(node.event_action_setting.table_add_row_num);
    }

    function changeActionTreeNode(){
        var target_table_name=$('#config_sub_table_list').val();
        var target_table_field_id=$('#config_sub_table_list option:selected').attr('field_id');
        var row_length=$('#config_table_row_num').text();
        var event_action_setting={
            target_table_field_id:target_table_field_id,
            target_table_name:target_table_name,
            table_add_row_num:row_length
        };

        var node_name = "在 "+target_table_name+" 添加 "+row_length+" 行";
        actionTreeObjNode.name=node_name;
        actionTreeObjNode.event_action_setting=event_action_setting;//
        actionTreeObj.updateNode(actionTreeObjNode);
    }

    return {
        loadTargetTableName:loadTargetTableName,
        setActionTreeObjNode:setActionTreeObjNode
    };
});