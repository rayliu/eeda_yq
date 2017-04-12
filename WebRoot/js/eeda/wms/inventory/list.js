define(['jquery', 'metisMenu', 'sb_admin','dataTables',  'dataTablesBootstrap', 'validate_cn', 'sco','./item_list'], function ($, metisMenu) {
	$(document).ready(function() {
    	document.title = '库存统计 | '+document.title;

    	$("#breadcrumb_li").text('库存统计 ');

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/inventory/list",
            columns:[
                { "width": "30px",
                    "render": function ( data, type, full, meta ) {
                      return '<button type="button" class="btn table_btn delete_btn btn-xs" disabled>'+
                        '<i class="fa fa-trash-o"></i> 删除</button>';
                    }
                }, 
                {"data": "ITEM_NAME", 
              	    "render": function ( data, type, full, meta ) {
              	    	if(!data)
              	    		data = '';
              	    	return "<a class='item_detail' item_no='"+full.ITEM_NO+"' data-target='#itemDetail' data-toggle='modal'>"+data+"<i class='glyphicon glyphicon-th-list'></i></a>";
              	    }
                },
                { "data": "ITEM_NO"}, 
				{ "data": "TOTAL"}
            ]
        });
        
        
      
        $('#resetBtn').click(function(e){
        	$("#orderForm")[0].reset();
        });

        $('#searchBtn').click(function(){
        	searchData(); 
        })
 
        buildCondition=function(){
	      	var item = {};
	      	var orderForm = $('#orderForm input,select');
	      	for(var i = 0; i < orderForm.length; i++){
	      		var name = orderForm[i].id;
	          	var value =orderForm[i].value;
	          	if(name){
	          		if(value)
	          			value = value.trim();
	          		item[name] = value;
	          	}
	      	}
	        return item;
        };
      
        var searchData=function(showMsg){
        	var itemJson = buildCondition();
        	var url = "/inventory/list?jsonStr="+JSON.stringify(itemJson);
        	dataTable.ajax.url(url).load();
        };
        
        $('#showBtn').on('click',function(){
        	var showMsg = this.textContent;
        	if(showMsg=='显示明细'){
        		//执行明细信息
        		this.textContent='显示汇总';
        	}else{
        		//执行汇总信息
        		this.textContent='显示明细';
        	}
        	
        	searchData(showMsg);
        	
        });
	});
});