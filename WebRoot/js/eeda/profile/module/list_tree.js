define(['jquery', 'zTree', './fields', './btns', './events', './edit_events', './interface/interface','./custom_search/custom_search'], 
    function ($, tree, fieldCont, btnsCont, eventsCont, editEventCont, intCont,customSearchCont) {

    $(document).ready(function() {
    	
        var module_obj;

        //---------------tree handle
        var setting = {
            view: {
                addHoverDom: addHoverDom,
                removeHoverDom: removeHoverDom,
                selectedMulti: false
            },
            edit: {
                enable: true,
                editNameSelectAll: true,
                showRemoveBtn: true,
                //showRenameBtn: showRenameBtn,
                renameTitle: "编辑",
                removeTitle: "删除",
                drag:{
                    isCopy: false,
                    isMove: true
                }
            },
            async: {
                enable: true,
                type: 'get',
                url:"/module/searchModule",
                autoParam:["id", "level=lv"],
                dataFilter: dataFilter//处理返回来的JSON 变为 nodes
            },
            callback: {
                beforeRename: beforeRename,
                onRename: onRename,
                beforeDrop: beforeDrop,//判断禁止模块拖拽到模块下
                onDrop: onDrop,
                onClick: onNodeClick,
                beforeRemove:beforeRemove
            }
        };

        function dataFilter(treeId, parentNode, childNodes) {
            if (!childNodes) return null;
            console.log(childNodes);
            for (var i=0, l=childNodes.length; i<l; i++) {
                childNodes[i].name = childNodes[i].MODULE_NAME.replace(/\.n/g, '.');
                childNodes[i].id = childNodes[i].ID;
                childNodes[i].parent_id = childNodes[i].PARENT_ID;
                childNodes[i].url = childNodes[i].URL;
                if(childNodes[i].PARENT_ID>0){
                    childNodes[i].isParent=false;
                }
            }
            return childNodes;
        };

        function beforeRename(treeId, treeNode, newName, isCancel) {
            if (newName.length == 0) {
                alert("节点名称不能为空.");
                var zTree = $.fn.zTree.getZTreeObj("moduleTree");
                setTimeout(function(){zTree.editName(treeNode)}, 10);
                return false;
            }
            return true;
        };

        function onRename(e, treeId, treeNode, isCancel) {
            $.post('/module/updateModule', 
                {id: treeNode.id, parent_id: treeNode.parent_id, module_name:treeNode.name}, 
                function(data){

            },'json');
        };

        function beforeDrop(treeId, treeNodes, targetNode, moveType) {
            if(treeNodes[0].level ==0 && targetNode.isParent && moveType =='inner'){
                alert("模块不能拖到模块下级");
                return false;
            }
            if(treeNodes[0].level ==1 && targetNode.level == 0 && moveType !='inner'){
                alert("单据不能拖到模块同级");
                return false;
            }
            if(treeNodes[0].level ==1 && targetNode.level == 1 && moveType =='inner'){
                alert("单据不能拖到单据下级");
                return false;
            }
            return true;
        };

        function onDrop(event, treeId, treeNodes, targetNode, moveType) {
           // console.log("moveType:"+moveType + "," + treeNodes.length + "," + (targetNode ? (targetNode.tId + ", " + targetNode.name) : "isRoot" ));
            console.log("moveType:"+moveType + ", " +treeNodes[0].name+":"+ treeNodes[0].id + ", "+targetNode.name+":"+targetNode.id);
            if(targetNode){
                $.post('/module/updateModuleSeq', 
                    {node_id: treeNodes[0].id, target_node_id: targetNode.id, move_type:moveType}, 
                    function(data){},
                    'json');
            }
        };

        var newCount = 1;
        function addHoverDom(treeId, treeNode) {
            var sObj = $("#" + treeNode.tId + "_span");
            //如果是单据则不能在其下级添加节点
            if (treeNode.level==1 ||treeNode.editNameFlag || $("#addBtn_"+treeNode.tId).length>0) return;
            var addStr = "<span class='button add' id='addBtn_" + treeNode.tId
                + "' title='添加' onfocus='this.blur();'></span>";
            sObj.after(addStr);
            var btn = $("#addBtn_"+treeNode.tId);
            if (btn) btn.bind("click", function(){
                //异步创建节点
                var zTree = $.fn.zTree.getZTreeObj("moduleTree");
                var nodeName = "新单据" + (newCount++);
               
                // 7-5 使用异步会导致树节点添加两次， 因为自己手动加了一个，ztree自己异步自动又加了一个
                $.post('/module/addModule', {parent_id: treeNode.id, id: null, name:nodeName}, function(data){
                    if ((!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) || treeNode.zAsync){ 
                        zTree.addNodes(treeNode, {id:data.ID, id: data.ID, parent_id: treeNode.PARENT_ID, isParent:true, name:nodeName});
                        zTree.reAsyncChildNodes(treeNode, "refresh");
                    } else{
                        zTree.reAsyncChildNodes(treeNode, "refresh");
                    }
                },'json');

                return false;
            });
        };

        function beforeRemove(treeId, treeNode) {
        	var btn = $("#"+treeNode.tId+"_remove");
                //异步创建节点
                var zTree = $.fn.zTree.getZTreeObj("moduleTree");
                var nodeName = treeNode.MODULE_NAME!=null?treeNode.MODULE_NAME:treeNode.name;
                $.post('/module/deleteModuleTree', {id: treeNode.id,nodeName:nodeName}, function(data){
                	alert("删除成功!");
                	viewModule();
                },'json');
        };

        function removeHoverDom(treeId, treeNode) {
            $("#addBtn_"+treeNode.tId).unbind().remove();
        };
        
        function onNodeClick(event, treeId, treeNode){
            if (treeNode.level==0 ||treeNode.editNameFlag || $("#addBtn_"+treeNode.tId).length>0) return;
            if(treeNode.parentTId != null){
                $("#addProductDiv").show();
                $("#displayDiv").show();
            }else{
                $("#addProductDiv").hide();
                $("#displayDiv").hide();
            }
            $(".form_info input[type='text']").val("");
            $(".form_info input[type='checkbox']").prop("checked",false);
            $("#module_id").text(treeNode.id);
            $("#form_name").val(treeNode.name);
            $("#module_url").val(treeNode.url);

            $.post('/module/getOrderStructure', {module_id: treeNode.id}, function(json){
                console.log('getOrderStructure....');
                console.log(json);
                module_obj = json;

                //跳回tab第一页
                $("#displayDiv ul[role=tablist] li:first a").click();
                //fields clear
                fieldCont.clear();

                var ue = UE.getEditor('container');
                ue.execCommand('cleardoc');//clear content

                var app_ue = UE.getEditor('app_container');
                app_ue.execCommand('cleardoc');//clear content

                btnsCont.clear();
                eventsCont.clear();
                editEventCont.clear();
                intCont.clear();

                if(module_obj.FORM){
                    $("#form_id").val(module_obj.FORM.ID);
                    $('#form_code').val(module_obj.FORM.CODE);
                    $('#form_name').val(module_obj.FORM.NAME);
                    if(module_obj.FORM.IS_HOME_INDEX=='Y'){
                        $("#is_home_index").prop("checked",true);
                    }
                    if(module_obj.FORM.IS_PUBLIC=='Y'){
                        $("#is_public").prop("checked",true);
                    }
                    $('input[value="'+module_obj.FORM.TYPE+'"]').prop("checked",true).trigger('change');

                    ue.setContent(module_obj.FORM.TEMPLATE_CONTENT);
                    if(module_obj.FORM.APP_TEMPLATE!=null){
                        app_ue.setContent(module_obj.FORM.APP_TEMPLATE);
                    }
                        
                    var fields_dataTable = fieldCont.dataTable;//$('#fields_table').DataTable();
                    
                    for (var i = 0; i < json.FORM_FIELDS.length; i++) {
                        var field = json.FORM_FIELDS[i];
                        fields_dataTable.row.add(field).draw(false);
                    }
                    fieldCont.add_elect_flag();
                    
                    //回显自定义查询
                    customSearchCont.clear();
                    if(json.CUSTOM_SEARCH_SOURCE.length>0){
                    	customSearchCont.sourceDisplay(json.CUSTOM_SEARCH_SOURCE);
                    }
                    if(json.CUSTOM_SEARCH_SOURCE_CONDITION.length>0){
                    	customSearchCont.sourceConditionDisplay(json.CUSTOM_SEARCH_SOURCE_CONDITION);
                    }
                    if(json.CUSTOM_SEARCH_COLS.length>0){
                    	customSearchCont.colsDisplay(json.CUSTOM_SEARCH_COLS);
                    }
                    if(json.CUSTOM_SEARCH_FILTER.length>0){
                    	customSearchCont.filterDisplay(json.CUSTOM_SEARCH_FILTER);
                    }
                    
                    
                    //回显按钮列表
                    btnsCont.list_dataTable.clear().draw();
                    var toolbar_list_table = btnsCont.list_dataTable;
                    for (var i = 0; i < json.BTN_LIST_QUERY.length; i++) {
                        var field = json.BTN_LIST_QUERY[i];
                        toolbar_list_table.row.add(field).draw(false);
                    }

                    btnsCont.edit_dataTable.clear().draw();
                    var toolbar_edit_table = btnsCont.edit_dataTable;
                    for (var i = 0; i < json.BTN_LIST_EDIT.length; i++) {
                        var field = json.BTN_LIST_EDIT[i];
                        toolbar_edit_table.row.add(field).draw(false);
                    }
                    //回显事件树的按钮列表
                    eventsCont.displayBtnTree();
                    editEventCont.displayBtnTree();
                }

                var permission_dataTable = $('#permission_table').DataTable();
                permission_dataTable.clear().draw();
                for (var i = 0; i < json.PERMISSION_LIST.length; i++) {
                    var permission = json.PERMISSION_LIST[i];
                    var permissionItem ={
                        ID: permission.ID,
                        CODE: permission.CODE,
                        NAME: permission.NAME,
                        URL: permission.URL
                    };

                    permission_dataTable.row.add(permissionItem).draw(false);
                }

                var auth_dataTable = $('#auth_table').DataTable();
                auth_dataTable.clear().draw();
                for (var i = 0; i < json.AUTH_LIST.length; i++) {
                    var auth = json.AUTH_LIST[i];
                    var authItem ={
                        ID: auth.ID,
                        ROLE_ID: auth.ROLE_ID,
                        ROLE_PERMISSION: auth.PERMISSION_LIST
                    };

                    auth_dataTable.row.add(authItem).draw(false);
                }

                var print_template_dataTable = $('#print_template_table').DataTable();
                print_template_dataTable.clear().draw();
                if(json.PRINT_TEMPLATE_LIST && json.PRINT_TEMPLATE_LIST.length>0){
                	for (var i = 0; i < json.PRINT_TEMPLATE_LIST.length; i++) {
                        var template = json.PRINT_TEMPLATE_LIST[i];
                        var item ={
                            ID: template.ID,
                            NAME: template.NAME,
                            DESC: template.DESC,
                            CONTENT: template.CONTENT
                        };

                        print_template_dataTable.row.add(item).draw(false);
                    }
                }
                

                var interface_dataTable = $('#interface_table').DataTable();
                interface_dataTable.clear().draw();
                if(json.INTERFACE_LIST && json.INTERFACE_LIST.length>0){
	                for (var i = 0; i < json.INTERFACE_LIST.length; i++) {
	                    var inter = json.INTERFACE_LIST[i];
	                    var item ={
	                        ID: inter.ID,
	                        NAME: inter.NAME,
	                        TYPE: inter.TYPE,
	                        HEIGHT: inter.HEIGHT,
	                        WIDTH: inter.WIDTH,
	                        IS_DISTINCT: inter.IS_DISTINCT,
	                        FILTER_CONDITION: inter.FILTER_CONDITION,
	                        COLS: inter.COLS,
	                        FILTER: inter.FILTER,
	                        SOURCE: inter.SOURCE
	                    };
	                
	                    interface_dataTable.row.add(item).draw(false);
	                }
                }
            }, 'json');
        };
        //---------------------------  tree handler end -------------

        
        
        var viewModule = function(){
        	// 显示当前用户公司的所有模块
            $.get('/module/searchModule', function(data){
                console.log(data);
                var zNodes =[];
                for(var i=0; i<data.length && data.length>0; i++){
        	        var node={};
        	        node.name=data[i].MODULE_NAME;
        	        node.id=data[i].ID;
                    node.isParent=true;
        	        //node.click=nodePlusClickHandler;
        	        zNodes.push(node);
        	        //console.log(node);
                }
                $.fn.zTree.init($("#moduleTree"), setting, zNodes);

                $('html, body').animate({scrollTop:0});
            },'json');
        };
        
        viewModule();
        
        $('#addModuleBtn').click(function function_name (argument) {
            var zTree = $.fn.zTree.getZTreeObj("moduleTree");
            $.post('/module/addModule', {parent_id: null, id: null, name:"新模块"}, function(data){
                zTree.addNodes(null, {parent_id: null, id: data.ID, name:"新模块", isParent:true});
            },'json');
        });

    });
});