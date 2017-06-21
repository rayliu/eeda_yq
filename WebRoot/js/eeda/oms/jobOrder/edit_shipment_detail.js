define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
    $(document).ready(function() {
    	
    	itemOrder.buildShipmentDetail=function(){
    		var arrays = [];
        	var item = {};
        	item['id'] = $('#shipment_id').val();
        	item['release_type'] = $('#shipmentForm input[type="radio"]:checked').val();
        	item['prepaid'] = $('#prepaid').val($('#prepaid').prop('checked')==true?'Y':'N');
            item['agent_prepaid'] = $('#agent_prepaid').val($('#agent_prepaid').prop('checked')==true?'Y':'N');
        	item['wait_overseaCustom'] = $('#wait_overseaCustom').val($('#wait_overseaCustom').prop('checked')==true?'Y':'N');
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
        
        //放货方式radio回显
        var radioVal = $('#hidden_release_type').val();
        $('#shipmentForm input[type="radio"]').each(function(){
        	var checkValue = $(this).val();
        	if(radioVal==checkValue){
        		$(this).attr("checked",true);
        	}
        });
        //预付回显
        var checkBoxVal = $('#hidden_prepaid').val();
        if(checkBoxVal=='Y'){
            $('#prepaid').attr("checked",true);    	
        }
        else{
        	$('#prepaid').attr("checked",false);
        }
        //代理预付回显
        var checkBoxVal = $('#hidden_agent_prepaid').val();
        if(checkBoxVal=='Y'){
            $('#agent_prepaid').attr("checked",true);     
        }
        else{
            $('#agent_prepaid').attr("checked",false);
        }
        //待海外报关回显
        var checkBoxVal = $('#hidden_wait_overseaCustom').val();
        if(checkBoxVal=='Y'){
            $('#wait_overseaCustom').attr("checked",true);    	
        }
        else{
        	$('#wait_overseaCustom').attr("checked",false);
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
        
        

        //常用海运信息模版
        $('#usedOceanInfo').on('click', '.selectOceanTemplate', function(){
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
            $('#ocean_booking_agent').val(li.attr('booking_agent'));
            $('#ocean_booking_agent_input').val(li.attr('booking_agent_name'));
            $('#carrier').val(li.attr('carrier'));
            $('#carrier_input').val(li.attr('carrier_name'));
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
        
        $('.deleteOceanTemplate').click(function(e) {
        	$(this).attr('disabled', true);
        	e.preventDefault();
        	var li = $(this).parent().parent();
        	var id = li.attr('id');
        	$.post('/jobOrder/deleteOceanTemplate', {id:id}, function(data){
        		$.scojs_message('删除成功', $.scojs_message.TYPE_OK);
        		$(this).attr('disabled', false);
        		li.css("display","none");
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
        
        
        
    });
});