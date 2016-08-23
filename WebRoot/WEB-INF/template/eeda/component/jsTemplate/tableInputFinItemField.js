<script id="table_fin_item_field_template" type="text/html">
        <div class="form-group">
            <input type="text" name="{{id}}" value="{{value}}" style="display:none;"/>
            <input type="text" class="form-control search-control" 
                   name="{{id}}_input" placeholder="请选择" value="{{display_value}}" style="width:{{width}}">
        </div> 
</script>
<ul id='table_fin_item_field_list' tabindex="-1" 
    class="pull-right dropdown-menu default dropdown-scroll" 
    style="top: 22%; left: 33%; width: 20%;">
</ul>