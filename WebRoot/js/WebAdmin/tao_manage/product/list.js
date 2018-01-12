define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
	$(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/tao_manage/product/list?shop_id="+$('#shop_id').val(),
            columns:[
                     {"data":"PRODUCTOR","width": "80px" },
                     {"data": "NAME", "width":"100px"},
                     {"data": "COVER", "width":"120px",
                    	 "render":function(data){
                    		 return "<img class='shadow' src='/upload/"+data+"' width='120' height='90'/>"
                    	 }
                     }, 
                     {"data": "PRICE", "width":"60px",
                    	 "render":function(data,full){
                    		 if(data == "-1"){
                    			 data = "面议"
                    		 }
                    		 return  data;
                    	 }
                     },
                     {"data": "IS_ACTIVE", "width":"60px",
                    	"render":function(data,full){
                    		if(data=="Y"){
                    			data="已上架";
                    		}else{
                    			data="未上架"
                    		}
                    		return data;
                    	}
                     },
                     {"data": "CREATE_STAMP", "width":"80px"},
                     {"data": "UPDATE_STAMP", "width":"80px"},
                     {"data": "ID", "width":"100px",
                    	 "render":function(data,type,full,meta){
                    			var status = "";
	     	            		var info = ""
	     	            		var button = "";
	     	            		if(full.IS_ACTIVE=="N"){
	     	            			button='modifibtn btn-red'
	     	            			status="toUp";
	     	            			info = '上架'; 
	     	            		}else if(full.IS_ACTIVE=='Y'){
	     	            			button='delete-btn'
	     	            			status='toDown'
	     	            			info = "下架";
	     	            		}
	     	            	data =  "<button class=' "+button+" wherether_carriage' " +
	     	              			" data-id="+full.ID+" href='#begin_date' status="+status+">"+info+"</button>"
	     	            			+"&nbsp&nbsp&nbsp<button class = 'modifibtn btn-red editBtn' data-id="+data+">编辑</button>" 
	     	            			+'<input class="delete-btn delete" type="button" value="删除" >';
                    		 return data;
                    	 }
                     }
                    ]
        });
        
        //删除 
        $('#eeda_table').on('click','.delete',function(){
      	  var id = $(this).parent().parent().attr('id');
      	  var result = confirm("您确定要删除这个商品吗？");
      	  if(result){
      		  $.post('/WebAdmin/tao_manage/product/deleteProduct',{id:id},function(data){
          		  if(data){
          			  $.scojs_message('删除成功', $.scojs_message.TYPE_OK);
          			  refleshTable();
       			  }else{
       				 $.scojs_message('删除失败', $.scojs_message.TYPE_ERROR); 
          		  }
          	  });
      	  }
        });
        
        //进入编辑页面
        $("#eeda_table").on("click"," .editBtn",function(){
        	var id = $(this).data('id');
        	window.location.href = "/WebAdmin/tao_manage/product/edit?id="+id;
        });
              //更新状态 
        $("#eeda_table").on("click"," .wherether_carriage",function(){
        	var self = $(this);
        	var id = self.data('id');
        	var status = self.attr("status");
    		$.post("/WebAdmin/tao_manage/product/whetherCarriage",{id:id,status:status},function(data){
        		if(data){
        			if(status == "toUp"){
        				$.scojs_message("上架成功",$.scojs_message.TYPE_OK);
        			}
        			if(status == "toDown"){
        				$.scojs_message("已下架",$.scojs_message.TYPE_OK);
        			}
        			refleshTable();
        		}else{
        			$.scojs.message("操作失败",$.scojs_message.TYPE_OK);
        		}
        	})
        });

        var refleshTable = function(){
       	 dataTable.ajax.url("/WebAdmin/tao_manage/product/list").load();
       }
    
      
      $('#eeda_table').on('click','.edit',function(){

      });
    	
});
});