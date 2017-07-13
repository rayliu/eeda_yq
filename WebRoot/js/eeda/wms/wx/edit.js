$(function(){
	var searchPartData = function(order_no,type){
		$("#loadingToast").css("display","block");
    	$('#dataValue').text("");
    	$.post("/wx/queryPartNo",{order_no:order_no,type:type},function(data){
    		if(data.length > 0){
    			$('#dataValue').css("text-align","left");
    			for(var i = 0 ;i<data.length;i++){
    				$('#dataValue').append('<a id="" class="weui-cell weui-cell_access" href="/wx/showImg?'
    					+'part_no='+data[i].PART_NO+'&total='+data[i].TOTAL+'&totalQuantity='+data[i].TOTALQUANTITY
    					+'&part_name='+data[i].PART_NAME+'">'
        	            +'<div class="weui-cell__bd">'
        	            +'<p id="">'+data[i].PART_NO+'</p>'
        	            +'<p id="">'+data[i].PART_NAME+'</p>'
        	            +'</div>'
        	            +'<div class="weui-cell__ft">'+data[i].TOTALQUANTITY+'件</div>'
        	            +'</a>'); 
    			}
    		}else if(data.length == 0){
    			$('#dataValue').html("<div class='weui-cell' ><center>无数据</center></div>");
    		}
    		$("#loadingToast").css("display","none");
    	});
	};
	
	
	var searchItemData = function(item_no){
		$("#loadingToast").css("display","block");
    	$('#dataValue').text("");
    	$.post("/wx/queryItemNo",{item_no:item_no},function(data){
    		if(data.length > 0){
    			$('#dataValue').css("text-align","left");
    			for(var i = 0 ;i<data.length;i++){
    				$('#dataValue').append('<a id="" class="weui-cell weui-cell_access itemNo" item_no='+data[i].ITEM_NO+' ">'
        	            +'<div class="weui-cell__bd">'
        	            +'<p id="">'+data[i].ITEM_NO+'</p>'
        	            +'</div>'
        	            +'</a>'); 
    			}
    		}else if(data.length == 0){
    			$('#dataValue').html("<div class='weui-cell' ><center>无数据</center></div>");
    		}
    		$("#loadingToast").css("display","none");
    	});
	};
	
	$('#dataValue').on('click','.itemNo', function(){
		var item_no = $(this).attr('item_no');
		searchPartData(item_no,'item_no');
	});
	
	
    var $tooltips = $('.js_tooltips');
    $('#queryPartNo').on('click', function(){
    	var item_no = $('#item_no').val().trim();
    	var part_no = $('#part_no').val().trim();
        if(item_no.length>0 || part_no.length>0){
        	if(part_no.length>0){
        		localStorage.setItem('part_no', part_no);
            	searchPartData(part_no,'part_no');
        	}else{
        		localStorage.setItem('item_no', item_no);
            	searchItemData(item_no);
        	}
        }else{
            $('#toast').css("display","block");
            $('.msg').text("请输入Part No");
            setTimeout(function () {
            	$('#toast').css("display","none");
            }, 2000);
        }
    });
    
    $('#qr_btn').on('click', function(){
         wx.scanQRCode({
            needResult: 1, // 默认为0，扫描结果由微信处理，1则直接返回扫描结果，
            scanType: ["qrCode","barCode"], // 可以指定扫二维码还是一维码，默认二者都有
            success: function (res) {
                var result = res.resultStr; // 当needResult 为 1 时，扫码返回的结果 */
                var part_no = result.split('(')[4].substring(0,result.split('(')[4].length-1);
                //
                $('#part_no').val(part_no);
                localStorage.setItem('part_no', part_no);
            	searchPartData(part_no,'part_no');
            }
        });
    });
                
    var loc_part_no = localStorage.getItem('part_no');
    var loc_item_no = localStorage.getItem('item_no');
    $('#part_no').val(loc_part_no);
	$('#item_no').val(loc_item_no);
	if(loc_part_no!=null || loc_item_no!=null){
		if(loc_part_no!=null){
        	searchPartData(loc_part_no,'part_no');
    	}else if(loc_item_no!=null){
        	searchItemData(loc_item_no);
    	}
		
    }
	
	
	$('#clear_btn').click(function(){
		$('#dataValue').text('');
		$('#part_no').val('');
		$('#item_no').val('');
		localStorage.clear();
	});
   
});