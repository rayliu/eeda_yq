define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) {
    $(document).ready(function() {
        document.title = '车辆列表 | '+document.title;
        
        var carTable = eeda.dt({
            id: 'car_table',
            autoWidth: false,
            ajax: "/carInfo/list",
            columns:[
                { "data":"CAR_NO",
                    "render": function ( data, type, full, meta ) {
                        return "<a href='/carInfo/edit/"+full.ID+"'target='_blank'>"+full.CAR_NO+"</a>";  
                    }
                },
                { "data":"CAR_OWNED","width":"55px"},
                { "data":"DRIVER"},
                { "data":"PHONE"},
                { "data":"CARTYPE"},
                { "data":"LENGTH"},
                { "data":null, 
                    "render": function ( data, type, full, meta ) {
                        if(order.delPermission){
                            return '<button type="button" class="btn btn-danger btn-xs delete" code="'+full.ID+'"" style="width:50px">停用</button>';   
                        }
                        return '';
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
                    carTable.draw();
                }else{
                    alert('停用失败');
                }
            },'json');
        });
        
       
    });
});