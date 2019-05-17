define(['jquery', './fields/field_pro_check_box', './fields/field_pro_detail_ref', './fields/field_pro_ref','./fields/field_pro_dropdown','./fields/field_pro_auto_no'], 
  function ($, checkboxCont, detailTableCont, refCont,dropdownCont, autoNoCont) {
    

        var dataTable = eeda.dt({
          id: 'fields_table',
          paging: false,
          lengthChange: false,
          columns: [
              { "width": "30px",
                  "render": function ( data, type, full, meta ) {
                    return '<button type="button" class="btn table_btn btn-xs delete_field" >'+
                          '<i class="fa fa-trash-o"></i> 删除</button>';
                  }
              },
              { "data": "SEQ", "width": "50px", 
                "render": function ( data, type, full, meta ) {
                  if(!data) data = '';
                  return '<input type="text" style="width:50px;" value="'+data+'">';
                }
              },
              { "data": "FIELD_DISPLAY_NAME", "width": "150px"}, 
              { "data": "FIELD_TYPE", "width": "120px"}, 
              { "data": "LISTED", "width": "80px", 
                "render": function ( data, type, full, meta ) {
                  var checked="checked";
                  if(data=='Y') 
                    checked="";
                  return '<input type="checkbox" value="'+data+'" '+checked+'>';
                }
              },
              { "data": "SORT_TYPE", "width": "30px"}
          ]
        });

        var current_tr_index = 0;
        var current_tr=null;
        
        $('#fields_table tbody').on('click', 'tr', function (event) {
        	if($(this).children('td').text()=="表中数据为空"){
        		$(this).css('background-color','#fff');
        		return;
        	}else{
        		var flag = false;
        		//选中中tr前，tr中有的elect_fla之前为Y的，表示上一条没有确定
        		$('#fields_table tbody tr').each(function(){
            		if($(this).attr("elect_flag")=="Y" && fields_property_change=='Y'){
            			alert("上一个字段修改未确定，请点击确定再操作");
            			flag = true;
            			return false;
            		}
            	});
        		if(flag){
        			return;
            }
              
              fields_property_re_display_running=true;
        		  current_tr = this;
              $(current_tr).attr("elect_flag","Y");//将当前选中tr的elect_flag设为Y，表示当前行被选中，可以对当前行编辑
              $('#fields_table tbody tr').css('background-color','#fff');
              $(current_tr).css('background-color','#00BCD4');
              current_tr_index = dataTable.row( this ).index();
              var data = dataTable.row( this ).data();
              
              $('#field_display_name').val(data.FIELD_DISPLAY_NAME);
              $('#field_type').val(data.FIELD_TYPE).change();
              $('#sort_type').val(data.SORT_TYPE);
              $('#default_value').val(data.DEFAULT_VALUE);
              $('#seq').val(data.SEQ);
//                 if(data.LISTED == 'Y'){
//                   $('#is_not_list_col').prop('checked', false);
//                 }else{
//                   $('#is_not_list_col').prop('checked', true);
//                 }
              if(data.READ_ONLY=="Y"){
                $('#read_only').prop('checked', true);
              }else{
                $('#read_only').prop('checked', false);
              }
              if(data.REQUIRED=="Y"){
                $('#required').prop('checked', true);
              }else{
                $('#required').prop('checked', false);
              }
              if(data.LISTED=="N"){
                $('#is_not_list_col').prop('checked', true);
              }else{
                $('#is_not_list_col').prop('checked', false);
              }

              if(data.APP_DISPLAY_COL=="Y"){
                $('#app_display_col').prop('checked', true);
              }else{
                $('#app_display_col').prop('checked', false);
              }

              if(!data.FIELD_TYPE){
                //清空所有属性
                $('#check_box_id').val('');
                checkboxCont.dataTable.clear().draw();
              }else if(data.FIELD_TYPE == '自动编号'){
                  re_display_auto_no_values(data);
              }else if(data.FIELD_TYPE == '复选框'){
                  re_display_checkbox_values(data);
              }else if(data.FIELD_TYPE == '从表引用'){
                  re_display_detail_ref_values(data);
              }else if(data.FIELD_TYPE == '字段引用'){
                  re_display_ref_values(data);
              }else if(data.FIELD_TYPE == '下拉列表'){
                  re_display_dropdown_values(data);
              }

              fields_property_re_display_running=false;
        	}
        } );

        var update_flag = "N";
        var field_update_flag = function(){
        	return update_flag;
        }
        var deleteList=[];
        $('#fields_table tbody').on('click', 'button', function () {
          update_flag = "Y";
          var btn = $(this);
          var tr = btn.closest('tr');
          var id = dataTable.row(tr).data().ID;

          dataTable.row(tr).remove().draw();

          if(id==null){
        	  return;
          }
          deleteList.push({ID: id, is_delete:'Y'});
          return false;
        });

        var re_display_auto_no_values = function(data) {
          var ref = data.AUTO_NO;
          if(!ref)
            return;
          $('#auto_no_id').val(ref.ID);

          if(ref.IS_GEN_BEFORE_SAVE == 'Y'){
            $('#is_gen_before_save').prop('checked', true);
          }else{
            $('#is_gen_before_save').prop('checked', false);
          }
          var display_list = ref.ITEM_LIST;
          autoNoCont.dataTable.clear().draw();
          if(display_list){
            for (var i = 0; i < display_list.length; i++) {
                var item = display_list[i];
                autoNoCont.dataTable.row.add(item).draw(false);
            }
          }
        };

        var re_display_ref_values = function(data) {
          var ref = data.REF;
          $('#ref_id').val(ref.ID);
          $('#ref_form').val(ref.REF_FORM);
          $('#ref_field').val(ref.REF_FIELD);
          $('#ref_condition').val(ref.REF_CONDITION);

          var $radios= $('input[name=ref_field_display_type]');
          if(ref.DISPLAY_TYPE == 'dropdown'){
            $radios.filter('[value=dropdown]').prop('checked', true);
          }else{
            $radios.filter('[value=pop]').prop('checked', true);
          }
          var display_list = ref.ITEM_LIST;
          refCont.dataTable.clear().draw();
          if(display_list){
            for (var i = 0; i < display_list.length; i++) {
                var item = display_list[i];
                refCont.dataTable.row.add(item).draw(false);
            }
          }
        };

        var re_display_detail_ref_values = function(data) {
          var detail_ref = data.DETAIL_REF;
          $('#detail_ref_id').val(detail_ref.ID);
          $('#detail_ref_form').val(detail_ref.TARGET_FORM_NAME);

          var condition_list = detail_ref.JOIN_CONDITION;
          detailTableCont.connect_dataTable.clear().draw();
          if(condition_list){
            for (var i = 0; i < condition_list.length; i++) {
                var item = condition_list[i];
                detailTableCont.connect_dataTable.row.add(item).draw(false);
            }
          }

          var display_list = detail_ref.DISPLAY_FIELD;
          detailTableCont.display_dataTable.clear().draw();
          if(display_list){
            for (var i = 0; i < display_list.length; i++) {
                var item = display_list[i];
                detailTableCont.display_dataTable.row.add(item).draw(false);
            }
          }
        }

        var re_display_checkbox_values = function(data) {
          var check_box = data.CHECK_BOX;
          $('#check_box_id').val(data.CHECK_BOX.ID);

          if(data.CHECK_BOX.IS_SINGLE_CHECK == 'Y'){
            $('#is_single_check').prop('checked', true);
          }else{
            $('#is_single_check').prop('checked', false);
          }
          if(data.CHECK_BOX.IS_SINGLE_CHECK == 'Y'){
            $('#is_single_check').prop('checked', true);
          }else{
            $('#is_single_check').prop('checked', false);
          }
          $('#line_display_numbers').val(data.CHECK_BOX.LINE_DISPLAY_NUM);

          var itemList = data.CHECK_BOX.ITEM_LIST;
          checkboxCont.dataTable.clear().draw();
          if(itemList){
            for (var i = 0; i < itemList.length; i++) {
                var item = itemList[i];
                checkboxCont.dataTable.row.add(item).draw(false);
            }
          }
        }
        
        var re_display_dropdown_values = function(data){
        	var display_list = data.DROPDOWN_LIST;
        	dropdownCont.dataTable.clear().draw();
            if(display_list){
              for (var i = 0; i < display_list.length; i++) {
                  var item = display_list[i];
                  dropdownCont.dataTable.row.add(item).draw(false);
              }
            }
        }

        var buildFieldsDetail = function(){
            var data = dataTable.rows().data();
            var itemList = [];
            for (var i = 0; i < data.length; i++) {
              itemList.push(data[i]);
            }

            var list = itemList.concat(deleteList);
            return list;
        };

        
        var clear = function(){
            $('#field_display_name').val('');
            $('#field_type').val('').change();
            $('#sort_type').val('');
            $('#default_value').val('');
            $('#seq').val('');
            $('#is_not_list_col').prop('checked', false);
            $('#app_display_col').prop('checked', false);
            
            $('#ref_id').val('');
            $('#ref_form').val('');
            $('#ref_field').val('');
            $('#ref_condition').val('');

            dataTable.clear().draw();
            checkboxCont.clear();
            detailTableCont.clear();
            refCont.clear();
        };

        $('#addFieldBtn').click(function(){
        	var elect_flag = "";
        	//循环获得fields_table中所有tr的elect_flag
        	$('#fields_table tbody tr').each(function(){
        		if($(this).attr("elect_flag")=="Y"){
        			elect_flag = "Y";
        		}
        	});
        	//只要elect_flag等于Y,则表示有一条正在编辑
        	if(elect_flag=="Y"){
        		alert("上一条正在编辑···");
        		return;
        	}else{
        		dataTable.row.add({}).draw(false);
                current_tr_index = dataTable.rows().data().length;
                current_tr = $('#fields_table tr:eq('+current_tr_index+')');
        	}
        });
        
        $('#field_type').change(function(event) {
          var checkText=$(this).find("option:selected").text();  //获取Select选择的Text
          $('.config').hide();
          if(checkText == '文本'){
            $('#text_config').show();
          }else if (checkText == '自动编号') {
            //autoNoCont.dataTable.clear().draw();
            $('.auto_no_config').show();
          }else if (checkText == '下拉列表') {
            $('.dropdown_config').show();
          }else if (checkText == '复选框') {
            checkboxCont.dataTable.clear().draw();
            $('.check_box_config').show();
          }else if (checkText == '从表引用') {
            checkboxCont.dataTable.clear().draw();
            $('.detail_table_re_config').show();
          }else if (checkText == '字段引用') {
            //checkboxCont.dataTable.clear().draw();
            $('.ref_config').show();
          }else if(checkText == '附件'){
        	  $('.file_config').show();
          }else if(checkText == '图片'){
        	  $('.pic_config').show();
          }
        });
        //取消字段修改
        $('#field_tab_cancelFieldBtn').click(function(){
          $(current_tr).attr("elect_flag","N");
            $(current_tr).css('background-color','#fff');
            $("#fields_property input[type='text']").val("");
            $("#fields_property input[type='checkbox']").prop("checked",false);
            fields_property_change='N';
        });
        //回写到table
        $('#field_tab_confirmFieldBtn').click(function(){
        	update_flag = "Y";
        	if($(current_tr).attr("elect_flag")!="Y"){
        		alert("请您选中需要编辑的行，再进行操作");
        		return;
        	}
            var item = dataTable.rows(current_tr_index).data()[0];
            item.FIELD_DISPLAY_NAME=$('#field_display_name').val();
            item.FIELD_TYPE=$('#field_type').val();
            item.SORT_TYPE=$('#sort_type').val();
            item.DEFAULT_VALUE=$('#default_value').val();
            item.SEQ=$('#seq').val();
            item.READ_ONLY=$('#read_only').prop('checked')==true?'Y':'N';
            item.LISTED=$('#is_not_list_col').prop('checked')==true?'N':'Y';
            item.REQUIRED=$('#required').prop('checked')==true?'Y':'N';
            item.APP_DISPLAY_COL=$('#app_display_col').prop('checked')==true?'Y':'N';

            var checkText=$('#field_type').find("option:selected").text();
            if(checkText == '文本'){
              
            }else if (checkText == '自动编号') {
              var auto_no_dto = autoNoCont.buildDto();
              item.AUTO_NO = auto_no_dto;
            }else if (checkText == '复选框') {
              var check_dto = checkboxCont.buildDto();
              item.CHECK_BOX = check_dto;
            }else if (checkText == '从表引用') {
              var dto = detailTableCont.buildDto();
              item.DETAIL_REF = dto;
            }else if (checkText == '字段引用') {
              var dto = refCont.buildDto();
              item.REF = dto;
            }else if(checkText == '下拉列表'){
        	  var dto = dropdownCont.buildDto();
              item.DROPDOWN = dto;
            }

            dataTable.row(current_tr).data( item ).draw();
            $(current_tr).attr("elect_flag","N");
            $(current_tr).css('background-color','#fff');
            $("#fields_property input[type='text']").val("");
            $("#fields_property input[type='checkbox']").prop("checked",false);
            fields_property_change='N';
        });

        var check = function(){
        	var flag = false;
        	$('#fields_table tbody tr').each(function(){
        		if($(this).attr("elect_flag")=="N"){
        			flag = true;
        		}else if($(this).children('td').eq(0).text()=="表中数据为空"){
        			flag = true;
        		}
        	});
        	return flag;
        }

        //序号输入
        $('#fields_table tbody').on('focus', 'input[type=text]', function(e){
            e.preventDefault();//不触发选中行
            return false;
        });
        $('#fields_table tbody').on('change', 'input[type=text]', function(e){
            e.preventDefault();
            var item = dataTable.rows(current_tr_index).data()[0];
            item.SEQ = this.value;

            dataTable.row(current_tr).data( item ).draw();
        });

        $('#fields_table tbody').on('change', 'input[type=checkbox]', function(e){
          e.preventDefault();
          var item = dataTable.rows(current_tr_index).data()[0];
          if($(this).prop('checked')){
            item.LISTED = 'N';  
          }else{
            item.LISTED = 'Y';  
          }
          
          dataTable.row(current_tr).data( item ).draw();
      });
        
        //保存后，重刷字段table表
        var refresh_table = function(form_field_list){
        	 update_flag = "N";
        	 dataTable.clear().draw();
             for (var i = 0; i < form_field_list.length; i++) {
                 var field = form_field_list[i];
                 dataTable.row.add(field).draw(false);
             }
             add_elect_flag();
        }
        
        //由于从数据库中load出来的tr没有经过操作，所以没有elect_flag。要给它添加elect_flag
        var add_elect_flag = function(){
        	$('#fields_table tbody tr').each(function(){
        		if($(this).children('td').eq(0).text()=="表中数据为空"){
        			return;
        		}else if($(this).attr("id")){
        			$(this).attr("elect_flag","N");
        		}
        	});
        }
        //回显标记位
        var fields_property_re_display_running=false;
        var fields_property_change='N';
        //等回显完，才判断所选字段的属性是否被改动
        $('#fields_property input, #fields_property select').change(function(){
          //列表中有行被选中才监听
          if(current_tr && !fields_property_re_display_running){
            fields_property_change='Y';
          }
        });
        
        return {
        	check:check,
            clear: clear,
            buildFieldsDetail: buildFieldsDetail,
            dataTable: dataTable,
            refresh_table:refresh_table,
            add_elect_flag:add_elect_flag,
            field_update_flag:field_update_flag
        };
    
});