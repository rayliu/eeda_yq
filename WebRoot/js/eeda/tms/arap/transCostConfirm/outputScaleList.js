define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '产值表  | '+document.title;
      $('#menu_cost').addClass('active').find('ul').addClass('in');
      $("#breadcrumb_li").text('产值表');
      var dataTable = eeda.dt({
          id: 'eeda_table',
          serverSide: false, //不打开会出现排序不对 
          ajax: "/outputScale/list?audit_flag_notequals="+$("#audit_flag").val(),
          columns: [
			{ "width": "10px",
				    "render": function ( data, type, full, meta ) {
				    	if(full.AUDIT_FLAG != 'Y')
				    		return '<input type="checkbox" class="checkBox">';
				    	else 
				    		return '<input type="checkbox" disabled>';
				    }
			},
			{ "data": "ORDER_NO", "width": "80px",
		    	  "render": function ( data, type, full, meta ) {
                    return "<a href='/transJobOrder/edit?id="+full.JOBID+"'target='_blank'>"+data+"</a>";
                }
			},
           { "data": "CABINET_DATE", "width": "60px"},			
            { "data": "CREATE_STAMP", "width": "80px"},
            { "data": "CONTAINER_NO", "width": "80px"},
            { "data": "SO_NO", "width": "100px"},
            { "data": "AUDIT_FLAG", "width": "40px",
            	"render": function ( data, type, full, meta ) {
			    	if(data != 'Y')
			    		return '未确认';
			    	else 
			    		return '已确认';
			    }
            },
            { "data": "CUSTOMER", "width": "80px"},
            { "data": "SP_NAME", "width": "80px"},
            { "data": "CAR_NO", "width": "80px"},
            { "data": "CHARGE_NAME", "width": "60px"},
            { "data": "PRICE", "width": "40px"},
            { "data": "PRICE", "width": "40px"},
            { "data": "AMOUNT","width": "40px"}
          ]
      });

      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })

     var searchData=function(){
          var order_no = $.trim($("#order_no").val()); 
          
          var order_export_date_begin_time = $("#order_export_date_begin_time").val();
          var order_export_date_end_time = $("#order_export_date_end_time").val();
          
          var customer = $("#customer").val(); 
          var sp = $("#sp").val(); 
          var car_id = $("#car_id").val();
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
          			   +"&order_export_date_begin_time="+order_export_date_begin_time
          			   +"&order_export_date_end_time="+order_export_date_end_time
			           +"&customer_id="+customer
			           +"&sp_id="+sp
                 +"&car_id="+car_id
		               +"&create_stamp_begin_time="+start_date
		               +"&create_stamp_end_time="+end_date
          			   +"&audit_flag_notequals="+audit_flag;

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
		$('#confirmBtn').click(function(){
			$('#confirmBtn').attr('disabled',true);
        	var itemIds=[];
        	$('#eeda_table input[type="checkbox"]').each(function(){
        		var checkbox = $(this).prop('checked');
        		if(checkbox){
        			var itemId = $(this).parent().parent().attr('id');
        			if(itemId!=undefined){
                itemIds.push(itemId);
              }
        		}
        	});
          if(itemIds.length==0){
            $.scojs_message('该单据没有费用，请先录入费用', $.scojs_message.TYPE_ERROR);
            return;
          }
	    	 $.post('/transCostConfirm/costConfirm?itemIds='+itemIds, function(data){
	    		 if(data.result==true){
	    			 $.scojs_message('单据确认成功', $.scojs_message.TYPE_OK);
	    			 searchData();
	    			 $('#confirmBtn').attr('disabled', false);
	    		 }
	    	 },'json').fail(function() {
	                $.scojs_message('单据确认失败', $.scojs_message.TYPE_ERROR);
	                $('#confirmBtn').attr('disabled', false);
	              });
        })
      
  });
});