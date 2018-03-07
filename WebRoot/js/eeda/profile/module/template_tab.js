define(['jquery', 'sco', '/js/lib/ueditor/ueditor.config.js', 'ueditor'], function ($) {
    $(document).ready(function(template) {

    	var ue = UE.getEditor('container', {allowDivTransToP:false, initialFrameHeight: 600,
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
                });
    });
 });