define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
    $(document).ready(function() {
    	
    	itemOrder.buildShipmentDetail=function(){
    		var arrays = [];
        	var item = {};
        	item['id'] = $('#shipment_id').val();
        	item['customer_id'] = $('#customer_id').val();
        	item['release_type'] = $('#release_radio_div input[type="radio"]:checked').val();
        	item['agent_type'] = $('#overseaAgent_radio_div input[type="radio"]:checked').val();
        	item['prepaid'] = $('#prepaid').val($('#prepaid').prop('checked')==true?'Y':'N');
            item['agent_prepaid'] = $('#agent_prepaid').val($('#agent_prepaid').prop('checked')==true?'Y':'N');
        	item['wait_overseaCustom'] = $('#wait_overseaCustom').val($('#wait_overseaCustom').prop('checked')==true?'Y':'N');
        	item['is_need_afr'] = $('#is_need_afr').val($('#is_need_afr').prop('checked')==true?'Y':'N');
        	item['is_need_custom_apply'] = $('#is_need_custom_apply').val($('#is_need_custom_apply').prop('checked')==true?'Y':'N');
        	item['is_need_delivery'] = $('#is_need_delivery').val($('#is_need_delivery').prop('checked')==true?'Y':'N');
        	var shipmentForm = $('#shipmentForm input,#shipmentForm select,#shipmentForm textarea');
        	for(var i = 0; i < shipmentForm.length; i++){
        		var name = shipmentForm[i].id;
            	var value =shipmentForm[i].value;
            	if(name){
            		
            		if(name.indexOf("ocean_")==0){
            			var rName = name.replace("ocean_","");
            			item[rName] = value;
            		}else{
            			item[name] = value;
            		}
            	}
        	}
        	arrays.push(item);
            return arrays;
        };
      
        
      //海运模板点击
        $('#collapseOceanInfo').on('show.bs.collapse', function () {
        	var	div = $('#usedOceanInfoDetail').empty();
            $('#collapseOceanIcon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
            var customer_id = $('#customer_id').val();
            if(!customer_id){
            	$.scojs_message('请先选择客户', $.scojs_message.TYPE_ERROR);
            	return
            }else{
            	$.post('/jobOrder/oceanTemplateShow', {customer_id:customer_id}, function(data){
            		if(data){
            			for(var i = 0;i<data.length;i++){
            				var li = '';
            				var li_val = '';
            				li +='<li '
            					+' id="'+data[i].ID+'" '
        						+' MBLshipper_id="'+(data[i].MBLSHIPPER==null?'':data[i].MBLSHIPPER)+'" '
        						+' MBLshipper_abbr="'+(data[i].MBLSHIPPERABBR==null?'':data[i].MBLSHIPPERABBR)+'" '
        						+' MBLshipper_info="'+data[i].MBLSHIPPER_INFO+'" '
        						+' MBLconsignee_id="'+data[i].MBLCONSIGNEE+'" '
        						+' MBLconsignee_abbr="'+(data[i].MBLCONSIGNEEABBR==null?'':data[i].MBLCONSIGNEEABBR)+'" '
        						+' MBLconsignee_info="'+data[i].MBLCONSIGNEE_INFO+'" '
        						+' MBLnotify_id="'+data[i].MBLNOTIFY_PARTY+'" '
        						+' MBLnotify_abbr="'+(data[i].MBLNOTIFY_PARTYABBR==null?'':data[i].MBLNOTIFY_PARTYABBR)+'" '
        						+' MBLnotify_info="'+data[i].MBLNOTIFY_PARTY_INFO+'" '
        						+' HBLshipper_id="'+data[i].HBLSHIPPER+'" '
        						+' HBLshipper_abbr="'+(data[i].HBLSHIPPERABBR==null?'':data[i].HBLSHIPPERABBR)+'" '
        						+' HBLshipper_info="'+data[i].HBLSHIPPER_INFO+'" '
        						+' HBLconsignee_id="'+data[i].HBLCONSIGNEE+'" '
        						+' HBLconsignee_abbr="'+(data[i].HBLCONSIGNEEABBR==null?'':data[i].HBLCONSIGNEEABBR)+'" '
        						+' HBLconsignee_info="'+data[i].HBLCONSIGNEE_INFO+'" '
        						+' HBLnotify_id="'+data[i].HBLNOTIFY_PARTY+'" '
        						+' HBLnotify_abbr="'+data[i].HBLNOTIFY_PARTY+'" '
        						+' HBLconsignee_info="'+data[i].HBLCONSIGNEE_INFO+'" '
        						+' HBLnotify_id="'+data[i].HBLNOTIFY_PARTY+'" '
        						+' HBLnotify_abbr="'+data[i].HBLNOTIFY_INFO+'" '
        						+' HBLnotify_info="'+data[i].HBLNOTIFY_PARTY_INFO+'" '
        						+' por_id="'+data[i].POR+'" '
        						+' pol_id="'+data[i].POL+'" '
        						+' pod_id="'+data[i].POD+'" '
        						+' fnd_id="'+data[i].FND+'" '
        						+' por_name="'+(data[i].POR_NAME==null?'':data[i].POR_NAME)+'" '
        						+' pol_name="'+(data[i].POL_NAME==null?'':data[i].POL_NAME)+'" '
        						+' pod_name="'+(data[i].POD_NAME==null?'':data[i].POD_NAME)+'" '
        						+' fnd_name="'+(data[i].FND_NAME==null?'':data[i].FND_NAME)+'" '
        						+' head_carrier="'+data[i].HEAD_CARRIER+'" '
        						+' head_carrier_name="'+(data[i].HEAD_CARRIER_NAME==null?'':data[i].HEAD_CARRIER_NAME)+'" '
        						+' oversea_agent="'+data[i].OVERSEA_AGENT+'" '
        						+' oversea_agent_name="'+(data[i].OVERSEA_AGENT_NAME==null?'':data[i].OVERSEA_AGENT_NAME)+'" '
        						+' oversea_agent_info="'+(data[i].OVERSEA_AGENT_INFO==null?'':data[i].OVERSEA_AGENT_INFO)+'" '
        						+' release_type="'+data[i].RELEASE_TYPE+'" '
        						+' shipping_mark="'+data[i].SHIPPING_MARK+'" '
        						+' cargo_desc="'+data[i].CARGO_DESC+'" '
        						+'>';
        					li_val = '<span></span>  MBLShipper ： '+data[i].MBLSHIPPERABBR+' ,  MBLConsignee ：'+data[i].MBLCONSIGNEEABBR+' , 启运港  POL :'+data[i].POL_NAME+' <br/>'
        							+' HBLShipper : '+data[i].HBLSHIPPERABBR+' ,  HBLConsignee :'+data[i].HBLCONSIGNEEABBR+' , 目的港  POD : '+data[i].POD_NAME;
            				
            				div.append('<ul class="usedOceanInfo" id="'+data[i].ID+'">'
            						+li
            						+'<div class="radio">'
            						+'	<a class="deleteOceanTemplate" style="margin-right: 10px;padding-top: 5px;float: left;">删除</a>'
            						+'	<div class="selectOceanTemplate" style="margin-left: 60px;padding-top: 0px;">'
            						+'      <input type="radio" value="1" name="usedOceanInfo">'
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
      
      
        
      
        
        //保存海运模板
        $('#oceanBtnTemplet').click(function(){
        	var oceanOrderTemplet={};
        	oceanOrderTemplet.oceanTemplet=itemOrder.buildShipmentDetail();
        	$.post('/jobOrder/saveOceanTemplet',{params:JSON.stringify(oceanOrderTemplet)},function(data){
        		$.scojs_message('海运信息模板保存保存成功', $.scojs_message.TYPE_OK);
        	});
        });
        
        
        //放货方式radio回显
        var radioVal = $('#hidden_release_type').val();
        $('#release_radio_div input[type="radio"]').each(function(){
        	var checkValue = $(this).val();
        	if(radioVal==checkValue){
        		$(this).attr("checked",true);
        	}
        });
        
        //国外代理回显
        var agentTypeRedio = $('#hidden_agent_type').val();
        $('#overseaAgent_radio_div input[type="radio"]').each(function(){
        	var checkValue = $(this).val();
        	if(agentTypeRedio==checkValue){
        		$(this).attr('checked',true);
        	}
        });
        
        //预付回显
        var checkBoxVal = $('#hidden_prepaid').val();
        if(checkBoxVal=='Y'){
            $('#prepaid').attr("checked",true);    	
        }else{
        	$('#prepaid').attr("checked",false);
        }
        //代理预付回显
        var checkBoxVal = $('#hidden_agent_prepaid').val();
        if(checkBoxVal=='Y'){
            $('#agent_prepaid').attr("checked",true);     
        }else{
            $('#agent_prepaid').attr("checked",false);
        }
        //待海外报关回显
        var checkBoxVal = $('#hidden_wait_overseaCustom').val();
        if(checkBoxVal=='Y'){
            $('#wait_overseaCustom').attr("checked",true);    	
        }else{
        	$('#wait_overseaCustom').attr("checked",false);
        }
        
        //AFR申报回显
        var checkBoxVal = $('#hidden_is_need_afr').val();
        if(checkBoxVal=='Y'){
        	$('#is_need_afr').attr("checked",true);
        }else{
        	$('#is_need_afr').attr("checked",false);
        }
        
        //海外报关回显
        var checkBoxVal = $('#hidden_is_need_custom_apply').val();
        if(checkBoxVal=='Y'){
        	$('#is_need_custom_apply').attr("checked",true);
        }else{
        	$('#is_need_custom_apply').attr("checked",false);
        }        
        //海外陆运配送回显
        var checkBoxVal = $('#hidden_is_need_delivery').val();
        if(checkBoxVal=='Y'){
        	$('#is_need_delivery').attr("checked",true);
        	$('#is_needed_land').attr("checked",true);
        }else{
        	$('#is_need_delivery').attr("checked",false);
        	$('#is_needed_land').attr("checked",false);
        }  

        //选择卸货港时自动填上目的港
        $('#pod_list').on('mousedown', '.fromLocationItem', function(){
    	    	$('#fnd_input').val($('#pod_input').val());
    	    	$('#fnd').val($('#pod').val());
        });

        $('#pod_list').on('keydown', 'li', function(event){
            if (event.keyCode == 13) {
                $('#fnd_input').val($('#pod_input').val());
                $('#fnd').val($('#pod').val());
            }
        });
        //SO NO赋值
        $('#SONO').keyup(function(){
        	$('#SONO_land').val($('#SONO').val());
        });
        $('#SONO_land').keyup(function(){
        	$('#SONO').val($('#SONO_land').val());
        });
        
      //MBL NO赋值
        $('#mbl_no').keyup(function(){
        	$('#mbl_no_land').val($('#mbl_no').val());
        });
        $('#mbl_no_land').keyup(function(){
        	$('#mbl_no').val($('#mbl_no_land').val());
        });	
    
    
    
        //ocean_MBLnotify_party_info 默认制是SAME AS CONSIGNEE
        $('#copy_consignee').click(function(){
        	var boolean = $('#copy_consignee').is(':checked');
        	if(boolean){
        		$('#ocean_MBLnotify_party_info').val('SAME AS CONSIGNEE');
        	}else{
        		$('#ocean_MBLnotify_party_info').val('');
        	}
        });
        if($('#ocean_MBLnotify_party_info').val()=='SAME AS CONSIGNEE'){
        	$('#copy_consignee').attr('checked',true)
        }
        
      //ocean_HBLnotify_party_info 默认制是SAME AS CONSIGNEE
        $('#copyConsignee').click(function(){
        	var boolean = $('#copyConsignee').is(':checked');
        	if(boolean){
        		$('#ocean_HBLnotify_party_info').val('SAME AS CONSIGNEE');
        	}else{
        		$('#ocean_HBLnotify_party_info').val('');
        	}
        });
        if($('#ocean_HBLnotify_party_info').val()=='SAME AS CONSIGNEE'){
        	$('#copyConsignee').attr('checked',true)
        }

        

        //常用海运信息模版
        $('#usedOceanInfoDetail').on('click', '.selectOceanTemplate', function(){
            var li = $(this).parent().parent();
            $('#ocean_HBLshipper_input').val(li.attr('HBLshipper_abbr'));
            $('#ocean_HBLshipper_info').val(li.attr('HBLshipper_info'));
            $('#ocean_HBLshipper').val(li.attr('HBLshipper_id'));
            $('#ocean_HBLconsignee_input').val(li.attr('HBLconsignee_abbr'));
            $('#ocean_HBLconsignee_info').val(li.attr('HBLconsignee_info'));
            $('#ocean_HBLconsignee').val(li.attr('HBLconsignee_id'));
            $('#ocean_HBLnotify_party_input').val(li.attr('HBLnotify_abbr'));
            $('#ocean_HBLnotify_party_info').val(li.attr('HBLnotify_info'));
            $('#ocean_HBLnotify_party').val(li.attr('HBLnotify_id'));
            
            $('#ocean_MBLshipper_input').val(li.attr('MBLshipper_abbr'));
            $('#ocean_MBLshipper_info').val(li.attr('MBLshipper_info'));
            $('#ocean_MBLshipper').val(li.attr('MBLshipper_id'));
            $('#ocean_MBLconsignee_input').val(li.attr('MBLconsignee_abbr'));
            $('#ocean_MBLconsignee_info').val(li.attr('MBLconsignee_info'));
            $('#ocean_MBLconsignee').val(li.attr('MBLconsignee_id'));
            $('#ocean_MBLnotify_party_input').val(li.attr('MBLnotify_abbr'));
            $('#ocean_MBLnotify_party_info').val(li.attr('MBLnotify_info'));
            $('#ocean_MBLnotify_party').val(li.attr('MBLnotify_id'));
            $('#por_input').val(li.attr('por_name'));
            $('#por').val(li.attr('por_id'));
            $('#pol_input').val(li.attr('pol_name'));
            $('#pol').val(li.attr('pol_id'));
            $('#pod_input').val(li.attr('pod_name'));
            $('#pod').val(li.attr('pod_id'));
            $('#fnd_input').val(li.attr('fnd_name'));
            $('#fnd').val(li.attr('fnd_id'));
//            $('#ocean_booking_agent').val(li.attr('booking_agent'));
//            $('#ocean_booking_agent_input').val(li.attr('booking_agent_name'));
//            $('#carrier').val(li.attr('carrier'));
//            $('#carrier_input').val(li.attr('carrier_name'));
            $('#head_carrier').val(li.attr('head_carrier'));
            $('#head_carrier_input').val(li.attr('head_carrier_name'));
            $('#oversea_agent').val(li.attr('oversea_agent'));
            $('#oversea_agent_input').val(li.attr('oversea_agent_name'));
            $('#oversea_agent_info').val(li.attr('oversea_agent_info'));
            $('#ocean_shipping_mark').val(li.attr('shipping_mark'));
            $('#ocean_cargo_desc').val(li.attr('cargo_desc'));
            
            var release_type = li.attr('release_type');
            $('#shipmentForm input[type="radio"]').each(function(){
            	var checkValue = $(this).val();
            	if(release_type==checkValue){
                    this.checked = true;
            	}
            });
        });
        $('#collapseOceanInfo').on('show.bs.collapse', function () {
          $('#collapseOceanIcon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
        });
        $('#collapseOceanInfo').on('hide.bs.collapse', function () {
          $('#collapseOceanIcon').removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
        });
        
        $('#usedOceanInfoDetail').on('click', '.deleteOceanTemplate', function(){
        	$(this).attr('disabled', true);
        	var ul = $(this).parent().parent();
    	  	var id = ul.attr('id');
        	$.post('/jobOrder/deleteOceanTemplate', {id:id}, function(data){
        		$.scojs_message('删除成功', $.scojs_message.TYPE_OK);
        		$(this).attr('disabled', false);
        		ul.css("display","none");
        	},'json').fail(function() {
        		$(this).attr('disabled', false);
                $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR);
            });
        })
        
        //相关文档
        $('#collapseDocInfo').on('show.bs.collapse', function () {
          $('#collapseDocIcon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
        });
        $('#collapseDocInfo').on('hide.bs.collapse', function () {
          $('#collapseDocIcon').removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
        });
        
        
        //------------事件处理,文档table
        var docTable = eeda.dt({
            id: 'ocean_table',
            autoWidth: false,
            columns:[
    			{ "data":"ID","width": "10px",
    			    "render": function ( data, type, full, meta ) {
    			    	if(data)
    			    		return '<input type="checkbox" class="checkBox" style="width:30px">';
    			    	else 
    			    		return '<input type="checkbox" class="checkBox" style="width:30px" disabled>';
    			    }
    			},
                { "width": "50px",
                    "render": function ( data, type, full, meta ) {
                    	return '<button type="button" class="delete btn table_btn delete_btn btn-xs">删除</button>'
                    	+'<button type="button" class="confirmSend btn table_btn delete_btn btn-xs">发送PC资料</button>';
                    }
                },
                { "data": "DOC_NAME","width": "280px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<a class="doc_name" href="/upload/doc/'+data+'" style="width:300px" target="_blank">'+data+'</a>';
                    }
                },
                { "data": "C_NAME","width": "180px",
                    "render": function ( data, type, full, meta ) {
                    	if(!data)
                            data='';
                    	return data;
                    }
                },
                { "data": "UPLOAD_TIME", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                },
                { "data": "REMARK","width": "280px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" name="remark" value="'+data+'" class="form-control" style="width:300px"/>';
                    }
                },
                { "data": "SENDER", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                },
                { "data": "SEND_TIME", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                },
                { "data": "SEND_STATUS", "width": "180px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                }
            ]
        });
        
        
        
        //海运内容disabled
        var oceanInfoShowHide=function(){
        	$('#shipmentForm input,#shipmentForm textarea').attr('disabled',true);
            $('#ocean_cargo_table input,#ocean_cargo_table select,#ocean_cargo_table button').attr('disabled',true);
            $('#orderForm input,#orderForm textarea,#orderForm select,#orderForm text').attr('disabled',true);
            $('#add_ocean_cargo').hide(); 
            $('#transport_type input,#supplier_contract_type input,#customer_contract_type input').attr('disabled',false);
        }
        
        
        var submit_agent_flag = $('#submit_agent_flag').val();
        if(submit_agent_flag=='Y'){
     	   if($('#status').val()!="审核不通过"){
     		   oceanInfoShowHide();
     		   $('#submitOverseaAgent').attr('disabled',true);    		   
     	   }else{
     		   $('#submitOverseaAgent').attr('disabled',false);  
     	   }    	   
        }
        
        
        
        //提交给海外代理
        $('#submitOverseaAgent').on('click',function(){
        	var oversea_agent=$('#oversea_agent').val()
        	if(!oversea_agent){
        		$.scojs_message('请选择代理', $.scojs_message.TYPE_ERROR);
        		$('#oversea_agent_input').focus();
        		return;
        	}
        	var oversea_agentRadio=$('#overseaAgent_radio_div input[type="radio"]:checked').val()
        	if(!oversea_agentRadio){
        		$.scojs_message('请选择代理类型：国内或者国外', $.scojs_message.TYPE_ERROR);
        		return;
        	}
        	var subAgentCondition_json={};
        	subAgentCondition_json.order_id=$('#order_id').val();
        	subAgentCondition_json.oversea_agent=$('#oversea_agent').val();
        	subAgentCondition_json.agent_type=$('#overseaAgent_radio_div input[type="radio"]:checked').val();
        	subAgentCondition_json.is_need_afr=($('#is_need_afr').prop('checked')==true?'Y':'N');
        	subAgentCondition_json.is_need_custom_apply=($('#is_need_custom_apply').prop('checked')==true?'Y':'N');
        	subAgentCondition_json.is_need_delivery=($('#is_need_delivery').prop('checked')==true?'Y':'N');
        	subAgentCondition_json.submit_agent_flag=$('#submit_agent_flag').val();
        	$.post('/jobOrder/subToAgent',{params:JSON.stringify(subAgentCondition_json)},function(data){
        		if(data){
        			$.scojs_message('提交成功', $.scojs_message.TYPE_OK);
        			if(data.SUBMIT_AGENT_FLAG=='Y'){
        				$('#submit_agent_flag').val(data.SUBMIT_AGENT_FLAG);
        				oceanInfoShowHide();
        			}
        		}
        		
        	},'json').fail(function(){
        		$.scojs_message('提交失败', $.scojs_message.TYPE_ERROR);
        	});
        	        	
        })
        //海外代理的内容隐藏已显示
        var from_order_type = $("#from_order_type").val();
        var from_order_no= $("#plan_order_no").val();
        if(from_order_type=="forwarderJobOrder"){
        	if(from_order_no){
        		$('#submitAgentDiv').hide();
        		if($('#status').val()!="审核通过"){
        			$('#saveBtn').css('display','none');
        			$("#passBtn").attr('disabled',false);
        			if($('#status').val()=="审核不通过"){
        				$("#refuseBtn").attr('disabled',true);
        			}
        		}else{
        			$("#passBtn").attr('disabled',true);
        		}        		
        		$('#agentDiv').show();
        		$('#auditBtn').show();
        		if($('#is_need_delivery').val()){
        			$('#is_need_delivery').prop('checked',true);
        		}else{
        			$('#is_need_delivery').prop('checked',false);
        		}
        		oceanInfoShowHide();
        	}
        }else{
        		$('#submitAgentDiv').show();
            	$('#saveBtn').css('display','');
        		$('#agentDiv').hide();
        		$('#auditBtn').hide();
        }
        
       
       
        
        
        //AFR已申报 ， 报关已放行
        $('#AFR_done,#custom_done').click(function(){
        	var thisVal=$(this).attr("id");
        	var afr_done_time=$('#afr_done_time').val();
        	var custom_done_time=$('#custom_done_time').val();
        	var order_id = $('#order_id').val();
    		var from_order_id = $('#from_order_id').val();
    		var from_order_type = $('#from_order_type').val();        		
    		$.post('/jobOrder/AFRCustomDone',{order_id:order_id,from_order_id:from_order_id,from_order_type:from_order_type,
    			thisVal:thisVal,afr_done_time:afr_done_time,custom_done_time:custom_done_time},function(data){
    				if(data){
    					if(data.AFR_DONE_TIME){
    						$('#afr_done_time').val(data.AFR_DONE_TIME);
    					}
    					if(data.CUSTOM_DONE_TIME){
    						$('#custom_done_time').val(data.CUSTOM_DONE_TIME);
    					}
    					$.scojs_message('AFR已申报回填成功', $.scojs_message.TYPE_OK);
    				}
    			
    		},'json').fail(function(){
    			$.scojs_message('提交失败', $.scojs_message.TYPE_ERROR);
    		});
        });
    });
});