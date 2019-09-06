define(['jquery'], function ($) {
    //右侧流程设置切换
    $("body").on('click','.initiator_staff',function(){
        $(".initiator_info_div").show();
        $(".initiator_info_div").siblings().hide();
        $(".flow_div_right").animate({display:"block",width:"350px"});
    });
    $("body").on('click','.approval_staff',function(){
        $(".approval_info_div").show();
        $(".approval_info_div").siblings().hide();
        $(".flow_div_right").animate({width:"350px"});
    });
    $("body").on('click','.copy_staff',function(){
        $(".copy_info_div").show();
        $(".copy_info_div").siblings().hide();
        $(".flow_div_right").animate({width:"350px"});
    });

    //流程设置取消按钮
    $(".btn-cancel").click(function(){
        $(".flow_div_right").animate({width:"0px"});
    });

    //流程名称
    $(".edit_div_title").click(function(){
        $(this).css({"border":"1px solid #bbb","background-color":"#ddd","height": "34px"});
    }).blur(function(){
        $(this).css({"border":"1px solid #fff","background-color":"#fff","height": "34px"});
    });

    //审批人单选按钮的显示块
    $(".approval_people").click(function(){
        var radio_id = $(this).attr("id");
        if(radio_id=='approvalName1'){
            $(".appoint_member").show();
            $(".appoint_member").siblings().hide();
        }else if(radio_id=='approvalName2'){
            $(".optional").show();
            $(".optional").siblings().hide();
        }else if(radio_id=='approvalName3'){
            $(".oneself").show();
            $(".oneself").siblings().hide();
        }
    });

    //审批人范围下拉框
    $(".select_box").change(function(){
        var range_id=$("#range").val();
        var number_id=$("#number").val();
        console.log("人数："+number_id);
        console.log("范围："+range_id);
        if(range_id=='range_all' && number_id=='number_many'){
            $("#approva_mode_div").show();
        }else{
            $("#approva_mode_div").hide();
        }
        if(range_id=='range_appoint'){
            $("#adding_approver").css("display","inline-block");
        }else{
            $("#adding_approver").css("display","none");
        }
    });

    //删除流程
    $("body").on('click','.delete_div',function(event){
        // $(this).parent().parent().parent().parent().remove();
        $(this).parents(".node-wrap").eq(0).remove();
        $(".cancel_info_div").hide();
        return false;    
    });

    //新增流程选择框
    $("body").on('click','.add_btn',function(){
        var html = '<div id="add_process">'+
                        '<div id="xsj"></div>'+
                        '<div class="col-lg-12"><p id="close_add_process">×</p></div>'+
                        '<div class="col-lg-4 add_process_approval">'+
                        '    <div style="height:25%;background-color: rgb(241, 157, 102)"></div>'+
                        '    <div style="text-align: center;line-height: 30px;">审批人</div>'+
                        '</div>'+
                        '<div class="col-lg-4 add_process_copy">'+
                        '    <div style="height:25%;background-color: rgb(125, 137, 182)"></div>'+
                        '    <div style="text-align: center;line-height: 30px;">抄送人</div>'+
                        '</div>'+
                        '<div class="col-lg-4 add_process_branch">'+
                        '    <div style="height:25%;background-color: rgb(219, 205, 74)"></div>'+
                        '    <div style="text-align: center;line-height: 30px;">条件分支</div>'+
                        '</div>'+
                    '</div>';
        $("#add_process").remove();
        $(this).parent().after(html);
    });

    //关闭新增流程选择框
    $("body").on('click','#close_add_process',function(){
        $("#add_process").remove();
    });

    //新增审批人
    $("body").on('click','.add_process_approval',function(){
        var who_pressed_it = $(this).parent().parent().parent().attr("class");
        var html = '<div class="node-wrap approval_div">'+
                        '<div class="node-wrap-box approval_staff">'+
                        '<div>'+
                        '    <div class="title title_approval">'+
                        '        <span>审批人</span>'+
                        '        <span class="delete_div">×</span>'+
                        '    </div>'+
                        '    <div class="content">'+
                        '        <div class="text text_approval">发起人自选</div>'+
                        '        <span class="glyphicon glyphicon-chevron-right"></span>'+
                        '    </div>'+
                        '</div>'+
                        '</div>'+
                        '<div class="add-node-btn-box">'+
                        '    <div class="add-node-btn">'+
                        '        <button class="btn add_btn" type="button">'+
                        '            <span class="glyphicon glyphicon-plus Y-axis"></span>'+
                        '        </button>'+
                        '    </div>'+
                        '</div>'+
                    '</div>';
        if(who_pressed_it=='branch_box_wrap'){
            $(this).parent().parent().parent().parent().after(html);
        }else{
            $(this).parent().parent().parent().after(html);
        }
        $("#add_process").remove();
    });

    //新增抄送人
    $("body").on('click','.add_process_copy',function(){
        var who_pressed_it = $(this).parent().parent().parent().attr("class");
        var html = '<div class="node-wrap copy_div">'+
                        '<div class="node-wrap-box copy_staff">'+
                        '<div>'+
                        '    <div class="title title_copy">'+
                        '        <span>抄送人</span>'+
                        '        <span class="delete_div">×</span>'+
                        '    </div>'+
                        '    <div class="content">'+
                        '        <div class="text text_copy">发起人自选</div>'+
                        '        <span class="glyphicon glyphicon-chevron-right"></span>'+
                        '    </div>'+
                        '</div>'+
                        '</div>'+
                        '<div class="add-node-btn-box">'+
                        '    <div class="add-node-btn">'+
                        '        <button class="btn add_btn" type="button">'+
                        '            <span class="glyphicon glyphicon-plus Y-axis"></span>'+
                        '        </button>'+
                        '    </div>'+
                        '</div>'+
                    '</div>';
        if(who_pressed_it=='branch_box_wrap'){
            $(this).parent().parent().parent().parent().after(html);
        }else{
            $(this).parent().parent().parent().after(html);
        }
        $("#add_process").remove();
    });

    //条件分支
    $("body").on('click','.add_process_branch',function(){
        var who_pressed_it = $(this).parent().parent().parent().attr("class");
        var except_class = ".process_end,.top-left-cover-line,.bottom-left-cover-line,.top-right-cover-line,.bottom-right-cover-line";
        var html_arr="";
        if(who_pressed_it=='condition-node-box' || who_pressed_it=='branch_box_wrap'){
            html_arr = $(this).parent().parent().parent().parent().nextAll().not(except_class);
        }else{
            html_arr = $(this).parent().parent().parent().nextAll().not(except_class);
        }

        var old_nodes_html="";
        for(var i=0;i<html_arr.length;i++){
            var h = html_arr[i];
            h=$(h).prop("outerHTML");
            old_nodes_html+=h;
        }
        
        var branch_html = '<div class="node-wrap branch_condition">'+
                            '<div class="condition-node-box">'+
                            '<div class="node-wrap-box condition_staff">'+
                            '    <div class="title title_condition">'+
                            '        <span>条件1</span>'+
                            '        <span class="delete_branch">×</span>'+
                            '    </div>'+
                            '    <div class="content">'+
                            '        <div class="text text_condition">请设置条件</div>'+
                            '    </div>'+
                            '</div>'+
                            '<div class="add-node-btn-box">'+
                            '    <div class="add-node-btn">'+
                            '        <button class="btn add_btn" type="button">'+
                            '            <span class="glyphicon glyphicon-plus Y-axis"></span>'+
                            '        </button>'+
                            '    </div>'+
                            '</div>'+
                            '</div>'+
                    '</div>';
        var final = '<div class="branch_wrap">'+
                        '<div class="branch_box_wrap">'+
                        '   <div class="branch_box">'+
                        '       <div class="col-box">'+
                                    branch_html+
                                    old_nodes_html+
                        '            <div class="top-left-cover-line"></div>'+
                        '            <div class="bottom-left-cover-line"></div>'+
                        '       </div>'+
                        '       <div class="col-box">'+
                                    branch_html+
                        '            <div class="top-right-cover-line"></div>'+
                        '            <div class="bottom-right-cover-line"></div>'+
                        '       </div>'+
                        '   </div>'+
                        '   <div class="add-node-btn-box">'+
                        '        <div class="add-node-btn">'+
                        '           <button class="btn add_btn" type="button">'+
                        '               <span class="glyphicon glyphicon-plus Y-axis"></span>'+
                        '           </button>'+
                        '       </div>'+
                        '   </div>'+
                        '</div>'+
                    '</div>';
        if(who_pressed_it=='condition-node-box' || who_pressed_it=='branch_box_wrap'){//不加这个，在分支没有流程情况下再加个分支会加错位置
            $(this).parent().parent().parent().parent().after(final);
        }else{
            $(this).parent().parent().parent().after(final);
        }
        $(html_arr).remove();
        $("#add_process").remove();
    });

    //删除分支
    $("body").on('click','.delete_branch',function(){
        $(this).parents(".branch_wrap").eq(0).remove();
    });

});