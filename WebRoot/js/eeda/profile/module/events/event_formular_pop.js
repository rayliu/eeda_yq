define(['jquery', './event_formular_pop_action', './event_formular_pop_condition'], function ($, actionCont, conditionCont) {

        //---------------tree handle
        var setting = {
          view: {
              // addHoverDom: addHoverDom,
              // removeHoverDom: removeHoverDom,
              selectedMulti: false
          },
          edit: {
              enable: false,
              editNameSelectAll: true,
              showRemoveBtn: false,
              //showRenameBtn: showRenameBtn,
              renameTitle: "编辑",
              removeTitle: "删除",
              drag:{
                  isCopy: false,
                  isMove: true
              }
          },
          callback: {
               onClick: onNodeClick,
               onDblClick: onNodeDblClick
          }
      };

      function onNodeDblClick(event, treeId, treeNode){
        if (treeNode.level==0 ) return;

        var oldStr = $('#formular_edit_modal_formular').val();
        $('#formular_edit_modal_formular').val(oldStr + treeNode.value);
      }

        function removeHoverDom(treeId, treeNode) {
            $("#addBtn_"+treeNode.tId).unbind().remove();
        };

        function onNodeClick(event, treeId, treeNode){
            if (treeNode.level==0 ) return;
            console.log('添加动作 clicked: '+treeNode.name+', action_type: '+treeNode.action_type);

            $('.event_config').hide();
            var name="";
            //返回一个根节点 
            switch (treeNode.action_type) {
                case 'open_link':
                    name="在 当前窗口 打开 链接";
                    break;
                case 'close_window':
                    name="关闭当前窗口";
                    break;
                case 'element_set_enable':
                    name="启用/禁用";
                    break;
                case 'element_set_show_hide':
                    name="显示/隐藏";
                    break;
                case 'element_set_text':
                    name="设置文本";
                    break;
                case 'element_set_checkbox':
                    name="设置复选框选中";
                    break;
                case 'element_set_droplist':
                    name="设置下拉列表选中";
                    break;
                case 'element_set_focus':
                    name="获取焦点";
                    break;
                case 'save_form':
                    name="保存表单";
                     break;
                case 'form_set_value':
                    name="表单赋值";
                    break;
                case 'print':
                    name="表单打印";
                    break;
                case 'table_add_row':
                    name='添加行';
                    break;
                case 'table_delete_row':
                    name='删除行';
                    break;
                default:
                    break;
            }
            var actionTreeObj = actionCont.actionTreeObj;
            //往组织动作树下添加节点
            var root_node = actionTreeObj.getNodesByFilter(function (node) { return node.level == 0 }, true); 
            var newNode = {name: name, action_type:treeNode.action_type};
            actionTreeObj.addNodes(root_node, newNode);
       }

      var defaultActionNodes = [
          { name:"链接", 
           isParent:true, 
           children: [
              {name:"打开链接", action_type:"open_link", desc:"在 当前窗口 打开 链接"},
              {name:"关闭窗口", action_type:"close_window", desc:"关闭当前窗口"}
           ]
          },
          { name:"字段/部件", 
           isParent:true, 
           children: [
              {name:"显示/隐藏", action_type:"element_set_show_hide", desc:"用途:显示/隐藏某个部件"},
              {name:"设置文本", action_type:"element_set_text", desc:"用途:设置某个部件的文本"},
              {name:"设置复选框选中", action_type:"element_set_checkbox", desc:"用途:设置某个图片部件的图片"},
              {name:"设置下拉列表选中", action_type:"element_set_droplist", desc:"用途:设置某个图片部件的选中，取消选中，切换选中状态"},
              {name:"启用/禁用", action_type:"element_set_enable", desc:"用途:设置某个部件的启用/禁用"},
              {name:"获取焦点", action_type:"element_set_focus", desc:""},
              //{name:"展开/折叠树节点", action_type:"element_set_node_open", desc:""},
           ]
          },
          { name:"表单", 
           isParent:true, 
           children: [
              {name:"保存", action_type:"save_form", desc:""},
              {name:"打印", action_type:"print", desc:""},
              {name:"表单赋值", action_type:"form_set_value", desc:""},
              {name:"导入excel", action_type:"import", desc:""},
              {name:"导出excel", action_type:"export", desc:""},
              {name:"下载excel模板", action_type:"download_template", desc:""}
           ]
          },
          { name:"全局变量", 
           isParent:true, 
           children: [
              {name:"设置变量值", action_type:"set_global_valu", desc:"用途:打开链接"}
           ]
          },
          { name:"表格(中继器)", 
           isParent:true, 
           children: [
              {name:"添加排序", action_type:"table_sort", desc:""},
              {name:"添加分页显示条数", action_type:"table_paging_num", desc:""},
              { name:"数据集", 
                isParent:true, 
                children: [
                    {name:"添加行", action_type:"table_add_row", desc:""},
                    {name:"标记行", action_type:"table_mark_row", desc:""},
                    {name:"取消标记", action_type:"table_unmark_row", desc:""},
                    {name:"更新行", action_type:"table_update_row", desc:""},
                    {name:"删除行", action_type:"table_delete_row", desc:""}
                ]
              }
           ]
          },
          { name:"其它", 
           isParent:true, 
           children: [
              {name:"等待", action_type:"wait_second", desc:""},
              {name:"alert弹窗", action_type:"alert_win", desc:""},
              {name:"触发事件", action_type:"trigger_event", desc:""}
           ]
          }
      //     { name:"日期与时间函数", 
      //      isParent:true, 
      //      children: [
      //         {name:"当前日期时间", value:"当前日期时间()", desc:"用途:返回当前日期时间<br>返回类型: 日期与时间 2018-01-01 12:00:00<br>示例: 当前日期时间()"},
      //         {name:"当前日期", value:"当前日期()", desc:"用途:返回当前日期<br>返回类型: 2018-01-01<br>示例: 当前日期()"},
      //         {name:"年份", value:"年份值()", desc:"用途:返回当前日期格式其年份<br>返回类型: 2018<br>示例: 年份值('2018-01-10')"},
      //         {name:"月份", value:"月份值()", desc:"用途:返回当前日期格式其月份<br>返回类型: 1<br>示例: 月份值('2018-01-10')"},
      //         {name:"日期", value:"日期值()", desc:"用途:返回当前日期格式其日期<br>返回类型: 10<br>示例: 日期值('2018-01-10')"},
      //         {name:"季度", value:"季度()", desc:"用途:返回当前日期格式其季度<br>返回类型: 1<br>示例: 季度('2018-01-10')"}
      //      ]
      //     },
      //    { name:"文本函数", isParent:true,
      //      children: [
      //         {name:"当前日期时间", value:"当前日期时间()", desc:"用途:返回当前日期时间<br>返回类型: 日期与时间 2018-01-01 12:00:00<br>示例: 当前日期时间()"},
      //         {name:"当前日期", value:"当前日期()", desc:"用途:返回当前日期<br>返回类型: 2018-01-01<br>示例: 当前日期()"},
      //         {name:"年份", value:"年份值()", desc:"用途:返回当前日期格式其年份<br>返回类型: 2018<br>示例: 年份值('2018-01-10')"},
      //         {name:"月份", value:"月份值()", desc:"用途:返回当前日期格式其月份<br>返回类型: 1<br>示例: 月份值('2018-01-10')"},
      //         {name:"日期", value:"日期值()", desc:"用途:返回当前日期格式其日期<br>返回类型: 10<br>示例: 日期值('2018-01-10')"},
      //         {name:"季度", value:"季度()", desc:"用途:返回当前日期格式其季度<br>返回类型: 1<br>示例: 季度('2018-01-10')"}
      //      ]
      //    },
      //    { name:"逻辑判断函数", isParent:true},
      //    { name:"常量", isParent:true},
      //    { name:"系统变量", isParent:true},
      //    { name:"数值函数", isParent:true},
      //    { name:"查找函数", isParent:true},
      //    { name:"统计函数", isParent:true},
       ];

       var defaultActionTreeObj = $.fn.zTree.init($("#functionTree"), setting, defaultActionNodes);
       defaultActionTreeObj.expandAll(true);

        $('#formular_edit_modal_ok_btn').click(function(){
            var treeObj = $.fn.zTree.getZTreeObj("actionTree");
            var nodes = treeObj.getNodes();
            var nodes_json = JSON.stringify(nodes);

            //redisplay tree
            var action_setting = {
                view: { selectedMulti: false},
                edit: { enable: false}
            };
            var data=$('#pc_events li.active').attr('data');
            if('list_events'==data){
                $.fn.zTree.init($("#list_event_tree"), action_setting, nodes);
                $('#list_event_action_json').val(nodes_json);
            }else{
                $.fn.zTree.init($("#edit_event_tree"), action_setting, nodes);
                $('#edit_event_action_json').val(nodes_json);
            }
            $('#formular_edit_modal').modal('hide');
        });

        function redisplayActionTree(nodes){
          actionCont.redisplayActionTree(nodes);
        }
        
        return {
        	redisplayActionTree:redisplayActionTree
        };
    
});