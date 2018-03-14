define(['jquery', './list_tree', './fields', './template_tab', './btns', './events', './edit_events', './permission', './auth',
         './print_template/print_template', './interface/interface', 'zTree'], 
    function ($, listCont, fieldContr, templateCont, btnsCont, eventsCont,editEventCont, perCont, authCont, printCont, interfaceCont) {

        $(document).ready(function() {

            var saveAction=function(btn, is_start){
            is_start = is_start || false; 
            var ue = UE.getEditor('container');
            var dto = {
                module_id: $('#module_id').text(),
                info:{
                    name: $('#form_name').val(),
                    code: $('#form_code').val(),
                },
                field_update_flag:fieldContr.field_update_flag(),
                fields: fieldContr.buildFieldsDetail(),
                template_content: ue.getContent(),
                btn_update_flag: btnsCont.btn_update_flag(),
                btns: btnsCont.buildTableDetail(),
                event_update_flag:eventsCont.listEvent_update_flag(),
                editEvent_update_flag:editEventCont.editEvent_update_flag(),
                events: eventsCont.buildTreeNodes(),
                permission_list: eeda.buildTableDetail('permission_table', perCont.deletedPermisstionTableIds),
                auth_list: authCont.buildAuthTableDetail(),
                print_template: printCont.buildPrintTemplateDetail(),
                interface: interfaceCont.buildDetail()
            };

            console.log('saveBtn.click....');
            console.log(dto);

            //异步向后台提交数据
            $.post('/module/saveStructure', {params:JSON.stringify(dto)}, function(data){
                var order = data;
                console.log(order);
                if(order.ID>0){
                	//回显
                    var form_field_list = order.FORM_FIELD_LIST;
                    fieldContr.refresh_table(form_field_list);
                    //
                    var btn_list_query = order.BTN_LIST_QUERY;
                    var btn_list_edit = order.BTN_LIST_EDIT;
                    btnsCont.refresh_table(btn_list_query,btn_list_edit);
                    //
                    var permission_list = order.PERMISSION_LIST;
                    perCont.refresh_table(permission_list);
                    //
                    var module_role_list = order.MODULE_ROLE_LIST;
                    authCont.refresh_table(module_role_list);
                    //
                    var editEventList = order.EDITEVENTLIST;
                    editEventCont.refresh_table(editEventList);
                    
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                    btn.attr('disabled', false);
                }else{
                    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                    btn.attr('disabled', false);
                }
            },'json').fail(function() {
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                btn.attr('disabled', false);
            });
        };


        $('#saveBtn').on('click', function(e){
            $(this).attr('disabled', true);

            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            //提交前，校验数据
            // if(!$("#orderForm").valid()){
            //     return;
            // }
            //提交前，校验数据
            if(!fieldContr.check()){
            	alert("字段列表尚有操作未完成");
                return;
            }
            saveAction($(this));
        });

        $('.formular_pop').on('click', function(e){
            var targetId = $(this).attr('target');
            $('#formular_edit_modal_target_id').val(targetId);

            $('#formular_edit_modal').modal('show');
        });


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
            // async: {
            //     enable: true,
            //     type: 'get',
            //     url:"/module/searchFormBtnEvents",
            //     autoParam:["id", "formId", "level=lv"],
            //     dataFilter: dataFilter//处理返回来的JSON 变为 nodes
            // },
            callback: {
            //     beforeRename: beforeRename,
            //     onRename: onRename,
            //     beforeDrop: beforeDrop,//判断禁止模块拖拽到模块下
            //     onDrop: onDrop,
                 onClick: onNodeClick,
                 onDblClick: onNodeDblClick
            }
        };

        function onNodeDblClick(event, treeId, treeNode){
          if (treeNode.level==0 ) return;

          var oldStr = $('#formular_edit_modal_formular').text();
          $('#formular_edit_modal_formular').text(oldStr + treeNode.value);
        }

        function removeHoverDom(treeId, treeNode) {
            $("#addBtn_"+treeNode.tId).unbind().remove();
        };

        function onNodeClick(event, treeId, treeNode){
          if (treeNode.level==0 ) return;

          $('#function_desc').html(treeNode.desc);
        }

        var zNodes = [
           { name:"日期与时间函数", 
             isParent:true, 
             children: [
                {name:"当前日期时间", value:"当前日期时间()", desc:"用途:返回当前日期时间<br>返回类型: 日期与时间 2018-01-01 12:00:00<br>示例: 当前日期时间()"},
                {name:"当前日期", value:"当前日期()", desc:"用途:返回当前日期<br>返回类型: 2018-01-01<br>示例: 当前日期()"},
                {name:"年份", value:"年份值()", desc:"用途:返回当前日期格式其年份<br>返回类型: 2018<br>示例: 年份值('2018-01-10')"},
                {name:"月份", value:"月份值()", desc:"用途:返回当前日期格式其月份<br>返回类型: 1<br>示例: 月份值('2018-01-10')"},
                {name:"日期", value:"日期值()", desc:"用途:返回当前日期格式其日期<br>返回类型: 10<br>示例: 日期值('2018-01-10')"},
                {name:"季度", value:"季度()", desc:"用途:返回当前日期格式其季度<br>返回类型: 1<br>示例: 季度('2018-01-10')"}
             ]
           },
           { name:"文本函数", isParent:true,
             children: [
                {name:"当前日期时间", value:"当前日期时间()", desc:"用途:返回当前日期时间<br>返回类型: 日期与时间 2018-01-01 12:00:00<br>示例: 当前日期时间()"},
                {name:"当前日期", value:"当前日期()", desc:"用途:返回当前日期<br>返回类型: 2018-01-01<br>示例: 当前日期()"},
                {name:"年份", value:"年份值()", desc:"用途:返回当前日期格式其年份<br>返回类型: 2018<br>示例: 年份值('2018-01-10')"},
                {name:"月份", value:"月份值()", desc:"用途:返回当前日期格式其月份<br>返回类型: 1<br>示例: 月份值('2018-01-10')"},
                {name:"日期", value:"日期值()", desc:"用途:返回当前日期格式其日期<br>返回类型: 10<br>示例: 日期值('2018-01-10')"},
                {name:"季度", value:"季度()", desc:"用途:返回当前日期格式其季度<br>返回类型: 1<br>示例: 季度('2018-01-10')"}
             ]
           },
           { name:"逻辑判断函数", isParent:true},
           { name:"常量", isParent:true},
           { name:"系统变量", isParent:true},
           { name:"数值函数", isParent:true},
           { name:"查找函数", isParent:true},
           { name:"统计函数", isParent:true},
         ];

         var zTreeObj = $.fn.zTree.init($("#functionTree"), setting, zNodes);

         $('.formular_operator button').click(function(event) {
            var btn = $(this);
            var oldStr = $('#formular_edit_modal_formular').text();
            $('#formular_edit_modal_formular').text(oldStr + btn.text);
         });

         $('#formular_edit_modal_ok_btn').click(function(event) {
            var targetId = $('#formular_edit_modal_target_id').val();
            $('#'+targetId).text($('#formular_edit_modal_formular').text());
            $('#formular_edit_modal').modal('hide');
         });
    });
});