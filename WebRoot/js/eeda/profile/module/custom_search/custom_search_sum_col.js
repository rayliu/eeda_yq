define(['jquery', 'layer'], 
  function ($) {
        $('#add_sum_col_btn').click(function(event) {
          var field_li = 
              '<li style="margin-top: 5px;">'
              +'    <a href="javascript:;" class="delete_sum_field"><span class="glyphicon glyphicon-remove"></span></a>'
              +'    <span class="field" name="field_name" field_display_name="库存" formular="采购入库单明细.数量">库存</span> '
              +'    <a href="javascript:;" class="edit_sum_field"><span class="glyphicon glyphicon-edit"></span></a>'
              +'</li>';
          $('#sum_cols_ul').append(field_li);
        });

        var deleteSumList=[];
        $('#sum_cols_ul').on('click', 'a.delete_sum_field', function () {
          var btn = $(this);
          var li = btn.closest('li');
          var id = li.attr('id');
          li.remove();
          if(id){
        	  deleteSumList.push({ID: id.toString(), IS_DELETE:'Y'});
          }
          return false;
        });

        $('#sum_cols_ul').on('click', 'a.edit_sum_field', function () {
          var li = $(this).closest('li');
          var block_arr = [];
          $('#custom_search_source_box .table_block').each(function(i, el){
              block_arr.push($(el).attr('form_name'));
          });
          layer.open({
            title: '编辑'
            ,type: 2
            ,resize: false
            ,area: ['650px', '510px']
            ,content: '/module/sumModal?id='+li.attr('id')+"&form_arr="+encodeURIComponent(JSON.stringify(block_arr))
          });
        });
        

        var buildDetail = function(){

            var li_arr = $('#sum_cols_ul li');
            var itemList = [];
            for (var i = 0; i < li_arr.length; i++) {
              var li_el = $(li_arr[i]);
              var field = li_el.children('span.field');
              var item={
                ID: li_el.attr('id'),
                FIELD_NAME: field.attr('field_name'),
                FIELD_DISPLAY_NAME: field.attr('field_display_name')
                ,EXPRESSION: field.attr('formular')
              };

              itemList.push(item);
            }

            var list = itemList.concat(deleteSumList);

            return list;
        };
        var display = function(custom_search_sum_cols){
            var box = $('#sum_cols_ul');
            box.empty();
            for(var i = 0;i<custom_search_sum_cols.length;i++){
              var field_li = 
              '<li style="margin-top: 5px;" id="'+custom_search_sum_cols[i].ID+'">'
              +'    <a href="javascript:;" class="delete_sum_field"><span class="glyphicon glyphicon-remove"></span></a>'
              +'    <span class="field" name="field_name" field_display_name="库存" formular="'+custom_search_sum_cols[i].EXPRESSION+'">'+custom_search_sum_cols[i].FIELD_DISPLAY_NAME+'</span> '
              +'    <a href="javascript:;" class="edit_sum_field"><span class="glyphicon glyphicon-edit"></span></a>'
              +'</li>';
              box.append(field_li);
            }
        }
        var clear = function() {
          $('#sum_cols_ul li').empty();
        }

        return {
            clear: clear,
            buildDetail: buildDetail,
            display: display
        };

});