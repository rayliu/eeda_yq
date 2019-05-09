define(['jquery','file_upload','sco'], function ($, printCont,metisMenu) {
    $(document).ready(function(){
    $('#action_popover a').click(function(){
        var btn_id=$(this).attr('btn_id');
        var module_id = $('#module_id').val();
        var order_id = $('#order_id').val();
        $.post('/form/'+module_id+'-click-'+btn_id,{order_id:order_id}, function(results){
            console.log(results);
            if(results.ERROR_CODE==500){
                mui.alert('操作失败,请刷新页面或联系客服查看错误。');
                return;
            }
            if(!results) return;
            for (var i = 0; i < results.length; i++) {
                var result = results[i];
                var event_id = results[i].ID;
                var form_id = results[i].FORM_ID
                var event_json = results[i].EVENT_JSON;
                if(!event_json) continue;
                var event = JSON.parse(event_json)[0];
                var condition = "";//TODO:
                var actions=event.children;
                for (var j = 0; j < actions.length; j++) {
                    var action = actions[j];
                    var action_type=action.action_type;
                    console.log('action_type='+action_type);
                    switch (action_type) {
                        case 'open_link'://打开链接
                            var event_action_setting=action.event_action_setting;
                            var radio_open_link=event_action_setting.radio_open_link;
                            switch (radio_open_link) {//打开链接有四种情况
                                case 'open_form':
                                    var module_id = event_action_setting.module_id;
                                    var url = '/app/form/'+module_id+'-list';
                                    if(event_action_setting.open_form_type=='edit'){
                                        url = '/app/form/'+module_id+'-add';
                                    }else{
                                        url = '/app/form/'+module_id+'-list';
                                    }
                                    if(event_action_setting.open_link_type == 'new'){
                                        window.open(url);
                                    }else if(event_action_setting.open_link_type = 'current'){
                                        window.location.href=url;
                                    }
                                    break;
                                case 'reload':
                                    //window.location.reload();
                                    break
                                default:
                                    break;
                            }
                            break;
                        case 'element_set_text':
                            element_set_text_cont.handle(action);
                            break;
                        case 'element_set_droplist':
                            element_set_enable_cont.handle(action);
                            break;
                        case 'save_form'://保存表单
                            var $form = $("#module_form");

                            //保存前处理，APP的处理与PC端的略有不同
                            //event_cont.handle('event_before_save_form', form_define_obj);

                            var data = getFormData($form);
                            console.log(data);
                            if(order_id==""){
                                doAdd(data);
                            }else{
                                if(event.TYPE == "set_value"){
                                    data.type = "set_value";
                                    data.event_id = event.ID;                         		
                                    data.form_id = event.FORM_ID.toString();
                                }else{
                                    data.TYPE = "save";
                                    data.event_id = event_id;                         		
                                    data.form_id = form_id;
                                }
                                doUpdate(data);
                            }        
                            break;
                        case 'form_set_value'://表单赋值
                            break;
                        case 'print':
                            var template_list = result.TEMPLATE_LIST;
                            $('#template_list').empty();
                            $.each(template_list, function(index, item) {
                                var html ='<div class="radio" style="width:100px;cursor: pointer;">'
                                        +'	<input type="radio" name="template_id" value="'+item.ID+'" checked>'+item.NAME
                                        +'	<pre id="template_content_'+item.ID +'" style="display:none;">'+item.CONTENT+'</pre>'
                                        +'</div>';
                                $('#template_list').append(html);
                            });
                            $('#print_template_list').modal('show');
                            break; 
                        case 'element_set_enable':
                            element_set_enable_cont.handle(action);
                            break;
                        case 'table_add_row'://表格新增一行
                            var event_action_setting=action.event_action_setting;
                            var target_table_id = "detail_table_"+event_action_setting.target_table_field_id;
                            var dataTable = $('#'+target_table_id).DataTable();
                            dataTable.row.add({}).draw(false);
                            break; 
                        default:
                            event_cont.handle(action_type, form_define_obj);
                            break;
                    }
                }
            }//end of for
            
        });
        mui('#action_popover').popover('hide');
    });

    //old from pc
	$.fn.serializeObject = function () {
	    var o = {};
	    var a = this.serializeArray();
	    $.each(a, function () {
	        if (o[this.name] !== undefined) {
	            if (!o[this.name].push) {
	                o[this.name] = [o[this.name]];
	            }
	            o[this.name].push(this.value || '');
	        } else {
	        	if($("input[name='"+this.name+"']").hasClass("Wdate")){
	        		o[this.name] = this.value || null;
	        	}else{
	        		o[this.name] = this.value || '';
	        	}
	        }
	    });
	    var $radio = $('input[type=radio],input[type=checkbox]',this);
	    $.each($radio,function(){
	        if(!o.hasOwnProperty(this.name)){
	            o[this.name] = '';
	        }
	    });
	    var $file_name = $('.file_name',this);
	    $.each($file_name,function(){
	        if($(this).text()!=''){
	            o[$(this).attr("name")] = $(this).text();
	        }
	    });
	    return o;
	};
	var deleteList=[];
        //按钮事件响应
        $('body').on('click', 'button111', function(event) {
            event.preventDefault();
            var btn = $(this);
            var btn_id = btn.attr('id');
            var btn_name = btn.attr('name');
            if(btn_id==undefined && btn_name==undefined){
            	return false;
            }
            
            if(btn_name == "add_detail_row_btn"){
                var target_table_id = btn.attr('target_table');
                var dataTable = $('#'+target_table_id).DataTable();
                dataTable.row.add({}).draw(false);
            }else if(btn_name == "table_delete_row_btn"){
                var target_table_id = btn.closest('table').attr('id');
                var target_table_tr = btn.closest('tr');
                var dataTable = $('#'+target_table_id).DataTable();
                dataTable.row(target_table_tr).remove().draw();
                var id =  $(this).parent().parent().attr("id");
                if(id==null){
                	return;
                }
                deleteList.push({ID: id, is_delete:'Y'});
               

                //var row_index = table.row( this ).index();
                
            }else{
                console.log('['+btn_id+'] btn click:');

                var module_id = $('#module_id').val();
                var order_id = $('#order_id').val();
                if(btn_id.split('-').length<=1)
                    return;
                    
                var btn_id = btn_id.split('-')[1].split('_')[1];

                $.post('/form/'+module_id+'-click-'+btn_id,{order_id:order_id}, function(events){
                    console.log(events);
                    if(events){
                        for (var i = 0; i < events.length; i++) {
                            var event = events[i];
                            if(event.TYPE == "open"){
                                var url = '/form/'+event.OPEN.MODULE_ID+'-add';
                                if(event.OPEN.OPEN_TYPE = 'newTab'){
                                    window.open(url);
                                }else if(event.OPEN.OPEN_TYPE = 'self'){
                                    window.location.href=url;
                                }else{
                                    window.open(url);
                                }
                            }else if(event.TYPE == "save"||event.TYPE == "set_value"){
                                var $form = $("#module_form");
                                var data = getFormData($form);
                                console.log('save action....');
                                console.log(data);
                                if(order_id==""){
                                    doAdd(data);
                                }else{
                                	if(event.TYPE == "set_value"){
                                		data.type = "set_value";
                                		data.event_id = event.ID;                         		
                                		data.form_id = event.FORM_ID.toString();
                                	}else{
                                		data.TYPE = "save";
                                		data.event_id = event.ID.toString();                         		
                                		data.form_id = event.FORM_ID.toString();
                                	}
                                	doUpdate(data);
                                }
                            }else if(event.TYPE == "refresh_list"){
                                var dataTable = $('#list_table').DataTable();
                                dataTable.ajax.reload();
                            }else if(event.TYPE == "print"){
                                var template_list = event.TEMPLATE_LIST;
                                $('#template_list').empty();
                                $.each(template_list, function(index, item) {
                                	var html ='<div class="radio" style="width:100px;cursor: pointer;">'
                                             +'	<input type="radio" name="template_id" value="'+item.ID+'" checked>'+item.NAME
                                             +'	<pre id="template_content_'+item.ID +'" style="display:none;">'+item.CONTENT+'</pre>'
                                             +'</div>';
                                    $('#template_list').append(html);
                                });
                                $('#print_template_list').modal('show');
                            }else if(event.TYPE == "list_add_row"){
                                var target_table_id = "detail_table_"+event.LIST_ADD_ROW.FIELD_ID;
                                var dataTable = $('#'+target_table_id).DataTable();
                                dataTable.row.add({}).draw(false);
                            }
                        }
                    }
                });
            }
        });

        function getFormData($form){
            // Find disabled inputs, and remove the "disabled" attribute
            var disabled = $form.find(':input:disabled').removeAttr('disabled');
            var unindexed_array = $form.serializeObject();
            // re-disabled the set of inputs that you previously enabled
            

            $.each(unindexed_array, function(index, item) {
            	if(Object.prototype.toString.call(item)=='[object Array]'){
            		unindexed_array[index] = item.toString();
            	}
            });

            var tables = $('.mui-slider-item.table');
            var field_id_list = [];
            var detail_tables = [];
            var img_list = [];

            $("table img").each(function(){
            	var img = {};
            	img.id = $(this).attr("id");
            	img.name = $(this).attr("name");
            	img.field_id = $(this).parent().parent().attr("id").split("f")[1];
            	img_list.push(img);
            });
            
            $.each(tables, function(index, item) {
                var id=$(item).attr('id');
                var field_id = id.split('_')[2];
                field_id_list.push(field_id);

                var ar = [];
                $("#"+id+" ul li").each(function(index, el) {// tr:nth-child(n+2)
                  rowData = $(el).find('input, select, textarea').serializeArray();
                  var rowAr = {};
                  $.each(rowData, function(e, v) {
                    rowAr[v['name']] = v['value'];
                  });
                  if(rowData.length>0){
                	  ar.push(rowAr);
                  }
                });
                var list = ar.concat(deleteList);
                var table_item={
                    table_id: id,
                    data_list: list
                };
                detail_tables.push(table_item);
            });

            unindexed_array.detail_tables = detail_tables;
            unindexed_array.img_list = img_list.concat(imgDeleteIds);

            disabled.attr('disabled','disabled');
            return unindexed_array;
        }


        function doAdd(data){
            mui.showLoading("正在提交..."); 
            $.post('/form/'+data.module_id+'-doAdd', {data: JSON.stringify(data)}, function(dto){
                mui.hideLoading(); 
                if(dto){
                    if(window.location.pathname.indexOf('-add')>0 && dto.ID){
                        history.pushState({foo: "create"}, "", data.module_id+"-edit-"+dto.ID);
                    }
                    $("#order_id").val(dto.ID);
                    mui.alert('操作成功。');
                }else{
                    mui.alert('操作失败,请刷新页面或联系客服查看错误。');
                }
            });
        }

        function doUpdate(data){
            mui.showLoading("正在提交..."); 
            $.post('/form/'+data.module_id+'-doUpdate', {data: JSON.stringify(data)}, function(dto){
                mui.hideLoading(); 
                if(dto){
//                	if(dto.TYPE=="set_value"){
//                		$("input[name='"+dto.TEXT_NAME+"']").val(dto.TEXT_VALUE);
//                	}
                	if(dto){
                		$("#order_id").val(dto.ID);
                		var keys = [];
                		var form_name = "";
             	        for (var key in dto){
             	        	if(key=="FORM_NAME"){
             	        		form_name = dto.FORM_NAME+"-";
             	        		continue;
             	        	}
             	        	keys.push(key);
             	        }
             	        for(var i = 0;i<keys.length;i++){
             	        	var input_name = form_name+keys[i].toLocaleLowerCase();
             	        	$("[name='"+input_name+"']").val(dto[keys[i]]);
             	        }
                	}
                	  
                    mui.alert('操作成功。');
                    
                }else{
                    mui.alert('操作失败,请刷新页面或联系客服查看错误。');
                }
            });
        }
        $("td").on("click","input[name='img_files']",function(){
        	var self = $(this);
	        $('#'+self.attr("id")).fileupload({
				autoUpload: true, 
			    url: '/form/uploadImg?order_id='+order_id,
			    dataType: 'json',
//			    maxFileSize:1 * 1024 ,
//			    acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
//			    messages: {
//			        maxFileSize: 'File exceeds maximum allowed size of 99MB',
//			        acceptFileTypes: 'File type not allowed'
//			    },
		        done: function (e, data) {
		        	if(data.result){
		        		$.scojs_message('上传成功', $.scojs_message.TYPE_OK);
		        		var returnStr = "<div style='width:150px;height:150px;margin-right:10px;float: left;position:relative;'>"
		        			+"<span style='cursor:pointer;background-color:#FFFFFF;font-size: 20px;position: absolute;left:87%;'><i class='Hui-iconfont'>&#xe706;</i></span>"
		        			+"<img name='"+data.result.FILENAME+"' src='/upload/"+data.result.FILENAME+"' style='width: 150px;height: 145px; max-width: 100%;max-height: 100%; '/></div>";
		        		var id = $(this).parent().parent().find("div[name='upload']").attr("id");
		        		$("#"+id).append(returnStr);
		        	}
			    	
			     },
		        error: function () {
		            alert('上传的时候出现了错误！');
		        }
	        });
        });
        
        $("td").on("click","input[name='files']",function(){
        	var self = $(this);
	        $('#'+self.attr("id")).fileupload({
				autoUpload: true, 
			    url: '/form/uploadFile',
			    dataType: 'json',
//			    maxFileSize:1 * 1024 ,
//			    acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
//			    messages: {
//			        maxFileSize: 'File exceeds maximum allowed size of 99MB',
//			        acceptFileTypes: 'File type not allowed'
//			    },
		        done: function (e, data) {
		        	if(data.result.RESULT){
		        		$.scojs_message('上传成功', $.scojs_message.TYPE_OK);
		        		var returnStr = "<a style='color:#06c;text-decoration: underline;' href='"+data.result.FILEURL+"' download='w3logo'>"+data.result.FILENAME+"</a>"
		        		$('#'+self.attr("id")).parent().parent().find(".file_name").html("");
		        		$('#'+self.attr("id")).parent().parent().find(".file_name").append(returnStr);
		        	}
			    	
			     },
		        error: function () {
		            alert('上传的时候出现了错误！');
		        }
	        });
        });
        
        var imgDeleteIds = [];
        $("td").on("click","span[name='deleteImgBtn']",function(){
        	$(this).parent().remove();
        	var img = {};
        	img.id = $(this).parent().find("img").attr("id");
        	img.is_delete = "Y";
        	imgDeleteIds.push(img);
        });
    });
});
