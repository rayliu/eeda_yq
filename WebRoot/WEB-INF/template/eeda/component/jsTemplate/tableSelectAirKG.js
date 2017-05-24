<script id="table_air_kg_field_template" type="text/html">
    <select class="form-control search-control air_kg" name="{{id}}" style="width:90px">
      <option {{if data == ''}}selected{{/if}}></option>
      <option value = 45 {{if value == '45'}}selected{{/if}}>+45</option>
      <option value = 100 {{if value == '100'}}selected{{/if}}>+100</option>
      <option value = 300 {{if value == '300'}}selected{{/if}}>+300</option>
      <option value = 500 {{if value == '500'}}selected{{/if}}>+500</option>
      <option value = 700 {{if value == '700'}}selected{{/if}}>+700</option>
      <option value = 1000 {{if value == '1000'}}selected{{/if}}>+1000</option>
      <option value = 2000 {{if value == '2000'}}selected{{/if}}>+2000</option>
    </select>
</script>
