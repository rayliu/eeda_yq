define(['jquery', 'metisMenu', 'sb_admin', 'dataTablesBootstrap', 'validate_cn',  'dtColReorder'], function ($, metisMenu) {

    $(document).ready(function() {
    	document.title = '重要客户提醒 | '+document.title;
    	$('#breadcrumb_li').text("重要客户提醒");
    	
    	$('#menu_profile').addClass('active').find('ul').addClass('in');

      /*  var dataTable = eeda.dt({
            id: 'eeda-table',
            paging: true,
            serverSide: true, //不打开会出现排序不对
            ajax: "/customerRemind/list",
            columns: [
                { "data": "group_abbr",
                	"render": function ( data, type, full, meta ) {
                		if()
                			var abbrs=full.GROUP_ABBR.split(",");
                			for(var i=0;i<abbrs.length;i++){
                				abbr=abbrs[i];

    	                        if(full.DAYS==7){
    	                             return abbr;
    	                         }else{
    	                             return "";
    	                         }
                			}
                    }
                },
                { "data": "group_abbr",
                	"render": function ( data, type, full, meta ) {
                        if(full.DAYS==15){
                             return full.GROUP_ABBR;
                         }else{
                             return "";
                         }
                    }
                },
                { "data": "group_abbr",
                	"render": function ( data, type, full, meta ) {
                        if(full.DAYS==30){
                             return full.GROUP_ABBR;
                         }else{
                             return "";
                         }
                    }
                },
                { "data": "group_abbr",
                	"render": function ( data, type, full, meta ) {
                        if(full.DAYS==90){
                             return full.GROUP_ABBR;
                         }else{
                             return "";
                         }
                    }
                },
                { "data": "group_abbr",
                	"render": function ( data, type, full, meta ) {
                        if(full.DAYS==180){
                             return full.GROUP_ABBR;
                         }else{
                             return "";
                         }
                    }
                }
            ]
        });*/
    	
    	
    	//添加数据
    	var abbrs7='',abbrs15='',abbrs30='',abbrs90='',abbrs180='';
    	var days7=$.ajax({url:"/customerRemind/list1?customer_remind=7",async:false});
    	var days15=$.ajax({url:"/customerRemind/list1?customer_remind=15",async:false});
    	var days30=$.ajax({url:"/customerRemind/list1?customer_remind=30",async:false});
    	var days90=$.ajax({url:"/customerRemind/list1?customer_remind=90",async:false});
    	var days180=$.ajax({url:"/customerRemind/list1?customer_remind=180",async:false});
    	 
    	 if(days7.responseJSON.data[0].GROUP_ABBR!=null&&days7.responseJSON.data[0].GROUP_ABBR!=''){
    		 abbrs7=days7.responseJSON.data[0].GROUP_ABBR.split(",");
    	 }
    	 if(days15.responseJSON.data[0].GROUP_ABBR!=null&&days15.responseJSON.data[0].GROUP_ABBR!=''){
    		 abbrs15=days15.responseJSON.data[0].GROUP_ABBR.split(",");
    	 }
    	 if(days30.responseJSON.data[0].GROUP_ABBR!=null&&days30.responseJSON.data[0].GROUP_ABBR!=''){
    		 abbrs30=days30.responseJSON.data[0].GROUP_ABBR.split(",");
    	 }
    	 if(days90.responseJSON.data[0].GROUP_ABBR!=null&&days90.responseJSON.data[0].GROUP_ABBR!=''){
    		 abbrs90=days90.responseJSON.data[0].GROUP_ABBR.split(",");
    	 }
    	 if(days180.responseJSON.data[0].GROUP_ABBR!=null&&days180.responseJSON.data[0].GROUP_ABBR!=''){
    		 abbrs180=days180.responseJSON.data[0].GROUP_ABBR.split(",");
    	 }
//    	$('#eeda-table').a
    	var customers;
    	
    	$($('#eeda-table tbody tr')[0]).append('<td>'+abbrs7+'</td>');
    	$($('#eeda-table tbody tr')[0]).append('<td>'+abbrs15+'</td>');
    	$($('#eeda-table tbody tr')[0]).append('<td>'+abbrs30+'</td>');
    	$($('#eeda-table tbody tr')[0]).append('<td>'+abbrs90+'</td>');
    	$($('#eeda-table tbody tr')[0]).append('<td>'+abbrs180+'</td>');
    	
    	
    	
    });
});