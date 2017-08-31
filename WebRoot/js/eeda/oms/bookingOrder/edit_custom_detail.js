define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
    $(document).ready(function() {
    	
    	
    	var showServiceTab=function(service){
            switch (service){
                case 'leave_port':
                    $('#custom_status').show();
                    $('#custom_leave_port').show();
                    break;
                case 'arrive_port':
                    $('#arrive_custom_status').show();
                    $('#custom_arrive_port').show();
                    break;                         
            }
        };

        var hideServiceTab=function(service){
            switch (service){
                case 'leave_port':
                    $('#custom_status').hide();
                    $('#custom_leave_port').hide();
                    break;
                case 'arrive_port':
                    $('#arrive_custom_status').hide();
                    $('#custom_arrive_port').hide();
                    break;                           
            }
        };
    	
        
        //委托类型checkbox回显,custom_type是用js拿值
        if(custom_type_hidden.indexOf("leave_port")>-1){
        	showServiceTab("leave_port");
        }
        if(custom_type_hidden.indexOf("arrive_port")>-1){
        	showServiceTab("arrive_port");
        }
        
        var checkArray = custom_type_hidden.split(",");
        for(var i=0;i<checkArray.length;i++){
    	    $('#custom_type input[type="checkbox"]').each(function(){
    	        var checkValue=$(this).val();
    	        if(checkArray[i]==checkValue){
    	        	this.checked = true;
    	        }
    	    })
        }
    	
        //单击时，tab的显示隐藏
        $('#custom_type input[type="checkbox"]').change(function(){
            var checkValue=$(this).val();
            if($(this).prop('checked')){
                showServiceTab(checkValue);
            }else{
                hideServiceTab(checkValue);
            }
        });
    	
    	
    	
    	itemOrder.buildCustomDetail=function(){
    		//服务项目checkbox遍历取值
            var custom_type = [];
            $('#custom_type input[type="checkbox"]:checked').each(function(){
            	custom_type.push($(this).val()); 
            });
            var custom_type_str = custom_type.toString();
    		var arrays = [];
        	var item = {};
        	item['id'] = $('#custom_id').val();
        	item['custom_type'] = custom_type_str;
        	var oceanForm = $('#customDetail input,#customDetail select,#customDetail textarea');
        	for(var i = 0; i < oceanForm.length; i++){
        		var name = oceanForm[i].id;
            	var value =oceanForm[i].value;
            	if(name){
            			item[name] = value;
            		}
            	}
        	arrays.push(item);
            return arrays;
        }
    	
        $('#collapseOceanInfo').on('show.bs.collapse', function () {
          $('#collapseOceanIcon').removeClass('fa-angle-double-down').addClass('fa-angle-double-up');
        });
        $('#collapseOceanInfo').on('hide.bs.collapse', function () {
          $('#collapseOceanIcon').removeClass('fa-angle-double-up').addClass('fa-angle-double-down');
        });
        
        $('#singleSearchBtn').click(function(event) {
            var id = $('#order_id').val();
            var custom_broker = $('#custom_broker_ref_office_id').val();
            
            if(custom_broker.trim()==''){
            	$.scojs_message('请先选中报关行', $.scojs_message.TYPE_ERROR);
            }else{
            	if(id!=''){
                	window.open("/customPlanOrder/create?bookingId="+id+"&to_office_id="+custom_broker, '_blank');
                }else{
                	 $.scojs_message('请先保存单据', $.scojs_message.TYPE_ERROR);
                }
            }
        });
        
        
        
    });
});