define(['jquery', 'dataTablesBootstrap', 'sb_admin', 'validate_cn', 'sco'], function ($) {
    $(document).ready(function() {


        $('#saveBtn').click(function(event) {
            if(!$('#orderForm').valid())
                return;

            var officeId = $('#officeId').val();
            var smtp = $('#smtp').val();
            var smtpPort = $('#smtpPort').val();
            var userName = $('#mailLoginName').val();
            var userPwd = $('#mailLoginPwd').val();
            
            $.post('/mailConfig/save', 
                {officeId: officeId, smtp: smtp, smtpPort:smtpPort, userName: userName, userPwd:userPwd}, 
                function(data, textStatus, xhr) {
                if(data=='OK'){
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                }else{
                    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                }
            });
        });

        $('#sendTestBtn').click(function(event) {
            var officeId = $('#officeId').val();
            $.post('/mailConfig/sendMail', {officeId:officeId}, function(data, textStatus, xhr) {
                if(data=='OK'){
                    $.scojs_message('测试邮件已成功发送至：'+$('#mailLoginName').val(), $.scojs_message.TYPE_OK);
                }else{
                    $.scojs_message('发送失败', $.scojs_message.TYPE_ERROR);
                }
            });
        });
    });//$(document).ready
});