define(['jquery', 'layer', 'zTree'], function ($) {
    console.log('js custom_search_sum_modal...');
    
    $('#sum_modal_cancel_btn').click(function(){
        parent.layer.closeAll();
    });
  
    $('#sum_modal_ok_btn').click(function(){
        var field_name = $('#sum_modal_field_name').val();
        var li_id = $(this).attr('li_id');
        var li= parent.$('#sum_cols_ul li#'+li_id);
        var span = li.find('span[name=field_name]');
        span.text(field_name);
        span.attr('field_display_name', field_name);
        span.attr('formular', $('#formular').val());
        parent.layer.closeAll();
    });

    //---------------tree handle
    var setting = {
        view: {
            //addHoverDom: addHoverDom,
            //removeHoverDom: removeHoverDom,
            selectedMulti: false
        },
        edit: {
            enable: false,
            editNameSelectAll: true,
            showRemoveBtn: false,
            showRenameBtn: false,
            renameTitle: "编辑",
            removeTitle: "删除",
            drag:{
                isCopy: false,
                isMove: true
            }
        },
        callback: {
            onClick: onNodeClick
        }
    };

    function onNodeClick(e, treeId, treeNode) {
        // debugger;
        //if (treeNode.level==0 ) return; 
        var name = treeNode.getParentNode().name+'.'+treeNode.name;
        var formular = $('#formular');
        formular.val(formular.val()+"+"+name);
    }

    var zNodes=[];
    var loadSourceTree = function(){
        $.post('/module/getSumModalTree', {form_arr: $('#form_arr').val()}, function(result){
            if(!result)
                return;
    
            var formList = result;
            for(var i=0;i<formList.length;i++){
               
                var form = formList[i];
                var node = {
                    name: form.FORM_NAME,
                    isParent:true, 
                    children: []
                };

                if(form.FIELD_LIST){
                  for (let index = 0; index < form.FIELD_LIST.length; index++) {
                      const field = form.FIELD_LIST[index];
                      var type = field.FIELD_TYPE;
                      if(type == "从表引用"){
                          var sub_list = field.FIELD_LIST;
                          $.each(sub_list,function(i,sub_field){
                            var sub_node = {
                                id: sub_field.ID,
                                name: sub_field.TARGET_FIELD_NAME
                            };
                            node.children.push(sub_node);
                          });
                      }else{
                        var sub_node = {
                            id: field.ID,
                            name: field.FIELD_DISPLAY_NAME
                        };
                        node.children.push(sub_node);
                      }
                      
                  }
                }
                zNodes.push(node);
            }
            var eventModuleTreeObj = $.fn.zTree.init($("#sum_col_tree"), setting, zNodes);
            //eventModuleTreeObj.expandAll(true);
        });
    };

    loadSourceTree();
});