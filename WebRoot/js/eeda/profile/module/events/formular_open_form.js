define(['jquery'], function ($) {

        var list_dataTable = eeda.dt({
          id: 'list_open_form_fields_table',
          paging: false,
          lengthChange: false,
          columns: [
              { "data": "ID", "width": "30px",
                  "render": function ( data, type, full, meta ) {
                      var id='';
                      if(data){
                        id=data;
                      }
                    return '<button type="button" class="btn table_btn delete_btn btn-xs" >'+
                          '<i class="fa fa-trash-o"></i> 删除</button>'
                    +'<input name="id" type="hidden" value="'+id+'">';;
                  }
              },
              { "data": "FIELD_DISPLAY_NAME", "width": "100px",
            	  "render": function ( data, type, full, meta ) {
            		  var str = "";
            		  if(data){
            			  str = data;
            		  }
                      return '<input value="'+str+'" name="field_display_name" style="width:200px;">';
                   }
              }, 
              { "data": "OPERATOR", "width": "50px",
                "render": function ( data, type, full, meta ) {
                    return '=';
                  }
              }, 
              { "data": "FORMULAR", "width": "100px",
            	  "render": function ( data, type, full, meta ) {
            		  var str = "";
            		  if(data){
            			  str = data;
            		  }
                      return '<input value="'+str+'" name="formular" style="width:200px;">';
                   }  
              }
          ]
        });

        $("#list_open_form_table_addBtn").click(function(){
        	list_dataTable.row.add({}).draw(false);
        });

        var list_deleteList=[];
        $('#list_open_form_fields_table tbody').on('click', 'button', function () {
          var btn = $(this);
          var tr = btn.closest('tr');
          var id = list_dataTable.row(tr).data().ID;

          list_dataTable.row(tr).remove().draw();

          if(id!=""){
        	  list_deleteList.push({ID: id, is_delete:'Y'});
          }
          return false;
        });
        
        var buildFieldsListDetail = function(){
        	 var inputs = list_dataTable.$('input, select');
             var itemList = [];
             for (var i = 0; i < inputs.length/3; i++) {
               var item={
                 ID: $(inputs[i*3]).val(),
                 NAME: $(inputs[i*3 + 1]).val(),
                 VALUE: $(inputs[i*3 + 2]).val()
               };
               itemList.push(item);
             }
            return itemList.concat(list_deleteList);
        };

        var edit_dataTable = eeda.dt({
            id: 'edit_open_form_fields_table',
            paging: false,
            lengthChange: false,
            columns: [
                { "data": "ID", "width": "30px",
                    "render": function ( data, type, full, meta ) {
                        var id='';
                        if(data){
                          id=data;
                        }
                      return '<button type="button" class="btn table_btn delete_btn btn-xs" >'+
                            '<i class="fa fa-trash-o"></i> 删除</button>'
                      +'<input name="id" type="hidden" value="'+id+'">';;
                    }
                },
                { "data": "FIELD_DISPLAY_NAME", "width": "50px"}, 
                { "data": "OPERATOR", "width": "50px",
                  "render": function ( data, type, full, meta ) {
                      return '=';
                    }
                }, 
                { "data": "FORMULAR", "width": "30px"}
            ]
          });

        $("#edit_open_form_table_addBtn").click(function(){
        	edit_dataTable.row.add({}).draw();
        });
        
        var edit_deleteList=[];
        $('#edit_open_form_fields_table tbody').on('click', 'button', function () {
          var btn = $(this);
          var tr = btn.closest('tr');
          var id = edit_dataTable.row(tr).data().ID;

          edit_dataTable.row(tr).remove().draw();

          if(id!=""){
        	  edit_dataTable.push({ID: id, is_delete:'Y'});
          }
          return false;
        });
        
        var buildFieldsEditDetail = function(){
            var data = edit_dataTable.rows().data();
            var itemList = [];
            for (var i = 0; i < data.length; i++) {
              itemList.push(data[i]);
            }
            return itemList.concat(edit_deleteList);
        };
        
        return {
        	buildFieldsListDetail: buildFieldsListDetail,
        	buildFieldsEditDetail:buildFieldsEditDetail,
            list_dataTable: list_dataTable,
            edit_dataTable:edit_dataTable
        };
    
});