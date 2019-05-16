

define(['jquery', 'template', './field_ref', '../btns', 'mui', 'mui_loading', 'mui_picker',
      './city.data-3', '../charts/form_chart'], function ($, template, fieldRefCont, btnsCont) {
    template.defaults.debug=true;

    var VConsole = require(['/js/lib/vconsole/vconsole.min.js']); 
        // 初始化
    var vConsole = new VConsole();
   
    console.log('Hello world');
    
    mui.init();
    mui.ready(function() {
        mui.showLoading();
        var dtPicker = new mui.DtPicker(); 

        var module_id=$('#module_id').val();
        var form_id=$('#form_id').val();
        var order_id = $('#order_id').val();
        console.log('edit.....');

        var url = '/app/form/'+module_id+'-doGet-'+order_id;

        console.log('app doGet order.................');
        var form_fields = $("#form_fields");
        var app_form_list = $("#app_form_list");
        form_fields.empty();
        app_form_list.empty();
        $.get(url, function(result){
            console.log(result);
            window.form_define=result;

            var field_list = result.FIELD_LIST;
            var detail_tables = result.DETAIL_TABLES;
            if(!field_list) return;
            field_list.forEach(element => {
                var type = element.DISPLAY_TYPE;
                
                switch (element.FIELD_TYPE) {
                    case '图片':
                        var str = template('form_pic_field', { 
                            "id": element.ID,
                            "value":element.VALUE,
                            "name":"form_"+form_id+"-f"+element.ID+"_"+element.FIELD_NAME,
                            "display_name":element.DISPLAY_NAME,
                            "item_list":element.ITEM_LIST,
                        }); 
                        break;
                    case '附件':
                        var str = template('form_file_field', { 
                            "id": element.ID,
                            "value":element.VALUE,
                            "name":"form_"+form_id+"-f"+element.ID+"_"+element.FIELD_NAME,
                            "display_name":element.DISPLAY_NAME,
                            "item_list":element.ITEM_LIST,
                        }); 
                        break;
                    case '从表引用':
                        break;
                    case '下拉列表':
                        var str = template('form_select_field', { 
                            "value":element.VALUE,
                            "name":"form_"+form_id+"-f"+element.ID+"_"+element.FIELD_NAME,
                            "display_name":element.DISPLAY_NAME,
                            "item_list":element.ITEM_LIST,
                        }); 
                        break;
                    case '字段引用':
                        var str = template('form_dropdown_field', { 
                            "value":element.VALUE,
                            "name":"form_"+form_id+"-f"+element.ID+"_"+element.FIELD_NAME,
                            "display_name":element.DISPLAY_NAME,
                            "target_form_id":element.FIELD_REF.TARGET_FORM,
                            "target_field_name":element.FIELD_REF.TARGET_FIELD_NAME,
                            "item_list":element.FIELD_REF.ITEM_LIST,
                        }); 
                        break;
                    case '复选框':
                        console.log(element.ITEM_LIST);
                        if(element.IS_SINGLE_CHECK=='Y'){
                            var str = template('form_radio_field', { 
                                "value":element.VALUE,
                                "name":"form_"+form_id+"-f"+element.ID+"_"+element.FIELD_NAME,
                                "display_name":element.DISPLAY_NAME,
                                "item_list":element.ITEM_LIST,
                            });
                        }else{
                            //自定义函数
                            template.helper("checkValue",function(value, item){
                                console.log(value);//同样可以打印日志到控制台
                                if(value.indexOf(item.NAME)>-1){
                                    return "Y"
                                }else {
                                    return "N"
                                }
                            });
                            var str = template('form_checkbox_field', { 
                                "value":element.VALUE,
                                "name":"form_"+form_id+"-f"+element.ID+"_"+element.FIELD_NAME,
                                "display_name":element.DISPLAY_NAME,
                                "item_list":element.ITEM_LIST,
                            });
                        }
                        break;
                    default:
                        var str = template('form_input_field', { 
                            "value":element.VALUE,
                            "name":"form_"+form_id+"-f"+element.ID+"_"+element.FIELD_NAME,
                            "display_name":element.DISPLAY_NAME
                        }); 
                        break;
                }
                
                form_fields.append(str);
                //字段生成后，绑定事件
                var field_name="form_"+form_id+"-f"+element.ID+"_"+element.FIELD_NAME;
                switch (element.FIELD_TYPE) {
                    case '字段引用':
                        var btn = document.getElementsByName(field_name)[0];
                        fieldRefCont.bindTap(btn);
                        break;
                    case '日期时间':
                        var dateField = document.getElementsByName(field_name)[0];
                        dateField.addEventListener('tap', function(){
                            dtPicker.show(function (selectItems) { 
                                $(dateField).val(selectItems.value);
                            })
                        });
                        break;
                    case '全国城市':
                        var cityPicker3 = new mui.PopPicker({
                            layer: 3
                        });
                        cityPicker3.setData(cityData3);
                        var field = document.getElementsByName(field_name)[0];
                        field.addEventListener('tap', function(){
                            cityPicker3.show(function (items) { 
                                var name = (items[0] || {}).text + "-" + (items[1] || {}).text + "-" + (items[2] || {}).text
                                $(field).val(name);
                            })
                        });
                        break;
                    default:
                        break;
                }

                
                $('.pic_list').on('click', '.mui-icon-closeempty', btnsCont.imgDelete);
                $('.file_list').on('click', '.mui-icon-closeempty', btnsCont.fileDelete);
                
            });

            //file 回显
            if(result.FILE_FIELD_LIST.length>0){
                var file_field_list = result.FILE_FIELD_LIST;
                for ( var p in file_field_list){
                    var file_list = file_field_list[p].FILE_LIST;
                    if(file_list.length>0){
                        for(var i in file_list){
                            var strHtml = ""
                                        +'<li style="list-style-type:none;clear: both;margin-bottom: 1.2rem;">'
                                        +'    <div>'
                                        +'        <span class="mui-icon-extra mui-icon-extra-order" style="margin-right: 5px;float:left;padding: 1.2rem;height:4rem;width:4rem;border: solid 1px #eee;position: relative;"></span>'
                                        +'        <a id="'+file_list[i].ID+'" href="'+file_list[i].IMG_URL+'" class="mui-ellipsis" style="float:left;width:65%;margin-top: 6%;">'+file_list[i].IMG_NAME+'</a>'
                                        +'        <span class="mui-icon mui-icon-closeempty" style="margin-top: 6%;background-color: #f0d54e;border-radius: 15px;"></span>'
                                        +'    </div>'
                                        +'</li>';
                            $("#"+file_list[i].FIELD_ID+" ul").append(strHtml);
                        }
                    }
                }
            }

            //图片回显
            if(result.IMG_FIELD_LIST.length>0){
                var imgfieldlist = result.IMG_FIELD_LIST;
                for ( var p in imgfieldlist){
                    var img_list = imgfieldlist[p].IMG_LIST;
                    if(img_list.length>0){
                        for(var i in img_list){
                            var strHtml = "";
                            strHtml += "<span>";
                            strHtml += "<img id='"+img_list[i].ID+"' src='"+img_list[i].IMG_URL+"' style='height:4rem;width:4rem;border: solid 1px #eee;margin-top: 10px;'/>";
                            strHtml += '<span class="mui-icon mui-icon-closeempty"></span>'
                            strHtml += '</span>';
                            $(strHtml).insertBefore($("#"+img_list[i].FIELD_ID+" .add_pic_btn"));
                        }
                    }
                }
            }
            
            //处理从表
            if(detail_tables.length>0){
                detail_tables.forEach(el=> {
                    var tab_name = el.TABLE_NAME;
                    var tab_id = el.TABLE_ID;
                    var tab_html='<a class="mui-control-item" href="#'+tab_id+'">'+ tab_name+'</a>';
                    var tab_container_html='<div id="'+tab_id+'" class="mui-slider-item mui-control-content table" style="background-color: #fff;margin-top: 5px;">'
                                                +'<ul class="mui-table-view mui-table-view-striped mui-table-view-condensed"></ul>'
                                            +'</div>';
                    $('#sliderSegmentedControl').append(tab_html);
                    $('#module_form .mui-slider-group').append(tab_container_html);
                    var table_ul = $('#'+tab_id+" ul");

                    var data_list = el.DATA_LIST;
                    var field_list = el.FIELD_LIST;
                    if(data_list.length>0){
                        data_list.forEach(data=>{
                            var field_str="";
                            var keys = [];
                            for (const key in data) {
                                keys.push(key);
                            }
                            keys = keys.sort();
                            
                            for(var i=0; i<keys.length; i++){
                                const key = keys[i];
                                console.log(key+":"+data[key]);
                                const value = data[key];
                                //根据key找到显示中文名
                                var display_name="";
                                field_list.forEach(field=>{
                                    if(field.REAL_NAME.toUpperCase()==key){
                                        display_name=field.FIELD_DISPLAY_NAME
                                    }
                                });
                                if(display_name.length>0){
                                    var h5_html='<div class="mui-ellipsis" style="display:block;">'
                                            +'<div style="width:30%;float: left;">'+display_name+'</div>'
                                            +'<input type="text" display_name="'+display_name+'" name="'+key+'" value="'+value+'" style="width: 60%;height: initial;padding: 0px 15px;" disabled></div>';
                                }else{
                                    var h5_html='<h5 class="mui-ellipsis" style="display:none;">'+display_name+': <input type="text" name="'+key+'" value="'+value+'" ></h5>';
                                }
                                field_str+=h5_html;
                            }

                            var li_html="<li class='mui-table-view-cell' id='"+data.ID+"' table_id='"+tab_id+"'>"
                                            +'<div class="mui-table">'
                                            +'    <div class="mui-table-cell mui-col-xs-10">'
                                            +         field_str
                                            +'    </div>'
                                            +'    <div class="mui-table-cell mui-col-xs-2 mui-text-right">'
                                            +'        <a class="mui-tab-item edit" href="#edit" style="display: block;margin-top: .3rem;"><span class="mui-icon mui-icon-compose"></span></a>'
                                            +'        <a class="mui-tab-item delete" href="#delete" style="display: block;margin-top: 1.3rem;"><span class="mui-icon mui-icon-trash"></span></a>'
                                            +'    </div>'
                                            +'</div>'
                                        +"</li>";
                            table_ul.append(li_html);

                        });

                        mui('#'+tab_id+" ul").on('tap', 'a.edit', function(){
                            var $ul = $('#edit_popover ul');
                            $ul.empty();

                            var $li=$(this).closest('li');
                            $('#edit_popover_table_id').val($li.attr('table_id'));
                            $('#edit_popover_li_id').val($li.attr('id'));
                            var input_arr = $li.find('input');
                            input_arr.each(function(index, input){
                                var display_name = $(input).attr('display_name');
                                var name = $(input).attr('name');
                                var value = $(input).val();
                                if(!display_name) return;//continue
                                var li_html='<li class="mui-table-view-cell">'
                                +'    <div class="mui-input-row">'
                                +'        <label>'+display_name+'</label>'
                                +'        <input type="text" name="'+name+'" placeholder="请输入..." value="'+value+'">'
                                +'    </div>'
                                +'</li>';
                                $ul.append(li_html);
                                
                            });
                            
                            mui('#edit_popover').popover('toggle');
                        });

                        mui('#'+tab_id+" ul").on('tap', 'a.delete', function(){
                            var $li=$(this).closest('li');
                            var btnArray = ['是', '否'];
                            mui.confirm('', '确认删除当前记录？', btnArray, function(e) {
                                if (e.index == 0) {
                                    $li.remove();
                                } else {
                                    
                                }
                            })
                        });
                    }else{
                        table_ul.append("没有数据");
                    }
                    
                    
                });
                $('#sliderSegmentedControl').show();
            }

            mui.hideLoading();
            //从表编辑
            var edit_popover_ok_btn = document.getElementById('edit_popover_ok');
            edit_popover_ok_btn.addEventListener('tap', function(){
                var table_id = $('#edit_popover_table_id').val();
                var li_id =$('#edit_popover_li_id').val();
                var $li = $('#'+table_id+' li[id='+li_id+']');

                if(li_id == ''){//新增
                    $table_ul = $('#'+table_id+' ul');
                    var field_str="";
                    var input_arr = $('#edit_popover ul input');
                    input_arr.each(function(index, input){
                        var display_name = $(input).attr('display_name');
                        var name = $(input).attr('name');
                        var value = $(input).val();
                        
                        var h5_html='<div class="mui-ellipsis" style="display:block;">'
                                +'<div style="width:30%;float: left;">'+display_name+'</div>'
                                +'<input type="text" display_name="'+display_name+'" name="'+name+'" value="'+value+'" style="width: 60%;height: initial;padding: 0px 15px;" disabled></div>';
                        
                        field_str+=h5_html;
                    });
                    var li_html="<li class='mui-table-view-cell' id='' table_id='"+table_id+"'>"
                                    +'<div class="mui-table">'
                                    +'    <div class="mui-table-cell mui-col-xs-10">'
                                    +         field_str
                                    +'    </div>'
                                    +'    <div class="mui-table-cell mui-col-xs-2 mui-text-right">'
                                    +'        <a class="mui-tab-item edit" href="#edit" style="display: block;margin-top: .3rem;"><span class="mui-icon mui-icon-compose"></span></a>'
                                    +'        <a class="mui-tab-item delete" href="#delete" style="display: block;margin-top: 1.3rem;"><span class="mui-icon mui-icon-trash"></span></a>'
                                    +'    </div>'
                                    +'</div>'
                                +"</li>";
                    $table_ul.append(li_html);
                }else{//编辑
                    var input_arr = $('#edit_popover ul input');
                    input_arr.each(function(index, input){
                        var name = $(input).attr('name');
                        var value = $(input).val();
                        var $target_input = $li.find('input[name='+name+']');
                        $target_input.val(value);
                    });
                }
                mui('#edit_popover').popover('toggle');
            });

            var edit_popover_cancel_btn = document.getElementById('edit_popover_cancel');
            edit_popover_cancel_btn.addEventListener('tap', function(){
                mui('#edit_popover').popover('toggle');
            });            

        });
    });//mui ready
});
