define(['jquery'], 
  function ($) {
        $('#add_source_col_btn').click(function(event) {
          var field_li = 
              '<li style="margin-top: 5px;">'
                  +'<a href="javascript:;" class="delete_field"><span class="glyphicon glyphicon-remove"></span></a>&ensp;'
                  +'<input type="text" name="field_name" placeholder="列名 （表名.字段）" autocomplete="off" size="30" value="采购入库单.收货仓库">&ensp;'
                  +'<input type="text" name="display_name" placeholder="自定义显示名" autocomplete="off">&ensp;'
                  +'<input type="text" name="seq" placeholder="序号" autocomplete="off" size="5">'
              +'</li>';
          $('#display_cols_ul').append(field_li);
        });

        var deleteList=[];
        $('#display_cols_ul').on('click', 'a.delete_field', function () {
          var btn = $(this);
          var li = btn.closest('li');
          var id = li.attr('id');
          li.remove();
          if(id){
        	  deleteList.push({ID: id.toString(), IS_DELETE:'Y'});
          }
          return false;
        });
        
        var buildDetail = function(){

            var li_arr = $('#display_cols_ul li');
            var itemList = [];
            for (var i = 0; i < li_arr.length; i++) {
              var li_el = $(li_arr[i]);
              var item={
                ID: li_el.attr('id'),
                FIELD_NAME: li_el.children('input[name=field_name]').val(),
                EXPRESSION: '',
                SORT: li_el.children('input[name=seq]').val(),
                HIDDEN_FLAG: '',
                WIDTH: ''
              };

              itemList.push(item);
            }

            var list = itemList.concat(deleteList);

            return list;
        };
        var display = function(custom_search_cols){
          var box = $('#display_cols_ul');
          for(var i = 0;i<custom_search_cols.length;i++){
            var field_li = 
              '<li style="margin-top: 5px;" id="'+ custom_search_cols[i].ID+'">'
                  +'<a href="javascript:;" class="delete_field"><span class="glyphicon glyphicon-remove"></span></a>&ensp;'
                  +'<input type="text" name="field_name" placeholder="列名 （表名.字段）" autocomplete="off" size="30" value="'+ custom_search_cols[i].FIELD_NAME+'">&ensp;'
                  +'<input type="text" name="display_name" placeholder="自定义显示名" autocomplete="off" value="'+ custom_search_cols[i].EXPRESSION+'">&ensp;'
                  +'<input type="text" name="seq" placeholder="序号" autocomplete="off" size="5" value="'+ custom_search_cols[i].SORT+'">'
              +'</li>';
            box.append(field_li);
          }
       }
        var clear = function() {
          $('#display_cols_ul').empty();
        }

        return {
            clear: clear,
            buildDetail: buildDetail,
            display: display
        };

});