<script id="table_carrier_field_template" type="text/html">
        <div class="form-group">
            <input type="text" name="{{id}}" value="{{value}}" field_type='carrier_id' style="display:none;"/>
            <input type="text" class="form-control search-control" 
                   name="carrier_input" placeholder="请选择" value="{{display_value}}">
        </div> 
</script>
<ul id='table_carrier_field_list' tabindex="-1" 
    class="pull-right dropdown-menu default dropdown-scroll" 
    style="top: 22%; left: 33%;">
</ul>