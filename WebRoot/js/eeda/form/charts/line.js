define(['jquery', 'echarts'], function ($, ec) {
    var show=function(container, data){
        // 基于准备好的dom，初始化echarts实例
        var myChart = ec.init(container);

        // 指定图表的配置项和数据
        line_option = {
            title: {
                text: '单据总量'
            },
            xAxis: {
                type: 'category',
                data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
            },
            yAxis: {
                type: 'value'
            },
            series: [{
                data: [820, 932, 901, 934, 1290, 1330, 1320],
                type: 'line'
            }]
        };

        // 使用刚指定的配置项和数据显示图表。
        myChart.setOption(line_option,{notMerge:true});
    }

    return {
        show:show
    }
});
