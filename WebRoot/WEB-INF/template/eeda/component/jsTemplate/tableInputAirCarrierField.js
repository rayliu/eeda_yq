<script id="table_air_carrier_field_template" type="text/html">
        <div class="form-group">
            <input type="text" name="{{id}}" value="{{value}}" field_type='air_carrier_id' style="display:none;"/>
            <input type="text" class="form-control search-control" 
                   name="air_carrier_input" placeholder="请选择" value="{{display_value}}">
        </div> 
</script>
<ul id='table_air_carrier_field_list' tabindex="-1" 
    class="pull-right dropdown-menu default dropdown-scroll datatable_dropdown_menu" 
    style="top: 22%; left: 33%; width: 20%;">
</ul>