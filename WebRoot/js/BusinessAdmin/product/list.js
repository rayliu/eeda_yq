define(['jquery', 'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
      var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: false,
          serverSide: true, 
          ajax: "/BusinessAdmin/product/list",
          columns: [
            { "data": "COVER" ,"width": "130px",
              render: function(data,type,full,meta){
                var str = "";
                if(!data){
                	data = 'nophoto.png';
                }
                str ="<a href='/BusinessAdmin/product/edit?id="+full.ID+"'>" +
                  		"<img style='width:120px; height: 90px;' src='/upload/"+data+"' </a>";
                return str;
              }
            },
            { "data": "NAME", "width": "200px",
              render:function(data,type,full,meta){
                   return "<a href='/BusinessAdmin/product/edit?id="+full.ID+"'>"+data+"</a>";
              }
            },
            { "data": "PRICE","width": "60px"},
            { "data": "IS_ACTIVE","width": "150px",
              render:function(data,type,full,meta){
                if(data && data == 'N'){
                  return "未上架 <a class='stdbtn btn_yellow setActive' flag='Y'>上架</a>";
                }else{
                  return "已上架 <a class='stdbtn btn_yellow setActive' flag='N'>下架</a>";
                }
              }
            },
            { "data": "CU_FLAG", "width": "100px",
              render:function(data,type,full,meta){
                if(data == 'Y'){
                	return "<input type='checkbox' class = 'cu'  data-id="+full.ID+" checked>";
                }else{
                	return "<input type='checkbox' class = 'cu' data-id="+full.ID+">";
                }
              }
            },
            { "data": "SEQ", "width": "60px"},
            { "data": null,
            	"render":function(data,type,full,meta){
	            	   var str = '<input class="stdbtn btn_red delete" type="button" value="删除" >';

	            	   return str;
	             }
            }
          ]
	    });
      
      $('#eeda_table').on('click','.setActive',function(){
    	  var id = $(this).parent().parent().attr('id');
    	  var flag = $(this).attr("flag");
    	  $.post('/BusinessAdmin/product/setActive',{id:id,flag:flag},function(data){
    		  if(data){
    			  if(flag=='Y'){
    				  $.scojs_message('上架成功', $.scojs_message.TYPE_OK);
    			  }else{
    				  $.scojs_message('下架成功', $.scojs_message.TYPE_OK);
    			  }
    			  refleshTable();
 			  }else{
 				 $.scojs_message('操作失败', $.scojs_message.TYPE_ERROR); 
    		  }
    	  });
      });
      
      $("#update_hui").change(function(){
    	  var self = $(this);
    	  var flag = "";
    		  if(self.prop("checked")){
        		  flag = "Y"
        	  }else {
        		  flag = "N"
        	  }
    		  $.post("/BusinessAdmin/product/openHui",{flag:flag},function(data){
	    			   if(data){
	    				  $.scojs_message('操作成功', $.scojs_message.TYPE_OK);
	    				  refleshTable();
	 			  }else{
	 				  	$.scojs_message('操作失败', $.scojs_message.TYPE_ERROR); 
	    		  }
    		  })
      })
      
      
      $('#eeda_table').on('change',".cu",function(){
    	  var self = $(this);
    	  var flag = "";
    	  var id = self.data("id");
    		  if(self.prop("checked")){
    			  flag = "Y"
    		  }else {
    			  flag = "N";
    		  }
    		  $.post("/BusinessAdmin/product/updateProduct",{id:id,flag:flag},function(data){
    			   if(data){
    			  $.scojs_message('更新成功', $.scojs_message.TYPE_OK);
    			  refleshTable();
 			  }else{
 				 $.scojs_message('更新失败', $.scojs_message.TYPE_ERROR); 
    		  }
    		  })
    	  
      })
      
      $('#eeda_table').on('click','.delete',function(){
    	  var id = $(this).parent().parent().attr('id');
    	  $.post('/BusinessAdmin/product/deleteProduct',{id:id},function(data){
    		  if(data){
    			  $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
    			  refleshTable();
 			  }else{
 				 $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR); 
    		  }
    	  });
      });
      
      
      var refleshTable = function(){
    	  dataTable.ajax.url("/BusinessAdmin/product/list").load();
      }
  });
});