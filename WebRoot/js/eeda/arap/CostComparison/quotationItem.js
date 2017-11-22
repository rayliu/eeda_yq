define(['jquery','metisMenu', 'template', 'sb_admin', 'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
    
var deletedTableIds=[];
//删除一行
$("#quotation_table").on('click', '.delete', function(){
    var tr = $(this).parent().parent();
    tr.css("display","none");
    deletedTableIds.push(tr.attr('id'))
}); 


        var quotationItemTable = eeda.dt({
            id: 'quotation_table',
            drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
            	
            },
            columns:[
                { "data": "ID", "visible": false},
                { "data":"ITEM_SP_ID","width":"90px",
	                "render": function ( data, type, full, meta ) {
	                    if(!data)
                        	data="";
                        var field_html = template('table_dropdown_template',
                            {
                                id: 'item_SP_ID',
                                value: data,//对应数据库字段
                                display_value: full.SP_ABBR,
                                style:'width:110px',
                                placeholder:'',
                                disabled:'disabled'
                            }
                         );
                        return field_html;
	                }
                },
                { "data": "CARRIER_ID", "width": "80px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        var field_html = template('table_dropdown_template',
                        {
	                         id:'CARRIER_ID',
	                         value:data,
	                         display_value:full.CARRIER_ABBR,
	                         style:'width:100px',
	                         placeholder:'',
	                         disabled:'disabled'
                        });
                        return field_html; 
                    }
                },
                { "data": "CONTRACT_NO","width": "60px",
                    "render": function ( data, type, full, meta ) {
                        return "<a href='/supplierContract/edit?id="+full.CONTRACT_ID+"' target='_blank' style='width:80px'>" + data+ "</a>"+
                               "<input type='hidden' id='contract_id' name='contract_id' value='"+full.CONTRACT_ID+"'>";
                    }
                },
                { "data": "TRANS_CLAUSE","width": "80px",
                    "render": function ( data, type, full, meta ) {
                        if(!data){
                        	data="";
                        }
                        return data+
                        "<input type='hidden' id='contact_loc_id' name='contact_loc_id' value='"+full.CONTACT_LOC_ID+"'>";
                    }
                },
                { "data": "TRADE_TYPE","width": "80px",
                    "render": function ( data, type, full, meta ) {
                        if(!data){
                        	data="";
                        }
                        return data+
                        "<input type='hidden' id='contact_item_id' name='contact_item_id' value='"+full.CONTACT_ITEM_ID+"'>";
                    }
                },
                { "data": "POR_ID", "width":"100px",
                    "render": function ( data, type, full, meta ) {
                    if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'POR_ID',
                               value: data,
                               display_value: full.POR_NAME,
                               style:'width:120px',
                               placeholder:'',
                               disabled:'disabled'
                           }
                       );
                       return field_html; 
                    }
                },
                { "data": "POL_ID", "width":"100px",
                    "render": function ( data, type, full, meta ) {
                    if(!data)
                           data='';
                       var field_html = template('table_dropdown_template',
                           {
                               id: 'POL_ID',
                               value: data,
                               display_value: full.POL_NAME,
                               style:'width:120px',
                               placeholder:'',
                               disabled:'disabled'
                           }
                       );
                       return field_html; 
                    }
                },
                { "data": "POD_ID", "width": "100px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        var field_html = template('table_dropdown_template',
                        {
	                         id:'POD_ID',
	                         value:data,
	                         display_value:full.POD_NAME,
	                         style:'width:120px',
	                         placeholder:'',
	                         disabled:'disabled'
                        });
                        return field_html; 
                    }
                },
                { "data": "FEE_ID", "width": "80px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        var field_html = template('table_dropdown_template',
                        {
                        	id:'FEE_ID',
                        	value:data,
                        	display_value:full.FEE_NAME,
                        	style:'width:100px',
                            placeholder:'',
                            disabled:'disabled'
                        	
                        });
                        return field_html; 
                    }
                },
                { "data": "PRICE", "width": "60px",
                    "render": function ( data, type, full, meta ) {
                    	 if(!data)
 	                        data='';
 	                    data = (parseFloat(data)).toFixed(2)
 	                    if(isNaN(data)){
 	                    	data=""
 	                    }
    	                    return '<input type="text" style="width:80px" name="price" disabled="disabled" value = "'+data+'" class="form-control notsave" >';
                    }
                },
                { "data": "CURRENCY_ID", "width": "50px",
                	"render": function ( data, type, full, meta ) {
                		 if(!data)
    	                        data='';
    	                    var field_html = template('table_dropdown_template',
    		                    {
    		                        id: 'CURRENCY_ID',
    		                        value: data,
    		                        display_value:full.CURRENCY_NAME,
    		                        style:'width:70px',
	                                placeholder:'',
	                                disabled:'disabled'
    		                    }
    		                );
    	                    return field_html;
                	}
                },
                { "data": "UOM", "width": "80px",
                	"render": function ( data, type, full, meta ) {
                		if(!data)
                			data='';
                		var field_html = template('table_dropdown_template',
            				{
    		            			id: 'UOM',
    		                        value: data,
    		                        display_value:full.UOM_NAME,
    		                        style:'width:100px',
	                                disabled:'disabled'
            				});
                		return field_html;
                	}
                },
                { "data": "CONTAINER_TYPE", "width": "50px",
                	"render": function ( data, type, full, meta ) {
                		 if(!data)
    	                        data='';
    	                    var str = '<select name= "container_type" class="form-control search-control" disabled style="width:70px">'
    	                    		  +'<option></option>'
    	                    		  +'<option value = "20GP" '+(data=='20GP'?'selected':'')+'>20GP</option>'
    	                    		  +'<option value = "40GP"'+(data=='40GP'?'selected':'')+'>40GP</option>'
    	                    		  +'<option value = "40HQ" '+(data=='40HQ'?'selected':'')+'>40HQ</option>'
    	                    		  +'<option value = "45GP"'+(data=='45GP'?'selected':'')+'>45GP</option>'
    	                    		  +'<select>';
    	                    return str;
                	}
                },
                { "data": "VOLUME1", "width": "50px",
                	"render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" disabled name="volume1" value="'+data+'" class="form-control" style="width:55px"/>';
                    }
                },
                { "data": "-","width": "5px",
                	"render": function ( data, type, full, meta ) {
                        
                        return "<span style='width:5px'>-</span>"
                    }
                },
                { "data": "VOLUME2", "width": "30px",
                	"render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" disabled name="volume2" value="'+data+'" class="form-control" style="width:50px"/>';
                    }
                },
                { "data": "GROSS_WEIGHT1", "width": "40px",
                    "render": function ( data, type, full, meta ) {
                    	 if(!data)
    	                        data='';
    	                    
    	                    return '<input type="text" disabled style="width:50px" name="gross_weight1" value = "'+data+'" class="form-control notsave" >';
                    }
                },
                { "data": "-","width": "5px",
                	"render": function ( data, type, full, meta ) {
                        
                        return "<span style='width:5px'>-</span>"
                    }
                },
                { "data": "GROSS_WEIGHT2", "width": "30px",
                    "render": function ( data, type, full, meta ) {
                    	 if(!data)
    	                        data='';
    	                    
    	                    return '<input type="text" disabled style="width:50px" name="gross_weight2" value = "'+data+'" class="form-control notsave" >';
                    }
                },
                {"data":"SP_ABBR","visible":false,
                	"render":function(data,type,full,meta){
                		return data;
                	}
                },
                {"data":"CARRIER_ABBR","visible":false,
                	"render":function(data,type,full,meta){
                		return data;
                	}
                },
                {"data":"POR_NAME","visible":false,
                	"render":function(data,type,full,meta){
                		return data;
                	}
                },
                {"data":"POL_NAME","visible":false,
                	"render":function(data,type,full,meta){
                		return data;
                	}
                },
                {"data":"POD_NAME","visible":false,
                	"render":function(data,type,full,meta){
                		return data;
                	}
                },
                {"data":"FEE_NAME","visible":false,
                	"render":function(data,type,full,meta){
                		return data;
                	}
                },
                {"data":"CURRENCY_NAME","visible":false,
                	"render":function(data,type,full,meta){
                		return data;
                	}
                },
                {"data":"UOM_NAME","visible":false,
                	"render":function(data,type,full,meta){
                		return "<input type='text' id='contact_item_id' name='contact_item_id' value='"+data+"'>";
                	}
                },
                {"data":"CONTRACT_ID","visible":false,
                	"render":function(data,type,full,meta){
                		return "<input type='text' id='contract_id' name='contract_id' value='"+data+"'>";
                	}
                },
                {"data":"CONTACT_LOC_ID","visible":false,
                	"render":function(data,type,full,meta){
                		return "<input type='text' id='contact_loc_id' name='contact_loc_id' value='"+data+"'>";
                	}
                },
                {"data":"CONTACT_ITEM_ID","visible":false,
                	"render":function(data,type,full,meta){
                		return "<input type='text' id='contact_item_id' name='contact_item_id' value='"+data+"'>";
                	}
                }
            ]
        });

      //添加新的明细
      $('#add_invoice_btn').click(function(){
    	  var item={};
          itemTable.row.add(item).draw(false);
      }) 
      //查询报价
      $('#quotation_btn').click(function(){
    	  itemOrder.searchShowItem();
      });
      
      var calcTotal=function() {
          //$("#ChargeOrder-table").DataTable()
          var CNY=0;
          var USD=0;
          var HKD=0;
          var JPY=0;

          //data table data 对象
          quotationItemTable.data().each(function(item, index) {
              var id = item.ID;
                  if(item.CURRENCY_NAME=='CNY'){
                      CNY+=item.PRICE;
                  }else if(item.CURRENCY_NAME=='USD'){
                      USD+=item.PRICE;
                  }else if(item.CURRENCY_NAME=='HKD'){
                      HKD+=item.PRICE;
                  }else if(item.CURRENCY_NAME=='JPY'){
                      JPY+=item.PRICE;
                  }
          });
          $('#cny').val(parseFloat(CNY).toFixed(2));
          $('#usd').val(parseFloat(USD).toFixed(2));
          $('#hkd').val(parseFloat(HKD).toFixed(2));
          $('#jpy').val(parseFloat(JPY).toFixed(2));

      }
      
      
      
      
      //查询报价
      itemOrder.searchShowItem = function(){ 
    	    var paraJson = itemOrder.spConditionJson();
    	    var sp_id_array=[];
    	    var Supplier_table_rows = $("#selectSupplier_table tr");
    	    for(var index=1; index<Supplier_table_rows.length; index++){
                var row = Supplier_table_rows[index];               
                var sp_id = $(row).find('[name=SP_ID]').val();
                sp_id_array.push(sp_id);
            }
    	    var sp_id_string =sp_id_array.toString();
		    var url = "/costComparison/searchShowItem?paraJson="+JSON.stringify(paraJson)
		     		 +"&sp_id_string="+sp_id_string
    	     quotationItemTable.ajax.url(url).load(calcTotal);
		}
      
      //刷新明细表
      itemOrder.refleshQuotationTable = function(order_id){
      	var url = "/costComparison/tableList?order_id="+order_id
          +"&table_type=quotationItem";
      	quotationItemTable.ajax.url(url).load();
      }
      
      itemOrder.buildquotationItem=function(){
          var cargo_table_rows = $("#quotation_table tr");
          var cargo_items_array=[];
          for(var index=0; index<cargo_table_rows.length; index++){
              if(index==0)
                  continue;

              var row = cargo_table_rows[index];
              var empty = $(row).find('.dataTables_empty').text();
              if(empty)
              	continue;
              
              var id = $(row).attr('id');
              if(!id){
                  id='';
              }
              
              var item={}
              item.id = id;
              for(var i = 0; i < row.childNodes.length; i++){
              	var el = $(row.childNodes[i]).find('input, select');
              	var name = el.attr('name');
              	if(el && name){
                  	var value = el.val();
                  	item[name] = value;
              	}
              }
              item.action = id.length > 0?'UPDATE':'CREATE';
              cargo_items_array.push(item);
          }
          
          for(var index=0; index<deletedTableIds.length; index++){
              var id = deletedTableIds[index];
              var item={
                  id: id,
                  action: 'DELETE'
              };
              cargo_items_array.push(item);
          }
          deletedTableIds = [];
          return cargo_items_array;
      };
      
    //显示地点条件
      var show_loc_concition=function(checkValue){
    	  if(checkValue=="ocean"){
    			$('#ocean_loc').show();
    			$('#por_div').show();
    			$('#land_loc').hide();
    			$('#pol').attr('port_type','port');
    			$('#pod').attr('port_type','port');
    			$('#quotation_table_head th').each(function(){
    				var head_name=$(this).text();
    				var i =$(this).index()+1;
    				if(i==3){
    					if(head_name!="船东(航空)公司"){
        					$('#quotation_table').dataTable().fnSetColumnVis(2, true);
        	    			$('#quotation_table').dataTable().fnSetColumnVis(6, true);
        				}
    				}
    				
    			});
    		}else if(checkValue=="air"){
    			$('#ocean_loc').show();
    			$('#land_loc').hide();
    			$('#por_div').hide();
    			$('#pol').attr('port_type','air_port');
    			$('#pod').attr('port_type','air_port');
    			$('#quotation_table_head th').each(function(){
    				var head_name=$(this).text();
    				var i =$(this).index()+1;
    				if(i==3){
    					if(head_name!="船东(航空)公司"){
    						$('#quotation_table').dataTable().fnSetColumnVis(2, true);
        	    			$('#quotation_table').dataTable().fnSetColumnVis(6, true);
        				}
    				}
    				
    			});
    			
    		}else if(checkValue=="land"){
    			$('#ocean_loc').hide();
    			$('#land_loc').show();
    			$('#quotation_table_head th').each(function(){
    				var head_name=$(this).text();
    				var i =$(this).index()+1;
    				if(i==3){
    					if(head_name!="船东(航空)公司"){
    						$('#quotation_table').dataTable().fnSetColumnVis(2, false);
    		    			$('#quotation_table').dataTable().fnSetColumnVis(6, false);
        				}
    				}
    				
    			});			
    		}else if(checkValue=="doorToPort"){
    			$('#ocean_loc').show();
    			$('#por_div').show();
    			$('#land_loc').show();
    			$('#pol').attr('port_type','port');
    			$('#pod').attr('port_type','port');    			
    			$('#quotation_table_head th').each(function(){
    				var head_name=$(this).text();
    				var i =$(this).index()+1;
    				if(i==3){
    					if(head_name!="船东(航空)公司"){
        					$('#quotation_table').dataTable().fnSetColumnVis(2, true);
        	    			$('#quotation_table').dataTable().fnSetColumnVis(6, true);
        				}
    				}
    				
    			});
    		}
      }
      
      //服务项目回显
      var radioVal = $('#service_typeRadio').val();
      $('#billing_method input[type="radio"]').each(function(){
      	var checkValue = $(this).val();
      	if(radioVal==checkValue){
      		$(this).attr("checked",true);
      	    //显示地点条件
      		show_loc_concition(checkValue);
      	}
      });
      
      //显示地点条件
      $('#billing_method input[name=service_type]').change(function(){
    	  var checkValue=$('input[name=service_type]:checked').val();
    	  show_loc_concition(checkValue);
      });
      
      
      
      
      

});