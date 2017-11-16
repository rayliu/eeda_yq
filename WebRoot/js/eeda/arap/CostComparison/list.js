define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco', 'jq_blockui','dtColReorder', 'datetimepicker_CN' ,'pageguide'], function ($, metisMenu) {
$(document).ready(function() {
	tl.pg.init({
        pg_caption: '本页教程'
    });

    var CostComparison_table = eeda.dt({
        id: 'CostComparison_table',
        autoWidth: false,
        colReorder: true,
        scrollY: 530,
        scrollCollapse: true,
        paging: true,
        drawCallback: function( settings ) {
        },
        initComplete: function (settings) {
            eeda.dt_float_header('CostComparison_table');
        },
        serverSide: true, 
        ajax: "/costComparison/list?status=新建",
          columns: [
	        { "width": "10px", "orderable": false,
	            "render": function ( data, type, full, meta ) {
	                var strcheck='<input type="checkbox" class="checkBox" name="order_check_box" value="'+full.ID+'">';
	                return strcheck;
	            }
	        },
            {"data":"ORDER_NO",
                "render": function(data, type, full, meta) {
                	var other ='';
	               	 if(!data){
	               		data='';
	              	  	}
                    return "<a href='/costComparison/edit?id="+full.ID+"'target='_self'>"+data+"</a>";
                }
            },
            {"data":"ORDER_NAME",
                "render": function(data,type,full,mate){
                    if(!data){
                    	data='';
                    }                        
                    return data;
                }   
            },
            {"data":"TYPE",
                "render": function(data,type,full,mate){
                if(!data){
                	data='';
                }  
                return data;
              }   
            },
            {"data":"POL_NAME",
          	  "render":function(data,type,full,meta){
          		  if(!data){
          			  data='';
          		  }
          		  return data;
          	  	}
            },
            {"data":"POD_NAME",
            	  "render":function(data,type,full,meta){
            		  if(!data){
            			  data='';
            		  }
            		  return data;
            	  	}
            },
            {"data":"CREATOR_NAME",
          	  "render":function(data,type,full,meta){
          		  if(!data){
          			  data='';
          		  }
          		  return data;
          	  	}
            },
            {"data":"CREATE_STAMP",
            	  "render":function(data,type,full,meta){
            		  if(!data){
            			  data='';
            		  }
            		  return data;
            	  }
            },
            {"data":"COMMENT",'class':'hkd',
                "render": function(data, type, full, meta) {
                    if(!data){
                    	data='';
                    }
                    return data;
                }
            }
        ]      
    });

    
     


      //查询已申请单
    $("#searchBtn").click(function(){
    	$('#checked_CostComparison_table').empty();
        
        back="";
        refreshData(back);
    });

    $("#resetBtn").click(function(){
        $('#CostComparisonForm')[0].reset();
        saveConditions();
    });
    
    
    //保留查询条件
    var saveConditions=function(){
        var conditions={
                sp_id:$('#sp_id').val(),
                
                payee_company:$('#sp_id_input').val().trim(),
                  
                charge_order_no : $('#orderNo').val().trim(), 
                CostComparisonOrderNo : $('#CostComparisonOrderNo').val(),
                status2 : $('#status2').val().trim(),
                fee_type : $('#fee_type').val().trim(),
                service_stamp : $('#service_stamp').val(),
                
                begin_date_begin_time : $('#begin_date_begin_time').val(),
                begin_date_end_time : $('#begin_date_end_time').val(),
                
                check_begin_date_begin_time : $('#check_begin_date_begin_time').val(),
                check_begin_date_end_time : $('#check_begin_end_begin_time').val(),
                
                confirmBegin_date_begin_time : $('#confirmBegin_date_begin_time').val(),
                confirmBegin_date_end_time : $('#confirmBegin_date_end_time').val()
        };
        if(!!window.localStorage){//查询条件处理
            localStorage.setItem("query_to", JSON.stringify(conditions));
        }
    };

    
    
    
    
    
    
    
    
    
    

    
    
    
    //简单查询
    $('#singleSearchBtn').click(function(){
    	$('#checked_CostComparison_table').empty();
    	singleSearchData();
    });
	var singleSearchData = function(){ 
		var sp_id = $("#single_sp_id").val();
		var service_stamp = $("#service_stamp_n").val();
		var status = $("#status").val();
	    var url = "/costComparison/CostComparisonList?sp_id="+sp_id
	     		 +"&service_stamp_between="+service_stamp
	     		 +"&status="+encodeURI(status);
	     CostComparison_table.ajax.url(url).load();
	     totalMoney();
	}

    //查询动作
    var refreshData=function(back){
          var sp_id = $('#sp_id').val();
          var sp_code = $('#sp_code').val();
          var payee_company = $('#sp_id_input').val().trim();
          
          var charge_order_no = $('#orderNo').val().trim(); 
          var CostComparisonOrderNo = $('#CostComparisonOrderNo').val();
//          if(back=="true"){
//              $('#status2').val("新建");
//            }
//          if(back=="confirmTrue"){
//              $('#status2').val("已复核");
//            }
          var status2 = $('#status2').val().trim();
          
          var service_stamp = $('#service_stamp').val();
          var fee_type = $('#fee_type').val();
          var invoices_no = $('#invoices_no').val();
          
          var begin_date_begin_time = $('#begin_date_begin_time').val();
          var begin_date_end_time = $('#begin_date_end_time').val();
          
          var check_begin_date_begin_time = $('#check_begin_date_begin_time').val();
          var check_begin_date_end_time = $('#check_begin_date_end_time').val();
          
          var confirmBegin_date_begin_time = $('#confirmBegin_date_begin_time').val();
          var confirmBegin_date_end_time = $('#confirmBegin_date_end_time').val();

          var url = "/costComparison/CostComparisonList?sp_id="+sp_id
            +"&code_like="+sp_code  
           +"&payee_company_equals="+payee_company  
            +"&charge_order_no="+charge_order_no
            +"&CostComparison_order_no="+CostComparisonOrderNo
            +"&status="+encodeURI(status2)
            +"&fee_type="+fee_type
            +"&invoices_no_like="+invoices_no
            +"&service_stamp_between="+service_stamp

            +"&create_stamp_begin_time="+begin_date_begin_time
            +"&create_stamp_end_time="+begin_date_end_time
            
            +"&check_stamp_begin_time="+check_begin_date_begin_time
            +"&check_stamp_end_time="+check_begin_date_end_time
            
            +"&receive_time_begin_time="+confirmBegin_date_begin_time
            +"&receive_time_end_time="+confirmBegin_date_end_time;
       CostComparison_table.ajax.url(url).load();
       totalMoney();
       saveConditions();
    };
    
    //查询条件处理
    var loadConditions=function(){
        if(!!window.localStorage){//查询条件处理
            var query_to = localStorage.getItem('query_to');
            if(!query_to)
                return;

            var conditions = JSON.parse(query_to);
            $("#sp_id").val(conditions.sp_id);
            $("#sp_id_input").val(conditions.payee_company);
            $("#orderNo").val(conditions.charge_order_no);
            $("#CostComparisonOrderNo").val(conditions.CostComparisonOrderNo);
            $("#status2").val(conditions.status2);
            $("#fee_type").val(conditions.fee_type);
            $("#service_stamp").val(conditions.service_stamp);
            $("#begin_date_begin_time").val(conditions.begin_date_begin_time);
            $("#begin_date_end_time").val(conditions.begin_date_end_time);
            $("#check_begin_date_begin_time").val(conditions.check_begin_date_begin_time);
            $("#check_begin_date_end_begin_time").val(conditions.check_begin_date_end_begin_time);
            $("#confirmBegin_date_begin_time").val(conditions.confirmBegin_date_begin_time);
            $("#confirmBegin_date_end_time").val(conditions.confirmBegin_date_end_time);
        }
    };

    
    if(back=="true"||back=="confirmTrue"){
        refreshData(back);
    }else{
        $('#CostComparisonForm')[0].reset();
    }   

});
});