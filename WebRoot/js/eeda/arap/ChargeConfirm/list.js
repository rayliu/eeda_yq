define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','sco'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '应收对账明细查询 | '+document.title;

    		$('#menu_charge').addClass('active').find('ul').addClass('in');

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: true, 
            ajax: "/chargeConfirm/list",
            columns:[
					  { "data": null,"width": "10px",
						  "render": function ( data, type, full, meta ) {
							  return '<input type="checkbox" class="checkBox" name="order_check_box" order_type="'+full.TYPE+'" value="'+full.ID+'">';			  
	                	  }		            
					  },		                
					  { "data": "ORDER_NO" }, 
					  { "data": "CREATE_STAMP", "width": "100px"},
					  { "data": "TYPE"}, 
					  { "data": "SP_NAME"}, 
					  { "data": "COST_NAME"},
	                  { "data": "PRICE" },
		              { "data": "AMOUNT"}, 
		              { "data": "UNIT_NAME"}, 
		              { "data": "CURRENCY_NAME"}, 
		              { "data": "TOTAL_AMOUNT"}, 
		              { "data": "EXCHANGE_RATE"}, 
                      { "data": "CURRENCY_TOTAL_AMOUNT"}, 
		              { "data": "REMARK"}
            ]
        });
        
		$("#allCheck").click(function(){
			dataTable.settings()[0].oFeatures.bServerSide=false;
	    	$("input[name='order_check_box']").each(function (row) {  
	            if($("#allCheck1").prop('checked')){
	            	this.checked = true;
	            }else{
	            	this.checked = false;
	            }
	        });
		});
        

        $("#chargeConfiremBtn").click(function(e){//确认
            e.preventDefault();
            $("#chargeConfiremBtn").attr("disabled",true);
            
        	var trArr=[];
        	var orderTypeArr=[];
        	var $checked = [];
            $("input[name='order_check_box']").each(function(){
            	if($(this).prop('checked') == true && $(this).prop('disabled') == false){
            		trArr.push($(this).val());
            		orderTypeArr.push($(this).attr('order_type'));
            		$checked.push($(this));
            		$(this).attr("disabled",true);
            	}
            });     
            
            if(trArr.length==0){
            	$.scojs_message('请选择单据$', $.scojs_message.TYPE_OK);
            	$("#chargeConfiremBtn").attr("disabled",false);
            	return false;
            }
            
            console.log(trArr);
            var returnOrderIds = trArr.join(",");
            var orderTypes=orderTypeArr.join(",");
            $.post("/chargeConfirm/chargeConFirmReturnOrder", {returnOrderIds:returnOrderIds,orderTypes:orderTypes}, function(data){
            	if(data.success){
            		//chargeConfiremTable.fnSettings().sAjaxSource = "/chargeConfiremList/list";
            		//chargeConfiremTable.fnDraw(); 
            		//refreshCreateList(); 
            		$("#chargeConfiremBtn").attr("disabled",false);
            		for(var i = 0;i<$checked.length;i++){
                		$checked[i].parent().parent().hide();
                	}
            		searchData();
            		$.scojs_message('单据确认成功', $.scojs_message.TYPE_OK);
            	}else{
            		alert('确认失败，请联系管理员进行优化');
            	}
            },'json');
        });
        
        
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
    	  dataTable.settings()[0].oFeatures.bServerSide=true;
          searchData(); 
      })

     var searchData=function(){
          var order_no = $("#order_no").val(); 
          var sp_name = $('#sp_input').val();
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/chargeConfirm/list?order_no="+order_no
               +"&sp_name="+sp_name
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date;
          dataTable.ajax.url(url).load();
      };
    	
    });
});