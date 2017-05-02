define(['jquery', 'metisMenu', 'sb_admin','dataTables',  'dataTablesBootstrap', 'validate_cn', 'sco','./item_list', 'jq_blockui'], function ($, metisMenu) {
	$(document).ready(function() {
    	document.title = '库存统计 | '+document.title;

    	$("#breadcrumb_li").text('库存统计 ');

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/inventory/list",
            "drawCallback": function( settings ) {
		        $.unblockUI();
		    },
            columns:[
                {"data": "ITEM_NO", 
              	    "render": function ( data, type, full, meta ) {
              	    	if(!data)
              	    		data = "<i class='glyphicon glyphicon-th-list'></i>";
              	    	return "<a class='partDetail' item_no='"+full.ITEM_NO+"' >"+data+"</a>";
              	    	//return "<a class='item_detail' item_no='"+full.ITEM_NO+"' data-target='#itemDetail' data-toggle='modal' style='cursor: pointer;'>"+data+"</a>";
              	    }
                },
                { "data": "ITEM_NAME"}, 
                { "data": "PART_NO","visible":false,
                	 "render": function ( data, type, full, meta ) {
               	    	if(!data)
               	    		data = "<i class='glyphicon glyphicon-th-list'></i>";
               	    	return "<a class='item_detail' part_no='"+full.PART_NO+"' data-target='#itemDetail' data-toggle='modal' style='cursor: pointer;'>"+data+"</a>";
               	    }
                }, 
                { "data": "PART_NAME","visible":false}, 
				{ "data": "TOTAL"}
            ]
        });
        
        
    	$("#eeda-table").on('click', '.partDetail', function(e){
          	var item_no = $(this).attr("item_no");

          	searchPartData(item_no);
        });
        
        
      
        $('#resetBtn').click(function(e){
        	$("#orderForm")[0].reset();
        });

        $('#searchBtn').click(function(){
        	var item_no = $('#item_no').val();
        	var item_name = $('#item_name').val();
        	var part_no = $('#part_no').val();
        	var part_name = $('#part_name').val();
        	if(part_no !='' || part_name!=''){
        		searchPartData(item_no); 
        	}else{
        		searchData(); 
        	}
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
      
        var searchData=function(){
        	$.blockUI({ 
                message: '<h1><img src="/images/loading.gif" style="height: 50px; margin-top: -3px;"/> LOADING...</h1>' 
            });
        	var table = $('#eeda-table').dataTable();
          	table.fnSetColumnVis(0, true);
          	table.fnSetColumnVis(1, true);
          	table.fnSetColumnVis(2, false);
          	table.fnSetColumnVis(3, false);
        	
        	var itemJson = buildCondition();
        	var url = "/inventory/list?jsonStr="+JSON.stringify(itemJson);
        	dataTable.ajax.url(url).load();
        };
        
        
        var searchPartData=function(item_no){
        	$.blockUI({ 
                message: '<h1><img src="/images/loading.gif" style="height: 50px; margin-top: -3px;"/> LOADING...</h1>' 
            });
        	
        	var table = $('#eeda-table').dataTable();
          	table.fnSetColumnVis(0, false);
          	table.fnSetColumnVis(1, false);
          	table.fnSetColumnVis(2, true);
          	table.fnSetColumnVis(3, true);
        	
        	var itemJson = buildCondition();
        	var url = "/inventory/partList?item_no="+item_no+"&jsonStr="+JSON.stringify(itemJson);
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