define(['jquery', 'sco', 'dataTablesBootstrap' , 'validate_cn'], function ($, metisMenu) {
	$(document).ready(function() {
		
	    var deletedTableIds=[];

	    //删除一行
	    $("#loc_table").on('click', '.delete', function(e){
	        e.preventDefault();
	        var tr = $(this).parent().parent();
	        deletedTableIds.push(tr.attr('id'));
	        
	        locTable.row(tr).remove().draw();
	    }); 
	   
	    var loc_table = eeda.dt({
          id: 'loc_table',
          paging: false,
          serverSide: false, 
          ajax: "/WebAdmin/listLocation",
          columns: [
	            { "data": "NAME","width":"100px" },
	            { "data": "ID",
	            	render: function(data,type,full,meta){
	            		return "<button class='modifibtn delete_loc' data-id='"+full.ID+"' >删除</button>";
	            	}
	            }
          ]
		});
	    
	    var category_table = eeda.dt({
	          id: 'category_table',
	          paging: false,
	          serverSide: false, 
	          ajax: "/WebAdmin/listCategory",
	          columns: [
		            { "data": "NAME","width":"100px" },
		            { "data": "ID",
		            	render: function(data,type,full,meta){
		            		return "<button class='modifibtn delete_cat' data-id='"+full.ID+"' >删除</button>";
		            	}
		            }
	          ]
			});
	    
	    $('#add_loc').on('click', function(){
	    	showlayer($("#pop_loc"));
	    });
	    
	    $("#add_category").on('click',function(){
	    	showlayer($("#pop_category"));
	    })
	    
	    $("#sure_add_loc").click(function(){
	    	hidelayer();
	    	var address = $("#address").val();
	        if(address == ''){
	        	return false;
	        }
	    	$.post("/WebAdmin/addLocation",{address:address},function(data){
	    		if(data){
	    			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	    			refleshTable(loc_table,"/WebAdmin/listLocation");
	    		}else{
	    			$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
	    		}
	    	})
	    })
	    
	    $("#sure_add_category").click(function(){
	    	hidelayer();
	    	var name = $("#category_name").val();
	        if(name == ''){
	        	return false;
	        }
	    	$.post("/WebAdmin/addCategory",{name:name},function(data){
	    		if(data){
	    			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	    			refleshTable(category_table,"/WebAdmin/listCategory");
	    		}else{
	    			$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
	    		}
	    	})
	    })
	    
	    $("#loc_table").on("click",".delete_loc",function(){
	    	var self = $(this);
	    	var id = self.data('id');
	    	var result = confirm("你確定要删除 ?");
	    	if(result){
	    		$.post("/WebAdmin/deleteLocation",{id:id},function(data){
	    			if(data){
		    			$.scojs_message('刪除成功', $.scojs_message.TYPE_OK);
		    			refleshTable(loc_table,"/WebAdmin/listLocation");
		    		}else{
		    			$.scojs_message('刪除失败', $.scojs_message.TYPE_ERROR);
		    		}
	    		})
	    	}
	    	
	    })
	    
	      $("#category_table").on("click",".delete_cat",function(){
	    	var self = $(this);
	    	var id = self.data('id');
	    	var result = confirm("你確定要删除 ?");
	    	if(result){
	    		$.post("/WebAdmin/deleteCategory",{id:id},function(data){
	    			if(data){
		    			$.scojs_message('刪除成功', $.scojs_message.TYPE_OK);
		    			refleshTable(category_table,"/WebAdmin/listCategory");
		    		}else{
		    			$.scojs_message('刪除失败', $.scojs_message.TYPE_ERROR);
		    		}
	    		})
	    	}
	    	
	    })
		
		 var refleshTable = function(table,url){
	    	  table.ajax.url(url).load();
	     }
	  

	    $("#layer").click(function(){
	    	hidelayer();
	    })
	    
	    function hidelayer(){
	    	$("#layer").hide();
	    	$(".pop").hide();
	    }
	    
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
	});
});