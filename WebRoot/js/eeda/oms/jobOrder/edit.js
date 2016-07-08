define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 

  $(document).ready(function() {
    	$('#menu_order').addClass('active').find('ul').addClass('in');

        var dataTable = eeda.dt({
          id: 'cargo_table',
          ajax: "/jobOrder/cargoItems",
          columns:[
              { "data": "COMPANY_NAME","width": "15%",
                  "render": function ( data, type, full, meta ) {
                      return "<a href='/serviceProvider/edit?id="+full.ID+"' target='_blank'>" + data+ "</a>";
                  }
              },
              { "data": "ABBR", "width": "10%"}, 
              { "data": "SP_TYPE", "width": "15%" }, 
              { "data": "CONTACT_PERSON"}, 
              { "data": "PHONE"}, 
              { "data": "ADDRESS", "width": "15%"},
              { "data": "RECEIPT"},
              { "data": "PAYMENT"},
              { "data":null,
                  "render": function(data, type, full, meta) {
                       if(full.DNAME == null){
                           return full.NAME;
                       }else{
                           return full.DNAME;
                       }
                   }
              },
              { 
                  "data": null, 
                  //"width": "8%",
                  "render": function(data, type, full, meta) {
                       var str ="<nobr>";
                       
                        str += "<a class='btn  btn-primary btn-sm' href='/serviceProvider/edit?id="+full.ID+"' target='_blank'>"+
                              "<i class='fa fa-edit fa-fw'></i>"+
                              "编辑"+"</a> ";
                       
                      
                          if(full.IS_STOP != true){
                              str += "<a class='btn btn-danger btn-sm' href='/serviceProvider/delete/"+full.ID+"'>"+
                                   "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                   "停用"+
                                   "</a>";
                           }else{
                              str +="<a class='btn btn-success btn-sm' href='/serviceProvider/delete/"+full.ID+"'>"+
                                       "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                       "启用"+
                                   "</a>";
                           }
                       
                       return str +="</nobr>";
                  }
              }
          ]
      });

      var chargeTable = eeda.dt({
          id: 'charge_table',
          ajax: "/jobOrder/chargeItems",
          columns:[
              { "data": "COMPANY_NAME","width": "15%",
                  "render": function ( data, type, full, meta ) {
                      return "<a href='/serviceProvider/edit?id="+full.ID+"' target='_blank'>" + data+ "</a>";
                  }
              },
              { "data": "ABBR", "width": "10%"}, 
              { "data": "SP_TYPE", "width": "15%" }, 
              { "data": "CONTACT_PERSON"}, 
              { "data": "PHONE"}, 
              { "data": "ADDRESS", "width": "15%"},
              { "data": "RECEIPT"},
              { "data": "PAYMENT"},
              { "data":null,
                  "render": function(data, type, full, meta) {
                       if(full.DNAME == null){
                           return full.NAME;
                       }else{
                           return full.DNAME;
                       }
                   }
              },
              { 
                  "data": null, 
                  //"width": "8%",
                  "render": function(data, type, full, meta) {
                       var str ="<nobr>";
                       
                        str += "<a class='btn  btn-primary btn-sm' href='/serviceProvider/edit?id="+full.ID+"' target='_blank'>"+
                              "<i class='fa fa-edit fa-fw'></i>"+
                              "编辑"+"</a> ";
                       
                      
                          if(full.IS_STOP != true){
                              str += "<a class='btn btn-danger btn-sm' href='/serviceProvider/delete/"+full.ID+"'>"+
                                   "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                   "停用"+
                                   "</a>";
                           }else{
                              str +="<a class='btn btn-success btn-sm' href='/serviceProvider/delete/"+full.ID+"'>"+
                                       "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                       "启用"+
                                   "</a>";
                           }
                       
                       return str +="</nobr>";
                  }
              }
          ]
      });

      var departTable = eeda.dt({
          id: 'depart_table',
          ajax: "/jobOrder/chargeItems",
          columns:[
              { "data": "COMPANY_NAME","width": "15%",
                  "render": function ( data, type, full, meta ) {
                      return "<a href='/serviceProvider/edit?id="+full.ID+"' target='_blank'>" + data+ "</a>";
                  }
              },
              { "data": "ABBR", "width": "10%"}, 
              { "data": "SP_TYPE", "width": "15%" }, 
              { "data": "CONTACT_PERSON"}, 
              { "data": "PHONE"}, 
              { "data": "ADDRESS", "width": "15%"},
              { "data": "RECEIPT"},
              { "data": "PAYMENT"},
              { "data":null,
                  "render": function(data, type, full, meta) {
                       if(full.DNAME == null){
                           return full.NAME;
                       }else{
                           return full.DNAME;
                       }
                   }
              },
              { 
                  "data": null, 
                  //"width": "8%",
                  "render": function(data, type, full, meta) {
                       var str ="<nobr>";
                       
                        str += "<a class='btn  btn-primary btn-sm' href='/serviceProvider/edit?id="+full.ID+"' target='_blank'>"+
                              "<i class='fa fa-edit fa-fw'></i>"+
                              "编辑"+"</a> ";
                       
                      
                          if(full.IS_STOP != true){
                              str += "<a class='btn btn-danger btn-sm' href='/serviceProvider/delete/"+full.ID+"'>"+
                                   "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                   "停用"+
                                   "</a>";
                           }else{
                              str +="<a class='btn btn-success btn-sm' href='/serviceProvider/delete/"+full.ID+"'>"+
                                       "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                       "启用"+
                                   "</a>";
                           }
                       
                       return str +="</nobr>";
                  }
              }
          ]
      });

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
        
         
      // 回显计费方式
      var chargeTypeOption=$("#chargeType>option");
      var chargeTypeVal=$("#chargeTypeSelect").val();
      for(var i=0;i<chargeTypeOption.length;i++){
         var svalue=chargeTypeOption[i].value;
         if(chargeTypeVal==svalue){
      	   $("#chargeType option[value='"+svalue+"']").attr("selected","selected");
         }
      }
      //自动提交改为手动提交
      $("#save").click(function(){
    	  $("#save").attr("disabled",true);
    	  
    	  /*$.post("/serviceProvider/check",$("#customerForm").serialize(),function(data){
    		  
    	  });*/
    	 if(!$("#customerForm").valid()){
    		  return false;
    	 }
    	 $.post("/serviceProvider/save", $("#customerForm").serialize(),function(data){
    		if(data=='abbrError'){
    			$.scojs_message('供应商简称已存在', $.scojs_message.TYPE_ERROR);
    			$("#save").attr("disabled",false);
    		}else if (data=='companyError'){
    			$.scojs_message('公司名称已存在', $.scojs_message.TYPE_ERROR);
    			$("#save").attr("disabled",false);
    		}else if(data.ID != null && data.ID != ""){
     			eeda.contactUrl("edit?id",data.ID);
     			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
     			$("#partyId").val(data.ID);
     			$("#sp_id").val(data.ID);
     			$("#addChargeType").attr("disabled",false);
     			$("#save").attr("disabled",false);
     		}else{
     			$.scojs_message('数据有误', $.scojs_message.TYPE_ERROR);
     			$("#save").attr("disabled",false);
     		}
         });
    	  
      });
      
	  
	    
    });
});