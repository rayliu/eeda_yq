define(['jquery'], function ($) {
    console.log('enter element_set_focus.js');

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

            var element_id = "[name^=form_"+form_id+"-f"+id+"]";
            $(element_id).focus();
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
