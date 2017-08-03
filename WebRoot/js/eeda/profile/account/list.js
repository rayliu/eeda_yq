define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) { 
	
	var dataTable = eeda.dt({
        id: 'eeda-table',
        ajax: "/account/list",
        columns:[
            {  "data": "ACCOUNT_NAME", 
                "render": function ( data, type, full, meta ) {
                    if(Account.isUpdate){
                        return "<a  href='/account/edit?id="+full.ID+"' target='_blank'>" + data + "</a>";
                    }else{
                        return data;
                    }
                }
            },
            {"data":"TYPE",
            	"render": function ( data, type, full, meta ) {
                    var displayName='';
            	    if(data ==='REC')
            	    	displayName='收款';
            	    if(data ==='PAY')
            	    	displayName='付款';
            	    if(data ==='ALL')
            	    	displayName='收款付款';
                    return displayName;
                }
        	},
            {"data":"ACCOUNT_NO"},
            {"data":"BANK_NAME"},
            {"data":"CURRENCY"},
            {"data":"REMARK"},
            {
                "data": null,
                //"sWidth": "8%",
                //"bVisible":(Warehouser.isUpdate || Warehouser.isDel),
                "render": function ( data, type, full, meta )  {  
                    var str="<nobr>";
                    if(Account.isUpdate){
                        str += "<a class='btn  btn-primary btn-sm' href='/account/edit?id="+full.ID+"' target='_blank'>"+
                            "<i class='fa fa-edit fa-fw'></i>"+
                            "编辑"+
                            "</a> ";
                    }
                    if(Account.isDel){
                        if(!full.IS_STOP){
                            str += "<button class='delete btn btn-danger  btn-sm' >"+
                                    "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                    "停用"+
                                "</button>";
                        }else{
                            str += "<button class='delete btn btn-success  btn-sm' >"+
                                    "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                    "启用"+
                                "</button>";
                        }
                    }
                    str+="</nobr>";
                    return str;
                 }
              }
        ]
    });

    $('#resetBtn').click(function(e){
        $("#orderForm")[0].reset();
    });

    $('#searchBtn').click(function(){
        searchData(); 
    });
    //停用按钮控制
    $("#eeda-table").on('click', '.delete', function(e){
    	var selfId = $(this).parent().parent().parent().attr('id');
    	$.post('/account/delete',{selfId:selfId},function(data){
    		if(data){
	    			var url = "/account/list";
	                dataTable.ajax.url(url).load();
	                $.scojs_message('成功', $.scojs_message.TYPE_OK);
                }
    		},'json').fail(function() {
                $.scojs_message('失败', $.scojs_message.TYPE_ERROR);
        });
    });

    var searchData=function(){
        var name = $("#bank_name").val();
        /*  
          查询规则：参数对应DB字段名
          *_no like
          *_id =
          *_status =
          时间字段需成双定义  *_begin_time *_end_time   between
        */
        var url = "/account/list?bank_name="+name;

        dataTable.ajax.url(url).load();
    };
	
});