define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) { 
$(document).ready(function() {
	var tableName = 'eeda-table';
	var itemIds=[]
	itemOrder.buildItemDetail=function(){
        var item_table_rows = $("#"+tableName+" tr");
        var items_array=[];
        for(var index=0; index<item_table_rows.length; index++){
            if(index==0)
                continue;

            var row = item_table_rows[index];
            var empty = $(row).find('.dataTables_empty').text();
            if(empty)
            	continue;
            
            var id = $(row).attr('id');
            if(!id){
                id='';
            }
            var item={}
            item.id = id;
            for(var i = 1; i < row.childNodes.length; i++){
            	var name = $(row.childNodes[i]).find('input').attr('name');
            	var value = $(row.childNodes[i]).find('input').val();
            	if(name){
            		item[name] = value;
            	}
            }
            item.action = $('#order_id').val() != ''?'UPDATE':'CREATE';
            items_array.push(item);
        }

        return items_array;
    };
    
    
    var ids = [];
    //------------事件处理
        var itemTable = eeda.dt({
            id: 'eeda-table',
            initComplete: function( settings ) {
            	ids = [];
            	cnames = [];
            },
            columns:[
            {"data": "ID","width":"30px",
            	"render": function ( data, type, full, meta ) {
            		var str = '<input type="checkbox" class="checkBox" style="width:30px">';
            		for(var i=0;i<ids.length;i++){
                        if(ids[i]==data){
                       	 str = '<input type="checkbox" class="checkBox" style="width:30px" checked>';
                        }
                    }
            		return str;
			    }
            },
            {"width":"50px",
              "render": function ( data, type, full, meta ) {
                    var str = '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:50px" >删除</button>';
                     if($("#status").val()=='已确认'){
                        return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:50px" disabled>删除</button>';
                     }
                    return str;
                }
            },
            { "data": "ORDER_NO", "width": "100px",
                  "render": function ( data, type, full, meta ) {
                      return "<a href='/transJobOrder/edit?id="+full.JOB_ORDER_ID+"'target='_blank'>"+data+"</a>";
                  }
            },
            { "data": "CREATE_STAMP", "width": "70px"},
            { "data": "CONTAINER_NO", "width": "70px"},
            { "data": "SO_NO", "width": "70px"},
            { "data": "CABINET_DATE", "width": "70px", 
              render: function(data){
                if(data)
                  return data.substr(0,10);
                return '';
              }
            },
            { "data": "CUSTOMER_NAME", "width": "70px"},
            { "data": "SP_NAME", "width": "70px"},
            { "data": "CAR_NO", "width": "70px"},
            { "data": "FIN_NAME", "width": "70px"},
            { "data": "CURRENCY_NAME","class":"currency_name", "width": "70px"},
            { "data": "TOTAL_AMOUNT","class":"total_amount",  "width": "70px",
            	"render": function ( data, type, full, meta ) {
            		if(full.ORDER_TYPE=='charge'){
	            		return '<span style="color:red;">'+'-'+eeda.numFormat(parseFloat(data).toFixed(2),3)+'</span>';
	            	}
            		return eeda.numFormat(parseFloat(data).toFixed(2),3);
                  }
            },
            { "data": "EXCHANGE_RATE" , "width": "70px"},
            { "data": "AFTER_TOTAL" , "width": "70px",
            	"render": function ( data, type, full, meta ) {
            		if(full.ORDER_TYPE=='charge'){
	            		return '<span style="color:red;">'+'-'+eeda.numFormat(parseFloat(data).toFixed(2),3)+'</span>';
	            	}else{
	            		return eeda.numFormat(parseFloat(data).toFixed(2),3);
	            	} 
            	}
            },
            { "data": "REMARK", "width": "70px"},
            { "data": "ORDER_TYPE", "visible": false,
                "render": function ( data, type, full, meta ) {
                    if(!data)
                        data='';
                    return data;
                }
            },
            { "data": "JOB_ORDER_ID", "visible": false}
        ]
    });
        var dataTable = eeda.dt({
          id: 'eeda_cost_table',
          serverSide: true, //不打开会出现排序不对
          ajax:{
                //url: "/transCostCheckOrder/list",
                type: 'POST'
          },
          // drawCallback: function( settings ) {
          //     flash();
          // },
          columns: [
                { "width": "30px","orderable": false,
                        "render": function ( data, type, full, meta ) {
                            var strcheck='<input type="checkbox" class="checkBox" name="order_check_box" value="'+full.ID+'">';
                            for(var i=0;i<itemIds.length;i++){
                                 if(itemIds[i]==full.ID){
                                     strcheck= '<input type="checkbox" class="checkBox" checked="checked"  name="order_check_box" value="'+full.ID+'">';
                                 }
                             }
                            return strcheck;
                        }
                },
                { "data": "ORDER_NO", "width": "100px",
                      "render": function ( data, type, full, meta ) {
                          return "<a href='/transJobOrder/edit?id="+full.JOBID+"'target='_blank'>"+data+"</a>";
                      }
                },
                { "data": "CREATE_STAMP", "width": "100px"},
                { "data": "CONTAINER_NO", "width": "60px"},
                { "data": "SO_NO", "width": "60px"},
                { "data": "CUSTOMER_NAME", "width": "100px"},
                { "data": "SP_NAME", "width": "100px","class":"SP_NAME"},
                { "data": "CAR_NO", "width": "100px"},
                { "data": "FEE_NAME", "width": "180px"},
                { "data": "TOTAL_AMOUNT", "width": "60px",'class':'TOTAL_AMOUNT',
                    "render": function ( data, type, full, meta ) {
                        if(full.SQL_TYPE=='charge'){
                            return '<span style="color:red;">'+'-'+eeda.numFormat(parseFloat(data).toFixed(2),3)+'</span>';
                        }
                        return eeda.numFormat(parseFloat(data).toFixed(2),3);
                      }
                },
                { "data": "CURRENCY_NAME", "width": "60px",'class':'CURRENCY_NAME'},
                { "data": "EXCHANGE_RATE", "width": "60px" },
                { "data": "AFTER_TOTAL", "width": "60px" ,'class':'AFTER_TOTAL',
                    "render": function ( data, type, full, meta ) {
                        if(full.SQL_TYPE=='charge'){
                            return '<span style="color:red;">'+'-'+eeda.numFormat(parseFloat(data).toFixed(2),3)+'</span>';
                        }
                        return eeda.numFormat(parseFloat(data).toFixed(2),3);
                      }
                },  
                { "data": "REMARK", "width": "100px"}
              ]
          });
            
 
    
    //刷新明细表
    itemOrder.refleshTable = function(order_id,ids){
    	var url = "/transCostCheckOrder/tableList?order_id="+order_id+"&ids="+ids;
    	itemTable.ajax.url(url).load();
    }
    //添加明细   
     $('#add_cost').click(function(){
            $('#allcost').prop('checked',false);
            $('#add_cost_item').prop('disabled',true);
            $('#cost_table_msg_btn').click();
            $('#searchBtn').click();
      }) 
      $('#resetBtn').click(function(e){
          $('#que_sp_input').val('');
          $('#que_order_no').val('');
          $('#que_create_stamp_begin_time').val('');
          $('#que_create_stamp_end_time').val('');
          $('#que_customer_input').val('');
      });
         

      $('#searchBtn').click(function(){
          searchData1(); 
      });

     var searchData1=function(){
          var checked = '';
            if($('#checkOrderAll').prop('checked')==true){
                 checked = 'Y';
             }
          var order_no = $("#que_order_no").val().trim(); 
          var sp_name = $('#company_abbr').val();
          $('#que_sp_input').val(sp_name);
          $('#que_sp_input').attr('disabled',true);

          if(!sp_name){
              $.scojs_message('请选择结算公司', $.scojs_message.TYPE_ERROR);
              return;
          }
          var customer_name = $('#que_customer_input').val().trim();
          var create_stamp_begin_time = $("#que_create_stamp_begin_time").val();
          var create_stamp_end_time = $("#que_create_stamp_end_time").val();
          
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/transCostCheckOrder/list?checked="+checked
                +"&order_no="+order_no
               +"&sp_name="+sp_name
               +"&customer_name="+customer_name
               +"&create_stamp_end_time="+create_stamp_end_time
               +"&create_stamp_begin_time="+create_stamp_begin_time;

          dataTable.ajax.url(url).load();
        }
    
      //添加新的明细
      $('#add_cost_item').on('click', function(){
          insertCostItem();
          searchData1(); 
      });
      var insertCostItem=function(){

          var order_id=$('#order_id').val();
           var cost_itemlist=[];
          $('#eeda_cost_table input[name=order_check_box]:checked').each(function(){
                var id=$(this).val();
                cost_itemlist.push(id);
          });
          if(cost_itemlist.length==0){
            $('#add_cost_item').attr('disabled',true);
          }
          $.post('/transCostCheckOrder/insertCostItem',{order_id:order_id,cost_itemlist:cost_itemlist.toString()},function(data){
                itemOrder.refleshTable(data.costOrderId.toString());
                 $('#cny').val((parseFloat(data.CNY)).toFixed(2));
                 $('#usd').val((parseFloat(data.USD)).toFixed(2));
                 $('#hkd').val((parseFloat(data.HKD)).toFixed(2));
                 $('#jpy').val((parseFloat(data.JPY)).toFixed(2));
                 $('#total_amount').val((parseFloat(data.CNY)).toFixed(2));
          },'json').fail(function() {
               $.scojs_message('添加失败', $.scojs_message.TYPE_ERROR);
          });
      }

      //添加明细的全选
      $('#allcost').click(function(){
          var itemIds=[];
          
          if($(this).prop('checked')){
            $("#eeda_cost_table input[name=order_check_box]").prop('checked',true);
          }else{
             $("#eeda_cost_table input[name=order_check_box]").prop('checked',false);
          }
         if($(this).prop('checked')){
                 $("#eeda_cost_table input[name=order_check_box]:checked").each(function(){                     
                     itemIds.push($(this).val());
                  });
                 $('#add_cost_item').attr('disabled',false);
           }else{
                $('#add_cost_item').attr('disabled',true);
           }
     });

      $('#eeda_cost_table').on('click',"input[name='order_check_box']",function () {
        var  flag=0;
          $("input[name='order_check_box']").each(function(){
              if($(this).prop('checked')){
                flag++;
              }
          });
          if(flag>0){
               $('#add_cost_item').attr('disabled',false);
          }else{
                $('#add_cost_item').attr('disabled',true);
          }
       });

      //删除明细
      $('#eeda-table').on('click',".delete",function(){
            var id=$(this).parent().parent().attr('id');
            var order_id=$('#order_id').val();
             $.post('/transCostCheckOrder/deleteCostItem', {cost_itemid:id,order_id:order_id},function(data){
                 itemOrder.refleshTable(data.costOrderId.toString());
                 $('#cny').val((parseFloat(data.CNY)).toFixed(2));
                 $('#usd').val((parseFloat(data.USD)).toFixed(2));
                 $('#hkd').val((parseFloat(data.HKD)).toFixed(2));
                 $('#jpy').val((parseFloat(data.JPY)).toFixed(2));

                 $('#total_amount').val((parseFloat(data.CNY)).toFixed(2));
             },'json').fail(function() {
               $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
          });

      });

      //查看应收应付对账结果
        $('#checkOrderAll').click(function(){
            searchData1(); 
         });


} );    
} );