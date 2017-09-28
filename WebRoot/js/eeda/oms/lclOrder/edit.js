define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'pageguide',
        'validate_cn', 'sco', 'datetimepicker_CN', 'jq_blockui', 'dtColReorder'], function ($, metisMenu) { 
    
    

    $(document).ready(function() {

     /*   tl.pg.init({
            pg_caption: '本页教程'
        });*/

        //------------save
        $('#saveBtn').click(function(e){
            //阻止a 的默认响应行为，不需要跳转
            e.preventDefault();
            //提交前，校验数据
            if(!$("#orderForm").valid()){
                return;
            }
            
            $.blockUI({ 
                message: '<h4><img src="/images/loading.gif" style="height: 20px; margin-top: -3px;"/> 正在提交...</h4>' 
            });
            $(this).attr('disabled', true);

            var order = {
                id: $('#order_id').val(),
                trans_clause: $('#trans_clause').val(),
                trade_type: $('#trade_type').val(),
                transport_type: $('#transport_type').val(),
                MBLshipper: $('#MBLshipper').val(),
                MBLconsignee: $('#MBLconsignee').val(),
                MBLnotify_party: $('#MBLnotify_party').val(),
                MBLshipper_info: $('#MBLshipper_info').val(),
                MBLconsignee_info: $('#MBLconsignee_info').val(),
                MBLnotify_party_info: $('#MBLnotify_party_info').val(),
                pieces: $('#pieces').val(),
                lcl_unit: $('#lcl_unit').val(),
                net_weight: $('#net_weight').val(),
                gross_weight: $('#gross_weight').val(),
                volume: $('#volume').val(),
                fee_count: $('#fee_count').val(),
                amount_20gp: $('#amount_20gp').val(),
                amount_40gp: $('#amount_40gp').val(),
                amount_40hq: $('#amount_40hq').val(),
                amount_45gp: $('#amount_45gp').val(),
                amount_45hp: $('#amount_45hp').val(),                
                lcl_shipping_mark: $('#lcl_shipping_mark').val(),
                lcl_cargo_desc: $('#lcl_cargo_desc').val(),                
                billing_method:$('#billing_method input[type="radio"]:checked').val(),
                remark: $('#remark').val(),
                status: $('#status').val()==''?'新建':$('#status').val(),
                itemList : lclOrder.buildCargoDetail()
            };


            //异步向后台提交数据
            $.post('/lclOrder/save', {params:JSON.stringify(order)}, function(data){
                var order = data;
                if(order.ID>0){
                	$("#creator_name").val(order.CREATOR_NAME);
                    $("#create_time").val(order.CREATE_TIME);
                    $("#order_id").val(order.ID);
                    $("#order_no").val(order.ORDER_NO);
                    $("#status").val(order.STATUS);
                    
                    eeda.contactUrl("edit?id",order.ID);
                    $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
                    $('#saveBtn').attr('disabled', false);
                    //异步刷新明细表
                   // salesOrder.refleshTable(order.ID);
                }else{
                    $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                    $('#saveBtn').attr('disabled', false);
                }
                $.unblockUI();
            },'json').fail(function() {
                $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
                $('#saveBtn').attr('disabled', false);
                $.unblockUI();
            });
        }); 
        
        //放货方式radio回显
        var radioVal = $('#hidden_billing_method').val();
        $('#billing_method input[type="radio"]').each(function(){
        	var checkValue = $(this).val();
        	if(radioVal==checkValue){
        		$(this).attr("checked",true);
        	}
        });

        
     });
});