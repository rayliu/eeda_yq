define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/user/project/list",
            columns: [
	                     { "data":"INDEX","width": "60px" },
	                     { "data": "PROJECT", "width":"100px"},
	                     { "data": "TYPE", "width":"80px",
	                    	 "render":function(data,type,full,meta){
	                    		 var str = '';
	                    		 if(data=='byProject'){
	                    			 str = '根据项目';
	                    		 }else if(data=='byTime'){
	                    			 str = '根据时间';
	                    		 }
	                    		 return "<span type='"+data+"' name='type'>"+str+"</span>";
	                    	 }
	                     },
	                     { "data": "CTEATE_TIME", "width":"90px"}, 
	                     { "width":"60px",
	                    	 "render":function(data,type,full,meta){
	                    		 return "<a href='/WebAdmin/user/project/projectItem?order_id="+full.ID+"&&type="+full.TYPE+"'><button type='button' class='itemBtn modifibtn'>查看明细</button></a>&nbsp<button class='delete-btn delete' >删除</button>";
	                    	 }
	                     }
                     ]
        });

        var refleshTable = function(){
        	dataTable.ajax.url("/WebAdmin/user/project/list").load();
        }
     
        $("#addBtn").on("click",function(){
        	var bh=$(window).height();//获取屏幕高度
	        var bw=$(window).width();//获取屏幕宽度
	        $("#layer").css({
	            height:bh,
	            width:bw,
	            display:"block"
	        });
	        $("#pop_loc").show();
        });
        
        $("#layer").click(function(){
	    	hidelayer();
	    })
	    
	    function hidelayer(){
	    	$("#layer").hide();
	    	$(".Popup").hide();
	    }
        
        $("#confirmBtn").click(function(){
        	var self = this;
        	var project = $("#project").val();
        	var type = $("#type").val();
        	
        	if(project.trim() == ''){
        		$.scojs_message('内容不能为空', $.scojs_message.TYPE_ERROR);
        		return ;
        	}
        	self.disabled = true;
        	$.post("/WebAdmin/user/project/addProject",{project:project,type:type},function(data){
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
        
        $("#eeda_table").on("click",".delete",function(){
        	var id = $(this).parent().parent().attr("id");
        	var type = $(this).parent().parent().find("[name='type']").attr("type");
        	$.post("/WebAdmin/user/project/deleteProject",{id:id,type:type},function(data){
        		if(data.result){
        			$.scojs_message('刪除成功', $.scojs_message.TYPE_OK);
        			refleshTable();
        		}else{
        			$.scojs_message('刪除失败', $.scojs_message.TYPE_ERROR);
        		}
        	});
        });
        
    });
});