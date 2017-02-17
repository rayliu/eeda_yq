define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 

  $(document).ready(function() {
  		 var dataTable = eeda.dt({
            id: 'delivery_express_table',
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
  		 $('#addair_transport_item').click(function(){
  				$('#delivery_express_table tbody').append('<tr>'+'<td><button type="button" class="btn table_btn delete_btn btn-xs">'
        		                  +'<i class="fa fa-trash-o"></i> 删除</button></td>'
        		    +'<td><select class="itemname">'
  					+'<option>价格</option>'
  					+'<option>附加费用</option>'
  					+'<option>付款方式</option>'
  					+'<option>紧急情况对应效率</option>'
  					+'<option>员工服务态度</option>'
  					+'<option>货物交付及时</option>'
  					+'<option>货物损坏率</option>'
  					+'<option>通关专业程度</option>'
  					+'<option>通关时效性</option>'
  					+'<option>其它</option></select></td>'
  					+'<td><input class="score" placeholder="只能输入整数"></td>'
  					+'<td><textarea class="remark" rows="3" cols="20"></textarea></td>'
  					+'<td></td>'
  					+'</tr>');
  		 });
  		 //删除
  		 $("#delivery_express_table").on('click', '.delete_btn', function(e){
  				$(this).parent().parent().remove();
  				// var id= $(this).val();
  				// $.post('/serviceProvider/markCustormerDelete',{id:id},function(data){
  				// 		freshItemTable();
  				// })
  	});

  });
});