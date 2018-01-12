define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/tao_manage/case/list?shop_id="+$('#shop_id').val(),
            columns:[
                {	"data":"PRODUCTOR",
                	"width": "80px"
                },
                {	"data": "NAME", "width":"120px"},
	              { "data": "CREATE_TIME", "width":"90px"}, 
	              { "data": "PICTURE_NAME", "width":"90px",
	            	"render":function(data,type,full,meta){
	            		var img ="<img src='/upload/"+data+"' class='shadow' style='width:120px;height:90px;'/>";
	            		return img;
	            	}  
	              }, 
	              { "data": "ID", "width":"60px",
	            	"render":function(data,type,full,meta){
	            		var flag='';
	            		if(full.FLAG == 1){
	            			flag = 'checked';
	            		}
	            		
	            		
	            		data ="<button  class='delete-btn  delete'  data-id="+data+" >删除</button>" 
       							+"&nbsp&nbsp&nbsp<button class = 'modifibtn btn-blue editBtn' data-id="+data+">编辑</button>"
       							+"&nbsp&nbsp&nbsp&nbsp精选<input class='check' style='width:20px' type='checkBox' name='checkbox' data-id='"+data+"' "+flag+">";
	            		return data;
	            	}  
	              }
            ]
        });
        
        $("#eeda_table").on("change"," .check",function(){
        	var self = $(this);
        	self.attr('disabled',true);
        	var flag = '';
        	var id = self.data('id');
        	if(self.is(":checked")){
        		flag = 1;
        	}else {
        		flag = 0;
        	}
        	$.post("/WebAdmin/tao_manage/case/updateFlag",{id:id,flag:flag},function(data){
        		if(data){
        			$.scojs_message("精选成功",$.scojs_message.TYPE_OK);
        			self.attr('disabled',false);
        		}else{
        			$.scojs_message("精选失败",$.scojs_message.TYPE_ERROR);
        		}
        	})
        })

        
        //进入编辑页面
        $("#eeda_table").on("click"," .editBtn",function(){
        	var id = $(this).data('id');
        	window.location.href = "/WebAdmin/tao_manage/case/modify?id="+id;
        });
      
        $("#eeda_table").on("click"," .delete",function(){
        	var result = confirm("确定要这样做吗？");
        	var self = $(this);
        	var id = self.data('id');
        	if(result){
        		$.post("/WebAdmin/tao_manage/case/delete",{id:id},function(data){
            		if(data){
            				$.scojs_message("删除成功",$.scojs_message.TYPE_OK);
            				refleshTable();
            		}else{
            			$.scojs.message("操作失败",$.scojs_message.TYPE_ERROR);
            		}
            	})
        	}
        });
        
   
     var searchData=function(){
          var creator = $.trim($("#creator").val()); 
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();

          var url = "/msgBoard/list?create_name_like="+creator
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
      };
      
      var refleshTable = function(){
     	 dataTable.ajax.url("/WebAdmin/tao_manage/case/list").load();
     }
    	
});
});