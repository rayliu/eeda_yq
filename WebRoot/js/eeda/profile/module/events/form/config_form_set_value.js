define(['jquery'], function ($) {
    var actionTreeObj, actionTreeObjNode;
    var setActionTreeObjNode =function(node, tree){
        actionTreeObjNode=node;
        actionTreeObj=tree;
        redisplay(node);
    }

    var redisplay= function(node){
        var setting = node.event_action_setting;
        if(!setting){
            $('#form_set_value_source').val('');
            $('#form_set_value_target').val('');
            $('#form_set_value_condition').val('');
            $('#form_set_value_action_type').val('');
            dataTable.clear().draw();//清除所有数据行
        }else{
            $('#form_set_value_source').val(setting.source_table_name);
            $('#form_set_value_target').val(setting.target_table_name);
            $('#form_set_value_condition').val(setting.condition);
            $('#form_set_value_action_type').val(setting.form_set_value_action_type);
            var form_set_value_edit_field_data=JSON.parse(setting.form_set_value_edit_field_data);
            var row_length = form_set_value_edit_field_data.length/2;
            dataTable.clear().draw();//清除所有数据行
            for(var i=0;i<row_length;i++){
                var field1 = form_set_value_edit_field_data[i*2];
                var field2 = form_set_value_edit_field_data[i*2+1];
                var field_name="", expression="";
                if(field1.name=="field_name"){
                    field_name=field1.value;
                    expression=field2.value;
                }else{
                    field_name=field2.value;
                    expression=field1.value;
                }
    
                var item={
                    field_name:field_name,
                    expression:expression
                };
                dataTable.row.add(item).draw(true);
            }
        }
        
    }

    var dataTable = eeda.dt({
        id: 'form_set_value_fields_table',
        paging: false,
        lengthChange: false,
        info: false,
        searching: false,
        columns: [
            { "data": null, "width": "10px",
                  "render": function ( data, type, full, meta ) {
                    return '<button type="button" class="btn table_btn delete_btn btn-xs" >'+
                          '<i class="fa fa-remove"></i></button>';
                  }
            },
            { "data": 'field_name', "width": "160px",
                  "render": function ( data, type, full, meta ) {
                    if(data){
                        data=data;
                    }else{
                        data='';
                    }
                    return '<input type="text" name="field_name" style="width:100%" value="'+data+'">';
                  }
            },
            { "data": 'expression', 
                  "render": function ( data, type, full, meta ) {
                    if(data){
                        data=data;
                    }else{
                        data='';
                    }
                    return '<input type="text" name="expression" style="width:100%" value="'+data+'">';
                  }
            },
        ]
    });
    
    $('#form_set_value_edit_field_btn').click(function(){
        $('#formular_form_set_value_field_modal').modal('show');
        $('#formular_form_set_value_field_modal .modal-backdrop').css({"z-index":"0"});
        return false;
    });

    $('#formular_form_set_value_field_modal_add_row_btn').click(function(){
        var item={};
        dataTable.row.add(item).draw(true);
    });

    $('#form_set_value_fields_table tbody').on('click', '.delete_btn', function(){
        var $tr = $(this).closest('tr');
        //var idx = dataTable.row($tr).index();
        dataTable.row($tr).remove().draw();

    });
    
    $('#formular_form_set_value_field_modal_ok_btn').click(function(){
        var data = dataTable.$('input').serializeArray();
        console.log(data);
        $('#form_set_value_edit_field_data').val(JSON.stringify(data));
        //set acttionTree node
        changeActionTreeNode();

        $('#formular_form_set_value_field_modal').modal('hide');
    });

    $('#form_set_value_source, #form_set_value_target, #form_set_value_condition').keyup(function(){
        changeActionTreeNode();
    });

    $('#form_set_value_action_type').change(function(){
        changeActionTreeNode();
    });

    function changeActionTreeNode(){
        var source_table_name=$('#form_set_value_source').val();
        var target_table_name=$('#form_set_value_target').val();
        var condition=$('#form_set_value_condition').val();
        var form_set_value_action_type=$('#form_set_value_action_type').val();
        var form_set_value_edit_field_data=$('#form_set_value_edit_field_data').val();
        var event_action_setting={
            source_table_name:source_table_name,
            target_table_name:target_table_name,
            condition:condition,
            form_set_value_action_type:form_set_value_action_type,
            form_set_value_edit_field_data:form_set_value_edit_field_data
        };

        // var node_name = "在 "+target_table_name+" 添加 "+row_length+" 行";
        // actionTreeObjNode.name=node_name;
        actionTreeObjNode.event_action_setting=event_action_setting;//
        actionTreeObj.updateNode(actionTreeObjNode);
    }

    return {
        setActionTreeObjNode:setActionTreeObjNode
    };
});