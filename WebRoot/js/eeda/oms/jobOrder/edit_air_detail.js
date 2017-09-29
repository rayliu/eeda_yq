define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	itemOrder.buildAirDetail=function(){
		var arrays = [];
    	var item = {};
    	item['id'] = $('#air_id').val();
    	item['customer_id'] = $('#customer_id').val();
    	var airForm = $('#airForm input,#airForm select,#airForm textarea');
    	for(var i = 0; i < airForm.length; i++){
    		var name = airForm[i].id;
        	var value =airForm[i].value;
        	if(name){
        		if(name.indexOf("air_")==0){
        			var rName = name.replace("air_","");
        			item[rName] = value;
        		}else{
        			item[name] = value;
        		}
        	}
    	}
    	arrays.push(item);
        return arrays;
    };
    
    
    //常用空运信息模版
    $('#usedAirInfoDetail').on('click', '.selectAirTemplate', function(){
    	var li = $(this).parent().parent();
        $('#shipper_input').val(li.attr('shipper_abbr'));
        $('#shipper_info').val(li.attr('shipper_info'));
        $('#shipper').val(li.attr('shipper_id'));
        $('#consignee_input').val(li.attr('consignee_abbr'));
        $('#consignee_info').val(li.attr('consignee_info'));
        $('#consignee').val(li.attr('consignee_id'));
        $('#notify_party_input').val(li.attr('notify_abbr'));
        $('#notify_party_info').val(li.attr('notify_info'));
        $('#notify_party').val(li.attr('notify_id'));
        $('#booking_agent_input').val(li.attr('booking_agent_name'));
        $('#booking_agent').val(li.attr('booking_agent'));
        $('#goods_mark').val(li.attr('goods_mark'));
        $('#shipping_mark').val(li.attr('shipping_mark'));
    });
    $('#collapseAirInfo').on('show.bs.collapse', function () {
      $('#collapseAirIcon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
    });
    $('#collapseAirInfo').on('hide.bs.collapse', function () {
      $('#collapseAirIcon').removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
    });
    
    $('.deleteAirTemplate').click(function(e) {
    	$(this).attr('disabled', true);
    	e.preventDefault();
    	var li = $(this).parent().parent();
    	var id = li.attr('id');
    	$.post('/jobOrder/deleteAirTemplate', {id:id}, function(data){
    		$.scojs_message('删除成功', $.scojs_message.TYPE_OK);
    		$(this).attr('disabled', false);
    		li.css("display","none");
    	},'json').fail(function() {
    		$(this).attr('disabled', false);
            $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
        });
    })
    
    //海运模板点击
    $('#collapseAirInfo').on('show.bs.collapse', function () {
    	var	div = $('#usedAirInfoDetail').empty();
        $('#collapseAirIcon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
        var customer_id = $('#customer_id').val();
        if(!customer_id){
        	$.scojs_message('请先选择客户', $.scojs_message.TYPE_ERROR);
        	return
        }else{
        	$.post('/jobOrder/airTemplateShow', {customer_id:customer_id}, function(data){
        		if(data){
        			for(var i = 0;i<data.length;i++){
        				var li = '';
        				var li_val = '';
        				li +='<li '
        					+' id="'+data[i].ID+'" '
    						+' shipper="'+(data[i].SHIPPER==null?'':data[i].SHIPPER)+'" '
    						+' shipper_abbr="'+(data[i].SHIPPERABBR==null?'':data[i].SHIPPERABBR)+'" '
    						+' shipper_info="'+data[i].SHIPPER_INFO+'" '
    						+' consignee="'+data[i].CONSIGNEE+'" '
    						+' consignee_abbr="'+(data[i].CONSIGNEEABBR==null?'':data[i].CONSIGNEEABBR)+'" '
    						+' consignee_info="'+data[i].CONSIGNEE_INFO+'" '
    						+' notify="'+data[i].NOTIFY+'" '
    						+' notify_abbr="'+(data[i].NOTIFY_PARTYABBR==null?'':data[i].NOTIFY_PARTYABBR)+'" '
    						+' notify_info="'+data[i].NOTIFY_PARTY_INFO+'" '
    						+' booking_agent="'+data[i].BOOKING_AGENT+'" '
    						+' booking_agent_name="'+(data[i].BOOKING_AGENT_NAME==null?'':data[i].BOOKING_AGENT_NAME)+'" '
    						+' shipping_mark="'+(data[i].SHIPPING_MARK==null?'':data[i].SHIPPING_MARK)+'" '
    						+' goods_mark="'+(data[i].GOODS_MARK==null?'':data[i].GOODS_MARK)+'" '
    						+'>';
    					li_val = '<span></span>  发货人Shipper ： '+(data[i].SHIPPERABBR==null?'':data[i].SHIPPERABBR)
    							+',  收货人Consignee ：'+(data[i].CONSIGNEEABBR==null?'':data[i].CONSIGNEEABBR)
    							+', 通知人NotifyParty:'+(data[i].NOTIFY_ABBR==null?'':data[i].NOTIFY_ABBR)
    							+' ;<br/>';
        				
        				div.append('<ul class="usedAirInfo" id="'+data[i].ID+'">'
        						+li
        						+'<div class="radio">'
        						+'	<a class="deleteAirTemplate" style="margin-right: 10px;padding-top: 5px;float: left;">删除</a>'
        						+'	<div class="selectAirTemplate" style="margin-left: 60px;padding-top: 0px;">'
        						+'      <input type="radio" value="1" name="usedAirInfo">'
        						+		li_val
        						+'	</div>'
        						+'</div><hr/>'
        						+'</li>'
        						+'</ul>');
        			}
        		}
        	});
        }
    });
	    //保存空运模板
	    $('#airBtnTemplet').click(function(){
	    	var airOrderTemplet={};
	    	airOrderTemplet.airTemplet=itemOrder.buildAirDetail();
	    	$.post('/jobOrder/saveAirTemplet',{params:JSON.stringify(airOrderTemplet)},function(data){
	    		$.scojs_message('空运信息模板保存保存成功', $.scojs_message.TYPE_OK);
	    	});
	    });
	    
	});
});