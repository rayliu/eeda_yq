define(['jquery'], function ($) {
       //condition modal --------------------
       $('#event_action_edit_condition_btn').click(function(event) {
            redisplay_conditions();
            $('#formular_conditon_modal').modal('show');
            $('#formular_conditon_modal .modal-backdrop').css({"z-index":0});
            return false;
       });

       var li_html= '<li>'+
                    '    <select class="condition_field1_type">'+
                    '        <option value="值">值</option>'+
                    '        <option value="被选项">被选项</option>'+
                    '        <option value="部件文字">部件文字</option>'+
                    '    </select>'+
                    '    <input type="text" class="condition_field condition_field1">'+
                    //'    <button type="button" class="btn btn-default btn-xs fx_btn">fx</button>'+
                    '    <select class="condition_operator">'+
                    '        <option value="==">==</option>'+
                    '        <option value="!=">!=</option>'+
                    '        <option value="<"><</option>'+
                    '        <option value=">">></option>'+
                    '        <option value="<="><=</option>'+
                    '        <option value=">=">>=</option>'+
                    '        <option value="in">包含</option>'+
                    '        <option value="not_in">不包含</option>'+
                    '    </select>'+
                    '    <select class="condition_field2_type">'+
                    '        <option value="值">值</option>'+
                    '        <option value="被选项">被选项</option>'+
                    '        <option value="部件文字">部件文字</option>'+
                    '    </select>'+
                    '    <input type="text" class="condition_field condition_field2">'+
                    // '    <button type="button" class="btn btn-default btn-xs fx_btn">fx</button>'+
                    '    <button type="button" class="btn btn-primary btn-xs add_condition">添加</button>'+
                    '    <button type="button" class="btn btn-danger btn-xs delete_condition">删除</button>'+
                    '</li>';
        
        $('#formular_condition').on('click', '.add_condition', function(){
            add_li();
        });

        $('#formular_condition').on('click', '.delete_condition', function(){
            var li=$(this).closest('li');
            var li_index = li.index();
            li.remove();
            $('#formular_condition_desc li').get(li_index).remove();
        });

        $('#formular_condition_add_btn').click(function(){
            add_li();
        });
        
        function add_li(){
            var li_list=$('#formular_condition li');
            var formular_conditon_match_type=$('#formular_conditon_match_type').val();
            if(li_list.length==0){
                formular_conditon_match_type="if ";
            }
            $('#formular_condition').append(li_html);

            var desc_li_html= '<li>'+
                '    <span class="formular_conditon_match_type">'+
                     formular_conditon_match_type+'</span>'+
                '    <span class="condition_field1_type_desc"></span>'+
                "    <span class='condition_field1_desc'>''</span>"+
                '    <span class="condition_operator_desc">==</span>'+
                '    <span class="condition_field2_type_desc"></span>'+
                "    <span class='condition_field2_desc'></span>"+
                '</li>';
            $('#formular_condition_desc').append(desc_li_html);
        }

        $('#formular_conditon_match_type').change(function(){
            var formular_conditon_match_type=$('#formular_conditon_match_type').val();
            $('#formular_condition_desc li .formular_conditon_match_type').not(':eq(0)').text(formular_conditon_match_type);
        });

        $('#formular_conditon_modal').on('change','select', function(){
            change_condition_desc();
        });

        $('#formular_conditon_modal #formular_condition').on('keyup','input',function(){
            change_condition_desc();
        });
        
        function change_condition_desc(){
            var li_list = $('#formular_condition li');
            var desc_li_list = $('#formular_condition_desc li');
            for (let i = 0; i < li_list.length; i++) {
                const li = $(li_list[i]);
                var condition_field1_type=li.find('.condition_field1_type').val();
                var condition_field1=li.find('.condition_field1').val();
                if(condition_field1.length==0){
                    condition_field1="''";
                }
                var condition_operator=li.find('.condition_operator').val();
                var condition_field2_type=li.find('.condition_field2_type').val();
                var condition_field2=li.find('.condition_field2').val();
                if(condition_field2.length==0){
                    condition_field2="''";
                }
                var desc_li=$(desc_li_list[i]);
                desc_li.find('.condition_field1_type_desc').text(condition_field1_type);
                desc_li.find('.condition_field1_desc').text(condition_field1);
                desc_li.find('.condition_operator_desc').text(condition_operator);
                desc_li.find('.condition_field2_type_desc').text(condition_field2_type);
                desc_li.find('.condition_field2_desc').text(condition_field2);
            }
        }

        $('#formular_condition_modal_cancel_btn').click(function(){
            $('#formular_conditon_modal').modal('hide');
        });

        $('#formular_condition_modal_ok_btn').click(function(){
            build_condition_json();
            $('#formular_conditon_modal').modal('hide');
        });

        function build_condition_json(){
            var desc_li_array=[];
            var desc_li_list = $('#formular_condition li');
            for (let i = 0; i < desc_li_list.length; i++) {
                const desc_li = $(desc_li_list[i]);
                var item={
                    condition_field1_type:desc_li.find('.condition_field1_type').val(),
                    condition_field1:desc_li.find('.condition_field1').val(),
                    condition_operator:desc_li.find('.condition_operator').val(),
                    condition_field2_type:desc_li.find('.condition_field2_type').val(),
                    condition_field2:desc_li.find('.condition_field2').val()
                }
                desc_li_array.push(item);
            }
            //往action tree的条件节点添加数据
            var treeObj = $.fn.zTree.getZTreeObj("actionTree");
            var node = treeObj.getNodeByTId("actionTree_1");
            var formular_conditon_match_type = $('#formular_conditon_match_type').val();
            node.name="条件(已设置)";
            node.condition_json=JSON.stringify(desc_li_array);
            node.formular_conditon_match_type = formular_conditon_match_type;
            treeObj.updateNode(node);
        };

        function redisplay_conditions(){
            var treeObj = $.fn.zTree.getZTreeObj("actionTree");
            var node = treeObj.getNodes()[0];
            var formular_conditon_match_type=node.formular_conditon_match_type;
            $('#formular_conditon_match_type').val(formular_conditon_match_type);

            var condition_json=node.condition_json;
            if(!condition_json) return;
            var condition_obj_array = JSON.parse(condition_json);
            console.log(condition_obj_array);
            $('#formular_condition').empty();
            $('#formular_condition_desc').empty();
            for (let index = 0; index < condition_obj_array.length; index++) {
                const element = condition_obj_array[index];
                var li_html= '<li>'+
                    '    <select class="condition_field1_type">'+
                    '        <option value="值" '+(element.condition_field1_type=='值'?'selected':'')+'>值</option>'+
                    '        <option value="被选项" '+(element.condition_field1_type=='被选项'?'selected':'')+'>被选项</option>'+
                    '        <option value="部件文字" '+(element.condition_field1_type=='部件文字'?'selected':'')+'>部件文字</option>'+
                    '    </select>'+
                    '    <input type="text" class="condition_field condition_field1" value="'+element.condition_field1+'">'+
                    //'    <button type="button" class="btn btn-default btn-xs fx_btn">fx</button>'+
                    '    <select class="condition_operator">'+
                    '        <option value="==" '+(element.condition_operator=='=='?'selected':'')+'>==</option>'+
                    '        <option value="!=" '+(element.condition_operator=='!='?'selected':'')+'>!=</option>'+
                    '        <option value="<" '+(element.condition_operator=='<'?'selected':'')+'><</option>'+
                    '        <option value=">" '+(element.condition_operator=='>'?'selected':'')+'>></option>'+
                    '        <option value="<=" '+(element.condition_operator=='<='?'selected':'')+'><=</option>'+
                    '        <option value=">=" '+(element.condition_operator=='>='?'selected':'')+'>>=</option>'+
                    '        <option value="in" '+(element.condition_operator=='in'?'selected':'')+'>包含</option>'+
                    '        <option value="not_in" '+(element.condition_operator=='not_in'?'selected':'')+'>不包含</option>'+
                    '    </select>'+
                    '    <select class="condition_field2_type">'+
                    '        <option value="值" '+(element.condition_field1_type=='值'?'selected':'')+'>值</option>'+
                    '        <option value="被选项" '+(element.condition_field1_type=='被选项'?'selected':'')+'>被选项</option>'+
                    '        <option value="部件文字" '+(element.condition_field1_type=='部件文字'?'selected':'')+'>部件文字</option>'+
                    '    </select>'+
                    '    <input type="text" class="condition_field condition_field2" value="'+element.condition_field2+'">'+
                    // '    <button type="button" class="btn btn-default btn-xs fx_btn">fx</button>'+
                    '    <button type="button" class="btn btn-primary btn-xs add_condition">添加</button>'+
                    '    <button type="button" class="btn btn-danger btn-xs delete_condition">删除</button>'+
                    '</li>';
                $('#formular_condition').append(li_html);
                var desc_li_html= '<li>'+
                '    <span class="formular_conditon_match_type">'+
                     formular_conditon_match_type+'</span>'+
                '    <span class="condition_field1_type_desc">'+element.condition_field1_type+'</span>'+
                '    <span class="condition_field1_desc">'+element.condition_field1+'</span>'+
                '    <span class="condition_operator_desc">'+element.condition_operator+'</span>'+
                '    <span class="condition_field2_type_desc">'+element.condition_field2_type+'</span>'+
                '    <span class="condition_field2_desc">'+element.condition_field2+'</span>'+
                '</li>';
                $('#formular_condition_desc').append(desc_li_html);
            }
        }
        
        return {
        	redisplay_conditions:redisplay_conditions
        };
    
});