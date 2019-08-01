define(['jquery', 'layer', 'zTree'], function ($, listCont) {
    $('.copy_btn').click(function(){
        var from_module_id = $(this).attr('module_id');
        var to_module_id = $('#module_id').val();
        $.post('/module/copyModule', {from_module_id:from_module_id, to_module_id:to_module_id}, function(data){
            if(data){
                debugger;
                var treeObj = parent.$.fn.zTree.getZTreeObj("moduleTree");
                var nodes = treeObj.getSelectedNodes();
                var node = nodes[0];
                node.name = data.NAME;
                treeObj.updateNode(node);
                //保存成功后刷新一下module
                //listCont.refresh_module_info(data.ID, data.NAME);
                parent.$("#"+node.tId+"_a").click();
                parent.layer.closeAll();
            }else{
                layer.msg("出错了");
                parent.layer.closeAll();
            }
        });
        
    });
});