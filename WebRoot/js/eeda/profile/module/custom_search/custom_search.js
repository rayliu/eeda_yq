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
	var colsDisplay = function(custom_search_cols){
		var custom_cols_table = colCont.dataTable;
        for (var i = 0; i < custom_search_cols.length; i++) {
            var field = custom_search_cols[i];
            custom_cols_table.row.add(field).draw(false);
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
            colsDisplay:colsDisplay
        };
    
});