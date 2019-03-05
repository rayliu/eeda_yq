define(['jquery', 'hui', '../btns', '../add/detail_table', '../value_change', '../drop_list_change', 
    '../table_drop_list', '../city_list', 'layer'],
    function ($, huiCont, btnCont, tableCont) {
//        $(".Hui-aside").Huifold({
//            titCell:'.menu_dropdown dl dt',
//            mainCell:'.menu_dropdown dl dd',
//        });
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
                    var type = $(field_id).attr('type');
                    var tagName = $(field_id).prop("tagName");
                    if(type == "text"){ //inputBox
                        $(field_id).val(data[p]);// 根据ID 显示所有的属性 
                    }else if(type == "radio"){
                        $(field_id+"[value='"+data[p]+"']").prop("checked",true);
                    }else if(tagName == "SELECT"){
                    	$(field_id).find("option[value='"+data[p]+"']").attr("selected","selected");
                    }else if(type == "checkbox"){
                    	if(data[p].indexOf(",")>0){
                    		var array = data[p].split(",");
                    		for(var i = 0;i<array.length;i++){
                        		$(field_id+"[value='"+array[i]+"']").prop("checked",true);
                        	}
                    	}else{
                    		$(field_id+"[value='"+data[p]+"']").prop("checked",true);
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
            					+"<span name='deleteImgBtn' style='cursor:pointer;background-color:#FFFFFF;font-size:20px;position:absolute;left:87%;'><i class='Hui-iconfont'>&#xe706;</i></span>"
            					+"<img id='"+imglist[i].ID+"' name='"+imglist[i].IMG_NAME+"' src='/upload/"+imglist[i].IMG_NAME+"' style='width: 150px;height:145px; max-width: 100%;max-height: 100%; '/></div>";
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
