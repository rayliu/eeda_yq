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
	   
	    var locTable = eeda.dt({
          id: 'loc_table',
          paging: false,
          serverSide: false, 
          ajax: "/BusinessAdmin/ad/cu/list",
          columns: [
	            { "data": "ORDER_NO","width":"100px" },
	            { "data": null,
	            	render: function(data,type,full,meta){
	            		return "<button class='modifibtn delete' id='"+full.ID+"' >删除</button>";
	            	}
	            }
          ]
		});
	    
	    var category_table = eeda.dt({
	          id: 'category_table',
	          paging: false,
	          serverSide: false, 
	          ajax: "/BusinessAdmin/ad/cu/list_category",
	          columns: [
		            { "data": "NAME","width":"100px" },
		            { "data": "PARENT_ID","width":"100px" },
		            { "data": "CUSTOMER_ID","width":"100px" },
		            { "data": null,
		            	render: function(data,type,full,meta){
		            		return "<button class='modifibtn delete' id='"+full.ID+"' >删除</button>";
		            	}
		            }
	          ]
			});
	    
	    $('#add_loc').on('click', function(){
	        var item={};
	        locTable.row.add(item).draw(true);
	    });
		
		 var refleshTable = function(){
	    	  dataTable.ajax.url("/BusinessAdmin/ad/cu/list").load();
	     }
    	
	  
	    $('#save_btn').click(function(event) {
	    	var self = this;
	    	$(self).attr('disabled',true);
    	  
	    	var order = {};
	    	order.id = $('#order_id').val();
	    	order.name = $('#name').val();
	    	order.category = $('#category').val();
	    	order.price_type = $('[name=price_type]:checked').val();
	    	if(order.price_type=='人民币'){
	    		order.price = $('#price').val();
	    	}else{
	    		order.price = 0.00;
	    	}
	    	order.unit = $('[name=unit]:checked').val();
	    	order.content = $('#content').val();
	    	order.cover = $('#img_cover').val();
	    	order.photo1 = $('#img_photo1').val();
	    	order.photo2 = $('#img_photo2').val();
	    	order.photo3 = $('#img_photo3').val();
	    	order.photo4 = $('#img_photo4').val();
	    	order.photo5 = $('#img_photo5').val();
	    	order.photo6 = $('#img_photo6').val();
	    	order.photo7 = $('#img_photo7').val();
	    	order.photo8 = $('#img_photo8').val();
	    	order.photo9 = $('#img_photo9').val();
	    	order.photo10 = $('#img_photo10').val();
	    	
 		  
	    	$.post('/BusinessAdmin/product/save',{jsonStr:JSON.stringify(order)}, function(data, textStatus, xhr) {
	    		if(data){
	    			eeda.refreshUrl('edit?id='+data.ID)
	    			$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
	    		}else{
	    			$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
	    		}
	    		$(self).attr('disabled',false);
	    	});
	    });
	});
});