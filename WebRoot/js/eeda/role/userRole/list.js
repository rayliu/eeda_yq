define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap'], function ($, metisMenu) {
	    $(document).ready(function(){
	  	 
	    document.title = '用户岗位查询 | '+document.title;
	    $('#menu_sys_profile').addClass('active').find('ul').addClass('in');
	  //------------事件处理
	    var cargoTable =eeda.dt({
	  	  id:'table',
	  	  paging: true,
	        serverSide: true, //不打开会出现排序不对
	        ajax: "/userRole/list",
		      columns:[
					{ "data":null,
						"render":function(data, type, full, meta){
		            		 if(userRole.update){
		            			 return "<a href ='/userRole/edit?username=" + data.USER_NAME + "' target='_blank'>" + data.USER_NAME + "</a>";
		            		}else{ 
		            			return data.USER_NAME;
		            		 } 
		            	}
					},
		            { "data":"C_NAME"},
		          	{ "data":"NAME"},
		            { "data":"REMARK"},
		            { "data":null,
		         		"Width": "8%",
		         		"Visible":(userRole.update || userRole.query),
			   	         "render": function(data, type, full, meta){  
			   	        	 var str = "<nobr>";
			   	        	 if(data.ROLE_CODE != "admin"){
			   	        		  if(userRole.update){
			   	        			  str = str + "<a target='_blank' class='btn  btn-sm btn-primary' href='/userRole/edit?username=" + data.USER_NAME + "' >"+
			                          			"<i class='fa fa-edit fa-fw'></i>编辑</a> ";
			   	        		  }  
				   	         }
			   	        	return str + "</nobr>" ;
			   	         }
			   	 	}
		      	]
			});
	    }) 
	})		