define(['jquery', 'zTree'], function ($) {
    var zTreeObj;
    var update_flag = "N";
    //---------------tree handle
    var setting = {
        data: {
            keep: {
                parent: true
            }
        },
        view: {
            addHoverDom: addHoverDom,
            removeHoverDom: removeHoverDom,
            selectedMulti: false
        },
        edit: {
            enable: true,
            editNameSelectAll: true,
            showRemoveBtn: showRemoveBtn,
            showRenameBtn: showRenameBtn,
            renameTitle: "编辑",
            removeTitle: "删除",
            drag:{
                isCopy: false,
                isMove: true
            }
        },
        callback: {
            onRename: zTreeOnRename,//捕获节点编辑名称结束之后的事件回调
            onRemove: onNodeRemove,
            onClick: onNodeClick
        }
    };

    function zTreeOnRename(event, treeId, treeNode, isCancel){
        if(!isCancel){
            update_flag = "Y";
        }
    }
    function showRenameBtn(treeId, treeNode) {
        return !treeNode.isParent;
    }

    function showRemoveBtn(treeId, treeNode) {
        return !treeNode.isParent;
    }
    
    var deleteList=[];
    function onNodeRemove(event, treeId, treeNode) {
        update_flag = "Y";
        deleteList.push(treeNode.id);
    }

    function onNodeClick(event, treeId, treeNode){

    }
    function removeHoverDom(treeId, treeNode) {
        $("#addBtn_"+treeNode.tId).unbind().remove();
    };

    var newCount=1;
    function addHoverDom(treeId, treeNode) {
        var sObj = $("#" + treeNode.tId + "_span");
        //如果是叶子则不能在其下级添加节点
        if (!treeNode.isParent|| $("#addBtn_"+treeNode.tId).length>0) return;

        var addStr = "<span class='button add' id='addBtn_" + treeNode.tId
            + "' title='添加' onfocus='this.blur();'></span>";
        sObj.after(addStr);
        var btn = $("#addBtn_"+treeNode.tId);
        if (btn) btn.bind("click", function(){
            update_flag = "Y";
            //创建节点
            var nodeName = "按钮" + (newCount++);
            //id:data.ID, id: data.ID, 
            var newNodes = zTreeObj.addNodes(treeNode, {btn_id: treeNode.id, parent_id: treeNode.tId, isParent:false, name:nodeName, type:'app_btn_edit'});
            currentNode=newNodes[0];
            return false;
        });
    };


    

    var buildDto=function(){
        var treeObj=$.fn.zTree.getZTreeObj('app_btn_tree');
        return treeObj.getNodes();
    }
    var btn_update_flag = function(){
        return update_flag;
    }

    var displayAppBtnApp=function(btn_list){
        var newNodes=[], formId;
        for (var i = 0; i < btn_list.length; i++) {
            var field = btn_list[i];
            var node={btn_id: field.ID, isParent:false, name:field.NAME, type:'app_btn_edit'};
            formId=field.FORM_ID;
            newNodes.push(node);
        }

        var zNodes = [
            // { name:"查询列表按钮", isParent:true, open: true, children: [], formId:'1'},
            { name:"编辑页按钮", isParent:true, children: newNodes}
        ];
    
        zTreeObj = $.fn.zTree.init($("#app_btn_tree"), setting, zNodes);
        zTreeObj.expandAll(true);
    }
    return{
        buildDto: buildDto,
        btn_update_flag:btn_update_flag,
        displayAppBtnApp: displayAppBtnApp
    }
});