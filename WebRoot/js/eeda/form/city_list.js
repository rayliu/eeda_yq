define(['jquery', 'sco'], function ($) {

    $('input.city_input').on('keyup click', function(event) {
        var inputField = $(this);
        var inputField_name = inputField.attr('name');

        // var hiddenField = $("#"+inputField_name);
        var hiddenProvinceField = $("#"+inputField_name+'_province');//这里是方便用户选错时，回选上级

        var locationList = $("#"+inputField_name+"_list");

        var me = this;
        var inputStr = inputField.val();

        searchLocation(null, null, locationList);
        locationList.css({
            left:$(me).position().left+"px",
            top:$(me).position().top+32+"px"
        });
    });

    var searchLocation = function(level, code, locationList){
        var locationListContent = locationList.find('.area-list-content');
        var locLevel = "province";
        level = level | 0;
        if(level == 1){
            locLevel = "city";
        }
        if(level == 2){
            locLevel = "area";
        }

        $.get('/serviceProvider/'+locLevel, {id:code}, function(data){
            locationListContent.empty();
            for(var i = 0; i < data.length; i++){
                var loc = data[i];
                locationListContent.append('<a next-level="'+(level+1)+'" p_code="'+loc.PCODE+'" href="javascript:void(0)" code="'+loc.CODE+'" name="'+loc.NAME+'">'+loc.NAME+'</a>');
            }
            locationList.find('input').removeClass('this');
            locationList.find('input[data-level='+level+']').addClass('this');
            locationList.show();
        },'json');
    };

    $(".area-list-content").on('click', 'a', function(event) {
        event.preventDefault();

        var locationList = $(this).closest('.area-list');
        var locationListContent = $(this).closest('.area-list-content');

        var dataLevel = $(this).attr('next-level');
        var code = $(this).attr('code');
        var name = $(this).attr('name');

        var inputField = $(this).parent().parent().parent().find('.city_input');
        var oldValue = inputField.val();

        var hiddenProvinceField = $(this).parent().parent().parent().find('.province');
        if(dataLevel == 1){
            hiddenProvinceField.val(code);
        }

        if(dataLevel>1){
            name = oldValue+'-'+name;
        }
        inputField.val(name);

        if(dataLevel == 3){
            locationList.hide();
            return;
        }
        searchLocation(dataLevel, code, locationList);

        locationList.find('input').removeClass('this');
        locationList.find('input[data-level='+dataLevel+']').addClass('this');
    });


    $(".area-list-title input").on('click', function(event) {
        event.preventDefault();
        var area_list_title = $(this);

        var inputField = $(this);
        var inputField_name = inputField.attr('name');

        // var hiddenField = $("#"+inputField_name);
        var hiddenProvinceField = $("#"+inputField_name+'_province');//这里是方便用户选错时，回选上级

        var selectedLevel=$(this).attr('data-level');
        var currentLevel=$(this).parent().find('.this').attr('data-level');

        if(selectedLevel>=currentLevel)
            return;
        
        if(selectedLevel<currentLevel && selectedLevel==0){
            inputField.val('');
            hiddenField.val('');
            hiddenProvinceField.val('');
            searchLocation();
        }else{
            inputField.val(inputField.val().split('-')[0]);
            searchLocation(1, hiddenProvinceField.val());
        }
    });

        
});
