define(['jquery', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
	 $('#updateBtn').on('click',function(){
		 
		 var p_c_d = $('#p_c_d').val();
		 var province = '';
		 var city = '';
		 var district = '';
		 if(p_c_d!=''){
			 var array = p_c_d.split("-");
			 for (var i = 0; i < array.length; i++) {
				 if(i == 0){
					 province = array[i];
				 }else if(i == 1){
					 city = array[i];
				 }else if(i == 2){
					 district = array[i];
				 }
			 }
		 }
		 
		 var order = {};
		 order.id = $('#order_id').val();
		 order.contact_person = $('#contact_person').val();
		 order.phone = $('#phone').val();
		 order.province = province;
		 order.city = city;
		 order.district = district;
		 order.address = $('#address').val();
		 order.address_phone = $('#address_phone').val();
		 order.qq = $('#qq').val();
		 order.intro = $('#intro').val();
		 order.logo = $('#logo').val();
		 
		 $.post('/BusinessAdmin/account/save_info',{jsonStr:JSON.stringify(order)},function(data){
			 if(data){
				 $('#order_id').val(data.ID);
				 $.scojs_message('保存成功', $.scojs_message.TYPE_OK);
			 }else{
				 $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
			 }
		 });
		 
	 });
  });
});