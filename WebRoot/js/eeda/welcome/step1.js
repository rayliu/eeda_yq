define(['jquery', 'layer'], function ($) {
    $('.pic-box').click(function(){
        var el = $(this);
        $('.pic-box').each(function(){
            $(this).removeClass("selected");
        });
        if(el.hasClass("selected")){
            $(this).removeClass("selected")
        }else{
            $(this).addClass("selected")
        }
    });

    $("#nextBtn").click(function(){
        if($('.pic-box.selected').length!=1){
            layer.msg('请选择一个行业');
            return;
        }

        var val = $('.pic-box.selected').attr("data");
        layer.msg(val);
        $.post('/survey/step1save',{val:val},function(data){
            if(data=='ok')
                window.location.href='/survey/step2';
        });
    });

});