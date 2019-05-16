define(['jquery',  '../btns', '../add/detail_table', '../event/events','../value_change', '../drop_list_change', 
    '../table_drop_list', '../city_list', 'layer'],
    function ($, btnCont, tableCont, eventCont) {
    $(document).ready(function(){        
        document.title = '编辑 | ' + document.title;

        $(".HuiTab").Huitab({
            index:0
        });
        var layer_index = layer.load(1, {
            shade: [0.3,'#000'] //0.3透明度的黑色背景
        });
        var form_define_json = JSON.parse($("#form_define").text());

        var order_id = $('#order_id').val();
        var module_form_id = form_define_json.ID;
        console.log('edit.....');
        
        //这里用回调保证 先应用了 dataTable setting,  再取数据回显
        tableCont.callback(function(){
            
            $.post('/form/'+form_define_json.MODULE_ID+"-doGet-"+order_id,  function(data){
                console.log(data);
                fillFormData(data);
                eventCont.handle('event_edit_page_onload', form_define_json);//处理 编辑页面打开后 载入时的处理事件
                layer.close(layer_index); 
            });
        });
        

        var fillFormData = function(data){
            // 开始遍历 主表字段
            for ( var p in data ){ // 方法 
                if( typeof ( data[p]) == "function"){
                }else if(typeof ( data[p]) == "object" ){ 
                    var value = data[p]; 
                    var field_id = '[name=form_'+module_form_id+'-'+p.toLowerCase()+']';
                } else { // p 为属性名称，obj[p]为对应属性的值 
                    var value = data[p]; 
                    var field_id = '[name=form_'+module_form_id+'-'+p.toLowerCase()+']';
                    //console.log ( field_id +" = "+value ) ;
                    var element = $(field_id);
                    var type = element.attr('type');
                    var tagName = element.prop("tagName");
                    if(type == "text"){ //inputBox
                        element.val(data[p]);// 根据ID 显示所有的属性 
                    }else if(type == "radio"){
                        $(field_id+"[value='"+data[p]+"']").prop("checked",true);
                    }else if(tagName == "SELECT"){
                        element.find("option[value='"+data[p]+"']").attr("selected","selected");
                    }else if(tagName == "TEXTAREA"){
                        element.text(data[p]);
                    }else if(type == "checkbox"){
                        $(field_id).prop("checked",false);
                        if(data[p].indexOf(",")>0){
                            var array = data[p].split(",");
                            for(var i = 0;i<array.length;i++){
                                $(field_id+"[value='"+array[i]+"']").prop("checked",true);
                            }
                        }else{
                            $(field_id+"[value='"+data[p]+"']").prop("checked",true);
                        }
                    }else{//非input元素
                        if(element.hasClass('file_name')){//附件
                            var aHtml = '<a style="color:#06c;text-decoration: underline;" href="/upload/'+data[p]+'" download="'+data[p]+'">'+data[p]+'</a>';
                            element.html(aHtml);
                        }
                    }
                }
            } 
            if(data.FILE_FIELD_LIST.length>0){
                var field_file_list = data.FILE_FIELD_LIST;
                for ( var p in field_file_list){
                    var filelist = field_file_list[p].FILE_LIST;
                    if(filelist.length>0){
                        for(var i in filelist){
                            var returnStr = "<div style='margin-right:15px;float: left;'>"
                                +"       <a id='"+filelist[i].ID+"' style='color:#06c;text-decoration: underline;' href='" + filelist[i].IMG_URL+"' download='"+filelist[i].IMG_NAME+"'>"+filelist[i].IMG_NAME+"</a>"
                                +'       <span name="deleteFileBtn" style="cursor:pointer;color:red;font-size:20px;"><i class="Hui-iconfont"></i></span>'
                                +'</div>';
                            $("#f"+filelist[i].FIELD_ID).append(returnStr);
                        }
                    }
                }
            }

            if(data.IMGFIELDLIST.length>0){
                var imgfieldlist = data.IMGFIELDLIST;
                for ( var p in imgfieldlist){
                    var imglist = imgfieldlist[p].IMGLIST;
                    if(imglist.length>0){
                        for(var i in imglist){
                            var returnStr = "<div style='width:150px;height:150px;margin-right:10px;float: left;position:relative;'>"
                                +"<span name='deleteImgBtn' style='cursor:pointer;color:red;font-size:20px;position:absolute;left:87%;'><i class='Hui-iconfont'>&#xe706;</i></span>"
                                +"<img id='"+imglist[i].ID+"' name='"+imglist[i].IMG_NAME+"' src='"+imglist[i].IMG_URL+"' style='width: 150px;height:145px; max-width: 100%;max-height: 100%; '/></div>";
                            $("#f"+imglist[i].FIELD_ID).append(returnStr);
                        }
                    }
                }
            }
            
            // 开始遍历  从表
            for ( var p in data ){ // 方法 
                if( data[p] instanceof Array ){ 
                   var list = data[p]; 
                   $.each(list, function(index, item) {
                        var target_table_id = item.TABLE_ID;
                        var data_list = item.DATA_LIST;
                        var dataTable = $('#'+target_table_id).DataTable();
                        if(data_list){
                          for (var i = 0; i < data_list.length; i++) {
                              var item = data_list[i];
                              dataTable.row.add(item).draw();
                          }
                        }
                   });
                }
            } 
        };
    });
});
