define(['jquery'], function ($) {
    
	var update_flag = "N";
        var list_dataTable = eeda.dt({
          id: 'list_btns_table',
          paging: false,
          lengthChange: false,
          columns: [
              { "width": "30px",
                  "render": function ( data, type, full, meta ) {
                    return '<button type="button" class="btn table_btn delete_btn btn-xs" >'+
                          '<i class="fa fa-trash-o"></i> 删除</button>';
                  }
              },
              { "data": "SEQ", "width": "50px"},
              { "data": "NAME"}, 
              // { "data": "TYPE"}, // 方便后台区分 list,  edit
              { "data": "BTN_TYPE",
                "render": function ( data, type, full, meta ) {
                    if(data == 'sys'){
                      return '系统';
                    }else{
                      return '用户自定义'
                    }
                  }
              }, //方便用户区分 sys,  customize
              { "data": "PERMISSION"},
              { "data": "HIDE_CONDITION"}
          ]
        });

        var list_btns_deleteIds=[];
        $('#list_btns_table tbody').on('click', 'button', function () {
          update_flag = "Y";
          var btn = $(this);
          var tr = btn.closest('tr');
          var id = list_dataTable.row(tr).data().ID;

          list_dataTable.row(tr).remove().draw();

          list_btns_deleteIds.push({ID: id, action:'DELETE'});
          return false;
        });
        
        var current_tr_index = 0;
        var current_tr=null;

        $('#list_btns_table tbody').on('click', 'tr', function () {
        	if($(this).children('td').text()=="表中数据为空"){
        		$(this).css('background-color','#fff');
        		return;
        	}else{
        		var flag = false;
        		//选中中tr前，tr中有的elect_fla之前为Y的，表示上一条没有确定
        		$('#list_btns_table tbody tr').each(function(){
            		if($(this).attr("elect_flag")=="Y"){
            			alert("上一条未确定，请点击确定再操作");
            			flag = true;
            			return false;
            		}
            	});
        		if(flag){
        			return;
        		}
        		current_tr = this;
        		$(current_tr).attr("elect_flag","Y");//将当前选中tr的elect_flag设为Y，表示当前行被选中，可以对当前行编辑
                $('#list_btns_table tbody tr').css('background-color','#fff');
                $(current_tr).css('background-color','#00BCD4');
                current_tr_index = list_dataTable.row( this ).index();
                var data = list_dataTable.row( this ).data();
                
                $('#list_btn_name').val(data.NAME);
                $('#list_btn_type').val(data.BTN_TYPE);
                $('#list_btn_seq').val(data.SEQ);
        	}
        } );

        //同时把两个table 的row 都放进去
        var buildTableDetail = function(){
            var data = list_dataTable.rows().data();
            var itemList = [];
            for (var i = 0; i < data.length; i++) {
              itemList.push(data[i]);
            }

            var edit_data = edit_dataTable.rows().data();
            
            for (var i = 0; i < edit_data.length; i++) {
              itemList.push(edit_data[i]);
            }
            var list = itemList.concat(list_btns_deleteIds);
            var mergeList = list.concat(edit_btns_deleteIds);
            return mergeList;
        };

        var clear = function(){
            $('#list_btn_name').val('');
            $('#list_btn_type').val('');
            $('#list_btn_seq').val('');

            $('#edit_btn_name').val('');
            $('#edit_btn_type').val('');
            $('#edit_btn_seq').val('');

            list_dataTable.clear().draw();
            edit_dataTable.clear().draw();
        }

        $('#addListBtn').click(function(){
        	var elect_flag = "";
        	//循环获得table中所有tr的elect_flag
        	$('#list_btns_table tbody tr').each(function(){
        		if($(this).attr("elect_flag")=="Y"){
        			elect_flag = "Y";
        		}
        	});
        	//只要elect_flag等于Y,则表示有一条正在编辑
        	if(elect_flag=="Y"){
        		alert("上一条正在编辑···");
        		return;
        	}else{
        		list_dataTable.row.add({type:'list', btn_type:'customize'}).draw(false);
                current_tr_index = list_dataTable.rows().data().length;
                current_tr = $('#list_btns_table tr:eq('+current_tr_index+')');
        	}
        });
        
        //回写到table
        $('#comfirmListBtn').click(function(){
        	update_flag = "Y";
        	if($(current_tr).attr("elect_flag")!="Y"){
        		alert("请您选中需要编辑的行，再进行操作");
        		return;
        	}
            var item = list_dataTable.rows(current_tr_index).data()[0];
            item.NAME=$('#list_btn_name').val();
            item.BTN_TYPE=$('#list_btn_type').val();
            item.SEQ=$('#list_btn_seq').val();
            list_dataTable.row(current_tr).data( item ).draw();
            
            $(current_tr).attr("elect_flag","N");
            $(current_tr).css('background-color','#fff');
            $("#fields_property input[type='text']").val("");
        });

        //-------------------edit
        var edit_dataTable = eeda.dt({
          id: 'edit_btns_table',
          paging: false,
          lengthChange: false,
          columns: [
              { "width": "30px",
                  "render": function ( data, type, full, meta ) {
                    return '<button type="button" class="btn table_btn delete_btn btn-xs" >'+
                          '<i class="fa fa-trash-o"></i> 删除</button>';
                  }
              },
              { "data": "SEQ", "width": "50px"},
              { "data": "NAME"}, 
              // { "data": "TYPE"}, // 方便后台区分 list,  edit
              { "data": "BTN_TYPE",
                "render": function ( data, type, full, meta ) {
                    if(data == 'sys'){
                      return '系统';
                    }else{
                      return '用户自定义'
                    }
                  }
              }, //方便用户区分 sys,  customize
              { "data": "PERMISSION"},
              { "data": "HIDE_CONDITION"}
          ]
        });

        var edit_btns_deleteIds=[];
        $('#edit_btns_table tbody').on('click', 'button', function () {
          update_flag = "Y";
          var btn = $(this);
          var tr = btn.closest('tr');
          var id = edit_dataTable.row(tr).data().ID;

          edit_dataTable.row(tr).remove().draw();

          edit_btns_deleteIds.push({ID: id, action:'DELETE'});
          return false;
        });
        
        var current_tr_index = 0;
        var current_tr=null;

        $('#edit_btns_table tbody').on('click', 'tr', function () {
        	if($(this).children('td').text()=="表中数据为空"){
        		$(this).css('background-color','#fff');
        		return;
        	}else{
        		var flag = false;
        		//选中中tr前，tr中有的elect_fla之前为Y的，表示上一条没有确定
        		$('#edit_btns_table tbody tr').each(function(){
            		if($(this).attr("elect_flag")=="Y"){
            			alert("上一条未确定，请点击确定再操作");
            			flag = true;
            			return false;
            		}
            	});
        		if(flag){
        			return;
        		}
	            current_tr = this;
	            $(current_tr).attr("elect_flag","Y");//将当前选中tr的elect_flag设为Y，表示当前行被选中，可以对当前行编辑
	            $('#edit_btns_table tbody tr').css('background-color','#fff');
	            $(current_tr).css('background-color','#00BCD4');
	            current_tr_index = edit_dataTable.row( this ).index();
	            var data = edit_dataTable.row( this ).data();
	            
	            $('#edit_btn_name').val(data.NAME);
	            $('#edit_btn_type').val(data.BTN_TYPE);
	            $('#edit_btn_seq').val(data.SEQ);
        	}
        } );

        
        $('#addEditBtn').click(function(){
        	var elect_flag = "";
        	//循环获得table中所有tr的elect_flag
        	$('#edit_btns_table tbody tr').each(function(){
        		if($(this).attr("elect_flag")=="Y"){
        			elect_flag = "Y";
        		}
        	});
        	//只要elect_flag等于Y,则表示有一条正在编辑
        	if(elect_flag=="Y"){
        		alert("上一条正在编辑···");
        		return;
        	}else{
        		edit_dataTable.row.add({type:'edit', btn_type:'customize'}).draw(false);
                current_tr_index = edit_dataTable.rows().data().length;
                current_tr = $('#edit_btns_table tr:eq('+current_tr_index+')');
        	}
        });
        
        //回写到table
        $('#comfirmEditBtn').click(function(){
        	update_flag = "Y";
        	if($(current_tr).attr("elect_flag")!="Y"){
        		alert("请您选中需要编辑的行，再进行操作");
        		return;
        	}
            var item = edit_dataTable.rows(current_tr_index).data()[0];
            item.NAME=$('#edit_btn_name').val();
            item.BTN_TYPE=$('#edit_btn_type').val();
            item.SEQ=$('#edit_btn_seq').val();
            edit_dataTable.row(current_tr).data( item ).draw();
            
            $(current_tr).attr("elect_flag","N");
            $(current_tr).css('background-color','#fff');
            $("#fields_property input[type='text']").val("");
        });
        
        //刷新table
        var refresh_table = function(btn_list_query,btn_list_edit){
        	list_btns_deleteIds.length = 0;
        	edit_btns_deleteIds.length = 0;
        	update_flag = "N";
        	//回显按钮列表
        	list_dataTable.clear().draw();
            for (var i = 0; i < btn_list_query.length; i++) {
                var field = btn_list_query[i];
                list_dataTable.row.add(field).draw(false);
            }

            edit_dataTable.clear().draw();
            for (var i = 0; i < btn_list_edit.length; i++) {
                var field = btn_list_edit[i];
                edit_dataTable.row.add(field).draw(false);
            }
        }

        var btn_update_flag = function(){
        	return update_flag;
        }
        return {
        	refresh_table:refresh_table,
            clear: clear,
            buildTableDetail: buildTableDetail,
            list_dataTable: list_dataTable,
            edit_dataTable: edit_dataTable,
            btn_update_flag:btn_update_flag
        };
    
});