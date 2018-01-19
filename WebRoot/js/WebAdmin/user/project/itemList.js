
define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/user/project/itemList?order_id="+$("#order_id").val()+"&type="+$("#type").val(),
            columns: [
	                     { "data":"ID","width": "60px" },
	                     { "data": "ITEM_NAME", "width":"100px",
	                    	 "render":function(data,type,full,meta){
	                    		 return "<span name='item_name'>"+data+"</span>";
	                    	 }
	                     },
	                     { "data": "TYPE", "width":"80px",
	                    	 "render":function(data,type,full,meta){
	                    		 var str = '';
	                    		 if($("#type").val()=='byProject'){
	                    			 str = '根据项目';
	                    		 }else if($("#type").val()=='byTime'){
	                    			 str = '根据时间';
	                    		 }
	                    		 return str;
	                    	 }
	                     },
	                     { "data": "CTEATE_TIME", "width":"90px"}, 
	                     { "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 return '<button type="button" class="updateBtn modifibtn">修改</button>&nbsp<button class="delete-btn delete">删除</button>';
	                    	 }
	                     }
                     ]
        });

        $("#addItemBtn").on("click",function(){
        	showlayer($("#pop_loc"));
        });
        
        $("#eeda_table").on("click",".updateBtn",function(){
        	var id = $(this).parent().parent().attr("id");
        	var item_name = $(this).parent().parent().find("[name='item_name']").text();
        	$("#id").val(id);
        	$("#update_item_name").val(item_name);
        	showlayer($("#pop_update"));
        });

        function showlayer(pop_object){
	        var bh=$(window).height();//获取屏幕高度
	        var bw=$(window).width();//获取屏幕宽度
	        $("#layer").css({
	            height:bh,
	            width:bw,
	            display:"block"
	        });
	        pop_object.show();
	    }
        
        $("#layer").click(function(){
	    	hidelayer();
	    })
	    
	    function hidelayer(){
	    	$("#layer").hide();
	    	$(".Popup").hide();
	    }
        
        var refleshTable = function(){
        	dataTable.ajax.url("/WebAdmin/user/project/itemList?order_id="+$("#order_id").val()+"&type="+$("#type").val()).load();
        }
     
        $("#confirmAddBtn").click(function(){
        	var self = this;
        	var order_id = $("#order_id").val();
        	var item_name = $("#item_name").val();
        	var type = $("#type").val();
        	
        	if(item_name.trim() == ''){
        		$.scojs_message('内容不能为空', $.scojs_message.TYPE_ERROR);
        		return ;
        	}
        	
        	self.disabled = true;
        	$.post("/WebAdmin/user/project/addProjectItem",{order_id:order_id,item_name:item_name,type:type},function(data){
        		if(data.result){
        			$.scojs_message('添加成功', $.scojs_message.TYPE_OK);
        			$("#layer").click();
        			refleshTable();
        		}else{
        			$.scojs_message('添加失败', $.scojs_message.TYPE_ERROR);
        			self.disabled = false;
        		}
        	});
        });
        
        $("#confirmUpdateBtn").click(function(){
        	var self = this;
        	var id = $("#id").val();
        	var item_name = $("#update_item_name").val();
        	
        	if(item_name.trim() == ''){
        		$.scojs_message('内容不能为空', $.scojs_message.TYPE_ERROR);
        		return ;
        	}
        	
        	self.disabled = true;
        	$.post("/WebAdmin/user/project/updateProjectItem",{id:id,item_name:item_name},function(data){
        		if(data.resultNumber>0){
        			$.scojs_message('修改成功', $.scojs_message.TYPE_OK);
        			$("#layer").click();
        			refleshTable();
        		}else{
        			$.scojs_message('修改失败', $.scojs_message.TYPE_ERROR);
        			self.disabled = false;
        		}
        	});
        });
        
        $("#eeda_table").on("click",".delete",function(){
        	var self = this;
        	var id = $(this).parent().parent().attr("id");
        	var type = $("#type").val();
        	
        	self.disabled = true;
        	$.post("/WebAdmin/user/project/deleteProjectItem",{id:id,type:type},function(data){
        		if(data.resultNumber>0){
        			$.scojs_message('刪除成功', $.scojs_message.TYPE_OK);
        			refleshTable();
        		}else{
        			$.scojs_message('刪除失败', $.scojs_message.TYPE_ERROR);
        			self.disabled = false;
        		}
        	});
        });
        
        $("#returnBtn").on("click",function(){
        	location.href = "/WebAdmin/user/project";
        });
    });
});