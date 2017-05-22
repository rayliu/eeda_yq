define(['jquery', 'metisMenu', 'sb_admin','dataTables',  'dataTablesBootstrap', 'validate_cn', 'sco', 'jq_blockui'], function ($, metisMenu) {
$(document).ready(function() {
	  //datatable, 动态处理
	var itemTable = eeda.dt({
        id: 'item_table',
        paging: true,
        serverSide: true, //不打开会出现排序不对
        scrollX:false,
        //ajax: "/inventory/list",
        "drawCallback": function( settings ) {
	        $.unblockUI();
            $('.product_img').popover({
                html: true,
                container: 'body',
                placement: 'right',
                trigger: 'hover'
            });
	    },
        columns:[
                 { "data": "ITEM_NO","class":"item_no", "visible":false,
              	    "render": function ( data, type, full, meta ) {
              	    	if(data){
                        	return "<a href='#'>"+data+"</a> ";
                        }else{
                            return '';
                        }
              	    }
                }, 
                { "data": "ITEM_NAME","class":"item_name" , "visible":false},
                { "data": "PART_NO" ,"class":"part_no",
              	    "render": function ( data, type, full, meta ) {
              	    	if(data){
                        	return "<a href='#'>"+data+"</a>";
                        }else{
                            return '';
                        }
              	    }
                }, 
                { "data": "PART_NAME" ,"class":"part_name"},
                { "data": "IMG_PATH", "class":"product", 
                    "render": function ( data, type, full, meta ) {
                    	return '<img class="product_img" src="/images/product/'+full.PART_NO+'.jpg" onerror="javascript:this.src=\'/images/product/no_photo.jpg\'"  width="50" '
                        +' data-content="<img src=&quot;/images/product/'+full.PART_NO+'.jpg&quot; onerror=&quot;javascript:this.src=\'/images/product/no_photo.jpg\'&quot;'
                        +' height=&quot;140&quot; >" >';
                    }
                }, 
				{ "data": "AMOUNT"}
        ]
    });
	
	$("#eeda-table").on('click', '.partDetail', function(e){
      	var item_no = $(this).attr("item_no");
      	var item_name = $($(this).parent().parent()).find('.item_name').text();
      	var part_amount = $($(this).parent().parent()).find('.part_amount').text();
      	$('#itemNo').text(item_no);
      	$('#itemName').text(item_name);
      	$('#part_amount').text(part_amount);
      	searchData(item_no);
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
	
	var searchData=function(item_no){
		$.blockUI({ 
            message: '<h1><img src="/images/loading.gif" style="height: 50px; margin-top: -3px;"/> LOADING...</h1>' 
        });
     	var itemJson = buildCondition();
     	var url = "/wmsproduct/itemList?item_no="+item_no+"&jsonStr="+JSON.stringify(itemJson);
     	itemTable.ajax.url(url).load();
    };

});
});