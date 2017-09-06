define(['jquery', 'file_upload' ,'sco'], function ($, metisMenu) { 
	$(document).ready(function() {
		
		var costTable = eeda.dt({
			id: 'cost_table',
			url:'/bookingOrder/edit',
			colReorder: true,
	        paging: false,
	        serverSide: true, //不打开会出现排序不对
	        columns:[
	     			{ "data": "CHARGE_NAME",width:"120px",
	     				"render": function ( data, type, full, meta ) {
	     						if(!data)
	     							data='';
	     						return data;
	     				}
	     			},
			        { "data": "CHARGE_ENG_NAME",width:"120px",
			        	"render": function ( data, type, full, meta ) {
			    			if(!data)
			    				data='';
			    			return data;
			        	}
			        },
			        { "data": "PRICE",width:"60px",
			            "render": function ( data, type, full, meta ) {
			            	if(data)
			                    var str =  parseFloat(data).toFixed(2);
			                else
			                
			                return '<input type="text" name="price" style="width:70px" value="'+str+'" class="form-control notsave"/>';
			           }
			        },
			        { "data": "AMOUNT",width:"60px",
			            "render": function ( data, type, full, meta ) {
			            	if(!data)
			                    data='1';
			                return '<input type="text" name="amount" style="width:70px" value="'+data+'" class="form-control notsave" />';
			          }
			        },
			        { "data": "UNIT_NAME",width:"60px",
			            "render": function ( data, type, full, meta ) {
			            	 if(!data)
			                     data='';
			                 return data;
			          }
			        },
			        { "data": "TOTAL_AMOUNT","className":"currency_total_amount",width:"60px",
			            "render": function ( data, type, full, meta ) {
			            	if(data)
			                    var str =  parseFloat(data).toFixed(3);
			                else
			                	str = '';
			            	return '<input type="text" name="total_amount" style="width:70px" value="'+str+'" class="form-control notsave" disabled />';
			            	
			            }
			        },
			        { "data": "CURRENCY_NAME",width:"60px",
			        	"render": function ( data, type, full, meta ) {
			        			if(!data)
			                        data='';
			                    return data;
			            }
			        }
			        ]
				});
		//两位小数处理
		$("#cost_table").find("tr").each(function(){
			self = $(this)
			self.find("td:eq(2),td:eq(5)").each(function(){
				var num = (parseFloat($(this).text())).toFixed(2)
				if(!isNaN(num)){
					$(this).text(num)					
				}
			})
		})
	});
});