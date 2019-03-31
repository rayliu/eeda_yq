define(['jquery', './list_tree', './fields', './custom_search/custom_search', './template_tab', './btns', './events', './edit_events', './permission', './auth',
         './print_template/print_template', './interface/interface', './charts/charts', 'zTree', 'layer'], 
    function ($, listCont, fieldContr, customSearchCont, templateCont, btnsCont, eventsCont,editEventCont, 
        perCont, authCont, printCont, interfaceCont, chartsCont) {

        $(document).ready(function() {
            
            var saveAction=function(btn, is_start){
                var layer_index = layer.load(1, {
                    shade: [0.3,'#000'] //0.3透明度的黑色背景
                });
                is_start = is_start || false; 
                var ue = UE.getEditor('container');
                var app_ue = UE.getEditor('app_container');
                var dto = {
                    module_id: $('#module_id').text(),
                    info:{
                        name: $('#form_name').val(),
                        type: $('input[name="form_type"]:checked').val(),
                        code: $('#form_code').val(),
                        desc: $('#desc').val(),
                        is_public: $('#is_public').prop("checked")==true?'Y':'N',
                        is_home_index: $('#is_home_index').prop("checked")==true?'Y':'N',
                        is_single_record: $('#is_single_record').prop("checked")==true?'Y':'N'
                    },
                    field_update_flag:fieldContr.field_update_flag(),
                    fields: fieldContr.buildFieldsDetail(),
                    template_content: ue.getContent(),
                    app_template_content: app_ue.getContent(),
                    btn_update_flag: btnsCont.btn_update_flag(),
                    btns: btnsCont.buildTableDetail(),
                    event_update_flag:eventsCont.listEvent_update_flag(),
                    editEvent_update_flag:editEventCont.editEvent_update_flag(),
                    events: eventsCont.buildTreeNodes(),
                    customSearch: customSearchCont.buildDetail(),
                    permission_list: eeda.buildTableDetail('permission_table', perCont.deletedPermisstionTableIds),
                    auth_list: authCont.buildAuthTableDetail(),
                    print_template: printCont.buildPrintTemplateDetail(),
                    interface: interfaceCont.buildDetail(),
                    charts: chartsCont.buildDetail()
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
                        //
                        customSearchCont.clear();
                        customSearchCont.sourceDisplay(order.CUSTOM_SEARCH_SOURCE);
                        customSearchCont.sourceConditionDisplay(order.CUSTOM_SEARCH_SOURCE_CONDITION);
                        customSearchCont.colsDisplay(order.CUSTOM_SEARCH_COLS);
                        customSearchCont.filterDisplay(order.CUSTOM_SEARCH_FILTER);
                        chartsCont.display(order.CHARTS);
                        
                        layer.close(layer_index); 
                        $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                        btn.attr('disabled', false);
                        //保存成功后刷新一下module
                        listCont.refresh_module_info(order.ID, order.MODULE_NAME);
                    }else{
                        layer.close(layer_index); 
                        $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                        btn.attr('disabled', false);
                    }
                },'json').fail(function() {
                    layer.close(layer_index); 
                    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                    btn.attr('disabled', false);
                });
            };


        $('#saveBtn').on('click', function(e){
            
            var self = $(this);
            $(this).attr('disabled', true);

            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            //提交前，校验数据
            // if(!$("#orderForm").valid()){
            //     return;
            // }
            //提交前，校验数据
            if(!fieldContr.check()){
                $(this).attr('disabled', false);
            	alert("字段列表尚有操作未完成");
                return;
            }
            
            var module_id = $('#module_id').text();
            if($("#is_home_index").prop("checked")){
            	checkExistIndex(self);
            }else{
                saveAction(self);
            }
        });

        var checkExistIndex = function(self){
            var module_id = $('#module_id').text();
            $.post("/module/checkExistIndex",{module_id:module_id},function(data){
                if(data.length>0){
                	 var mymessage = confirm("已有设为首页的页面，是否继续？");
                     if(!mymessage){
                         self.attr('disabled', false);
                         return false;
                     }
                     saveAction(self);
                }else{
                    saveAction(self);
                }
            });
        }

        $('.formular_pop').on('click', function(e){
            var targetId = $(this).attr('target');
            $('#formular_edit_modal_target_id').val(targetId);
            if("custom_filter_condition"==targetId){
            	$('#formular_edit_modal_formular').val($("#"+targetId).val());
            	var names = $("#custom_cols_table").DataTable().$("input[name='expression']");
            	$("#field").html("");
            	for(var i = 0;i<names.length;i++){
            		$("#field").append("<span class='list-group-item' style='cursor:pointer;'>"+names[i].defaultValue+"</span>");
            	}
            }else if("list_event_open_condition"==targetId){
            	$("#field").html("");
            }else if("list_event_set_css_condition"==targetId){
            	$("#field").html("");
            }else if("list_event_set_value_condition"==targetId){
            	$("#field").html("");
            }else if("list_event_save_set_value_condition"==targetId){
            	$("#field").html("");
            }else{
            	$("#field").html("");
            }
            
            $('#formular_edit_modal').modal('show');
        });

        //-------选择引用表单 start----
        var form_select_pop_dataTable = eeda.dt({
            id: 'form_select_pop_dataTable',
            paging: true,
            lengthChange: false,
            columns: [
                { "data": "ID", "width": "30px",
                    "render": function ( data, type, full, meta ) {
                        var id='';
                        if(data){
                          id=data;
                        }
                      return '<input type="radio" name="checkBox" style="margin-right:5px;">'
                      +'<input name="id" type="hidden" value="'+id+'">';
                    }
                },
                { "data": "NAME",
                    "render": function ( data, type, full, meta ) {
                        var str = "";
                        if(data){
                            str = data;
                        }
                        return data;
                     }
                }
            ]
          });

        $('.form_select_pop').on('click', function(e){
            var url="/module/getAllForms";
            form_select_pop_dataTable.ajax.url(url).load();
            var targetId = $(this).attr('target');
            $('#form_select_modal_target_id').val(targetId);
            $('#form_select_modal').modal('show');
        });

        $('#form_select_modal_ok_btn').click(function(event) {
            var targetId = $('#form_select_modal_target_id').val();
            var form_name = $('#form_select_pop_dataTable td input[type=radio]:checked').closest('tr').find('td:nth-child(2)').text();
            $('#'+targetId).val(form_name);
            $('#form_select_modal').modal('hide');
         });
        //-------选择引用表单 end----

        

        $("#field").on("click",".list-group-item",function(){
        	$("#formular_edit_modal_formular").val($('#formular_edit_modal_formular').val()+$(this).text());
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

          var oldStr = $('#formular_edit_modal_formular').val();
          $('#formular_edit_modal_formular').val(oldStr + treeNode.value);
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
            var oldStr = $('#formular_edit_modal_formular').val();
            $('#formular_edit_modal_formular').val(oldStr + btn.text());
         });

         $('#formular_edit_modal_ok_btn').click(function(event) {
            var targetId = $('#formular_edit_modal_target_id').val();
            $('#'+targetId).val($('#formular_edit_modal_formular').val());
            $('#formular_edit_modal').modal('hide');
         });

         //切换form类型
         $('#addProductDiv input[name=form_type]').change(function(e){
            var val = this.value;
            if(val=='form'){
                //显示所有tab 1-基本信息  2字段 3自定义查询 4表单模板 5工具栏按钮 6事件响应 7回写公式 8打印模板 9数据接口 10图表
                $('#tablist li').show();
                $('#tablist li.customize_search').hide();
                $('#tablist li.charts').hide();
            }else if(val=='search_form'){
                $('#tablist li').hide();
                //显示自定义查询tab
                $('#tablist li.info'
                +', #tablist li.customize_search'
                +', #tablist li.toolbar'
                +', #tablist li.event').show();
            }else{
                $('#tablist li').hide();
                //显示图表tab
                $('#tablist li.info'
                +', #tablist li.customize_search'
                +', #tablist li.charts').show();
            }
            //跳回tab第一页
            $("#displayDiv ul[role=tablist] li:first a").click();

         });
    });
});