define(['jquery', 'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
	$(document).ready(function() {

		var dataTable = eeda.dt({
          id: 'eeda_table',
          paging: true,
          aLengthMenu:[5,10],
          serverSide: true, 
          ajax: "/BusinessAdmin/ad/list",
          columns: [
            { "data": "BEGIN_DATE" ,"width": "100px"},
            { "data": "END_DATE","class":"title", "width": "100px"},
            { "data": "PRICE","width": "100px"},
            { "data": "PHONE","width": "100px" },
            { "data": "BEGIN_DATE", "width": "100px",
            	render: function(data,type,full,meta){
            		var data = "";
            		if(full.STATUS!="新建"){
            			data = '已审批'; 
            		}else{
            			data =  "<a class='stdbtn btn_blue editBtn' " +
              				" data-begin_date="+full.BEGIN_DATE+" " +
              				"data-end_date="+full.END_DATE+" data-price="+full.PRICE+" " +
              				"data-phone="+full.PHONE+"  data-status="+full.STATUS+" " +
              				" data-ad_location="+full.AD_LOCATION+" data-total_day="+full.TOTAL_DAY+" data-id="+full.ID+" href='#begin_date'>编辑</a>";
            	  	}
            		return data;
            	} 
            }
          ]
		});
		
	 var refleshTable = function(){
	    	  dataTable.ajax.url("/BusinessAdmin/ad/list").load();
	     }
	 $("#saveBtn").click(function(){
		 refleshTable();
	 })
		
	});
});