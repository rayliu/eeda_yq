define(['jquery', 'ueditor'], 
  function ($, checkboxCont, detailTableCont, refCont) {
    

        var dataTable = eeda.dt({
          id: 'print_template_table',
          paging: false,
          lengthChange: false,
          columns: [
              { "data": "ID", "width": "30px",
                  "render": function ( data, type, full, meta ) {
                    if(!data)
                      data='';
                    return '<button type="button" class="btn table_btn btn-xs delete" >'+
                          '<i class="fa fa-trash-o"></i> 删除</button>'+
                          ' <button type="button" class="btn table_btn btn-xs edit" >'+
                          '<i class="fa fa-edit"></i> 编辑</button>'+
                          '<input name="ID" type="hidden" value="'+data+'">';
                  }
              },
              { "data": "NAME", "width":"20%"}, 
              { "data": "DESC"},
              { "data": "CONTENT", visible: false}
          ]
        });

        var current_tr_index = 0;
        var current_tr=null;

        var deleteList=[];
        $('#print_template_table tbody').on('click', 'button.delete', function () {
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

       var ue = UE.getEditor('template_container', {
            allowDivTransToP:false, 
            initialFrameHeight: 600,
            zIndex : 1100,
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

       $('#print_template_table tbody').on('click', 'button.edit', function () {
          var btn = $(this);
          current_tr = btn.closest('tr');
          var row = dataTable.row(current_tr).data();

          $("#template_id").val(row.ID);
          $("#template_name").val(row.NAME);
          $("#template_desc").val(row.DESC);
          if(row.CONTENT){
            ue.setContent(row.CONTENT);
          }else{
            ue.setContent('');
          }
          $('#templateDetail').modal('show');
       });


        var buildPrintTemplateDetail = function(){
            var data = dataTable.rows().data();
            var itemList = [];
            for (var i = 0; i < data.length; i++) {
              itemList.push(data[i]);
            }

            var list = itemList.concat(deleteList);
            return list;
        };

        $('#addTemplateBtn').click(function(){
            dataTable.row.add({}).draw(false);
        });


        $('#template_modal_ok_btn').click(function(event) {
          var item={
            ID:$("#template_id").val(),
            NAME:$("#template_name").val(),
            DESC:$("#template_desc").val(),
            CONTENT: ue.getContent()
          }

          dataTable.row(current_tr).data( item).draw();
          $('#templateDetail').modal('hide');
        });

        return {
            buildPrintTemplateDetail: buildPrintTemplateDetail,
            dataTable: dataTable
        };
    
});