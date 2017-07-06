define(['jquery', 'metisMenu', 'template', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', './edit_ocean_table',
        './edit_ocean_item_table','./edit_internal_trade_table','./edit_bulkCargo_table','./edit_bulkCargo_item_table'], function ($, metisMenu) { 

  $(document).ready(function() {

	  
    	var id = $("#partyId").val();
    	if(id != null && id != ""){
    		$("#addChargeType").attr("disabled",false);
    	}

    	
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
        
         

      
    //构造主表json
      var buildOrder = function(){
      	var item = {};
      	item.id = $('#partyId').val();
      	var orderForm = $('#customerForm input,#customerForm select,#customerForm textarea');
      	for(var i = 0; i < orderForm.length; i++){
      		var name = orderForm[i].id;
          	var value =orderForm[i].value;
          	if(name){
          		item[name] = value;
          	}
      	}
          return item;
      }
      
      //构造checkBox保存数据
      var getQualification_type=function(name){
    	  var qualification_type = [];
    	  $('#'+name+' input[type="checkbox"]').each(function(){
  	        	var checkValue=$(this).val();
  	        	if($(this).prop('checked')){
  	        		qualification_type.push(checkValue);
  	        	}
  	      });
    	  return qualification_type.toString();
      }
      
      
      //自动提交改为手动提交
      $("#saveBtn").click(function(){
    	 if(!$("#customerForm").valid()){
    		  return false;
    	 }
    	 
    	 var order = {};
    	 order = buildOrder();
    	 order.sp_qualification_type = getQualification_type('sp_qualification_type_val');
    	 if(order.sp_qualification_type.indexOf("ocean_shipment") >= 0){
    		 order.oceanCargo = itemOrder.buildOceanCargoDetail();
    		 order.oceanCargoItem = itemOrder.buildOceanCargoItemDetail(); 
    		 order.ocean_qualification_type = getQualification_type('ocean_qualification_type_val');
    		 order.ocean_shipment = $('#ocean_shipment').val();
    	 }
    	 if(order.sp_qualification_type.indexOf("internal_trade") >= 0){
    		 order.internalTrade = itemOrder.buildInternalTradeDetail(); 
    		 order.trade_qualification_type = getQualification_type('trade_qualification_type_val');
    	 }
    	 if(order.sp_qualification_type.indexOf("ocean_bulkCargo") >= 0){
    		 order.bulkCargo = itemOrder.buildBulkCargoDetail();
    		 order.bulkCargoItem = itemOrder.buildBulkCargoItemDetail();
    		 order.bulk_qualification_type = getQualification_type('bulk_qualification_type_val');
    		 order.bulk_shipment = $('#bulk_shipment').val();
    	 }
    	 if(order.sp_qualification_type.indexOf("land_transport") >= 0){
    		 order.landTransport = itemOrder.buildLandTransportDetail(); 
    		 order.landTransportItem = itemOrder.buildLandTransportItemDetail(); 
    		 order.land_qualification_type = getQualification_type('land_qualification_type_val');
    	 }
    	 if(order.sp_qualification_type.indexOf("storage") >= 0){
    		 order.storage = itemOrder.buildStorageDetail(); 
    		 order.storage_qualification_type = getQualification_type('storage_qualification_type_val');
    	 }
    	 if(order.sp_qualification_type.indexOf("air_transport") >= 0){
    		 order.airTransport = itemOrder.buildAirTransportDetail(); 
    		 order.airTransportItem = itemOrder.buildAirTransportItemDetail(); 
    	 }
    	 if(order.sp_qualification_type.indexOf("custom") >= 0){
    		 order.custom = itemOrder.buildCustomDetail(); 
    	 }
    	 if(order.sp_qualification_type.indexOf("delivery_express") >= 0){
    		 order.express_service = $('#express_service').val();
    		 order.express_remark = $('#express_remark').val();
    	 }
    	 if(order.sp_qualification_type.indexOf("pickup_van") >= 0){
    		 order.pickingCrane = itemOrder.buildPickupVanDetail(); 
    	 }
    	 if(order.sp_qualification_type.indexOf("cargo_insurance") >= 0){
    		 order.cargoInsurance = itemOrder.buildCargoInsuranceDetail(); 
    	 }
    	 
    	 $("#saveBtn").attr("disabled",true);
    	 $.post("/supplierContract/save",{params:JSON.stringify(order)},function(data){
    		if(data=='abbrError'){
    			$.scojs_message('供应商简称已存在', $.scojs_message.TYPE_ERROR);
    			$("#saveBtn").attr("disabled",false);
    		}else if (data=='companyError'){
    			$.scojs_message('公司名称已存在', $.scojs_message.TYPE_ERROR);
    			$("#saveBtn").attr("disabled",false);
    		}else if(data.ID != null && data.ID != ""){
     			eeda.contactUrl("edit?id",data.ID);
     			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
     			$("#partyId").val(data.ID);
     			$("#sp_id").val(data.ID);
     			$("#addChargeType").attr("disabled",false);
     			$("#saveBtn").attr("disabled",false);
     		}else{
     			$.scojs_message('数据有误', $.scojs_message.TYPE_ERROR);
     			$("#saveBtn").attr("disabled",false);
     		}
    		
    		if(order.sp_qualification_type.indexOf("ocean_shipment") >= 0){
    			itemOrder.refleshOceanCargoTable(data.ID); 
    			itemOrder.refleshOceanCargoItemTable(data.ID); 
	       	 }
	       	 if(order.sp_qualification_type.indexOf("internal_trade") >= 0){
	       		 itemOrder.refleshInternalTradeTable(data.ID); 
	       	 }
	       	 if(order.sp_qualification_type.indexOf("ocean_bulkCargo") >= 0){
	       		 itemOrder.refleshBulkCargoTable(data.ID); 
	       		 itemOrder.refleshBulkCargoItemTable(data.ID); 
	       	 }
	       	 if(order.sp_qualification_type.indexOf("land_transport") >= 0){ 
	       		 itemOrder.refleshLandTransportTable(data.ID);
	       		 itemOrder.refleshLandTransportItemTable(data.ID);
	       	 }
	       	 if(order.sp_qualification_type.indexOf("storage") >= 0){
	       		 itemOrder.refleshStorageTable(data.ID); 
	       	 }
	       	 if(order.sp_qualification_type.indexOf("air_transport") >= 0){
	       		 itemOrder.refleshAirTransportTable(data.ID);
	       		 itemOrder.refleshAirTransportItemTable(data.ID);
	       	 }
	       	 if(order.sp_qualification_type.indexOf("custom") >= 0){
	       		 itemOrder.refleshCustomTable(data.ID); 
	       	 }
	       	 if(order.sp_qualification_type.indexOf("pickup_van") >= 0){
	       		 itemOrder.refleshPickupVanTable(data.ID); 
	       	 }
	       	 if(order.sp_qualification_type.indexOf("cargo_insurance") >= 0){
	       		 itemOrder.refleshCargoInsuranceTable(data.ID); 
	       	 }
         }).fail(function() {
             $.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
             $('#saveBtn').attr('disabled', false);
             $.unblockUI();
         });
    	  
      });
     
	    
	    var showServiceTab=function(service){
	        switch (service){
	            case 'ocean_shipment':
	                $('#sp_ocean_shipment').show();
	               
	                break;
	            case 'internal_trade':
	                $('#sp_internal_trade').show();
	                
	                break;
	            case 'ocean_bulkCargo':
	                $('#sp_ocean_bulkCargo').show();
	               
	                break;
	            case 'land_transport':
	                $('#sp_land_transport').show();
	                
	                break;
	            case 'storage':
	                $('#sp_storage').show();
	               
	                break;
	            case 'air_transport':
	                $('#sp_air_transport').show();
	               
	                break;
	            case 'delivery_express':
	                $('#sp_delivery_express').show();
	               
	                break;
	            case 'custom':
	                $('#sp_custom').show();
	                
	                break;
	            case 'pickup_van':
	                $('#sp_pickup_van').show();
	                
	                break;
	            case 'cargo_insurance':
	                $('#sp_cargo_insurance').show();
	                
	                break;
	        }
	    };

	    var hideServiceTab=function(service){
	    	switch (service){
	    	   case 'ocean_shipment':
	                $('#sp_ocean_shipment').hide();
	               
	                break;
	            case 'internal_trade':
	                $('#sp_internal_trade').hide();
	                
	                break;
	            case 'ocean_bulkCargo':
	                $('#sp_ocean_bulkCargo').hide();
	               
	                break;
	            case 'land_transport':
	                $('#sp_land_transport').hide();
	                
	                break;
	            case 'storage':
	                $('#sp_storage').hide();
	                
	                break;
	            case 'air_transport':
	                $('#sp_air_transport').hide();
	                
	                break;
	            case 'delivery_express':
	                $('#sp_delivery_express').hide();
	                
	                break;
	            case 'custom':
	                $('#sp_custom').hide();
	                
	                break;
	            case 'pickup_van':
	                $('#sp_pickup_van').hide();
	                
	                break;
	            case 'cargo_insurance':
	                $('#sp_cargo_insurance').hide();
	                
	                break;
	        }
	    };

	    $('#sp_qualification_type_val input[type="checkbox"]').change(function(){
	        var checkValue=$(this).val();
	        if($(this).prop('checked')){
	            showServiceTab(checkValue);
	        }else{
	            hideServiceTab(checkValue);
	        }
	    });
	    

	    //构造checkBox回显数据
		var showQualification_type=function(value){
			var qualification_type = $('#'+value).val().split(",");
			for(var i=0;i < qualification_type.length;i++){
				$('#'+value+'_val input[type="checkbox"]').each(function(){
					var checkValue=$(this).val();
			        if(qualification_type[i]==checkValue){
			        	this.checked = true;
			        	
			        	if(value=='sp_qualification_type'){
			        		showServiceTab(checkValue);
			        	}
			        }
				});
			}
		}
		
		showQualification_type('sp_qualification_type');
		showQualification_type('ocean_qualification_type');
		showQualification_type('land_qualification_type');
		showQualification_type('trade_qualification_type');
		showQualification_type('bulk_qualification_type');
		showQualification_type('storage_qualification_type');

    });
});