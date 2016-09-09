define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap','sco','validate_cn'], function ($, metisMenu) { 

    $(document).ready(function() {
    	document.title = '应收对账单查询 | '+document.title;


    	//datatable, 动态处理
        var dataTable = eeda.dt({
            id: 'eeda-tableChecked',
            paging: true,
            scrollX:false,
            serverSide: true, //不打开会出现排序不对
            ajax: "/chargeCheckOrder/checkedList",
            columns:[
					  { "data": null,"width":"30px",
						  "render": function ( data, type, full, meta ) {
							  if(full.BILL_FLAG != 'Y')
							  return '<input type="checkbox" class="checkBox" name="order_check_box" order_type="'+full.SP_NAME+'" value="'+full.ID+'">';
							  else 
						    		return '<input type="checkbox" disabled  value="'+full.ID+'" >';
						  }
					    },		                
					  { "data": "ORDER_NO",
						  "render": function ( data, type, full, meta ) {
		                      return "<a href='/jobOrder/edit?id="+full.ID+"'target='_blank'>"+data+"</a>";
		                  }
					  },
					  { "data": "CREATE_TIME"},  
					  { "data": "SP_NAME","sClass":"SP_NAME"}, 
					  { "data": "BILL_FLAG",
						 "render":function(data){
							 if(data !='Y')
								 return '未创建对账单';
							 else
								 return '已创建对账单';
						  } 
					     },
					  { "data": "RMB",
				    	 "render":function(data, type, full, meta){
				    		 if(data<0){
				    			 return '<span style="color:red">'+data+'</span>';
				    		 }else{
				    			 return data;
				    		 }
				    	 }
					     },
	                  { "data": "USD",
			    		 "render":function(data, type, full, meta){
			    			 if(data<0){
			    				 return '<span style="red">'+data+'</span>';
			    			 }else{
			    				 return data;
			    			 }
				    	}
					    },
//		              { "data": "HKD"}, 
//		              { "data": "JPY"}, 
		              { "data": "FND"}, 
		              { "data": "VGM"}, 
		              { "data": "CONTAINER_AMOUNT",
		            	  "render":function(data, type, full, meta){
		            		  if(data){
		            			  var dataArr = data;
		            			  var Arr = dataArr.split(",");
		            			  var a = 0;
			  	            	  var b = 0;
			  	            	  var c = 0;
			  	            	  var dataStr = "";
			  	            	for(var i=0;i<Arr.length;i++){
			            			if(Arr[i]=="20GP"){
			            				a++;
			            			}
			            			if(Arr[i]=="40GP"){
			            				b++;
			            			}
			            			if(Arr[i]=="45GP"){
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
                      { "data": "GROSS_WEIGHT"}, 
		              { "data": "CONTAINER_NO"},
		              { "data": "REF_NO"}, 
		              { "data": "MBL_NO"},
		              { "data": "HBL_NO"},
		              { "data": "TRUCK_TYPE"}
            ]
        });
        //反选
		$('#allCheck').click(function(){
	    	$("input[name='order_check_box']").each(function () {  
	            this.checked = !this.checked;  
	         });
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
          var url = "/chargeCheckOrder/checkedList?order_no="+order_no
               +"&sp_name="+sp_name
               +"&create_stamp_begin_time="+start_date
               +"&create_stamp_end_time="+end_date;

          dataTable.ajax.url(url).load();
       }
       
    });
});