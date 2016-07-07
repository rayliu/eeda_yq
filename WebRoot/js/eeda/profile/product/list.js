define(['jquery', 'metisMenu', 'zTree', 'sb_admin',  'dataTablesBootstrap', 'validate_cn'], function ($, metisMenu) { 

	document.title = '产品查询 | '+document.title;
    $('#menu_profile').addClass('active').find('ul').addClass('in');
    
    // 显示所有的客户
    $.get('/product/searchAllCustomer', function(data){
       // console.log(data);
        var zNodes =[];
        for(var i=0;i<data.length && data.length>0;i++){
	        var node={};
	        node.name=data[i].ABBR;
	        node.isParent=true;
	        node.customerId=data[i].PID;
	        node.categoryId=data[i].CAT_ID;
	        node.click=nodePlusClickHandler;
	        zNodes.push(node);
	        //console.log(node);
        }
        $.fn.zTree.init($("#categoryTree"), setting, zNodes);
    },'json');
    
	// 树状结构点击效果
    $('.tree li:has(ul)').addClass('parent_li').find(' > span').attr('title', 'Collapse this branch');
    
    $('.tree li.parent_li > span').on('click', function (e) {
        var children = $(this).parent('li.parent_li').find(' > ul > li');
        if (children.is(":visible")) {
            children.hide('fast');
            $(this).attr('title', 'Expand this branch').find(' > i').addClass('icon-plus-sign').removeClass('icon-minus-sign');
        } else {
            children.show('fast');
            $(this).attr('title', 'Collapse this branch').find(' > i').addClass('icon-minus-sign').removeClass('icon-plus-sign');
        }
        e.stopPropagation();
    });
    //获取所有单位
    $.post('/unit/searchAllUnit',function(data){
      	 if(data.length > 0){
      		 var unitOptions = $("#unitOptions");
      		 unitOptions.empty();
      		 unitOptions.append("<option ></option>");
      		 for(var i=0; i<data.length; i++){
      			unitOptions.append("<option value='"+data[i].NAME+"'>"+data[i].NAME+"</option>");	
      		 }
      	 }
       },'json');
	//datatable, 动态处理
    productDataTable = $('#eeda-table').dataTable({
    	"sDom": "<'row-fluid'<'span6'l><'span6'f>r><'datatable-scroll't><'row-fluid'<'span12'i><'span12 center'p>>",
        "bFilter": true, //不需要默认的搜索框
        //"sPaginationType": "bootstrap",
        "iDisplayLength": 10,
        "aLengthMenu": [ [10, 25, 50, 100, 9999999], [10, 25, 50, 100, "All"] ],
        "bServerSide": true,
        "sAjaxSource": "/product/list",
    	"oLanguage": {
            "sUrl": "/eeda/dataTables.ch.txt"
        },
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
			$(nRow).attr('id', aData.ID);
			return nRow;
		},
        "aoColumns": [
				{ 
				    "mDataProp": null, 
				    "sWidth": "60px", 
				    "bVisible":Product.isDel,
					"sClass": "item_desc",
				    "fnRender": function(obj) {
				    	var str="<nobr>";
				    	if(Product.isDel){
				    		if(obj.aData.IS_STOP != true){
				    			str +="<a class='btn btn-danger  btn-xs deleteProduct' id="+obj.aData.ID+" title='停用'>"+
					                    "<i class='fa fa-edit fa-fw'></i>"+
					                    "</a>";
				        	}else{
				        		str +=	"<a class='btn btn-success  btn-xs deleteProduct' id="+obj.aData.ID+" title='启用'>"+
					                    "<i class='fa fa-edit fa-fw'></i>"+
					                    "</a>";
				        	}
				    	}
				    	if(Product.isUpdate){
				    		str +="<a class='btn btn-primary btn-xs updateProduct' data-toggle='modal' data-target='#addOrder' data-backdrop='static' id="+obj.aData.ID+" target='_blank' title='编辑'>"+
                            "<i class='fa fa-edit fa-fw'></i>"+
                            "</a> "
				    	}
				    	str +="</nobr>";
				        return str;
				    }
				}, 
            {
            	"mDataProp":"ITEM_NO",
            	"sClass": "item_no",
            	"fnRender":function(obj){
            		var str = obj.aData.ITEM_NO ;
            		if(str == null){
            			str ="";
            		}
            		return str;
            	}
            },       	
            {
            	"mDataProp":"SERIAL_NO",
            	"sClass": "serial_no",
            	"fnRender":function(obj){
            		var str = obj.aData.SERIAL_NO ;
            		if(str == null){
            			str ="";
            		}
            			
            		return str;
            	}
            },        	
            {
            	"mDataProp":"ITEM_NAME",
            	"sClass": "item_name",
            	"fnRender":function(obj){
            		var str = obj.aData.ITEM_NAME ;
            		if(str == null){
            			str ="";
            		}
            		return str;
            	}
            },
            {
            	"mDataProp":"SIZE", 
            	"sClass": "size",
            	"fnRender":function(obj){
            		var str = obj.aData.SIZE ;
            		if(str == null){
            			str ="";
            		}
            		return str;
            	}
        	},
            {
            	"mDataProp":"WIDTH",
            	"sClass": "width",
            	"fnRender":function(obj){
            		var str = obj.aData.WIDTH ;
            		if(str == null){
            			str ="";
            		}
            		return str;
            	}
            },
            {
            	"mDataProp":"HEIGHT", 
            	"sClass": "height",
            	"fnRender":function(obj){
            		var str = obj.aData.HEIGHT ;
            		if(str == null){
            			str ="";
            		}
            		return str;
            	}
        	},
        	{
            	"mDataProp":"INSURANCE_AMOUNT", 
            	"sClass": "insurance_amount",
            	"fnRender":function(obj){
            		var str = obj.aData.INSURANCE_AMOUNT;
            		if(str == null){
            			str ="";
            		}
            		return str;
            	}
        	},
            {
            	"mDataProp":"CATEGORY_NAME",            	
            	"sWidth": "100px",
            	"sClass": "category",
            	"fnRender":function(obj){
            		var str =obj.aData.CATEGORY_NAME;
            		if(str==null){
            			str = '';
            		}
            		return str;
            	}
        	},
        	{
            	"mDataProp":"UNIT",
            	"sClass": "unit",
            	"fnRender":function(obj){
		        	var str =obj.aData.UNIT;
            		if(str==null){
            			str = '';
            		}
            		return str;
            	}
            },
            {
            	"mDataProp":"VOLUME",
            	"sClass": "volume",
            	"fnRender":function(obj){
            		var str = obj.aData.VOLUME;
            		if(str == null){
            			str ="";
            		}
            		return str;
            	}
            }, 
            {
            	"mDataProp":"WEIGHT",
            	"sClass": "weight",
            	"fnRender":function(obj){
            		var str = obj.aData.WEIGHT ;
            		if(str == null){
            			str ="";
            		}
            		return str;
            	}
            },
            {"mDataProp":"ITEM_DESC",
            	"fnRender":function(obj){
            		var str = obj.aData.ITEM_DESC;
            		if(str == null){
            			str ="";
            		}
            		return str;
            	}}
        ],      
    });
   
  /*  var alerMsg='<div id="message_trigger_err" class="alert alert-danger alert-dismissable" style="display:none">'+
			    '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>'+
			    'Lorem ipsum dolor sit amet, consectetur adipisicing elit. <a href="#" class="alert-link">Alert Link</a>.'+
			    '</div>';
    $('body').append(alerMsg);*/
    // 计算体积
	var sumVolume = function(currentEle){
		$(currentEle).parent().children('.volume')[0].innerHTML = parseFloat($(currentEle).parent().children('.size')[0].innerHTML)/1000 * parseFloat($(currentEle).parent().children('.width')[0].innerHTML)/1000 * parseFloat($(currentEle).parent().children('.height')[0].innerHTML)/1000;
	};

	// 刷新产品列表
	var refreshProductTable = function(){
		var categoryId = $("#categoryId").val();
		// 刷新产品列表
		productDataTable.fnSettings().sAjaxSource = "/product/list?categoryId="+categoryId;
		productDataTable.fnDraw();
	};
	
                                                      
        
    var nodePlusClickHandler= function(e){
    	//e.preventDefault();
    	var customerId = $('#customerId').val();
    	var categoryId = $(this).find('li').attr('categoryId');
    	var nodeElement = $(this);
    	nodeElement.unbind('click');
    	$.get('/product/searchNodeCategory', {categoryId: categoryId, customerId: customerId}, function(data){
    		//console.log(data);
    		if(data.length > 0){		
	    		addNode(data, nodeElement);
    		}else{
    			console.log(nodeElement);
    		}
    	},'json');
    };
    
    var addNode = function(data, nodeElement){
    	nodeElement.find('ul').empty();
    	for(var i = 0; i < data.length; i++){
    		nodeElement.append(function() {
    			return $("<ul><li class='parent_li productList' categoryId='"+data[i].ID+"'><span ><i class='fa fa-folder'>" +    		
				"</i>  <a href='javascript:void(0)' id='"+data[i].ID+"'>"+data[i].NAME+
				"</a></span>  <div class='edit_icons'><a class='fa fa-plus addCategory' href='javascript:void(0)' title='添加' id='"+data[i].ID+"'></a>  " +
				"<a class='fa fa-pencil editCategory' href='javascript:void(0)' title='编辑' id='"+data[i].ID+"'></a>  " +
				"<a class='fa fa-trash-o deleteCategory' href='javascript:void(0)' title='删除' id='"+data[i].ID+"'></a></div></li></ul>").click(nodePlusClickHandler);
    			}
    		);
    	}
    };
    
    $("#productTreeLi").on('click', '.p roductList', function(e){    	
    	//显示子目录
    	var customerId = $('#customerId').val();
    	var categoryId = $(this).attr('categoryId');
    	var nodeElement = $(this);
    	$.get('/product/searchNodeCategory', {categoryId: categoryId, customerId: customerId}, function(data){
    		if(data.length > 0){
	    		addNode(data, nodeElement);
    		}
    	},'json');
    	//显示产品列表
    	e.preventDefault();
    	/*var categoryId = $(this).attr('id');
    	$("#hiddenCategoryId").val(categoryId);
    	$("#categoryId").val(categoryId);
    	*/
    	if($("#displayDiv").attr('style') != ""){
    		$("#displayDiv").show();
    		productDataTable.fnSettings().sAjaxSource = "/product/list?categoryId="+categoryId;
        	productDataTable.fnDraw();
    	}else{
    		productDataTable.fnSettings().sAjaxSource = "/product/list?categoryId="+categoryId;
        	productDataTable.fnDraw();
    	} 
    });
	
    // 保存产品
    $("#productFormBtn").click(function(){
    	$.post('/product/save', $("#productForm").serialize(), function(data){    
    		//保存成功后，刷新列表
            console.log(data);
            if(data.ID>0){
            	productDataTable.fnSettings().sAjaxSource = "/product/list?categoryId="+$("#categoryId").val();
                productDataTable.fnDraw();
            }else{
                alert('数据保存失败。');
            }
    		$('#myModal').modal('hide');	
    		$("#productForm")[0].reset();
        },'json');
    });
    
    var searchAllCategory=function(){
    	var customerId = $('#customerId').val();
        if(customerId > 0){
        	$.get('/product/searchAllCategory', {customerId:customerId}, function(data){
    			console.log(data);	
    			var productTreeUl = $("#productTreeUl");
    			productTreeUl.empty();
    			if(data.length > 0){
    				for(var i = 0; i < data.length; i++)
    				{
    					var id = data[i].ID;
    					if(data[i].PARENT_ID == null){
	    					productTreeUl.append("<li class='parent_li'><span><i class='fa fa-folder'>" +
	    							"</i>  <a href='javascript:void(0)' class='productList' id='"+data[i].ID+"'>"+data[i].NAME+
	    							"</a></span>  <div class='edit_icons'><a class='fa fa-plus addCategory' href='javascript:void(0)' title='添加' id='"+data[i].ID+"'></a>  " +
	    							"<a class='fa fa-pencil editCategory' href='javascript:void(0)' title='编辑' id='"+data[i].ID+"'></a>  " +
	    							"<a class='fa fa-trash-o deleteCategory' href='javascript:void(0)' title='删除' id='"+data[i].ID+"'></a></div></li></ul></li>");
	    					for(var j = 0; j < data.length; j++)
	        				{
	    						if(data[j].PARENT_ID == id){
		    						productTreeUl.append("<ul><li class='parent_li'><span><i class='fa fa-folder'>" +
			    							"</i>  <a href='javascript:void(0)' class='productList' id='"+data[j].ID+"'>"+data[j].NAME+
			    							"</a></span>  <div class='edit_icons'><a class='fa fa-plus addCategory' href='javascript:void(0)' title='添加' id='"+data[j].ID+"'></a>  " +
			    							"<a class='fa fa-pencil editCategory' href='javascript:void(0)' title='编辑' id='"+data[j].ID+"'></a>  " +
			    							"<a class='fa fa-trash-o deleteCategory' href='javascript:void(0)' title='删除' id='"+data[j].ID+"'></a></div></li></ul></li></ul>");
	    						}
	    					}
    					}
    				}
    			}
    		},'json');
        }
    };
    
    // 编辑产品
    $("#eeda-table").on('click', '.editProduct', function(e){
    	var productId = $(this).attr('id');
		$("#productId").val(productId);	
		$.post('/product/getProduct', {productId:productId}, function(data){
  	    	// 编辑时回显数据
			$("#hiddenProductId").val(data.ID);
			$("#categoryId").val(data.CATEGORY_ID);	    	
  	    	$("#item_no").val(data.ITEM_NO);
  	    	$("#item_name").val(data.ITEM_NAME);
  	    	$("#size").val(data.SIZE);	    	
  	    	$("#width").val(data.WIDTH);	    	
  	    	$("#height").val(data.HEIGHT);	    	
  	    	$("#unit").val(data.UNIT);	    	
  	    	$("#volume").val(data.VOLUME);	    	
  	    	$("#weight").val(data.WEIGHT);	  
  	    	$("#insurance_amount").val(data.INSURANCE_AMOUNT);	
  	    	$("#categorySelect").val(data.CNAME);	    	
  	    	$("#item_desc").val(data.ITEM_DESC);    	
		},'json');
  		// 模态框:修改货品明细
		$('#myModal').modal('show');	
	});
    
    // 删除产品
    $("#eeda-table").on('click', '.deleteProduct', function(e){
    	var productId = $(this).attr('id');	
    	$.post('/product/delete', {productId:productId}, function(data){ 	
    		
		},'json');
    	refreshProductTable();
    });
    $.post('/gateOut/alluom',function(data){
		var uom=$('#unit_update');
		uom.empty();
		uom.append("<option>请选择</option>");
		for(var i=0; i<data.length; i++){
			var name=data[i].NAME;
			uom.append("<option value='"+name+"'>"+name+"</option>");
		}
	},'json');
    // 编辑产品
    $("#eeda-table").on('click', '.updateProduct', function(e){
    	var productId = $(this).attr('id');
    	$("#itemId").val(productId);	
    	$.post('/product/update', {productId:productId}, function(data){ 	
    		$("#item_no_update").val(data.ITEM_NO); 
    		$("#item_no_hidden").val(data.ITEM_NO); 
    		$("#serial_no_update").val(data.SERIAL_NO); 
    		$("#item_name_update").val(data.ITEM_NAME);
    		$("#size_update").val(data.SIZE);
    		$("#width_update").val(data.WIDTH);
    		$("#height_update").val(data.HEIGHT);
    		$("#insurance_amount_update").val(data.INSURANCE_AMOUNT);
    		$("#category_update").val(data.CNAME);
    		$("#unit_update").val(data.UNIT);
    		$("#volume_update").val(data.VOLUME);
    		$("#weight_update").val(data.WEIGHT);
    		$("#item_desc_update").val(data.ITEM_DESC);
		},'json');
    });
    $('#addOrder').on('hidden.bs.modal', function () {
    	$("#itemId").val("");
    	$("#item_no_update").val(""); 
    	$("#item_no_hidden").val(""); 
		$("#serial_no_update").val(""); 
		$("#item_name_update").val("");
		$("#size_update").val("");
		$("#width_update").val("");
		$("#height_update").val("");
		$("#insurance_amount_update").val("");
		$("#category_update").val("");
		$("#unit_update").val("");
		$("#volume_update").val("");
		$("#weight_update").val("");
		$("#item_desc_update").val("");
    });
    $("#editProduct").on('click', function(e){
    	var categoryId = $("#categoryId").val();
    	$.post('/product/getCategory', {categoryId:categoryId}, function(data){
    		$("#category_update").val(data.CNAME);
		});
    });
    var selectCategory = function(){
    	// 获取所有类别
    	/*var treeObj = $.fn.zTree.getZTreeObj("categoryTree");
    	var sNodes = treeObj.getSelectedNodes();
    	if (sNodes.length > 0) {
    		var node = sNodes[0].getParentNode();
    		var categoryId = node.ID;
    		var customerId = sNodes[0].customerId;
    	}*/
    	$.get('/product/findCategory', {categoryId:$("#categoryId").val()}, function(data){
	   		if(data.ID > 0){
		   		$("#categorySelect").val(data.NAME);
	   		}
   	    },'json');
    };
    $("#item_no_update,#size_update,#width_update,#height_update,#volume_update,#weight_update,#insurance_amount_update").on('keyup click', function(){
    	var item_no_update=$("#item_no_update").val();
    	var size_update=$("#size_update").val();
    	var width_update=$("#width_update").val();
    	var height_update=$("#height_update").val();
    	var volume_update=$("#volume_update").val();
    	var weight_update=$("#weight_update").val();
    	var insurance_amount_update=$("#insurance_amount_update").val();
    	if(item_no_update!=""){
    		$("#item_no_err").hide();
    	}else{
    		$("#item_no_err").show();
    	}
    	if(!isNaN(size_update)){
    		$("#size_err").hide();
    	}else{
    		$("#size_err").show();
    	}
		if(!isNaN(width_update)){
			$("#width_err").hide();
		}else{
			$("#width_err").show();
    	}
		if(!isNaN(height_update)){
			$("#height_err").hide();
		}else{
			$("#height_err").show();
    	}
		if(!isNaN(insurance_amount_update)){
			$("#insurance_amount_err").hide();
		}else{
			$("#insurance_amount_err").show();
    	}
		if(!isNaN(volume_update)){
			$("#volume_err").hide();
		}else{
			$("#volume_err").show();
    	}
		if(!isNaN(weight_update)){
			$("#weight_err").hide();
		}else{
			$("#weight_err").show();
    	}	
    });
    // 新增或者编辑产品 
    $('#productSave').on('click', function() { 
    	var item_no_update=$("#item_no_update").val();
    	var size_update=$("#size_update").val();
    	var width_update=$("#width_update").val();
    	var height_update=$("#height_update").val();
    	var volume_update=$("#volume_update").val();
    	var weight_update=$("#weight_update").val();
    	var insurance_amount_update=$("#insurance_amount_update").val();
    	if(item_no_update==""){
    		$("#item_no_err").show();
    		return false;
    	}
    	if(isNaN(size_update)){
    		$("#size_err").show();
    		return false;
    	}
		if(isNaN(width_update)){
			$("#width_err").show();
    		return false;	
		}
		if(isNaN(height_update)){
			$("#height_err").show();
    		return false;
		}
		if(isNaN(insurance_amount_update)){
			$("#insurance_amount_err").show();
    		return false;
		} 
		if(isNaN(volume_update)){
			$("#volume_err").show();
    		return false;
		}
		if(isNaN(weight_update)){
			$("#weight_err").show();
    		return false;
		}	
 		$.post('/product/productSave',$("#itemForm").serialize(), function(data){
 			if(data=="item_no"){
 				$.scojs_message('该型号已存在', $.scojs_message.TYPE_ERROR);			
            }else{
                $.scojs_message('数据保存成功', $.scojs_message.TYPE_OK);
 				$('#addOrder').modal('hide')
 				refreshProductTable();
            }
 		});

    });
    
    // 计算产品体积
    $('#height, #width, #size').on('keyup click', function() { 
    	var height = $('#height').val();
    	var width = $('#width').val();
    	var size = $('#size').val();
    	$("#volume").val((height/1000)*(width/1000)*(size/1000));
    });


    //---------------tree handle
    var setting = {
            view: {
                addHoverDom: addHoverDom,
                removeHoverDom: removeHoverDom,
                selectedMulti: false
            },
            edit: {
                enable: true,
                editNameSelectAll: true,
                showRemoveBtn: showRemoveBtn,
                showRenameBtn: showRenameBtn
            },
            async: {
                enable: true,
                url:"/product/searchNodeCategory",
                autoParam:["categoryId", "customerId", "level=lv"],
                otherParam:{"otherParam":"zTreeAsyncTest"},
                dataFilter: filter
            },
            callback: {
                beforeClick: beforeClick,
                onClick: onNodeClick,
                beforeAsync: beforeAsync,
                onAsyncError: onAsyncError,
                onAsyncSuccess: onAsyncSuccess,
                beforeExpand: beforeExpand,
                beforeDrag: beforeDrag,
                beforeEditName: beforeEditName,
                beforeRemove: beforeRemove,
                beforeRename: beforeRename,
                onRemove: onRemove,
                onRename: onRename
            }
        };
        
        //-------------edit--------------
        var log, className = "dark";
        function beforeDrag(treeId, treeNodes) {
            return false;
        }

        function beforeEditName(treeId, treeNode) {
            className = (className === "dark" ? "":"dark");
            showLog("[ "+getTime()+" beforeEditName ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name);
            var zTree = $.fn.zTree.getZTreeObj("categoryTree");
            zTree.selectNode(treeNode);
            return true;
            //confirm("进入节点 -- " + treeNode.name + " 的编辑状态吗？");
        }
        function beforeRemove(treeId, treeNode) {
            className = (className === "dark" ? "":"dark");
            showLog("[ "+getTime()+" beforeRemove ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name);
            var zTree = $.fn.zTree.getZTreeObj("categoryTree");
            zTree.selectNode(treeNode);
            return confirm("确认删除 类别: " + treeNode.name + " 吗？其类别下所有产品也将被删除。");
        }
        function onRemove(e, treeId, treeNode) {
            showLog("[ "+getTime()+" onRemove ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name);
            var pNode = treeNode.getParentNode();
            pNode.isParent = true;
            var zTree = $.fn.zTree.getZTreeObj("categoryTree");
            $.post('/product/deleteCategory', {categoryId: treeNode.categoryId}, function(data){  
            	if(data.success){
	                zTree.reAsyncChildNodes(pNode, "refresh");
	                
	                zTree.selectNode(pNode,false);
	                //设置选中节点后右边编辑内容的载入
	                onNodeClick(e, pNode.categoryId, pNode);
            	}else{
            		alert("该类别下面的产品已被引用不能删除!");
            	}
            },'json');

            
        }
        function beforeRename(treeId, treeNode, newName, isCancel) {
            className = (className === "dark" ? "":"dark");
            showLog((isCancel ? "<span style='color:red'>":"") + "[ "+getTime()+" beforeRename ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name + (isCancel ? "</span>":""));
            if (newName.length == 0) {
                alert("节点名称不能为空.");
                var zTree = $.fn.zTree.getZTreeObj("categoryTree");
                setTimeout(function(){zTree.editName(treeNode)}, 10);
                return false;
            }
            return true;
        }
        function onRename(e, treeId, treeNode, isCancel) {
            showLog((isCancel ? "<span style='color:red'>":"") + "[ "+getTime()+" onRename ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name + (isCancel ? "</span>":""));
            var name = treeNode.name;
            $("#customerId").val(treeNode.customerId);
            var isNodeExist =false;
            var id = treeNode.id;
            if(id == null){
            	id = treeNode.ID;
            }
            $.ajax({  
                type : "post",  
                url : "/product/checkCategory",  
                data : {id: id, name: name},  
                async : false,  
                success : function(data){  
                	subNodes=data; 
                	for(var i=0;i<data.categories.length && data.categories.length > 0;i++){
	                	if(data.name == data.categories[i].NAME){
	                		isNodeExist = true;
	                	}
                	}
                }  
            });
            if(isNodeExist){
            	alert("该类别已存在!");
            	treeNode.name = treeNode.NAME;
            }else{
                $.post('/product/saveCategory', {categoryId: treeNode.categoryId, customerId: treeNode.customerId, categoryName:name}, function(data){     
                },'json');
            }
        }
        function showRemoveBtn(treeId, treeNode) {
            //根节点，不能删除
            if(treeNode.level==0)
                return false;
            //有子节点，不能删除
            var subNodes=[];
            $.ajax({  
                  type : "post",  
                  url : "/product/searchNodeCategory",  
                  data : {categoryId: treeNode.categoryId, customerId: treeNode.customerId},  
                  async : false,  
                  success : function(data){  
                    subNodes=data; 
                  }  
             });

            if(subNodes.length>0)
                return false;
            return true;
            
        }

        function showRenameBtn(treeId, treeNode) {
            //根节点，不能重命名
            if(treeNode.level==0)
                return false;
            
            return true;
        }

        var newCount = 1;
        function addHoverDom(treeId, treeNode) {
            var sObj = $("#" + treeNode.tId + "_span");
            if (treeNode.editNameFlag || $("#addBtn_"+treeNode.tId).length>0) return;
            var addStr = "<span class='button add' id='addBtn_" + treeNode.tId
                + "' title='添加类别' onfocus='this.blur();'></span>";
            sObj.after(addStr);
            var btn = $("#addBtn_"+treeNode.tId);
            if (btn) btn.bind("click", function(){
                //异步创建节点
                var zTree = $.fn.zTree.getZTreeObj("categoryTree");
                var nodeName = "新类别" + (newCount++);                
               
                // 7-5 使用异步会导致树节点添加两次， 因为自己手动加了一个，ztree自己异步自动又加了一个
                $.post('/product/addCategory', {categoryId: treeNode.categoryId, customerId: treeNode.customerId, name:nodeName}, function(data){
                    if ((!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) || treeNode.zAsync){ 
                        zTree.addNodes(treeNode, {id:data.ID, categoryId: data.ID, customerId: treeNode.customerId, isParent:true, name:nodeName});
                        zTree.reAsyncChildNodes(treeNode, "refresh");
                    } else{
                        zTree.reAsyncChildNodes(treeNode, "refresh");
                    }
                },'json');

                
                return false;
            });
        };
        function removeHoverDom(treeId, treeNode) {
            $("#addBtn_"+treeNode.tId).unbind().remove();
        };
        function selectAll() {
            var zTree = $.fn.zTree.getZTreeObj("categoryTree");
            zTree.setting.edit.editNameSelectAll =  $("#selectAll").attr("checked");
        }
        //-------------edit end--------------
        function onNodeClick(event, treeId, treeNode){
        	if(treeNode.parentTId != null){
	        	$("#addProductDiv").show();
	            $("#displayDiv").show();
        	}else{
        		$("#addProductDiv").hide();
	            $("#displayDiv").hide();
        	}
            $("#categoryId").val(treeNode.categoryId);
            $("#category_UpdateId").val(treeNode.categoryId);
            $("#hiddenCategoryId").val(treeNode.categoryId);
            productDataTable.fnSettings().sAjaxSource = "/product/list?categoryId="+treeNode.categoryId;
            productDataTable.fnDraw();
        }

        function filter(treeId, parentNode, childNodes) {
            if (!childNodes) return null;
            for (var i=0, l=childNodes.length; i<l; i++) {
                childNodes[i].name = childNodes[i].NAME.replace(/\.n/g, '.');
                childNodes[i].categoryId = childNodes[i].ID;
                childNodes[i].customerId = childNodes[i].CUSTOMER_ID;
                childNodes[i].parentId = childNodes[i].PARENT_ID;
                childNodes[i].isParent=true;
            }
            return childNodes;
        }
        function beforeClick(treeId, treeNode) {
            if (!treeNode.isParent) {
                alert("pls select parent node");
                return false;
            } else {
                return true;
            }
        }
        var log, className = "dark";
        function beforeAsync(treeId, treeNode) {
            className = (className === "dark" ? "":"dark");
            showLog("[ "+getTime()+" beforeAsync ]" + ((!!treeNode && !!treeNode.name) ? treeNode.name : "root") +", treeNode:"+treeNode);
            return true;
        }
        function onAsyncError(event, treeId, treeNode, XMLHttpRequest, textStatus, errorThrown) {
            showLog("[ "+getTime()+" onAsyncError ]" + ((!!treeNode && !!treeNode.name) ? treeNode.name : "root") +", treeNode:"+treeNode);
        }
        function onAsyncSuccess(event, treeId, treeNode, msg) {
            showLog("[ "+getTime()+" onAsyncSuccess ]" + ((!!treeNode && !!treeNode.name) ? treeNode.name : "root") +", treeNode:"+treeNode);
            
        }
        function beforeExpand(treeId, treeNode){
        	showLog("[ "+getTime()+" beforeExpand ]" + ((!!treeNode && !!treeNode.name) ? treeNode.name : "root") +", treeNode:"+treeNode);
        	
        }
        function showLog(str) {
            //console.log(str);
        }
        function getTime() {
            var now= new Date(),
            h=now.getHours(),
            m=now.getMinutes(),
            s=now.getSeconds(),
            ms=now.getMilliseconds();
            return (h+":"+m+":"+s+ " " +ms);
        }

        function refreshNode(e) {
            showLog('refreshNode');
            var zTree = $.fn.zTree.getZTreeObj("categoryTree"),
            type = e.data.type,
            silent = e.data.silent,
            nodes = zTree.getSelectedNodes();
            if (nodes.length == 0) {
                alert("pls select parent node");
            }
            for (var i=0, l=nodes.length; i<l; i++) {
                zTree.reAsyncChildNodes(nodes[i], type, silent);
                if (!silent) zTree.selectNode(nodes[i]);
            }
        }

        var zNodes =[{}];

        $.fn.zTree.init($("#categoryTree"), setting, zNodes);
} );