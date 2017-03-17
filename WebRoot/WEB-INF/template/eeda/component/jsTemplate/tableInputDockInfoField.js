//本文件应用于tms  jobOrder的选择车牌带出对应的司机, 电话
<script id="table_dock_no_field_template" type="text/html">
        <div class="form-group">
            <input type="text" name="{{id}}" value="{{value}}"  style="display:none;"/>
            <input type="text" class="form-control search-control" 
                   name="{{id}}_input" placeholder="请选择" value="{{display_value}}">
        </div> 
</script>
<ul id='table_dock_no_field_list' tabindex="-1" 
    class="pull-right dropdown-menu default dropdown-scroll datatable_dropdown_menu" 
    style="top: 22%; left: 33%; width: 50px;">
</ul>
