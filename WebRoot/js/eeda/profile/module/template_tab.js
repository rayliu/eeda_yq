define(['jquery', 'template', 'sco', '/js/lib/ueditor/ueditor.config.js', 'ueditor'], function ($, template) {
    $(document).ready(function() {

        var ue_config = {
            allowDivTransToP:false, initialFrameHeight: 600,
            filterTxtRules:function() {
                function transP(node) {
                    node.tagName = 'p';
                    node.setStyle();
                }
                return {
                    //直接删除及其字节点内容
                    '-': 'script style object iframe embed input select',
                    'p': {
                        $: {}
                    },
                    'br': {
                        $: {}
                    },
                    'li': {
                        '$': {}
                    },
                    'caption': transP,
                    'th': transP,
                    'tr': transP,
                    'h1': transP,
                    'h2': transP,
                    'h3': transP,
                    'h4': transP,
                    'h5': transP,
                    'h6': transP,
                    'td': function(node) {
                        //没有内容的td直接删掉
                        var txt = !! node.innerText();
                        if (txt) {
                            node.parentNode.insertAfter(UE.uNode.createText('    '), node);
                        }
                        node.parentNode.removeChild(node, node.innerText())
                    }
                }
            }()
        };

    	var ue = UE.getEditor('container', ue_config);

        var app_ue = UE.getEditor('app_container', ue_config);

        $('#appRefreshBtn').click(function(){
            var form_input_field=
                    '<div class="mui-input-row">'+
                        '<label>{{label}}</label>'+
                        '<input type="text" placeholder="普通输入框" disabled value="{{value}}">'+
                    '</div>';
            var render = template.compile(form_input_field);
            var app_content = app_ue.getContent();
            var doms = $.parseHTML(app_content);//解析Html串
            //var pList = $(doms).children("p");//children()方法：查找img元素
            var dataArr=[];
            var form_fields = $("#app_iframe").contents().find("#form_fields");
            var app_form_list = $("#app_iframe").contents().find("#app_form_list");
            form_fields.empty();
            app_form_list.empty();
            if(!doms)
                return;
            doms.forEach(element => {
                var field_name = $(element).text()
                if(field_name.indexOf(".")>0){
                    field_name=field_name.replace("#{", "").replace("}", "").split(".")[1];
                    if(field_name.indexOf("明细表")>=0 || field_name.indexOf("从表")>=0){
                        var htmlStr = '<li class="mui-table-view-cell">'+
                            '<a class="mui-navigate-right" href="javascript:void(0)">'+
                            '    明细表'+
                            '</a>'+
                        '</li>'; 
                        //dataArr.push(data);
                        app_form_list.append(htmlStr);
                    }else{
                        data={"label":field_name, "value":"demo", "disabled":true};
                        var htmlStr = render(data); 
                        //dataArr.push(data);
                        form_fields.append(htmlStr);
                    }
                    
                }
            });
                
            
        });
    });
 });