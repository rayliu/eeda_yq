define(['jquery','metisMenu', 'template', 'sb_admin', 'dataTablesBootstrap', 'validate_cn', 'sco'], function ($, metisMenu, template) {
    
		var deletedTableIds=[];
		//删除一行
		$("#selectSupplier_table").on('click', '.delete', function(){
		    var tr = $(this).parent().parent();
		    deletedTableIds.push(tr.attr('id'));		    
		    supplierTable.row(tr).remove().draw();
		}); 
		//condition条件
		itemOrder.spConditionJson=function(){
			  var condition_json={};
			  condition_json.service_typeRadio=$('#billing_method input[type="radio"]:checked').val();
			  condition_json.trans_clause =$('#trans_clause').val();
			  condition_json.trade_type =$('#trade_type').val();
			  condition_json.pickup_loc =$('#pickup_loc').val();
			  condition_json.delivery_loc =$('#delivery_loc').val();
			  condition_json.por =$('#por').val();
			  condition_json.pol =$('#pol').val();
			  condition_json.pod =$('#pod').val();
			  return condition_json;
		} 
		
		//获取SP的table Item ID
		

		var bingFieldEevent = function(){
			eeda.bindTableField('selectSupplier_table','SP_ID','/costComparison/searchSpcomparison','spConditionJson');
		}
        var supplierTable = eeda.dt({
            id: 'selectSupplier_table',
            drawCallback: function( settings ) {//生成相关下拉组件后, 需要再次绑定事件
            	bingFieldEevent();
            },
            columns:[
                { "width": "60px",
	                "render": function ( data, type, full, meta ) {
	                	return '<button type="button" class="delete btn table_btn delete_btn btn-xs" style="width:70px">删除</button>';
	                }
                },
                { "data": "SP_ID", "width": "100px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                        	data="";
                        var field_html = template('table_dropdown_template',
                            {
                                id: 'SP_ID',
                                value: data,//对应数据库字段
                                display_value: full.SP_NAME,
                                style:'width:120px',
                            }
                         );
                        return field_html;
                    }
                },
                { "data": "SP_TYPE","width":"300px",
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return '<input type="text" name="SP_TYPE" value="'+data+'" class="form-control" style="width:320px"/>';
                    }
                },
                { "data": "SP_NAME","visible":false,
                    "render": function ( data, type, full, meta ) {
                        if(!data)
                            data='';
                        return data;
                    }
                }
            ]
        });

      //添加新的明细
      $('#supplier_btn').click(function(){
    	  var item={};
          supplierTable.row.add(item).draw(false);
      }) 
      
      $('#table_input_field_list').on('click', '.fromLocationItem', function(){
    	  var sp_type= $(this).attr("sp_type");
    	  var str = "";
    	  typeArr = sp_type.split(";");          
          $.each(typeArr, function(index, val) {
         	 //line;delivery;pickup;personal;carrier;air;broker;head_car;oversea_agent
              if(val == "line"){
                  str += "干线运输供应商;";
              }else if(val == "delivery"){
                  str += "配送供应商;";
              }else if(val == "pickup"){
                  str += "提货供应商;";
              }else if(val == "personal"){
                  str += "个体供应商;";
              }else if(val == "carrier"){
             	 str += "船公司;";
              }else if(val == "air"){
             	 str += "航空公司;";
              }else if(val == "broker"){
             	 str += "报关行;";
              }else if(val == "head_car"){
             	 str += "头程船公司;";
              }else if(val == "oversea_agent"){
             	 str += "海外代理;";
              }else if(val == "booking_agent"){
                  str += "订舱代理;";
              }else if(val == "truck"){
                  str += "运输公司;";
              }else if(val == "cargo_agent"){
                  str += "货代公司;";
              }else if(val == "manufacturer"){
                  str += "生产商;";
              }else if(val == "traders"){
                  str += "贸易商;";
              }else if(val == "port_supervision"){
             	 str +="港口监管;";
              }else if(val == "wharf"){
             	 str +="码头;";
              }else if(val == "warehouse"){
             	 str +="仓储;";
              }
          });
    	  eeda._inputField.parent().parent().parent().find('[name=SP_TYPE]').val(str);
      });

      
      
    //刷新明细表
      itemOrder.refleshSupplierTable = function(order_id){
      	var url = "/costComparison/tableList?order_id="+order_id
          +"&table_type=supplierItem";
      	supplierTable.ajax.url(url).load();
      }
      
      
      
      
      itemOrder.buildSelectSupplierItem=function(){
          var cargo_table_rows = $("#selectSupplier_table tr");
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
              for(var i = 1; i < row.childNodes.length; i++){
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

});