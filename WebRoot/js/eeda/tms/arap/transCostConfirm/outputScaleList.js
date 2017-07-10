define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	
      var dataTable = eeda.dt({
          id: 'eeda_table',
          serverSide: false, //不打开会出现排序不对 
          ajax: "/outputScale/list",
          columns: [
			{ "width": "10px",
				    "render": function ( data, type, full, meta ) {
				    		return '<input type="checkbox" disabled>';
				    }
			},
			{ "data": "ORDER_NO", "width": "80px",
		    	  "render": function ( data, type, full, meta ) {
                    return "<a href='/transJobOrder/edit?id="+full.TJOID+"'target='_blank'>"+data+"</a>";
                }
			},
           { "data": "LADING_NO", "width": "60px"},			
            { "data": "C_DATE", "width": "80px"},
            { "data": "CUSTOMER_NAME", "width": "80px"},
            { "data": "TYPE", "width": "100px"},
            { "data": "COMBINE_WHARF", "width": "150px"},
            { "data": "CONTAINER_NO", "width": "80px"},
            { "data": "CABINET_TYPE", "width": "40px"},
            { "data": "COMBINE_UNLOAD_TYPE", "width": "80px"},
            { "data": "COMBINE_CAR_NO", "width": "70px"},
            { "data": "null", "width": "40px"},
            { "data": "FREIGHT","width":"60px",
                "render": function ( data, type, full, meta ) {
                  if(data)
                    return eeda.numFormat(parseFloat(data).toFixed(2),3)
                  else
                    return '';
                  }
            },
            { "data": "REMARK", "width": "200px"},
            { "data": "CREATE_STAMP","width": "80px"}
          ]
      });

      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
          var order_no = $.trim($("#order_no").val()); driver
          var driver = $("#driver").val().trim();
          var c_date_begin_time = $("#c_date_begin_time").val();
          var c_date_end_time = $("#c_date_end_time").val();
          
          var customer = $("#customer").val();
          var customer_name = $("#customer_input").val().trim(); 
//          var sp = $("#sp").val(); 
          var car_id = $("#car_id").val();
          var car_no = $("#car_id_input").val().trim();
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          var audit_flag = $("#audit_flag").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/outputScale/list?order_no="+order_no
          			   +"&c_date_begin_time="+c_date_begin_time
          			   +"&c_date_end_time="+c_date_end_time
			           +"&customer="+customer
			           +"&customer_name="+customer_name
//			           +"&sp_id="+sp
                       +"&car_id="+car_id
                       +"&car_no="+car_no
                       +"&driver="+driver
		               +"&create_stamp_begin_time="+start_date
		               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
      };
      
      
      
      //全选
      $('#AllCheck').click(function(){
	      	$(".checkBox").prop("checked",this.checked);
	      	if($('#AllCheck').prop('checked')){
        		$('#confirmBtn').attr('disabled',false);
        	}else{
        		$('#confirmBtn').attr('disabled',true);
        	}
      });
      
      $("#eeda_table").on('click','.checkBox',function(){
		    $("#AllCheck").prop("checked",$(".checkBox").length == $(".checkBox:checked").length ? true : false);
      });
      
      
      

      	//checkbox选中则button可点击
		$('#eeda_table').on('click','.checkBox',function(){
			
			var hava_check = 0;
			$('#eeda_table input[type="checkbox"]').each(function(){	
				var checkbox = $(this).prop('checked');
	    		if(checkbox){
	    			hava_check=1;
	    		}	
			})
			if(hava_check>0){
				$('#confirmBtn').attr('disabled',false);
			}else{
				$('#confirmBtn').attr('disabled',true);
			}
		});
		//确认费用
//		$('#confirmBtn').click(function(){
//			$('#confirmBtn').attr('disabled',true);
//        	var itemIds=[];
//        	$('#eeda_table input[type="checkbox"]').each(function(){
//        		var checkbox = $(this).prop('checked');
//        		if(checkbox){
//        			var itemId = $(this).parent().parent().attr('id');
//        			if(itemId!=undefined){
//                itemIds.push(itemId);
//              }
//        		}
//        	});
//          if(itemIds.length==0){
//            $.scojs_message('该单据没有费用，请先录入费用', $.scojs_message.TYPE_ERROR);
//            return;
//          }
//	    	 $.post('/transCostConfirm/costConfirm?itemIds='+itemIds, function(data){
//	    		 if(data.result==true){
//	    			 $.scojs_message('单据确认成功', $.scojs_message.TYPE_OK);
//	    			 searchData();
//	    			 $('#confirmBtn').attr('disabled', false);
//	    		 }
//	    	 },'json').fail(function() {
//	                $.scojs_message('单据确认失败', $.scojs_message.TYPE_ERROR);
//	                $('#confirmBtn').attr('disabled', false);
//	              });
//        })
        //导出产值表
        $('#export_outputTable').click(function(){
        	var car_no = $('#car_id_input').val().trim();
        	var driver = $('#driver').val().trim();
        	if(car_no||driver){
        		var order_id = $('#order_id').val();
            	var company_name = $('#company_name').val();
            	$.post('/outputScale/downloadList',{car_no:car_no,driver:driver},function(data){
            		if(data){
            			window.open(data);
            		}else{
            			$.scojs_message('生成产值表PDF失败',$.scojs_message.TYPE_ERROR);
            		}
            	});
        	}else{
        		$.scojs_message('请填上车牌或者司机', $.scojs_message.TYPE_ERROR);
        	}
        });
      
  });
});