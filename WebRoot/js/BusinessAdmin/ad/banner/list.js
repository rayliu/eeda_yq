define(['jquery', 'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
	$(document).ready(function() {

		var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: true,
          serverSide: true, 
          ajax: "/BusinessAdmin/ad/bannerList",
          columns: [
            { "data": "AD_LOCATION" ,"width": "100px",
            	render: function(data,type,full,meta){
            		return "广告位"+data;
            	}
            },
            { "data": "BEGIN_DATE" ,"width": "100px"},
            { "data": "END_DATE","class":"", "width": "100px"},
            { "data": "TOTAL_DAY","width": "100px"},
            { "data": "PRICE","width": "100px"},
            { "data": "TOTAL_PRICE","width": "100px"},
            { "data": "PHONE","width": "100px" },
            { "data": "BEGIN_DATE",
            	render: function(data,type,full,meta){
            		var data = "";
            		if(full.STATUS == "新建"){
            			data =  "<a class='stdbtn btn_blue editBtn' " +
              				" begin_date="+full.BEGIN_DATE+
              				" end_date="+full.END_DATE+
              				" price="+full.PRICE+
              				" phone="+full.PHONE+
              				" status="+full.STATUS+
              				" ad_location="+full.AD_LOCATION+
              				" total_day="+full.TOTAL_DAY+
              				" id="+full.ID+
              				" remark="+full.REMARK+
              				" total_price="+full.TOTAL_PRICE+
              				" href='#begin_date'>编辑</a>";
            	  	}else{
            	  		data = full.STATUS;
            	  	}
            		return data;
            	} 
            }
          ]
		});
		
		itemOrder.refleshTable = function(){
			dataTable.ajax.url("/BusinessAdmin/ad/bannerList").load();
		}
		
	});
});