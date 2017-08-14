define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
    $(document).ready(function() {
    	
    	itemOrder.buildOceanDetail=function(){
    		var arrays = [];
        	var item = {};
        	item['id'] = $('#ocean_id').val();
        	var oceanForm = $('#oceanDetail input,#oceanDetail select,#oceanDetail textarea');
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