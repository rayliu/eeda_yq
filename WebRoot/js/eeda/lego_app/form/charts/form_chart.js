define(['jquery', './bar', './line','./pie'], function ($, bar, line, pie) {

    var charts = $('.eeda_chart_container');
    charts.each(function(index, chart_container){
        var form_name = $(chart_container).attr('form_name');
        var form_id = form_name.split('_')[1];
        $.post('/form/chart',{form_id: form_id}, function(data){
            var chart_type = data.CHART_TYPE;
            switch (chart_type) {
                case 'bar':
                    bar.show(chart_container, data);
                    break;
                case 'line':
                    line.show(chart_container, data);
                    break;
                case 'pie':
                    pie.show(chart_container, data);
                    break;
                case 'scatter':
                    
                    break;
                default:
                    break;
            }
        });
    });
    
});
