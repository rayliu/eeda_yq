 $(document).ready(function() {
	 $('#menu_profile').addClass('active').find('ul').addClass('in');
    	var province = '${location.province!''}';
    	var city= '${location.city!''}';
    	var district = '${location.district!''}';
    	
    	$("#mbProvince").val(province);
    	$("#cmbCity").val(city);
    	$("#cmbArea").val(district);
    	
    	var payment = '${party.payment!''}';
    	$("#payment").val(payment);
    	
        $('#customerForm').validate({
            rules: {
              company_name: {//form 中company_name为必填, 注意input 中定义的id, name都要为company_name
                required: true
              },
              abbr:{//form 中 abbr为必填
                required: true
              },
              contact_person:{//form 中 name为必填
            	  required: true
              },
              location:{
                required: true
              },
           	  email:{
                email: true
              }
            },
            highlight: function(element) {
                $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
            },
            success: function(element) {
                element.addClass('valid').closest('.form-group').removeClass('has-error').addClass('has-success');
            }
        });
        //获取全国省份
       $(function(){
        	var province = $("#mbProvince");
        	$.post('/serviceProvider/province',function(data){
        		province.append("<option>--请选择省份--</option>");
				var hideProvince = $("#hideProvince").val();
        		for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideProvince){
        				province.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
        				
        				
					}else{
        				province.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
        		
        	},'json');
       });
        //获取省份的城市
        $('#mbProvince').on('change', function(){
        	//var inputStr = $(this).parent("option").attr('id'); 
			var inputStr = $(this).val();
			$.get('/serviceProvider/city', {id:inputStr}, function(data){
				var cmbCity =$("#cmbCity");
				var cmbArea =$("#cmbArea");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				cmbArea.empty()
				for(var i = 0; i < data.length; i++)
				{
					cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
				}
				
			},'json');
		});
        //获取城市的区县
        $('#cmbCity').on('change', function(){
        	//var inputStr = $(this).parent("option").attr('id'); 
			var inputStr = $(this).val();
			var code = $("#location").val(inputStr);
			
			$.get('/serviceProvider/area', {id:inputStr}, function(data){
				var cmbArea =$("#cmbArea");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				for(var i = 0; i < data.length; i++)
				{
					cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");	
				}
				
			},'json');
		});
        
        $('#cmbArea').on('change', function(){
        	//var inputStr = $(this).parent("option").attr('id'); 
			var inputStr = $(this).val();
			var code = $("#location").val(inputStr);
		});         

        // 回显城市
		var hideProvince = $("#hideProvince").val();
        $.get('/serviceProvider/searchAllCity', {province:hideProvince}, function(data){
			if(data.length > 0){
				var cmbCity =$("#cmbCity");
				cmbCity.empty();
				cmbCity.append("<option>--请选择城市--</option>");
				var hideCity = $("#hideCity").val();
				for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideCity){
						cmbCity.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
					}else{
						cmbCity.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
			}
		},'json');

        // 回显区
        var hideCity = $("#hideCity").val();
        $.get('/serviceProvider/searchAllDistrict', {city:hideCity}, function(data){
			if(data.length > 0){
				var cmbArea =$("#cmbArea");
				cmbArea.empty();
				cmbArea.append("<option>--请选择区(县)--</option>");
				var hideDistrict = $("#hideDistrict").val();
				for(var i = 0; i < data.length; i++)
				{
					if(data[i].NAME == hideDistrict){
						cmbArea.append("<option value= "+data[i].CODE+" selected='selected'>"+data[i].NAME+"</option>");
					}else{
						cmbArea.append("<option value= "+data[i].CODE+">"+data[i].NAME+"</option>");						
					}
				}
			}
		},'json');
    });