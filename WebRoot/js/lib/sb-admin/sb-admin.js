define(function(){
    
    $(document).ready(function() {
        $('#side-menu').metisMenu();

        var menu_width = "0px";
        $(window).bind("load resize", function() {
            if ($(this).width() < 768) {

                $('div.sidebar-collapse').addClass('collapse');
                $('#hide_menu_btn').hide();
                $('#left_side_bar').show();
                $('#page-wrapper').css('margin-left', '0px');
                $('.breadcrumb').css('margin-left', '0px');
            } else {
                $('div.sidebar-collapse').removeClass('collapse');
                $('#hide_menu_btn').show();
                $('#hide_menu_btn').css('left', menu_width);
                $('#page-wrapper').css('margin-left', menu_width);

                loadSideBar();
            }
        });

        var showSideBar=function(){
        	$('#left_side_bar').show();
    		$('#page-wrapper').css('margin-left', menu_width);
    		$('#hide_menu_btn').css('left', menu_width);
            $('.breadcrumb').css('margin-left', menu_width);
    		$('#hide_menu_icon').removeClass('fa-angle-double-right');
    	    $('#hide_menu_icon').addClass('fa-angle-double-left');
    	    if(!!window.localStorage){
                localStorage.setItem("is_show_eeda_side_bar", true);
            }
        };

        var hideSideBar=function(){
        	$('#left_side_bar').hide();

    		$('#page-wrapper').css('margin', '0');
    		$('#hide_menu_btn').css('left', '0px');
            $('.breadcrumb').css('margin-left', '0px');
    		$('#hide_menu_icon').removeClass('fa-angle-double-left');
    	    $('#hide_menu_icon').addClass('fa-angle-double-right');

    	    if(!!window.localStorage){
                localStorage.setItem("is_show_eeda_side_bar", false);
            }
        };

        $('#hide_menu_btn').mouseenter(function() {
            $("#hide_menu_btn").css('background-color', 'rgb(231, 231, 231)');
        }).mouseleave(function() {
            $("#hide_menu_btn").css('background-color', 'rgb(241, 241, 241)');
        });

    	$('#hide_menu_btn').click(function(){
    		if ($('#left_side_bar:visible').length > 0){//the element is visible
    			hideSideBar();
    		}else{
    			showSideBar();
    		}
    	});

    	var loadSideBar=function(){
            if(!!window.localStorage){//查询条件处理
                var is_show_eeda_side_bar = localStorage.getItem('is_show_eeda_side_bar');
                
                if(is_show_eeda_side_bar=='false'){
                    hideSideBar();
                }else{
                    showSideBar();
                }

            }
        };

        loadSideBar();

        
    });
});