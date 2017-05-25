define(['jquery', 'metisMenu', 'sb_admin','dataTables',  'dataTablesBootstrap', 'validate_cn', 'sco','./item_list', 'jq_blockui'], function ($, metisMenu) {
	$(document).ready(function() {
    	document.title = '库存统计 | '+document.title;
    	
    	$('#eeda-table').on('mouseover','.part_no',function(body){////body可以随便

   		 var value = $(this).text();
   		 var tooltip = '<div id="c" style="position: absolute; z-index: 10; box-shadow:0 0 20px #4F4F4F;">'
   			 +'<img src="/images/product/'+value+'.jpg" height="200" width="300" onerror="javascript:this.src=\'/images/product/no_photo.jpg\'"/>'
   			 +'</div>';
            $("body").append(tooltip);
            $("#c").css({
                "top": body.pageY+'px', "left": body.pageX+'px'//这里body要和上面的一致
            }).show("fast");
       }).mouseout(function() {
           $("#c").remove();
       });

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
                {"data": "ITEM_NO","class":"item_no",
              	    "render": function ( data, type, full, meta ) {
              	    	if(!data){
              	    		data = "<i class='glyphicon glyphicon-th-list itemDetail'></i>";
              	    	}
              	    	return "<a class='itemDetail' item_no='"+full.ITEM_NO+"' style='cursor: pointer;'>"+data+"</a>";
              	    }
                },
                { "data": "ITEM_NAME","class":"item_name"}
            ]
        });
        
        
        var partTable = eeda.dt({
            id: 'part-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/inventory/list",
            "drawCallback": function( settings ) {
		        $.unblockUI();
		    },
            columns:[
                { "data": "PART_NO","class":"part_no",
                	 "render": function ( data, type, full, meta ) {
               	    	if(!data)
               	    		data = "<i class='glyphicon glyphicon-th-list'></i>";
               	    	return "<a class='partDetail' part_no='"+full.PART_NO+"' data-target='#partDetail' data-toggle='modal' style='cursor: pointer;'>"+data+"</a>";
               	    }
                }, 
                { "data": "USEFOR","class":"item_nos"}, 
                { "data": "PART_NAME","class":"part_name"}, 
				{ "data": "TOTALBOX","class":"totalBox"},
                { "data": "TOTALPIECE","class":"totalPiece","visible":false}
            ]
        });
        
        
    	$("#eeda-table").on('click', '.itemDetail', function(e){
          	var value = $(this).attr("item_no");
          	var name = $($(this).parent().parent()).find('.item_name').text();
          	
          	$('#eedaTable').hide();
          	$('#partTable').show();
          	if(item_no){
          		$('.itemShow').show();
          		$('#orderText').text("产品编码："+value);
            	$('#partText').text("产品名称："+name);
          	}
          	searchPartData(value);
        });
        
        
      
        $('#resetBtn').click(function(e){
        	$("#orderForm")[0].reset();
            $('.itemShow').hide();
            searchData();
        });

        $('#searchBtn').click(function(e){
    		searchData(); 	
        });
 
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
        	$('.itemShow').hide();
            $('#orderText').text("");
            $('#eedaTable').show();
          	$('#partTable').hide();
        	
        	var itemJson = buildCondition();
        	var url = "/inventory/list?jsonStr="+JSON.stringify(itemJson);
        	dataTable.ajax.url(url).load();
        };
        
        var searchPartData=function(item_no){
        	$.blockUI({ 
                message: '<h1><img src="/images/loading.gif" style="height: 50px; margin-top: -3px;"/> LOADING...</h1>' 
            });

        	var itemJson = buildCondition();
        	var url = "/inventory/partList?item_no="+item_no+"&jsonStr="+JSON.stringify(itemJson);
        	partTable.ajax.url(url).load();
        };

	});
});