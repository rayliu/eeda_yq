define(['jquery', './custom_search_source', './custom_search_cols', './custom_search_filter'], 
  function ($, sourceCont, colCont, filterCont) {

	var buildDetail = function(){
		var dto = {};
		dto.custom_search_source = sourceCont.buildDetail();
		dto.custom_search_cols = colCont.buildDetail();
		dto.custom_search_filter = filterCont.buildDetail();
		return dto;
	}
	
	var sourceDisplay = function(custom_search_source){
		sourceCont.display(custom_search_source); 
	}
	var sourceConditionDisplay = function(custom_search_source_condition){
		if(custom_search_source_condition.length>0){
			sourceCont.tableDisplay(custom_search_source_condition); 
		}
	}
	var colsDisplay = function(custom_search_cols){
		var custom_cols_table = colCont.dataTable;
        for (var i = 0; i < custom_search_cols.length; i++) {
            var field = custom_search_cols[i];
            custom_cols_table.row.add(field).draw(false);
        }
	}
	var filterDisplay = function(custom_search_filter){
		var custom_filter_table = filterCont.dataTable;
		for (var i = 0; i < custom_search_filter.length; i++) {
            var field = custom_search_filter[i];
            custom_filter_table.row.add(field).draw(false);
        }
	}

        var clear = function() {
          sourceCont.clear(); 
          colCont.clear(); 
          filterCont.clear(); 
        }

        return {
            clear: clear,
            buildDetail: buildDetail,
            sourceDisplay:sourceDisplay,
            sourceConditionDisplay:sourceConditionDisplay,
            colsDisplay:colsDisplay,
            filterDisplay:filterDisplay
        };
    
});