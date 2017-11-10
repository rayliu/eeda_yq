define(['jquery', 'metisMenu',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda_table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/WebAdmin/tao_manage/video/list",
            columns:[
                {	"data":"PRODUCTOR",
                	"width": "80px"
                },
                {	"data": "LOCATION", "width":"120px"},
	              { "data": "NAME", "width":"90px"}, 
	              { "data": "COVER", "width":"60px",
	            	  "render":function(data,type,full,meta){
	            		  var img="<img class='shadow' src='/upload/"+data+"' style='width:120px;height:90px'/>";
	            		  return img;
	            	  }
	              }, 
	              { "data": "VIDEO_URL", "width":"90px",
	            	  "render":function(data,type,full,meta){
	            		  var url = data;
	            		  if(data.indexOf('http')<0){
	            			  url = "<a href='http://"+data+"' target='_blank'>点击跳转至视频观看</a>"
	            		  }else{
	            			  url = "<a href='"+data+"' target='_blank'>点击跳转至视频观看</a>"
	            		  }
	            		  return url;
	            	  }
	              },
	              { "data": "ID", "width":"90px",
	            	"render":function(data,type,full,meta){
	            		data="<button  class='delete-btn  delete' data-id="+full.ID+" >删除</button>";
	            		return data
	            	}  
	              }
            ]
        });
      
        $("#eeda_table").on("click"," .delete",function(){
        	var result = confirm("确定要这样做吗？");
        	var self = $(this);
        	var id = self.data('id');
        	if(result){
        		$.post("/WebAdmin/tao_manage/video/delete",{id:id},function(data){
            		if(data){
            				$.scojs_message("删除成功",$.scojs_message.TYPE_OK);
            				refleshTable();
            		}else{
            			$.scojs.message("操作失败",$.scojs_message.TYPE_OK);
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
      
      $('#eeda_table').on('click','.edit',function(){
    	  var tr = $(this).parent().parent();
    	  $('#edit_id').val(tr.attr('id'));
    	  $('#edit_radioTitle').val($(this).text());
    	  $('#edit_radioContent').val($(tr.find(".content")).text());
    	  $('#editRadio').click();
      });
     
      var refleshTable = function(){
        	 dataTable.ajax.url("/WebAdmin/tao_manage/video/list").load();
        }
     
    	
});
});