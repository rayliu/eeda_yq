define(['jquery', 'metisMenu', 'sb_admin',  'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu) {
$(document).ready(function() {
	document.title = '城市查询 | '+document.title;
	
	$('#menu_profile').addClass('active').find('ul').addClass('in');
	        eeda.dt({
	            id: 'example',
	            paging: true,
	            serverSide: true, //不打开会出现排序不对 
	            ajax: "/location/listLocation",
	            columns: [
						{ "data": "PROVINCE" },
						{ "data": "CITY" },
						{ "data": "DISTRICT" },
						{ "data": "NAME" }
						]
					});
});
});