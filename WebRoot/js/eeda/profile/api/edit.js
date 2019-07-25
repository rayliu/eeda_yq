define(['jquery','hui', 'layer'], function ($,huiCont) {
    $(document).ready(function() {
        document.title = '系统API | '+document.title;
        var myTextarea = document.getElementById('editor');
        var codeMirrorEditor = CodeMirror.fromTextArea(myTextarea, {
            mode: "text/x-java",
            lineNumbers: true,
            matchBrackets: true, 
            theme: "material"
        });
        
        //监听提交
        $('#save_btn').click(function(data){
            var order = {

            }
            $("#orderForm input:enabled, select").each(function(){
                var type = $(this).attr("name");
                if(type.length>0){
                    order[type] = $(this).val();
                }
            });
            order["codes"] = codeMirrorEditor.getValue();
            $.post("/webadmin/java/save",{params:JSON.stringify(order)},function(data){
                if(data.RESULT){
                    layer.msg('保存成功', {icon: 1});
                    var stateObj = { foo: "create" };
                    history.pushState(stateObj, "edit", "edit?id="+data.ID);
                }else{
                    layer.msg('保存失败', {icon: 2});
                }
            });
            return false;
        });
    });
});