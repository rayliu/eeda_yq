define(['jquery'], function ($) {
    console.log('enter element_set_text.js');

    var handle = function(action){
        var event_action_setting=action.event_action_setting;
        var tree_json = event_action_setting.tree_json;
        var treeObj = JSON.parse(tree_json);
        //获取checked的节点
        var selected_nodes=get_selected_nodes(treeObj);

        var form_define_json = JSON.parse($("#form_define").text());
        var order_id = $('#order_id').val();
        var form_id = form_define_json.ID;

        for (let index = 0; index < selected_nodes.length; index++) {
            const node = selected_nodes[index];
            var type=node.type;
            var id = node.id;
            var value = node.text_val;

            var regex = /\{(.+?)\}/g;  // {} 花括号，大括号
            var field_array = value.match(regex);
            if(field_array){
                field_array.forEach(key => {
                    switch (key) {
                        case '{当前用户姓名}':
                                var user_name=$('#login_user_name').text();
                                value=value.replace(key, user_name);
                            break;
                        case '{当前日期}':
                            var date = new Date().Format("yyyy-MM-dd");
                            value=value.replace(key, date);
                            break;
                        case '{当前日期时间}':
                            var datetime = new Date().Format("yyyy-MM-dd HH:mm:ss");
                            value=value.replace(key, datetime);
                            break;
                        case '{当前用户账户}':
                            var user_name=$('#login_user_name').text();
                            break;
                        case '{当前用户部门}':
                            var user_name=$('#login_user_name').text();
                            break;
                        case '{当前用户角色}':
                            var user_name=$('#login_user_name').text();
                            break;
                        case '{当前用户手机}':
                            var user_name=$('#login_user_name').text();
                            break;
                        case '{当前用户邮件}':
                            var user_name=$('#login_user_name').text();
                            break;
                        default:
                            break;
                    }
                });
            }

            var element_id = "[name^=form_"+form_id+"-f"+id+"]";
            var e = $(element_id);
            var tagName = e.prop("tagName");
            if(tagName=='INPUT'){
                e.val(value);
            }
            
        }
    }

    var get_selected_nodes=function(treeObj){
        var selected_nodes=[];
        for (let index = 0; index < treeObj.length; index++) {
            const node = treeObj[index];
            var children = node.children;
            for (let i = 0; i < children.length; i++) {
                const sub_node = children[i];
                if(sub_node.checked){
                    selected_nodes.push(sub_node);
                }
            }
        }
        return selected_nodes;
    }
    
	return {
        handle:handle
    }
});
