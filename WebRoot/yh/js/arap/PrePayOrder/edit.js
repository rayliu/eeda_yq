$(document).ready(function() {
	if(order_no){
		document.title = order_no +' | '+document.title;
	}else{
		document.title = "创建预付单" +' | '+document.title;
	}
	$('#menu_finance').addClass('active').find('ul').addClass('in');
	
	var saveCostMiscOrder = function(e, callback){
		//阻止a 的默认响应行为，不需要跳转
		e.preventDefault();
		//提交前，校验数据
        if(!$("#orderForm").valid()){
	       	return;
        }
        
        
        var tableRows = $("#feeItemList-table tr");
        var itemsArray=[];
        for(var index=0; index<tableRows.length; index++){
        	if(index==0)
        		continue;

        	var row = tableRows[index];
        	var id = $(row).attr('id');
        	if(!id){
        		id='';
        	}

        	var item={
        		ID: id,
			 	ITEM_DESC: $(row.children[0]).find('input').val(),
			 	FIN_ITEM_ID: $(row.children[1]).find('select').val(),
			 	AMOUNT: $(row.children[2]).find('input').val(),
			 	ACTION: 'create'
        	};
        	itemsArray.push(item);
        }

		var amount = 0.00;
        for(var i=0; i<itemsArray.length; i++){
        	amount+=Number(itemsArray[i].AMOUNT);
        }
        $('#totalAmountSpan').html(amount);

        //add deleted items
        for(var index=0; index<deletedIds.length; index++){
        	var id = deletedIds[index];
        	var item={
        		ID: id,
			 	ACTION: 'DELETE'
        	};
        	itemsArray.push(item);
        }

        var order={
        	orderId: $('#orderId').val(),
        	sp_id: $('#sp_id').val(),
        	ref_no: $('#ref_no').val(),
        	remark: $('#remark').val(),
        	amount: amount,
        	items: itemsArray
        };
        console.log(order);
        
		//异步向后台提交数据
		$.post('/costPrePayOrder/save',{params:JSON.stringify(order)}, function(data){
			if(data.ID>0){
				$("#orderNo").html(data.ORDER_NO);
				$("#ref_order_no").text(data.REF_ORDER_NO);
				$("#create_time").html(data.CREATE_STAMP);
				$("#costMiscOrderId").val(data.ID);
				$.scojs_message('保存成功', $.scojs_message.TYPE_OK);
				$("#saveOrderBtn").attr("disabled",false);
				contactUrl("edit?id", data.ID);
				deletedIds=[];
				//callback(data.ID);//回调函数，确保主表保存成功，有ID，再插入从表
			}else{
				$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
			}
		},'json').fail(function(){
			$.scojs_message('保存失败', $.scojs_message.TYPE_ERROR);
		});
	};
   
	/*--------------------------------------------------------------------*/
	
	
	//设置一个变量值，用来保存当前的ID
	var parentId = "costMiscOrderbasic";
	$("#transferOrderMilestoneList").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	$("#costMiscOrderbasic").click(function(e){
		parentId = e.target.getAttribute("id");
	});
	/*--------------------------------------------------------------------*/
	//点击保存的事件，保存运输单信息
	//transferOrderForm 不需要提交	
 	$("#saveOrderBtn").click(function(e){
 		$("#saveOrderBtn").attr("disabled", true);
 		saveCostMiscOrder(e);
	});
	
    if($("#costMiscOrderStatus").text() == 'new'){
    	$("#costMiscOrderStatus").text('新建');
	}
    
    var feeTable = $('#feeItemList-table').dataTable({    	
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
    	"bPaginate": false, //翻页功能
        "bInfo": false,//页脚信息
        "bFilter": false, //不需要默认的搜索框
    	"bSort": false, // 不要排序
    	"oLanguage": {
    		"sUrl": "/eeda/dataTables.ch.txt"
    	},
    	//"sAjaxSource": "/costMiscOrder/costMiscOrderItemList?costMiscOrderId="+$("#costMiscOrderId").val(),
        "fnRowCallback": function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},		
        "aoColumns": [
            {"mDataProp":"ITEM_DESC","sWidth": "130px",
        	    "fnRender": function(obj) {
			        if(obj.aData.ITEM_DESC!='' && obj.aData.ITEM_DESC != null){
			            return "<input type='text' name='item_desc' value='"+obj.aData.ITEM_DESC+"'  class='form-control search-control'>";
			        }else{
			        	 return "<input type='text'  name='item_desc' class='form-control search-control'>";
			        }
			    }
        	},
			{"mDataProp":"NAME","sWidth": "70px",
			    "fnRender": function(obj) {
			        if(obj.aData.NAME!='' && obj.aData.NAME != null){
			        	var str="";
			        	$("#receivableItemList").children().each(function(){
			        		if(obj.aData.NAME == $(this).text()){
			        			str+="<option value='"+$(this).val()+"' selected = 'selected'>"+$(this).text()+"</option>";   
			        			$("#receivableTotal").val(obj.aData.NAME);
			        		}else{
			        			str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        		}
			        	});
			            return "<select name='fin_item_id' class='form-control search-control'>"+str+"</select>";
			        }else{
			        	var str="";
			        	$("#receivableItemList").children().each(function(){
			        		str+="<option value='"+$(this).val()+"'>"+$(this).text()+"</option>";
			        	});
			        	return "<select name='fin_item_id' class='form-control search-control'>"+str+"</select>";
			        }
			 }},
			 {"mDataProp":"AMOUNT",
			    "fnRender": function(obj) {
			        if(obj.aData.AMOUNT!='' && obj.aData.AMOUNT != null){
			            return "<input type='text' name='amount' value="+obj.aData.AMOUNT+" class='form-control search-control'>";
			        }else{
			        	return "<input type='text' name='amount' class='form-control search-control'>";
			        }
			}},
			{"mDataProp":"CHANGE_AMOUNT","sWidth": "120px",},
			{"mDataProp":"STATUS"},
			{"mDataProp": null,
                "fnRender": function(obj) {
               		 return	"<a class='btn btn-danger finItemdel' code='"+obj.aData.ID+"'><i class='fa fa-trash-o fa-fw'> </i>删除</a>";
                }
            }   
        ]      
    });
   
 
    var deletedIds=[];
    //删除一行
	$("#feeItemList-table").on('click', '.finItemdel', function(e){
		e.preventDefault();
		var tr = $(this).parent().parent();
		deletedIds.push(tr.attr('id'))
		tr.remove();
	});	
	
	//添加一行
	$("#addFee").click(function(){
		 feeTable.fnAddData({
		 	ITEM_DESC:'',
		 	NAME:'',
		 	AMOUNT: '0',
		 	CHANGE_AMOUNT: '',
		 	STATUS: '新建'
		 });
		 feeTable.fnDraw(); 
	});	

	    
    

    //供应商查询
    //获取供应商的list，选中信息在下方展示其他信息
    $('#sp_filter').on('keyup click', function(){
		var inputStr = $('#sp_filter').val();
		var spList =$("#spList");
		$.get('/serviceProvider/searchSp', {input:inputStr}, function(data){
			spList.empty();
			for(var i = 0; i < data.length; i++){
				var abbr = data[i].ABBR;
				var company_name = data[i].COMPANY_NAME;
				if(abbr == null) 
					abbr = '';
				if(company_name == null)
					company_name = '';
				spList.append("<li><a tabindex='-1' class='fromLocationItem' partyId='"+data[i].PID+"' company_name='"+company_name+"'>"+abbr+" "+company_name+"</a></li>");
			}
		},'json');
		spList.css({left:$(this).position().left+"px",top:$(this).position().top+32+"px"}).show();
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
		$('#sp_filter').val($(this).attr('company_name'));
		var provider = $(this).attr('partyId');
		$('#sp_id').val(provider);
        $('#spList').hide();
    });
    
});

function datetimepicker(data){
	if(!$("#saveCarSummaryBtn").prop("disabled")){
		$('.input-append').datetimepicker({  
			    format: 'yyyy-MM-dd',  
			    language: 'zh-CN',
			    autoclose: true,
			    pickerPosition: "bottom-left"
			}).on('changeDate', function(ev){
				$(".bootstrap-datetimepicker-widget").hide();
				$(data).parent().prev("input").focus();
		});
	}
}


//按钮控制
var status = $("#status").val();
if(status == '' || status == '新建' || status == 'new'){
	$("#saveOrderBtn").attr("disabled",false);
}else{
	$("#saveOrderBtn").attr("disabled",true);
}
