define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
    $(document).ready(function() {
    	
    	var showServiceTab=function(service){
            switch (service){
                case 'land_take':
                    $('#land_take').show();
                    break;
                case 'land_delivery':
                    $('#land_delivery').show();
                    break;                         
            }
        };

        var hideServiceTab=function(service){
            switch (service){
                case 'land_take':
                    $('#land_take').hide();
                    break;
                case 'land_delivery':
                    $('#land_delivery').hide();
                    break;                           
            }
        };
    	
    	
    	//委托类型checkbox回显,land_type是用js拿值
        var checkArray = land_type_hidden.split(",");
        for(var i=0;i<checkArray.length;i++){
    	    $('#land_type input[type="checkbox"]').each(function(){
    	        var checkValue=$(this).val();
    	        if(checkArray[i]==checkValue){
    	        	this.checked = true;
                    showServiceTab(checkValue);
    	        }
    	    })
        }
    	
        //单击时，tab的显示隐藏
        $('#land_type input[type="checkbox"]').change(function(){
            var checkValue=$(this).val();
            if($(this).prop('checked')){
                showServiceTab(checkValue);
            }else{
                hideServiceTab(checkValue);
            }
        });
    	
    	
//        order.land_type = land_type_str;
    	
    	itemOrder.buildLandDetail=function(){
    		//服务项目checkbox遍历取值
            var land_type = [];
            $('#land_type input[type="checkbox"]:checked').each(function(){
            	land_type.push($(this).val()); 
            });
            var land_type_str = land_type.toString();
    		var arrays = [];
        	var item = {};
        	item['id'] = $('#land_id').val();
        	item['land_type'] = land_type_str;
        	var oceanForm = $('#landDetail input,#landDetail select,#landDetail textarea');
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
        
        
    });
});