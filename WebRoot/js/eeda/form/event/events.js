define(['jquery','./on_load'], function ($,onload_cont) {
    var handle = function(action_type, formObj){
        if('default_event_on_load'==action_type){
            onload_cont.handle(formObj);
        }
    }

    
	return {
        handle:handle
    }
});
