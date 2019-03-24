define(['jquery', 'echarts'], function ($, ec) {

    var show=function(container, data){
        // 基于准备好的dom，初始化echarts实例
        var myChart = ec.init(container);

        // 指定图表的配置项和数据
        var bar_option = {
            title: {
                text: '品类销量'
            },
            tooltip: {},
            legend: {
                data:['销量']
            },
            xAxis: {
                data: ["衬衫","羊毛衫","雪纺衫","裤子","高跟鞋","袜子"]
            },
            yAxis: {},
            series: [{
                name: '销量',
                type: 'bar',
                data: [5, 20, 36, 10, 10, 20]
            }]
        };

        // 使用刚指定的配置项和数据显示图表。
        myChart.setOption(bar_option,{notMerge:true});
    }

    return {
        show:show
    }
});
