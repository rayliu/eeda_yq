define(function(){
var eeda={};
window.eeda =eeda;
//dataTables builder for 1.10
eeda.dt = function(opt){
    var option = {
        processing: opt.processing || true,
        searching: opt.searching || false,
        paging: opt.paging || false,
        //"serverSide": true,
        scrollX: opt.scrollX || true,
        responsive: true,
        //scrollY: opt.scrollY || true, //"300px",
        //scrollCollapse: opt.scrollCollapse || true,
        autoWidth: opt.autoWidth || false,
        aLengthMenu: [ [10, 25, 50, 100, -1], [10, 25, 50, 100, "All"] ],
        language: {
            "url": "/js/lib/datatables/i18n/Chinese.json"
        },
        createdRow: opt.createdRow || function ( row, data, index ) {
            $(row).attr('id', data.ID);
        },
        drawCallback: opt.drawCallback || function ( settings ) {},
        ajax: opt.ajax || '',
        columns: opt.columns || []
    };

    var dataTable = $('#'+opt.id).DataTable(option);

    return dataTable;
}

var refreshUrl=function(url){
  	var state = window.history.state;
  	if(state){
  		window.history.replaceState(state, "", url);
  	}else{
  		window.history.pushState({}, "", url);
  	}
 };

eeda.refreshUrl = refreshUrl;
 
 var contactUrl=function(str,id){
	 refreshUrl(window.location.protocol + "//" + window.location.host+window.location.pathname.substring(0, window.location.pathname.lastIndexOf('/')+1)+str+"="+id);
 };

 eeda.contactUrl=contactUrl;

 eeda.urlAfterSave=function(str,id){
    var http = window.location.protocol;
    var path = window.location.host+window.location.pathname.substring(0, window.location.pathname.lastIndexOf('/')+1);
    refreshUrl( http+ "//" + path +str+"-"+id);
 };
 
 eeda.getUrlByNo= function(id, orderNo) {
 	var str = "";
 	 if(orderNo.indexOf("PS") == 0){//配送
         str = "<a href='/delivery/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("PC") == 0){//拼车
         str = "<a href='/pickupOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("HD") == 0){//回单
         str = "<a href='/returnOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("DC") == 0){//调车
         str = "<a href='/pickupOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("FC") == 0){//发车
         str = "<a href='/departOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("SGFK") == 0){//手工付款
         str = "<a href='/costMiscOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("SGSK") == 0){//手工收款
         str = "<a href='/chargeMiscOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("BX") == 0){//保险
         str = "<a href='/insuranceOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("XCBX") == 0){//行车报销
         str = "<a href='/costReimbursement/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("YFBX") == 0){//应付报销
         str = "<a href='/costReimbursement/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("YFSQ") == 0){//应付申请
         str = "<a href='/costPreInvoiceOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("YSSQ") == 0){//应收申请
         str = "<a href='/chargePreInvoiceOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("YFQR") == 0){//应付确认
         str = "<a href='/costConfirm/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("YSQR") == 0){//应收确认
         str = "<a href='/chargeConfirm/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("YSFP") == 0){//应收开票记录
         str = "<a href='/chargeInvoiceOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("YS") == 0){//配送
        str = "<a href='/transferOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("XC") == 0){
         str = "<a href='/carsummary/edit?carSummaryId="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("YSDZ") == 0){//应收对账
         str = "<a href='/chargeCheckOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("YFDZ")== 0){//应付对账
         str = "<a href='/costCheckOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("ZZSQ")== 0){//转账
         str = "<a href='/transferAccountsOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("WLPJ") == 0){//往来票据
         str = "<a href='/inOutMiscOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("YF") == 0){//预付
         str = "<a href='/costPrePayOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }else if(orderNo.indexOf("HSD") == 0){//预付
         str = "<a href='/damageOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
     }

     return str;
 };
 
 
 
  /**
  * JS格式化
  * @param number 要格式化的数字
  * @param d [0-9]位 逗号隔开
  */

  eeda.numFormat = function(number,d) {  
	  var numArrs = ['0','1','2','3','4','5','6','7','8','9'],
          REG_NUMBER = /^\d+(.\d+)?$/;

      d = d || 3, // 不传 是3位 千分位
      isMinus = false;
      
      if(number<0){
    	  number *= -1;
    	  isMinus = true;
      }; 

      if(isNumber(number) || isString(number) || REG_NUMBER.test(number)) {
    	  // 先转换成字符串
	      var toString = number + '',
	          isPoint = toString.indexOf('.'),
	          prefix,   // 前缀
	          suffix,   // 后缀
	          t = '';
	
	      if(isPoint > 0) {
	         prefix = toString.substring(0,isPoint);
	         suffix = toString.substring(isPoint + 1);
	
	      }else if(isPoint == 0) {
	             prefix = '';
	             suffix = toString.substring(1);
	      }else {
	             prefix = toString;
	             suffix = '';
	      }
	
	      if(prefix != '') {
	         prefixArr = prefix.split('').reverse();
	         var isArrayIndex = isArray(d,numArrs);
	         if(isArrayIndex > -1) {
	        	 for(var i = 0, ilen = prefixArr.length; i < ilen; i+=1) {
	                 t += prefixArr[i] + ((i + 1) % isArrayIndex == 0 && (i + 1) != prefixArr.length ? "," : "");
	             }
	             t = t.split("").reverse().join("");
	             
	             if(isMinus)        //判断是否为负数
  	            	 t = '-' + t;
	             
	             if(suffix != '') {
	                 return t + "." + suffix;
	             }else {
	                 return t;
	             }
	         }else {
	             return '传入的多少位不正确';
	         }
	      }else if(prefix == '' && suffix != ''){
              prefix = 0;
              return prefix + suffix;
	      }else {
	          return "有错误";
	      }
     }else {
         return '传入的要格式化的数字不符合';
     }
  };
  function isArray(item,arrs) {
      for(var i = 0, ilen = arrs.length; i < ilen; i++) {
          if(item == arrs[i]) {
              return i;
          }
      }
      return -1;
   }
   function isNumber(number) {
      return Object.prototype.toString.apply(number) === '[object Number]';
   }

   function isString(number) {
      return Object.prototype.toString.apply(number) === ['object String'];
   }
 


   window.onunload=function(){
      //页面刷新时调用，这里需要判断是否当前单据是否有更新，提示用户先保存
  	//暂时不处理 
   };
   
  eeda.bindTablePortField = function() {
      var companyList = $('#table_port_field_list');
      $('table input[name=port_input]').on('keyup click', function(event){
          var me = this;
          var inputField = $(this);
          var hiddenField = $(this).parent().find('input[field_type=port_id]');

          var inputStr = inputField.val();

           $.get("/location/searchPort", {portName:inputStr}, function(data){
             if(inputStr!=inputField.val()){//查询条件与当前输入值不相等，返回
               return;
             }
              companyList.empty();
              for(var i = 0; i < data.length; i++)
                  companyList.append("<li><a tabindex='-1' class='fromLocationItem' portId='"+data[i].ID
                    +"' code='"+data[i].CODE
                    +"', name='"+data[i].NAME+"', >"+data[i].NAME+"</a></li>");
              companyList.css({ 
                  left:$(me).offset().left+"px", 
                  top:$(me).offset().top+28+"px" 
              });
              companyList.show();
              companyList.inputField = inputField;
              companyList.hiddenField = hiddenField;
          },'json');
      });
      
      companyList.on('click', '.fromLocationItem', function(e){
          var hiddenField = companyList.hiddenField;
          var inputField = companyList.inputField;
          inputField.val($(this).text());//名字
          companyList.hide();
          var portId = $(this).attr('portId');
          hiddenField.val(portId);//id
      });

      // 1 没选中客户，焦点离开，隐藏列表
      $('table input[name=port_input]').on('blur', function(){
        var hiddenField = companyList.hiddenField;
        
        if ($(this).val().trim().length ==0) {
            hiddenField.val('');
        };
        companyList.hide();
      });
      
      // 2 当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
      companyList.on('mousedown', function(){
          return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
      });
    
  };

});