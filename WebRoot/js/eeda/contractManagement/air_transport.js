define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 

  $(document).ready(function() {
      var deletedTableIds=[];

      itemOrder.buildDataTable=function(){
        var data_table_rows = $("#ocean_table tr");
          var data_items_array=[];
          for (var index = 0; i <data_table_rows.length ; index++) {
             if(index==0)
                continue;
             var row=data_table_rows[index];
             var empty=$(row).find('.dataTables_empty').text();
             if(empty)
               continue;

             var id=$(row).attr('id');
             if(!id){
                id='';
             }
             var item={};
             item.id=id;

             for (var i =1; i <row.childNodes.length; i++) {
               var name=$(row.childNodes[i]).find('input,select').attr('name');
               var value=$(row.childNodes[i]).find('input,select').val();
               if(name){
                   item[name]=value;
               }
             };
             item.action=id.length >0?'UPDATE':'CREATE';
             data_items_array.push(item);
        });
        //deleted items
           for (var i = 0; i < deletedTableIds.length; i++) {
             var  id=deletedTableIds[i];
             var item={
                      id:id,
                      action:'DELETE'
             };
             data_items_array.push(item);
           }
           deletedTableIds=[];
           return  data_items_array;
      };

  		 var dataTable = eeda.dt({
            id: 'air_transport_table',
            // paging: true,
            // serverSide: true, //不打开会出现排序不对
            // // ajax: "/serviceProvider/list",
            //  "drawCallback": function( settings ) {
            //       bindFieldEvent();
            //       $.unblockUI();
            //  },
            columns:[
                {
                  "render": function ( data, type, full, meta ) {
                    return '<button type="button" class="delete btn table_btn delete_btn btn-xs">'+
                        '<i class="fa fa-trash-o"></i> 删除</button>';
                  }
                },
                { "data": "REGION",
                  "render": function ( data, type, full, meta ) {
                    if(!data)
                      data='';
                    return '<input type="text" name="region"  value="'+data+'" class="form-control" />';
                 }
               }, 
                { "data": "SERVICE_CONTENT", 
                  "render": function ( data, type, full, meta ) {
                    if(!data)
                      data='';
                    return '<input type="text" name="service_content"  value="'+data+'" class="form-control" />';
                  }
                },
                { "data": "REMARK",
                  "render": function ( data, type, full, meta ) {
                    if(!data)
                      data='';
                    return '<input type="text" name="remark"  value="'+data+'" class="form-control" />';
                  }
                }
            ]
        });

  		 //添加
  		 $('#addair_transport_item').click(function(){
           var item={};
          dataTable.row.add(item).draw(true);
          itemOrder.buildDataTable();
  				// $('#air_transport_table tbody').append('<tr>'+'<td><button type="button" class="btn table_btn delete_btn btn-xs">'
      //   		                  +'<i class="fa fa-trash-o"></i> 删除</button></td>'
      //       +'<td><textarea class="remark" rows="1" cols="20"></textarea></td>'
      //       +'<td><textarea class="remark" rows="1" cols="20"></textarea></td>'
  				// 	+'<td><textarea class="remark" rows="1" cols="20"></textarea></td>'
  				// 	+'</tr>');
  		 });
  		 //删除
  		 $("#air_transport_table").on('click', '.delete_btn', function(e){
  			   e.preventDefault();
           var tr=$(this).parent().parent();
            deletedTableIds.push(tr.attr('id'));
           dataTable.row(tr).remove().draw();


        	// $(this).parent().parent().remove();
  				// var id= $(this).val();
  				// $.post('/serviceProvider/markCustormerDelete',{id:id},function(data){
  				// 		freshItemTable();
  				// })
  	});

       //刷新明细表
      itemOrder.refleshTable = function(order_id){
        var url = "/planOrder/tableList?order_id="+order_id;
        cargoTable.ajax.url(url).load();
      }
      
  });
});