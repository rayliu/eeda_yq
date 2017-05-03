define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'jq_blockui'], function ($, metisMenu) {
	$(document).ready(function() {
    	document.title = '产品列表 | '+document.title;

    	$("#breadcrumb_li").text('产品列表');

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/wmsproduct/list",
            "drawCallback": function( settings ) {
		        $.unblockUI();
		    },
            columns:[
                
                { "data": "ITEM_NO","class":"item_no", 
              	    "render": function ( data, type, full, meta ) {
              	    	if(data){
              	    		$('#orderText').text(data);
                        	return "<a href='#'>"+data+"</a>";
                        }else{
                            return '';
                        }
              	    }
                }, 
                { "data": "ITEM_NAME" },
                { "data": "PART_NO" ,"class":"part_no", 
              	    "render": function ( data, type, full, meta ) {
              	    	if(data){
                        	return "<a href='#'>"+data+"</a>";
                        }else{
                            return '';
                        }
              	    }
                }, 
                { "data": "PART_NAME" },
				{ "data": "UNIT"}, 
				{ "data": "AMOUNT"}, 
				{ "data": "CREATE_TIME"}, 
				{ "data": "CREATOR_NAME"},
                { "width": "30px",
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
          	if(item_no){
          		$('#orderText').text(item_no);
          	}
        	searchData();
        });
      
        $('#resetBtn').click(function(e){
        	$("#orderForm")[0].reset();
        	searchData();
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
      
        var searchData=function(){
        	$.blockUI({ 
                message: '<h1><img src="/images/loading.gif" style="height: 50px; margin-top: -3px;"/> LOADING...</h1>' 
            });
        	
        	var item_no = $('#item_no').val();
        	var item_name = $('#item_name').val();
        	var part_no = $('#part_no').val();
        	var part_name = $('#part_name').val();
        	var table = $('#eeda-table').dataTable();
        	if(part_no.trim()=='' && part_name.trim()==''){
              	table.fnSetColumnVis(0, true);
              	table.fnSetColumnVis(1, true);
              	$('.itemShow').hide();
              	$('#orderText').text('');
        	}else {
              	table.fnSetColumnVis(0, false);
              	table.fnSetColumnVis(1, false);
              	$('.itemShow').show();
        	} 
        	
        	var itemJson = buildCondition();
        	var url = "/wmsproduct/list?jsonStr="+JSON.stringify(itemJson);
        	dataTable.ajax.url(url).load();
        };
        
        
	});
});