define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	
    	document.title = order_no + ' | ' + document.title;

    	//------------save
        $('#saveBtn').click(function(e){
            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            //提交前，校验数据
            if(!$("#orderForm").valid()){
                return;
            }
            $(this).attr('disabled', true);
            
          //取上面的form的所有input的值
            var order  = {};
            order.id=$('#id').val();
            order.status=$('#status').val();
            order.driver=$('#driver').val();
            order.driver_tel=$('#driver_tel').val();
            order.truck_type=$('#truck_type').val();
            order.car_no=$('#car_no').val();
            order.consignor=$('#consignor').val();
            order.consignor_phone=$('#consignor_phone').val();
            order.take_address=$('#take_address').val();
            order.sign_desc=$('#sign_desc').val();
            order.sign_status=$('#sign_status').val();

            //异步向后台提交数据
            $.post('/truckOrder/save', {params:JSON.stringify(order)}, function(data){
                var order = data;
                if(order.ID>0){
                	
//                    eeda.contactUrl("edit?id",order.ID);
//                    $('#load_sp_id').val(order.LOAD_SP_ID);
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                    $('#saveBtn').attr('disabled', false);
                    
                    //异步刷新明细表
                    itemOrder.refleshCargoTable(order.ID);
                    itemOrder.refleshChargeTable(order.ID);
                    
                }else{
                    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                    $('#saveBtn').attr('disabled', false);
                }
            },'json').fail(function() {
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
              });
        });  

     });
});