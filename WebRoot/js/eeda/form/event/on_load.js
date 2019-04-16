define(['jquery','./element_set_enable','./element_set_show_hide'],
     function ($, element_set_enable_cont, element_set_show_hide_cont) {
    var handle = function(formObj){
        console.log('enter on_load.js');
        console.log(formObj);
        var onload_list = get_onload_list(formObj);
        onload_list.forEach(event => {
            var event_json = event.EVENT_JSON;
            var nodes = JSON.parse(event_json);
            console.log(nodes);
            nodes.forEach(node => {
                var children = node.children;
                children.forEach(action => {
                    var action_type = action.action_type;
                    switch (action_type) {
                        case 'element_set_enable':
                            element_set_enable_cont.handle(action);
                            break;
                        case 'element_set_show_hide':
                            element_set_show_hide_cont.handle(action);
                            break;
                        default:
                            break;
                    }
                });
            });
        });
    }

    var get_onload_list=function(formObj){
        var onload_list =[];
        var events = formObj.EVENTS;
        events.forEach(e => {
            if(e.EVENT_ACTION=="default_event_on_load"){
                onload_list.push(e);
            }
        });
        return onload_list;
    }
	return {
        handle:handle
    }
});
