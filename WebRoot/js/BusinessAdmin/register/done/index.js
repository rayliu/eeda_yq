define(['jquery', 'sco', 'file_upload',"validate_cn",'dataTablesBootstrap'], function ($, metisMenu) {
	
	$(document).ready(function() {
		$('.hornav a').click(function(){
	        $('.hornav li').removeClass('current');
	        $(this).parent().addClass('current');
	        $("#personal").hide();
	        $("#company").hide();

	        var curShowType = $(this).data('type');
	        $("#"+curShowType).show();

	        if(curShowType=="personal"){
	            $("#register_type").val("1");
	        }else{
	            $("#register_type").val("2");
	        }
	    });
		//点击下一步
		$("#nextBtn").click(function(){
			var type=$("#register_type").val();
			var user={};
			alert($(":input[name=contact_"+type+"]").val())
			user.login_name=$(":input[name=user_name]").val();
			user.mobile=$(":input[name=phone]").val();
			user.contact=$(":input[name=contact_"+type+"]").val();
			user.contact=$(":input[name=telephone_"+type+"]").val();
			user.contact=$(":input[name=trade_type_"+type+"]").val();
			user.contact=$(":input[name=shop_province_"+type+"]").val();
			user.contact=$(":input[name=shop_city_"+type+"]").val();
			user.contact=$(":input[name=shop_address_"+type+"]").val();
			user.contact=$(":input[name=shop_telephone_"+type+"]").val();
			user.contact=$(":input[name=qq_"+type+"]").val();
			user.contact=$(":input[name=about_"+type+"]").val();
			user.contact=$(":input[name=logo_"+type+"]").val();
			window.location.href="localhost:8080/BusinessAdmin/register/"
		})
		
	});
});