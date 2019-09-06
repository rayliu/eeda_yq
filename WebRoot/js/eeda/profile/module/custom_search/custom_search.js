define(['jquery', './custom_search_source', './custom_search_cols', './custom_search_filter', './custom_search_sum_col'], 
  function ($, sourceCont, colCont, filterCont, sumColCont) {

	var buildDetail = function(){
		var dto = {};
		dto.custom_search_source = sourceCont.buildDetail();
		dto.custom_search_cols = colCont.buildDetail();
		dto.custom_search_sum_cols = sumColCont.buildDetail();
		dto.custom_search_filter = filterCont.buildDetail();
		dto.custom_filter_condition = $('#custom_filter_condition').val();
		return dto;
	}
	
	var sourceDisplay = function(custom_search_source){
		sourceCont.display(custom_search_source); 
	}
	var colsDisplay = function(custom_search_cols){
		colCont.display(custom_search_cols); 
	}
	var sumColsDisplay = function(custom_search_sum_cols){
		sumColCont.display(custom_search_sum_cols); 
	}
	var sourceConditionDisplay = function(custom_search_source_condition){
		if(custom_search_source_condition.length>0){
			sourceCont.tableDisplay(custom_search_source_condition); 
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
		  sumColCont.clear();
          filterCont.clear();
        }

        return {
            clear: clear,
            buildDetail: buildDetail,
            sourceDisplay:sourceDisplay,
            sourceConditionDisplay:sourceConditionDisplay,
			colsDisplay:colsDisplay,
			sumColsDisplay:sumColsDisplay,
            filterDisplay:filterDisplay
        };
    
});