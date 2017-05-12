define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco','app/wms/gateIn/upload', 'jq_blockui'], function ($, metisMenu) {
	$(document).ready(function() {
    	document.title = '入库记录 | '+document.title;

    	$("#breadcrumb_li").text('入库记录');
    	
    	$('#eeda-table').on('mouseover','.part_no',function(body){////body可以随便
   		 //var value = $($(this).find('.part_no')).text();
   		 var value = $(this).text();
   		 var tooltip = '<div id="c" style="position: absolute; z-index: 10;">'
   			 +'<img src="/images/product/'+value+'.jpg" height="200" width="300" onerror="javascript:this.src=\'/images/product/no_photo.jpg\'"/>'
   			 +'</div>';
            $("body").append(tooltip);
            $("#c").css({
                "top": body.pageY+'px', "left": body.pageX+'px'//这里body要和上面的一致
            }).show("fast");
       }).mouseout(function() {
           $("#c").remove();
       });

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/gateIn/list?error_flag=N",
            "drawCallback": function( settings ) {
		        $.unblockUI();
		    },
            columns:[
                { "data": "ITEM_NO", "class":"item_no", "width": "80px",
                    "render": function ( data, type, full, meta ) {
                        if(data){
                        	return "<a href='#'>"+data+"</a>";
                        }else{
                            return '';
                        }
                    }
                }, 
                {"data": "ITEM_NAME", "class":"item_name", "width": "280px"},
                { "data": "PART_NO", "class":"part_no", "width": "140px",
                    "render": function ( data, type, full, meta ) {
                        if(data){
                         return "<a href='#'>"+data+"</a>";
                        }else{
                            return '';
                        }
                    }
                }, 
				{ "data": "PART_NAME", "class":"part_name", "width": "320px"}, 
				{ "data": "QUANTITY", "width": "50px"},
				{ "data": "SHELVES", "width": "80px"},
				{ "data": "RETURN_FLAG", "width": "80px"},
				{ "data": "MOVE_FLAG", "width": "80px"}, 
				{ "data": "CREATE_TIME", "width": "180px"},
				{ "data": "CREATOR_NAME", "width": "80px"},
                { "data": "QR_CODE", "width": "580px"}, 
                { "width": "30px", visible: false,
                    "render": function ( data, type, full, meta ) {
                      return '<button type="button" class="btn table_btn delete_btn btn-xs" disabled>'+
                        '<i class="fa fa-trash-o"></i> 删除</button>';
                    }
                }
            ]
        });
        
        
        
        $('#eeda-table').on('click','.item_no',function(){
        	var value = $(this).text();
        	var name = $(this).parent().find('.item_name').text();
        	$('#item_no').val(value);
        	$('#part_no').val("");
        	
        	$('#orderText').text("产品编码："+value);
        	$('#partText').text("产品名称："+name);
        	showText();
        	searchData();
        });
        
        $('#eeda-table').on('click','.part_no',function(){
        	var value = $(this).text();
        	var item_no = $(this).parent().find('.item_no').text();
        	var name = $(this).parent().find('.part_name').text();
        	$('#part_no').val(value);
        	$('#item_no').val("");
        	$('#orderText').text("组件编码："+value);
        	$('#partText').text("组件名称："+name);
        	showText();
        	searchData();
        });
        
        var showText = function(){
        	var item_no = $('#item_no').val();
        	var item_name = $('#item_name').val();
        	var part_no = $('#part_no').val();
        	var part_name = $('#part_name').val();
        	var table = $('#eeda-table').dataTable();
        	if(part_no.trim()!='' || part_name.trim()!=''){
              	table.fnSetColumnVis(0, false);
              	table.fnSetColumnVis(1, false);
              	table.fnSetColumnVis(2, false);
              	table.fnSetColumnVis(3, false);
              	$('.itemShow').show();
        	}else if(item_no.trim()!='' || item_name.trim()!='') {
              	table.fnSetColumnVis(0, false);
              	table.fnSetColumnVis(1, false);
              	$('.itemShow').show();
        	}else{
        		table.fnSetColumnVis(0, true);
              	table.fnSetColumnVis(1, true);
              	table.fnSetColumnVis(2, true);
              	table.fnSetColumnVis(3, true);
              	$('.itemShow').hide();
              	$('#orderText').text("");
        	} 
        }
        
        
        $('#searchBtn').click(function(){
        	searchData(); 
        })
 
        
        var errorTable = eeda.dt({
            id: 'error-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/gateIn/list?error_flag=Y",
            "drawCallback": function( settings ) {
		        $.unblockUI();
		    },
            columns:[
                
				{ "data": "ERROR_MSG", "width": "220px",
					"render": function ( data, type, full, meta ) {
              	  		return "<span style='color:red;'>"+data+"</span>";
              	  	}	
				}, 
                {"data": "ITEM_NO", "width": "80px", 
              	  "render": function ( data, type, full, meta ) {
              		  return "<a href='#'>"+data+"</a>";
              	  }
                },
                {"data": "ITEM_NAME", "width": "280px"},
                { "data": "PART_NO", "width": "140px"}, 
				{ "data": "PART_NAME", "width": "320px"}, 
				{ "data": "QUANTITY", "width": "50px"},
				{ "data": "SHELVES", "width": "80px"},
				{ "data": "RETURN_FLAG", "width": "80px"},
				{ "data": "MOVE_FLAG", "width": "80px"}, 
				{ "data": "CREATE_TIME", "width": "120px"},
				{ "data": "CREATOR_NAME", "width": "80px"},
                { "data": "QR_CODE", "width": "480px"}, 
                { "width": "30px", visible: false,
                    "render": function ( data, type, full, meta ) {
                      return '<button type="button" class="btn table_btn delete_btn btn-xs" disabled>'+
                        '<i class="fa fa-trash-o"></i> 删除</button>';
                    }
                },
            ]
        });
        
        var invTable = eeda.dt({
            id: 'inv-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/gateIn/list?inv_flag=Y",
            "drawCallback": function( settings ) {
		        $.unblockUI();
		    },
            columns:[
				{ "data": "INV_MSG", "width": "280px", 
					"render": function ( data, type, full, meta ) {
              	  		return "<span style='color:red;'>"+data+"</span>";
              	  	}	
				}, 
                {"data": "ITEM_NO", "width": "80px", 
              	  "render": function ( data, type, full, meta ) {
              		  return "<a href='#'>"+data+"</a>";
              	  }
                },
                { "data": "ITEM_NAME", "width": "280px"}, 
                { "data": "PART_NO", "width": "120px"}, 
				{ "data": "PART_NAME", "width": "320px"}, 
				{ "data": "QUANTITY", "width": "50px"},
				{ "data": "SHELVES", "width": "80px"},
				{ "data": "RETURN_FLAG", "width": "80px"},
				{ "data": "MOVE_FLAG", "width": "80px"}, 
				{ "data": "CREATE_TIME", "width": "120px"},
				{ "data": "CREATOR_NAME", "width": "80px"},
                { "data": "QR_CODE", "width": "480px"}, 
                { "width": "30px", visible: false,
                    "render": function ( data, type, full, meta ) {
                      return '<button type="button" class="btn table_btn delete_btn btn-xs" disabled>'+
                        '<i class="fa fa-trash-o"></i> 删除</button>';
                    }
                },
            ]
        });
      
        $('#resetBtn').click(function(e){
        	$("#orderForm")[0].reset();
        	showText();
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
        	var itemJson = buildCondition();
        	var url = "/gateIn/list?error_flag=N&jsonStr="+JSON.stringify(itemJson);
        	dataTable.ajax.url(url).load();
        };
        
        order.refleshGateInTable = function(){
        	dataTable.ajax.url("/gateIn/list?error_flag=N").load();
        	errorTable.ajax.url("/gateIn/list?error_flag=Y").load();
        	invTable.ajax.url("/gateIn/list?inv_flag=Y").load();
        }
	});
});