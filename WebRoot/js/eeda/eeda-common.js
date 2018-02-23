define(['jquery', 'dataTablesBootstrap', 'jq_blockui'], function($){
  
    var url = window.location;
    
    var element = $('#side-menu a').filter(function() {
        var pathname = '/'+url.pathname.split('/')[1];

        var href = '/'+this.href.split('/')[3];
        
        return href == pathname;
    }).addClass('active').parent();

    var pathname = '/'+url.pathname.split('/')[1];
    var order_name = $('#side-menu a[href="'+pathname+'"]').text();
    $('#breadcrumb_li').text(order_name);

    if(document.title.indexOf('|') == -1){
      document.title = order_name+' | '+document.title;
    }
    

    while (true) {
        if (element.is('li')) {
            element = element.parent().addClass('in').parent();
        } else {
            break;
        }
    }
    //全局的ajax访问，处理ajax请求时sesion超时, 跳转到登录页面
    $.ajaxSetup({
        //contentType:"application/x-www-form-urlencoded;charset=utf-8",
        beforeSend: function(jqXHR, settings){
          var url = this.url;
          //console.log(url);
          if( url.indexOf('/customer/search')!=-1 
              || url.indexOf('/serviceProvider/searchTruckCompany')!=-1 
              || url.indexOf('/serviceProvider/searchCompany')!=-1 
              || url.indexOf('/finItem/search')!=-1 
              || url.indexOf('/serviceProvider/searchCurrency')!=-1 
              || url.indexOf('/location/searchPort')!=-1 
              || url.indexOf('/serviceProvider/searchUnit')!=-1 
              || url.indexOf('/serviceProvider/searchTruckOut')!=-1
              || url.indexOf('/serviceProvider/searchTruckIn')!=-1
              || url.indexOf('/dockInfo/searchLoading')!=-1
              || url.indexOf('/serviceProvider/searchChargeUnit')!=-1
              || url.indexOf('/dockInfo/searchDock')!=-1
              || url.indexOf('/carInfo/search')!=-1 
              || url.indexOf('/trJobOrder/searchCommodity')!=-1 
              || url.indexOf('/costComparison/searchSpcomparison')!=-1
              || url.indexOf('/currencyRate/gainRateList')!=-1
              || url.indexOf('/transJobOrder/getCustomerQuotationAddress')!=-1
              || url.indexOf('/currencyRate/gainRate')!=-1  
              || url.indexOf('/loginUser/searchCustomer')!=-1  
          ){
            return;
          }

          $.blockUI({ 
              message: '<h4><img src="/images/loading.gif" style="height: 20px; margin-top: -3px;"/></h4>' 
          });
        },
        error: function (xhr, e) {
            if(xhr.responseText.indexOf('请输入用户名')>0){
              alert( 'error: 您未登录或超过15分钟未操作, 请重新登录.' );
              window.location.href="/login";
            }
            // if (x.status == 403) {
            //     window.location.reload(); 
            // }
        },
        complete:function(XMLHttpRequest, textStatus){
           //console.log("ajaxSetup textStatus:"+textStatus);
           if(XMLHttpRequest.responseText.indexOf('请输入用户名')>0){
              alert( '您未登录或超过15分钟未操作, 请重新登录.' );
              window.location.href="/login";
            }

            $.unblockUI();
           // var sessionstatus=XMLHttpRequest.getResponseHeader("sessionstatus"); //通过XMLHttpRequest取得响应头，sessionstatus，  
           // if(sessionstatus=="timeout"){
           //     //如果超时就处理 ，指定要跳转的页面
           //             window.location.replace("${path}/common/login.do");
           //     }
          }
    });
    //防止退格键返回上一页
    $(document).keydown(function (e) {
        var doPrevent;
        if (e.keyCode == 8) {
            var d = e.srcElement || e.target;
            if (     (d.tagName.toUpperCase() == 'INPUT' && d.type.toUpperCase() =='TEXT')
            	  ||(d.tagName.toUpperCase() == 'INPUT' && d.type.toUpperCase() =='SEARCH')
                  || (d.tagName.toUpperCase() == 'INPUT' && d.type.toUpperCase() =='PASSWORD')
                  || d.tagName.toUpperCase() == 'TEXTAREA') {
                doPrevent = d.readOnly || d.disabled;
            }
            else
                doPrevent = true;
        }
        else
            doPrevent = false;

        if (doPrevent)
            e.preventDefault();
    });

    //全局：切换tab时刷新其底下的 dataTable, 解决表头不齐的问题
    $('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        var tab = e.target // newly activated tab
        //e.relatedTarget // previous active tab
        var tab_div_id = $(tab).attr('href');
        $(tab_div_id+' table.table').filter(':not(.customized)').DataTable().columns.adjust();
    });

    //全局：当浏览器改变大小时刷新其底下的 dataTable, 解决表头不齐的问题
    $(window).on('resize', function(){
        $('table.table').filter(':not(.customized)').DataTable().columns.adjust();
    });

    //只要属性中使用 limit=10, 控制td长度, 超出10 显示...
    jQuery.fn.limit=function(){ 
        var self = $("td[limit]"); 
        self.each(function(){ 
            var objString = $(this).text(); 
            var objLength = $(this).text().length; 
            var num = $(this).attr("limit"); 
            if(objLength > num){ 
            $(this).attr("title",objString); 
                objString = $(this).text(objString.substring(0,num) + "..."); 
            } 
        }) ;
    } ;
    //$(document.body).limit();
    
    //弹出下拉列表的消失控制: 当点击别的任意地方, 消除已弹出的列表
    
    $(document).click(function (e) {
        var targetEl = e.srcElement || e.target;
        if (targetEl.tagName.toUpperCase() == 'A' && $(targetEl).hasClass('popListItem')) {
          //选中item
        }else{
          //没选中, 获取已弹出list 并隐藏
          eeda.hidePopList();
        }
    });

    $(document).ready(function() {
      $('#version_no').text(eeda_version);
      //需要TODO时才加载TODO.js
      var moudleUrl = window.location.pathname.split('/')[1];
      if(moudleUrl.length>0 && location.search.indexOf('type')>0){
        require(['app/index/todo'], function (todoController) {
            if(moudleUrl.length>0 && location.search.indexOf('type')>0){
                todoController.updateTodo();
            }

            $('#menu_todo_list').click(function(){
              if($(".planOrderWait").html()==""){
                todoController.updateTodo();
              }
            });

            var pathname = window.location.pathname;
            if(pathname == '/')
                return;
            if(window.location.search.indexOf('type=')==-1){
                pathname = pathname.split('/')[1];
                var folder_li = $('#left_side_bar').find('[href="/'+pathname+'"]').parent().parent().parent();
                folder_li.addClass('active').find('ul').addClass('in');
            }
        });
      }
   });
var eeda={};
window.eeda =eeda;

$.fn.dataTable.ext.errMode = function ( settings, helpPage, message ) { 
    console.log(message);
    if(message.indexOf("reinitialise")>0){
    	throw message;
    }
};

//dataTables builder for 1.10
eeda.dt = function(opt){
    var option = {
    	  bSort: opt.sort || false,
        deferLoading: opt.deferLoading || null, //默认null, 一装载就call ajax
        stateSave: opt.stateSave || true,
        processing: opt.processing || true,
        searching: opt.searching || false,
        paging: opt.paging || false,
        info:  opt.info || true,
        lengthChange: opt.lengthChange || false,
        serverSide: opt.serverSide || false, 
        colReorder: opt.colReorder || false, 
        scrollX: opt.scrollX || true,
        scrollY: opt.scrollY || true, //"300px",
        scrollCollapse: opt.scrollCollapse || true,
        deferRender: opt.deferRender || true,
        responsive:opt.responsive || true,
        autoWidth: opt.autoWidth || false,
        pageLength: opt.pageLength || 10,
        aLengthMenu: [ [10, 25, 50, 100, '9999999999'], [10, 25, 50, 100, "All"] ],
        language: {
            "url": "/js/lib/datatables/i18n/Chinese.json"
        },
        createdRow: opt.createdRow || function ( row, data, index ) {
            if(data.ID){
                $(row).attr('id', data.ID);
            }
        },
        drawCallback: opt.drawCallback || function ( settings ) {},
        initComplete: opt.initComplete || function ( settings ) {},
        ajax: opt.ajax || '',
//        ajax: {
//            url: opt.ajax || '',
//            type: 'POST'
//        },
        // ajax: {
        //   url: opt.ajax || '',
          // success:     function(data,status,xhr){
          //   console.log(status);
          // },
          // error: function (xhr, error, thrown) {
            // if(xhr.responseText.indexOf('忘记密码')>0){
            //   alert( '您未登录, 请重新登录.' );
            // }else{
            //   throw error;
            // }
            //alert(error);
        //   }
        // } || '',
        columns: opt.columns || []
    };

    var dataTable = $('#'+opt.id).DataTable(option);

    return dataTable;
}

//根据config来显示/隐藏列头
eeda.showCols=function(dataTable, cols_config){
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
};

eeda.dt_float_header =  function(table_id){
  var table = $('#'+table_id);//表格的id
  var bar_head = $("#"+table_id+"_head");//表头
  eeda.is_in_fix= false;

  //处理浏览器的竖向滚动条
  $(window).scroll(function(){
      var scroll_top = $('body').scrollTop() - table.offset().top;//判断是否到达窗口顶部

      console.log('scroll_top='+scroll_top);
      var scroll_bottom =$('body').scrollTop() - $("#"+table_id+"_info").offset().top+50 ;

      var tbody_left = $("#"+table_id+" tbody").offset().left

      if (scroll_top > 0 && scroll_bottom < 0 ) {
        is_in_fix= true;
        bar_head.css({'z-index':'1','border-radius':'15px 15px 0 0','position':'fixed','top':'0', 'left': tbody_left, 'width':table.width()});
      }else {
        is_in_fix= false;
        bar_head.css({'z-index':'auto', 'position':'static', 'top':'auto', 'left':tbody_left, 'width':table.width() });
        console.log('left='+tbody_left);
        bar_head.offset().left = tbody_left;
        //$("#"+table_id).DataTable().draw();
      }
  });

  //处理table的横向滚动条
  $(".dataTables_scrollBody").scroll(function(e) {
    horizontal = e.currentTarget.scrollLeft;
    // console.log('horizontal='+horizontal);
    
    var left = 176 - horizontal;
    bar_head.offset({ left: left });
  });
};

eeda.hidePopList=function(){
    var listArr=$(".dropdown-menu");
    $(listArr).each(function(i, el) {
        if($(el).is(':visible')){
          $(el).hide();
          var inputField = eeda._inputField;
//		  var hiddenField = eeda._hiddenField;
      if(inputField!=undefined){
    		  
    			  $(inputField).blur(function(){
    				  if(eeda._hiddenField.val()==''){
    					  inputField.val('');
    				  }
    			  });
    			  if(inputField == ""){
    				  if(eeda._hiddenField){
    	            	  eeda._hiddenField.val('');
    	            	}
    			  }
           }
        }
    });
};

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

 eeda.hideSideBar=function(){
        $('#left_side_bar').hide();
        $('#page-wrapper').css('margin', '0');
        $('#hide_menu_btn').css('left', '0px');
            $('.breadcrumb').css('margin-left', '0px');
        $('#hide_menu_icon').removeClass('fa-angle-double-left');
          $('#hide_menu_icon').addClass('fa-angle-double-right');
        };
 
 eeda.getUrlByNo= function(id, orderNo) {
	 	var str = "";
	 	 if(orderNo.indexOf("JH") == 0){//配送
	         str = "<a href='/planOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
	     }else if(orderNo.indexOf("EK") == 0 || orderNo.indexOf("GZ") == 0 ){//拼车
	         str = "<a href='/jobOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
	     }else if(orderNo.indexOf("SGFK") == 0){//手工付款
	         str = "<a href='/costMiscOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
	     }else if(orderNo.indexOf("SGSK") == 0){//手工收款
	         str = "<a href='/chargeMiscOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
	     }else if(orderNo.indexOf("YFSQ") == 0){//应付申请
	         str = "<a href='/costRequest/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
	     }else if(orderNo.indexOf("YSSQ") == 0){//应收申请
	         str = "<a href='/chargeRequest/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
	     }else if(orderNo.indexOf("YFQR") == 0){//应付确认
	         str = "<a href='/costConfirm/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
	     }else if(orderNo.indexOf("YSQR") == 0){//应收确认
	         str = "<a href='/chargeConfirm/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
	     }else if(orderNo.indexOf("YSKP") == 0){//应收开票记录
	         str = "<a href='/chargeInvoiceOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
	     }else if(orderNo.indexOf("YSDZ") == 0){//应收对账
	         str = "<a href='/chargeCheckOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
	     }else if(orderNo.indexOf("YFDZ")== 0){//应付对账
	         str = "<a href='/costCheckOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
	     }else{
	    	 str = "<a href='/jobOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
	     }
	 	//costComparison/searchSpcomparison
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
   
    //表格中，凡是按enter自动跳到右边下一个input
    $(document).on('keydown', 'table input:not([name$="_input"]),select', function(e) {
      if (e.keyCode == 13) {//enter
          var inputField = $(this);

          var td = inputField.parent();
          var row = td.parent();
          var colCount = row.find('td').length;

          var nextTdInput, nextTd=td;
          var index = 0;
          while(!nextTdInput && index<colCount){
              nextTd = nextTd.next();
              index = nextTd.index();
              nextTdInput = nextTd.find('input:last ,select:last');
              if(nextTdInput && !nextTdInput.prop('disabled')){
                  nextTdInput.focus();
                  break;
              }else{
                  nextTdInput=null;
              }
          }
        }
    });

   //dataTable里的下拉列表，查询参数为input,url,添加的参数para,下拉显示的数据库字段
   eeda.bindTableField = function(table_id, el_name,url,para) {
		  var tableFieldList = $('#table_input_field_list');

      //处理中文输入法, 没完成前不触发查询
      var cpLock = false;
      var isEmpty = 0;
      
      
      $('#'+table_id+' input[name='+el_name+'_input]').on('compositionstart', function () {
          cpLock = true;
      }).on('compositionend', function () {
          cpLock = false;
      });
      	  $('#'+table_id+' input[name='+el_name+'_input]').off("keyup click");
		  $('#'+table_id+' input[name='+el_name+'_input]').on('keyup click', function(event){
			  var paraJson="";
			  if(para=="spConditionJson"){
				  paraJson= itemOrder.spConditionJson();
				    var obj = null;
				    try{
					    obj = eval(paraJson);
					}catch(e){
					}
					var xy = Object.prototype.toString.call(obj);
					if (xy == "[object Object]" || xy == "[object Array]") {
						paraJson = JSON.stringify(paraJson);
					} else {
					    console.log("不是json格式");
					}
			  }
			  
			  var paraCustomerSelectIds="";			  
			  if(para=="userCustomerSelectIds"){
				  paraCustomerSelectIds=itemOrder.userCustomerIdFunction().toString();
			  }
			    
			  var me = this;
			  var inputField = $(this);
			  var hiddenField = $(this).parent().find('input[name='+el_name+']');
			  var inputStr = inputField.val();

        if(cpLock)
            return;

        if (event.keyCode == 40) {
            tableFieldList.find('li').first().focus();
            return false;
        }
        
        
        
        if(event.keyCode==8){
        	if(inputStr==''){
        		hiddenField.val('');
        	}
        	return ;
        }else{
			  $.get(url, {input:inputStr,para:para,paraJson,paraCustomerSelectIds:paraCustomerSelectIds}, function(data){
  				  if(inputStr!=inputField.val()){//查询条件与当前输入值不相等，返回
  					  return;
  				  }
  				  tableFieldList.empty();

            if(inputStr=='' && data.length>0){
              if(data[0].REF_ID){
                tableFieldList.append('<span style="font-size: 10px;color: gray;">您曾经使用过的'+data.length+'行记录, 需要别的数据请输入查询条件</span>');
              }else{
                tableFieldList.append('<span style="font-size: 10px;color: gray;">最多只显示'+data.length+'行记录, 如无想要记录, 请输入更多查询条件</span>');
              }
            }else if(data.length==0){
            	if(url=="/serviceProvider/searchCompany"){
            		tableFieldList.append('<span class="search_hint">您没有该结算公司的权限,请联系管理员</span>');
            	}if(url=="/serviceProvider/searchChargeUnit"){
            		tableFieldList.append('<span class="search_hint">没有此单位记录，请设置<a href="/unit/create" target="_blank">点击此处</a></span>');
            	}else{
            		tableFieldList.append('<span style="font-size: 10px;color: gray;">无记录</span>');
            	}
            	isEmpty = 1;
            }else if(inputStr.length>0 && data.length==10){
              tableFieldList.append('<span style="font-size: 10px;color: gray;">最多只显示'+data.length+'行记录, 如无想要记录, 请输入更多查询条件</span>');
            }else if(data=="无记录"){
            	if(url=="/serviceProvider/searchCompany"){
            		tableFieldList.append('<span class="search_hint">搜索无记录<a href="/customer/add"  target="_blank">点击设置客户</a> - <a href="/customer/add"  target="_blank">点击设置供应商</a></span>');
            	}
            }

            if(para == 'air_port' || para == 'port'){
                for(var i = 0; i < data.length; i++){
                    tableFieldList.append("<li tabindex='"+i+"'><a class='fromLocationItem' dataId='"+data[i].ID+"' >"+data[i].NAME+" -"+data[i].CODE+"</a></li>"); 
                }
            }else{
            	if(data!="无记录"){
            		for(var i = 0; i < data.length; i++){
                        tableFieldList.append("<li tabindex='"+i+"'><a class='fromLocationItem' dataId='"+data[i].ID+"' dataName='"+data[i].NAME+"' sp_type='"+data[i].SP_TYPE+"' ref_office_id='"+data[i].REF_OFFICE_ID+"'>"+data[i].NAME+"</a></li>");
                    }
            	}
            }

		        tableFieldList.css({ 
			          left:$(me).offset().left+"px", 
                top:$(me).offset().top+28+"px" 
            });
            tableFieldList.show();
            eeda._inputField = inputField;
            eeda._hiddenField = hiddenField;
            if(data.length==0)
                hiddenField.val('');
            if(data.length==1&&data[0].ID){
//            	  inputField.val(data[0].NAME);
                hiddenField.val(data[0].ID);
//                tableFieldList.hide();
            }
            if(!inputStr && data.length>1){
	                hiddenField.val('');
            }
            //tableFieldList;
  	    });
        }
		  });

		  tableFieldList.on('click', '.fromLocationItem', function(e){
			  var inputField = eeda._inputField;
			  var hiddenField = eeda._hiddenField;
			  inputField.val($(this).text());//名字
			  tableFieldList.hide();
			  var dataId = $(this).attr('dataId');
			  hiddenField.val(dataId);//id
			  inputField.attr("ref_office_id",$(this).attr("ref_office_id"));
		  });

      tableFieldList.on('keydown', 'li', function(e){
        e.preventDefault();
        if (e.keyCode == 13) {//enter
          var inputField = eeda._inputField;
          var hiddenField = eeda._hiddenField;
          inputField.val($(this).text());//名字
          tableFieldList.hide();
          var dataId = $(this).find('a').attr('dataId');
          hiddenField.val(dataId);//id

          var td = inputField.parent().parent();
          var row = td.parent();
          var colCount = row.find('td').length;

          var nextTdInput, nextTd=td;
          var index = 0;
          while(!nextTdInput && index<colCount){
              nextTd = nextTd.next();
              index = nextTd.index();
              nextTdInput = nextTd.find('input:last');
              if(nextTdInput && !nextTdInput.prop('disabled')){
                  nextTdInput.focus();
                  break;
              }else{
                  nextTdInput=null;
              }
          }
        }
      });

		  // 1 没选中客户，焦点离开，隐藏列表
		  $(document).on('click', function(event){
        //console.log("tableFieldList.is(':visible') == "+tableFieldList.is(':visible'));
          if (tableFieldList.is(':visible') ){
              var clickedEl = $(this);
              var hiddenField = eeda._hiddenField;
              if ($(this).find('a').val().trim().length ==0) {
                  hiddenField.val('');
              };
              tableFieldList.hide();
              
              if(isEmpty == 1){
            	  hiddenField.val('');
            	  inputField.val('');
            	  isEmpty = 0;
              }
          }
		  });
		  

		  // 2 当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
		  tableFieldList.on('mousedown', function(){
			  return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
		  });

      tableFieldList.on('focus', 'li', function() {
          $this = $(this);
          $this.addClass('active').siblings().removeClass();
          // $this.closest('div.container').scrollTop($this.index() * $this.outerHeight());
      }).on('keydown', 'li', function(e) {
          $this = $(this);
          if (e.keyCode == 40) {
              $this.next().focus();
              return false;
          } else if (e.keyCode == 38) {
              $this.prev().focus();
              return false;
          }
      }).find('li').first().focus();  
	  };
	  
		  
	  eeda.bindTableFieldCurrencyId = function(table_id, el_name,url,para) {
		  var tableFieldList = $('#table_currency_input_field_list');

		  $('#'+table_id+' input[name='+el_name+'_input]').off('keyup click');
		  $('#'+table_id+' input[name='+el_name+'_input]').on('keyup click', function(event){
//			  eeda.para_middle=para;
			  var me = this;
			  var inputField = $(this);
			  var hiddenField = $(this).parent().find('input[name='+el_name+']');
			  var inputStr = inputField.val();
			  var CURRENCY_ID_input = "";

			  if (event.keyCode == 40) {
				  tableFieldList.find('li').first().focus();
				  return false;
			  }
			  
			  $.get(url, {input:inputStr,para:para}, function(data){
				  if(inputStr!=inputField.val()){//查询条件与当前输入值不相等，返回
					  return;
				  }
				  tableFieldList.empty();
				  if(data.length==0){
					  tableFieldList.append('<span class="search_hint">没有此币制汇率，请设置<a href="/currencyRate/create" target="_blank">点击此处</a></span>');
				  }
				  for(var i = 0; i < data.length; i++)
					  tableFieldList.append("<li tabindex='"+i+"'><a class='fromLocationItem' dataId='"+data[i].ID
							  +"' dataName='"+data[i].NAME+"' currency_rate='"+data[i].RATE+"' >"+data[i].NAME+"</a></li>");
				  tableFieldList.css({ 
					  left:$(me).offset().left+"px", 
					  top:$(me).offset().top+28+"px" 
				  });
				  tableFieldList.show();
				  eeda._inputField = inputField;
				  eeda._hiddenField = hiddenField;
				  //tableFieldList;
				  if(data.length==0)
		                hiddenField.val('');
		            if(data.length==1&&data[0].ID)
		                hiddenField.val(data[0].ID);
		            if(!inputStr){
		            	if(data.length>1)
			                hiddenField.val('');
		            }
		            
			  },'json');
		  });
		  tableFieldList.off('keyup click');
		  tableFieldList.on('click', '.fromLocationItem', function(e){
			  var inputField = eeda._inputField;
			  var hiddenField = eeda._hiddenField;
			  inputField.val($(this).text());//名字
			  tableFieldList.hide();
			  var dataId = $(this).attr('dataId');
			  hiddenField.val(dataId);//id
			  
			  //datatable里按照   币制，汇率，转换后金额 相邻排列
			  
			  var td = inputField.parent().parent();
			  var exchange_currency_rate = 1.000000;
			  td.parent().find('.cny_to_other input').val($(this).text());
			  td.parent().find('[name=exchange_currency_id]').val(dataId);

			  var class_name = td.attr('class');
			  var currency_rate = $(this).attr('currency_rate');
			  if(para!="notRate"){
				  td.next().children().val(currency_rate);//选择币制则填入汇率
			  }
              
              td.parent().find('.exchange_currency_rate_rmb input').val(currency_rate);
              if(class_name==' cny_to_other'&&td.parent().find('[name=CURRENCY_ID_input]').val()!=$(this).text()){
            	   var para_currency=td.parent().find('[name=CURRENCY_ID_input]').val()
            	   td.parent().find('.exchange_currency_rate input').val('');
            	   td.parent().find('.exchange_total_amount input').val('');
                   td.parent().find('.exchange_total_amount_rmb input').val('');
                   td.parent().find('.rmb_difference input').val('');
            	   exchange_currency_rate='';
            	   $.get('/currencyRate/gainRate',{para_exchange_currency:inputField.val(),para_currency:para_currency},function(data){
            		   if(data.RATE){
            			   exchange_currency_rate=data.RATE;
            			   td.parent().find('.exchange_currency_rate input').val(exchange_currency_rate);
            		   }
            		   var total = td.parent().find('.currency_total_amount input').val();//此币种的金额
                       if(exchange_currency_rate!=''&&currency_rate!=undefined && total!=undefined && currency_rate!='' && total!='' && !isNaN(currency_rate) && !isNaN(total)){
                     	  if(class_name==' cny_to_other'){
                     		  td.next().next().children().val((total*exchange_currency_rate).toFixed(3));//转换后的金额
                     	   }else{
                     		  td.next().next().children().val((currency_rate*total).toFixed(3));//转换后的金额
                     		  td.parent().find('.exchange_total_amount input').val((total*exchange_currency_rate).toFixed(3));
                     	  }
                         var exchange_total = td.parent().find('.exchange_total_amount input').val();//此币种的金额
                         	td.parent().find('.exchange_total_amount_rmb input').val((exchange_total*currency_rate).toFixed(3));
                         var cny_total_amount=parseFloat(td.parent().find('.cny_total_amount input').val());
                         var exchange_total_amount_rmb=parseFloat(td.parent().find('.exchange_total_amount_rmb input').val());
                         	td.parent().find('.rmb_difference input').val((exchange_total_amount_rmb-cny_total_amount).toFixed(3));
                       }            		   
            	   },'json');            	   
            	   
            	  }else{
        				  td.parent().find('[name=exchange_currency_rate]').val(exchange_currency_rate);           		  
            	  }

              var total = td.parent().find('.currency_total_amount input').val();//此币种的金额
              if(exchange_currency_rate!=''&&currency_rate!=undefined && total!=undefined && currency_rate!='' && total!='' && !isNaN(currency_rate) && !isNaN(total)){
            	  if(class_name==' cny_to_other'){
            		  td.next().next().children().val((total*exchange_currency_rate).toFixed(3));//转换后的金额
            	   }else{
            		  td.next().next().children().val((currency_rate*total).toFixed(3));//转换后的金额
            		  td.parent().find('.exchange_total_amount input').val((total*exchange_currency_rate).toFixed(3));
            	  }
                var exchange_total = td.parent().find('.exchange_total_amount input').val();//此币种的金额
                	td.parent().find('.exchange_total_amount_rmb input').val((exchange_total*currency_rate).toFixed(3));
                var cny_total_amount=parseFloat(td.parent().find('.cny_total_amount input').val());
                var exchange_total_amount_rmb=parseFloat(td.parent().find('.exchange_total_amount_rmb input').val());
                	td.parent().find('.rmb_difference input').val((exchange_total_amount_rmb-cny_total_amount).toFixed(3));
              }
		  });
//		  eeda.para_middle="";
		  tableFieldList.on('keydown', 'li', function(e){
              if (e.keyCode == 13) {
                  var inputField = eeda._inputField;
                  var hiddenField = eeda._hiddenField;
                  inputField.val($(this).text());//名字
                  tableFieldList.hide();
                  var $a = $(this).find('a');
                  var dataId = $a.attr('dataId');
                  hiddenField.val(dataId);//id
                  //datatable里按照   币制，汇率，转换后金额 相邻排列
                  var td = inputField.parent().parent();
                  
                  var exchange_currency_rate = 1.000000;
    			  td.parent().find('.cny_to_other input').val($(this).text());
    			  td.parent().find('[name=exchange_currency_id]').val(dataId);
                  
                  var class_name = td.attr('class');
                  var currency_rate = $a.attr('currency_rate');

                  td.next().children().val(currency_rate);//选择币制则填入汇率
                  td.parent().find('.exchange_currency_rate_rmb input').val(currency_rate);
                  if(class_name==' cny_to_other'&&td.parent().find('[name=CURRENCY_ID_input]').val()!=$(this).text()){
                	   td.parent().find('.exchange_currency_rate input').val('');
                	   td.parent().find('.exchange_total_amount input').val('');
                     td.parent().find('.exchange_total_amount_rmb input').val('');
                     td.parent().find('.rmb_difference input').val('');
                	   exchange_currency_rate='';
                	   
                	  }else{
                	   td.parent().find('[name=exchange_currency_rate]').val(exchange_currency_rate); 
                	  }

                	  var total = td.parent().find('.currency_total_amount input').val();//此币种的金额
                  if(exchange_currency_rate!=''&&currency_rate!=undefined && total!=undefined && currency_rate!='' && total!='' && !isNaN(currency_rate) && !isNaN(total)){
                	  if(class_name==' cny_to_other'){
                		  td.next().next().children().val((total*exchange_currency_rate).toFixed(3));//转换后的金额
                	   }else{
                		  td.next().next().children().val((currency_rate*total).toFixed(3));//转换后的金额
                		  td.parent().find('.exchange_total_amount input').val((total*exchange_currency_rate).toFixed(3));
                	  }
                     var exchange_total = td.parent().find('.exchange_total_amount input').val();//此币种的金额
                    td.parent().find('.exchange_total_amount_rmb input').val((exchange_total*currency_rate).toFixed(3));
                    var  cny_total_amount=parseFloat(td.parent().find('.cny_total_amount input').val());
                    var exchange_total_amount_rmb=parseFloat(td.parent().find('.exchange_total_amount_rmb input').val());
                    td.parent().find('.rmb_difference input').val((exchange_total_amount_rmb-cny_total_amount).toFixed(3));
                  }
                  
                  var row = td.parent();
                  var colCount = row.find('td').length;
                  
                  var nextTdInput, nextTd=td;
                  var index = 0;
                  while(!nextTdInput && index<colCount){
                      nextTd = nextTd.next();
                      index = nextTd.index();
                      nextTdInput = nextTd.find('input:last');
                      if(nextTdInput && !nextTdInput.prop('disabled')){
                          nextTdInput.focus();
                          break;
                      }else{
                          nextTdInput=null;
                      }
                  }
              }
          });
		  
		  // 1 没选中客户，焦点离开，隐藏列表
		  $(document).on('click', function(event){
			  if (tableFieldList.is(':visible') ){
				  var clickedEl = $(this);
				  var hiddenField = eeda._hiddenField;
				  if ($(this).find('a').val().trim().length ==0) {
					  hiddenField.val('');
				  };
				  tableFieldList.hide();
			  }
		  });
		  
		  // 2 当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
		  tableFieldList.on('mousedown', function(){
			  return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
		  });
		  
		  tableFieldList.on('focus', 'li', function() {
			  $this = $(this);
			  $this.addClass('active').siblings().removeClass();
			  // $this.closest('div.container').scrollTop($this.index() * $this.outerHeight());
		  }).on('keydown', 'li', function(e) {
			  $this = $(this);
			  if (e.keyCode == 40) {
				  $this.next().focus();
				  return false;
			  } else if (e.keyCode == 38) {
				  $this.prev().focus();
				  return false;
			  }
		  }).find('li').first().focus();
	  };
   
    eeda.buildTableDetail=function(table_id, deletedTableIds){
      var item_table_rows = $("#"+table_id+" tr:visible");
        var items_array=[];
        for(var index=0; index<item_table_rows.length; index++){
            if(index==0)
                continue;

            var row = item_table_rows[index];
            var empty = $(row).find('.dataTables_empty').text();
            if(empty)
              continue;
            
            var id = $(row).attr('id');
            if(!id){
                id='';
            }
            
            var item={}
            item.id = id;
            for(var i = 1; i < row.childNodes.length; i++){
              var name = $(row.childNodes[i]).find('input,select').attr('name');
              var value = $(row.childNodes[i]).find('input,select').val();
              if(name){
                item[name] = value;
              }
            }
            item.action = id.length > 0?'UPDATE':'CREATE';
            items_array.push(item);
        }

        //add deleted items
        if(deletedTableIds!=''){
        	
        	for(var index=0; index<deletedTableIds.length; index++){
        		var id = deletedTableIds[index];
        		var item={
        				id: id,
        				action: 'DELETE'
        		};
        		items_array.push(item);
        	}
        	deletedTableIds = [];
        }
        return items_array;
    };
    
    
    eeda.getDate =  function() {
      	var d = new Date(); 
      	var year = d.getFullYear(); 
      	var month = d.getMonth()+1; 
      	var date = d.getDate(); 
      	var day = d.getDay(); 
      	var hours = d.getHours(); 
      	var minutes = d.getMinutes(); 
      	var seconds = d.getSeconds(); 
      	var ms = d.getMilliseconds(); 
      	var curDateTime= year;
      	if(month>9)
      		curDateTime = curDateTime +"-"+month;
      	else
      		curDateTime = curDateTime +"-0"+month;
      	if(date>9)
      		curDateTime = curDateTime +"-"+date+" ";
      	else
      		curDateTime = curDateTime +"-0"+date+" ";
      	if(hours>9)
      		curDateTime = curDateTime +" "+hours;
      	else
      		curDateTime = curDateTime +"0"+hours;
      	if(minutes>9)
      		curDateTime = curDateTime +":"+minutes;
      	else
      		curDateTime = curDateTime +":0"+minutes;
      	if(seconds>9)
      		curDateTime = curDateTime +":"+seconds;
      	else
      		curDateTime = curDateTime +":0"+seconds;
      	return curDateTime; 
	};

    eeda.bindTableFieldTruckOut = function(table_id, el_name) {
          var tableFieldList = $('#table_truck_out_input_field_list');
          $('#'+table_id+' input[name='+el_name+'_input]').off('keyup click');
          $('#'+table_id+' input[name='+el_name+'_input]').on('keyup click', function(event){
              var me = this;
              var inputField = $(this);
              var hiddenField = $(this).parent().find('input[name='+el_name+']');
              var inputStr = inputField.val();

              if (event.keyCode == 40) {
                  tableFieldList.find('li').first().focus();
                  return false;
              }

              $.get('/serviceProvider/searchTruck', {input:inputStr,land_party_type:"CONSIGNOR"}, function(data){
                  if(inputStr!=inputField.val()){//查询条件与当前输入值不相等，返回
                      return;
                  }
                  tableFieldList.empty();
                  for(var i = 0; i < data.length; i++)
                	  
                      tableFieldList.append("<li tabindex='"+i+"'><a class='item' dataId='"+data[i].ID
                              +"' dataName='"+data[i].NAME+"' "
                              +" phone='"+data[i].PHONE+"' "
                              +" addr='"+data[i].ADDRESS+"' "
                              +" contact_man='"+data[i].CONTACT_PERSON+"' >"+data[i].NAME
                              +"</a></li>");
                  tableFieldList.css({ 
                      left:$(me).offset().left+"px", 
                      top:$(me).offset().top+28+"px" 
                  });
                  tableFieldList.show();
                  eeda._inputField = inputField;
                  eeda._hiddenField = hiddenField;
                  if(data.length==0)
		                hiddenField.val('');
		            if(data.length==1&&data[0].ID)
		                hiddenField.val(data[0].ID);
		            if(!inputStr){
		            	if(data.length>1)
			                hiddenField.val('');
		            }
              },'json');
          });
          
          tableFieldList.on('click', '.item', function(e){
              var inputField = eeda._inputField;
              var hiddenField = eeda._hiddenField;
              inputField.val($(this).text());//名字
              tableFieldList.hide();
              var dataId = $(this).attr('dataId');
              hiddenField.val(dataId);//id

              var row = inputField.parent().parent().parent();
              var phone = $(this).attr('phone');
              row.find('.consignor_phone input').val(phone!="null"?phone:"");
              var addr = $(this).attr('addr');
              row.find('.consignor_addr input').val(addr!="null"?addr:"");
              var contact_man = $(this).attr('contact_man');
              row.find('.consignor_contact_man input').val(contact_man!="null"?contact_man:"");
          });

          tableFieldList.on('keydown', 'li', function(e){
              if (e.keyCode == 13) {
                  var inputField = eeda._inputField;
                  var hiddenField = eeda._hiddenField;
                  inputField.val($(this).text());//名字
                  tableFieldList.hide();
                  var $a = $(this).find('a');
                  var dataId = $a.attr('dataId');
                  hiddenField.val(dataId);//id

                  var row = inputField.parent().parent().parent();
                  row.find('.consigner_phone input').val($a.attr('phone'));
                  row.find('.consigner_addr input').val($a.attr('addr'));
              }
          });
          // 1 没选中item，焦点离开，隐藏列表
          $(document).on('click', function(event){
              if (tableFieldList.is(':visible') ){
                  var clickedEl = $(this);
                  var hiddenField = eeda._hiddenField;
                  if ($(this).find('a').val().trim().length ==0) {
                    hiddenField.val('');
                  };
                  tableFieldList.hide();
              }
          });

          // 2 当用户只点击了滚动条，没选item，再点击页面别的地方时，隐藏列表
          tableFieldList.on('mousedown', function(){
              return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
          });

          tableFieldList.on('focus', 'li', function() {
              $this = $(this);
              $this.addClass('active').siblings().removeClass();
              // $this.closest('div.container').scrollTop($this.index() * $this.outerHeight());
          }).on('keydown', 'li', function(e) {
              $this = $(this);
              if (e.keyCode == 40) {
                  $this.next().focus();
                  return false;
              } else if (e.keyCode == 38) {
                  $this.prev().focus();
                  return false;
              }
          }).find('li').first().focus();
      };

    eeda.bindTableFieldTruckIn = function(table_id, el_name) {
          var tableFieldList = $('#table_truck_in_input_field_list');
          $('#'+table_id+' input[name='+el_name+'_input]').off("keyup click");
          $('#'+table_id+' input[name='+el_name+'_input]').on('keyup click', function(event){
              var me = this;
              var inputField = $(this);
              var hiddenField = $(this).parent().find('input[name='+el_name+']');
              var inputStr = inputField.val();

              if (event.keyCode == 40) {
                  tableFieldList.find('li').first().focus();
                  return false;
              }

              $.get('/serviceProvider/searchTruck', {input:inputStr,land_party_type:"CONSIGNEE"}, function(data){
                  if(inputStr!=inputField.val()){//查询条件与当前输入值不相等，返回
                      return;
                  }
                  tableFieldList.empty();
                  for(var i = 0; i < data.length; i++)
                      tableFieldList.append("<li tabindex='"+i+"'><a class='item' dataId='"+data[i].ID
                              +"' dataName='"+data[i].NAME+"' "
                              +" phone='"+data[i].PHONE+"' "
                              +" addr='"+data[i].ADDRESS+"' "
                              +" contact_man='"+data[i].CONTACT_PERSON+"' >"+data[i].NAME
                              +"</a></li>");
                  tableFieldList.css({ 
                      left:$(me).offset().left+"px", 
                      top:$(me).offset().top+28+"px" 
                  });
                  tableFieldList.show();
                  eeda._inputField = inputField;
                  eeda._hiddenField = hiddenField;
                  if(data.length==0)
		                hiddenField.val('');
		            if(data.length==1&&data[0].ID)
		                hiddenField.val(data[0].ID);
		            if(!inputStr){
		            	if(data.length>1)
			                hiddenField.val('');
		            }
              },'json');
          });

          tableFieldList.on('click', '.item', function(e){
              var inputField = eeda._inputField;
              var hiddenField = eeda._hiddenField;
              inputField.val($(this).text());//名字
              tableFieldList.hide();
              var dataId = $(this).attr('dataId');
              hiddenField.val(dataId);//id

              var row = inputField.parent().parent().parent();
              var phone = $(this).attr('phone');
              row.find('.consignee_phone input').val(phone!="null"?phone:"");
              var addr = $(this).attr('addr');
              row.find('.consignee_addr input').val(addr!="null"?addr:"");
              var contact_man = $(this).attr('contact_man');
              row.find('.consignee_contact_man input').val(contact_man!="null"?contact_man:"");
          });

          tableFieldList.on('keydown', 'li', function(e){
              if (e.keyCode == 13) {
                  var inputField = eeda._inputField;
                  var hiddenField = eeda._hiddenField;
                  inputField.val($(this).text());//名字
                  tableFieldList.hide();
                  var $a = $(this).find('a');
                  var dataId = $a.attr('dataId');
                  hiddenField.val(dataId);//id

                  var row = inputField.parent().parent().parent();
                  row.find('.consignee_phone input').val($a.attr('phone'));
                  row.find('.consignee_addr input').val($a.attr('addr'));
              }
          });
          
          // 1 没选中item，焦点离开，隐藏列表
          $(document).on('click', function(event){
              if (tableFieldList.is(':visible') ){
                  var clickedEl = $(this);
                  var hiddenField = eeda._hiddenField;
                  if ($(this).find('a').val().trim().length ==0) {
                    hiddenField.val('');
                  };
                  tableFieldList.hide();
              }
          });
          
          // 2 当用户只点击了滚动条，没选item，再点击页面别的地方时，隐藏列表
          tableFieldList.on('mousedown', function(){
              return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
          });

          tableFieldList.on('focus', 'li', function() {
              $this = $(this);
              $this.addClass('active').siblings().removeClass();
              // $this.closest('div.container').scrollTop($this.index() * $this.outerHeight());
          }).on('keydown', 'li', function(e) {
              $this = $(this);
              if (e.keyCode == 40) {
                  $this.next().focus();
                  return false;
              } else if (e.keyCode == 38) {
                  $this.prev().focus();
                  return false;
              }
          }).find('li').first().focus();
      };
      

      eeda.bindTableFieldCarInfo = function(table_id, el_name) {
          var tableFieldList = $('#table_car_no_field_list');
          $('#'+table_id+' input[name='+el_name+'_input]').off("keyup click"); 
          $('#'+table_id+' input[name='+el_name+'_input]').on('keyup click', function(event){
              var me = this;
              amongK=$('#'+table_id+' tr').index(this); 
              var inputField = $(this);
              var hiddenField = $(this).parent().find('input[name='+el_name+']');
              var inputStr = inputField.val();
              
              

              if (event.keyCode == 40) {
                  tableFieldList.find('li').first().focus();
                  return false;
              }
              
              $.get('/carInfo/search', {input:inputStr}, function(data){
                  if(inputStr!=inputField.val()){//查询条件与当前输入值不相等，返回
                      return;
                  }
                  tableFieldList.empty();
                  for(var i = 0; i < data.length; i++)
                      tableFieldList.append("<li tabindex='"+i+"'><a class='item' dataId='"+data[i].ID
                              +"' carType='"+data[i].CARTYPE+"' "
                              +" phone='"+data[i].PHONE+"' "
                              +" toca_weight='"+data[i].TOCA_WEIGHT+"' "
                              +" head_weight='"+data[i].HEAD_WEIGHT+"' "
                              +" parent_id='"+data[i].PARENT_ID+"' "
                              +" sp_name='"+data[i].SP_NAME+"' "
                              +" driver='"+data[i].DRIVER+"' >"+data[i].CAR_NO
                              +"</a></li>");
                  tableFieldList.css({ 
                      left:$(me).offset().left+"px", 
                      top:$(me).offset().top+28+"px" 
                  });
                  tableFieldList.show();
                  eeda._inputField = inputField;
                  eeda._hiddenField = hiddenField;
                  if(data.length==0)
		                hiddenField.val('');
		            if(data.length==1&&data[0].ID)
		                hiddenField.val(data[0].ID);
		            if(!inputStr){
		            	if(data.length>1)
			                hiddenField.val('');
		            }
              },'json');
          });
      

          tableFieldList.on('click', '.item', function(e){
              var inputField = eeda._inputField;
              var hiddenField = eeda._hiddenField;
              inputField.val($(this).text());//名字
              tableFieldList.hide();
              hiddenField.val($(this).attr('dataId'));
              hiddenField.attr("car_id",$(this).attr('dataId'));
              var toca_weight=$(this).attr('toca_weight')=="null"?'':$(this).attr('toca_weight');
              var head_weight=$(this).attr('head_weight')=="null"?'':$(this).attr('head_weight');
              var row = inputField.parent().parent().parent();
              row.find('select.truck_type').val($(this).attr('carType'));
              row.find('input.driver ').val($(this).attr('driver'));
              row.find('input.phone').val($(this).attr('phone'));
              row.find('input.toca_weight').val(toca_weight);
              row.find('input.head_weight').val(head_weight);
              if($(this).attr('parent_id')!='null'){
                  row.find('input[name=SP_ID]').val($(this).attr('parent_id'));
                  row.find('input[name=SP_ID_input]').val($(this).attr('sp_name'));
              }else{
                  row.find('input[name=SP_ID]').val('');
                  row.find('input[name=SP_ID_input]').val('');            	  
              }
              if($(this).attr('toca_weight').indexOf('k')!=-1)
                row.find('input.toca_weight').val(toca_weight.substring(0,toca_weight.indexOf('k',0)+1));
              if($(this).attr('head_weight').indexOf('k')!=-1)
                row.find('input.head_weight').val(head_weight.substring(0,toca_weight.indexOf('k',0)+1));
          });

          tableFieldList.on('keydown', 'li', function(e){
              if (e.keyCode == 13) {
                  var inputField = eeda._inputField;
                  var hiddenField = eeda._hiddenField;
                  inputField.val($(this).text());//名字
                  tableFieldList.hide();
                  var $a = $(this).find('a');
                  hiddenField.val($a.attr('dataId'));
                  hiddenField.attr("car_id",$a.attr('dataId'));
                  var row = inputField.parent().parent().parent();
                  row.find('select.truck_type').val($a.attr('carType'));
                  row.find('input.driver ').val($a.attr('driver'));
                  row.find('input.phone').val($a.attr('phone'));
              }
          });
          
          // 1 没选中item，焦点离开，隐藏列表
          $(document).on('click', function(event){
              if (tableFieldList.is(':visible') ){
                  var clickedEl = $(this);
                  var hiddenField = eeda._hiddenField;
                  if ($(this).find('a').val().trim().length ==0) {
                    hiddenField.val('');
                  };
                  tableFieldList.hide();
              }
          });
          
          // 2 当用户只点击了滚动条，没选item，再点击页面别的地方时，隐藏列表
          tableFieldList.on('mousedown', function(){
              return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
          });

          tableFieldList.on('focus', 'li', function() {
              $this = $(this);
              $this.addClass('active').siblings().removeClass();
              // $this.closest('div.container').scrollTop($this.index() * $this.outerHeight());
          }).on('keydown', 'li', function(e) {
              $this = $(this);
              if (e.keyCode == 40) {
                  $this.next().focus();
                  return false;
              } else if (e.keyCode == 38) {
                  $this.prev().focus();
                  return false;
              }
          }).find('li').first().focus();
      };
      
      eeda.bindTableLocationField = function(table_id, el_name) {
    	  var spList = $('#input_location_field_list');
           var area_list_title = $("#input_location_field_list .area-list-title");
           var spListContent =$("#input_location_field_list .area-list-content");
           var hiddenProvinceField = $('#input_location_field_list [name='+el_name+'_province]');//这里是方便用户选错时，回选上级
           
                     
           var searchLocation = function(level, code){
             var locLevel = "province";
             level = level | 0;
             if(level == 1){
               locLevel = "city";
             }
             if(level == 2){
               locLevel = "area";
             }
        
             $.get('/serviceProvider/'+locLevel, {id:code}, function(data){
               spListContent.empty();
               for(var i = 0; i < data.length; i++){
                 var loc = data[i];
                 spListContent.append('<a next-level="'+(level+1)+'" p_code="'+loc.PCODE+'" href="javascript:void(0)" code="'+loc.CODE+'" name="'+loc.NAME+'">'+loc.NAME+'</a>');
               }
               spList.find('input').removeClass('this');
                 spList.find('input[data-level='+level+']').addClass('this');
                 spList.css({ 
                     left:$(eeda._inputField).offset().left+"px", 
                     top:$(eeda._inputField).offset().top+30+"px" 
                   }); 
                 spList.show();
               
             },'json');
           };
           $('#'+table_id+' input[name='+el_name+'_INPUT]').off("click");
           $('#'+table_id+' input[name='+el_name+'_INPUT]').on(' click', function(){
               var me = this;
               var inputField = $(this);
               var hiddenField = $(this).parent().find('input[name='+el_name+']');
             var inputStr = $(this).val();
             
             searchLocation();
             spList.css({ 
                   left:$(me).offset().left+"px", 
                   top:$(me).offset().top+30+"px" 
                 }); 
             spList.show();

                  eeda._inputField = inputField;
                  eeda._hiddenField = hiddenField;
             });
        
             spListContent.on('click', 'a', function(){
            	 var inputField = eeda._inputField;
                 var hiddenField = eeda._hiddenField;
                 // inputField.val($(this).text());//名字
               var dataLevel = $(this).attr('next-level');
               var code = $(this).attr('code');
               var name = $(this).attr('name');
               oldValue = inputField.val();
               if(dataLevel == 1){
                 hiddenProvinceField.val(code);
               }
               
               if(dataLevel>1){
                if(oldValue.indexOf(name,0)!=-1){
                  oldValue=oldValue.substring(0,oldValue.indexOf("-"+name,0));
                }
                 name = oldValue+'-'+name;
               }
               inputField.val(name);
               hiddenField.val(code);
        
               if(dataLevel == 3){
                 spList.hide();
                 return;
               }
               searchLocation(dataLevel, code);
        
               spList.find('input').removeClass('this');
               spList.find('input[data-level='+dataLevel+']').addClass('this');
             });
             
             // 没选中，焦点离开，隐藏列表
           $(document).on('click', function(event){
              if (spList.is(':visible') ){
               spList.hide();
             }
           });
        
           //当用户只点击了滚动条，没选，再点击页面别的地方时，隐藏列表
           spList.on('blur', function(){
             spList.hide();
           });
        
           spList.on('mousedown', function(){
             return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
           });


      };
      
      eeda.bindTableFieldDockInfo = function(table_id, el_name) {
          var tableFieldList = $('#table_dock_no_field_list');
          $('#'+table_id+' input[name='+el_name+'_input]').off('keyup click');
          $('#'+table_id+' input[name='+el_name+'_input]').on('keyup click', function(event){
              var me = this;
              var inputField = $(this);
              var hiddenField = $(this).parent().find('input[name='+el_name+']');
              var inputStr = inputField.val();

              if (event.keyCode == 40) {
                  tableFieldList.find('li').first().focus();
                  return false;
              }

      if(event.keyCode==8){
          if(inputStr==''){
            hiddenField.val('');
          }
          return ;
        }else{
              $.get('/dockInfo/searchLoading', {input:inputStr}, function(data){
                  if(inputStr!=inputField.val()){//查询条件与当前输入值不相等，返回
                      return;
                  }
                  tableFieldList.empty();
                  if(inputStr=='' && data.length>0){
                      if(data[0].REF_ID){
                        tableFieldList.append('<span style="font-size: 10px;color: gray;">您曾经使用过的'+data.length+'行记录, 需要别的数据请输入查询条件</span>');
                      }else{
                        tableFieldList.append('<span style="font-size: 10px;color: gray;">最多只显示'+data.length+'行记录, 如无想要记录, 请输入更多查询条件</span>');
                      }
                    }else if(data.length==0){
                      tableFieldList.append('<span style="font-size: 10px;color: gray;">无记录</span>');
                    }else if(inputStr.length>0 && data.length==10){
                      tableFieldList.append('<span style="font-size: 10px;color: gray;">最多只显示'+data.length+'行记录, 如无想要记录, 请输入更多查询条件</span>');
                    }
                  for(var i = 0; i < data.length; i++){
                	  if(data[i].DOCK_ID){
                		  tableFieldList.append("<li tabindex='"+i+"'><a class='item' loc_type='land' dataId='"+data[i].DOCK_ID
                                  +"' >"+data[i].LOC_NAME
                                  +"</a></li>");
                	  }else if(data[i].PORT_ID){
                		  tableFieldList.append("<li tabindex='"+i+"'><a class='item' loc_type='port' dataId='"+data[i].PORT_ID
                                  +"' >"+data[i].LOC_NAME
                                  +"</a></li>");
                	  }
                  } 
                  tableFieldList.css({ 
                      left:$(me).offset().left+"px", 
                      top:$(me).offset().top+28+"px" 
                  });
                  tableFieldList.show();
                  eeda._inputField = inputField;
                  eeda._hiddenField = hiddenField;
                  if(data.length==0)
		                hiddenField.val('');
		            if(data.length==1&&data[0].LOC_NAME){
//                     inputField.val(data[0].DOCK_NAME);
		            	if(data[0].DOCK_ID){
		            		hiddenField.val(data[0].DOCK_ID);
		            	}else if(data[0].PORT_ID){
		            		hiddenField.val(data[0].PORT_ID);
		            	}
		                  
                    }
		            if(!inputStr && data.length>1){
			                hiddenField.val('');
		            }
              },'json');
            }
          });

          tableFieldList.on('click', '.item', function(e){
              var inputField = eeda._inputField;
              var hiddenField = eeda._hiddenField;
              hiddenField.attr('loc_type',$(this).attr('loc_type'))
              inputField.val($(this).text());//名字
              tableFieldList.hide();
              hiddenField.val($(this).attr('dataId'));

              var input_name = hiddenField.attr('name');
			  var row = inputField.parent().parent().parent();
			  if(input_name=="LOADING_WHARF1"){
	              row.find('.consigner_addr input[name="LOADING_WHARF1"]').attr("locType",$(this).attr('loc_type'));
			  }else if(input_name=="LOADING_WHARF2"){
				  row.find('.consigner_addr input[name="LOADING_WHARF2"]').attr("locType",$(this).attr('loc_type'));
			  }else if(input_name=="DELIVERY_ADDRESS"){
				  row.find('.delivery_address input[name="DELIVERY_ADDRESS"]').attr("locType",$(this).attr('loc_type'));
			  }
          });

          tableFieldList.on('keydown', 'li', function(e){
              if (e.keyCode == 13) {
                  var inputField = eeda._inputField;
                  var hiddenField = eeda._hiddenField;
                  inputField.val($(this).text());//名字
                  tableFieldList.hide();
                  hiddenField.val($(this).find('a').attr('dataId'));//不保存Id, 只保存车牌号

              }
          });
          
          // 1 没选中item，焦点离开，隐藏列表
          $(document).on('click', function(event){
              if (tableFieldList.is(':visible') ){
                  var clickedEl = $(this);
                  var hiddenField = eeda._hiddenField;
                  if ($(this).find('a').val().trim().length ==0) {
                    hiddenField.val('');
                  };
                  tableFieldList.hide();
              }
          });
          
          // 2 当用户只点击了滚动条，没选item，再点击页面别的地方时，隐藏列表
          tableFieldList.on('mousedown', function(){
              return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
          });

          tableFieldList.on('focus', 'li', function() {
              $this = $(this);
              $this.addClass('active').siblings().removeClass();
              // $this.closest('div.container').scrollTop($this.index() * $this.outerHeight());
          }).on('keydown', 'li', function(e) {
              $this = $(this);
              if (e.keyCode == 40) {
                  $this.next().focus();
                  return false;
              } else if (e.keyCode == 38) {
                  $this.prev().focus();
                  return false;
              }
          }).find('li').first().focus();
      };
      
      
      eeda.bindTableFieldTradeItem = function(table_id, el_name,url) {
          var tableFieldList = $('#table_trade_item_input_field_list');
          $('#'+table_id+' input[name='+el_name+'_input]').off('keyup click');
          $('#'+table_id+' input[name='+el_name+'_input]').on('keyup click', function(event){
              var me = this;
              var inputField = $(this);
              var hiddenField = $(this).parent().find('input[name='+el_name+']');
              var inputStr = inputField.val();

              if (event.keyCode == 40) {
                  tableFieldList.find('li').first().focus();
                  return false;
              }

              $.get(url, {input:inputStr}, function(data){
                  if(inputStr!=inputField.val()){//查询条件与当前输入值不相等，返回
                      return;
                  }
                  tableFieldList.empty();
                  for(var i = 0; i < data.length; i++)
                      tableFieldList.append("<li tabindex='"+i+"'><a class='item' dataId='"+data[i].ID+"'"
                              +" dataName='"+data[i].COMMODITY_NAME+"' "
                              +" unit_name='"+data[i].UNIT_NAME+"' "
                              +" vat_rate='"+data[i].VAT_RATE+"' "
                              +" rebate_rate='"+data[i].REBATE_RATE+"' >"+data[i].COMMODITY_NAME
                              +"</a></li>");
                  tableFieldList.css({ 
                      left:$(me).offset().left+"px", 
                      top:$(me).offset().top+28+"px" 
                  });
                  tableFieldList.show();
                  eeda._inputField = inputField;
                  eeda._hiddenField = hiddenField;
                  if(data.length==0)
		                hiddenField.val('');
		            if(data.length==1&&data[0].ID)
		                hiddenField.val(data[0].ID);
		            if(!inputStr){
		            	if(data.length>1)
			                hiddenField.val('');
		            }
              },'json');
          });

          tableFieldList.on('click', '.item', function(e){
              var inputField = eeda._inputField;
              var hiddenField = eeda._hiddenField;
              inputField.val($(this).text());//名字
              tableFieldList.hide();
              var dataId = $(this).attr('dataId');
              hiddenField.val(dataId);//id

              var row = inputField.parent().parent().parent();
              row.find('[name=legal_unit]').val($(this).attr('unit_name'));
              row.find('[name=value_added_tax]').val($(this).attr('vat_rate'));
              row.find('[name=tax_refund_rate]').val($(this).attr('rebate_rate'));

              //选择后跳到下一行的同一个格子
              row.next().find('input[name='+el_name+'_input]').focus();
          });

          tableFieldList.on('keydown', 'li', function(e){
              if (e.keyCode == 13) {
                  var inputField = eeda._inputField;
                  var hiddenField = eeda._hiddenField;
                  inputField.val($(this).text());//名字
                  tableFieldList.hide();
                  var $a = $(this).find('a');
                  var dataId = $a.attr('dataId');
                  hiddenField.val(dataId);//id

                  var row = inputField.parent().parent().parent();
                  row.find('[name=legal_unit]').val($(this).attr('unit_name'));
                  row.find('[name=value_added_tax]').val($(this).attr('vat_rate'));
                  row.find('[name=tax_refund_rate]').val($(this).attr('rebate_rate'));

                  //选择后跳到下一行的同一个格子
                  row.next().find('input[name='+el_name+'_input]').focus();
              }
          });
          
          // 1 没选中item，焦点离开，隐藏列表
          $(document).on('click', function(event){
              if (tableFieldList.is(':visible') ){
                  var clickedEl = $(this);
                  var hiddenField = eeda._hiddenField;
                  if ($(this).find('a').val().trim().length ==0) {
                    hiddenField.val('');
                  };
                  tableFieldList.hide();
              }
          });
          
          // 2 当用户只点击了滚动条，没选item，再点击页面别的地方时，隐藏列表
          tableFieldList.on('mousedown', function(){
              return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
          });

          tableFieldList.on('focus', 'li', function() {
              $this = $(this);
              $this.addClass('active').siblings().removeClass();
              // $this.closest('div.container').scrollTop($this.index() * $this.outerHeight());
          }).on('keydown', 'li', function(e) {
              $this = $(this);
              if (e.keyCode == 40) {
                  $this.next().focus();
                  return false;
              } else if (e.keyCode == 38) {
                  $this.prev().focus();
                  return false;
              }
          }).find('li').first().focus();
      };
      
      //跳转到下一个按下enter键input
      skipNextInput=function(td){
          var row = td.parent();
          var colCount = row.find('td').length;

          var nextTdInput, nextTd=td;
          var index = 0;
          while(!nextTdInput && index<colCount){
              nextTd = nextTd.next();
              index = nextTd.index();
              nextTdInput = nextTd.find('input:last');
              if(nextTdInput && !nextTdInput.prop('disabled')){
                  nextTdInput.focus();
                  break;
              }else{
                  nextTdInput=null;
              }
          }
      }
      
      
      eeda.bindTableFieldChargeId = function(table_id, el_name,url,para) {
		  var tableFieldList = $('#table_fin_item_field_list');
		  $('#'+table_id+' input[name='+el_name+'_input]').off('keyup click');
		  $('#'+table_id+' input[name='+el_name+'_input]').on('keyup click', function(event){
			  
			  var me = this;
			  var inputField = $(this);
			  var hiddenField = $(this).parent().find('input[name='+el_name+']');
			  var inputStr = inputField.val();
			  
			  if (event.keyCode == 40) {
				  tableFieldList.find('li').first().focus();
				  return false;
			  }
			  
			  $.get(url, {input:inputStr,para:para}, function(data){
				  if(inputStr!=inputField.val()){//查询条件与当前输入值不相等，返回
					  return;
				  }
				  tableFieldList.empty();
				  for(var i = 0; i < data.length; i++){
					  var fin_code =  " - "+data[i].CODE 
					  if(!data[i].CODE){
						  fin_code="";
					  }
				  }
				  if(inputStr=='' && data.length>0){
					  if(data[0].REF_ID){
						  tableFieldList.append('<span style="font-size:10px;color:gray;">您曾经使用过的'+data.length+'行记录, 需要别的数据请输入查询条件</span>');
					  }else{
						  tableFieldList.append('<span style="font-size:10px;color:gray;">最多只显示'+data.length+'行记录, 如无想要记录, 请输入更多查询条件</span>');
					  }
				}else if(data.length==0){
					tableFieldList.append('<span style="font-size:10px;color:gray;">没有此费用记录，请设置<a href="/finItem/create" target="_blank">点击此处</a></span>');
				}else if(inputStr.length>0 && data.length==10){
					tableFieldList.append('<span style="font-size:10px;color:gray;">最多只显示'+data.length+'行记录, 如无想要记录, 请输入更多查询条件</span>');
				}
				  for(var i = 0; i < data.length; i++){
					  var codeVal = data[i].CODE;
					  if(codeVal != ''){
						  codeVal = " - "+data[i].CODE 
					  }
				  tableFieldList.append("<li tabindex='"+i+"'><a class='fromLocationItem' dataId='"+data[i].ID
						  +"' charge_name='"+data[i].NAME+"' currency_id='"+data[i].CURRENCY_ID
						  +"' charge_name_eng='"+data[i].NAME_ENG
						  +"' currency_code='"+data[i].CURRENCY_CODE+"' currency_rate='"+data[i].RATE+"' >"+data[i].NAME+codeVal+"</a></li>");
				  }
				  		
				  tableFieldList.css({ 
					  left:$(me).offset().left+"px", 
					  top:$(me).offset().top+28+"px" 
				  });
				  tableFieldList.show();
				  eeda._inputField = inputField;
				  eeda._hiddenField = hiddenField;
				  //tableFieldList;
				  if(data.length==0)
		                hiddenField.val('');
	              if(data.length==1&&data[0].ID)
	                   hiddenField.val(data[0].ID);
	              if(!inputStr){
	            	   if(data.length>1)
		                   hiddenField.val('');
		              }
			  });
		  });
		  
		  tableFieldList.on('click', '.fromLocationItem', function(e){
			  var inputField = eeda._inputField;
			  var hiddenField = eeda._hiddenField;
			  inputField.val($(this).text());//名字
			  tableFieldList.hide();
			  var dataId = $(this).attr('dataId');
			  hiddenField.val(dataId);//id
			  
			  //某条费用对应某币制			  
			  var td = inputField.parent().parent();

			  var charge_id = $(this).attr('dataId');
			  var charge_name = $(this).attr('charge_name');
			  var charge_name_eng = $(this).attr('charge_name_eng');
			  var currency_id = $(this).attr('currency_id');
			  var currency_code = $(this).attr('currency_code');
			  var currency_rate = $(this).attr('currency_rate');
			  td.parent().find('input[name=CHARGE_ID] ').val(charge_id);
			  td.parent().find('input[name=CHARGE_ID_input] ').val(charge_name);
			  td.parent().find('input[name=CHARGE_ENG_ID] ').val(charge_id);
			  td.parent().find('input[name=CHARGE_ENG_ID_input] ').val(charge_name_eng);
			  if(currency_id!='undefined' && currency_id!='null'){
				  td.parent().find('input[name=CURRENCY_ID] ').val(currency_id);	
				  td.parent().find('input[name=CURRENCY_ID_input] ').val(currency_code);
				  td.parent().find('input[name=exchange_currency_id] ').val(currency_id);
				  td.parent().find('input[name=exchange_currency_id_input] ').val(currency_code);
			  }
			  if(currency_rate!='undefined' && currency_rate!='null'){
				  td.parent().find('input[name=exchange_rate] ').val(currency_rate);
				  td.parent().find('input[name= exchange_currency_rate] ').val(1.000000);
				  td.parent().find('input[name= exchange_currency_rate_rmb] ').val(currency_rate);
			  }        
                         
		  });
		  
		  tableFieldList.on('keydown', 'li', function(e){
              if (e.keyCode == 13) {
                  var inputField = eeda._inputField;
                  var hiddenField = eeda._hiddenField;                  
                  var $a = $(this).find('a');
                  
                  inputField.val($a.attr('charge_name'));//名字
                  tableFieldList.hide();
                  var dataId = $a.attr('dataId');
                  hiddenField.val(dataId);//id

                  //某条费用对应某币制			  
    			  var td = inputField.parent().parent();

    			  var charge_id = $a.attr('dataId');
    			  var charge_name = $a.attr('charge_name');
    			  var charge_name_eng = $a.attr('charge_name_eng');
    			  var currency_id = $a.attr('currency_id');
    			  var currency_code = $a.attr('currency_code');
    			  var currency_rate = $a.attr('currency_rate');
    			  td.parent().find('input[name=CHARGE_ID] ').val(charge_id);
    			  td.parent().find('input[name=CHARGE_ID_input] ').val(charge_name);
    			  td.parent().find('input[name=CHARGE_ENG_ID] ').val(charge_id);
    			  td.parent().find('input[name=CHARGE_ENG_ID_input] ').val(charge_name_eng);
    			  if(currency_id!='undefined' && currency_id!='null'){
    				  td.parent().find('input[name=CURRENCY_ID] ').val(currency_id);	
    				  td.parent().find('input[name=CURRENCY_ID_input] ').val(currency_code);
    				  td.parent().find('input[name=exchange_currency_id] ').val(currency_id);
    				  td.parent().find('input[name=exchange_currency_id_input] ').val(currency_code);
    			  }
    			  if(currency_rate!='undefined' && currency_rate!='null'){
    				  td.parent().find('input[name=exchange_rate] ').val(currency_rate);
    				  td.parent().find('input[name= exchange_currency_rate] ').val(1.000000);
    				  td.parent().find('input[name= exchange_currency_rate_rmb] ').val(currency_rate);
    			  }
                  
                  skipNextInput(td);
                  
              }
          });
		  
		  
		  
		  
		  // 1 没选中客户，焦点离开，隐藏列表
		  $(document).on('click', function(event){
			  if (tableFieldList.is(':visible') ){
				  var clickedEl = $(this);
				  var hiddenField = eeda._hiddenField;
				  if ($(this).find('a').val().trim().length ==0) {
					  hiddenField.val('');
				  };
				  tableFieldList.hide();
			  }
		  });
		  
		  // 2 当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
		  tableFieldList.on('mousedown', function(){
			  return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
		  });
		  
		  tableFieldList.on('focus', 'li', function() {
			  $this = $(this);
			  $this.addClass('active').siblings().removeClass();
			  // $this.closest('div.container').scrollTop($this.index() * $this.outerHeight());
		  }).on('keydown', 'li', function(e) {
			  $this = $(this);
			  if (e.keyCode == 40) {
				  $this.next().focus();
				  return false;
			  } else if (e.keyCode == 38) {
				  $this.prev().focus();
				  return false;
			  }
		  }).find('li').first().focus();
	  };
	  
	  
	  
	  
	  eeda.bindTableAddressField = function(table_id, el_name,url,para) {
		  var tableFieldList = $('#table_address_input_field_list');

	      //处理中文输入法, 没完成前不触发查询
	      var cpLock = false;
	      
	      $('#'+table_id+' input[name='+el_name+'_input]').on('compositionstart', function () {
	          cpLock = true;
	      }).on('compositionend', function () {
	          cpLock = false;
	      });
	      	  $('#'+table_id+' input[name='+el_name+'_input]').off('keyup click');
			  $('#'+table_id+' input[name='+el_name+'_input]').on('keyup click', function(event){

				  var me = this;
				  var inputField = $(this);
				  var hiddenField = $(this).parent().find('input[name='+el_name+']');
				  var inputStr = $(this).parents('tr').find('[name='+para+']').val();
           var  addressInputStr=inputField.val();

	        if(cpLock)
	            return;

	        if (event.keyCode == 40) {
	            tableFieldList.find('li').first().focus();
	            return false;
	        }
	        if(event.keyCode==8){
	        	if(inputStr==''){
	        		hiddenField.val('');
	        	}
	        	return ;
	        }else{
				  $.get(url, {input:inputStr,addressInputStr:addressInputStr}, function(data){
	  				  // if(inputStr!=inputField.val()){//查询条件与当前输入值不相等，返回
	  					 //  return;
	  				  // }
	  				  tableFieldList.empty();

	            // if(inputStr=='' && data.length>0){
	            //   if(data[0].REF_ID){
	            //     tableFieldList.append('<span style="font-size: 10px;color: gray;">您曾经使用过的'+data.length+'行记录, 需要别的数据请输入查询条件</span>');
	            //   }else{
	            //     tableFieldList.append('<span style="font-size: 10px;color: gray;">最多只显示'+data.length+'行记录, 如无想要记录, 请输入更多查询条件</span>');
	            //   }
	            // }else if(data.length==0){
	            //   tableFieldList.append('<span style="font-size: 10px;color: gray;">无记录</span>');
	            // }else if(inputStr.length>0 && data.length==10){
	            //   tableFieldList.append('<span style="font-size: 10px;color: gray;">最多只显示'+data.length+'行记录, 如无想要记录, 请输入更多查询条件</span>');
	            // }

	            
              for(var i = 0; i < data.length; i++){
            	 if(data[i].PORT_ID){
            		 tableFieldList.append("<li tabindex='"+i+"'><a class='fromLocationItem' loc_type='port' " 
              		       +"  dataId='"+data[i].DOCK_NAMES+"' " 
              		       +"  contact_man='' " 
              		       +"  consignor_phone='' " 
              		       +"  dockId='" +data[i].PORT_ID+"' "
              		       +">"+data[i].DOCK_NAMES+"</a></li>");
            	 }else{
            		 if(data[i].DOCK_NAMES){
            			 var d_unions=data[i].DOCK_NAMES.split(',');
                         if(d_unions!=undefined && d_unions!=''){
                             for (var j=0;j< d_unions.length;j++) {
                               var d_separate=d_unions[j].split(':');
                               tableFieldList.append("<li tabindex='"+i+"'><a class='fromLocationItem' loc_type='land' " 
                            		       +"  dataId='"+d_separate[0]+"' " 
                            		       +"  contact_man='"+d_separate[1]+"' " 
                            		       +"  consignor_phone='"+d_separate[2]+"' " 
                            		       +"  dockId='" +data[i].DOCK_ID+"' "
                            		       +">"+d_separate[0]+"</a></li>");
                               
                             }
                          }  
            		 }            		 
            	 }
              }

			        tableFieldList.css({ 
				          left:$(me).offset().left+"px", 
	                top:$(me).offset().top+28+"px" 
	            });
	            tableFieldList.show();
	            eeda._inputField = inputField;
	            eeda._hiddenField = hiddenField;
	  	    },'json');
	        }
			  });

			  tableFieldList.on('click', '.fromLocationItem', function(e){
				  var inputField = eeda._inputField;
				  var hiddenField = eeda._hiddenField;
				  hiddenField.attr('loc_type',$(this).attr('loc_type'))
				  inputField.val($(this).text());//名字
				  tableFieldList.hide();
				  var dataId = $(this).attr('dataId');
				  hiddenField.val(dataId);//id
				  var input_name = hiddenField.attr('name');
				  
				  var row = inputField.parent().parent().parent();
				  if(input_name=="TAKE_ADDRESS"){
					  row.find('.consignor_phone input').val($(this).attr('consignor_phone'));
		              row.find('.consignor_contact_man input').val($(this).attr('contact_man')); 
		              row.find('.consignor_contact_id input[name="take_address_id"]').val($(this).attr('dockId')); 
		              row.find('.consignor_contact_id input[name="take_address_id"]').attr("loc_type",$(this).attr('loc_type'));
				  }
				  if(input_name=="DELIVERY_ADDRESS"){
					  row.find('.consignee_phone input').val($(this).attr('consignor_phone'));
		              row.find('.consignee_contact_man input').val($(this).attr('contact_man')); 
		              row.find('.consignee_contact_id input[name="delivery_address_id"]').val($(this).attr('dockId')); 
		              row.find('.consignee_contact_id input[name="delivery_address_id"]').attr("loc_type",$(this).attr('loc_type')); 
				  }
	              
				  
			  });

	      tableFieldList.on('keydown', 'li', function(e){
	        e.preventDefault();
	        if (e.keyCode == 13) {//enter
	          var inputField = eeda._inputField;
	          var hiddenField = eeda._hiddenField;
	          inputField.val($(this).text());//名字
	          tableFieldList.hide();
	          var dataId = $(this).find('a').attr('dataId');
	          hiddenField.val(dataId);//id

	          var td = inputField.parent().parent();
	          var row = td.parent();
	          var colCount = row.find('td').length;

	          var nextTdInput, nextTd=td;
	          var index = 0;
	          while(!nextTdInput && index<colCount){
	              nextTd = nextTd.next();
	              index = nextTd.index();
	              nextTdInput = nextTd.find('input:last');
	              if(nextTdInput && !nextTdInput.prop('disabled')){
	                  nextTdInput.focus();
	                  break;
	              }else{
	                  nextTdInput=null;
	              }
	          }
	        }
	      });

			  // 1 没选中客户，焦点离开，隐藏列表
			  $(document).on('click', function(event){
	        //console.log("tableFieldList.is(':visible') == "+tableFieldList.is(':visible'));
	          if (tableFieldList.is(':visible') ){
	              var clickedEl = $(this);
	              var hiddenField = eeda._hiddenField;
	              if ($(this).find('a').val().trim().length ==0) {
	                  hiddenField.val('');
	              };
	              tableFieldList.hide();
	          }
			  });
			  

			  // 2 当用户只点击了滚动条，没选客户，再点击页面别的地方时，隐藏列表
			  tableFieldList.on('mousedown', function(){
				  return false;//阻止事件回流，不触发 $('#spMessage').on('blur'
			  });

	      tableFieldList.on('focus', 'li', function() {
	          $this = $(this);
	          $this.addClass('active').siblings().removeClass();
	          // $this.closest('div.container').scrollTop($this.index() * $this.outerHeight());
	      }).on('keydown', 'li', function(e) {
	          $this = $(this);
	          if (e.keyCode == 40) {
	              $this.next().focus();
	              return false;
	          } else if (e.keyCode == 38) {
	              $this.prev().focus();
	              return false;
	          }
	      }).find('li').first().focus();
		  };
});