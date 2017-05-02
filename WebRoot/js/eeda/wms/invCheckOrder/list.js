define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'jq_blockui'], function ($, metisMenu) {
	$(document).ready(function() {
    	document.title = '盘点记录 | '+document.title;

    	$("#breadcrumb_li").text('盘点记录 ');

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/invCheckOrder/list",
            "drawCallback": function( settings ) {
		        $.unblockUI();
		    },
            columns:[
                
                { "data": "ORDER_NO"}, 
                { "data": "ITEM_NO","class":"item_no",
                    "render": function ( data, type, full, meta ) {
                        if(data){
                        	return "<a href='#'>"+data+"</a>";
                        }else{
                            return '';
                        }
                    }
                },  
                { "data": "ITEM_NAME"},
                { "data": "PART_NO","class":"part_no",
                    "render": function ( data, type, full, meta ) {
                        if(data){
                        	return "<a href='#'>"+data+"</a>";
                        }else{
                            return '';
                        }
                    }
                },  
				{ "data": "PART_NAME"}, 
				{ "data": "ACTRAL_AMOUNT"},
				{ "data": "CREATE_TIME"},
				{ "data": "CREATOR_NAME"},
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
        	$('#item_no').val(value);
        	$('#part_no').val("");
        	searchData();
        });
        
        $('#eeda-table').on('click','.part_no',function(){
        	var value = $(this).text();
        	var item_no = $(this).parent().find('.item_no').text()
        	$('#part_no').val(value);
        	$('#item_no').val("");
        	
        	var table = $('#eeda-table').dataTable();
          	table.fnSetColumnVis(1, false);
          	table.fnSetColumnVis(2, false);
          	
          	$('.itemShow').show();
          	if(item_no){
          		$('#orderText').text(item_no);
          	}
        	searchData();
        });
        
      
        $('#resetBtn').click(function(e){
        	$("#orderForm")[0].reset();
        });

        $('#searchBtn').click(function(){
        	var table = $('#eeda-table').dataTable();
          	table.fnSetColumnVis(1, true);
          	table.fnSetColumnVis(2, true);
          	$('.itemShow').hide();
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
      
        var searchData=function(){
        	$.blockUI({ 
                message: '<h1><img src="/images/loading.gif" style="height: 50px; margin-top: -3px;"/> LOADING...</h1>' 
            });
        	var itemJson = buildCondition();
        	var url = "/invCheckOrder/list?jsonStr="+JSON.stringify(itemJson);
        	dataTable.ajax.url(url).load();
        };
        
        order.refleshTable = function(){
        	$.blockUI({ 
                message: '<h1><img src="/images/loading.gif" style="height: 50px; margin-top: -3px;"/> LOADING...</h1>' 
            });
        	dataTable.ajax.url("/invCheckOrder/list").load();
        }
        
        
	});
});