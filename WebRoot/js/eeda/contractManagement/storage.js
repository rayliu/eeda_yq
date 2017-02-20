define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 

  $(document).ready(function() {
  		 var dataTable = eeda.dt({
            id: 'storage_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            // ajax: "/serviceProvider/list",
            columns:[
                { "data": "COMPANY_NAME","width": "10%",
                    "render": function ( data, type, full, meta ) {
                        if(Provider.isUpdate){
                             return "<a href='/supplierContract/edit?id="+full.ID+"' target='_blank'>" + data+ "</a>";
                        }else{
                             return data;
                        }
                    }
                },
                { "data": "ABBR", "width": "10%"}, 
                { "data": "CODE", "width": "10%"},
                { "data": "CODE", "width": "10%"}
            ]
        });

  		 //添加
  		 $('#addstorage_item').click(function(){
  				$('#storage_table tbody').append('<tr>'+'<td><button type="button" class="btn table_btn delete_btn btn-xs">'
        		                  +'<i class="fa fa-trash-o"></i> 删除</button></td>'
        		+'<td><textarea class="remark" rows="3" cols="20"></textarea></td>'
            +'<td><textarea class="remark" rows="3" cols="20"></textarea></td>'
  					+'<td><textarea class="remark" rows="3" cols="20"></textarea></td>'
  					+'</tr>');
  		 });
  		 //删除
  		 $("#storage_table").on('click', '.delete_btn', function(e){
  				$(this).parent().parent().remove();
  				// var id= $(this).val();
  				// $.post('/serviceProvider/markCustormerDelete',{id:id},function(data){
  				// 		freshItemTable();
  				// })
  	});

  });
});