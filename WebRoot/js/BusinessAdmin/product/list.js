define(['jquery', 'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
  $(document).ready(function() {

      var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: false,
          serverSide: true, 
          ajax: "/BusinessAdmin/product/list",
          columns: [
            { "data": "COVER" ,"width": "80px",
              render: function(data,type,full,meta){
                var str = "<img style='width: 100px; height: 75px;' src='http://via.placeholder.com/200x150'>";
                if(data)
                  str ="<img style='width: 100px; height: 75px;' src='"+data+"'>";
                return str;
              }
            },
            { "data": "NAME", "width": "100px",
              render:function(data,type,full,meta){
                   return "<a href='/BusinessAdmin/product/edit?id="+full.ID+"'>"+data+"</a>";
              }
            },
            { "data": "PRICE","width": "100px"},
            { "data": "IS_ACTIVE","width": "100px",
              render:function(data,type,full,meta){
                if(data && data == 'N'){
                  return "未上架 <a class='stdbtn btn_yellow' href='/BusinessAdmin/product/setActive?id="+full.ID+"&flag=Y'>上架</a>";
                }else{
                  return "已上架 <a class='stdbtn btn_yellow' href='/BusinessAdmin/product/setActive?id="+full.ID+"&flag=Y'>下架</a>";
                }
              }
            },
            { "data": "CU_FLAG", "width": "100px",
              render:function(data,type,full,meta){
                if(data && data == 'N'){
                  return "<input type='checkbox'>";
                }else{
                  return "<input type='checkbox' checked>";
                }
              }
            },
            { "data": "SEQ", "width": "100px"},
            { "data": null, "width": "100px",
            	"render":function(data,type,full,meta){
	            	   var str = '<input class="stdbtn btn_red" type="button" value="删除" >';

	            	   return str;
	             }
            }
          ]
	    });
  });
});