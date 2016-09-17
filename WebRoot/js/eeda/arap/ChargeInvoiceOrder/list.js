define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','sco','validate_cn'], function ($, metisMenu) { 

    $(document).ready(function() {
    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'create-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/chargeInvoiceOrder/createlist",
            columns:[
				      { "width": "10px",
					    "render": function ( data, type, full, meta ) {
					    	return '<input type="checkbox" class="checkBox">';
					    }
				      },
					  { "data": "ORDER_NO" },
					  { "data": "PAYEE_NAME","class":"SP_NAME"}, 
					  { "data": "STATUS"}, 
					  { "data": "CREATE_NAME"}, 
					  { "data": "CREATE_STAMP"}	 
            ]
        });
        
		//选择是否是同一个客户
		var cnames = [];
		$('#create-table').on('click',".checkBox",function () {
				var cname = $(this).parent().siblings('.SP_NAME')[0].textContent;
				
				if($(this).prop('checked')==true){	
					if(cnames.length > 0 ){
						if(cnames[0]!=cname){
							$.scojs_message('请选择同一个结算公司', $.scojs_message.TYPE_ERROR);
							$(this).attr('checked',false);
							return false;
						}else{
							cnames.push(cname);
						}
					}else{
						cnames.push(cname);	
					}
				}else{
					cnames.pop(cname);
			 }
			 if (cnames.length>0){
				 $('#createBtn').prop('disabled',false);
			 }else{
				 $('#createBtn').prop('disabled',true);
			 }
    	 });
		
		//查看应收应付对账结果
		var checked = '';
    	$('#checkOrderAll').click(function(){
    		 checked = '';
	         if($('#checkOrderAll').prop('checked')==true){
	        	 checked = $('#checkOrderAll').val();	        	
	            }
	         var url = "/chargeCheckOrder/list?checked="+checked;	         
	         dataTable.ajax.url(url).load();
    	});
		
		
      	//checkbox选中则button可点击   创建对账单
		$('#eeda-table').on('click',"input[name='order_check_box']",function () {
			
			var hava_check = 0;
			$('.checkBox').each(function(){	
				var checkbox = $(this).prop('checked');
	    		if(checkbox){
	    			hava_check=1;
	    		}	
			});
			if(hava_check>0){
				$('#createBtn').attr('disabled',false);
			}else{
				$('#createBtn').attr('disabled',true);
			}
		});
		
		$('#createBtn').click(function(){
			$('#createBtn').attr('disabled',true);
			
        	var itemIds=[];
        	$('.checkBox').each(function(){
        		var checkbox = $(this).prop('checked');
        		if(checkbox){
        			var itemId = $(this).parent().parent().attr('id');
        			itemIds.push(itemId);
        		}
        	});
        	
        	$('#idsArray').val(itemIds);
        	$('#billForm').submit();
        });
  
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      });

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
          var url = "/chargeCheckOrder/list?order_no="+order_no
               +"&sp_name="+sp_name
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
        }
       
    });
});