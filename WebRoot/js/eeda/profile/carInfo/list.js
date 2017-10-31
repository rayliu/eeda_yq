define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) {
    $(document).ready(function() {

        var carTable = eeda.dt({
            id: 'car_table',
            autoWidth: false,
            paging:true,
            ajax: "/carInfo/list",
            columns:[
                { "data":"CAR_NO",
                    "render": function ( data, type, full, meta ) {
                        return "<a href='/carInfo/edit?id="+full.ID+"'target='_blank'>"+full.CAR_NO+"</a>";  
                    }
                },
                { "data":"CAR_OWNED","width":"55px"},
                { "data":"DRIVER"},
                { "data":"PHONE"},
                { "data":"CARTYPE"},
                { "data":"LENGTH"},
                { "data":null, 
                    "render": function ( data, type, full, meta ) {
                       // if(order.delPermission){
                    	if(data.IS_STOP=="1"){
                            return '<button type="button" class="btn btn-danger btn-xs delete" code="'+full.ID+'"" style="width:30px">启用</button>';   
                        }else{
                        	return '<button type="button" class="btn btn-danger btn-xs delete" code="'+full.ID+'"" style="width:30px">停用</button>';   
                        }
                    }
                }
            ]
        });

        $("#car_table").on('click', '.delete', function(){
            var id = $(this).attr('code');
            $.post('/carInfo/delect/'+id,function(data){
                //保存成功后，刷新列表
                console.log(data);
                if(data.success){
                	carTable.ajax.reload().draw()
                    $.scojs_message('停用成功', $.scojs_message.TYPE_OK);
                }else{
                	$.scojs_message('停用失败', $.scojs_message.TYPE_ERROR);
                }
            },'json');
        });
        
        $('#resetBtn').click(function(){
        	$('#query_car_no').val('');
        	$("#query_driver").val('');
        	$("#query_cartype").val('');
        });
        $('#searchBtn').click(function(){
            searchData(); 
        });

       var searchData=function(){
            var car_no = $('#query_car_no').val().trim();
            var driver = $("#query_driver").val().trim();
            var cartype = $("#query_cartype").val().trim();
            
            var url = "/carInfo/list?car_no="+car_no
            	   +"&driver_like="+driver
                 +"&cartype_equals="+cartype;

            carTable.ajax.url(url).load();
          }
        
       
    });
});