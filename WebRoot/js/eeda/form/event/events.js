define(['jquery','./on_load'], function ($,onload_cont) {
    
    var handle = function(action_type, formObj){
        onload_cont.handle(formObj, action_type);
    }

	return {
        handle:handle
    }
});
