define(['jquery', 'metisMenu', 'sb_admin',  'file_upload' ,'sco','dataTablesBootstrap'], function ($, metisMenu) { 

    $(document).ready(function() {

    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-table',
            ajax: "/tradeItem/list",
            columns:[
                 { "data": "COMMODITY_NAME","width":"100px"},  
                 { "data": "COMMODITY_CODE","width":"100px"}, 
                 { "data": "UNIT_NAME"},                   
                 { "data": "VAT_RATE","width":"80px"},
                 { "data": "REBATE_RATE"},          
                 { "data": "REMARK"},
                 {"data": null, 
                    "render": function ( data, type, full, meta ) {
                      var str = "<a class='btn table_btn btn-success btn-sm' href='/tradeItem/edit?id="+full.ID+"' target='_blank'>"+
                        "<i class='fa fa-edit fa-fw'></i>"+
                        "编辑"+"</a> ";
                      return str;
                    }
                }
            ]
        });
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })
      $('#exportTotaledExcel').click(function(){
    	  $(this).attr('disabled', true);
          var commodity_name = $("#commodity_name").val();
          var commodity_code = $("#commodity_code").val();
          excel_method(commodity_name,commodity_code);
      });
      var excel_method = function(commodity_name,commodity_code){
		  $.post('/tradeItem/downloadExcelList',{commodity_name:commodity_name,commodity_code:commodity_code}, function(data){
	          $('#exportTotaledExcel').prop('disabled', false);
	          $.scojs_message('生成应收Excel对账单成功', $.scojs_message.TYPE_OK);
	          window.open(data);
	      }).fail(function() {
	          $('#exportTotaledExcel').prop('disabled', false);
	          $.scojs_message('生成应收Excel对账单失败', $.scojs_message.TYPE_ERROR);
	      });
      }
      
     var searchData=function(){
          var commodity_name = $("#commodity_name").val().trim();
          var commodity_code = $("#commodity_code").val().trim();
          var url = "/tradeItem/search?commodity_name="+commodity_name+"&commodity_code="+commodity_code;
          dataTable.ajax.url(url).load();
      };
      
      $("#import_tradeItem").click(function(){
	    	order_id = $('#order_id').val();
	    	fileUpload(order_id);
	    	if($('#order_id').val() == ''){
	    		$.scojs_message('先保存订单才可导入', $.scojs_message.TYPE_ERROR);
	    		return false;
	    	}
	    	
	    	$("#importFileUpload").click();
	    	
	    });
      
      
      
      var fileUpload = function(order_id){
	    	var str = null;
		    var errCustomerNo = null;
		    var errCustomerNoArr = [];
			$('#importFileUpload').fileupload({
		        dataType: 'json',
		        url: '/importOrder?order_type=trade_item',
		        done: function (e,data) {
		        	$("#footer").show();
		        	$("#msgLoad").empty().append('<h4>'+data.result.CAUSE+'</h4>');
		        	searchData();
		        },  
		        progressall: function (e, data) {//设置上传进度事件的回调函数  
		        	str = null;
		            errCustomerNo = null;
		            errCustomerNoArr = [];
		        	$('#msgLoad').empty().append('<center><img src="/yh/image/loading5.gif" width="20%"><h4>导入过程可能需要一点时间，请勿退出页面！</h4></center>');
		        	$('#myModal').modal('show');
		        	$("#footer").hide();
		        } 
		    },'json').error(function (jqXHR, textStatus, errorThrown) {
		        alert("出错了，请刷新页面重新尝试。")
		        console.log(errorThrown);
		    });
	    }
      
    });
});