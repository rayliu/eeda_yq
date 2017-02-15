define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 

  $(document).ready(function() {
  		var buildItemList=function(){
  			var items=[];
  			$('#mark_table tr').each(function(index){
  				item1={};
  			    if(index>0){
  					var item=$(this).children().find('[class=itemname]').val();
  					var score=$(this).children().find('[class=score]').val();
  					var remark=$(this).children().find('[class=remark]').val();
  					item1.item=item;
  					item1.score=score;
  					item1.remark=remark;
  				 }
  			  if(item1.item!=null){
  			   items.push(item1);
  			  }
  			})
  			return items;
  		}

  		var dataTable = eeda.dt({
            id: 'mark_table',
            // ajax:{
            //     url: "/serviceProvider/markCustormerList?sp_id="+$('#markCustomer').val(),
            //     type: 'POST'
            // }, 
            columns:[
            { "width": "10px", "orderable": false,
            "render": function ( data, type, full, meta ) {
                 var str = '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:50px" value='+full.ID+'>删除</button>';
                return str;
            	}
            },
              { "data": "ITEM", "width": "100px"},
              { "data": "SCORE", "width": "100px"},
              { "data": "REMARK", "width": "100px"},
              { "data": "C_NAME", "width": "100px"},
              { "data": "MARK_DATE", "width": "100px"},
            ]
        });

  	$('#markCustomer_input').on('mouseout',function(){
  		if($('#markCustomer').val()!=null&&$('#markCustomer').val()!=''){
  			freshItemTable();
  		}
  	})

	 $('#marking').click(function(){
            $('#mark_table_msg_btn').click();             
        })

  	$('#add_mark').click(function(){
  		var marker_name=$('#marker_name').val();
  		if($($('#mark_table tbody tr')[0]).find(".dataTables_empty").length>0){
  			$($('#mark_table tbody tr')[0]).remove();
  		}
  		$('#mark_table tbody').append('<tr>'+'<td><button type="button" class="btn table_btn delete_btn btn-xs">'
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


  	 $("#mark_table").on('click', '.delete_btn', function(e){
  		// $(this).parent().parent().remove();
  		var id= $(this).val();
  		$.post('/serviceProvider/markCustormerDelete',{id:id},function(data){
  				freshItemTable();
  		})
  	});
  	 //保存
  	 $('#save_mark').click(function(){
  	 	var item_list=buildItemList();
  	 	var sp_id=$('#markCustomer').val()
  	 	 //异步向后台提交数据
        $.post('/serviceProvider/markCustormerSave', {params:JSON.stringify(item_list),sp_id:sp_id}, function(data){
        	freshItemTable();
        	$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
        },'JSON').fail(function(){
        	 $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
        	});
  	 });

  	 var freshItemTable=function(){
  	 	 var sp_id=$('#markCustomer').val()
  	 	 var url = "/serviceProvider/markCustormerList?sp_id="+sp_id;
          dataTable.ajax.url(url).load(function(){
          	calculate();
          });
  	 }
  	 //计算总分
  	 var calculate=function(){
  	 	var totalscore=80;
  	 	dataTable.data().each(function(item,index){
  	 		totalscore+=parseInt(item.SCORE);
  	 	})
  	 	$('#total_mark').val(totalscore);
  	 	$('#total_mark').html(totalscore);
  	 }
  })
})