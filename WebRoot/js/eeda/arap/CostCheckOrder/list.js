define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {
  	document.title = '应付对账单查询  | '+document.title;
  	  if(type!=""){
  		  $('#menu_todo_list').addClass('active').find('ul').addClass('in');
  		  $('#menu_cost').removeClass('active').find('ul').removeClass('in');
  	  }else{
  		$('#menu_todo_list').removeClass('active').find('ul').removeClass('in');
		  $('#menu_cost').addClass('active').find('ul').addClass('in');
  	  }
  	//datatable, 动态处理
      var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: true,
          serverSide: false, //不打开会出现排序不对 
          ajax: "/costCheckOrder/list",
          columns: [
			{ "width": "10px",
				    "render": function ( data, type, full, meta ) {
				    	if(full.BILL_FLAG != 'Y')
				    		return '<input type="checkbox" class="checkBox">';
				    	else
				    		return '<input type="checkbox" class="checkBox" disabled>';
				    }
			},
            { "data": "ORDER_NO", "width": "100px"},
            { "data": "CREATE_STAMP", "width": "100px"},
            { "data": "BILL_FLAG", "width": "60px",
            	"render": function ( data, type, full, meta ) {
			    	if(data != 'Y')
			    		return '未创建对账单';
			    	else 
			    		return '已创建对账单';
			    }
            },
            { "data": null, "width": "60px"},
            
            { "data": "TYPE", "width": "60px"},
            { "data": "SP_NAME", "width": "100px"},
            { "data": "TOTAL_COSTRMB", "width": "60px"
            },
            { "data": null, "width": "60px"},
            { "data": null, "width": "60px"},
            { "data": null, "width": "60px"},
            
            { "data": "FND", "width": "60px",
            	"render": function ( data, type, full, meta ) {
            		if(data)
			    		return data;
			    	else 
			    		return full.DESTINATION;
			    }
            },
            { "data": "VOLUME", "width": "60px"},
            { "data": "CONTAINER_AMOUNT","width": "60px",
            	"render": function ( data, type, full, meta ) {
	            	if(data){
	            		var dataArr = data.split(",");
	            		var a = 0;
	            		var b = 0;
	            		var c = 0;
	            		var dataStr = "";
	            		for(var i=0;i<dataArr.length;i++){
	            			if(dataArr[i]=="20GP"){
	            				a++;
	            			}
	            			if(dataArr[i]=="40GP"){
	            				b++;
	            			}
	            			if(dataArr[i]=="45GP"){
	            				c++;
	            			}
	            		}
	            		if(a>0){
	            			dataStr+="20GPx"+a+";"
	            		}
	            		if(b>0){
	            			dataStr+="40GPx"+b+";"
	            		}
	            		if(c>0){
	            			dataStr+="45GPx"+c+";"
	            		}
	            		return dataStr;
	            	}else{
	            		return '';
	            	}
            	}
            },
            { "data": "NET_WEIGHT", "width": "60px"},
            { "data": null, "width": "60px"},
            
            { "data": "MBL_NO", "width": "60px"},
            { "data": "CONTAINER_NO", "width": "100px"},
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
          var customer = $("#customer").val(); 
          var sp = $("#sp").val(); 
          var start_date = $("#create_stamp_begin_time").val();
          var end_date = $("#create_stamp_end_time").val();
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/costCheckOrder/list?order_no="+order_no
			           +"&customer_id="+customer
			           +"&sp_id="+sp
		               +"&create_stamp_begin_time="+start_date
		               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
      };
      
      //计算创建对账单的总额
      var amount = 0;
      var sum = 0;
      $('#eeda_table').on('click','.checkBox',function(){
    	  var id = $(this).parent().parent().attr('id');
    	  var amountStr = $($('#'+id+' td')[7]).text();
    	  if(amountStr!=''){
	    	  amount = parseFloat( amountStr );
	    	  if(this.checked==true){
	    		  sum+=amount;
	    	  }else{
	    		  sum-=amount;
	    	  }
	    	  $("#totalAmountSpan").html(parseFloat(sum).toFixed(2));
    	  }
      })
      
      //全选，全不选
      $('#AllCheck').click(function(){
    	  var ischeck = this.checked;
      	$(".checkBox").each(function () {  
              this.checked = ischeck;  
           });  
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
				$('#createBtn').attr('disabled',false);
			}else{
				$('#createBtn').attr('disabled',true);
			}
		});
		$('#createBtn').click(function(){
			$('#createBtn').attr('disabled',true);
        	var itemIds=[];
        	$('#eeda_table input[type="checkbox"]').each(function(){
        		var checkbox = $(this).prop('checked');
        		if(checkbox){
        			var itemId = $(this).parent().parent().attr('id');
        			itemIds.push(itemId);
        		}
        	});
        	var totalAmount = parseFloat($("#totalAmountSpan").text());
        	location.href ="/costCheckOrder/create?totalAmount="+totalAmount+"&itemIds="+itemIds;
        })
      
  });
});