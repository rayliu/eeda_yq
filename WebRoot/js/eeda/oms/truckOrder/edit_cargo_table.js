define([ 'jquery', 'metisMenu', 'sb_admin', 'dataTablesBootstrap','validate_cn', 'sco' ],
function($, metisMenu) {
	$(document).ready(function() {

								var deletedTableIds = [];

								// 删除一行
								$("#cargo_table").on('click', '.delete',
										function(e) {
											e.preventDefault();
											var tr = $(this).parent().parent();
											deletedTableIds.push(tr.attr('id'))

											cargoTable.row(tr).remove().draw();
										});

								// 添加一行
								$('#add_cargo').on('click', function() {
									var item = {};
									cargoTable.row.add(item).draw(true);
								});

								// 刷新明细表
								itemOrder.refleshCargoTable = function(order_id) {
									var url = "/truckOrder/tableList?order_id="
											+ order_id + "&type=cargo";
									cargoTable.ajax.url(url).load();
								}

								itemOrder.buildCargoDetail = function() {
									var cargo_table_rows = $("#cargo_table tr");
									var cargo_items_array = [];
									for (var index = 0; index < cargo_table_rows.length; index++) {
										if (index == 0)
											continue;

										var row = cargo_table_rows[index];
										var empty = $(row).find(
												'.dataTables_empty').text();
										if (empty)
											continue;

										var id = $(row).attr('id');
										if (!id) {
											id = '';
										}

										var item = {}
										item.id = id;
										for(var i = 1; i < row.childNodes.length; i++){
								            	var el = $(row.childNodes[i]).find('input, select');
								            	var name = el.attr('name'); //name='abc'
								            	
								            	if(el && name){
								                	var value = el.val();//元素的值
								                	item[name] = value;
								            	}
								            }
										
										item.action = id.length > 0 ? 'UPDATE':'CREATE';
										cargo_items_array.push(item);
									}

									// add deleted items
									for (var index = 0; index < deletedTableIds.length; index++) {
										var id = deletedTableIds[index];
										var item = {
											id : id,
											action : 'DELETE'
										};
										cargo_items_array.push(item);
									}
									deletedTableIds = [];
									return cargo_items_array;
								};

								// ------------事件处理
								var cargoTable = $('#cargo_table')
										.DataTable(
												{
													"processing" : true,
													"searching" : false,
													"paging" : false,
													"info" : false,
													"scrollX" : true,
													"autoWidth" : true,
													"language" : {
														"url" : "/yh/js/plugins/datatables-1.10.9/i18n/Chinese.json"
													},
													"createdRow" : function(
															row, data, index) {
														$(row).attr('id',
																data.ID);
													},
													"columns" : [
															{
																"width" : "30px",
																"render" : function(
																		data,
																		type,
																		full,
																		meta) {
																	return '<button type="button" class="delete btn btn-default btn-xs">删除</button> ';
																}
															},
												            { "data": "CONTAINER_TYPE", 
												                "render": function ( data, type, full, meta ) {
												                    if(!data)
												                        data='';
												                    var str = '<select name="container_type" class="form-control search-control">'
												                    			+'<option></option>'
															                   +'<option value="20GP" '+(data=='20GP' ? 'selected':'')+'>20GP</option>'
															                   +'<option value="40GP" '+(data=='40GP' ? 'selected':'')+'>40GP</option>'
															                   +'<option value="45GP" '+(data=='45GP' ? 'selected':'')+'>45GP</option>'
															                   +'</select>';
												                    return str;
												                }
												            },
															{
																"data" : "CONTAINER_AMOUNT",
																"render" : function(
																		data,
																		type,
																		full,
																		meta) {
																	if (!data)
																		data = '';
																	return '<input type="text" name="container_amount" value="'
																			+ data
																			+ '" class="form-control" />';
																}
															},
															{
																"data" : "CARGO_NAME",
																"render" : function(
																		data,
																		type,
																		full,
																		meta) {
																	if (!data)
																		data = '';
																	return '<input type="text" name="cargo_name" value="'
																			+ data
																			+ '" class="form-control" />';
																}
															},
															{
																"data" : "PIECES",
																"render" : function(
																		data,
																		type,
																		full,
																		meta) {
																	if (!data)
																		data = '';
																	return '<input type="text" name="pieces" value="'
																			+ data
																			+ '" class="form-control" />';
																}
															},
															{
																"data" : "VOLUME",
																"render" : function(
																		data,
																		type,
																		full,
																		meta) {
																	if (!data)
																		data = '1';
																	return '<input type="number" name="volume" value="'
																			+ data
																			+ '" class="form-control easyui-numberbox" data-options="max:0"/>';
																}
															},
															{
																"data" : "NET_WEIGHT",
																"render" : function(
																		data,
																		type,
																		full,
																		meta) {
																	if (!data)
																		data = '';
																	return '<input type="text" name="net_weight" value="'
																			+ data
																			+ '" class="form-control" />';
																}
															},
															{
																"data" : "GROSS_WEIGHT",
																"render" : function(
																		data,
																		type,
																		full,
																		meta) {
																	if (!data)
																		data = '1';
																	return '<input type="text" name="gross_weight" value="'
																			+ data
																			+ '" class="form-control" />';
																}
															},
															{
																"data" : "REMARK",
																"render" : function(
																		data,
																		type,
																		full,
																		meta) {
																	if (!data)
																		data = '0';
																	return '<input type="text" name="remark" value="'
																			+ data
																			+ '" class="form-control" />';
																}
															} ]
												});

							});

		});