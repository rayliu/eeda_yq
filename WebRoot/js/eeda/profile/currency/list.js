define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'sco'], function ($, metisMenu) { 
	document.title = '币制查询 | '+document.title;
	$('#menu_profile').addClass('active').find('ul').addClass('in');

	var dataTable = eeda.dt({
        id: 'eeda-table',
        ajax: "/currency/list",
        columns:[
            {  "data": "NAME", 
                "render": function ( data, type, full, meta ) {
                    if(Account.isUpdate){
                        return "<a  href='/currency/edit?id="+full.ID+"' target='_blank'>" + data + "</a>";
                    }else{
                        return data;
                    }
                }
            },
            {"data":"CODE"},
            {"data":"ENGLISH_NAME"},
            {"data":"CREATOR_NAME"},
            {"data":"CREATE_STAMP"},
            {"data":"REMARK"},
            {
                "data": null,
                "render": function ( data, type, full, meta )  {  
                    var str="<nobr>";
                    if(Account.isUpdate){
                        str += "<a class='btn  btn-primary btn-sm' href='/currency/edit?id="+full.ID+"' target='_blank'>"+
                            "<i class='fa fa-edit fa-fw'></i>"+
                            "编辑"+
                            "</a> ";
                    }
                    if(Account.isDel){
                        if(full.STATUS != "inactive"){
                            str += "<a class='btn btn-danger  btn-sm' "+
                                    "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                    "删除"+
                                "</a>";
                        }
                    }
                    str+="</nobr>";
                    return str;
                 }
              }
        ]
    });
	
	//删除功能
	$('#eeda-table').on('click','.btn-danger',function(){
		var id = $(this).parent().parent().parent().attr('id');
		$.post('/currency/delete',{id:id},function(data){
			if(data.ID){
				$.scojs_message('删除成功', $.scojs_message.TYPE_OK);
				searchData();
			}
		})
	})
	

    $('#resetBtn').click(function(e){
        $("#orderForm")[0].reset();
    });

    $('#searchBtn').click(function(){
        searchData(); 
    });

    var searchData=function(){
        var name = $("#name").val();
        /*  
          查询规则：参数对应DB字段名
          *_no like
          *_id =
          *_status =
          时间字段需成双定义  *_begin_time *_end_time   between
        */
        var url = "/currency/list?name="+name;

        dataTable.ajax.url(url).load();
    };
	
});