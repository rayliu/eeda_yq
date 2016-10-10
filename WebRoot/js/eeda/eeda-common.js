define(['app/index/todo'], function(todoController){

    //防止退格键返回上一页
    $(document).keydown(function (e) {
        var doPrevent;
        if (e.keyCode == 8) {
            var d = e.srcElement || e.target;
            if (     (d.tagName.toUpperCase() == 'INPUT' && d.type.toUpperCase() =='TEXT')
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
    //控制td 长度
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
    
   $(document).ready(function() {

      var moudleUrl = window.location.pathname.split('/')[1];
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
var eeda={};
window.eeda =eeda;
//dataTables builder for 1.10
eeda.dt = function(opt){
    var option = {
        processing: opt.processing || true,
        searching: opt.searching || false,
        paging: opt.paging || false,
        lengthChange: opt.lengthChange || false,
        serverSide: opt.serverSide || false, 
        scrollX: opt.scrollX || true,
        responsive:opt.responsive || true,
        //scrollY: opt.scrollY || true, //"300px",
        scrollCollapse: opt.scrollCollapse || true,
        autoWidth: opt.autoWidth || false,
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
        ajax: opt.ajax || '',
        // ajax: {
        //   url: opt.ajax || '',
        //   error: function (xhr, error, thrown) {
        //     if(xhr.responseText.indexOf('忘记密码')>0){
        //       alert( '您未登录, 请重新登录.' );
        //     }else{
        //       console.log(thrown);
        //       alert('表格处理出错了, 请联系技术人员查看.' );
        //     }
        //   }
        // } || '',
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
	 	 if(orderNo.indexOf("JH") == 0){//配送
	         str = "<a href='/planOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
	     }else if(orderNo.indexOf("EK") == 0 ){//拼车
	         str = "<a href='/jobOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
	     }else if(orderNo.indexOf("SGFK") == 0){//手工付款
	         str = "<a href='/costMiscOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
	     }else if(orderNo.indexOf("SGSK") == 0){//手工收款
	         str = "<a href='/chargeMiscOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
	     }else if(orderNo.indexOf("YFSQ") == 0){//应付申请
	         str = "<a href='/costPreInvoiceOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
	     }else if(orderNo.indexOf("YSSQ") == 0){//应收申请
	         str = "<a href='/chargeAcceptOrder/edit?id="+id+"' target='_blank'>"+orderNo+"</a>";
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
	                 return parseFloat(t).toFixed(2);
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
   
   //dataTable里的下拉列表，查询参数为input,url,添加的参数para,下拉显示的数据库字段
   eeda.bindTableField = function(table_id, el_name,url,para) {
		  var tableFieldList = $('#table_input_field_list');
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
  				  for(var i = 0; i < data.length; i++)
  					  tableFieldList.append("<li tabindex='"+i+"'><a class='fromLocationItem' dataId='"+data[i].ID
  							  +"' dataName='"+data[i].NAME+"' >"+data[i].NAME+"</a></li>");
  				  tableFieldList.css({ 
  					  left:$(me).offset().left+"px", 
              top:$(me).offset().top+28+"px" 
            });
            tableFieldList.show();
            eeda._inputField = inputField;
            eeda._hiddenField = hiddenField;
            //tableFieldList;
  	    },'json');
		  });
		  
		  tableFieldList.on('click', '.fromLocationItem', function(e){
			  var inputField = eeda._inputField;
			  var hiddenField = eeda._hiddenField;
			  inputField.val($(this).text());//名字
			  tableFieldList.hide();
			  var dataId = $(this).attr('dataId');
			  hiddenField.val(dataId);//id
		  });

      tableFieldList.on('keydown', 'li', function(e){
        if (e.keyCode == 13) {
          var inputField = eeda._inputField;
          var hiddenField = eeda._hiddenField;
          inputField.val($(this).text());//名字
          tableFieldList.hide();
          var dataId = $(this).attr('dataId');
          hiddenField.val(dataId);//id
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
	  
	  eeda.bindTableFieldCurrencyId = function(table_id, el_name,url,para) {
		  var tableFieldList = $('#table_currency_input_field_list');
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
			  },'json');
		  });
		  
		  tableFieldList.on('click', '.fromLocationItem', function(e){
			  var inputField = eeda._inputField;
			  var hiddenField = eeda._hiddenField;
			  inputField.val($(this).text());//名字
			  tableFieldList.hide();
			  var dataId = $(this).attr('dataId');
			  hiddenField.val(dataId);//id
			  
			  var row = inputField.parent().parent().parent();
              row.find('.currency_rate input').val($(this).attr('currency_rate'));//选择币制则填入汇率
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
                  row.find('.currency_rate input').val($a.attr('currency_rate'));
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
      var item_table_rows = $("#"+table_id+" tr");
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
        for(var index=0; index<deletedTableIds.length; index++){
            var id = deletedTableIds[index];
            var item={
                id: id,
                action: 'DELETE'
            };
            items_array.push(item);
        }
        deletedTableIds = [];
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
      		curDateTime = curDateTime +"-"+date;
      	else
      		curDateTime = curDateTime +"-0"+date;
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

    eeda.bindTableFieldTruckOut = function(talbe_id, el_name) {
          var tableFieldList = $('#table_truck_out_input_field_list');
          $('#'+talbe_id+' input[name='+el_name+'_input]').on('keyup click', function(event){
              var me = this;
              var inputField = $(this);
              var hiddenField = $(this).parent().find('input[name='+el_name+']');
              var inputStr = inputField.val();

              if (event.keyCode == 40) {
                  tableFieldList.find('li').first().focus();
                  return false;
              }

              $.get('/serviceProvider/searchTruckOut', {input:inputStr}, function(data){
                  if(inputStr!=inputField.val()){//查询条件与当前输入值不相等，返回
                      return;
                  }
                  tableFieldList.empty();
                  for(var i = 0; i < data.length; i++)
                      tableFieldList.append("<li tabindex='"+i+"'><a class='item' dataId='"+data[i].ID
                              +"' dataName='"+data[i].NAME+"' "
                              +" phone='"+data[i].PHONE+"' "
                              +" addr='"+data[i].ADDRESS+"' >"+data[i].NAME
                              +"</a></li>");
                  tableFieldList.css({ 
                      left:$(me).offset().left+"px", 
                      top:$(me).offset().top+28+"px" 
                  });
                  tableFieldList.show();
                  eeda._inputField = inputField;
                  eeda._hiddenField = hiddenField;
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
              row.find('.consigner_phone input').val($(this).attr('phone'));
              row.find('.consigner_addr input').val($(this).attr('addr'));
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

    eeda.bindTableFieldTruckIn = function(talbe_id, el_name) {
          var tableFieldList = $('#table_truck_in_input_field_list');
          $('#'+talbe_id+' input[name='+el_name+'_input]').on('keyup click', function(event){
              var me = this;
              var inputField = $(this);
              var hiddenField = $(this).parent().find('input[name='+el_name+']');
              var inputStr = inputField.val();

              if (event.keyCode == 40) {
                  tableFieldList.find('li').first().focus();
                  return false;
              }

              $.get('/serviceProvider/searchTruckIn', {input:inputStr}, function(data){
                  if(inputStr!=inputField.val()){//查询条件与当前输入值不相等，返回
                      return;
                  }
                  tableFieldList.empty();
                  for(var i = 0; i < data.length; i++)
                      tableFieldList.append("<li tabindex='"+i+"'><a class='item' dataId='"+data[i].ID
                              +"' dataName='"+data[i].NAME+"' "
                              +" phone='"+data[i].PHONE+"' "
                              +" addr='"+data[i].ADDRESS+"' >"+data[i].NAME
                              +"</a></li>");
                  tableFieldList.css({ 
                      left:$(me).offset().left+"px", 
                      top:$(me).offset().top+28+"px" 
                  });
                  tableFieldList.show();
                  eeda._inputField = inputField;
                  eeda._hiddenField = hiddenField;
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
              row.find('.consignee_phone input').val($(this).attr('phone'));
              row.find('.consignee_addr input').val($(this).attr('addr'));
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
      
      
});