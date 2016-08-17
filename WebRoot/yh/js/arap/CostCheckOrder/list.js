
$(document).ready(function() {
	document.title = '应付对账单查询 | '+document.title;

    $('#menu_cost').addClass('active').find('ul').addClass('in');
    var datatable=$('#costCheckList-table').dataTable({
        "bProcessing": true, 
        "bFilter": false, //不需要默认的搜索框
        "sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
    	  "oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "sAjaxSource": "/costCheckOrder/list",
        "aoColumns": [   
            {"mDataProp":"ORDER_NO",
            	"fnRender": function(obj) {
        			return "<a href='/costCheckOrder/edit?id="+obj.aData.ID+"'target='_blank'>"+obj.aData.ORDER_NO+"</a>";
        		}},
            {"mDataProp":"ORDER_STATUS"},
            {"mDataProp":"C_STAMP"},
            {"mDataProp":"ONAME"},
            {"mDataProp":"CNAME"},
            {"mDataProp":"SERIAL_NO"},
            {"mDataProp":null},
            {"mDataProp":"TOTAL_AMOUNT"},
            {"mDataProp":null},
            {"mDataProp":"DEBIT_AMOUNT"},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":null},
            {"mDataProp":"COST_AMOUNT"},
            {"mDataProp":"REMARK"},
            {"mDataProp":"CREATOR_NAME"},        	
            {"mDataProp":"CREATE_STAMP"}, 
            { 
                "mDataProp": null, 
                "sWidth": "8%",                
                "fnRender": function(obj) {
                    return	"<a class='btn btn-danger' href='#'"+obj.aData.ID+"'>"+
                                "<i class='fa fa-trash-o fa-fw'></i>"+ 
                                "取消"+
                            "</a>";
                }
            }                         
        ]      
    });	
    
    /*--------------------------------------------------------------------*/
    //获取所有客户
    $('#customer_filter').on('keyup click', function(){
           var inputStr = $('#customer_filter').val();
           
           $.get("/customerContract/search", {locationName:inputStr}, function(data){
               //console.log(data);
               var companyList =$("#companyList");
               companyList.empty();
               for(var i = 0; i < data.length; i++)
               {
                   companyList.append("<li><a tabindex='-1' class='fromLocationItem' post_code='"+data[i].POSTAL_CODE+"' contact_person='"+data[i].CONTACT_PERSON+"' email='"+data[i].EMAIL+"' phone='"+data[i].PHONE+"' partyId='"+data[i].PID+"' address='"+data[i].ADDRESS+"', company_name='"+data[i].COMPANY_NAME+"', >"+data[i].ABBR+"</a></li>");
               }
               if(data.length>0)
                   companyList.show();
               
           },'json');
       });



   //选中某个客户时候
      $('#companyList').on('click', '.fromLocationItem', function(e){        
           $('#customer_filter').val($(this).text());
           $("#companyList").hide();
           var companyId = $(this).attr('partyId');
           $('#customerId').val(companyId);
          
       });
    // 没选中客户，焦点离开，隐藏列表
       $('#customer_filter').on('blur', function(){
           $('#companyList').hide();
       });

       //当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
       $('#companyList').on('blur', function(){
           $('#companyList').hide();
       });

       $('#companyList').on('mousedown', function(){
           return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
       });

       	$('#sp_id1_list').on('mousedown', function(){
       		return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
       	});

       	// 选中供应商
       	$('#sp_id1_list').on('mousedown', function(e){
               refreshList();
           });
        $('#datetimepicker3').datetimepicker({  
            format: 'yyyy-MM-dd',  
            language: 'zh-CN',
            autoclose: true,
            pickerPosition: "bottom-left"
        }).on('changeDate', function(ev){
            $(".bootstrap-datetimepicker-widget").hide();
            $('#kaishi_filter').trigger('keyup');
        });


        $('#datetimepicker4').datetimepicker({  
            format: 'yyyy-MM-dd',  
            language: 'zh-CN', 
            autoclose: true,
            pickerPosition: "bottom-left"
        }).on('changeDate', function(ev){
            $(".bootstrap-datetimepicker-widget").hide();
            $('#jieshu_filter').trigger('keyup');
        });
        var refreshList = function(){
        	var order_no = $("#order_no").val();
          	var status = $("#status_filter").val();
        	var sp = $("#sp_id1_input").val();
        	var serial_no = $("#serial_no_filter").val();
        	datatable.fnSettings().sAjaxSource = "/costCheckOrder/list?order_no="+order_no
                                +"&status="+status
                                +"&sp="+sp
                                +"&serial_no="+serial_no;
        	datatable.fnDraw();
        };

        $("#order_no, #sp_id1_input,  #customer_filter, #serial_no_filter").on('keyup',function(){
        	refreshList();
        });

        $("#status_filter").on('change',function(){
          refreshList();
        });
} );