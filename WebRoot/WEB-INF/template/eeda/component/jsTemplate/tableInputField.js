<script id="table_dropdown_template" type="text/html">
        <div class="form-group">
            <input type="text" name="{{id}}" value="{{value}}" style="display:none;"/>
            {{if placeholder==''}} 
	            <input type="text" {{disabled}} class="form-control search-control" 
	                   name="{{id}}_input" placeholder="{{placeholder}}" value="{{display_value}}" style="{{style}}" >
            {{else}}
	            <input type="text" {{disabled}} class="form-control search-control" 
	                name="{{id}}_input" placeholder="请选择" value="{{display_value}}" style="{{style}}" >
            {{/if}} 
        </div> 
</script>
<ul id='table_input_field_list' tabindex="-1" 
    class="pull-right dropdown-menu dropdown-scroll datatable_dropdown_menu" 
    style="left: 33%; width: 20%;">
</ul>
