define(['jquery', 'layer'], function ($) {
    console.log('header...');
    $('a.about').on('click', function(){
        layer.open({
            title:'关于我们',
            type: 1,
            area: ['600px'],
            shadeClose: true, //点击遮罩关闭
            content: "\<\h3 style='padding:20px;'>"+system_title+"\<\/h3>"+
                "\<\p class='pd-10'>"+office_desc+"\<\/p>"+
                "\<\p class='pd-10'>客服支持: "+office_support+"\<\/p><hr>"+
                "\<\h3 style='padding:20px;'>易得SAAS （V3.3.1）\<\/h3>"+
                "\<\p class='pd-10'>本系统基于易得SAAS软件开发平台构建，易得SAAS可以快速构建报表系统、ERP、OA、CRM、EAI、BI等适合用户自身需求特色的信息化系统。随着业务的不断发展和变化，易得SAAS可以快速、灵活的应需求而变。靓丽美观的界面、高效灵活的设计、简单易用的平台、费用低廉的价格、较强的伸缩和扩展性都会带给你耳目一新的非凡体验！\<\/p>"+
                "\<\p class='pd-10'>易得联系电话:0756-2160009 &nbsp;&nbsp;&nbsp;&nbsp; 客服QQ:1417381763\<\/p>"+
                "\<\p class='pd-10'>官网: \<a href='http://www.eeda123.com' target='_blank'>www.eeda123.com\<\/a>\<\/p>"
        });
    });
});