
$(document).ready(function() {

    $('#menu_finance').addClass('active').find('ul').addClass('in');

	//datatable, 动态处理
    var invoiceApplicationOrderIds = $("#invoiceApplicationOrderIds").val();
    var total = 0.00;
    var datatable=$('#chargeConfirm_table').dataTable({
    	"bProcessing": true, //table载入数据时，是否显示‘loading...’提示
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 100,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/chargeConfirm/list",
        "aoColumns": [
            {"mDataProp":"ORDER_NO", "sWidth": "100px", //付款确认单号
            	"fnRender": function(obj) {
        			return "<a href='/chargeConfirm/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
    				$("#total_amount").val(total);
        		}
            },
            {"mDataProp": null , "sWidth": "100px",//付款申请单号
            	"fnRender": function(obj) {
            		if(obj.aData.SKSQ_NO != null){
            			return obj.aData.SKSQ_NO;
            		}else if(obj.aData.SKDZ_NO != null){
            			return obj.aData.SKDZ_NO;
            		}else if(obj.aData.WLPJ_NO != null){
            			return obj.aData.WLPJ_NO;
            		}else{
            			return obj.aData.MISC_NO;
            		}
            	}
            }, 
            {"mDataProp":"STATUS", "sWidth": "60px"},//状态
            {"mDataProp":"CUSTOMER_NAME", "sWidth": "100px"},//客户
            {"mDataProp":"SP_NAME", "sWidth": "100px"},//供应商
            {"mDataProp":"RECEIVE_PERSON","sWidth": "80px"},//收款人
            {"mDataProp":"PAY_AMOUNT","sWidth": "60px"},//付款金额  
            {"mDataProp":"ALREADY_PAY","sWidth": "60px"},//已付金额
            {"mDataProp":null,"sWidth": "60px", //未付金额
                "fnRender": function(obj) {
                    return obj.aData.PAY_AMOUNT - obj.aData.ALREADY_PAY;
                }    
            },
            {"mDataProp":"REMARKS","sWidth": "100px"},//备注
            {"mDataProp":"USER_NAME","sWidth": "60px"},//创建人
            {"mDataProp":"CREATE_DATE","sWidth": "80px",
                "fnRender":function(obj){
                    var create_stamp=obj.aData.CREATE_DATE;
                    var str=create_stamp.substr(0,10);
                    return str;
                }
            },//创建时间           
        ]      
    });	

    //供应商查询
    //获取供应商的list，选中信息在下方展示其他信息
    $('#sp_filter').on('input click', function(){
        var me = this;
        var inputStr = $('#sp_filter').val();
        var spList =$("#spList");
        $.get('/transferOrder/searchSp', {input:inputStr}, function(data){
            if(inputStr!=$('#sp_filter').val()){//查询条件与当前输入值不相等，返回
                return;
            }
            spList.empty();
            for(var i = 0; i < data.length; i++){
                var abbr = data[i].ABBR;
                var company_name = data[i].COMPANY_NAME;
                var contact_person = data[i].CONTACT_PERSON;
                var phone = data[i].PHONE;
                
                if(abbr == null) 
                    abbr = '';
                if(company_name == null)
                    company_name = '';
                if(contact_person == null)
                    contact_person = '';
                if(phone == null)
                    phone = '';
                
                spList.append("<li><a tabindex='-1' class='fromLocationItem' chargeType='"+data[i].CHARGE_TYPE+"' partyId='"+data[i].PID+"' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' spid='"+data[i].ID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+abbr+" "+company_name+" "+contact_person+" "+phone+"</a></li>");
            }
            spList.css({ 
                left:$(me).position().left+"px", 
                top:$(me).position().top+28+"px" 
            }); 
            
            spList.show();
            
        },'json');
        
        
    });
    
    // 没选中供应商，焦点离开，隐藏列表
    $('#sp_filter').on('blur', function(){
        $('#spList').hide();
    });

    //当用户只点击了滚动条，没选供应商，再点击页面别的地方时，隐藏列表
    $('#spList').on('blur', function(){
        $('#spList').hide();
    });

    $('#spList').on('mousedown', function(){
        return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
    });

    // 选中供应商
    $('#spList').on('mousedown', '.fromLocationItem', function(e){
        console.log($('#spList').is(":focus"));
        var message = $(this).text();
        $('#sp_filter').val(message.substring(0, message.indexOf(" ")));
        $('#sp_id').val($(this).attr('partyId'));
        var pageSpName = $("#pageSpName");
        pageSpName.empty();
        var pageSpAddress = $("#pageSpAddress");
        pageSpAddress.empty();
        pageSpAddress.append($(this).attr('address'));
        var contact_person = $(this).attr('contact_person');
        if(contact_person == 'null')
            contact_person = '';
        pageSpName.append(contact_person+'&nbsp;');
        var phone = $(this).attr('phone');
        if(phone == 'null')
            phone = '';
        pageSpName.append(phone); 
        pageSpAddress.empty();
        var address = $(this).attr('address');
        if(address == 'null')
            address = '';
        pageSpAddress.append(address);
        $('#spList').hide();
        refreshData();
    });


    $('#datetimepicker').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();   
        $('#beginTime_filter').trigger('keyup');
    });

    $('#datetimepicker2').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#endTime_filter').trigger('keyup');
    });
    
    $('#datetimepicker3').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();   
        $('#beginTime_filter2').trigger('keyup');
    });

    $('#datetimepicker4').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#endTime_filter2').trigger('keyup');
    });
    
    $('#datetimepicker5').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN'
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();   
        $('#beginTime_filter3').trigger('keyup');
    });

    $('#datetimepicker6').datetimepicker({  
        format: 'yyyy-MM-dd',  
        language: 'zh-CN', 
        autoclose: true,
        pickerPosition: "bottom-left"
    }).on('changeDate', function(ev){
        $(".bootstrap-datetimepicker-widget").hide();
        $('#endTime_filter3').trigger('keyup');
    });

    
    var refreshData = function(){
    	var receiveConfirnNo = $('#receiveConfirnNo_filter').val();
        var order_no = $('#orderNo').val();
        var status = $('#status_filter').val();
        var sp_name = $('#sp_filter').val();
        var customer_name = $('#customer_filter3').val();
        var receiverName = $('#receiver_filter').val();
        var beginTime = $('#beginTime_filter3').val();
        var endTime = $('#endTime_filter3').val();
        datatable.fnSettings().sAjaxSource = "/chargeConfirm/list?orderNo="+order_no
            +"&status="+status
            +"&receiveConfirnNo="+receiveConfirnNo
            +"&sp_name="+sp_name
            +"&receiverName="+receiverName
            +"&customer_name="+customer_name
            +"&beginTime="+beginTime
            +"&endTime="+endTime;
            
        datatable.fnDraw(); 
    };
    
    
    $('#query_btn').click(function(){
    	refreshData();
    });
    
    
    $('#receiveConfirnNo_filter,#orderNo,#sp_filter,#customer_filter3,#receiver_filter,#beginTime_filter3,#endTime_filter3').on('keyup',function(){
    	refreshData();
    });
    
    
    
    
    //已收款界面的客户
    //获取客户列表，自动填充
      $('#customer_filter3').on('keyup click', function(event){
          var me = this;
          var inputStr = $('#customer_filter3').val();
          var companyList3 =$("#companyList3");
          $.get("/transferOrder/searchCustomer", {input:inputStr}, function(data){
              companyList3.empty();
              for(var i = 0; i < data.length; i++)
                  companyList3.append("<li><a tabindex='-1' class='fromLocationItem3' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
                  
              companyList3.css({ 
  		    	left:$(me).position().left+"px", 
  		    	top:$(me).position().top+32+"px" 
  		    });
  	        companyList3.show();    
          },'json');  
      });
      
      $('#companyList3').on('click', '.fromLocationItem3', function(e){        
          $('#customer_filter3').val($(this).text());
          $("#companyList3").hide();
      	refreshData();
      });
      // 没选中客户，焦点离开，隐藏列表
      $('#customer_filter3').on('blur', function(){
          $('#companyList3').hide();
      });


} );