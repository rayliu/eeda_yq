define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco',  'dtColReorder'], function ($, metisMenu) {
  $(document).ready(function() {
  	  
      var dataTable = eeda.dt({
          id: 'eeda_table',
          colReorder: true,
          paging: true,
          serverSide: true, //不打开会出现排序不对 
//          ajax: "/salesBillReport/list",
          initComplete:function(settings){
    	  cssTd();
          },
          columns: [
      			{ "data": "ORDER_NO", "width": "80px","className":"order_no",
      				"render":function(data,type,full,meta){
      					return "<a href = '/jobOrder/edit?id="+full.ID+" 'target='_blank'>"+data+"</a>";      					
      				}
      			},
      			{ "data": "MBL_NO", "width": "100px","className":"mbl_no"},
	            { "data": "ORDER_EXPORT_DATE", "width": "80px" ,"className":"order_export_date"},
	            { "data": "USER_NAME", "width": "100px","className":"user_name"},
	            { "data": "ABBR", "width": "100px","className":"abbr"},
	            { "data": "CONTRACT_NO", "width": "80px","className":"contract_no"},
	            { "data": "POL_NAME", "width": "80px","className":"pol_name"},
	            { "data": "POD_NAME", "width": "80px","className":"pod_name"},
	            { "data": "FEE_COUNT", "width": "80px","className":"fee_count"},
	            { "width": "100px","className":"charge",
	            	"render": function(data, type, full, meta) {
		            	    var str = '';
		            	    var sum_charge_CNY='';

		            	    if(full.SUM_CHARGE_CNY){
		            	    	sum_charge_CNY = 'CNY: '+eeda.numFormat(full.SUM_CHARGE_CNY.toFixed(2),3)+'<br>';
		            	    }
		            	    var sum_charge_USD='';
		            	    if(full.SUM_CHARGE_USD){
		            	    	sum_charge_USD = 'USD: '+eeda.numFormat(full.SUM_CHARGE_USD.toFixed(2),3)+'<br>';
		            	    }
		            	    var sum_charge_JPY='';
		            	    if(full.SUM_CHARGE_JPY){
		            	    	sum_charge_JPY = 'JPY: '+eeda.numFormat(full.SUM_CHARGE_JPY.toFixed(2),3)+'<br>';
		            	    }
		            	    var sum_charge_HKD='';
		            	    if(full.SUM_CHARGE_HKD){
		            	    	sum_charge_HKD = 'HKD: '+eeda.numFormat(full.SUM_CHARGE_HKD.toFixed(2),3)+'<br>';
		            	    }
		            	    str = sum_charge_CNY+sum_charge_USD+sum_charge_JPY+sum_charge_HKD;
		            	    return str;
				  }
	            },
	            { "data": "SUM_CHARGE_TOTAL", "width": "100px"  ,"className":"sum_charge_total",
	            	"render": function(data, type, full, meta) {
		            	    if(!data){
		            	    	return '';
		            	    	
		            	     }
							return eeda.numFormat(data.toFixed(2),3);
					}
	            },
	            {  "width": "100px","className":"pay_charge",
	            	"render": function(data, type, full, meta) {
		            		var str = '';
		            	    var sum_pay_charge_CNY='';
	
		            	    if(full.SUM_PAY_CHARGE_CNY){
		            	    	sum_pay_charge_CNY = 'CNY: '+eeda.numFormat(full.SUM_PAY_CHARGE_CNY.toFixed(2),3)+'<br>';
		            	    }
		            	    var sum_pay_charge_USD='';
		            	    if(full.SUM_PAY_CHARGE_USD){
		            	    	sum_pay_charge_USD = 'USD: '+eeda.numFormat(full.SUM_PAY_CHARGE_USD.toFixed(2),3)+'<br>';
		            	    }
		            	    var sum_pay_charge_JPY='';
		            	    if(full.SUM_PAY_CHARGE_JPY){
		            	    	sum_pay_charge_JPY = 'JPY: '+eeda.numFormat(full.SUM_PAY_CHARGE_JPY.toFixed(2),3)+'<br>';
		            	    }
		            	    var sum_pay_charge_HKD='';
		            	    if(full.SUM_PAY_CHARGE_HKD){
		            	    	sum_pay_charge_HKD = 'HKD: '+eeda.numFormat(full.SUM_PAY_CHARGE_HKD.toFixed(2),3)+'<br>';
		            	    }
		            	    str = sum_pay_charge_CNY+sum_pay_charge_USD+sum_pay_charge_JPY+sum_pay_charge_HKD;
		            	    return str;
				  }
	            },
	            { "data": "SUM_PAY_CHARGE_TOTAL", "width": "100px","className":"sum_pay_charge_total",
	            	"render": function(data, type, full, meta) {
	            			if(!data){
		            	    	return '';
		            	    }
							return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            {  "width": "100px","class":"cost",
	            	"render": function(data, type, full, meta) {
		            		var str = '';
		            	    var sum_cost_CNY='';
	
		            	    if(full.SUM_COST_CNY){
		            	    	sum_cost_CNY = 'CNY: '+eeda.numFormat(full.SUM_COST_CNY.toFixed(2),3)+'<br>';
		            	    }
		            	    var sum_cost_USD='';
		            	    if(full.SUM_COST_USD){
		            	    	sum_cost_USD = 'USD: '+eeda.numFormat(full.SUM_COST_USD.toFixed(2),3)+'<br>';
		            	    }
		            	    var sum_cost_JPY='';
		            	    if(full.SUM_COST_JPY){
		            	    	sum_cost_JPY = 'JPY: '+eeda.numFormat(full.SUM_COST_JPY.toFixed(2),3)+'<br>';
		            	    }
		            	    var sum_cost_HKD='';
		            	    if(full.SUM_COST_HKD){
		            	    	sum_cost_HKD = 'HKD: '+eeda.numFormat(full.SUM_COST_HKD.toFixed(2),3)+'<br>';
		            	    }			            	    
		             	    str = sum_cost_CNY+sum_cost_USD+sum_cost_JPY+sum_cost_HKD;
		             	    return str;
				  }
	            },
	            { "data": "SUM_COST_TOTAL", "width": "100px"  ,"className":"sum_cost_total",
	            	"render": function(data, type, full, meta) { 
	            		if(!data){
		            	    	return '';
		            	    }
							return eeda.numFormat(data.toFixed(2),3);
				  }
	            },
	            {  "width": "100px","className":"gross_profit",
	            	"render": function(data, type, full, meta) {
		            		var str="";
		            		
		            		var sum_charge_total="";
		            		var sum_cost_total="";
		            		if(full.SUM_CHARGE_TOTAL==0&&full.SUM_COST_TOTAL==0){
		            			return "";
		            		}
		            		if(full.SUM_CHARGE_TOTAL){
		            			sum_charge_total=full.SUM_CHARGE_TOTAL;
		            		}
		            		if(full.SUM_COST_TOTAL){
		            			sum_cost_total=full.SUM_COST_TOTAL;
		            		}
		            		str = sum_charge_total-sum_cost_total;
		            	    if(str<0){
		            	    	return '<span style="color:red;">'+eeda.numFormat(str.toFixed(2),3)+'</span>';
		            	    }
							return eeda.numFormat(str.toFixed(2),3);
				  }
	            },
	            { "width": "100px","className":"current_profit",
	            	"render": function(data, type, full, meta) {
		            		var str=0;
		            		var sum_pay_charge_total=0;
		            		var sum_cost_total=0;
		            		if(full.SUM_PAY_CHARGE_TOTAL==0 && full.SUM_COST_TOTAL==0){
		            			return "";
		            		}
		            		if(full.SUM_PAY_CHARGE_TOTAL){
		            			sum_pay_charge_total=full.SUM_PAY_CHARGE_TOTAL;
		            		}
		            		if(full.SUM_COST_TOTAL){
		            			sum_cost_total=full.SUM_COST_TOTAL;
		            		}
		            		str = sum_pay_charge_total-sum_cost_total;
		            	    if(str<0){
		            	    	return '<span style="color:red;">'+eeda.numFormat(str.toFixed(2),3)+'</span>';
		            	    }
							return eeda.numFormat(str.toFixed(2),3);
				  }
	            },
	            { "data": "ROYALTY_RATE", "width": "80px","class":"royalty_rate"},
	            { "width": "100px","className":"commission_money",
					"render": function(data, type, full, meta) {
							var str=0;
		            		
		            		var sum_charge_total=0;
		            		var sum_cost_total=0;
		            		if(full.SUM_CHARGE_TOTAL){
		            			sum_charge_total=full.SUM_CHARGE_TOTAL;
		            		}
		            		if(full.SUM_COST_TOTAL){
		            			sum_cost_total=full.SUM_COST_TOTAL;
		            		}
		            		str = sum_charge_total-sum_cost_total;
		            		
		            		var royalty_rate = 0;
		            		if(full.ROYALTY_RATE){
		            			royalty_rate = full.ROYALTY_RATE;
		            		}else{
		            			return ""
		            		}
		            		var commission_money = (str*royalty_rate)/100;
		            		if(commission_money<0){
		            	    	return '<span style="color:red;">'+eeda.numFormat(commission_money.toFixed(2),3)+'</span>';
		            	    }
		            		
							return eeda.numFormat(commission_money.toFixed(2),3);
					}
				}
	            
	          ]
	      });

      //base on config hide cols
      dataTable.columns().eq(0).each( function(index) {
          var column = dataTable.column(index);
          $.each(cols_config, function(index, el) {
              
              if(column.dataSrc() == el.COL_FIELD){
                
                if(el.IS_SHOW == 'N'){
                  column.visible(false, false);
                }else{
                  column.visible(true, false);
                }
              }
          });
      });
      
      $('#resetBtn').click(function(e){
          $("#orderForm")[0].reset();
      });

      $('#searchBtn').click(function(){
          searchData(); 
      })
      
      var cssTd=function(){
//    	  $("#eeda_table td:nth-child(6)").css('background-color','#f5f5dc');
//    	  $("#eeda_table td:nth-child(14)").css('background-color','#f5f5dc');
      }
      
      
	
     var searchData=function(){
          var customer_id = $("#customer_id").val();
          var abbr_name=$('#customer_id_input').val();
          var order_export_date_begin_time = $("#order_export_date_begin_time").val();
          var order_export_date_end_time = $("#order_export_date_end_time").val();
          var user_name = $("#user_id_input").val();
          
          
        //合计字段
          $.post('salesBillReport/listTotal',{
        	  customer_id:customer_id,
        	  order_export_date_begin_time:order_export_date_begin_time,
        	  order_export_date_end_time:order_export_date_end_time
          },function(data){
      	  
        	  var sum_foot_charge_total = parseFloat(data.SUM_FOOT_CHARGE_TOTAL).toFixed(2);
        	  var sum_foot_cost_total = parseFloat(data.SUM_FOOT_COST_TOTAL).toFixed(2);
        	  var sum_foot_gross_profit = parseFloat(data.SUM_FOOT_GROSS_PROFIT).toFixed(2);
        	  var sum_foot_pay_charge_total = parseFloat(data.SUM_FOOT_PAY_CHARGE_TOTAL).toFixed(2);
        	  var sum_foot_current_profit = parseFloat(data.SUM_FOOT_CURRENT_PROFIT).toFixed(2);
        	  var foot_commission_money = parseFloat(data.FOOT_COMMISSION_MONEY).toFixed(2);

        	  var total=parseFloat(data.TOTAL);

        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=order_no]').html('共'+total+'项汇总：');
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=sum_charge_total]').html("折合应收CNY:<br>"+eeda.numFormat(sum_foot_charge_total,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=sum_pay_charge_total]').html("折合实收CNY:<br>"+eeda.numFormat(sum_foot_pay_charge_total,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=sum_cost_total]').html("折合应付CNY:<br>"+eeda.numFormat(sum_foot_cost_total,3));
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=gross_profit]').html("毛利润CNY:<br>"+eeda.numFormat(sum_foot_gross_profit,3));
        	  if(sum_foot_current_profit<0){
        		  $($('.dataTables_scrollFoot tr')[0]).find('th[class=current_profit]').html("当前盈亏CNY:<br>"+eeda.numFormat(sum_foot_current_profit,3)).css("color","red");
        	  }else{
        		  $($('.dataTables_scrollFoot tr')[0]).find('th[class=current_profit]').html("当前盈亏CNY:<br>"+eeda.numFormat(sum_foot_current_profit,3)).css("color","");; 
        	  }
        	  $($('.dataTables_scrollFoot tr')[0]).find('th[class=commission_money]').html("提成金额CNY:<br>"+eeda.numFormat(foot_commission_money,3));
          });
          /*  
              查询规则：参数对应DB字段名
              *_no like
              *_id =
              *_status =
              时间字段需成双定义  *_begin_time *_end_time   between
          */
          var url = "/salesBillReport/list?customer_id="+customer_id
          				+"&abbr_equals="+abbr_name
          				+"&user_name_equals="+user_name  
          				+"&order_export_date_begin_time="+order_export_date_begin_time
				        +"&order_export_date_end_time="+order_export_date_end_time;
          dataTable.ajax.url(url).load();
      };
      searchData();
  });
});